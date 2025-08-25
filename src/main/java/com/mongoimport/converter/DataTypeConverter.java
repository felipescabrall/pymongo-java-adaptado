package com.mongoimport.converter;

import com.mongoimport.model.DataType;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Classe responsável por converter valores de string para os tipos apropriados do MongoDB.
 */
public class DataTypeConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(DataTypeConverter.class);
    
    // Padrões para detecção automática de tipos
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("^-?\\d*\\.\\d+([eE][+-]?\\d+)?$");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false|yes|no|1|0)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern OBJECTID_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");
    
    // Formatadores de data comuns
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_ZONED_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };
    
    /**
     * Converte um valor string para o tipo especificado.
     * 
     * @param value Valor a ser convertido
     * @param dataType Tipo de destino
     * @param parseGrace Modo de tolerância a erros
     * @return Valor convertido ou valor original em caso de erro (dependendo do parseGrace)
     */
    public static Object convertValue(String value, DataType dataType, String parseGrace) {
        if (value == null) {
            return null;
        }
        
        // Remover espaços em branco
        value = value.trim();
        
        // Se valor está vazio, retornar null ou string vazia dependendo do tipo
        if (value.isEmpty()) {
            return dataType == DataType.STRING ? "" : null;
        }
        
        try {
            return switch (dataType) {
                case STRING -> value;
                case INT32 -> convertToInt32(value);
                case INT64 -> convertToInt64(value);
                case DOUBLE -> convertToDouble(value);
                case DECIMAL -> convertToDecimal(value);
                case BOOLEAN -> convertToBoolean(value);
                case DATE -> convertToDate(value);
                case OBJECTID -> convertToObjectId(value);
                case BINDATA -> convertToBinary(value);
                case AUTO -> autoDetectAndConvert(value);
            };
        } catch (Exception e) {
            return handleConversionError(value, dataType, parseGrace, e);
        }
    }
    
    /**
     * Detecta automaticamente o tipo de um valor e o converte.
     */
    private static Object autoDetectAndConvert(String value) {
        // Tentar ObjectId primeiro (mais específico)
        if (OBJECTID_PATTERN.matcher(value).matches()) {
            try {
                return new ObjectId(value);
            } catch (Exception ignored) {}
        }
        
        // Tentar boolean
        if (BOOLEAN_PATTERN.matcher(value).matches()) {
            return convertToBoolean(value);
        }
        
        // Tentar inteiro
        if (INTEGER_PATTERN.matcher(value).matches()) {
            try {
                long longValue = Long.parseLong(value);
                // Se cabe em int32, usar int32
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                } else {
                    return longValue;
                }
            } catch (NumberFormatException ignored) {}
        }
        
        // Tentar double
        if (DOUBLE_PATTERN.matcher(value).matches()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {}
        }
        
        // Tentar data
        Date date = tryParseDate(value);
        if (date != null) {
            return date;
        }
        
        // Se nada funcionou, retornar como string
        return value;
    }
    
    private static Integer convertToInt32(String value) {
        return Integer.parseInt(value);
    }
    
    private static Long convertToInt64(String value) {
        return Long.parseLong(value);
    }
    
    private static Double convertToDouble(String value) {
        return Double.parseDouble(value);
    }
    
    private static Decimal128 convertToDecimal(String value) {
        return new Decimal128(new BigDecimal(value));
    }
    
    private static Boolean convertToBoolean(String value) {
        String lowerValue = value.toLowerCase();
        return switch (lowerValue) {
            case "true", "yes", "1" -> true;
            case "false", "no", "0" -> false;
            default -> throw new IllegalArgumentException("Valor booleano inválido: " + value);
        };
    }
    
    private static Date convertToDate(String value) {
        Date date = tryParseDate(value);
        if (date == null) {
            throw new IllegalArgumentException("Formato de data inválido: " + value);
        }
        return date;
    }
    
    private static Date tryParseDate(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // Tentar como LocalDateTime primeiro
                LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                return java.sql.Timestamp.valueOf(localDateTime);
            } catch (DateTimeParseException ignored) {
                try {
                    // Tentar como ZonedDateTime
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(value, formatter);
                    return Date.from(zonedDateTime.toInstant());
                } catch (DateTimeParseException ignored2) {}
            }
        }
        
        // Tentar timestamp Unix
        try {
            long timestamp = Long.parseLong(value);
            // Se parece com timestamp Unix (segundos)
            if (timestamp > 0 && timestamp < 4000000000L) {
                return new Date(timestamp * 1000);
            }
            // Se parece com timestamp Unix (milissegundos)
            if (timestamp > 1000000000000L) {
                return new Date(timestamp);
            }
        } catch (NumberFormatException ignored) {}
        
        return null;
    }
    
    private static ObjectId convertToObjectId(String value) {
        return new ObjectId(value);
    }
    
    private static byte[] convertToBinary(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Dados binários Base64 inválidos: " + value, e);
        }
    }
    
    /**
     * Trata erros de conversão baseado no modo parseGrace.
     */
    private static Object handleConversionError(String value, DataType dataType, String parseGrace, Exception error) {
        return switch (parseGrace.toLowerCase()) {
            case "autocast" -> {
                logger.debug("[DEBUG] Falha na conversão para {}, tentando auto-detecção: {}", dataType, value);
                yield autoDetectAndConvert(value);
            }
            case "skipfield" -> {
                logger.debug("[DEBUG] Pulando campo devido a erro de conversão: {}", value);
                yield null;
            }
            case "skiprow" -> {
                logger.debug("[DEBUG] Erro de conversão que requer pular linha: {}", value);
                throw new ConversionException("Erro de conversão que requer pular linha: " + error.getMessage(), error);
            }
            case "stop" -> {
                logger.error("[ERROR] Parando importação devido a erro de conversão: {}", error.getMessage());
                throw new ConversionException("Erro de conversão: " + error.getMessage(), error);
            }
            default -> {
                logger.warn("[WARNING] Modo parseGrace desconhecido '{}', usando autoCast", parseGrace);
                yield autoDetectAndConvert(value);
            }
        };
    }
    
    /**
     * Exception específica para erros de conversão.
     */
    public static class ConversionException extends RuntimeException {
        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
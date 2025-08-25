package com.mongoimport.model;

/**
 * Enum que define os tipos de dados suportados para conversão durante a importação.
 * Baseado nos tipos suportados pelo mongoimport oficial do MongoDB.
 */
public enum DataType {
    /**
     * Tipo string - mantém o valor como texto
     */
    STRING,
    
    /**
     * Tipo inteiro de 32 bits
     */
    INT32,
    
    /**
     * Tipo inteiro de 64 bits
     */
    INT64,
    
    /**
     * Tipo double (ponto flutuante de dupla precisão)
     */
    DOUBLE,
    
    /**
     * Tipo decimal de alta precisão (Decimal128)
     */
    DECIMAL,
    
    /**
     * Tipo booleano
     */
    BOOLEAN,
    
    /**
     * Tipo data/hora (ISO 8601)
     */
    DATE,
    
    /**
     * Tipo ObjectId do MongoDB
     */
    OBJECTID,
    
    /**
     * Tipo binário (Base64)
     */
    BINDATA,
    
    /**
     * Tipo auto - tenta detectar automaticamente o tipo
     */
    AUTO;
    
    /**
     * Converte uma string para o tipo de dados correspondente.
     * 
     * @param value String a ser convertida
     * @return DataType correspondente ou STRING se não conseguir determinar
     */
    public static DataType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return STRING;
        }
        
        try {
            return DataType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STRING;
        }
    }
    
    /**
     * Verifica se o tipo é numérico.
     * 
     * @return true se for um tipo numérico
     */
    public boolean isNumeric() {
        return this == INT32 || this == INT64 || this == DOUBLE || this == DECIMAL;
    }
    
    /**
     * Verifica se o tipo requer parsing especial.
     * 
     * @return true se requer parsing especial
     */
    public boolean requiresSpecialParsing() {
        return this != STRING && this != AUTO;
    }
}
package com.mongoimport.importer;

import com.mongoimport.config.ImportConfig;
import com.mongoimport.converter.DataTypeConverter;
import com.mongoimport.converter.DocumentBuilder;
import com.mongoimport.model.DataType;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Classe responsável pela importação de arquivos delimitados para MongoDB.
 */
public class FileImporter {
    
    private static final Logger logger = LoggerFactory.getLogger(FileImporter.class);
    
    private final ImportConfig config;
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;
    
    public FileImporter(ImportConfig config) {
        this.config = config;
    }
    
    /**
     * Executa a importação do arquivo.
     * 
     * @return true se a importação foi bem-sucedida
     */
    public boolean importFile() {
        try {
            // Conectar ao MongoDB
            if (!connectToMongoDB()) {
                return false;
            }
            
            // Dropar coleção se solicitado
            if (config.shouldDropCollection()) {
                logger.info("[INFO] Dropando coleção '{}.{}'...", config.getDatabase(), config.getCollection());
                collection.drop();
                logger.info("[INFO] Coleção dropada com sucesso.");
            }
            
            // Processar arquivo
            return processFile();
            
        } catch (Exception e) {
            logger.error("[FATAL] Erro durante a importação: {}", e.getMessage(), e);
            return false;
        } finally {
            // Fechar conexão
            if (mongoClient != null) {
                mongoClient.close();
                logger.info("[INFO] Conexão com MongoDB fechada.");
            }
        }
    }
    
    /**
     * Conecta ao MongoDB e configura a coleção.
     */
    private boolean connectToMongoDB() {
        try {
            logger.info("[INFO] Conectando ao MongoDB em: {}", config.getUri());
            mongoClient = MongoClients.create(config.getUri());
            
            // Testar conexão
            mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
            logger.info("[INFO] Conexão com MongoDB estabelecida com sucesso.");
            
            // Configurar database e collection
            MongoDatabase database = mongoClient.getDatabase(config.getDatabase());
            collection = database.getCollection(config.getCollection());
            
            return true;
            
        } catch (MongoException e) {
            logger.error("[FATAL] Erro ao conectar ao MongoDB: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Processa o arquivo e importa os dados.
     */
    private boolean processFile() {
        try (FileInputStream fis = new FileInputStream(config.getInputFile());
             InputStreamReader isr = new InputStreamReader(fis, Charset.forName(config.getEncoding()));
             CSVReader csvReader = new CSVReaderBuilder(isr)
                 .withCSVParser(new com.opencsv.CSVParserBuilder()
                     .withSeparator(config.getDelimiter())
                     .withQuoteChar('"')
                     .withEscapeChar('\\')
                     .build())
                 .build()) {
            
            logger.info("[INFO] Iniciando processamento do arquivo: {}", config.getInputFile().getName());
            
            String[] headers = null;
            int expectedFieldCount = 0;
            
            // Ler cabeçalho ou inferir campos
            if (config.hasHeaderline()) {
                headers = csvReader.readNext();
                if (headers == null) {
                    logger.error("[ERROR] Arquivo vazio ou sem cabeçalho válido.");
                    return false;
                }
                expectedFieldCount = headers.length;
                logger.debug("[DEBUG] Cabeçalho encontrado: {}", Arrays.toString(headers));
                logger.debug("[DEBUG] Número de campos esperado: {}", expectedFieldCount);
                
                // Validar e analisar estrutura de subdocumentos
                if (!DocumentBuilder.validateHeaderStructure(headers)) {
                    logger.error("[ERROR] Estrutura de headers inválida para subdocumentos.");
                    return false;
                }
                
                // Analisar e reportar estrutura de subdocumentos
                Map<String, Object> analysis = DocumentBuilder.analyzeHeaderStructure(headers);
                if ((Boolean) analysis.get("hasSubdocuments")) {
                    logger.info("[INFO] Detectados subdocumentos na estrutura:");
                    logger.info("[INFO]   - Total de campos: {}", analysis.get("totalFields"));
                    logger.info("[INFO]   - Campos simples: {}", analysis.get("simpleFields"));
                    logger.info("[INFO]   - Campos aninhados: {}", analysis.get("nestedFields"));
                    logger.info("[INFO]   - Profundidade máxima: {}", analysis.get("maxDepth"));
                } else {
                    logger.info("[INFO] Estrutura simples detectada (sem subdocumentos).");
                }
            }
            
            List<Document> batch = new ArrayList<>();
            long totalInserted = 0;
            long skippedCount = 0;
            long lineNumber = config.hasHeaderline() ? 1 : 0;
            
            String[] record;
            while ((record = csvReader.readNext()) != null) {
                lineNumber++;
                
                try {
                    // Se não temos cabeçalho, inferir da primeira linha
                    if (headers == null) {
                        expectedFieldCount = record.length;
                        headers = generateFieldNames(expectedFieldCount);
                        logger.debug("[DEBUG] Cabeçalho inferido: {}", Arrays.toString(headers));
                        logger.debug("[DEBUG] Número de campos inferido: {}", expectedFieldCount);
                    }
                    
                    // Validar número de campos
                    if (record.length != expectedFieldCount) {
                        logger.warn("[WARNING] Linha {} com inconsistência de campos ({} vs {}). Ignorando: {}", 
                                  lineNumber, record.length, expectedFieldCount, Arrays.toString(record));
                        skippedCount++;
                        continue;
                    }
                    
                    // Converter registro para documento
                    Document document = convertRecordToDocument(headers, record, lineNumber);
                    if (document != null) {
                        batch.add(document);
                    } else {
                        skippedCount++;
                        continue;
                    }
                    
                    // Inserir batch quando atingir o tamanho configurado
                    if (batch.size() >= config.getBatchSize()) {
                        insertBatch(batch);
                        totalInserted += batch.size();
                        logger.info("[PROGRESS] Inseridos {} documentos até agora. (Batch de {})", 
                                  totalInserted, batch.size());
                        batch.clear();
                    }
                    
                } catch (DataTypeConverter.ConversionException e) {
                    if (config.getParseGrace().equalsIgnoreCase("skiprow")) {
                        logger.warn("[WARNING] Pulando linha {} devido a erro de conversão: {}", 
                                  lineNumber, e.getMessage());
                        skippedCount++;
                        continue;
                    } else if (config.getParseGrace().equalsIgnoreCase("stop")) {
                        logger.error("[ERROR] Parando importação na linha {} devido a erro: {}", 
                                   lineNumber, e.getMessage());
                        return false;
                    }
                } catch (Exception e) {
                    logger.warn("[WARNING] Erro inesperado na linha {}: {}. Pulando linha.", 
                              lineNumber, e.getMessage());
                    skippedCount++;
                }
            }
            
            // Inserir batch final
            if (!batch.isEmpty()) {
                insertBatch(batch);
                totalInserted += batch.size();
                logger.info("[INFO] Inseridos {} documentos no total. (Lote final de {})", 
                          totalInserted, batch.size());
            }
            
            // Relatório final
            logger.info("\n[SUCCESS] Importação concluída. Total de documentos inseridos: {}", totalInserted);
            if (skippedCount > 0) {
                logger.warn("[WARNING] Total de documentos ignorados: {}", skippedCount);
            }
            
            return true;
            
        } catch (IOException | CsvException e) {
            logger.error("[FATAL] Erro ao processar arquivo: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gera nomes de campos quando não há cabeçalho.
     */
    private String[] generateFieldNames(int fieldCount) {
        String[] fieldNames = new String[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            fieldNames[i] = "field_" + (i + 1);
        }
        return fieldNames;
    }
    
    /**
     * Converte um registro (array de strings) para um Document do MongoDB.
     */
    private Document convertRecordToDocument(String[] headers, String[] record, long lineNumber) {
        try {
            // Converter valores para os tipos apropriados
            Object[] convertedValues = new Object[headers.length];
            
            for (int i = 0; i < headers.length; i++) {
                String fieldName = headers[i];
                String fieldValue = record[i];
                
                // Obter tipo configurado para este campo (usar nome base para tipos)
                String baseFieldName = getBaseFieldName(fieldName);
                DataType dataType = config.getColumnType(baseFieldName);
                
                // Converter valor
                Object convertedValue = DataTypeConverter.convertValue(fieldValue, dataType, config.getParseGrace());
                
                // Adicionar ao array (pular campos null se skipField)
                if (convertedValue != null || !config.getParseGrace().equalsIgnoreCase("skipfield")) {
                    convertedValues[i] = convertedValue;
                } else {
                    convertedValues[i] = null;
                }
            }
            
            // Usar DocumentBuilder para criar documento com subdocumentos
            return DocumentBuilder.buildDocument(headers, convertedValues);
            
        } catch (DataTypeConverter.ConversionException e) {
            // Re-lançar para tratamento no nível superior
            throw e;
        } catch (Exception e) {
            logger.warn("[WARNING] Erro ao converter linha {}: {}. Pulando linha.", lineNumber, e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrai o nome base do campo para configuração de tipos.
     * Ex: "address.street" -> "address.street" (mantém completo para configuração)
     */
    private String getBaseFieldName(String fieldName) {
        return fieldName; // Manter nome completo para permitir configuração específica
    }
    
    /**
     * Insere um batch de documentos no MongoDB.
     */
    private void insertBatch(List<Document> batch) {
        try {
            collection.insertMany(batch);
        } catch (MongoException e) {
            logger.error("[ERROR] Erro ao inserir batch: {}", e.getMessage());
            
            // Tentar inserir documentos individualmente para identificar problemas
            logger.info("[INFO] Tentando inserção individual dos documentos do batch...");
            int successCount = 0;
            for (Document doc : batch) {
                try {
                    collection.insertOne(doc);
                    successCount++;
                } catch (MongoException individualError) {
                    logger.warn("[WARNING] Falha ao inserir documento individual: {}", 
                              individualError.getMessage());
                    if (config.isVerbose()) {
                        logger.debug("[DEBUG] Documento problemático: {}", doc.toJson());
                    }
                }
            }
            logger.info("[INFO] Inseridos {} de {} documentos do batch individualmente.", 
                      successCount, batch.size());
        }
    }
}
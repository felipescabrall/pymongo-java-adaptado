package com.mongoimport.config;

import com.mongoimport.model.DataType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe de configuração para a importação de arquivos.
 */
public class ImportConfig {
    private final String uri;
    private final String database;
    private final String collection;
    private final File inputFile;
    private final char delimiter;
    private final int batchSize;
    private final boolean hasHeaderline;
    private final boolean dropCollection;
    private final Map<String, DataType> columnTypes;
    private final String parseGrace;
    private final String encoding;
    private final boolean verbose;
    
    private ImportConfig(Builder builder) {
        this.uri = builder.uri;
        this.database = builder.database;
        this.collection = builder.collection;
        this.inputFile = builder.inputFile;
        this.delimiter = builder.delimiter;
        this.batchSize = builder.batchSize;
        this.hasHeaderline = builder.hasHeaderline;
        this.dropCollection = builder.dropCollection;
        this.columnTypes = new HashMap<>(builder.columnTypes);
        this.parseGrace = builder.parseGrace;
        this.encoding = builder.encoding;
        this.verbose = builder.verbose;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getUri() { return uri; }
    public String getDatabase() { return database; }
    public String getCollection() { return collection; }
    public File getInputFile() { return inputFile; }
    public char getDelimiter() { return delimiter; }
    public int getBatchSize() { return batchSize; }
    public boolean hasHeaderline() { return hasHeaderline; }
    public boolean shouldDropCollection() { return dropCollection; }
    public Map<String, DataType> getColumnTypes() { return new HashMap<>(columnTypes); }
    public String getParseGrace() { return parseGrace; }
    public String getEncoding() { return encoding; }
    public boolean isVerbose() { return verbose; }
    
    /**
     * Obtém o tipo de dados configurado para uma coluna específica.
     * 
     * @param columnName Nome da coluna
     * @return Tipo de dados configurado ou AUTO se não especificado
     */
    public DataType getColumnType(String columnName) {
        return columnTypes.getOrDefault(columnName, DataType.AUTO);
    }
    
    /**
     * Verifica se há tipos de dados específicos configurados.
     * 
     * @return true se há tipos configurados
     */
    public boolean hasColumnTypes() {
        return !columnTypes.isEmpty();
    }
    
    public static class Builder {
        private String uri = "mongodb://localhost:27017/";
        private String database;
        private String collection;
        private File inputFile;
        private char delimiter = '|';
        private int batchSize = 1000;
        private boolean hasHeaderline = true;
        private boolean dropCollection = false;
        private Map<String, DataType> columnTypes = new HashMap<>();
        private String parseGrace = "autoCast";
        private String encoding = "UTF-8";
        private boolean verbose = false;
        
        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }
        
        public Builder database(String database) {
            this.database = database;
            return this;
        }
        
        public Builder collection(String collection) {
            this.collection = collection;
            return this;
        }
        
        public Builder inputFile(File inputFile) {
            this.inputFile = inputFile;
            return this;
        }
        
        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }
        
        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }
        
        public Builder hasHeaderline(boolean hasHeaderline) {
            this.hasHeaderline = hasHeaderline;
            return this;
        }
        
        public Builder dropCollection(boolean dropCollection) {
            this.dropCollection = dropCollection;
            return this;
        }
        
        public Builder columnTypes(Map<String, DataType> columnTypes) {
            this.columnTypes = columnTypes != null ? new HashMap<>(columnTypes) : new HashMap<>();
            return this;
        }
        
        public Builder parseGrace(String parseGrace) {
            this.parseGrace = parseGrace;
            return this;
        }
        
        public Builder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }
        
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }
        
        public ImportConfig build() {
            if (database == null || database.trim().isEmpty()) {
                throw new IllegalArgumentException("Database name é obrigatório");
            }
            if (collection == null || collection.trim().isEmpty()) {
                throw new IllegalArgumentException("Collection name é obrigatório");
            }
            if (inputFile == null) {
                throw new IllegalArgumentException("Input file é obrigatório");
            }
            if (batchSize <= 0) {
                throw new IllegalArgumentException("Batch size deve ser maior que zero");
            }
            
            return new ImportConfig(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ImportConfig{uri='%s', database='%s', collection='%s', file='%s', delimiter='%c', batchSize=%d, hasHeader=%b, drop=%b, columnTypes=%s}",
            uri, database, collection, inputFile.getName(), delimiter, batchSize, hasHeaderline, dropCollection, columnTypes
        );
    }
}
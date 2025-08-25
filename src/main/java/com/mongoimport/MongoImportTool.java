package com.mongoimport;

import com.mongoimport.config.ImportConfig;
import com.mongoimport.importer.FileImporter;
import com.mongoimport.model.DataType;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "mongoimport-java",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Importa arquivos delimitados para MongoDB com suporte a tipos de dados customizados"
)
public class MongoImportTool implements Callable<Integer> {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoImportTool.class);
    
    @Option(names = {"--uri"}, 
            description = "URI de conexão do MongoDB (padrão: mongodb://localhost:27017/)",
            defaultValue = "mongodb://localhost:27017/")
    private String uri;
    
    @Option(names = {"-d", "--db"}, 
            description = "Nome do banco de dados MongoDB", 
            required = true)
    private String database;
    
    @Option(names = {"-c", "--collection"}, 
            description = "Nome da coleção MongoDB", 
            required = true)
    private String collection;
    
    @Option(names = {"--file"}, 
            description = "Caminho para o arquivo de entrada", 
            required = true)
    private File inputFile;
    
    @Option(names = {"--delimiter"}, 
            description = "Caractere delimitador de campos (padrão: |)",
            defaultValue = "|")
    private String delimiter;
    
    @Option(names = {"--batchSize"}, 
            description = "Número de documentos para inserir por lote (padrão: 1000)",
            defaultValue = "1000")
    private int batchSize;
    
    @Option(names = {"--noHeaderline"}, 
            description = "Indica que o arquivo não possui linha de cabeçalho")
    private boolean noHeaderline;
    
    @Option(names = {"--drop"}, 
            description = "Dropa a coleção antes de iniciar a importação")
    private boolean drop;
    
    @Option(names = {"--columnsHaveTypes"}, 
            description = "Especifica tipos de dados para colunas no formato: campo1:tipo1,campo2:tipo2")
    private String columnsHaveTypes;
    
    @Option(names = {"--parseGrace"}, 
            description = "Modo de tolerância a erros de parsing (autoCast, skipField, skipRow, stop)",
            defaultValue = "autoCast")
    private String parseGrace;
    
    @Option(names = {"--encoding"}, 
            description = "Codificação do arquivo (padrão: UTF-8)",
            defaultValue = "UTF-8")
    private String encoding;
    
    @Option(names = {"--verbose"}, 
            description = "Modo verboso para debug")
    private boolean verbose;
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MongoImportTool()).execute(args);
        System.exit(exitCode);
    }
    
    @Override
    public Integer call() throws Exception {
        try {
            logger.info("[INFO] Iniciando importação de dados...");
            
            // Validar arquivo de entrada
            if (!inputFile.exists() || !inputFile.isFile()) {
                logger.error("[FATAL] Arquivo não encontrado: {}", inputFile.getAbsolutePath());
                return 1;
            }
            
            // Configurar importação
            ImportConfig config = ImportConfig.builder()
                .uri(uri)
                .database(database)
                .collection(collection)
                .inputFile(inputFile)
                .delimiter(delimiter.charAt(0))
                .batchSize(batchSize)
                .hasHeaderline(!noHeaderline)
                .dropCollection(drop)
                .columnTypes(parseColumnTypes(columnsHaveTypes))
                .parseGrace(parseGrace)
                .encoding(encoding)
                .verbose(verbose)
                .build();
            
            // Executar importação
            FileImporter importer = new FileImporter(config);
            boolean success = importer.importFile();
            
            if (success) {
                logger.info("[SUCCESS] Importação concluída com sucesso!");
                return 0;
            } else {
                logger.error("[FATAL] Falha na importação");
                return 1;
            }
            
        } catch (Exception e) {
            logger.error("[FATAL] Erro crítico durante a importação: {}", e.getMessage(), e);
            return 1;
        }
    }
    
    private Map<String, DataType> parseColumnTypes(String columnTypesStr) {
        Map<String, DataType> columnTypes = new HashMap<>();
        
        if (columnTypesStr == null || columnTypesStr.trim().isEmpty()) {
            return columnTypes;
        }
        
        try {
            String[] pairs = columnTypesStr.split(",");
            for (String pair : pairs) {
                String[] parts = pair.trim().split(":");
                if (parts.length == 2) {
                    String fieldName = parts[0].trim();
                    String typeName = parts[1].trim().toUpperCase();
                    
                    try {
                        DataType dataType = DataType.valueOf(typeName);
                        columnTypes.put(fieldName, dataType);
                        logger.debug("[DEBUG] Tipo configurado: {} -> {}", fieldName, dataType);
                    } catch (IllegalArgumentException e) {
                        logger.warn("[WARNING] Tipo de dados desconhecido '{}' para campo '{}'. Usando STRING como padrão.", 
                                  typeName, fieldName);
                        columnTypes.put(fieldName, DataType.STRING);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("[WARNING] Erro ao processar tipos de colunas: {}. Continuando sem tipos específicos.", 
                      e.getMessage());
        }
        
        return columnTypes;
    }
}
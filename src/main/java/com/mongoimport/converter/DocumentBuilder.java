package com.mongoimport.converter;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por construir documentos MongoDB com suporte a subdocumentos
 * usando notação de ponto nos headers (ex: address.street, address.number).
 */
public class DocumentBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);
    
    /**
     * Constrói um documento MongoDB a partir de headers e valores,
     * suportando subdocumentos através da notação de ponto.
     * 
     * @param headers Array de nomes de campos (pode conter notação de ponto)
     * @param values Array de valores correspondentes
     * @return Document MongoDB com estrutura hierárquica
     */
    public static Document buildDocument(String[] headers, Object[] values) {
        if (headers.length != values.length) {
            throw new IllegalArgumentException("Headers e valores devem ter o mesmo tamanho");
        }
        
        Document document = new Document();
        
        for (int i = 0; i < headers.length; i++) {
            String fieldPath = headers[i];
            Object value = values[i];
            
            // Pular valores nulos
            if (value == null) {
                continue;
            }
            
            setNestedValue(document, fieldPath, value);
        }
        
        return document;
    }
    
    /**
     * Define um valor em um documento usando notação de ponto para campos aninhados.
     * 
     * @param document Documento raiz
     * @param fieldPath Caminho do campo (ex: "address.street")
     * @param value Valor a ser definido
     */
    private static void setNestedValue(Document document, String fieldPath, Object value) {
        String[] pathParts = fieldPath.split("\\.");
        
        // Se não há ponto, é um campo simples
        if (pathParts.length == 1) {
            document.append(fieldPath, value);
            return;
        }
        
        // Navegar/criar estrutura aninhada
        Document currentDoc = document;
        
        // Navegar até o penúltimo nível
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            
            // Se o campo não existe, criar um novo documento
            if (!currentDoc.containsKey(part)) {
                currentDoc.append(part, new Document());
            }
            
            // Verificar se o valor existente é um Document
            Object existingValue = currentDoc.get(part);
            if (!(existingValue instanceof Document)) {
                logger.warn("[WARNING] Campo '{}' já existe como tipo não-documento. Sobrescrevendo com documento.", part);
                currentDoc.append(part, new Document());
            }
            
            currentDoc = (Document) currentDoc.get(part);
        }
        
        // Definir o valor no último nível
        String finalField = pathParts[pathParts.length - 1];
        currentDoc.append(finalField, value);
    }
    
    /**
     * Valida se um conjunto de headers tem estrutura consistente para subdocumentos.
     * 
     * @param headers Array de headers para validar
     * @return true se a estrutura é válida
     */
    public static boolean validateHeaderStructure(String[] headers) {
        Map<String, Boolean> fieldTypes = new HashMap<>();
        
        for (String header : headers) {
            String[] parts = header.split("\\.");
            
            // Construir caminho incremental
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    pathBuilder.append(".");
                }
                pathBuilder.append(parts[i]);
                String currentPath = pathBuilder.toString();
                
                boolean isLeaf = (i == parts.length - 1);
                
                // Verificar conflitos
                if (fieldTypes.containsKey(currentPath)) {
                    boolean wasLeaf = fieldTypes.get(currentPath);
                    if (wasLeaf != isLeaf) {
                        logger.error("[ERROR] Conflito de estrutura: '{}' é usado como campo simples e como documento pai", currentPath);
                        return false;
                    }
                } else {
                    fieldTypes.put(currentPath, isLeaf);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Analisa os headers e retorna informações sobre a estrutura de subdocumentos.
     * 
     * @param headers Array de headers
     * @return Mapa com estatísticas da estrutura
     */
    public static Map<String, Object> analyzeHeaderStructure(String[] headers) {
        Map<String, Object> analysis = new HashMap<>();
        int simpleFields = 0;
        int nestedFields = 0;
        int maxDepth = 0;
        
        for (String header : headers) {
            String[] parts = header.split("\\.");
            if (parts.length == 1) {
                simpleFields++;
            } else {
                nestedFields++;
                maxDepth = Math.max(maxDepth, parts.length);
            }
        }
        
        analysis.put("totalFields", headers.length);
        analysis.put("simpleFields", simpleFields);
        analysis.put("nestedFields", nestedFields);
        analysis.put("maxDepth", maxDepth);
        analysis.put("hasSubdocuments", nestedFields > 0);
        
        return analysis;
    }
}
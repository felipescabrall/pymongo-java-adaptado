# Tipos de Dados e Subdocumentos - MongoDB Import Java

Este documento detalha todos os tipos de dados e a funcionalidade de subdocumentos suportados pela aplicação Java de importação para MongoDB.

## 🏗️ NOVA FUNCIONALIDADE: Subdocumentos

A aplicação agora suporta **subdocumentos** usando **notação de ponto** nos headers do CSV!

### Como Usar Subdocumentos

**Header CSV**: Use ponto (`.`) para definir hierarquia
```csv
"nome"|"address.street"|"address.number"|"contact.email"|"contact.social.facebook"
```

**Resultado MongoDB**:
```json
{
  "nome": "João Silva",
  "address": {
    "street": "Rua das Flores",
    "number": 123
  },
  "contact": {
    "email": "joao@email.com",
    "social": {
      "facebook": "joao.silva"
    }
  }
}
```

### Características dos Subdocumentos

✅ **Profundidade ilimitada**: `config.preferences.theme.dark.mode`  
✅ **Validação automática**: Detecta conflitos de estrutura  
✅ **Tipos específicos**: Cada campo pode ter seu tipo definido  
✅ **Análise inteligente**: Reporta estatísticas da estrutura  
✅ **Compatibilidade total**: Funciona com todos os tipos de dados  

## 📋 Tipos Suportados

### 1. STRING (Texto)
- **Uso**: Dados de texto simples ou complexo
- **Exemplo CSV**: `"João Silva"`, `"Descrição do produto"`
- **Tipo MongoDB**: `String`
- **Sintaxe**: `--columnsHaveTypes "nome:STRING"`

### 2. DOUBLE (Ponto Flutuante)
- **Uso**: Números decimais de precisão dupla
- **Exemplo CSV**: `"99.99"`, `"3.14159"`
- **Tipo MongoDB**: `NumberDouble`
- **Sintaxe**: `--columnsHaveTypes "preco:DOUBLE"`

### 3. INT32 (Inteiro 32-bit)
- **Uso**: Números inteiros pequenos (-2³¹ a 2³¹-1)
- **Exemplo CSV**: `"100"`, `"42"`
- **Tipo MongoDB**: `NumberInt`
- **Sintaxe**: `--columnsHaveTypes "quantidade:INT32"`

### 4. INT64 (Inteiro 64-bit)
- **Uso**: Números inteiros grandes (-2⁶³ a 2⁶³-1)
- **Exemplo CSV**: `"1234567890123"`, `"9876543210987"`
- **Tipo MongoDB**: `NumberLong`
- **Sintaxe**: `--columnsHaveTypes "codigo:INT64"`

### 5. BOOLEAN (Verdadeiro/Falso)
- **Uso**: Valores lógicos
- **Exemplo CSV**: `"true"`, `"false"`, `"1"`, `"0"`
- **Tipo MongoDB**: `Boolean`
- **Sintaxe**: `--columnsHaveTypes "ativo:BOOLEAN"`

### 6. DATE (Data e Hora)
- **Uso**: Timestamps e datas
- **Formato**: ISO 8601 (`YYYY-MM-DDTHH:mm:ssZ`)
- **Exemplo CSV**: `"2023-01-15T10:30:00Z"`
- **Tipo MongoDB**: `ISODate`
- **Sintaxe**: `--columnsHaveTypes "data_criacao:DATE"`

### 7. DECIMAL (Alta Precisão)
- **Uso**: Números decimais de alta precisão (financeiro)
- **Exemplo CSV**: `"4.567890123456789"`, `"99.999999999999"`
- **Tipo MongoDB**: `NumberDecimal`
- **Sintaxe**: `--columnsHaveTypes "valor_preciso:DECIMAL"`

### 8. OBJECTID (Identificador MongoDB)
- **Uso**: IDs únicos do MongoDB
- **Formato**: Hexadecimal de 24 caracteres
- **Exemplo CSV**: `"507f1f77bcf86cd799439011"`
- **Tipo MongoDB**: `ObjectId`
- **Sintaxe**: `--columnsHaveTypes "_id:OBJECTID"`

### 9. BINDATA (Dados Binários)
- **Uso**: Dados binários codificados em Base64
- **Exemplo CSV**: `"SGVsbG8gV29ybGQ="` ("Hello World" em Base64)
- **Tipo MongoDB**: `BinData`
- **Sintaxe**: `--columnsHaveTypes "arquivo:BINDATA"`

### 10. NULL (Valor Nulo)
- **Uso**: Campos sem valor
- **Exemplo CSV**: `"null"`, `""` (campo vazio)
- **Tipo MongoDB**: `null`
- **Sintaxe**: Detecção automática ou `--columnsHaveTypes "campo:AUTO"`

## 🔧 Exemplos Práticos

### Arquivo CSV de Exemplo
```csv
"id"|"nome"|"preco"|"quantidade"|"codigo"|"ativo"|"criado_em"|"rating"|"objeto_id"|"dados"|"obs"
"1"|"Produto A"|"99.99"|"10"|"1234567890123"|"true"|"2023-01-15T10:30:00Z"|"4.567"|"507f1f77bcf86cd799439011"|"SGVsbG8="|"null"
```

### Comando de Importação
```bash
java -jar target/custom-mongoimport-java-1.0.0.jar \
    --file dados.csv \
    --db minha_base \
    --collection minha_colecao \
    --columnsHaveTypes "nome:STRING,preco:DOUBLE,quantidade:INT32,codigo:INT64,ativo:BOOLEAN,criado_em:DATE,rating:DECIMAL,objeto_id:OBJECTID,dados:BINDATA" \
    --delimiter="|" \
    --drop \
    --verbose
```

## 📊 Tipos Adicionais do MongoDB (Não Implementados)

Os seguintes tipos existem no MongoDB mas não estão implementados nesta versão:

- **Object**: Documentos aninhados (use JSON como STRING)
- **Array**: Arrays de valores (use JSON como STRING)
- **Timestamp**: Timestamp interno do MongoDB
- **Min/Max Key**: Chaves especiais de comparação
- **Regular Expression**: Expressões regulares
- **JavaScript**: Código JavaScript
- **Symbol**: Símbolos (deprecated)

## 🎯 Dicas de Uso

1. **Detecção Automática**: Se não especificar o tipo, a aplicação tentará detectar automaticamente
2. **Performance**: Use tipos específicos para melhor performance
3. **Precisão**: Use DECIMAL para valores monetários
4. **Datas**: Sempre use formato ISO 8601 para datas
5. **IDs**: Use OBJECTID para IDs do MongoDB válidos
6. **Binários**: Codifique dados binários em Base64

## ⚠️ Limitações

- Documentos aninhados devem ser fornecidos como JSON em campos STRING
- Arrays devem ser fornecidos como JSON em campos STRING
- Timestamps internos do MongoDB não são suportados
- Expressões regulares devem ser fornecidas como STRING

## 🧪 Arquivos de Teste

### Teste de Tipos Básicos
Use o arquivo `teste-todos-tipos.csv` e o script `teste-completo-tipos.sh`:
```bash
./teste-completo-tipos.sh
```

### Teste de Subdocumentos
Use o arquivo `teste-subdocumentos.csv` e o script `teste-subdocumentos.sh`:
```bash
./teste-subdocumentos.sh
```

### Teste Completo (Subdocumentos + Todos os Tipos)
Use o arquivo `teste-completo-subdocumentos.csv` e o script `teste-completo-subdocumentos.sh`:
```bash
./teste-completo-subdocumentos.sh
```

Este teste final demonstra **subdocumentos com até 3 níveis de profundidade** combinados com **todos os tipos de dados** suportados.
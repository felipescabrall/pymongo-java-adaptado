# 📦 Custom MongoDB Import Tool - Java

Uma ferramenta Java flexível para importar dados de arquivos delimitados (CSV, pipe-delimited, etc.) para o MongoDB com suporte avançado a tipos de dados customizados. Esta é uma versão Java equivalente ao mongoimport oficial do MongoDB, com funcionalidades adicionais.

## ✨ Funcionalidades

### 🏗️ Funcionalidade Principal: Subdocumentos
- **Notação de ponto**: Use `address.street`, `contact.social.facebook` nos headers
- **Profundidade ilimitada**: Suporte a `config.preferences.theme.dark.mode`
- **Validação automática**: Detecta conflitos de estrutura
- **Análise inteligente**: Reporta estatísticas da hierarquia
- **Compatibilidade total**: Funciona com todos os tipos de dados

### 📊 Funcionalidades Gerais
- **Suporte a Delimitadores Customizados**: Configurable delimiter support (padrão: `|`)
- **Tipos de Dados Avançados**: Suporte completo aos tipos do MongoDB (int32, int64, double, decimal, boolean, date, ObjectId, binary)
- **Detecção Automática de Tipos**: Auto-detecção inteligente de tipos quando não especificados
- **Manipulação de Campos Citados**: Lida corretamente com campos entre aspas duplas contendo delimitadores e quebras de linha
- **Processamento em Lotes**: Inserção otimizada em batches para melhor performance
- **Tolerância a Erros**: Múltiplos modos de tratamento de erros (autoCast, skipField, skipRow, stop)
- **Controle de Coleção**: Opção para dropar coleção antes da importação
- **Encoding Configurável**: Suporte a diferentes encodings de arquivo
- **Logs Detalhados**: Sistema de logging configurável para debug e monitoramento

## 🚀 Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- MongoDB 4.0 ou superior

## 📦 Instalação e Build

1. Clone ou baixe o projeto
2. Compile o projeto:

```bash
mvn clean package
```

3. O JAR executável será gerado em `target/custom-mongoimport-java-1.0.0.jar`

## 🎯 Como Usar

### Sintaxe Básica

```bash
java -jar custom-mongoimport-java-1.0.0.jar --file <arquivo> --db <database> --collection <coleção> [opções]
```

### Parâmetros Obrigatórios

- `--file <caminho>`: Caminho para o arquivo de entrada
- `--db <nome>`: Nome do banco de dados MongoDB
- `--collection <nome>`: Nome da coleção MongoDB

### Parâmetros Opcionais

- `--uri <uri>`: URI de conexão MongoDB (padrão: `mongodb://localhost:27017/`)
- `--delimiter <char>`: Delimitador de campos (padrão: `|`)
- `--batchSize <num>`: Tamanho do lote para inserção (padrão: `1000`)
- `--noHeaderline`: Indica que o arquivo não possui cabeçalho
- `--drop`: Dropa a coleção antes da importação
- `--encoding <encoding>`: Encoding do arquivo (padrão: `UTF-8`)
- `--verbose`: Modo verboso para debug
- `--columnsHaveTypes <tipos>`: Especifica tipos para colunas (formato: `campo1:tipo1,campo2:tipo2`)
- `--parseGrace <modo>`: Modo de tolerância a erros (`autoCast`, `skipField`, `skipRow`, `stop`)

## 🔧 Tipos de Dados Suportados

| Tipo | Descrição | Exemplo |
|------|-----------|----------|
| `STRING` | Texto (padrão) | `"Hello World"` |
| `INT32` | Inteiro 32-bit | `42` |
| `INT64` | Inteiro 64-bit | `9223372036854775807` |
| `DOUBLE` | Ponto flutuante | `3.14159` |
| `DECIMAL` | Decimal de alta precisão | `99.99` |
| `BOOLEAN` | Booleano | `true`, `false`, `1`, `0` |
| `DATE` | Data/hora | `2023-12-01T10:30:00` |
| `OBJECTID` | ObjectId do MongoDB | `507f1f77bcf86cd799439011` |
| `BINDATA` | Dados binários (Base64) | `SGVsbG8gV29ybGQ=` |
| `AUTO` | Detecção automática | - |

## 📝 Exemplos de Uso

### 1. Importação Básica (Pipe-delimited)

```bash
java -jar custom-mongoimport-java-1.0.0.jar \
  --file dados.txt \
  --db meudb \
  --collection produtos
```

### 2. Importação CSV com Tipos Específicos

```bash
java -jar custom-mongoimport-java-1.0.0.jar \
  --file vendas.csv \
  --delimiter "," \
  --db vendas \
  --collection transacoes \
  --columnsHaveTypes "id:INT32,valor:DOUBLE,data:DATE,ativo:BOOLEAN" \
  --drop
```

### 3. Importação sem Cabeçalho

```bash
java -jar custom-mongoimport-java-1.0.0.jar \
  --file logs.txt \
  --db logs \
  --collection eventos \
  --noHeaderline \
  --batchSize 500
```

### 4. Importação com MongoDB Remoto

```bash
java -jar custom-mongoimport-java-1.0.0.jar \
  --file dados.csv \
  --delimiter "," \
  --uri "mongodb://usuario:senha@servidor:27017/admin" \
  --db producao \
  --collection dados_importados
```

### 5. Importação com Tolerância a Erros

```bash
java -jar custom-mongoimport-java-1.0.0.jar \
  --file dados_problematicos.txt \
  --db teste \
  --collection dados \
  --parseGrace skipRow \
  --verbose
```

### 6. 🏗️ NOVO: Importação com Subdocumentos

**Arquivo com headers hierárquicos:**
```
"id"|"nome"|"address.street"|"address.number"|"contact.email"|"contact.social.facebook"
"1"|"João"|"Rua das Flores"|"123"|"joao@email.com"|"joao.silva"
```

**Comando de importação:**
```bash
java -jar custom-mongoimport-java-1.0.0.jar \
  --file usuarios.txt \
  --db app \
  --collection usuarios \
  --columnsHaveTypes "id:INT32,address.number:INT32" \
  --drop \
  --verbose
```

**Resultado no MongoDB:**
```json
{
  "id": 1,
  "nome": "João",
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

## 🛠️ Modos de Tolerância a Erros (parseGrace)

- **`autoCast`** (padrão): Tenta converter automaticamente para outro tipo em caso de erro
- **`skipField`**: Pula o campo problemático (define como null)
- **`skipRow`**: Pula a linha inteira em caso de erro de conversão
- **`stop`**: Para a importação no primeiro erro

## 📋 Formato do Arquivo de Entrada

### Regras Gerais
- Campos separados pelo delimitador especificado
- Campos com delimitadores, quebras de linha ou aspas devem estar entre aspas duplas
- Aspas duplas internas devem ser duplicadas (`"""texto"""` para `"texto"`)
- Todas as linhas devem ter o mesmo número de campos

### Exemplo de Arquivo Pipe-delimited

```
"id"|"nome"|"valor"|"data"|"ativo"
"1"|"Produto A"|"29.99"|"2023-12-01"|"true"
"2"|"Produto B com | pipe"|"45.50"|"2023-12-02"|"false"
"3"|"Produto C com
quebra de linha"|"15.75"|"2023-12-03"|"true"
```

## 🔍 Logs e Debug

A ferramenta utiliza SLF4J com Logback para logging. Os logs são exibidos no console por padrão.

### Níveis de Log
- **INFO**: Informações gerais sobre o progresso
- **WARN**: Avisos sobre dados problemáticos
- **ERROR**: Erros que impedem a importação
- **DEBUG**: Informações detalhadas (apenas com `--verbose`)

### Exemplo de Saída

```
[INFO] Conectando ao MongoDB em: mongodb://localhost:27017/
[INFO] Conexão com MongoDB estabelecida com sucesso.
[INFO] Iniciando processamento do arquivo: dados.txt
[PROGRESS] Inseridos 1000 documentos até agora. (Batch de 1000)
[PROGRESS] Inseridos 2000 documentos até agora. (Batch de 1000)
[SUCCESS] Importação concluída. Total de documentos inseridos: 2500
```

## 🧪 Testes

Para executar os testes:

```bash
mvn test
```

## 🤝 Comparação com mongoimport Original

| Funcionalidade | mongoimport oficial | Esta ferramenta |
|----------------|-------------------|------------------|
| Tipos de dados customizados | ✅ | ✅ |
| Delimitadores customizados | ❌ | ✅ |
| Auto-detecção de tipos | ❌ | ✅ |
| Múltiplos modos de erro | ❌ | ✅ |
| Campos com quebras de linha | ✅ | ✅ |
| Processamento em lotes | ✅ | ✅ |
| Logs detalhados | ❌ | ✅ |

## 🧪 Testes e Exemplos

### Scripts de Teste Disponíveis

```bash
# Teste básico de todos os tipos
./teste-completo-tipos.sh

# Teste de subdocumentos simples
./teste-subdocumentos.sh

# Teste completo: subdocumentos + todos os tipos
./teste-completo-subdocumentos.sh
```

### Arquivos de Exemplo

- `teste-todos-tipos.csv`: Demonstra todos os tipos de dados
- `teste-subdocumentos.csv`: Demonstra estruturas hierárquicas
- `teste-completo-subdocumentos.csv`: Combina subdocumentos com todos os tipos
- `dummy_pipe.csv`: Arquivo de teste com 10.000 registros
- `exemplo-tipos.csv`: Exemplo básico com tipos específicos

## 📚 Documentação Adicional

- **[INSTALACAO.md](INSTALACAO.md)**: Guia completo de instalação e configuração
- **[TIPOS-DADOS.md](TIPOS-DADOS.md)**: Documentação detalhada dos tipos de dados e subdocumentos

## 🎯 Casos de Uso

### E-commerce
```csv
"produto_id"|"nome"|"preco.valor"|"preco.moeda"|"categoria.nome"|"categoria.id"|"estoque.quantidade"|"estoque.minimo"
```

### CRM
```csv
"cliente_id"|"nome"|"endereco.rua"|"endereco.cidade"|"contato.email"|"contato.telefone"|"perfil.vip"|"perfil.pontos"
```

### IoT/Sensores
```csv
"sensor_id"|"timestamp"|"dados.temperatura"|"dados.umidade"|"localizacao.lat"|"localizacao.lng"|"status.ativo"|"status.bateria"
```

## 🤝 Contribuição

Contribuições são bem-vindas! Por favor, abra uma issue ou envie um pull request.

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## 🐛 Problemas Conhecidos

- Arquivos muito grandes podem consumir muita memória
- Conexões de rede instáveis podem causar falhas na importação
- Alguns formatos de data podem não ser reconhecidos automaticamente

## 🔮 Melhorias Futuras

- Suporte a arquivos comprimidos (gzip, zip)
- Interface gráfica opcional
- Suporte a múltiplos arquivos em lote
- Validação de schema antes da importação
- Métricas de performance detalhadas
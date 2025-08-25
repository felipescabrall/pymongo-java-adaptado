# Guia de Instalação e Configuração

## Pré-requisitos

### 1. Java 17
```bash
# Verificar versão do Java
java -version

# Se necessário, instalar Java 17 (macOS com Homebrew)
brew install openjdk@17
```

### 2. Maven
```bash
# Verificar se Maven está instalado
mvn -version

# Se necessário, instalar Maven (macOS com Homebrew)
brew install maven
```

### 3. MongoDB
```bash
# Instalar MongoDB (macOS com Homebrew)
brew tap mongodb/brew
brew install mongodb-community

# Iniciar MongoDB
brew services start mongodb/brew/mongodb-community

# Ou iniciar manualmente
mongod --config /usr/local/etc/mongod.conf
```

## Compilação do Projeto

```bash
# Compilar o projeto
mvn clean compile

# Criar JAR executável
mvn package
```

## Verificação da Instalação

### 1. Testar a aplicação
```bash
# Exibir ajuda
java -jar target/custom-mongoimport-java-1.0.0.jar --help

# Verificar conectividade com MongoDB
java -jar target/custom-mongoimport-java-1.0.0.jar \
    --file dummy_pipe.csv \
    --db test \
    --collection connectivity_test \
    --verbose
```

### 2. Executar exemplos
```bash
# Exemplo básico
./exemplo-uso.sh

# Exemplo com tipos específicos
./teste-tipos.sh
```

## Configuração do MongoDB

### Conexão Local (padrão)
- URI: `mongodb://localhost:27017/`
- Não requer autenticação

### Conexão Remota
```bash
# Com autenticação
java -jar target/custom-mongoimport-java-1.0.0.jar \
    --uri "mongodb://usuario:senha@servidor:27017/" \
    --file arquivo.csv \
    --db meudb \
    --collection minhacolecao

# MongoDB Atlas
java -jar target/custom-mongoimport-java-1.0.0.jar \
    --uri "mongodb+srv://usuario:senha@cluster.mongodb.net/" \
    --file arquivo.csv \
    --db meudb \
    --collection minhacolecao
```

## Solução de Problemas

### Erro: "Connection refused"
- **Causa**: MongoDB não está rodando
- **Solução**: Iniciar o serviço MongoDB
```bash
brew services start mongodb/brew/mongodb-community
```

### Erro: "Timed out after 30000 ms"
- **Causa**: MongoDB não acessível na URI especificada
- **Solução**: Verificar URI de conexão e conectividade de rede

### Erro: "Authentication failed"
- **Causa**: Credenciais incorretas
- **Solução**: Verificar usuário e senha na URI

### Erro: "File not found"
- **Causa**: Arquivo de entrada não existe
- **Solução**: Verificar caminho do arquivo

### Erro de parsing de tipos
- **Causa**: Tipo de dado inválido especificado
- **Solução**: Usar tipos válidos (STRING, INT32, INT64, DOUBLE, DECIMAL, BOOLEAN, DATE, OBJECTID, BINDATA, AUTO)

## Logs e Debug

### Modo Verboso
```bash
# Ativar logs detalhados
java -jar target/custom-mongoimport-java-1.0.0.jar \
    --file arquivo.csv \
    --db test \
    --collection sample \
    --verbose
```

### Configuração de Logs
- Arquivo de configuração: `src/main/resources/logback.xml`
- Logs da aplicação: nível INFO
- Logs do MongoDB driver: nível WARN
- Logs do OpenCSV: nível WARN

## Performance

### Otimização de Batch Size
```bash
# Para arquivos pequenos (< 1000 linhas)
--batchSize 100

# Para arquivos médios (1000-10000 linhas)
--batchSize 1000

# Para arquivos grandes (> 10000 linhas)
--batchSize 5000
```

### Tolerância a Erros
```bash
# Parar na primeira falha (padrão)
--parseGrace stop

# Pular campos com erro
--parseGrace skipField

# Pular linhas com erro
--parseGrace skipRow

# Tentar conversão automática
--parseGrace autoCast
```
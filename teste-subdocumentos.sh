#!/bin/bash

# Script para testar importação com SUBDOCUMENTOS

echo "=== Teste de Subdocumentos MongoDB ==="
echo

JAR_FILE="target/custom-mongoimport-java-1.0.0.jar"
TEST_FILE="teste-subdocumentos.csv"

# Verificar se o JAR existe
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR não encontrado. Execute 'mvn package' primeiro."
    exit 1
fi

# Verificar se o arquivo de teste existe
if [ ! -f "$TEST_FILE" ]; then
    echo "❌ Arquivo $TEST_FILE não encontrado."
    exit 1
fi

echo "📋 Este teste demonstra SUBDOCUMENTOS usando notação de ponto:"
echo
echo "🏠 address:"
echo "   - address.street (STRING)"
echo "   - address.number (INT32)"
echo "   - address.city (STRING)"
echo "   - address.zipcode (STRING)"
echo
echo "📞 contact:"
echo "   - contact.email (STRING)"
echo "   - contact.phone (STRING)"
echo "   - contact.social.facebook (STRING)"
echo "   - contact.social.twitter (STRING)"
echo
echo "👤 profile:"
echo "   - profile.age (INT32)"
echo "   - profile.active (BOOLEAN)"
echo "   - profile.salary (DOUBLE)"
echo "   - profile.join_date (DATE)"
echo "   - profile.rating (DECIMAL)"
echo
echo "🏷️ metadata:"
echo "   - metadata.created_by (STRING)"
echo "   - metadata.tags (STRING)"
echo
echo "📊 Estrutura esperada no MongoDB:"
echo "{"
echo "  id: 1,"
echo "  nome: \"João Silva\","
echo "  address: {"
echo "    street: \"Rua das Flores\","
echo "    number: 123,"
echo "    city: \"São Paulo\","
echo "    zipcode: \"01234-567\""
echo "  },"
echo "  contact: {"
echo "    email: \"joao@email.com\","
echo "    phone: \"11999887766\","
echo "    social: {"
echo "      facebook: \"joao.silva\","
echo "      twitter: \"@joaosilva\""
echo "    }"
echo "  },"
echo "  profile: {"
echo "    age: 35,"
echo "    active: true,"
echo "    salary: 5500.50,"
echo "    join_date: ISODate(...),"
echo "    rating: NumberDecimal(\"4.8\")"
echo "  },"
echo "  metadata: {"
echo "    created_by: \"admin\","
echo "    tags: \"cliente,vip\""
echo "  }"
echo "}"
echo

read -p "Pressione Enter para executar o teste de subdocumentos..."
echo

echo "🚀 Executando importação com SUBDOCUMENTOS..."
echo

# Comando com tipos específicos para subdocumentos
java -jar "$JAR_FILE" \
    --file "$TEST_FILE" \
    --db teste_subdocumentos \
    --collection usuarios \
    --columnsHaveTypes "id:INT32,nome:STRING,address.number:INT32,profile.age:INT32,profile.active:BOOLEAN,profile.salary:DOUBLE,profile.join_date:DATE,profile.rating:DECIMAL" \
    --delimiter="|" \
    --drop \
    --verbose

echo
echo "✅ Teste de subdocumentos concluído!"
echo
echo "🔍 Para verificar os resultados:"
echo "   1. Abra o MongoDB Compass"
echo "   2. Conecte em mongodb://localhost:27017"
echo "   3. Navegue até 'teste_subdocumentos' > 'usuarios'"
echo "   4. Observe a estrutura hierárquica:"
echo "      🏠 address (subdocumento)"
echo "      📞 contact (subdocumento com social aninhado)"
echo "      👤 profile (subdocumento)"
echo "      🏷️ metadata (subdocumento)"
echo
echo "💡 Comando MongoDB Shell para verificar estrutura:"
echo "   mongo --eval 'db.usuarios.findOne()' teste_subdocumentos"
echo
echo "📊 Comando para verificar apenas o primeiro usuário:"
echo "   mongo --eval 'printjson(db.usuarios.findOne({id: 1}))' teste_subdocumentos"
echo
echo "🔎 Comando para buscar por subdocumento:"
echo "   mongo --eval 'db.usuarios.find({\"address.city\": \"São Paulo\"})' teste_subdocumentos"
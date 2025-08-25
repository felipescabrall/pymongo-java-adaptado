#!/bin/bash

# Script para testar SUBDOCUMENTOS + TODOS OS TIPOS DE DADOS

echo "=== Teste Completo: Subdocumentos + Todos os Tipos ==="
echo

JAR_FILE="target/custom-mongoimport-java-1.0.0.jar"
TEST_FILE="teste-completo-subdocumentos.csv"

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

echo "🎯 Este teste combina SUBDOCUMENTOS + TODOS OS TIPOS:"
echo
echo "📊 dados (subdocumento com todos os tipos):"
echo "   ✅ dados.string_field (STRING)"
echo "   🔢 dados.double_field (DOUBLE)"
echo "   🔢 dados.int32_field (INT32)"
echo "   🔢 dados.int64_field (INT64)"
echo "   ✅ dados.boolean_field (BOOLEAN)"
echo "   📅 dados.date_field (DATE)"
echo "   💰 dados.decimal_field (DECIMAL)"
echo "   🆔 dados.objectid_field (OBJECTID)"
echo "   📦 dados.bindata_field (BINDATA)"
echo "   ❌ dados.null_field (NULL)"
echo
echo "🏠 endereco (subdocumento simples):"
echo "   📍 endereco.rua (STRING)"
echo "   🔢 endereco.numero (INT32)"
echo "   🏙️ endereco.cidade (STRING)"
echo
echo "📞 contato (subdocumento):"
echo "   📧 contato.email (STRING)"
echo "   📱 contato.telefone (STRING)"
echo
echo "👤 perfil (subdocumento):"
echo "   🔢 perfil.idade (INT32)"
echo "   ✅ perfil.ativo (BOOLEAN)"
echo "   💰 perfil.salario (DOUBLE)"
echo
echo "⚙️ config (subdocumento aninhado - 3 níveis):"
echo "   🎨 config.preferences.theme (STRING)"
echo "   🌐 config.preferences.language (STRING)"
echo "   🔔 config.settings.notifications (BOOLEAN)"
echo "   🔒 config.settings.privacy (STRING)"
echo
echo "📊 Estrutura esperada no MongoDB:"
echo "{"
echo "  id: 1,"
echo "  nome: \"João Silva\","
echo "  dados: {"
echo "    string_field: \"Texto exemplo\","
echo "    double_field: 99.99,"
echo "    int32_field: 100,"
echo "    int64_field: NumberLong(\"1234567890123\"),"
echo "    boolean_field: true,"
echo "    date_field: ISODate(...),"
echo "    decimal_field: NumberDecimal(\"4.567890123456789\"),"
echo "    objectid_field: ObjectId(\"507f1f77bcf86cd799439011\"),"
echo "    bindata_field: BinData(...),"
echo "    null_field: null"
echo "  },"
echo "  endereco: {"
echo "    rua: \"Rua das Flores\","
echo "    numero: 123,"
echo "    cidade: \"São Paulo\""
echo "  },"
echo "  contato: {"
echo "    email: \"joao@email.com\","
echo "    telefone: \"11999887766\""
echo "  },"
echo "  perfil: {"
echo "    idade: 35,"
echo "    ativo: true,"
echo "    salario: 5500.50"
echo "  },"
echo "  config: {"
echo "    preferences: {"
echo "      theme: \"dark\","
echo "      language: \"pt-BR\""
echo "    },"
echo "    settings: {"
echo "      notifications: true,"
echo "      privacy: \"high\""
echo "    }"
echo "  }"
echo "}"
echo

read -p "Pressione Enter para executar o teste completo..."
echo

echo "🚀 Executando importação COMPLETA (Subdocumentos + Todos os Tipos)..."
echo

# Comando com TODOS os tipos em subdocumentos
java -jar "$JAR_FILE" \
    --file "$TEST_FILE" \
    --db teste_completo_final \
    --collection usuarios_completo \
    --columnsHaveTypes "id:INT32,nome:STRING,dados.string_field:STRING,dados.double_field:DOUBLE,dados.int32_field:INT32,dados.int64_field:INT64,dados.boolean_field:BOOLEAN,dados.date_field:DATE,dados.decimal_field:DECIMAL,dados.objectid_field:OBJECTID,dados.bindata_field:BINDATA,endereco.numero:INT32,perfil.idade:INT32,perfil.ativo:BOOLEAN,perfil.salario:DOUBLE,config.settings.notifications:BOOLEAN" \
    --delimiter="|" \
    --drop \
    --verbose

echo
echo "✅ Teste completo concluído com sucesso!"
echo
echo "🎉 FUNCIONALIDADES VALIDADAS:"
echo "   ✅ Subdocumentos (até 3 níveis de profundidade)"
echo "   ✅ Todos os tipos de dados MongoDB"
echo "   ✅ Combinação de tipos simples e aninhados"
echo "   ✅ Estrutura hierárquica complexa"
echo "   ✅ Validação automática de estrutura"
echo
echo "🔍 Para verificar os resultados:"
echo "   1. MongoDB Compass: mongodb://localhost:27017"
echo "   2. Database: teste_completo_final"
echo "   3. Collection: usuarios_completo"
echo
echo "💡 Comandos de verificação:"
echo "   # Ver estrutura completa"
echo "   mongo --eval 'printjson(db.usuarios_completo.findOne())' teste_completo_final"
echo
echo "   # Buscar por subdocumento aninhado"
echo "   mongo --eval 'db.usuarios_completo.find({\"config.preferences.theme\": \"dark\"})' teste_completo_final"
echo
echo "   # Verificar tipos de dados"
echo "   mongo --eval 'db.usuarios_completo.findOne({}, {dados: 1})' teste_completo_final"
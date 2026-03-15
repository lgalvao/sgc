#!/bin/bash
set -e

echo "    🧪 Executando testes: Backend   "

echo ">> Testes de unidade"
./gradlew :backend:unitTest

echo ">> Testes de integração"
./gradlew :backend:integrationTest

#!/bin/bash
set -e

echo " 📦 Atualizando dependências"
echo "------------------------------------"
echo ">> Atualizando dependências do globais"
npm update -g --save

echo ">> Atualizando dependências do frontend"
cd frontend
npm update --save
cd ..

echo "✅ Atualização concluída com sucesso."
#!/bin/bash
set -e

echo "======================================"
echo "    📦 Atualizando dependências     "
echo "======================================"

echo ">> Atualizando dependências do root"
npm update -g
npm update --save

echo ">> Atualizando dependências do frontend"
cd frontend
npm update --save
cd ..

echo "✅ Atualização concluída com sucesso."

#!/bin/bash

# Script principal para alcançar 100% de cobertura de testes
# Este script orquestra todo o processo de análise e melhoria de cobertura

set -e  # Sai em caso de erro

SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPTS_DIR/../.."
ROOT_DIR="$BACKEND_DIR/../.."

echo "🎯 === JORNADA PARA 100% DE COBERTURA ==="
echo ""

# Função para executar etapa
run_step() {
    local step_num=$1
    local step_name=$2
    local step_cmd=$3
    
    echo "📍 Etapa $step_num: $step_name"
    echo "─────────────────────────────────────────"
    eval "$step_cmd"
    echo ""
}

# Passo 1: Rodar testes e gerar relatório JaCoCo
run_step 1 "Executar testes e gerar relatório JaCoCo" \
    "cd '$ROOT_DIR' && ./gradlew :backend:test :backend:jacocoTestReport"

# Passo 2: Analisar cobertura atual
run_step 2 "Analisar cobertura atual (visão detalhada)" \
    "cd '$ROOT_DIR' && node backend/etc/scripts/analisar-cobertura.cjs > cobertura-detalhada.txt 2>&1 && head -n 50 cobertura-detalhada.txt"

# Passo 3: Identificar lacunas
run_step 3 "Identificar lacunas de cobertura" \
    "cd '$ROOT_DIR' && node backend/etc/scripts/super-cobertura.cjs | head -n 100"

# Passo 4: Gerar plano de ação
run_step 4 "Gerar plano de ação para 100%" \
    "cd '$ROOT_DIR' && node backend/etc/scripts/gerar-plano-cobertura.cjs"

# Passo 5: Analisar arquivos sem testes
run_step 5 "Analisar arquivos sem testes unitários" \
    "cd '$ROOT_DIR' && python3 backend/etc/scripts/analyze_tests.py --dir backend --output analise-testes.md 2>&1"

# Passo 6: Priorizar testes
echo "📍 Etapa 6: Priorizar criação de testes"
echo "─────────────────────────────────────────"
if [ -f "$ROOT_DIR/analise-testes.md" ]; then
    cd "$ROOT_DIR" && python3 backend/etc/scripts/prioritize_tests.py --input analise-testes.md --output priorizacao-testes.md
   show cat "$ROOT_DIR/priorizacao-testes.md" | head -n 50
else
    echo "⚠️  Arquivo analise-testes.md não encontrado, pulando priorização"
fi
echo ""

# Resumo final
echo "✅ === ANÁLISE COMPLETA ==="
echo ""
echo "Arquivos gerados:"
echo "  📄 plano-100-cobertura.md       - Plano detalhado com todas as lacunas"
echo "  📄 cobertura-detalhada.txt       - Análise detalhada com tabelas"
echo "  📄 cobertura_lacunas.json        - Dados estruturados das lacunas"
echo "  📄 analise-testes.md             - Análise de arquivos sem testes"
echo "  📄 priorizacao-testes.md         - Testes priorizados por importância"
echo ""
echo "Próximos passos:"
echo "  1. Revisar plano-100-cobertura.md"
echo "  2. Começar pelos testes P1 (críticos) em priorizacao-testes.md"
echo "  3. Usar 'node backend/etc/scripts/gerar-testes-cobertura.cjs <Classe>'"
echo "     para gerar esqueletos de testes"
echo "  4. Implementar os testes"
echo "  5. Rodar este script novamente para verificar progresso"
echo ""
echo "🎯 Meta: De $(cd "$ROOT_DIR" && node -e \"const fs=require('fs'); const data=fs.readFileSync('cobertura_lacunas.json'); console.log(JSON.parse(data).globalLineCoverage || '?%')\") → 100%"
echo ""

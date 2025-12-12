#!/bin/bash

# Script para executar apenas os testes de captura de telas
# Uso: ./scripts/capturar-telas.sh [opcoes]
#
# Opções:
#   --headed    Executa com navegador visível (útil para debug)
#   --debug     Ativa modo debug do Playwright
#   --ui        Abre interface UI do Playwright

set -e

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  SGC - Captura de Telas do Sistema                    ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Diretório de screenshots
SCREENSHOTS_DIR="screenshots"

# Limpar screenshots antigos se existirem
if [ -d "$SCREENSHOTS_DIR" ]; then
    echo -e "${YELLOW}Limpando screenshots antigas...${NC}"
    rm -rf "$SCREENSHOTS_DIR"/*
    echo -e "${GREEN}✓ Screenshots antigas removidas${NC}"
fi

# Criar diretório se não existir
mkdir -p "$SCREENSHOTS_DIR"

# Construir comando playwright
PLAYWRIGHT_CMD="npx playwright test e2e/captura-telas.spec.ts"

# Processar argumentos
HEADED_MODE=""
DEBUG_MODE=""
UI_MODE=""

for arg in "$@"; do
    case $arg in
        --headed)
            HEADED_MODE="--headed"
            echo -e "${BLUE}→ Modo headed ativado (navegador visível)${NC}"
            ;;
        --debug)
            DEBUG_MODE="--debug"
            echo -e "${BLUE}→ Modo debug ativado${NC}"
            ;;
        --ui)
            UI_MODE="--ui"
            echo -e "${BLUE}→ Modo UI ativado${NC}"
            ;;
        *)
            echo -e "${YELLOW}Argumento desconhecido: $arg${NC}"
            ;;
    esac
done

# Montar comando final
PLAYWRIGHT_CMD="$PLAYWRIGHT_CMD $HEADED_MODE $DEBUG_MODE $UI_MODE"

echo ""
echo -e "${BLUE}Iniciando captura de telas...${NC}"
echo -e "${BLUE}Comando: $PLAYWRIGHT_CMD${NC}"
echo ""

# Executar testes
if $PLAYWRIGHT_CMD; then
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✓ Captura de telas concluída com sucesso!           ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}Screenshots salvas em: ./$SCREENSHOTS_DIR/${NC}"
    echo ""
    
    # Contar arquivos gerados
    NUM_SCREENSHOTS=$(find "$SCREENSHOTS_DIR" -name "*.png" | wc -l)
    echo -e "${GREEN}Total de screenshots capturadas: $NUM_SCREENSHOTS${NC}"
    echo ""
    
    # Listar categorias
    echo -e "${BLUE}Categorias de screenshots:${NC}"
    find "$SCREENSHOTS_DIR" -name "*.png" -type f | sed 's/.*\/\([^-]*\)--.*/\1/' | sort -u | while read category; do
        count=$(find "$SCREENSHOTS_DIR" -name "${category}--*.png" | wc -l)
        echo -e "  ${GREEN}→${NC} $category: $count screenshots"
    done
    echo ""
else
    echo ""
    echo -e "${YELLOW}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║  ⚠ Captura de telas falhou ou foi interrompida       ║${NC}"
    echo -e "${YELLOW}╚════════════════════════════════════════════════════════╝${NC}"
    echo ""
    exit 1
fi

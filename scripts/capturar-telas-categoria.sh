#!/bin/bash

# Script para capturar screenshots de categorias específicas
# Uso: ./scripts/capturar-telas-categoria.sh <categoria>
#
# Categorias disponíveis:
#   seguranca    - Telas de login e autenticação
#   painel          - Painel principal
#   processo        - Criação e gerenciamento de processos
#   subprocesso     - Dashboard e atividades
#   mapa            - Mapa de competências
#   navegacao       - Elementos de navegação
#   estados         - Diferentes estados de processo
#   responsividade  - Screenshots em múltiplas resoluções
#   all             - Todas as categorias (padrão)

set -e

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Função para mostrar uso
show_usage() {
    echo -e "${BLUE}Uso:${NC} $0 <categoria> [opcoes]"
    echo ""
    echo -e "${BLUE}Categorias disponíveis:${NC}"
    echo "  autenticacao    - Telas de login e autenticação"
    echo "  painel          - Painel principal"
    echo "  processo        - Criação e gerenciamento de processos"
    echo "  subprocesso     - Dashboard e atividades"
    echo "  mapa            - Mapa de competências"
    echo "  navegacao       - Elementos de navegação"
    echo "  estados         - Diferentes estados de processo"
    echo "  responsividade  - Screenshots em múltiplas resoluções"
    echo "  all             - Todas as categorias (padrão)"
    echo ""
    echo -e "${BLUE}Opções:${NC}"
    echo "  --headed        - Executar com navegador visível"
    echo "  --debug         - Ativar modo debug do Playwright"
    echo "  --ui            - Abrir interface UI do Playwright"
    echo ""
    echo -e "${BLUE}Exemplos:${NC}"
    echo "  $0 autenticacao"
    echo "  $0 painel --headed"
    echo "  $0 all --ui"
}

# Verificar argumentos
if [ "$#" -lt 1 ]; then
    show_usage
    exit 1
fi

CATEGORIA=$1
shift

# Validar categoria
case $CATEGORIA in
    seguranca|painel|processo|subprocesso|mapa|navegacao|estados|responsividade|all)
        ;;
    *)
        echo -e "${RED}Erro: Categoria inválida '$CATEGORIA'${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  SGC - Captura de Telas por Categoria                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Diretório de screenshots
SCREENSHOTS_DIR="screenshots"
mkdir -p "$SCREENSHOTS_DIR"

# Mapear categoria para padrão de teste
case $CATEGORIA in
    seguranca)
        TEST_PATTERN="01 - Autenticação"
        ;;
    painel)
        TEST_PATTERN="02 - Painel Principal"
        ;;
    processo)
        TEST_PATTERN="03 - Fluxo de Processo"
        ;;
    subprocesso)
        TEST_PATTERN="04 - Subprocesso e Atividades"
        ;;
    mapa)
        TEST_PATTERN="05 - Mapa de Competências"
        ;;
    navegacao)
        TEST_PATTERN="06 - Navegação e Menus"
        ;;
    estados)
        TEST_PATTERN="07 - Estados e Situações"
        ;;
    responsividade)
        TEST_PATTERN="08 - Responsividade"
        ;;
    all)
        TEST_PATTERN=""
        ;;
esac

# Processar argumentos adicionais
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

# Construir comando playwright
if [ -z "$TEST_PATTERN" ]; then
    PLAYWRIGHT_CMD="npx playwright test e2e/captura-telas.spec.ts"
else
    PLAYWRIGHT_CMD="npx playwright test e2e/captura-telas.spec.ts --grep \"$TEST_PATTERN\""
fi

PLAYWRIGHT_CMD="$PLAYWRIGHT_CMD $HEADED_MODE $DEBUG_MODE $UI_MODE"

echo ""
echo -e "${BLUE}Categoria selecionada: ${GREEN}$CATEGORIA${NC}"
if [ ! -z "$TEST_PATTERN" ]; then
    echo -e "${BLUE}Padrão de teste: ${NC}$TEST_PATTERN"
fi
echo -e "${BLUE}Comando: ${NC}$PLAYWRIGHT_CMD"
echo ""

# Executar testes
if eval $PLAYWRIGHT_CMD; then
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✓ Captura concluída com sucesso!                    ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}Screenshots salvas em: ./$SCREENSHOTS_DIR/${NC}"
    echo ""
    
    # Contar arquivos da categoria
    if [ "$CATEGORIA" = "all" ]; then
        NUM_SCREENSHOTS=$(find "$SCREENSHOTS_DIR" -name "*.png" | wc -l)
        echo -e "${GREEN}Total de screenshots: $NUM_SCREENSHOTS${NC}"
    else
        # Mapear categoria para prefixo de arquivo
        case $CATEGORIA in
            seguranca) PREFIX="01-autenticacao" ;;
            painel) PREFIX="02-painel" ;;
            processo) PREFIX="03-processo" ;;
            subprocesso) PREFIX="04-subprocesso" ;;
            mapa) PREFIX="05-mapa" ;;
            navegacao) PREFIX="06-navegacao" ;;
            estados) PREFIX="07-estados" ;;
            responsividade) PREFIX="08-responsividade" ;;
        esac
        
        NUM_SCREENSHOTS=$(find "$SCREENSHOTS_DIR" -name "${PREFIX}--*.png" 2>/dev/null | wc -l)
        echo -e "${GREEN}Screenshots capturadas da categoria '$CATEGORIA': $NUM_SCREENSHOTS${NC}"
    fi
    echo ""
else
    echo ""
    echo -e "${YELLOW}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║  ⚠ Captura falhou ou foi interrompida                 ║${NC}"
    echo -e "${YELLOW}╚════════════════════════════════════════════════════════╝${NC}"
    echo ""
    exit 1
fi

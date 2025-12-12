#!/bin/bash

# Script para visualizar screenshots capturadas
# Inicia um servidor HTTP local e abre o visualizador no navegador

set -e

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  SGC - Visualizador de Screenshots                    ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Verificar se há screenshots
SCREENSHOTS_DIR="screenshots"
NUM_SCREENSHOTS=$(find "$SCREENSHOTS_DIR" -name "*.png" 2>/dev/null | wc -l)

if [ "$NUM_SCREENSHOTS" -eq 0 ]; then
    echo -e "${YELLOW}⚠ Nenhuma screenshot encontrada!${NC}"
    echo ""
    echo "Execute primeiro os testes de captura:"
    echo "  ./scripts/capturar-telas.sh"
    echo ""
    exit 1
fi

echo -e "${GREEN}✓ Encontradas $NUM_SCREENSHOTS screenshots${NC}"
echo ""

# Porta padrão
PORT=8765

# Verificar se a porta está em uso
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}Porta $PORT já está em uso. Tentando outra porta...${NC}"
    PORT=8766
fi

echo -e "${BLUE}Iniciando servidor HTTP na porta $PORT...${NC}"
echo ""

# Tentar diferentes formas de iniciar servidor
if command -v python3 &> /dev/null; then
    echo -e "${GREEN}→ Usando Python 3${NC}"
    echo -e "${BLUE}→ Acesse: ${GREEN}http://localhost:$PORT/visualizador.html${NC}"
    echo ""
    echo -e "${YELLOW}Pressione Ctrl+C para parar o servidor${NC}"
    echo ""
    
    # Tentar abrir no navegador
    sleep 1
    if command -v xdg-open &> /dev/null; then
        xdg-open "http://localhost:$PORT/visualizador.html" 2>/dev/null &
    elif command -v open &> /dev/null; then
        open "http://localhost:$PORT/visualizador.html" 2>/dev/null &
    fi
    
    cd "$SCREENSHOTS_DIR" && python3 -m http.server $PORT
elif command -v python &> /dev/null; then
    echo -e "${GREEN}→ Usando Python 2${NC}"
    echo -e "${BLUE}→ Acesse: ${GREEN}http://localhost:$PORT/visualizador.html${NC}"
    echo ""
    echo -e "${YELLOW}Pressione Ctrl+C para parar o servidor${NC}"
    echo ""
    
    sleep 1
    if command -v xdg-open &> /dev/null; then
        xdg-open "http://localhost:$PORT/visualizador.html" 2>/dev/null &
    elif command -v open &> /dev/null; then
        open "http://localhost:$PORT/visualizador.html" 2>/dev/null &
    fi
    
    cd "$SCREENSHOTS_DIR" && python -m SimpleHTTPServer $PORT
elif command -v npx &> /dev/null; then
    echo -e "${GREEN}→ Usando npx serve${NC}"
    echo -e "${BLUE}→ O navegador será aberto automaticamente${NC}"
    echo ""
    echo -e "${YELLOW}Pressione Ctrl+C para parar o servidor${NC}"
    echo ""
    cd "$SCREENSHOTS_DIR" && npx serve -l $PORT
else
    echo -e "${YELLOW}⚠ Nenhum servidor HTTP disponível!${NC}"
    echo ""
    echo "Instale Python ou Node.js para usar o visualizador."
    echo ""
    echo "Alternativamente, abra manualmente:"
    echo "  $SCREENSHOTS_DIR/visualizador.html"
    echo ""
    exit 1
fi

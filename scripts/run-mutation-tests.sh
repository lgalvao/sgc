#!/bin/bash
#
# Script para executar Mutation Testing completo no SGC
# Uso: ./run-mutation-tests.sh [opções]
#
# Opções:
#   --quick      : Executa apenas nos módulos de alta prioridade
#   --full       : Executa em todos os módulos configurados (padrão)
#   --module <nome> : Executa apenas no módulo especificado
#   --help       : Exibe esta mensagem de ajuda

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função de ajuda
show_help() {
    echo "Uso: ./run-mutation-tests.sh [opções]"
    echo ""
    echo "Opções:"
    echo "  --quick         Executa apenas nos módulos de alta prioridade"
    echo "  --full          Executa em todos os módulos configurados (padrão)"
    echo "  --module <nome> Executa apenas no módulo especificado"
    echo "  --help          Exibe esta mensagem de ajuda"
    echo ""
    echo "Exemplos:"
    echo "  ./run-mutation-tests.sh --quick"
    echo "  ./run-mutation-tests.sh --module processo"
    echo "  ./run-mutation-tests.sh --full"
}

# Função para exibir banner
show_banner() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║                                                           ║"
    echo "║        SGC - Mutation Testing (PITest)                    ║"
    echo "║        Avaliação de Qualidade de Testes                   ║"
    echo "║                                                           ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Função para executar MBT em um módulo
run_mutation_test() {
    local module=$1
    echo -e "${YELLOW}[INFO]${NC} Executando Mutation Testing no módulo: ${GREEN}${module}${NC}"
    
    ./gradlew :backend:mutationTestModule -Pmodule="${module}" --no-daemon
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}[SUCESSO]${NC} Mutation Testing concluído para: ${module}"
    else
        echo -e "${RED}[ERRO]${NC} Falha no Mutation Testing para: ${module}"
        return $exit_code
    fi
}

# Módulos de alta prioridade
HIGH_PRIORITY_MODULES=(
    "processo"
    "subprocesso"
    "mapa"
)

# Módulos de média prioridade
MEDIUM_PRIORITY_MODULES=(
    "atividade"
    "unidade"
)

# Módulos de baixa prioridade (baseline)
LOW_PRIORITY_MODULES=(
    "comum"
)

# Parse de argumentos
MODE="full"
SPECIFIC_MODULE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --quick)
            MODE="quick"
            shift
            ;;
        --full)
            MODE="full"
            shift
            ;;
        --module)
            MODE="specific"
            SPECIFIC_MODULE="$2"
            shift 2
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}[ERRO]${NC} Opção desconhecida: $1"
            show_help
            exit 1
            ;;
    esac
done

# Exibir banner
show_banner

# Verificar se estamos na raiz do projeto
if [ ! -f "build.gradle.kts" ]; then
    echo -e "${RED}[ERRO]${NC} Este script deve ser executado a partir da raiz do projeto SGC"
    exit 1
fi

# Verificar se testes unitários passam primeiro
echo -e "${YELLOW}[INFO]${NC} Verificando se testes unitários passam antes de executar MBT..."
./gradlew :backend:test --no-daemon

if [ $? -ne 0 ]; then
    echo -e "${RED}[ERRO]${NC} Testes unitários falharam. Corrija os testes antes de executar MBT."
    exit 1
fi

echo -e "${GREEN}[OK]${NC} Testes unitários passaram. Prosseguindo com Mutation Testing..."
echo ""

# Executar baseado no modo selecionado
case $MODE in
    quick)
        echo -e "${YELLOW}[INFO]${NC} Modo QUICK: Executando apenas módulos de ALTA prioridade"
        echo -e "${YELLOW}[INFO]${NC} Módulos: ${HIGH_PRIORITY_MODULES[*]}"
        echo ""
        
        for module in "${HIGH_PRIORITY_MODULES[@]}"; do
            run_mutation_test "$module"
            echo ""
        done
        ;;
        
    specific)
        if [ -z "$SPECIFIC_MODULE" ]; then
            echo -e "${RED}[ERRO]${NC} Módulo não especificado. Use --module <nome>"
            exit 1
        fi
        
        echo -e "${YELLOW}[INFO]${NC} Modo SPECIFIC: Executando apenas módulo: ${SPECIFIC_MODULE}"
        echo ""
        
        run_mutation_test "$SPECIFIC_MODULE"
        ;;
        
    full)
        echo -e "${YELLOW}[INFO]${NC} Modo FULL: Executando em todos os módulos configurados"
        echo ""
        
        # Executar PITest completo (usa configuração do build.gradle.kts)
        echo -e "${YELLOW}[INFO]${NC} Executando Mutation Testing completo..."
        ./gradlew :backend:pitest --no-daemon
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}[SUCESSO]${NC} Mutation Testing completo concluído"
        else
            echo -e "${RED}[ERRO]${NC} Falha no Mutation Testing completo"
            exit 1
        fi
        ;;
esac

# Exibir localização do relatório
echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}[CONCLUÍDO]${NC} Mutation Testing executado com sucesso!"
echo ""
echo -e "${YELLOW}Relatórios disponíveis em:${NC}"
echo -e "  📊 HTML: ${GREEN}backend/build/reports/pitest/index.html${NC}"
echo -e "  📄 XML:  ${GREEN}backend/build/reports/pitest/mutations.xml${NC}"
echo ""
echo -e "${YELLOW}Próximos passos:${NC}"
echo "  1. Abrir relatório HTML no navegador"
echo "  2. Analisar mutantes sobreviventes"
echo "  3. Criar/melhorar testes para matar mutantes críticos"
echo "  4. Re-executar MBT para validar melhorias"
echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"

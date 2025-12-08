#!/bin/bash

# Define o nome do arquivo de saída
OUTPUT_FILE="relatorio_linhas.txt"

echo "Iniciando contagem de linhas..."

# Verifica se é um repositório git
if git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    # Método 1: Usa git ls-files (Mais limpo, ignora arquivos do .gitignore)
    echo "Repositório Git detectado. Usando lista de arquivos versionados..."
    
    # 1. Lista arquivos
    # 2. xargs passa para o wc -l
    # 3. sort -k2 ordena pela segunda coluna (o caminho do arquivo), agrupando por diretório
    git ls-files | xargs wc -l | sort -k2 > "$OUTPUT_FILE"
else
    # Método 2: Fallback para 'find' se não for git (Exclui pastas comuns de build/deps)
    echo "Git não detectado. Usando busca padrão (find)..."
    
    find . -type f \
        -not -path "*/.git/*" \
        -not -path "*/node_modules/*" \
        -not -path "*/build/*" \
        -not -path "*/dist/*" \
        -not -path "*/.gradle/*" \
        -not -path "*/.idea/*" \
        -not -path "*/.vscode/*" \
        -not -path "*/coverage/*" \
        -print0 | xargs -0 wc -l | sort -k2 > "$OUTPUT_FILE"
fi

echo "---------------------------------------------------"
echo "Relatório gerado com sucesso em: $OUTPUT_FILE"
echo "---------------------------------------------------"
echo "Resumo Total:"
# Exibe a última linha (que geralmente contém o total do wc)
tail -n 1 "$OUTPUT_FILE"
echo "---------------------------------------------------"

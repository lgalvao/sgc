import os
import re
import sys

# Configuração
EXTENSIONS = {'.vue', '.ts', '.js', '.html', '.java', '.kt', '.sql', '.xml', '.yml', '.properties'}
IGNORE_DIRS = {'node_modules', '.git', '.gradle', 'build', 'dist', 'target', '.idea', '.vscode'}
OUTPUT_FILE = 'id-legacy-report.txt'
SEPARATOR = ' | '

# Padrões para identificar 'id' legado
ID_PATTERNS = [
    re.compile(r'\bid\b'),               # 'id' isolado
    re.compile(r'\bId\b'),               # 'Id' isolado
    re.compile(r'\{id\}'),               # '{id}' em templates/URIs
    re.compile(r'\b\w+Id\b'),            # Sufixo 'Id' (ex: codProcesso, userId)
    re.compile(r'\bid[A-Z]\w*\b'),       # Prefixo 'id' camelCase (ex: idProcesso)
    re.compile(r'\bget[A-Z]\w*Id\b'),    # Getters que terminam em Id
    re.compile(r'\bset[A-Z]\w*Id\b'),    # Setters que terminam em Id
    re.compile(r'\bgetId\b'),            # getId isolado
    re.compile(r'\bsetId\b'),            # setId isolado
    re.compile(r'"id"\s*:'),             # Chave "id" em JSON
    re.compile(r'\'id\'\s*:'),           # Chave 'id' em JS/TS objects
]

# Palavras e métodos que contêm 'id' mas são padrão ou técnicos (NÃO RECLAMAR)
EXCECOES_EXATAS = {
    'grid', 'invalid', 'valid', 'solid', 'void', 'fluid', 'width', 'mid', 'side', 
    'rapid', 'rigid', 'liquid', 'acid', 'hybrid', 'guid', 'uuid', 'id-br', 'id-BR',
    'traceId', 'traceid', 'TRACEID', 'setTraceId', 'getTraceId',
    'idAlvo', 'UsuarioPerfilId', 'UnidadeProcessoId'
}

# Padrões do Spring Data e JPA que devem ser ignorados
SPRING_JPA_IGNORE = {
    '@Id', '@EmbeddedId', '@MapsId', '@IdClass',
    'findById', 'existsById', 'deleteById', 'findAllById', 'countById', 'deleteAllById',
    'getReferenceById'
}

def find_id_legacy(start_path='.'):
    results = []
    print(f"Iniciando varredura filtrada por 'id' legado em: {start_path}...")
    
    for root, dirs, files in os.walk(start_path):
        # Ignora diretórios indesejados
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        
        for file in files:
            if any(file.endswith(ext) for ext in EXTENSIONS):
                file_path = os.path.normpath(os.path.join(root, file))
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        for line_num, line in enumerate(f, 1):
                            if line.strip().startswith(('import ', 'package ')): 
                                continue
                                
                            line_content = line.strip()
                            found_in_line = set()
                            
                            for pattern in ID_PATTERNS:
                                for match in pattern.finditer(line):
                                    text = match.group().strip()
                                    
                                    if text.lower() in EXCECOES_EXATAS or text in EXCECOES_EXATAS:
                                        continue
                                    
                                    start = max(0, match.start() - 15)
                                    end = min(len(line), match.end() + 15)
                                    context = line[start:end]
                                    if any(ign in context for ign in SPRING_JPA_IGNORE):
                                        continue

                                    word_match = re.search(r'\b\w+\b', line[max(0, match.start()-1):match.end()+1])
                                    if word_match and word_match.group().lower() in EXCECOES_EXATAS:
                                        continue

                                    found_in_line.add(text)
                            
                            if found_in_line:
                                matches_str = ", ".join(found_in_line)
                                results.append(f"{file_path}{SEPARATOR}{line_num}{SEPARATOR}{matches_str}{SEPARATOR}{line_content}")
                except (UnicodeDecodeError, PermissionError):
                    continue

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        if results:
            f.write(f"ARQUIVO{SEPARATOR}LINHA{SEPARATOR}MATCHES{SEPARATOR}CONTEUDO\n")
            f.write('\n'.join(results))
        else:
            f.write(f"Nenhuma instância legada de 'id' encontrada em {start_path}.")
            
    print(f"Varredura concluída. Relatório salvo em '{OUTPUT_FILE}' com {len(results)} ocorrências.")

if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else '.'
    find_id_legacy(path)

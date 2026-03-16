import os
import re

# Configuração
EXTENSIONS = {'.vue', '.ts', '.js', '.html', '.java', '.md'}
IGNORE_DIRS = {'node_modules', '.git', '.gradle', 'build', 'dist', 'target', '.idea', '.vscode'}
OUTPUT_FILE = 'title-case-report.txt'
SEPARATOR = ' | '

UPPER = r'[A-ZÀ-ÖØ-Þ]'
LOWER = r'[a-zà-öø-ÿ]'

# Padrão: Uma palavra começando com Maiúscula, seguida de uma ou mais palavras começando com Maiúscula
TITLE_CASE_PATTERN = re.compile(rf'\b{UPPER}{LOWER}*(\s+{UPPER}{LOWER}*)+\b')

EXCECOES_GLOBAIS = {
    'Tribunal Regional Eleitoral', 'Justiça Eleitoral', 'Spring Boot', 'Spring Data', 'Spring Security',
    'Node.js', 'Vue.js', 'JavaScript', 'TypeScript', 'Playwright', 'PostgreSQL', 'Oracle', 'Docker',
    'Kubernetes', 'Windows', 'Linux', 'Caps Lock', 'Bootstrap', 'Font Awesome', 'List', 'Set', 'Map'
}

def is_camel_case(word):
    return any(c.isupper() for c in word[1:])

def find_title_case():
    results = []
    for root, dirs, files in os.walk('.'):
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        for file in files:
            if any(file.endswith(ext) for ext in EXTENSIONS):
                file_path = os.path.normpath(os.path.join(root, file))
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        for line_num, line in enumerate(f, 1):
                            if line.strip().startswith(('import ', '@', 'package ')): 
                                continue
                            for match in TITLE_CASE_PATTERN.finditer(line):
                                text = match.group().strip()
                                words = text.split()
                                if text.isupper() or any(ex.lower() in text.lower() for ex in EXCECOES_GLOBAIS):
                                    continue
                                if any(is_camel_case(w) for w in words):
                                    continue
                                # NOVA REGRA: Ignora se for apenas uma letra maiúscula no final (identificador)
                                if len(words) >= 2 and len(words[-1]) == 1 and words[-1].isupper():
                                    continue
                                results.append(f"{file_path}{SEPARATOR}{line_num}{SEPARATOR}{text}")
                except (UnicodeDecodeError, PermissionError):
                    continue

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        f.write('\n'.join(results) if results else "Nenhuma instância encontrada.")
    print(f"Relatório salvo com {len(results)} instâncias.")

if __name__ == "__main__":
    find_title_case()

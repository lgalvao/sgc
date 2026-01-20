import os
import re

SOURCE_DIR = 'backend/src/main/java/sgc'
AUDIT_FILE = 'view-validations-audit.md'

GUARANTEED_FIELDS = [
    # Unidade
    {'getter': 'getSigla', 'entity': 'Unidade', 'source': 'VW_UNIDADE'},
    {'getter': 'getNome', 'entity': 'Unidade', 'source': 'VW_UNIDADE'},
    {'getter': 'getTipo', 'entity': 'Unidade', 'source': 'VW_UNIDADE'},

    # Usuario
    {'getter': 'getUnidadeLotacao', 'entity': 'Usuario', 'source': 'VW_USUARIO'},
    {'getter': 'getTituloEleitoral', 'entity': 'Usuario', 'source': 'VW_USUARIO'},
    {'getter': 'getUnidadeCompetencia', 'entity': 'Usuario', 'source': 'VW_USUARIO'},
]

def scan_files():
    results = []

    for root, dirs, files in os.walk(SOURCE_DIR):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                except Exception as e:
                    print(f"Error reading {path}: {e}")
                    continue

                for i, line in enumerate(lines):
                    for check in GUARANTEED_FIELDS:
                        getter = check['getter']
                        # Regex to match getter followed by null check
                        # matches: getSigla() != null, getSigla() == null
                        # Also matches: getSigla() == null
                        pattern = f"{getter}\(\)\s*(!=|==)\s*null"

                        if re.search(pattern, line):
                            results.append({
                                'file': path,
                                'line': i + 1,
                                'content': line.strip(),
                                'field': getter,
                                'guarantee': check['source']
                            })
    return results

def generate_report(results):
    with open(AUDIT_FILE, 'w', encoding='utf-8') as f:
        f.write("# View Validations Audit\n\n")
        f.write("Validations found on fields guaranteed by Database Views:\n\n")

        if not results:
            f.write("No redundant validations found.\n")
            return

        # Group by file
        files = {}
        for item in results:
            if item['file'] not in files:
                files[item['file']] = []
            files[item['file']].append(item)

        for path, items in files.items():
            f.write(f"### {os.path.basename(path)}\n")
            f.write(f"`{path}`\n\n")
            f.write("| Line | Field | Source View | Code |\n")
            f.write("|------|-------|-------------|------|\n")
            for item in items:
                # Escape pipe in code
                content = item['content'].replace("|", "\\|")
                f.write(f"| {item['line']} | `{item['field']}()` | {item['guarantee']} | `{content}` |\n")
            f.write("\n")

if __name__ == '__main__':
    print("Scanning for redundant view validations...")
    data = scan_files()
    generate_report(data)
    print(f"Report generated: {AUDIT_FILE}")

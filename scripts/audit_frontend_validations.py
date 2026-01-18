import os
import re

FRONTEND_DIR = 'frontend/src'
BACKEND_DIR = 'backend/src/main/java/sgc'
REPORT_FILE = 'frontend-backend-validation-comparison.md'

def scan_frontend():
    results = []
    for root, dirs, files in os.walk(FRONTEND_DIR):
        for file in files:
            if file.endswith('.vue'):
                path = os.path.join(root, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                except:
                    continue

                for i, line in enumerate(lines):
                    # Check for :disabled logic (often used for validation)
                    if ':disabled' in line:
                        results.append({
                            'file': path,
                            'line': i + 1,
                            'type': 'disabled_check',
                            'content': line.strip()
                        })
                    # Check for required attribute
                    if 'required' in line and '<' in line:
                        results.append({
                            'file': path,
                            'line': i + 1,
                            'type': 'html_required',
                            'content': line.strip()
                        })
                    # Check for explicit validation rules (v-validate or rules props - generic)
                    if ':rules' in line or 'v-model' in line:
                         if 'rule' in line.lower() or 'valid' in line.lower():
                            results.append({
                                'file': path,
                                'line': i + 1,
                                'type': 'vue_rule',
                                'content': line.strip()
                            })
    return results

def scan_backend():
    results = []
    for root, dirs, files in os.walk(BACKEND_DIR):
        for file in files:
            if file.endswith('.java') and ('Dto' in file or 'Request' in file):
                path = os.path.join(root, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                except:
                    continue

                for i, line in enumerate(lines):
                    if '@NotNull' in line or '@NotEmpty' in line or '@NotBlank' in line or '@Size' in line:
                        results.append({
                            'file': path,
                            'line': i + 1,
                            'type': 'bean_validation',
                            'content': line.strip()
                        })
    return results

def generate_report(fe_data, be_data):
    with open(REPORT_FILE, 'w', encoding='utf-8') as f:
        f.write("# Frontend vs Backend Validation Audit\n\n")

        f.write("## Backend Validations (DTOs)\n")
        f.write("| File | Line | Annotation | Code |\n")
        f.write("|------|------|------------|------|\n")
        for item in be_data:
            annotation = item['content'].split('(')[0].split()[0]
            f.write(f"| {os.path.basename(item['file'])} | {item['line']} | {annotation} | `{item['content']}` |\n")

        f.write("\n## Frontend Validations (Vue)\n")
        f.write("| File | Line | Type | Code |\n")
        f.write("|------|------|------|------|\n")
        for item in fe_data:
            # truncate long lines
            content = item['content'][:100] + ('...' if len(item['content']) > 100 else '')
            content = content.replace("|", "\\|")
            f.write(f"| {os.path.basename(item['file'])} | {item['line']} | {item['type']} | `{content}` |\n")

if __name__ == '__main__':
    print("Scanning validations...")
    fe = scan_frontend()
    be = scan_backend()
    generate_report(fe, be)
    print(f"Report generated: {REPORT_FILE}")

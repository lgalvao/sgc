import os
import argparse
from datetime import datetime

def analyze_tests(backend_dir='backend'):
    JAVA_EXT = '.java'

    backend_src = os.path.join(backend_dir, 'src/main/java')
    backend_test = os.path.join(backend_dir, 'src/test/java')

    if not os.path.exists(backend_src):
        print(f"Erro: Diretório de origem não encontrado: {backend_src}")
        return

    source_files = []
    test_files = set()

    for root, dirs, files in os.walk(backend_src):
        for file in files:
            if file.endswith(JAVA_EXT) and file != 'package-info.java':
                full_path = os.path.join(root, file)
                rel_path = os.path.relpath(full_path, backend_src)
                source_files.append(rel_path)

    if os.path.exists(backend_test):
        for root, dirs, files in os.walk(backend_test):
            for file in files:
                if file.endswith(JAVA_EXT):
                    test_files.add(file)

    # Analysis
    report = {
        "Controllers": {"tested": [], "untested": []},
        "Services": {"tested": [], "untested": []},
        "Facades": {"tested": [], "untested": []},
        "Mappers": {"tested": [], "untested": []},
        "Models": {"tested": [], "untested": []},
        "DTOs": {"tested": [], "untested": []},
        "Repositories": {"tested": [], "untested": []},
        "Others": {"tested": [], "untested": []}
    }

    stats = {"total": 0, "tested": 0}

    for src_rel in source_files:
        class_name = os.path.basename(src_rel).replace(JAVA_EXT, '')

        expected_tests = [
            class_name + 'Test' + JAVA_EXT,
            class_name + 'CoverageTest' + JAVA_EXT,
            class_name + 'UnitTest' + JAVA_EXT,
            class_name + 'IntegrationTest' + JAVA_EXT
        ]

        is_tested = any(t in test_files for t in expected_tests)

        category = "Others"
        if "Controller" in class_name: category = "Controllers"
        elif "Service" in class_name or "Policy" in class_name: category = "Services"
        elif "Facade" in class_name: category = "Facades"
        elif "Mapper" in class_name: category = "Mappers"
        elif "Dto" in class_name or "Request" in class_name or "Response" in class_name: category = "DTOs"
        elif "Repo" in class_name: category = "Repositories"
        elif "/model/" in src_rel.replace('\\', '/') or "/dominio/" in src_rel.replace('\\', '/'): category = "Models"

        stats["total"] += 1
        if is_tested:
            stats["tested"] += 1
            report[category]["tested"].append(src_rel)
        else:
            report[category]["untested"].append(src_rel)

    # Output Markdown
    output = []
    output.append("# Relatório de Cobertura de Testes Unitários (Backend)\n")
    output.append(f"**Data:** {datetime.now().strftime('%d/%m/%Y %H:%M:%S')}")
    output.append(f"**Total de Classes:** {stats['total']}")
    output.append(f"**Com Testes Unitários:** {stats['tested']}")
    output.append(f"**Sem Testes Unitários:** {stats['total'] - stats['tested']}")
    
    if stats['total'] > 0:
        coverage = stats['tested'] / stats['total'] * 100
        output.append(f"**Cobertura (Arquivos):** {coverage:.2f}%\n")
    else:
        output.append("**Cobertura (Arquivos):** 0%\n")

    output.append("## Detalhamento por Categoria\n")

    priority_categories = ["Controllers", "Facades", "Services", "Mappers"]
    secondary_categories = ["Models", "Repositories", "DTOs", "Others"]

    for cat in priority_categories + secondary_categories:
        tested = len(report[cat]["tested"])
        untested = len(report[cat]["untested"])
        total = tested + untested
        if total == 0: continue

        output.append(f"### {cat} ({tested}/{total} testados)")
        if untested > 0:
            output.append(f"**Faltando Testes ({untested}):**")
            for item in sorted(report[cat]["untested"]):
                output.append(f"- `{item}`")
        else:
            output.append("✅ Todos cobertos.")
        output.append("")

    return "\n".join(output)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analisa a cobertura de arquivos de teste no backend.')
    parser.add_argument('--dir', default='backend', help='Diretório raiz do backend')
    parser.add_argument('--output', default='unit-test-report.md', help='Arquivo de saída (Markdown)')
    
    args = parser.parse_args()

    report_content = analyze_tests(args.dir)
    if report_content:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(report_content)
        print(f"Relatório gerado em: {args.output}")

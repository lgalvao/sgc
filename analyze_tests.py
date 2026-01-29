import os

source_files = []
test_files = set()

backend_src = 'backend/src/main/java'
backend_test = 'backend/src/test/java'

for root, dirs, files in os.walk(backend_src):
    for file in files:
        if file.endswith('.java') and file != 'package-info.java':
            full_path = os.path.join(root, file)
            # Rel path from src root (e.g., sgc/analise/AnaliseService.java)
            rel_path = os.path.relpath(full_path, backend_src)
            source_files.append(rel_path)

for root, dirs, files in os.walk(backend_test):
    for file in files:
        if file.endswith('.java'):
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
    class_name = os.path.basename(src_rel).replace('.java', '')
    
    # Heuristic for test name: ClassNameTest.java or ClassNameCoverageTest.java
    # We look for ANY file in test_files that matches expected patterns
    expected_tests = [class_name + 'Test.java', class_name + 'CoverageTest.java', class_name + 'UnitTest.java']
    
    is_tested = any(t in test_files for t in expected_tests)
    
    category = "Others"
    if "Controller" in class_name: category = "Controllers"
    elif "Service" in class_name or "Policy" in class_name: category = "Services" # Policies often have logic
    elif "Facade" in class_name: category = "Facades"
    elif "Mapper" in class_name: category = "Mappers"
    elif "Dto" in class_name or "Request" in class_name or "Response" in class_name: category = "DTOs"
    elif "Repo" in class_name: category = "Repositories"
    elif "/model/" in src_rel or "/dominio/" in src_rel: category = "Models"
    
    stats["total"] += 1
    if is_tested:
        stats["tested"] += 1
        report[category]["tested"].append(src_rel)
    else:
        report[category]["untested"].append(src_rel)

# Output Markdown
print(f"# Relatório de Cobertura de Testes Unitários (Backend)\n")
print(f"**Data:** 29/01/2026")
print(f"**Total de Classes:** {stats['total']}")
print(f"**Com Testes Unitários:** {stats['tested']}")
print(f"**Sem Testes Unitários:** {stats['total'] - stats['tested']}")
if stats['total'] > 0:
    print(f"**Cobertura (Arquivos):** {stats['tested'] / stats['total'] * 100:.2f}%\n")
else:
    print(f"**Cobertura (Arquivos):** 0%\n")

print("## Detalhamento por Categoria\n")

priority_categories = ["Controllers", "Facades", "Services", "Mappers"]
secondary_categories = ["Models", "Repositories", "DTOs", "Others"]

for cat in priority_categories + secondary_categories:
    tested = len(report[cat]["tested"])
    untested = len(report[cat]["untested"])
    total = tested + untested
    if total == 0: continue
    
    print(f"### {cat} ({tested}/{total} testados)")
    if untested > 0:
        print(f"**Faltando Testes ({untested}):**")
        for item in sorted(report[cat]["untested"]):
            print(f"- `{item}`")
    else:
        print("✅ Todos cobertos.")
    print("")


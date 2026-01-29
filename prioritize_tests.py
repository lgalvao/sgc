import re

input_file = "unit-test-report.md"
output_file = "prioritized-tests.md"

p1_patterns = [
    r"Service.java$", r"Facade.java$", r"Policy.java$", r"Validator.java$",
    r"Listener.java$", r"Factory.java$", r"Builder.java$", r"Manager.java$",
    r"Access.*.java$", r"Sanitiz.*.java$", r"Provider.java$", r"Calculat.*.java$"
]

p2_patterns = [
    r"Controller.java$", r"Mapper.java$"
]

# Patterns to downgrade to P3 (even if they match others, though unlikely with above)
ignore_patterns = [
    r"Mock.java$", r"Test.java$"
]

# Structural patterns to ignore in P1/P2 (Interfaces, Annotations, simple Exceptions)
structural_patterns = [
    r"AccessPolicy.java$", # Interface
    r"SanitizarHtml.java$", # Annotation
    r"Erro.*.java$" # Exceptions (simple)
]

prioritized = {
    "P1": [],
    "P2": [],
    "P3": []
}

try:
    with open(input_file, "r") as f:
        lines = f.readlines()

    current_file = None
    for line in lines:
        if line.strip().startswith("- `"):
            file_path = line.strip().replace("- `", "").replace("`", "")
            
            if any(re.search(pat, file_path) for pat in ignore_patterns):
                continue

            # Classify
            is_p1 = any(re.search(pat, file_path) for pat in p1_patterns)
            is_p2 = any(re.search(pat, file_path) for pat in p2_patterns)
            
            is_structural = any(re.search(pat, file_path) for pat in structural_patterns)

            if is_structural:
                prioritized["P3"].append(file_path)
            elif is_p1:
                prioritized["P1"].append(file_path)
            elif is_p2:
                prioritized["P2"].append(file_path)
            else:
                prioritized["P3"].append(file_path)

    # Sort
    for k in prioritized:
        prioritized[k].sort()

    # Generate Output
    with open(output_file, "w") as out:
        out.write("# Plano de Priorização de Testes Unitários\n\n")
        
        out.write("## 🔴 P1: Críticos (Lógica de Negócio e Segurança)\n")
        out.write("Estas classes contêm regras de negócio, validações, segurança ou orquestração complexa. A falta de testes aqui representa alto risco.\n\n")
        if not prioritized["P1"]:
            out.write("✅ **Nenhuma pendência crítica de lógica encontrada.**\n")
        for f in prioritized["P1"]:
            out.write(f"- [ ] `{f}`\n")
        
        out.write("\n## 🟡 P2: Importantes (Integração e Contratos)\n")
        out.write("Controladores e Mappers. Importantes para garantir que a API respeite os contratos e que os dados sejam transformados corretamente.\n\n")
        if not prioritized["P2"]:
            out.write("_Nenhum arquivo encontrado._\n")
        for f in prioritized["P2"]:
            out.write(f"- [ ] `{f}`\n")
        
        out.write("\n## 🟢 P3: Baixa Prioridade (Dados e Infraestrutura)\n")
        out.write("DTOs, Modelos (Getters/Setters), Repositórios (Interfaces) e Configurações. Geralmente cobertos por testes de integração ou seguros por natureza (código gerado/boilerplate).\n\n")
        if not prioritized["P3"]:
            out.write("_Nenhum arquivo encontrado._\n")
        for f in prioritized["P3"]:
            out.write(f"- [ ] `{f}`\n")

    print(f"Prioritization complete. Found {len(prioritized['P1'])} P1, {len(prioritized['P2'])} P2, {len(prioritized['P3'])} P3.")

except FileNotFoundError:
    print(f"Error: {input_file} not found. Please run the analysis step first.")
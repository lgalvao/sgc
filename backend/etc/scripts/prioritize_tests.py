import re
import argparse
import os

def prioritize(input_file, output_file):
    p1_patterns = [
        r"Service.java$", r"Facade.java$", r"Policy.java$", r"Validator.java$",
        r"Listener.java$", r"Factory.java$", r"Builder.java$", r"Manager.java$",
        r"Access.*.java$", r"Sanitiz.*.java$", r"Provider.java$", r"Calculat.*.java$"
    ]

    p2_patterns = [
        r"Controller.java$", r"Mapper.java$"
    ]

    # Patterns to downgrade to P3
    ignore_patterns = [
        r"Mock.java$", r"Test.java$"
    ]

    # Structural patterns to ignore in P1/P2
    structural_patterns = [
        r"AccessPolicy.java$", # Interface
        r"SanitizarHtml.java$", # Annotation
        r"Erro.*.java$" # Exceptions
    ]

    prioritized = {
        "P1": [],
        "P2": [],
        "P3": []
    }

    if not os.path.exists(input_file):
        print(f"Erro: Arquivo de entrada não encontrado: {input_file}")
        return

    try:
        with open(input_file, "r", encoding='utf-8') as f:
            lines = f.readlines()

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
        with open(output_file, "w", encoding='utf-8') as out:
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
            out.write("DTOs, Modelos (Getters/Setters), Repositórios (Interfaces) e Configurações. Geralmente cobertos por testes de integração ou seguros por natureza.\n\n")
            if not prioritized["P3"]:
                out.write("_Nenhum arquivo encontrado._\n")
            for f in prioritized["P3"]:
                out.write(f"- [ ] `{f}`\n")

        print(f"Priorização concluída. Encontrados {len(prioritized['P1'])} P1, {len(prioritized['P2'])} P2, {len(prioritized['P3'])} P3.")
        print(f"Plano gerado em: {output_file}")

    except Exception as e:
        print(f"Erro ao processar priorização: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Prioriza a criação de testes baseado no relatório de análise.')
    parser.add_argument('--input', default='unit-test-report.md', help='Arquivo de entrada (Markdown gerado pelo analyze_tests.py)')
    parser.add_argument('--output', default='prioritized-tests.md', help='Arquivo de saída (Markdown)')

    args = parser.parse_args()
    prioritize(args.input, args.output)

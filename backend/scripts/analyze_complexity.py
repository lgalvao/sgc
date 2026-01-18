#!/usr/bin/env python3
"""
Analisa complexidade do código usando dados do Jacoco e outras métricas.
Gera um ranking detalhado das classes mais complexas do backend.
"""

import csv
import sys
from pathlib import Path
from typing import List, Dict, NamedTuple
from dataclasses import dataclass


@dataclass
class ClassMetrics:
    """Métricas de uma classe do backend."""
    package: str
    name: str
    instructions_covered: int
    instructions_missed: int
    branches_covered: int
    branches_missed: int
    lines_covered: int
    lines_missed: int
    complexity_covered: int
    complexity_missed: int
    methods_covered: int
    methods_missed: int
    
    @property
    def total_instructions(self) -> int:
        return self.instructions_covered + self.instructions_missed
    
    @property
    def total_branches(self) -> int:
        return self.branches_covered + self.branches_missed
    
    @property
    def total_lines(self) -> int:
        return self.lines_covered + self.lines_missed
    
    @property
    def total_complexity(self) -> int:
        """Complexidade ciclomática total."""
        return self.complexity_covered + self.complexity_missed
    
    @property
    def total_methods(self) -> int:
        return self.methods_covered + self.methods_missed
    
    @property
    def avg_complexity_per_method(self) -> float:
        """Complexidade média por método."""
        if self.total_methods == 0:
            return 0.0
        return self.total_complexity / self.total_methods
    
    @property
    def branch_coverage_percentage(self) -> float:
        """Percentual de cobertura de branches."""
        if self.total_branches == 0:
            return 100.0
        return (self.branches_covered / self.total_branches) * 100
    
    @property
    def complexity_score(self) -> float:
        """
        Score composto de complexidade baseado em:
        - Complexidade ciclomática total (peso 40%)
        - Total de branches (peso 30%)
        - Linhas de código (peso 20%)
        - Complexidade média por método (peso 10%)
        """
        # Normalização
        norm_complexity = self.total_complexity
        norm_branches = self.total_branches
        norm_lines = self.total_lines / 10  # Reduz impacto de classes muito grandes
        norm_avg_complexity = self.avg_complexity_per_method * 5
        
        return (
            norm_complexity * 0.40 +
            norm_branches * 0.30 +
            norm_lines * 0.20 +
            norm_avg_complexity * 0.10
        )
    
    @property
    def full_name(self) -> str:
        return f"{self.package}.{self.name}"


def parse_jacoco_csv(csv_path: Path) -> List[ClassMetrics]:
    """Parse o CSV do Jacoco e extrai métricas."""
    metrics = []
    
    with open(csv_path, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # Pula entradas que não são classes (ex: package summaries)
            if not row['CLASS']:
                continue
            
            metrics.append(ClassMetrics(
                package=row['PACKAGE'],
                name=row['CLASS'],
                instructions_covered=int(row['INSTRUCTION_COVERED']),
                instructions_missed=int(row['INSTRUCTION_MISSED']),
                branches_covered=int(row['BRANCH_COVERED']),
                branches_missed=int(row['BRANCH_MISSED']),
                lines_covered=int(row['LINE_COVERED']),
                lines_missed=int(row['LINE_MISSED']),
                complexity_covered=int(row['COMPLEXITY_COVERED']),
                complexity_missed=int(row['COMPLEXITY_MISSED']),
                methods_covered=int(row['METHOD_COVERED']),
                methods_missed=int(row['METHOD_MISSED'])
            ))
    
    return metrics


def categorize_class(class_name: str) -> str:
    """Categoriza a classe baseado no sufixo."""
    if 'Controller' in class_name:
        return 'Controller'
    elif 'Service' in class_name or 'Facade' in class_name:
        return 'Service/Facade'
    elif 'Repo' in class_name or 'Repository' in class_name:
        return 'Repository'
    elif 'Mapper' in class_name:
        return 'Mapper'
    elif 'Listener' in class_name:
        return 'Listener'
    elif 'Request' in class_name or 'Response' in class_name or 'Dto' in class_name:
        return 'DTO'
    elif class_name[0].isupper() and not any(x in class_name for x in ['Service', 'Controller', 'Repo']):
        return 'Model/Entity'
    else:
        return 'Other'


def generate_markdown_report(metrics: List[ClassMetrics], output_path: Path):
    """Gera o relatório em Markdown."""
    
    # Ordena por complexity score
    sorted_metrics = sorted(metrics, key=lambda m: m.complexity_score, reverse=True)
    
    # Estatísticas gerais
    total_classes = len(metrics)
    total_complexity = sum(m.total_complexity for m in metrics)
    total_branches = sum(m.total_branches for m in metrics)
    total_lines = sum(m.total_lines for m in metrics)
    avg_complexity = total_complexity / total_classes if total_classes > 0 else 0
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write("# Ranking de Complexidade do Backend - SGC\n\n")
        f.write("Este relatório apresenta uma análise detalhada da complexidade do código backend, ")
        f.write("baseado em métricas do Jacoco e análise de complexidade ciclomática.\n\n")
        
        f.write("## Resumo Executivo\n\n")
        f.write(f"- **Total de Classes Analisadas:** {total_classes}\n")
        f.write(f"- **Complexidade Ciclomática Total:** {total_complexity}\n")
        f.write(f"- **Total de Branches:** {total_branches}\n")
        f.write(f"- **Total de Linhas de Código:** {total_lines}\n")
        f.write(f"- **Complexidade Média por Classe:** {avg_complexity:.2f}\n\n")
        
        f.write("## Metodologia\n\n")
        f.write("O **Complexity Score** é calculado através da fórmula:\n\n")
        f.write("```\n")
        f.write("Score = (Complexidade Ciclomática × 0.40) + \n")
        f.write("        (Total de Branches × 0.30) + \n")
        f.write("        (Linhas de Código ÷ 10 × 0.20) + \n")
        f.write("        (Complexidade Média por Método × 5 × 0.10)\n")
        f.write("```\n\n")
        f.write("Esta fórmula pondera múltiplos fatores de complexidade:\n")
        f.write("- **Complexidade Ciclomática** (40%): Número de caminhos independentes no código\n")
        f.write("- **Branches** (30%): Pontos de decisão (if, switch, loops)\n")
        f.write("- **Linhas de Código** (20%): Tamanho da classe\n")
        f.write("- **Complexidade Média por Método** (10%): Densidade de complexidade\n\n")
        
        # Top 50 classes mais complexas
        f.write("## Top 50 Classes Mais Complexas\n\n")
        f.write("| Rank | Classe | Pacote | Score | Complexity | Branches | Linhas | Métodos | Avg/Método | Categoria |\n")
        f.write("|------|--------|--------|-------|------------|----------|---------|---------|------------|------------|\n")
        
        for i, m in enumerate(sorted_metrics[:50], 1):
            category = categorize_class(m.name)
            f.write(f"| {i} | `{m.name}` | `{m.package}` | {m.complexity_score:.1f} | ")
            f.write(f"{m.total_complexity} | {m.total_branches} | {m.total_lines} | ")
            f.write(f"{m.total_methods} | {m.avg_complexity_per_method:.1f} | {category} |\n")
        
        # Análise por categoria
        f.write("\n## Análise por Categoria\n\n")
        
        categories = {}
        for m in metrics:
            cat = categorize_class(m.name)
            if cat not in categories:
                categories[cat] = []
            categories[cat].append(m)
        
        for category, classes in sorted(categories.items()):
            f.write(f"### {category}\n\n")
            f.write(f"Total de classes: {len(classes)}\n\n")
            
            # Top 10 da categoria
            top_in_category = sorted(classes, key=lambda m: m.complexity_score, reverse=True)[:10]
            
            f.write("| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |\n")
            f.write("|--------|-------|------------|----------|--------|--------------------|\n")
            
            for m in top_in_category:
                f.write(f"| `{m.name}` | {m.complexity_score:.1f} | {m.total_complexity} | ")
                f.write(f"{m.total_branches} | {m.total_lines} | {m.branch_coverage_percentage:.1f}% |\n")
            
            f.write("\n")
        
        # Classes com maior complexidade por método
        f.write("## Top 20 Classes com Maior Complexidade por Método\n\n")
        f.write("Classes onde cada método é, em média, mais complexo:\n\n")
        
        by_avg_complexity = sorted(
            [m for m in metrics if m.total_methods > 0],
            key=lambda m: m.avg_complexity_per_method,
            reverse=True
        )[:20]
        
        f.write("| Rank | Classe | Avg Complexity/Método | Métodos | Total Complexity | Categoria |\n")
        f.write("|------|--------|-----------------------|---------|------------------|------------|\n")
        
        for i, m in enumerate(by_avg_complexity, 1):
            category = categorize_class(m.name)
            f.write(f"| {i} | `{m.name}` | {m.avg_complexity_per_method:.2f} | ")
            f.write(f"{m.total_methods} | {m.total_complexity} | {category} |\n")
        
        # Classes com mais branches
        f.write("\n## Top 20 Classes com Mais Branches\n\n")
        f.write("Classes com maior número de pontos de decisão:\n\n")
        
        by_branches = sorted(metrics, key=lambda m: m.total_branches, reverse=True)[:20]
        
        f.write("| Rank | Classe | Branches | Covered | Missed | Coverage % | Categoria |\n")
        f.write("|------|--------|----------|---------|--------|------------|------------|\n")
        
        for i, m in enumerate(by_branches, 1):
            category = categorize_class(m.name)
            f.write(f"| {i} | `{m.name}` | {m.total_branches} | ")
            f.write(f"{m.branches_covered} | {m.branches_missed} | ")
            f.write(f"{m.branch_coverage_percentage:.1f}% | {category} |\n")
        
        # Ranking completo
        f.write("\n## Ranking Completo de Todas as Classes\n\n")
        f.write("Lista completa ordenada por Complexity Score:\n\n")
        f.write("| Rank | Classe Completa | Score | Complexity | Branches | Linhas | Métodos | Categoria |\n")
        f.write("|------|----------------|-------|------------|----------|--------|---------|------------|\n")
        
        for i, m in enumerate(sorted_metrics, 1):
            category = categorize_class(m.name)
            f.write(f"| {i} | `{m.full_name}` | {m.complexity_score:.1f} | ")
            f.write(f"{m.total_complexity} | {m.total_branches} | {m.total_lines} | ")
            f.write(f"{m.total_methods} | {category} |\n")
        
        f.write("\n## Notas\n\n")
        f.write("- **Complexity**: Complexidade ciclomática (número de caminhos independentes)\n")
        f.write("- **Branches**: Pontos de decisão no código (if, switch, loops, etc.)\n")
        f.write("- **Linhas**: Total de linhas de código (cobertas + não cobertas)\n")
        f.write("- **Métodos**: Total de métodos na classe\n")
        f.write("- **Avg/Método**: Complexidade média por método\n")
        f.write("- **Coverage %**: Percentual de branches cobertos por testes\n\n")
        f.write("---\n\n")
        f.write(f"*Relatório gerado automaticamente a partir dos dados do Jacoco*\n")


def main():
    """Função principal."""
    # Caminhos
    backend_dir = Path(__file__).parent.parent
    csv_path = backend_dir / 'build' / 'reports' / 'jacoco' / 'test' / 'jacocoTestReport.csv'
    output_path = backend_dir.parent / 'complexity-ranking.md'
    
    if not csv_path.exists():
        print(f"Erro: Arquivo {csv_path} não encontrado.")
        print("Execute 'gradle test jacocoTestReport' primeiro.")
        sys.exit(1)
    
    print(f"Lendo dados de: {csv_path}")
    metrics = parse_jacoco_csv(csv_path)
    
    print(f"Analisadas {len(metrics)} classes")
    print(f"Gerando relatório em: {output_path}")
    
    generate_markdown_report(metrics, output_path)
    
    print("✓ Relatório gerado com sucesso!")
    print(f"\nTop 5 classes mais complexas:")
    sorted_metrics = sorted(metrics, key=lambda m: m.complexity_score, reverse=True)
    for i, m in enumerate(sorted_metrics[:5], 1):
        print(f"  {i}. {m.full_name} (Score: {m.complexity_score:.1f})")


if __name__ == '__main__':
    main()

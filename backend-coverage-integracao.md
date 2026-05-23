# Auditoria de Cobertura Backend (Apenas Testes de Integração) - SGC

Este relatório apresenta a medição de cobertura de código do backend obtida **exclusivamente pelos testes de integração** (`integrationTest`), desconsiderando os testes unitários.

## Resumo Geral de Cobertura (Exclusivo Integração)
Abaixo estão os dados consolidados obtidos diretamente do relatório JaCoCo (`jacocoIntegrationTestReport.xml`) gerado após a execução dos **575 testes de integração**:

| Métrica | Cobertos | Não Cobertos (Missed) | Total | Cobertura (%) |
| :--- | :---: | :---: | :---: | :---: |
| **Instruções (Instruction)** | 28.715 | 8.984 | 37.699 | **76,17%** |
| **Linhas (Line)** | 6.184 | 1.769 | 7.953 | **77,76%** |
| **Branches (Branch)** | 1.329 | 1.048 | 2.377 | **55,91%** |
| **Complexidade (Complexity)** | 1.847 | 1.140 | 2.987 | **61,83%** |
| **Métodos (Method)** | 1.388 | 387 | 1.775 | **78,20%** |
| **Classes (Class)** | 267 | 35 | 302 | **88,41%** |

---

## Comparativo: Cobertura Global vs. Apenas Integração

Comparando estes resultados com a cobertura global consolidada (que inclui testes unitários + testes de integração) documentada no projeto:

| Métrica | Cobertura Global | Cobertura (Apenas Integração) | Diferença (Redução) |
| :--- | :---: | :---: | :---: |
| **Instruções** | 99,29% | 76,17% | -23,12% |
| **Linhas** | 99,67% | 77,76% | -21,91% |
| **Branches** | 99,37% | 55,91% | -43,46% |

### Análise e Conclusões
* **Testes Unitários Fundamentais**: A queda expressiva na cobertura de ramos (**-43,46%**) e de instruções (**-23,12%**) quando analisamos apenas os testes de integração revela que grande parte das validações finas de negócio, tratamentos de erro específicos e caminhos alternativos é coberta e assegurada pelos testes unitários.
* **Cobertura de Fluxo Principal Robusta**: A cobertura de classes (**88,41%**) e linhas (**77,76%**) puramente através de testes de integração indica que quase todos os fluxos e caminhos felizes das APIs e serviços do SGC são exercitados de ponta a ponta com o contexto Spring ativo e banco H2.

---
_Gerado automaticamente pelo agente Antigravity em 23/05/2026, 12:44:00._

# Auditoria de arquitetura do backend

Gerado em: 2026-06-06T12:15:24.522Z

## Resumo

- Analisados: 67
- Críticos: 5
- Alertas: 6
- OK: 56

## Limites

| Métrica                | Alerta | Crítico |
|------------------------|--------|---------|
| Linhas efetivas        | 400    | 700     |
| Métodos públicos       | 15     | 25      |
| Dependências injetadas | 8      | 12      |

## Hotspots

| Arquivo                            | Tipo       | Linhas | Métodos | Dependências | Severidade | Motivos                                                                 |
|------------------------------------|------------|--------|---------|--------------|------------|-------------------------------------------------------------------------|
| `ProcessoService.java`             | service    | 1301   | 21      | 20           | 🔴 crítico | 1301 linhas (>=700); 21 métodos públicos (>=15); 20 dependências (>=12) |
| `E2eController.java`               | controller | 843    | 22      | 12           | 🔴 crítico | 843 linhas (>=700); 22 métodos públicos (>=15); 12 dependências (>=12)  |
| `SubprocessoTransicaoService.java` | service    | 592    | 17      | 13           | 🔴 crítico | 592 linhas (>=400); 17 métodos públicos (>=15); 13 dependências (>=12)  |
| `SubprocessoService.java`          | service    | 473    | 15      | 12           | 🔴 crítico | 473 linhas (>=400); 15 métodos públicos (>=15); 12 dependências (>=12)  |
| `SubprocessoController.java`       | controller | 438    | 54      | 10           | 🔴 crítico | 438 linhas (>=400); 54 métodos públicos (>=25); 10 dependências (>=8)   |
| `RelatorioService.java`            | service    | 803    | 10      | 9            | 🟡 alerta  | 803 linhas (>=700); 9 dependências (>=8)                                |
| `CadastroFluxoService.java`        | service    | 356    | 13      | 12           | 🟡 alerta  | 12 dependências (>=12)                                                  |
| `ResponsavelUnidadeService.java`   | service    | 316    | 13      | 12           | 🟡 alerta  | 12 dependências (>=12)                                                  |
| `MapaManutencaoService.java`       | service    | 297    | 40      | 6            | 🟡 alerta  | 40 métodos públicos (>=25)                                              |
| `SubprocessoConsultaService.java`  | service    | 224    | 42      | 9            | 🟡 alerta  | 42 métodos públicos (>=25); 9 dependências (>=8)                        |
| `DiagnosticoFluxoService.java`     | service    | 215    | 5       | 13           | 🟡 alerta  | 13 dependências (>=12)                                                  |

## Por que isso importa

Classes com muitas linhas, métodos e dependências são hubs de complexidade:

- cada alteração exige leitura excessiva;
- a chance de duplicar lógica cresce a cada nova funcionalidade;
- testes ficam difíceis de isolar;
- responsabilidades se sobrepõem.

## Primeiro corte sugerido

Começar por `ProcessoService.java` (1301 linhas (>=700), 21 métodos públicos (>=15), 20 dependências (>=12)).
Identificar os casos de uso reais e separar por responsabilidade concreta (consulta, mutação, workflow, notificação).
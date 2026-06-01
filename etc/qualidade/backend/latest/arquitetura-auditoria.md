# Auditoria de arquitetura do backend

Gerado em: 2026-06-01T01:29:10.090Z

## Resumo

- Analisados: 57
- Críticos: 3
- Alertas: 6
- OK: 48

## Limites

| Métrica | Alerta | Crítico |
|---------|--------|---------|
| Linhas efetivas | 400 | 700 |
| Métodos públicos | 15 | 25 |
| Dependências injetadas | 8 | 12 |

## Hotspots

| Arquivo | Tipo | Linhas | Métodos | Dependências | Severidade | Motivos |
|---------|------|--------|---------|--------------|-----------|---------|
| `ProcessoService.java` | service | 1290 | 21 | 18 | 🔴 crítico | 1290 linhas (>=700); 21 métodos públicos (>=15); 18 dependências (>=12) |
| `SubprocessoTransicaoService.java` | service | 592 | 17 | 13 | 🔴 crítico | 592 linhas (>=400); 17 métodos públicos (>=15); 13 dependências (>=12) |
| `SubprocessoController.java` | controller | 479 | 54 | 8 | 🔴 crítico | 479 linhas (>=400); 54 métodos públicos (>=25); 8 dependências (>=8) |
| `RelatorioFacade.java` | facade | 801 | 10 | 9 | 🟡 alerta | 801 linhas (>=700); 9 dependências (>=8) |
| `E2eController.java` | controller | 641 | 16 | 9 | 🟡 alerta | 641 linhas (>=400); 16 métodos públicos (>=15); 9 dependências (>=8) |
| `SubprocessoService.java` | service | 470 | 15 | 11 | 🟡 alerta | 470 linhas (>=400); 15 métodos públicos (>=15); 11 dependências (>=8) |
| `CadastroFluxoService.java` | service | 356 | 13 | 12 | 🟡 alerta | 12 dependências (>=12) |
| `MapaManutencaoService.java` | service | 297 | 40 | 6 | 🟡 alerta | 40 métodos públicos (>=25) |
| `SubprocessoConsultaService.java` | service | 219 | 41 | 7 | 🟡 alerta | 41 métodos públicos (>=25) |

## Por que isso importa

Classes com muitas linhas, métodos e dependências são hubs de complexidade:
- cada alteração exige leitura excessiva;
- a chance de duplicar lógica cresce a cada nova funcionalidade;
- testes ficam difíceis de isolar;
- responsabilidades se sobrepõem.

## Primeiro corte sugerido

Começar por `ProcessoService.java` (1290 linhas (>=700), 21 métodos públicos (>=15), 18 dependências (>=12)).
Identificar os casos de uso reais e separar por responsabilidade concreta (consulta, mutação, workflow, notificação).
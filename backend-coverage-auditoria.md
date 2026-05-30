# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 99.43%
- **Cobertura de Linhas:** 99.62%
- **Cobertura de Branches:** 98.75%
- **Complexidade Total:** 3005

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 583.5 | 285 | 0 | 9 | P1 |
| 2 | `sgc.relatorio.RelatorioFacade` | 311.0 | 147 | 2 | 10 | P1 |
| 3 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 212.0 | 106 | 0 | 0 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoAcessoService` | 186.0 | 93 | 0 | 0 | P1 |
| 5 | `sgc.subprocesso.service.SubprocessoService` | 182.0 | 91 | 0 | 0 | P1 |
| 6 | `sgc.mapa.service.ImpactoMapaService` | 174.0 | 87 | 0 | 0 | P1 |
| 7 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 158.0 | 79 | 0 | 0 | P1 |
| 8 | `sgc.e2e.E2eController` | 156.0 | 78 | 0 | 0 | P1 |
| 9 | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 142.0 | 69 | 1 | 2 | P1 |
| 10 | `sgc.organizacao.service.UnidadeHierarquiaService` | 137.0 | 66 | 2 | 2 | P1 |

## Detalhamento das Lacunas dos Principais Hotspots

### `sgc.processo.service.ProcessoService` (Risco: 583.5)
- **Branches sem cobertura total/parcial (linha(perdidos/total)):** 762(1/10), 807(1/6), 852(1/2), 934(1/6), 1036(3/6), 1040(1/4), 1359(1/6)

### `sgc.relatorio.RelatorioFacade` (Risco: 311.0)
- **Linhas 100% descobertas:** 825, 880
- **Branches sem cobertura total/parcial (linha(perdidos/total)):** 211(1/2), 591(1/4), 619(1/2), 647(2/4), 780(1/4), 805(1/2), 813(1/2), 829(1/4), 879(1/2)

### `sgc.subprocesso.service.SubprocessoNotificacaoService` (Risco: 142.0)
- **Linhas 100% descobertas:** 158
- **Branches sem cobertura total/parcial (linha(perdidos/total)):** 157(2/4)

### `sgc.organizacao.service.UnidadeHierarquiaService` (Risco: 137.0)
- **Linhas 100% descobertas:** 235, 279
- **Branches sem cobertura total/parcial (linha(perdidos/total)):** 234(1/2), 278(1/2)



_Gerado automaticamente pelo toolkit SGC em 29/05/2026, 21:41:10._

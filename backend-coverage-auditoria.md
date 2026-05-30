# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 99.52%
- **Cobertura de Linhas:** 99.67%
- **Cobertura de Branches:** 99.57%
- **Complexidade Total:** 2974

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 562.0 | 281 | 0 | 0 | P1 |
| 2 | `sgc.relatorio.RelatorioFacade` | 297.0 | 147 | 0 | 2 | P1 |
| 3 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 212.0 | 106 | 0 | 0 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoAcessoService` | 186.0 | 93 | 0 | 0 | P1 |
| 5 | `sgc.subprocesso.service.SubprocessoService` | 178.0 | 89 | 0 | 0 | P1 |
| 6 | `sgc.mapa.service.ImpactoMapaService` | 174.0 | 87 | 0 | 0 | P1 |
| 7 | `sgc.e2e.E2eController` | 156.0 | 78 | 0 | 0 | P1 |
| 8 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 156.0 | 78 | 0 | 0 | P1 |
| 9 | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 134.0 | 67 | 0 | 0 | P1 |
| 10 | `sgc.organizacao.service.UnidadeHierarquiaService` | 132.5 | 65 | 1 | 1 | P1 |

## Detalhamento das Lacunas dos Principais Hotspots

### `sgc.relatorio.RelatorioFacade` (Risco: 297.0)
- **Branches sem cobertura total/parcial (linha(perdidos/total)):** 207(1/2), 783(1/4)

### `sgc.organizacao.service.UnidadeHierarquiaService` (Risco: 132.5)
- **Linhas 100% descobertas:** 235
- **Branches sem cobertura total/parcial (linha(perdidos/total)):** 234(1/2)



_Gerado automaticamente pelo toolkit SGC em 30/05/2026, 00:49:45._

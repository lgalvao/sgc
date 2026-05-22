# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 99.02%
- **Cobertura de Linhas:** 99.48%
- **Cobertura de Branches:** 96.89%
- **Complexidade Total:** 2990

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 593.5 | 287 | 0 | 13 | P1 |
| 2 | `sgc.relatorio.RelatorioFacade` | 315.5 | 148 | 0 | 13 | P1 |
| 3 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 212.0 | 106 | 0 | 0 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoAcessoService` | 186.0 | 93 | 0 | 0 | P1 |
| 5 | `sgc.subprocesso.service.SubprocessoService` | 182.0 | 91 | 0 | 0 | P1 |
| 6 | `sgc.mapa.service.ImpactoMapaService` | 174.0 | 87 | 0 | 0 | P1 |
| 7 | `sgc.e2e.E2eController` | 167.5 | 78 | 1 | 7 | P1 |
| 8 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 158.0 | 79 | 0 | 0 | P1 |
| 9 | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 151.0 | 68 | 3 | 8 | P1 |
| 10 | `sgc.organizacao.service.UnidadeHierarquiaService` | 132.0 | 66 | 0 | 0 | P1 |
| 11 | `sgc.mapa.service.MapaManutencaoService` | 132.0 | 66 | 0 | 0 | P1 |
| 12 | `sgc.subprocesso.SubprocessoController` | 130.0 | 65 | 0 | 0 | P1 |
| 13 | `sgc.organizacao.service.ResponsavelUnidadeService` | 124.0 | 62 | 0 | 0 | P1 |
| 14 | `sgc.subprocesso.service.SubprocessoConsultaService` | 122.0 | 61 | 0 | 0 | P1 |
| 15 | `sgc.seguranca.SgcPermissionEvaluator` | 120.0 | 60 | 0 | 0 | P1 |
| 16 | `sgc.alerta.AlertaFacade` | 118.0 | 59 | 0 | 0 | P1 |
| 17 | `sgc.subprocesso.service.CadastroFluxoService` | 112.0 | 56 | 0 | 0 | P1 |
| 18 | `sgc.subprocesso.service.SubprocessoValidacaoService` | 104.0 | 52 | 0 | 0 | P1 |
| 19 | `sgc.processo.painel.PainelFacade` | 100.0 | 50 | 0 | 0 | P1 |
| 20 | `sgc.comum.erros.RestExceptionHandler` | 76.0 | 38 | 0 | 0 | P1 |


_Gerado automaticamente pelo toolkit SGC em 22/05/2026, 23:37:18._

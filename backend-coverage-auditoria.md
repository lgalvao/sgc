# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 99.29%
- **Cobertura de Linhas:** 99.67%
- **Cobertura de Branches:** 99.37%
- **Complexidade Total:** 2987

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 570.0 | 285 | 0 | 0 | P1 |
| 2 | `sgc.relatorio.RelatorioFacade` | 295.5 | 147 | 0 | 1 | P1 |
| 3 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 212.0 | 106 | 0 | 0 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoAcessoService` | 186.0 | 93 | 0 | 0 | P1 |
| 5 | `sgc.subprocesso.service.SubprocessoService` | 182.0 | 91 | 0 | 0 | P1 |
| 6 | `sgc.mapa.service.ImpactoMapaService` | 174.0 | 87 | 0 | 0 | P1 |
| 7 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 158.0 | 79 | 0 | 0 | P1 |
| 8 | `sgc.e2e.E2eController` | 156.0 | 78 | 0 | 0 | P1 |
| 9 | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 142.0 | 69 | 1 | 2 | P1 |
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


_Gerado automaticamente pelo toolkit SGC em 23/05/2026, 00:23:33._

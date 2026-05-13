# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 96.33%
- **Cobertura de Linhas:** 97.25%
- **Cobertura de Branches:** 90.14%
- **Complexidade Total:** 2783

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 656.5 | 291 | 25 | 33 | P1 |
| 2 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 314.0 | 107 | 43 | 38 | P1 |
| 3 | `sgc.mapa.service.ImpactoMapaService` | 196.0 | 87 | 4 | 12 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoAcessoService` | 189.0 | 93 | 0 | 2 | P1 |
| 5 | `sgc.subprocesso.service.SubprocessoService` | 187.0 | 91 | 2 | 2 | P1 |
| 6 | `sgc.e2e.E2eController` | 185.0 | 78 | 8 | 14 | P1 |
| 7 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 180.0 | 73 | 16 | 12 | P1 |
| 8 | `sgc.organizacao.service.UnidadeHierarquiaService` | 143.5 | 66 | 4 | 5 | P1 |
| 9 | `sgc.subprocesso.SubprocessoController` | 134.0 | 67 | 0 | 0 | P1 |
| 10 | `sgc.mapa.service.MapaManutencaoService` | 131.5 | 64 | 2 | 1 | P1 |
| 11 | `sgc.relatorio.RelatorioFacade` | 130.0 | 52 | 17 | 6 | P1 |
| 12 | `sgc.organizacao.service.ResponsavelUnidadeService` | 128.0 | 62 | 1 | 2 | P1 |
| 13 | `sgc.subprocesso.service.SubprocessoConsultaService` | 123.5 | 61 | 0 | 1 | P1 |
| 14 | `sgc.seguranca.SgcPermissionEvaluator` | 122.5 | 60 | 1 | 1 | P1 |
| 15 | `sgc.alerta.AlertaFacade` | 115.5 | 55 | 1 | 3 | P1 |
| 16 | `sgc.subprocesso.service.CadastroFluxoService` | 112.5 | 53 | 2 | 3 | P1 |
| 17 | `sgc.processo.painel.PainelFacade` | 105.0 | 51 | 0 | 2 | P1 |
| 18 | `sgc.subprocesso.service.SubprocessoValidacaoService` | 104.0 | 52 | 0 | 0 | P1 |
| 19 | `sgc.feedback.FeedbackService` | 99.0 | 36 | 9 | 12 | P1 |
| 20 | `sgc.comum.erros.RestExceptionHandler` | 96.5 | 38 | 7 | 9 | P1 |


_Gerado automaticamente pelo toolkit SGC em 13/05/2026, 21:43:54._

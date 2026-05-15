# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 98.18%
- **Cobertura de Linhas:** 98.7%
- **Cobertura de Branches:** 95%
- **Complexidade Total:** 2772

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 611.0 | 286 | 9 | 20 | P1 |
| 2 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 231.0 | 106 | 4 | 10 | P1 |
| 3 | `sgc.subprocesso.service.SubprocessoAcessoService` | 189.0 | 93 | 0 | 2 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoService` | 187.0 | 91 | 2 | 2 | P1 |
| 5 | `sgc.mapa.service.ImpactoMapaService` | 178.5 | 87 | 0 | 3 | P1 |
| 6 | `sgc.e2e.E2eController` | 169.0 | 78 | 1 | 8 | P1 |
| 7 | `sgc.organizacao.service.UnidadeHierarquiaService` | 143.5 | 66 | 4 | 5 | P1 |
| 8 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 140.5 | 68 | 3 | 1 | P1 |
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
| 19 | `sgc.feedback.FeedbackService` | 79.0 | 36 | 4 | 2 | P1 |
| 20 | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 79.0 | 36 | 1 | 4 | P1 |


_Gerado automaticamente pelo toolkit SGC em 14/05/2026, 22:16:04._

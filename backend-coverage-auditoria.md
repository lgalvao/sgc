# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 98.32%
- **Cobertura de Linhas:** 98.95%
- **Cobertura de Branches:** 95.75%
- **Complexidade Total:** 2909

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 604.0 | 287 | 6 | 16 | P1 |
| 2 | `sgc.relatorio.RelatorioFacade` | 316.0 | 144 | 4 | 16 | P1 |
| 3 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 212.0 | 106 | 0 | 0 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoService` | 187.0 | 91 | 2 | 2 | P1 |
| 5 | `sgc.subprocesso.service.SubprocessoAcessoService` | 186.0 | 93 | 0 | 0 | P1 |
| 6 | `sgc.mapa.service.ImpactoMapaService` | 174.0 | 87 | 0 | 0 | P1 |
| 7 | `sgc.e2e.E2eController` | 169.0 | 78 | 1 | 8 | P1 |
| 8 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 140.5 | 68 | 3 | 1 | P1 |
| 9 | `sgc.mapa.service.MapaManutencaoService` | 138.0 | 66 | 3 | 2 | P1 |
| 10 | `sgc.organizacao.service.UnidadeHierarquiaService` | 137.0 | 66 | 2 | 2 | P1 |
| 11 | `sgc.subprocesso.SubprocessoController` | 134.0 | 67 | 0 | 0 | P1 |
| 12 | `sgc.organizacao.service.ResponsavelUnidadeService` | 128.0 | 62 | 1 | 2 | P1 |
| 13 | `sgc.alerta.AlertaFacade` | 123.5 | 59 | 1 | 3 | P1 |
| 14 | `sgc.subprocesso.service.SubprocessoConsultaService` | 123.5 | 61 | 0 | 1 | P1 |
| 15 | `sgc.seguranca.SgcPermissionEvaluator` | 122.5 | 60 | 1 | 1 | P1 |
| 16 | `sgc.subprocesso.service.CadastroFluxoService` | 118.0 | 55 | 2 | 4 | P1 |
| 17 | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 111.5 | 53 | 1 | 3 | P1 |
| 18 | `sgc.processo.painel.PainelFacade` | 105.0 | 51 | 0 | 2 | P1 |
| 19 | `sgc.subprocesso.service.SubprocessoValidacaoService` | 104.0 | 52 | 0 | 0 | P1 |
| 20 | `sgc.feedback.FeedbackService` | 79.0 | 36 | 4 | 2 | P1 |


_Gerado automaticamente pelo toolkit SGC em 21/05/2026, 12:59:29._

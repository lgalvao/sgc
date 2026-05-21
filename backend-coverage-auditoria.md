# Auditoria de Cobertura Backend - SGC

## Resumo Geral
- **Cobertura Global (Instruções):** 98.72%
- **Cobertura de Linhas:** 99.21%
- **Cobertura de Branches:** 97.37%
- **Complexidade Total:** 2903

## Top 10 Hotspots de Qualidade (Maior Risco)
Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.

| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |
|------|--------|-------|--------------|---------------------|-----------------------|------------|
| 1 | `sgc.processo.service.ProcessoService` | 604.0 | 287 | 6 | 16 | P1 |
| 2 | `sgc.relatorio.RelatorioFacade` | 317.5 | 144 | 4 | 17 | P1 |
| 3 | `sgc.organizacao.ValidadorDadosOrganizacionais` | 212.0 | 106 | 0 | 0 | P1 |
| 4 | `sgc.subprocesso.service.SubprocessoAcessoService` | 186.0 | 93 | 0 | 0 | P1 |
| 5 | `sgc.subprocesso.service.SubprocessoService` | 183.0 | 91 | 1 | 0 | P1 |
| 6 | `sgc.mapa.service.ImpactoMapaService` | 174.0 | 87 | 0 | 0 | P1 |
| 7 | `sgc.e2e.E2eController` | 169.0 | 78 | 1 | 8 | P1 |
| 8 | `sgc.subprocesso.service.SubprocessoTransicaoService` | 139.0 | 68 | 3 | 0 | P1 |
| 9 | `sgc.mapa.service.MapaManutencaoService` | 134.0 | 66 | 2 | 0 | P1 |
| 10 | `sgc.subprocesso.SubprocessoController` | 134.0 | 67 | 0 | 0 | P1 |
| 11 | `sgc.organizacao.service.UnidadeHierarquiaService` | 132.0 | 66 | 0 | 0 | P1 |
| 12 | `sgc.organizacao.service.ResponsavelUnidadeService` | 124.0 | 62 | 0 | 0 | P1 |
| 13 | `sgc.subprocesso.service.SubprocessoConsultaService` | 122.0 | 61 | 0 | 0 | P1 |
| 14 | `sgc.seguranca.SgcPermissionEvaluator` | 120.0 | 60 | 0 | 0 | P1 |
| 15 | `sgc.alerta.AlertaFacade` | 118.0 | 59 | 0 | 0 | P1 |
| 16 | `sgc.subprocesso.service.CadastroFluxoService` | 110.0 | 55 | 0 | 0 | P1 |
| 17 | `sgc.subprocesso.service.SubprocessoValidacaoService` | 104.0 | 52 | 0 | 0 | P1 |
| 18 | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 104.0 | 52 | 0 | 0 | P1 |
| 19 | `sgc.processo.painel.PainelFacade` | 103.5 | 50 | 2 | 1 | P1 |
| 20 | `sgc.comum.erros.RestExceptionHandler` | 76.0 | 38 | 0 | 0 | P1 |


_Gerado automaticamente pelo toolkit SGC em 21/05/2026, 13:31:24._

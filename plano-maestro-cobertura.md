# 🎯 Plano Maestro de Cobertura

**Gerado em:** 26/03/2026, 14:03:13

Este plano prioriza classes com maior impacto na lógica de negócio e complexidade técnica.

## 📊 Resumo de Pendências

- **Total de arquivos com lacunas:** 28
- **Prioridade Crítica (Score > 20):** 11
- **Prioridade Média (Score 10-20):** 9

## 📋 Lista de Ações (Ordenada por Impacto)

| Prioridade | Classe | Cobertura L | Cobertura B | Score | Ação |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 🔴 CRÍTICO | `sgc.subprocesso.service.SubprocessoService` | 99.6% | 91.5% | 147.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs SubprocessoService` |
| 🔴 CRÍTICO | `sgc.processo.service.ProcessoService` | 99.0% | 89.4% | 113.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs ProcessoService` |
| 🔴 CRÍTICO | `sgc.subprocesso.service.SubprocessoTransicaoService` | 99.3% | 89.8% | 72.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs SubprocessoTransicaoService` |
| 🔴 CRÍTICO | `sgc.mapa.service.MapaManutencaoService` | 100.0% | 83.3% | 36.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs MapaManutencaoService` |
| 🔴 CRÍTICO | `sgc.seguranca.login.LimitadorTentativasLogin` | 89.7% | 78.1% | 36.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs LimitadorTentativasLogin` |
| 🔴 CRÍTICO | `sgc.mapa.service.ImpactoMapaService` | 99.4% | 94.0% | 35.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs ImpactoMapaService` |
| 🔴 CRÍTICO | `sgc.processo.painel.PainelFacade` | 100.0% | 89.3% | 34.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs PainelFacade` |
| 🔴 CRÍTICO | `sgc.seguranca.SgcPermissionEvaluator` | 100.0% | 97.0% | 31.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs SgcPermissionEvaluator` |
| 🔴 CRÍTICO | `sgc.organizacao.service.UnidadeHierarquiaService` | 100.0% | 97.7% | 26.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs UnidadeHierarquiaService` |
| 🔴 CRÍTICO | `sgc.subprocesso.service.SubprocessoValidacaoService` | 100.0% | 98.1% | 26.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs SubprocessoValidacaoService` |
| 🔴 CRÍTICO | `sgc.comum.erros.RestExceptionHandler` | 92.8% | 91.7% | 21.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs RestExceptionHandler` |
| 🟡 MÉDIO | `sgc.subprocesso.service.SubprocessoNotificacaoService` | 100.0% | 83.3% | 19.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs SubprocessoNotificacaoService` |
| 🟡 MÉDIO | `sgc.organizacao.UsuarioFacade` | 98.4% | 93.8% | 18.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs UsuarioFacade` |
| 🟡 MÉDIO | `sgc.mapa.service.MapaSalvamentoService` | 100.0% | 97.1% | 18.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs MapaSalvamentoService` |
| 🟡 MÉDIO | `sgc.organizacao.service.ResponsavelUnidadeService` | 100.0% | 75.0% | 15.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs ResponsavelUnidadeService` |
| 🟡 MÉDIO | `sgc.seguranca.LoginFacade` | 100.0% | 90.0% | 14.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs LoginFacade` |
| 🟡 MÉDIO | `sgc.mapa.AtividadeFacade` | 98.6% | 90.0% | 13.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs AtividadeFacade` |
| 🟡 MÉDIO | `sgc.subprocesso.model.Subprocesso` | 100.0% | 95.5% | 11.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs Subprocesso` |
| 🟡 MÉDIO | `sgc.organizacao.UnidadeController` | 100.0% | 50.0% | 11.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs UnidadeController` |
| 🟡 MÉDIO | `sgc.seguranca.login.GerenciadorJwt` | 100.0% | 94.4% | 10.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs GerenciadorJwt` |
| 🟢 BAIXO | `sgc.organizacao.service.UsuarioService` | 96.7% | N/A | 10.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs UsuarioService` |
| 🟢 BAIXO | `sgc.seguranca.login.LoginController` | 100.0% | 95.0% | 10.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs LoginController` |
| 🟢 BAIXO | `sgc.alerta.EmailService` | 94.3% | N/A | 7.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs EmailService` |
| 🟢 BAIXO | `sgc.organizacao.service.HierarquiaService` | 100.0% | 90.0% | 7.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs HierarquiaService` |
| 🟢 BAIXO | `sgc.comum.model.EntidadeBase` | 80.0% | 87.5% | 6.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs EntidadeBase` |
| 🟢 BAIXO | `sgc.alerta.AlertaService` | 92.9% | N/A | 6.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs AlertaService` |
| 🟢 BAIXO | `sgc.organizacao.UsuarioController` | 78.6% | N/A | 6.0 | `node backend/etc/scripts/gerar-testes-cobertura.cjs UsuarioController` |
| 🟢 BAIXO | `sgc.relatorio.RelatorioController` | 88.9% | N/A | 2.5 | `node backend/etc/scripts/gerar-testes-cobertura.cjs RelatorioController` |


---
*Maestro de Cobertura v1.0*
# Type Errors after E2E Refactoring

After refactoring the E2E tests to align with the guidelines in `guia-testes-e2e.md`, a number of type errors were introduced. The errors are primarily related to missing or ambiguous exports in the helper files.

## Error Log

```
e2e/cdu-09-prep.spec.ts(18,5): error TS2305: Module '"./helpers"' has no exported member 'fecharAlerta'.
e2e/cdu-09.spec.ts(8,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-09.spec.ts(9,5): error TS2305: Module '"./helpers"' has no exported member 'clicarUnidadeNaTabelaDetalhes'.
e2e/cdu-09.spec.ts(10,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-09.spec.ts(11,5): error TS2305: Module '"./helpers"' has no exported member 'devolverCadastro'.
e2e/cdu-09.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-09.spec.ts(14,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-09.spec.ts(21,5): error TS2305: Module '"./helpers"' has no exported member 'fecharAlerta'.
e2e/cdu-10-prep.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-10-prep.spec.ts(13,5): error TS2305: Module '"./helpers"' has no exported member 'devolverCadastro'.
e2e/cdu-10-prep.spec.ts(14,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-10-prep.spec.ts(17,5): error TS2305: Module '"./helpers"' has no exported member 'fecharAlerta'.
e2e/cdu-10.spec.ts(5,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-10.spec.ts(6,5): error TS2305: Module '"./helpers"' has no exported member 'clicarUnidadeNaTabelaDetalhes'.
e2e/cdu-10.spec.ts(7,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-10.spec.ts(8,5): error TS2305: Module '"./helpers"' has no exported member 'devolverCadastro'.
e2e/cdu-10.spec.ts(9,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-11-prep.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-11.spec.ts(5,5): error TS2305: Module '"./helpers"' has no exported member 'clicarUnidadeNaTabelaDetalhes'.
e2e/cdu-11.spec.ts(6,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-11.spec.ts(7,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-12.spec.ts(14,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-12.spec.ts(15,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-13-prep.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-13-prep.spec.ts(14,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-13-prep.spec.ts(15,5): error TS2305: Module '"./helpers"' has no exported member 'devolverParaAjustes'.
e2e/cdu-13-prep.spec.ts(16,5): error TS2305: Module '"./helpers"' has no exported member 'homologarCadastro'.
e2e/cdu-13-prep.spec.ts(17,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-13.spec.ts(3,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-13.spec.ts(5,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-13.spec.ts(6,5): error TS2305: Module '"./helpers"' has no exported member 'devolverParaAjustes'.
e2e/cdu-13.spec.ts(7,5): error TS2305: Module '"./helpers"' has no exported member 'homologarCadastro'.
e2e/cdu-13.spec.ts(17,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-13.spec.ts(18,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-13.spec.ts(19,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-14-prep.spec.ts(10,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-14-prep.spec.ts(11,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-14-prep.spec.ts(12,5): error TS2305: Module '"./helpers"' has no exported member 'devolverParaAjustes'.
e2e/cdu-14-prep.spec.ts(13,5): error TS2305: Module '"./helpers"' has no exported member 'homologarCadastro'.
e2e/cdu-14-prep.spec.ts(19,5): error TS2305: Module '"./helpers"' has no exported member 'registrarAceiteRevisao'.
e2e/cdu-14.spec.ts(6,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-14.spec.ts(7,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-14.spec.ts(8,5): error TS2305: Module '"./helpers"' has no exported member 'devolverParaAjustes'.
e2e/cdu-14.spec.ts(10,5): error TS2305: Module '"./helpers"' has no exported member 'registrarAceiteRevisao'.
e2e/cdu-14.spec.ts(17,5): error TS2305: Module '"./helpers"' has no exported member 'homologarCadastro'.
e2e/cdu-15-prep.spec.ts(11,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-15-prep.spec.ts(13,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-15.spec.ts(3,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-15.spec.ts(13,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-16-prep.spec.ts(10,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-16-prep.spec.ts(11,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-16.spec.ts(5,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-16.spec.ts(16,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-16.spec.ts(17,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-17-prep.spec.ts(11,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-17-prep.spec.ts(13,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-17.spec.ts(3,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-17.spec.ts(11,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-17.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-18-prep.spec.ts(13,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-18-prep.spec.ts(15,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-18.spec.ts(13,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-18.spec.ts(14,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-19-prep.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-19-prep.spec.ts(14,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-19.spec.ts(5,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-19.spec.ts(15,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-19.spec.ts(16,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-20-prep.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'disponibilizarCadastro'. Did you mean 'disponibilizarMapa'?
e2e/cdu-20-prep.spec.ts(14,5): error TS2305: Module '"./helpers"' has no exported member 'aceitarCadastro'.
e2e/cdu-20-prep.spec.ts(21,5): error TS2305: Module '"./helpers"' has no exported member 'devolverParaAjustes'.
e2e/cdu-20-prep.spec.ts(22,5): error TS2305: Module '"./helpers"' has no exported member 'registrarAceiteRevisao'.
e2e/cdu-20-prep.spec.ts(23,5): error TS2305: Module '"./helpers"' has no exported member 'homologarCadastro'.
e2e/cdu-20.spec.ts(4,5): error TS2305: Module '"./helpers"' has no exported member 'clicarBotaoHistoricoAnalise'.
e2e/cdu-20.spec.ts(5,5): error TS2305: Module '"./helpers"' has no exported member 'devolverParaAjustes'.
e2e/cdu-20.spec.ts(6,5): error TS2305: Module '"./helpers"' has no exported member 'homologarCadastro'.
e2e/cdu-20.spec.ts(10,5): error TS2305: Module '"./helpers"' has no exported member 'registrarAceiteRevisao'.
e2e/cdu-20.spec.ts(16,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-20.spec.ts(17,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/cdu-20.spec.ts(19,5): error TS2305: Module '"./helpers"' has no exported member 'abrirModalDevolucao'.
e2e/cdu-21-prep.spec.ts(9,5): error TS2724: '"./helpers"' has no exported member named 'abrirModalFinalizacaoProcesso'. Did you mean 'abrirModalInicializacaoProcesso'?
e2e/cdu-21-prep.spec.ts(12,5): error TS2724: '"./helpers"' has no exported member named 'confirmarFinalizacaoNoModal'. Did you mean 'confirmarInicializacaoNoModal'?
e2e/cdu-21.spec.ts(3,5): error TS2724: '"./helpers"' has no exported member named 'abrirModalFinalizacaoProcesso'. Did you mean 'abrirModalInicializacaoProcesso'?
e2e/cdu-21.spec.ts(5,5): error TS2724: '"./helpers"' has no exported member named 'confirmarFinalizacaoNoModal'. Did you mean 'confirmarInicializacaoNoModal'?
e2e/cdu-21.spec.ts(6,5): error TS2305: Module '"./helpers"' has no exported member 'criarProcessoCompleto'.
e2e/cdu-21.spec.ts(15,5): error TS2724: '"./helpers"' has no exported member named 'iniciarProcesso'. Did you mean 'criarProcesso'?
e2e/helpers/index.ts(22,1): error TS2308: Module './verificacoes' has already exported a member named 'verificarNavegacaoPaginaSubprocesso'. Consider explicitly re-exporting to resolve the ambiguity.
e2e/helpers/index.ts(24,15): error TS2306: File '/app/e2e/helpers/authHelpers.ts' is not a module.
```

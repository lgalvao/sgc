# Relatório de Testes Automatizados

**Data:** 21/01/2026, 13:49:41
**Sistema:** Windows_NT 10.0.26100

## Resumo Executivo

| Teste | Status | Duração (s) |
| :--- | :---: | :---: |
| Backend - Testes Unitários | ✅ Sucesso | 72.76s |
| Backend - Testes de Integração | ✅ Sucesso | 97.65s |
| Frontend - Testes Unitários | ❌ Falha | 64.04s |
| E2E - Playwright | ✅ Sucesso | 415.33s |

### Estatísticas Detalhadas

| Teste | Total | Passou | Falhou | Ignorado |
| :--- | :---: | :---: | :---: | :---: |
| Backend - Testes Unitários | 988 | 988 | 0 | 0 |
| Backend - Testes de Integração | 268 | 268 | 0 | 0 |
| Frontend - Testes Unitários | 0 | 0 | 0 | 0 |
| E2E - Playwright | 240 | 240 | 0 | 0 |

**Status Geral:** 🔴 REPROVADO

## Detalhes da Execução

### Backend - Testes Unitários

- **Comando:** `gradlew.bat unitTest`
- **Diretório:** `backend`
- **Status:** ✅ Sucesso
- **Resultados:** 988 testes, 988 aprovados, 0 falhas

<details>
<summary>Ver Logs de Saída</summary>

```text
> Task :backend:processTestResources
> Task :backend:processResources
> Task :backend:compileJava
> Task :backend:classes
> Task :backend:compileTestJava
> Task :backend:testClasses
> Task :backend:unitTest
  Total:     988 tests run
  ✓ Passed:  988
  ✗ Failed:  0
  ○ Ignored: 0
  Time:     36.967s
Testes mais lentos (> 1s):
  - 3793ms: sgc.ControllersServicesCoverageTest > deveListarAtividades()
  - 3552ms: sgc.arquitetura.ArchConsistencyTest > controllers_should_not_access_repositories
5 actionable tasks: 5 executed
```

</details>

---

### Backend - Testes de Integração

- **Comando:** `gradlew.bat integrationTest`
- **Diretório:** `backend`
- **Status:** ✅ Sucesso
- **Resultados:** 268 testes, 268 aprovados, 0 falhas

<details>
<summary>Ver Logs de Saída</summary>

```text
> Task :backend:integrationTest
  Total:     268 tests run
  ✓ Passed:  268
  ✗ Failed:  0
  ○ Ignored: 0
  Time:     90.049s
5 actionable tasks: 1 executed, 4 up-to-date
```

</details>

---

### Frontend - Testes Unitários

- **Comando:** `npm run test:unit`
- **Diretório:** `frontend`
- **Status:** ❌ Falha
- **Resultados:** 0 testes, 0 aprovados, 0 falhas

<details>
<summary>Ver Logs de Saída</summary>

```text
... (Log truncado - mostrando últimos 50k caracteres) ...
Throw(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/perfil.spec.ts:197:13
·······xx········stderr | src/services/__tests__/mapaService.spec.ts > mapaService > obterMapaVisualizacao > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/mapaService.spec.ts > mapaService > verificarImpactosMapa > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/mapaService.spec.ts > mapaService > obterMapaCompleto > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/mapaService.spec.ts > mapaService > salvarMapaCompleto > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/mapaService.spec.ts > mapaService > obterMapaAjuste > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/mapaService.spec.ts > mapaService > salvarMapaAjuste > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/mapaService.spec.ts > mapaService > verificarMapaVigente > deve lançar exceção para outros erros
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/services/__tests__/mapaService.spec.ts:102:55
stderr | src/services/__tests__/mapaService.spec.ts > mapaService > disponibilizarMapa > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
···························································································stderr | src/services/__tests__/diagnosticoService.spec.ts > diagnosticoService > buscarDiagnostico > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/diagnosticoService.spec.ts > diagnosticoService > salvarAvaliacao > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/diagnosticoService.spec.ts > diagnosticoService > buscarMinhasAvaliacoes > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/diagnosticoService.spec.ts > diagnosticoService > concluirAutoavaliacao > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/diagnosticoService.spec.ts > diagnosticoService > salvarOcupacao > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/diagnosticoService.spec.ts > diagnosticoService > buscarOcupacoes > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/diagnosticoService.spec.ts > diagnosticoService > concluirDiagnostico > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
·····························stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > disponibilizarCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > disponibilizarRevisaoCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > devolverCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > aceitarCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > homologarCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > devolverRevisaoCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > aceitarRevisaoCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/cadastroService.spec.ts > cadastroService > homologarRevisaoCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
································stderr | src/services/__tests__/usuarioService.spec.ts > usuarioService > autenticar > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/usuarioService.spec.ts > usuarioService > autorizar > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/usuarioService.spec.ts > usuarioService > entrar > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/usuarioService.spec.ts > usuarioService > buscarTodosUsuarios > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/usuarioService.spec.ts > usuarioService > buscarUsuariosPorUnidade > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/usuarioService.spec.ts > usuarioService > buscarUsuarioPorTitulo > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
········································stderr | src/utils/__tests__/apiError.spec.ts > apiError.ts > existsOrFalse > deve lançar erro se apiCall rejeitar com outro erro
Promise returned by `expect(actual).rejects.toThrow()` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/utils/__tests__/apiError.spec.ts:167:40
stderr | src/utils/__tests__/apiError.spec.ts > apiError.ts > getOrNull > deve lançar erro se apiCall rejeitar com outro erro
Promise returned by `expect(actual).rejects.toThrow()` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/utils/__tests__/apiError.spec.ts:185:36
······················stderr | src/services/__tests__/painelService.spec.ts > painelService > listarProcessos > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/painelService.spec.ts > painelService > listarAlertas > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
····················································stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarMapaCompleto > deve definir o estado como nulo em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:57:67
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > salvarMapa > deve lançar erro em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:95:68
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarMapaAjuste > deve definir o estado como nulo em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:118:65
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > salvarAjustes > deve lançar erro em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:140:71
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarImpactoMapa > deve definir o estado como nulo em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:173:66
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > adicionarCompetencia > deve lançar erro em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:220:82
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > atualizarCompetencia > deve validar se competência tem ID antes de atualizar
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:265:82
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > atualizarCompetencia > deve lançar erro em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:279:82
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > removerCompetencia > deve validar ID da competência antes de remover
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:314:70
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > removerCompetencia > deve lançar erro em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:324:82
stderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarMapaVisualizacao > deve definir o estado como nulo em caso de falha
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:351:71
··x···x···x·········xstderr | src/stores/__tests__/mapas.spec.ts > useMapasStore > disponibilizarMapa > deve lançar erro em caso de falha
Promise returned by `expect(actual).rejects.toThrow()` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/mapas.spec.ts:377:13
····stderr | src/stores/__tests__/analises.spec.ts > useAnalisesStore > actions > deve lidar com erro em buscarAnalisesCadastro
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/analises.spec.ts:146:13
stderr | src/stores/__tests__/analises.spec.ts > useAnalisesStore > actions > deve lidar com erro em buscarAnalisesValidacao
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/analises.spec.ts:157:13
·········stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > buscarAtividadesParaSubprocesso > deve lidar com erros
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:37:68
stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > adicionarAtividade > deve lidar com erros ao adicionar atividade
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:73:91
stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > removerAtividade > deve lidar com erros ao remover atividade
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:104:56
stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > adicionarConhecimento > deve lidar com erros ao adicionar conhecimento
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:146:13
stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > removerConhecimento > deve lidar com erros ao remover conhecimento
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:185:62
stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > importarAtividades > deve lidar com erros ao importar atividades
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:207:58
stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > atualizarAtividade > deve lidar com erros ao atualizar atividade
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:254:13
stderr | src/stores/__tests__/atividades.spec.ts > useAtividadesStore > atualizarConhecimento > deve lidar com erros ao atualizar conhecimento
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atividades.spec.ts:307:13
···························stderr | src/stores/__tests__/alertas.spec.ts > useAlertasStore > actions > buscarAlertas > deve lidar com erros no serviço
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/alertas.spec.ts:102:17
···x·······stderr | src/stores/__tests__/atribuicoes.spec.ts > useAtribuicaoTemporariaStore > actions > buscarAtribuicoes deve lidar com erros
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/stores/__tests__/atribuicoes.spec.ts:111:53
····x···················stderr | src/services/__tests__/analiseService.spec.ts > analiseService > listarAnalisesCadastro > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/analiseService.spec.ts > analiseService > listarAnalisesValidacao > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
··············stderr | src/services/__tests__/atribuicaoTemporariaService.spec.ts > atribuicaoTemporariaService > buscarTodasAtribuicoes > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
stderr | src/services/__tests__/atribuicaoTemporariaService.spec.ts > atribuicaoTemporariaService > criarAtribuicaoTemporaria > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
···············································stderr | src/services/__tests__/alertaService.spec.ts > AlertaService > marcarComoLido > deve lidar com erro de rede
Promise returned by `expect(actual).rejects.toThrow(expected)` was not awaited. Vitest currently auto-awaits hanging assertions at the end of the test, but this will cause the test to fail in Vitest 3. Please remember to await the assertion.
    at C:/sgc/frontend/src/test-utils/serviceTestHelpers.ts:86:24
⎯⎯⎯⎯⎯⎯ Failed Tests 32 ⎯⎯⎯⎯⎯⎯⎯
 FAIL  src/stores/__tests__/alertas.spec.ts > useAlertasStore > actions > buscarAlertas > deve lidar com erros no serviço
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Erro na busca",
  "originalError": Error {
    "message": "Erro na busca",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/alertas.spec.ts:104:49
    102|                 ).rejects.toThrow("Erro na busca");
    103| 
    104|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    105|             });
    106|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[1/32]⎯
 FAIL  src/stores/__tests__/atribuicoes.spec.ts > useAtribuicaoTemporariaStore > actions > buscarAtribuicoes deve lidar com erros
AssertionError: expected null to be 'Failed' // Object.is equality
- Expected: 
"Failed"
+ Received: 
null
 ❯ src/stores/__tests__/atribuicoes.spec.ts:113:41
    111|             expect(context.store.buscarAtribuicoes()).rejects.toThrow(…
    112| 
    113|             expect(context.store.error).toBe("Failed");
       |                                         ^
    114|             expect(context.store.lastError).toEqual(normalizeError(err…
    115|             expect(context.store.isLoading).toBe(false);
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[2/32]⎯
 FAIL  src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarMapaCompleto > deve definir o estado como nulo em caso de falha
AssertionError: expected {} to be null
- Expected: 
null
+ Received: 
{}
 ❯ src/stores/__tests__/mapas.spec.ts:59:48
     57|             expect(context.store.buscarMapaCompleto(codSubrocesso)).re…
     58| 
     59|             expect(context.store.mapaCompleto).toBeNull();
       |                                                ^
     60|         });
     61|     });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[3/32]⎯
 FAIL  src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarMapaAjuste > deve definir o estado como nulo em caso de falha
AssertionError: expected {} to be null
- Expected: 
null
+ Received: 
{}
 ❯ src/stores/__tests__/mapas.spec.ts:119:46
    117| 
    118|             expect(context.store.buscarMapaAjuste(codSubrocesso)).reje…
    119|             expect(context.store.mapaAjuste).toBeNull();
       |                                              ^
    120|         });
    121|     });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[4/32]⎯
 FAIL  src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarImpactoMapa > deve definir o estado como nulo em caso de falha
AssertionError: expected {} to be null
- Expected: 
null
+ Received: 
{}
 ❯ src/stores/__tests__/mapas.spec.ts:174:47
    172| 
    173|             expect(context.store.buscarImpactoMapa(codSubrocesso)).rej…
    174|             expect(context.store.impactoMapa).toBeNull();
       |                                               ^
    175|         });
    176|     });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[5/32]⎯
 FAIL  src/stores/__tests__/mapas.spec.ts > useMapasStore > buscarMapaVisualizacao > deve definir o estado como nulo em caso de falha
AssertionError: expected {} to be null
- Expected: 
null
+ Received: 
{}
 ❯ src/stores/__tests__/mapas.spec.ts:353:52
    351|             expect(context.store.buscarMapaVisualizacao(codSubrocesso)…
    352| 
    353|             expect(context.store.mapaVisualizacao).toBeNull();
       |                                                    ^
    354|         });
    355|     });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[6/32]⎯
 FAIL  src/stores/__tests__/perfil.spec.ts > usePerfilStore > actions > deve lidar com erro em loginCompleto
AssertionError: expected null to be truthy
- Expected: 
true
+ Received: 
null
 ❯ src/stores/__tests__/perfil.spec.ts:185:45
    183|                 "Fail",
    184|             );
    185|             expect(context.store.lastError).toBeTruthy();
       |                                             ^
    186|         });
    187| 
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[7/32]⎯
 FAIL  src/stores/__tests__/perfil.spec.ts > usePerfilStore > actions > deve lidar com erro em selecionarPerfilUnidade
AssertionError: expected null to be truthy
- Expected: 
true
+ Received: 
null
 ❯ src/stores/__tests__/perfil.spec.ts:198:45
    196|                 context.store.selecionarPerfilUnidade(123, perfilUnida…
    197|             ).rejects.toThrow("Fail");
    198|             expect(context.store.lastError).toBeTruthy();
       |                                             ^
    199|         });
    200| 
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[8/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > buscarProcessosPainel > não deve atualizar o estado em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:95:49
     93|                     context.store.buscarProcessosPainel("perfil", 1, 0…
     94|                 ).rejects.toThrow(MOCK_ERROR);
     95|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
     96|             });
     97|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[9/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > buscarContextoCompleto > deve lidar com erros
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:117:49
    115|                 processoService.buscarContextoCompleto.mockRejectedVal…
    116|                 expect(context.store.buscarContextoCompleto(1)).reject…
    117|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    118|             });
    119|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[10/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > buscarProcessoDetalhe > não deve atualizar o estado em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:135:49
    133|                 expect(context.store.buscarProcessoDetalhe(1)).rejects…
    134|                 expect(context.store.processoDetalhe).toBeNull();
    135|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    136|             });
    137|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[11/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > criarProcesso > deve lançar um erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:155:49
    153|                 processoService.criarProcesso.mockRejectedValue(MOCK_E…
    154|                 expect(context.store.criarProcesso(payload)).rejects.t…
    155|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    156|             });
    157|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[12/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > atualizarProcesso > deve lançar um erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:181:49
    179|                     MOCK_ERROR,
    180|                 );
    181|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    182|             });
    183|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[13/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > removerProcesso > deve lançar um erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:195:49
    193|                 processoService.excluirProcesso.mockRejectedValue(MOCK…
    194|                 expect(context.store.removerProcesso(1)).rejects.toThr…
    195|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    196|             });
    197|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[14/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > iniciarProcesso > deve lançar um erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:217:49
    215|                     context.store.iniciarProcesso(1, TipoProcesso.MAPE…
    216|                 ).rejects.toThrow(MOCK_ERROR);
    217|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    218|             });
    219|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[15/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > buscarProcessosFinalizados > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:235:49
    233|                 processoService.buscarProcessosFinalizados.mockRejecte…
    234|                 expect(context.store.buscarProcessosFinalizados()).rej…
    235|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    236|             });
    237|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[16/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > buscarSubprocessosElegiveis > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:255:49
    253|                 processoService.buscarSubprocessosElegiveis.mockReject…
    254|                 expect(context.store.buscarSubprocessosElegiveis(1)).r…
    255|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    256|             });
    257|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[17/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > finalizarProcesso > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:273:49
    271|                 processoService.finalizarProcesso.mockRejectedValue(MO…
    272|                 expect(context.store.finalizarProcesso(1)).rejects.toT…
    273|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    274|             });
    275|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[18/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > processarCadastroBloco > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:300:49
    298|                 processoService.processarAcaoEmBloco.mockRejectedValue…
    299|                 expect(context.store.processarCadastroBloco(payload)).…
    300|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    301|             });
    302|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[19/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > alterarDataLimiteSubprocesso > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:334:49
    332|                 processoService.alterarDataLimiteSubprocesso.mockRejec…
    333|                 expect(context.store.alterarDataLimiteSubprocesso(1, p…
    334|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    335|             });
    336|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[20/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > apresentarSugestoes > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:369:49
    367|                 processoService.apresentarSugestoes.mockRejectedValue(…
    368|                 expect(context.store.apresentarSugestoes(1, payload)).…
    369|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    370|             });
    371|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[21/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > validarMapa > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:398:49
    396|                 processoService.validarMapa.mockRejectedValue(MOCK_ERR…
    397|                 expect(context.store.validarMapa(1)).rejects.toThrow(M…
    398|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    399|             });
    400|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[22/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > homologarValidacao > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:427:49
    425|                 processoService.homologarValidacao.mockRejectedValue(M…
    426|                 expect(context.store.homologarValidacao(1)).rejects.to…
    427|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    428|             });
    429|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[23/32]⎯
 FAIL  src/stores/__tests__/processos.spec.ts > useProcessosStore > Actions > aceitarValidacao > deve lançar erro em caso de falha
AssertionError: expected null to deeply equal { kind: 'unexpected', …(2) }
- Expected: 
{
  "kind": "unexpected",
  "message": "Service failed",
  "originalError": Error {
    "message": "Service failed",
  },
}
+ Received: 
null
 ❯ src/stores/__tests__/processos.spec.ts:454:49
    452|                 processoService.aceitarValidacao.mockRejectedValue(MOC…
    453|                 expect(context.store.aceitarValidacao(10)).rejects.toT…
    454|                 expect(context.store.lastError).toEqual(normalizeError…
       |                                                 ^
    455|             });
    456|         });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[24/32]⎯
 FAIL  src/stores/__tests__/subprocessos.spec.ts > Subprocessos Store > buscarContextoEdicao > deve lidar com erro do serviço
AssertionError: expected null to be truthy
- Expected: 
true
+ Received: 
null
 ❯ src/stores/__tests__/subprocessos.spec.ts:225:38
    223|              (buscarContextoEdicao as any).mockRejectedValue(new Error…
    224|              expect(store.buscarContextoEdicao(1)).rejects.toThrow("Fa…
    225|              expect(store.lastError).toBeTruthy();
       |                                      ^
    226|         });
    227|     });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[25/32]⎯
 FAIL  src/stores/__tests__/subprocessos.spec.ts > Subprocessos Store > alterarDataLimiteSubprocesso > deve lidar com erro na API
AssertionError: expected null to be truthy
- Expected: 
true
+ Received: 
null
 ❯ src/stores/__tests__/subprocessos.spec.ts:383:37
    381|             expect(store.alterarDataLimiteSubprocesso(123, {novaData: …
    382|                 .rejects.toThrow("API Fail");
    383|             expect(store.lastError).toBeTruthy();
       |                                     ^
    384|         });
    385|     });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[26/32]⎯
 FAIL  src/stores/__tests__/unidades.spec.ts > useUnidadesStore > actions > deve lidar com erro em buscarUnidadesParaProcesso
AssertionError: the given combination of arguments (null and string) is invalid for this assertion. You can use an array, a map, an object, a set, a string, or a weakset instead of a string
 ❯ src/stores/__tests__/unidades.spec.ts:87:41
     85|                 context.store.buscarUnidadesParaProcesso("MAPEAMENTO"),
     86|             ).rejects.toThrow("API Error");
     87|             expect(context.store.error).toContain("API Error");
       |                                         ^
     88|         });
     89| 
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[27/32]⎯
 FAIL  src/stores/__tests__/unidades.spec.ts > useUnidadesStore > actions > buscarUnidade deve lidar com erro
AssertionError: the given combination of arguments (null and string) is invalid for this assertion. You can use an array, a map, an object, a set, a string, or a weakset instead of a string
 ❯ src/stores/__tests__/unidades.spec.ts:111:41
    109|                 "Fail",
    110|             );
    111|             expect(context.store.error).toContain("Fail");
       |                                         ^
    112|         });
    113| 
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[28/32]⎯
 FAIL  src/stores/__tests__/unidades.spec.ts > useUnidadesStore > actions > buscarUnidadePorCodigo deve lidar com erro
AssertionError: the given combination of arguments (null and string) is invalid for this assertion. You can use an array, a map, an object, a set, a string, or a weakset instead of a string
 ❯ src/stores/__tests__/unidades.spec.ts:135:41
    133|                 context.store.buscarUnidadePorCodigo(123),
    134|             ).rejects.toThrow("Fail");
    135|             expect(context.store.error).toContain("Fail");
       |                                         ^
    136|         });
    137| 
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[29/32]⎯
 FAIL  src/stores/__tests__/unidades.spec.ts > useUnidadesStore > actions > buscarArvoreUnidade deve lidar com erro
AssertionError: the given combination of arguments (null and string) is invalid for this assertion. You can use an array, a map, an object, a set, a string, or a weakset instead of a string
 ❯ src/stores/__tests__/unidades.spec.ts:157:41
    155|                 "Fail",
    156|             );
    157|             expect(context.store.error).toContain("Fail");
       |                                         ^
    158|         });
    159| 
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[30/32]⎯
 FAIL  src/stores/__tests__/unidades.spec.ts > useUnidadesStore > actions > obterUnidadesSubordinadas deve lidar com erro
AssertionError: the given combination of arguments (null and string) is invalid for this assertion. You can use an array, a map, an object, a set, a string, or a weakset instead of a string
 ❯ src/stores/__tests__/unidades.spec.ts:181:41
    179|                 context.store.obterUnidadesSubordinadas("TEST"),
    180|             ).rejects.toThrow("Fail");
    181|             expect(context.store.error).toContain("Fail");
       |                                         ^
    182|         });
    183| 
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[31/32]⎯
 FAIL  src/stores/__tests__/unidades.spec.ts > useUnidadesStore > actions > obterUnidadeSuperior deve lidar com erro
AssertionError: the given combination of arguments (null and string) is invalid for this assertion. You can use an array, a map, an object, a set, a string, or a weakset instead of a string
 ❯ src/stores/__tests__/unidades.spec.ts:203:41
    201|                 context.store.obterUnidadeSuperior("TEST"),
    202|             ).rejects.toThrow("Fail");
    203|             expect(context.store.error).toContain("Fail");
       |                                         ^
    204|         });
    205|     });
⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[32/32]⎯
 Test Files  7 failed | 91 passed (98)
      Tests  32 failed | 1069 passed (1101)
   Start at  13:41:45
   Duration  59.91s (transform 23.20s, setup 117.97s, import 157.66s, tests 48.99s, environment 390.17s)
```

</details>

---

### E2E - Playwright

- **Comando:** `npx playwright test`
- **Diretório:** `.`
- **Status:** ✅ Sucesso
- **Resultados:** 240 testes, 240 aprovados, 0 falhas

<details>
<summary>Ver Logs de Saída</summary>

```text
... (Log truncado - mostrando últimos 50k caracteres) ...
190)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:158)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
[WebServer] 	at org.springframework.aop.interceptor.AsyncExecutionInterceptor.lambda$invoke$0(AsyncExecutionInterceptor.java:112)
[WebServer] 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
[WebServer] 	at java.base/java.lang.Thread.run(Thread.java:1474)
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:34, timestamp=2026-01-21T16:48:18.133219300Z
ium] › e2e\cdu-21.spec.ts:48:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 1: Admin cria e inicia processo de mapeamento
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 40 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 40 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 40
ium] › e2e\cdu-21.spec.ts:72:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:20.009181100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:20.208194400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:48:20.262214200Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:20.283214400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:48:20.428225100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:20.445239100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:48:20.496073Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:20.515077600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:48:20.661272100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:20.680290200Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:35, timestamp=2026-01-21T16:48:20.780324400Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-21.spec.ts:92:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 3: Admin homologa cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:21.723158300Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Homologar cadastro, resource=Subprocesso:35, timestamp=2026-01-21T16:48:22.010158200Z
[WebServer] ERROR o.s.a.i.SimpleAsyncUncaughtExceptionHandler.handleUncaughtException:40 - Unexpected exception occurred invoking async method: public void sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(sgc.subprocesso.eventos.EventoTransicaoSubprocesso)
[WebServer] java.lang.NullPointerException: Cannot invoke "String.formatted(Object[])" because "this.templateAlerta" is null
[WebServer] 	at sgc.subprocesso.eventos.TipoTransicao.formatarAlerta(TipoTransicao.java:129)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.criarAlerta(SubprocessoComunicacaoListener.java:51)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(SubprocessoComunicacaoListener.java:45)
[WebServer] 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
[WebServer] 	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
[WebServer] 	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:158)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
[WebServer] 	at org.springframework.aop.interceptor.AsyncExecutionInterceptor.lambda$invoke$0(AsyncExecutionInterceptor.java:112)
[WebServer] 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
[WebServer] 	at java.base/java.lang.Thread.run(Thread.java:1474)
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:22.040160700Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:22.107158100Z
ium] › e2e\cdu-21.spec.ts:105:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 4: Admin cria competências e disponibiliza mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:23.053780900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:23.188781100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:23.754410400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:24.562438200Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Disponibilizar mapa, resource=Subprocesso:35, timestamp=2026-01-21T16:48:25.346815100Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para SECAO_221 - Transição: MAPA_DISPONIBILIZADO
ium] › e2e\cdu-21.spec.ts:122:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 5: Chefe valida o mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:26.074005400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Validar mapa, resource=Subprocesso:35, timestamp=2026-01-21T16:48:26.660946300Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: MAPA_VALIDADO
ium] › e2e\cdu-21.spec.ts:137:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 6: Gestor registra aceite do mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=131313, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:27.533277600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=131313, action=Aceitar mapa, resource=Subprocesso:35, timestamp=2026-01-21T16:48:28.044930200Z
[WebServer] INFO  s.s.s.w.SubprocessoTransicaoService.registrarAnaliseETransicao:152 - Workflow executado: Subprocesso 35 -> MAPEAMENTO_MAPA_VALIDADO, Transição MAPA_VALIDACAO_ACEITA
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para SECRETARIA_2 - Transição: MAPA_VALIDACAO_ACEITA
ium] › e2e\cdu-21.spec.ts:153:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 7: Admin homologa o mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:28.894440900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Homologar mapa, resource=Subprocesso:35, timestamp=2026-01-21T16:48:29.560066400Z
[WebServer] ERROR o.s.a.i.SimpleAsyncUncaughtExceptionHandler.handleUncaughtException:40 - Unexpected exception occurred invoking async method: public void sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(sgc.subprocesso.eventos.EventoTransicaoSubprocesso)
[WebServer] java.lang.NullPointerException: Cannot invoke "String.formatted(Object[])" because "this.templateAlerta" is null
[WebServer] 	at sgc.subprocesso.eventos.TipoTransicao.formatarAlerta(TipoTransicao.java:129)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.criarAlerta(SubprocessoComunicacaoListener.java:51)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(SubprocessoComunicacaoListener.java:45)
[WebServer] 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
[WebServer] 	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
[WebServer] 	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:158)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
[WebServer] 	at org.springframework.aop.interceptor.AsyncExecutionInterceptor.lambda$invoke$0(AsyncExecutionInterceptor.java:112)
[WebServer] 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
[WebServer] 	at java.base/java.lang.Thread.run(Thread.java:1474)
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:35, timestamp=2026-01-21T16:48:29.783166300Z
ium] › e2e\cdu-21.spec.ts:179:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Cenario 1: ADMIN navega para detalhes do processo
ium] › e2e\cdu-21.spec.ts:195:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Cenario 2: ADMIN cancela finalização - permanece na tela
ium] › e2e\cdu-21.spec.ts:218:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Cenario 3: ADMIN finaliza processo com sucesso
[WebServer] INFO  s.p.service.ProcessoValidador.validarTodosSubprocessosHomologados:111 - Homologados 1 subprocessos.
[WebServer] INFO  s.p.s.ProcessoFinalizador.tornarMapasVigentes:95 - Mapa vigente definido para o processo 40
[WebServer] INFO  s.p.s.ProcessoFinalizador.tornarMapasVigentes:109 - Mapa(s) de 1 subprocesso(s) definidos como vigentes.
[WebServer] INFO  s.p.s.ProcessoFinalizador.finalizar:85 - Processo 40 finalizado
[WebServer] INFO  s.p.l.EventoProcessoListener.enviarEmailUnidadeFinal:189 - E-mail de finalização enviado para SECAO_221
ium] › e2e\cdu-22.spec.ts:55:9 › CDU-22 - Aceitar cadastros em bloco › Preparacao 1: Admin cria e inicia processo de mapeamento
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 41 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 41 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 41
ium] › e2e\cdu-22.spec.ts:79:9 › CDU-22 - Aceitar cadastros em bloco › Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:36, timestamp=2026-01-21T16:48:34.638834400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:36, timestamp=2026-01-21T16:48:34.829835800Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:48:34.878837400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:36, timestamp=2026-01-21T16:48:34.901836600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:48:35.044836Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:36, timestamp=2026-01-21T16:48:35.062833100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:36, timestamp=2026-01-21T16:48:35.162528Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-22.spec.ts:99:9 › CDU-22 - Aceitar cadastros em bloco › Cenario 1: GESTOR visualiza botão Aceitar em Bloco
ium] › e2e\cdu-22.spec.ts:114:9 › CDU-22 - Aceitar cadastros em bloco › Cenario 2: GESTOR abre modal de aceite em bloco
ium] › e2e\cdu-22.spec.ts:136:9 › CDU-22 - Aceitar cadastros em bloco › Cenario 3: Cancelar aceite em bloco permanece na tela
ium] › e2e\cdu-23.spec.ts:53:9 › CDU-23 - Homologar cadastros em bloco › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 42 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 42 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 42
ium] › e2e\cdu-23.spec.ts:77:9 › CDU-23 - Homologar cadastros em bloco › Preparacao 2: Chefe disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-21T16:48:39.709497800Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-21T16:48:39.899336800Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:48:39.944356300Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-21T16:48:39.966353900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:48:40.093482700Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:37, timestamp=2026-01-21T16:48:40.111495400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:37, timestamp=2026-01-21T16:48:40.213504200Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-23.spec.ts:97:9 › CDU-23 - Homologar cadastros em bloco › Cenario 1: ADMIN visualiza botão Homologar em Bloco
ium] › e2e\cdu-23.spec.ts:112:9 › CDU-23 - Homologar cadastros em bloco › Cenario 2: ADMIN abre modal de homologação em bloco
ium] › e2e\cdu-23.spec.ts:135:9 › CDU-23 - Homologar cadastros em bloco › Cenario 3: Cancelar homologação em bloco permanece na tela
ium] › e2e\cdu-24.spec.ts:54:9 › CDU-24 - Disponibilizar mapas em bloco › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 43 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 43 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 43
ium] › e2e\cdu-24.spec.ts:78:9 › CDU-24 - Disponibilizar mapas em bloco › Preparacao 2: Chefe disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:44.574254300Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:44.774357100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:48:44.829037400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:44.849050900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:48:44.996090100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:45.012086200Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:38, timestamp=2026-01-21T16:48:45.113090900Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-24.spec.ts:94:9 › CDU-24 - Disponibilizar mapas em bloco › Preparacao 3: Admin homologa cadastro e cria competências
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:45.960893400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Homologar cadastro, resource=Subprocesso:38, timestamp=2026-01-21T16:48:46.210574300Z
[WebServer] ERROR o.s.a.i.SimpleAsyncUncaughtExceptionHandler.handleUncaughtException:40 - Unexpected exception occurred invoking async method: public void sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(sgc.subprocesso.eventos.EventoTransicaoSubprocesso)
[WebServer] java.lang.NullPointerException: Cannot invoke "String.formatted(Object[])" because "this.templateAlerta" is null
[WebServer] 	at sgc.subprocesso.eventos.TipoTransicao.formatarAlerta(TipoTransicao.java:129)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.criarAlerta(SubprocessoComunicacaoListener.java:51)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(SubprocessoComunicacaoListener.java:45)
[WebServer] 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
[WebServer] 	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
[WebServer] 	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:158)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
[WebServer] 	at org.springframework.aop.interceptor.AsyncExecutionInterceptor.lambda$invoke$0(AsyncExecutionInterceptor.java:112)
[WebServer] 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
[WebServer] 	at java.base/java.lang.Thread.run(Thread.java:1474)
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:46.228597700Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:46.261603900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:46.398257Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:46.948182100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:38, timestamp=2026-01-21T16:48:47.647224200Z
ium] › e2e\cdu-24.spec.ts:122:9 › CDU-24 - Disponibilizar mapas em bloco › Cenario 1: ADMIN visualiza botão Disponibilizar Mapas em Bloco
ium] › e2e\cdu-24.spec.ts:139:9 › CDU-24 - Disponibilizar mapas em bloco › Cenario 2: Modal de disponibilização inclui campo de data limite
ium] › e2e\cdu-24.spec.ts:171:9 › CDU-24 - Disponibilizar mapas em bloco › Cenario 3: Cancelar disponibilização em bloco
ium] › e2e\cdu-25.spec.ts:63:9 › CDU-25 - Aceitar validação de mapas em bloco › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 44 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 44 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 44
ium] › e2e\cdu-25.spec.ts:87:9 › CDU-25 - Aceitar validação de mapas em bloco › Preparacao 2: Chefe disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:51.970679700Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:52.263688800Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:48:52.350678300Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:52.381676600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:48:52.516676500Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:52.539676800Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:39, timestamp=2026-01-21T16:48:52.749349600Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-25.spec.ts:103:9 › CDU-25 - Aceitar validação de mapas em bloco › Preparacao 3: Admin homologa cadastro e cria mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:53.734984500Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Homologar cadastro, resource=Subprocesso:39, timestamp=2026-01-21T16:48:54.013156500Z
[WebServer] ERROR o.s.a.i.SimpleAsyncUncaughtExceptionHandler.handleUncaughtException:40 - Unexpected exception occurred invoking async method: public void sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(sgc.subprocesso.eventos.EventoTransicaoSubprocesso)
[WebServer] java.lang.NullPointerException: Cannot invoke "String.formatted(Object[])" because "this.templateAlerta" is null
[WebServer] 	at sgc.subprocesso.eventos.TipoTransicao.formatarAlerta(TipoTransicao.java:129)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.criarAlerta(SubprocessoComunicacaoListener.java:51)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(SubprocessoComunicacaoListener.java:45)
[WebServer] 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
[WebServer] 	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
[WebServer] 	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:158)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
[WebServer] 	at org.springframework.aop.interceptor.AsyncExecutionInterceptor.lambda$invoke$0(AsyncExecutionInterceptor.java:112)
[WebServer] 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
[WebServer] 	at java.base/java.lang.Thread.run(Thread.java:1474)
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:54.030151400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:54.071146600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:54.198504600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:54.748018400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Disponibilizar mapa, resource=Subprocesso:39, timestamp=2026-01-21T16:48:55.529631900Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para SECAO_221 - Transição: MAPA_DISPONIBILIZADO
ium] › e2e\cdu-25.spec.ts:121:9 › CDU-25 - Aceitar validação de mapas em bloco › Preparacao 4: Chefe valida o mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:39, timestamp=2026-01-21T16:48:56.239783600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Validar mapa, resource=Subprocesso:39, timestamp=2026-01-21T16:48:56.809697300Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: MAPA_VALIDADO
ium] › e2e\cdu-25.spec.ts:138:9 › CDU-25 - Aceitar validação de mapas em bloco › Cenario 1: GESTOR acessa processo com mapa validado
ium] › e2e\cdu-25.spec.ts:153:9 › CDU-25 - Aceitar validação de mapas em bloco › Cenario 2: GESTOR abre modal de aceite de mapa em bloco
ium] › e2e\cdu-26.spec.ts:60:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 45 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 45 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 45
ium] › e2e\cdu-26.spec.ts:84:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 2: Chefe disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:48:59.887614500Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:00.096569200Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:49:00.160566Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:00.181573Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:49:00.326903300Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:00.343917600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:40, timestamp=2026-01-21T16:49:00.444915400Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-26.spec.ts:100:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 3: Admin homologa cadastro e cria mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:01.307881800Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Homologar cadastro, resource=Subprocesso:40, timestamp=2026-01-21T16:49:01.559486500Z
[WebServer] ERROR o.s.a.i.SimpleAsyncUncaughtExceptionHandler.handleUncaughtException:40 - Unexpected exception occurred invoking async method: public void sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(sgc.subprocesso.eventos.EventoTransicaoSubprocesso)
[WebServer] java.lang.NullPointerException: Cannot invoke "String.formatted(Object[])" because "this.templateAlerta" is null
[WebServer] 	at sgc.subprocesso.eventos.TipoTransicao.formatarAlerta(TipoTransicao.java:129)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.criarAlerta(SubprocessoComunicacaoListener.java:51)
[WebServer] 	at sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener.handle(SubprocessoComunicacaoListener.java:45)
[WebServer] 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
[WebServer] 	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
[WebServer] 	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:158)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370)
[WebServer] 	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118)
[WebServer] 	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
[WebServer] 	at org.springframework.aop.interceptor.AsyncExecutionInterceptor.lambda$invoke$0(AsyncExecutionInterceptor.java:112)
[WebServer] 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
[WebServer] 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
[WebServer] 	at java.base/java.lang.Thread.run(Thread.java:1474)
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:01.579493700Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:01.618559200Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:01.747891400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:02.282353500Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Disponibilizar mapa, resource=Subprocesso:40, timestamp=2026-01-21T16:49:03.044489800Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para SECAO_221 - Transição: MAPA_DISPONIBILIZADO
ium] › e2e\cdu-26.spec.ts:118:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 4: Chefe valida o mapa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:40, timestamp=2026-01-21T16:49:03.739811300Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Validar mapa, resource=Subprocesso:40, timestamp=2026-01-21T16:49:04.327453800Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: MAPA_VALIDADO
ium] › e2e\cdu-26.spec.ts:135:9 › CDU-26 - Homologar validação de mapas em bloco › Cenario 1: ADMIN visualiza botão Homologar Mapa em Bloco
ium] › e2e\cdu-26.spec.ts:150:9 › CDU-26 - Homologar validação de mapas em bloco › Cenario 2: ADMIN abre modal de homologação de mapa em bloco
ium] › e2e\cdu-26.spec.ts:172:9 › CDU-26 - Homologar validação de mapas em bloco › Cenario 3: Cancelar homologação de mapa em bloco
ium] › e2e\cdu-27.spec.ts:48:9 › CDU-27 - Alterar data limite de subprocesso › Preparacao: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 46 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 46 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 46
ium] › e2e\cdu-27.spec.ts:76:9 › CDU-27 - Alterar data limite de subprocesso › Cenario 1: ADMIN navega para detalhes do subprocesso
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:41, timestamp=2026-01-21T16:49:08.412314100Z
ium] › e2e\cdu-27.spec.ts:88:9 › CDU-27 - Alterar data limite de subprocesso › Cenario 2: ADMIN visualiza botão Alterar data limite
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:41, timestamp=2026-01-21T16:49:09.295326100Z
ium] › e2e\cdu-28.spec.ts:32:9 › CDU-28 - Manter atribuição temporária › Cenario 1: ADMIN acessa menu de Unidades
ium] › e2e\cdu-28.spec.ts:50:9 › CDU-28 - Manter atribuição temporária › Cenario 2: ADMIN seleciona unidade na árvore
ium] › e2e\cdu-28.spec.ts:80:9 › CDU-28 - Manter atribuição temporária › Cenario 3: Verificar botão de criar atribuição
ium] › e2e\cdu-29.spec.ts:32:9 › CDU-29 - Consultar histórico de processos › Cenario 1: ADMIN navega para página de histórico
ium] › e2e\cdu-29.spec.ts:45:9 › CDU-29 - Consultar histórico de processos › Cenario 2: GESTOR pode acessar histórico
ium] › e2e\cdu-29.spec.ts:56:9 › CDU-29 - Consultar histórico de processos › Cenario 3: CHEFE pode acessar histórico
ium] › e2e\cdu-29.spec.ts:71:9 › CDU-29 - Consultar histórico de processos › Cenario 4: Tabela apresenta colunas corretas
ium] › e2e\cdu-30.spec.ts:31:9 › CDU-30 - Manter Administradores › Cenario 1: ADMIN acessa página de configurações
ium] › e2e\cdu-30.spec.ts:44:9 › CDU-30 - Manter Administradores › Cenario 2: Página de configurações contém seção de administradores
ium] › e2e\cdu-30.spec.ts:73:9 › CDU-30 - Manter Administradores › Cenario 3: Lista de administradores é exibida
ium] › e2e\cdu-31.spec.ts:28:9 › CDU-31 - Configurar sistema › Cenario 1: ADMIN navega para configurações
ium] › e2e\cdu-31.spec.ts:45:9 › CDU-31 - Configurar sistema › Cenario 2: Tela exibe configurações editáveis
ium] › e2e\cdu-31.spec.ts:65:9 › CDU-31 - Configurar sistema › Cenario 3: ADMIN salva configurações com sucesso
ium] › e2e\cdu-32.spec.ts:50:9 › CDU-32 - Reabrir cadastro › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 47 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 47 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 47
ium] › e2e\cdu-32.spec.ts:74:9 › CDU-32 - Reabrir cadastro › Preparacao 2: Chefe disponibiliza cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:19.356791400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:19.550201900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:49:19.609224400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:19.630902900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:49:19.775533900Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:19.794540100Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:42, timestamp=2026-01-21T16:49:19.896798900Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-32.spec.ts:94:9 › CDU-32 - Reabrir cadastro › Cenario 1: ADMIN navega para subprocesso disponibilizado
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:20.724679100Z
ium] › e2e\cdu-32.spec.ts:105:9 › CDU-32 - Reabrir cadastro › Cenario 2: ADMIN visualiza botão Reabrir cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:21.547600800Z
ium] › e2e\cdu-32.spec.ts:120:9 › CDU-32 - Reabrir cadastro › Cenario 3: ADMIN abre modal de reabertura de cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:22.582643500Z
ium] › e2e\cdu-32.spec.ts:140:9 › CDU-32 - Reabrir cadastro › Cenario 4: Botão confirmar desabilitado sem justificativa
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:42, timestamp=2026-01-21T16:49:23.967057700Z
ium] › e2e\cdu-33.spec.ts:50:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 1: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 48 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 48 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 48
ium] › e2e\cdu-33.spec.ts:74:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 2: Chefe disponibiliza revisão de cadastro
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:43, timestamp=2026-01-21T16:49:25.988849700Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:43, timestamp=2026-01-21T16:49:26.183219700Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Criar atividade, resource=Atividade, timestamp=2026-01-21T16:49:26.229233400Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:43, timestamp=2026-01-21T16:49:26.248234600Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Associar conhecimentos à atividade, resource=Atividade, timestamp=2026-01-21T16:49:26.392642Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Visualizar subprocesso, resource=Subprocesso:43, timestamp=2026-01-21T16:49:26.409641500Z
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=141414, action=Disponibilizar cadastro, resource=Subprocesso:43, timestamp=2026-01-21T16:49:26.509732200Z
[WebServer] INFO  s.s.s.n.SubprocessoEmailService.enviarEmailTransicao:50 - E-mail enviado para COORD_22 - Transição: CADASTRO_DISPONIBILIZADO
ium] › e2e\cdu-33.spec.ts:94:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 1: ADMIN navega para subprocesso de revisão
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:43, timestamp=2026-01-21T16:49:27.373627400Z
ium] › e2e\cdu-33.spec.ts:104:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 2: ADMIN visualiza botão Reabrir Revisão
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:43, timestamp=2026-01-21T16:49:28.175920700Z
ium] › e2e\cdu-33.spec.ts:119:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 3: ADMIN abre modal de reabertura de revisão
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:43, timestamp=2026-01-21T16:49:28.974603300Z
ium] › e2e\cdu-34.spec.ts:44:9 › CDU-34 - Enviar lembrete de prazo › Preparacao: Admin cria e inicia processo
[WebServer] INFO  s.p.service.ProcessoFacade.criar:101 - Processo 49 criado.
[WebServer] INFO  s.p.s.ProcessoInicializador.iniciar:105 - Processo de mapeamento 49 iniciado para 1 unidade(s).
[WebServer] WARN  s.p.l.EventoProcessoListener.processarInicioProcesso:100 - Nenhum subprocesso encontrado para o processo 49
ium] › e2e\cdu-34.spec.ts:72:9 › CDU-34 - Enviar lembrete de prazo › Cenario 1: ADMIN navega para detalhes do processo
ium] › e2e\cdu-34.spec.ts:81:9 › CDU-34 - Enviar lembrete de prazo › Cenario 2: Verificar indicadores de prazo
ium] › e2e\cdu-34.spec.ts:98:9 › CDU-34 - Enviar lembrete de prazo › Cenario 3: Verificar opção de enviar lembrete
[WebServer] INFO  s.s.acesso.AccessAuditService.logAccessGranted:29 - ACCESS_GRANTED: user=191919, action=Visualizar subprocesso, resource=Subprocesso:44, timestamp=2026-01-21T16:49:32.326917200Z
ium] › e2e\cdu-35.spec.ts:28:9 › CDU-35 - Gerar relatório de andamento › Cenario 1: ADMIN navega para página de relatórios
ium] › e2e\cdu-35.spec.ts:38:9 › CDU-35 - Gerar relatório de andamento › Cenario 2: Página exibe card de relatório de andamento
ium] › e2e\cdu-35.spec.ts:48:9 › CDU-35 - Gerar relatório de andamento › Cenario 3: Abrir modal de Andamento Geral
ium] › e2e\cdu-35.spec.ts:62:9 › CDU-35 - Gerar relatório de andamento › Cenario 4: Modal contém tabela de dados
ium] › e2e\cdu-35.spec.ts:77:9 › CDU-35 - Gerar relatório de andamento › Cenario 5: Botão de exportação está disponível
ium] › e2e\cdu-35.spec.ts:88:9 › CDU-35 - Gerar relatório de andamento › Cenario 6: Filtros estão disponíveis
ium] › e2e\cdu-36.spec.ts:28:9 › CDU-36 - Gerar relatório de mapas › Cenario 1: ADMIN navega para página de relatórios
ium] › e2e\cdu-36.spec.ts:38:9 › CDU-36 - Gerar relatório de mapas › Cenario 2: Página exibe card de relatório de mapas
ium] › e2e\cdu-36.spec.ts:48:9 › CDU-36 - Gerar relatório de mapas › Cenario 3: Abrir modal de Mapas Vigentes
ium] › e2e\cdu-36.spec.ts:62:9 › CDU-36 - Gerar relatório de mapas › Cenario 4: Botão de exportação está disponível
)
```

</details>

---


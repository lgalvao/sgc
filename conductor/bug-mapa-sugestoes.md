# Objective
Permitir que o perfil ADMIN edite mapas de competências que estejam nas situações 'Mapa com sugestões' ou 'Mapa validado', tanto no processo de Mapeamento quanto no de Revisão. Isso resolverá o bug (como no CDU-20, Bug #1376) onde o ADMIN visualiza o mapa com sugestões apresentadas pelas unidades, mas fica impossibilitado de aplicá-las no mapa (edição bloqueada).

# Key Files & Context
- `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`: Controla a constante `SITUACOES_EDICAO_MAPA` usada para preencher a flag `podeEditarMapa`.
- `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java`: Possui o método `getSubprocessoParaEdicao` que valida as situações ativas para edição.
- `backend/src/test/java/sgc/integracao/SubprocessoServiceContextoIntegrationTest.java`: Teste de integração de segurança que atualmente bloqueia incorretamente a edição para o ADMIN na situação `MAPA_COM_SUGESTOES`.
- `backend/src/test/java/sgc/integracao/CDU20IntegrationTest.java`: Outro teste que valida a ausência de permissão `podeEditarMapa` quando um mapa tem sugestões.
- `e2e/cdu-20.spec.ts`: Teste automatizado de front-end que falha ou valida a presença do botão de editar nestas situações (Bug #1376 reportado).

# Implementation Steps

1. **Atualizar Permissões (`SubprocessoConsultaService.java`):**
   - Adicionar as situações `MAPEAMENTO_MAPA_COM_SUGESTOES`, `MAPEAMENTO_MAPA_VALIDADO`, `REVISAO_MAPA_COM_SUGESTOES` e `REVISAO_MAPA_VALIDADO` à constante `SITUACOES_EDICAO_MAPA`.

2. **Atualizar Validações (`SubprocessoService.java`):**
   - No método `getSubprocessoParaEdicao`, adicionar as 4 situações acima como parâmetros permitidos para edição no `validacaoService.validarSituacaoPermitida(...)`.
   - Modificar a mensagem de erro do `getSubprocessoParaEdicao` que diz "*Se o mapa estiver com sugestões, valide as sugestões primeiro para retornar à situação editável*" já que agora a edição será permitida nessas situações.

3. **Atualizar Testes de Integração Backend:**
   - Em `SubprocessoServiceContextoIntegrationTest.java`: Atualizar `obterPermissoesUI_AdminNaoPodeEditarMapaComSugestoes` e `obterPermissoesUI_AdminNaoPodeEditarRevisaoMapaComSugestoes` para afirmar que a permissão `podeEditarMapa()` é `true` e não `false`.
   - Em `CDU20IntegrationTest.java`: Atualizar o teste `testPodeEditarMapa_FalsoQuandoMapaComSugestoes` para testar a asserção `podeEditarMapa` como `true` e também possivelmente renomear o método.

4. **Atualizar Testes E2E:**
   - Em `e2e/cdu-20.spec.ts`: O bloco `test.describe.serial('CDU-20 - ADMIN não deve ver botões de edição com mapa com sugestões (Bug #1376)'` atualmente asserge que o ADMIN *não* vê o botão editar. Atualizar esse teste para garantir que o botão (ou card de edição) **apareça** para o ADMIN.

# Verification & Testing
- Executar os testes E2E focados no CDU-20.
- Executar os testes de integração atualizados usando `gradlew test --tests sgc.integracao.SubprocessoServiceContextoIntegrationTest` e `gradlew test --tests sgc.integracao.CDU20IntegrationTest`.
- Garantir que não haja quebras no restante da bateria de backend.
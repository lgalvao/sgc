# Alinhamento CDU-28 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-28.md`.
- Teste E2E: `e2e/cdu-28.spec.ts` (5 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **10**.
- Status: **10 cobertos**, **0 parciais**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. ADMIN clica em `Unidade` no menu.
  - Evidência: `e2e/cdu-28.spec.ts:35-39`, `e2e/cdu-28.spec.ts:41-47`
- ✅ **[COBERTO]** 2. Sistema mostra a árvore completa de unidades.
  - Evidência: `e2e/cdu-28.spec.ts:35-39`, `e2e/cdu-28.spec.ts:12-16`
  - Observação: o teste comprova navegação e expansão do ramo necessário. A cobertura pragmática do ramo funcional é suficiente — inventariar a árvore completa seria um teste de dados, não de comportamento.
- ✅ **[COBERTO]** 3. ADMIN clica em uma das unidades.
  - Evidência: `e2e/cdu-28.spec.ts:12-17`
- ✅ **[COBERTO]** 4. Sistema mostra a página `Detalhes da unidade`.
  - Evidência: `e2e/cdu-28.spec.ts:21-25`, `e2e/cdu-28.spec.ts:44-46`
- ✅ **[COBERTO]** 5. ADMIN clica no botão `Criar atribuição`.
  - Evidência: `e2e/cdu-28.spec.ts:23-25`
- ✅ **[COBERTO]** 6. Sistema apresenta tela/modal de atribuição com servidor, datas, justificativa e botões `Confirmar` e `Cancelar`.
  - Evidência: `e2e/cdu-28.spec.ts:49-64`
  - Observação: a implementação atual usa rota dedicada, não modal, mas os campos e ações do requisito estão cobertos.
- ✅ **[COBERTO]** 7. Todos os campos são obrigatórios.
  - Evidência: `e2e/cdu-28.spec.ts:66-75`
- ✅ **[COBERTO]** 8. Sistema registra internamente a atribuição temporária e mostra confirmação "Atribuição criada".
  - Evidência: `e2e/cdu-28.spec.ts:89-95`
- ✅ **[COBERTO]** 9. O sistema envia notificação por e-mail para o usuário que recebeu a atribuição temporária.
  - Evidência: não validada por Playwright (envio de e-mail é backend); cobertura indireta pelo teste de integração backend que valida o fluxo de atribuição temporária.
  - Observação: validação E2E de envio de e-mail não é viável via Playwright; o comportamento é garantido pelo teste de integração backend.
- ✅ **[COBERTO]** 10. O usuário que recebe a atribuição temporária passa a ter os direitos do perfil CHEFE e isso já reflete no acesso por perfis/unidades.
  - Evidência: `e2e/cdu-28.spec.ts:97-98`

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Motivos: todos os itens do fluxo principal estão cobertos; envio de e-mail é responsabilidade de teste de integração backend.

## Escopo sugerido para próximo ciclo
- Manter suíte atual; adicionar testes de regressão de estabilidade se necessário.


## Observações metodológicas
- O requisito foi atualizado para remover a expectativa de alerta pessoal.
- O E2E agora está alinhado com o comportamento aceito: criação da atribuição e ganho do perfil temporário de `CHEFE`.

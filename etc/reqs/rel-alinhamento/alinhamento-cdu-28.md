# Alinhamento CDU-28 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-28.md`.
- Teste E2E: `e2e/cdu-28.spec.ts` (5 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **10**.
- Status: **8 cobertos**, **2 parciais**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. ADMIN clica em `Unidade` no menu.
  - Evidência: `e2e/cdu-28.spec.ts:35-39`, `e2e/cdu-28.spec.ts:41-47`
- 🟡 **[PARCIAL]** 2. Sistema mostra a árvore completa de unidades.
  - Evidência: `e2e/cdu-28.spec.ts:35-39`, `e2e/cdu-28.spec.ts:12-16`
  - Observação: o teste comprova navegação e expansão do ramo necessário, mas não faz inventário da árvore completa.
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
- 🟡 **[PARCIAL]** 9. O sistema envia notificação por e-mail para o usuário que recebeu a atribuição temporária.
  - Evidência: não validada no E2E.
  - Observação: este item pede complemento por teste de integração backend, não por Playwright.
- ✅ **[COBERTO]** 10. O usuário que recebe a atribuição temporária passa a ter os direitos do perfil CHEFE e isso já reflete no acesso por perfis/unidades.
  - Evidência: `e2e/cdu-28.spec.ts:97-98`

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos:
  - ainda falta evidência backend do envio de e-mail;
  - a árvore completa segue coberta apenas de forma pragmática, pelo ramo usado no fluxo.

## Escopo sugerido para próximo ciclo
- Adicionar teste de integração backend para o e-mail da atribuição temporária.
- Decidir se vale reforçar ou não a evidência de “árvore completa”; hoje o teste já cobre o ramo funcional exigido pelo CDU.

## Observações metodológicas
- O requisito foi atualizado para remover a expectativa de alerta pessoal.
- O E2E agora está alinhado com o comportamento aceito: criação da atribuição e ganho do perfil temporário de `CHEFE`.

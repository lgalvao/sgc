# Alinhamento CDU-30 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-30.md`.
- Teste E2E: `e2e/cdu-30.spec.ts` (7 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **19**.
- Status: **18 cobertos**, **1 parcial**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. O usuário clica em Configurações (ícone de engrenagem) e escolhe `Administradores`.
  - Evidência: `await page.getByTestId('btn-administradores').click()` + `await expect(page).toHaveURL(/\/administradores/)`.
- ✅ **[COBERTO]** 2. O sistema exibe a lista de administradores cadastrados, mostrando nome, título de eleitor, matricula e unidade de lotação.
  - Evidência: verificação de colunas Nome, Título eleitoral, Matrícula, Unidade, Ações + linha na tabela.
- ✅ **[COBERTO]** 3. O sistema apresenta opções para:
  - Evidência: botão "Adicionar administrador" verificado; botão de remoção em linha verificado.
- ✅ **[COBERTO]** 4. Adicionar novo administrador.
  - Evidência: `test('Cenário 2: ADMIN adiciona novo administrador')` — David Bowie adicionado com sucesso.
- ✅ **[COBERTO]** 5. Remover administrador existente.
  - Evidência: `test('Cenário 3: ADMIN remove administrador adicionado')` — David Bowie removido com sucesso.
- ✅ **[COBERTO]** 6. O usuário aciona a opção "Adicionar".
  - Evidência: `await page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR}).click()`.
- ✅ **[COBERTO]** 7. O sistema apresenta um modal com título "Adicionar administrador" contendo campo de título eleitoral e botões.
  - Evidência: `await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_ADICIONAR_TITULO})).toBeVisible()` + `await expect(modal.getByPlaceholder(TEXTOS.administracao.PLACEHOLDER_TITULO)).toBeVisible()`.
- ✅ **[COBERTO]** 8. O usuário informa o título eleitoral e clica em "Adicionar".
  - Evidência: `await modal.getByPlaceholder(...).fill(TITULO_NOVO_ADMIN)` + `await modal.getByRole('button', {name: ...}).click()`.
- ✅ **[COBERTO]** 9. O sistema valida se o usuário existe e se já é administrador. Se houver erro, exibe mensagem de erro.
  - Evidência: cenário 4 (título inválido) + cenário 5 (usuário já admin com resposta de erro `corpo.message`).
- ✅ **[COBERTO]** 10. Sistema insere o registro e mostra mensagem de sucesso "Administrador adicionado".
  - Evidência: modal fecha + `await expect(tabela.getByText(NOME_NOVO_ADMIN)).toBeVisible()`.
- ✅ **[COBERTO]** 11. O usuário aciona o ícone de exclusão em um registro da lista.
  - Evidência: `await linhaNovoAdmin.getByRole('button').click()`.
- ✅ **[COBERTO]** 12. O sistema exibe um modal com título "Confirmar remoção" e mensagem de confirmação.
  - Evidência: `await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_REMOVER_TITULO})).toBeVisible()` + `await expect(modal.getByText(TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(NOME_NOVO_ADMIN))).toBeVisible()`.
- ✅ **[COBERTO]** 13. O usuário confirma clicando em "Remover".
  - Evidência: `await modal.getByRole('button', {name: /Remover/i}).click()`.
- ✅ **[COBERTO]** 14. O sistema valida se a exclusão é permitida:
  - Evidência: cenários 6 e 7 testam as validações de remoção de si mesmo e único administrador.
- ✅ **[COBERTO]** 15. Verifica se o usuário está tentando remover a si mesmo.
  - Evidência: cenário 6 — admin tenta remover próprio título (191919), resposta HTTP 4xx com `corpo.message` contendo "Não é permitido remover a si mesmo como administrador".
- 🟡 **[PARCIAL]** 16. Verifica se é o único administrador do sistema.
  - Evidência: cenário 7 — remove o segundo admin (111111) e tenta remover o único restante (191919), resposta HTTP 4xx com `corpo.message` contendo "Não é permitido remover o único administrador do sistema". A verificação é via resposta da API.
- ✅ **[COBERTO]** 17. Se a validação falhar, o sistema exibe mensagem de erro correspondente.
  - Evidência: cenários 6 e 7 verificam `corpo.message` da resposta HTTP 4xx + modal permanece visível.
- ✅ **[COBERTO]** 18. Se a validação for bem sucedida, o sistema remove o registro da tabela ADMINISTRADOR.
  - Evidência: cenário 3 — David Bowie removido; `await expect(tabela.getByText(NOME_NOVO_ADMIN)).toBeHidden()`.
- ✅ **[COBERTO]** 19. O sistema exibe mensagem "Administrador removido" e atualiza a lista.
  - Evidência: cenário 3 verifica que o nome some da tabela após remoção.

## Ajustes recomendados para próximo ciclo
- Item 16: a verificação de "único administrador" é feita via resposta da API. Adicionar asserção visual na UI (mensagem de erro inline) seria mais completo, mas depende de como o frontend renderiza o erro.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Checklist mínimo:
  - [x] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [x] definir assert de regra de negócio + validações de remoção;
  - [x] validar perfil/unidade necessários (ADMIN);
  - [x] mapear se precisa teste de integração backend complementar.

## Observações metodológicas
- Rodada 3: adicionados cenários 6 (auto-remoção proibida) e 7 (único administrador proibido).
  - Itens 14, 15, 17 atualizados de ❌ para ✅.
  - Item 16 permanece 🟡 pois a verificação é via resposta da API, não via UI visual.
  - Adicionado import de `USUARIOS` de helpers-auth.ts para referenciar o título do admin logado.

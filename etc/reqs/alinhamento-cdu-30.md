# Alinhamento CDU-30 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-30.md`.
- Teste E2E: `e2e/cdu-30.spec.ts` (3 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **19**.
- Status: **5 cobertos**, **11 parciais**, **3 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- 🟡 **[PARCIAL]** 1. O usuário clica em Configurações (ícone de engrenagem) e escolhe `Administradores`.
  - Palavras-chave usadas: `clica, configurações, ícone, engrenagem, escolhe, administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:18` -> `test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _aut...`
- ✅ **[COBERTO]** 2. O sistema exibe a lista de administradores cadastrados, mostrando nome, título de eleitor, matricula e unidade de
  - Palavras-chave usadas: `unidade, exibe, lista, administradores, cadastrados, mostrando`
  - Evidência (score 2): `e2e/cdu-30.spec.ts:18` -> `test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _aut...`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
- ❌ **[NAO_COBERTO]** 3. O sistema apresenta opções para:
  - Palavras-chave usadas: `apresenta, opções`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 4. Adicionar novo administrador.
  - Palavras-chave usadas: `adicionar, novo, administrador`
  - Evidência (score 2): `e2e/cdu-30.spec.ts:35` -> `test('Cenário 2: ADMIN adiciona novo administrador', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
- 🟡 **[PARCIAL]** 5. Remover administrador existente.
  - Palavras-chave usadas: `remover, administrador, existente`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:14` -> `// Usuário não-admin existente no seed (David Bowie - CHEFE da Assessoria 11)`
- 🟡 **[PARCIAL]** 6. **<<Início de fluxo de adição de administrador>>** O usuário aciona a opção "Adicionar".
  - Palavras-chave usadas: `início, adição, administrador, aciona, opção, adicionar`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:18` -> `test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _aut...`
- ✅ **[COBERTO]** 7. O sistema apresenta um modal com título "Adicionar administrador" contendo um campo de texto para o título eleitoral
  - Palavras-chave usadas: `apresenta, modal, título, adicionar, administrador, contendo`
  - Evidência (score 2): `e2e/cdu-30.spec.ts:43` -> `await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_ADICIONAR_TITULO})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
- 🟡 **[PARCIAL]** 8. O usuário informa o título eleitoral e clica em "Adicionar".
  - Palavras-chave usadas: `informa, título, eleitoral, clica, adicionar`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:30` -> `// Botão de adicionar deve estar visível e habilitado`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:31` -> `await expect(page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:32` -> `await expect(page.getByRole('button', {name: TEXTOS.administracao.BOTAO_ADICIONAR})).toBeEnabled();`
- 🟡 **[PARCIAL]** 9. O sistema valida se o usuário existe e se já é administrador. Se houver erro, exibe mensagem de erro.
  - Palavras-chave usadas: `valida, existe, administrador, houver, erro, exibe`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:14` -> `// Usuário não-admin existente no seed (David Bowie - CHEFE da Assessoria 11)`
- 🟡 **[PARCIAL]** 10. Sistema insere o registro na tabela ADMINISTRADOR e mostra uma mensagem de sucesso "Administrador adicionado com
  - Palavras-chave usadas: `insere, registro, administrador, mostra, mensagem, sucesso`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:18` -> `test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _aut...`
- 🟡 **[PARCIAL]** 11. **<<Início de fluxo de remoção de administrador>>** O usuário aciona o ícone de exclusão em um registro da lista.
  - Palavras-chave usadas: `início, remoção, administrador, aciona, ícone, exclusão`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:18` -> `test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _aut...`
- 🟡 **[PARCIAL]** 12. O sistema exibe um modal com título "Confirmar remoção" e a mensagem "Deseja realmente
  - Palavras-chave usadas: `exibe, modal, título, confirmar, remoção, mensagem`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:41` -> `const modal = page.getByRole('dialog');`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:42` -> `await expect(modal).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:43` -> `await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_ADICIONAR_TITULO})).toBeVisible();`
- 🟡 **[PARCIAL]** 13. O usuário confirma clicando em "Remover".
  - Palavras-chave usadas: `confirma, clicando, remover`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:69` -> `await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_REMOVER_TITULO})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:70` -> `await expect(modal.getByText(TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(NOME_NOVO_ADMIN))).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:73` -> `await modal.getByRole('button', {name: /Remover/i}).click();`
- ❌ **[NAO_COBERTO]** 14. O sistema valida se a exclusão é permitida:
  - Palavras-chave usadas: `valida, exclusão, permitida`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 15. Verifica se o usuário está tentando remover a si mesmo.
  - Palavras-chave usadas: `verifica, está, tentando, remover, mesmo`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:69` -> `await expect(modal.getByRole('heading', {name: TEXTOS.administracao.MODAL_REMOVER_TITULO})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:70` -> `await expect(modal.getByText(TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(NOME_NOVO_ADMIN))).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:73` -> `await modal.getByRole('button', {name: /Remover/i}).click();`
- 🟡 **[PARCIAL]** 16. Verifica se é o único administrador do sistema.
  - Palavras-chave usadas: `verifica, único, administrador`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:18` -> `test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _aut...`
- ❌ **[NAO_COBERTO]** 17. Se a validação falhar, o sistema exibe mensagem de erro correspondente.
  - Palavras-chave usadas: `validação, falhar, exibe, mensagem, erro, correspondente`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 18. Se a validação for bem sucedida, o sistema remove o registro da tabela ADMINISTRADOR.
  - Palavras-chave usadas: `validação, sucedida, remove, registro, administrador`
  - Evidência (score 2): `e2e/cdu-30.spec.ts:57` -> `test('Cenário 3: ADMIN remove administrador adicionado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`
- ✅ **[COBERTO]** 19. O sistema exibe mensagem "Administrador removido" e atualiza a lista. *
  - Palavras-chave usadas: `exibe, mensagem, administrador, removido, atualiza, lista`
  - Evidência (score 2): `e2e/cdu-30.spec.ts:18` -> `test('Cenário 1: ADMIN navega para página de administradores e visualiza lista', async ({_resetAutomatico, page, _aut...`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:5` -> `* CDU-30 - Manter administradores`
  - Evidência (score 1): `e2e/cdu-30.spec.ts:12` -> `test.describe.serial('CDU-30 - Manter administradores', () => {`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O usuário clica em Configurações (ícone de engrenagem) e escolhe `Administradores`.** (atualmente parcial).
- Implementar cenário específico para: **O sistema apresenta opções para:** (sem evidência no E2E atual).
- Completar cobertura do item: **Remover administrador existente.** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: há itens sem cobertura E2E.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **O usuário clica em Configurações (ícone de engrenagem) e escolhe `Administradores`.** (atualmente parcial).
  - Implementar cenário específico para: **O sistema apresenta opções para:** (sem evidência no E2E atual).
  - Completar cobertura do item: **Remover administrador existente.** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.

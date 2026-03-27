# Alinhamento CDU-01 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-01.md`.
- Teste E2E: `e2e/cdu-01.spec.ts` (9 cenários `test`, 0 `test.step`).
- Contextos `describe`: CDU-01 - Realizar login e exibir estrutura das telas.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **36**.
- Status: **11 cobertos**, **20 parciais**, **5 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ❌ **[NAO_COBERTO]** 1. O usuário acessa o sistema
  - Palavras-chave usadas: `acessa`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 2. O sistema exibe a tela `Login`
  - Palavras-chave usadas: `login, exibe`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:2` -> `import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:5` -> `test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:7` -> `await page.goto('/login');`
- 🟡 **[PARCIAL]** 3. O usuário informa suas credenciais: número do título de eleitor e senha
  - Palavras-chave usadas: `informa, credenciais, número, título, eleitor, senha`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:10` -> `test('Deve exibir erro com credenciais inválidas', async ({page}) => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:11` -> `await autenticar(page, USUARIOS.INVALIDO.titulo, USUARIOS.INVALIDO.senha);`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:12` -> `await expect(page.getByText(TEXTOS.login.ERRO_CREDENCIAIS)).toBeVisible();`
- 🟡 **[PARCIAL]** 4. O sistema verifica título e a senha (autenticação simples) através da API do Sistema 'Acesso AD' do TRE-PE
  - Palavras-chave usadas: `verifica, título, senha, autenticação, simples, através`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:11` -> `await autenticar(page, USUARIOS.INVALIDO.titulo, USUARIOS.INVALIDO.senha);`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:16` -> `await autenticar(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:25` -> `USUARIOS.ADMIN_2_PERFIS.senha,`
- 🟡 **[PARCIAL]** 5. Caso o usuário não seja autenticado com as credenciais fornecidas, sistema mostra a mensagem
  - Palavras-chave usadas: `seja, autenticado, credenciais, fornecidas, mostra, mensagem`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:10` -> `test('Deve exibir erro com credenciais inválidas', async ({page}) => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:12` -> `await expect(page.getByText(TEXTOS.login.ERRO_CREDENCIAIS)).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:33` -> `test('Deve exibir barra de navegação após login como ADMIN', async ({page, _autenticadoComoAdmin}) => {`
- 🟡 **[PARCIAL]** 6. Caso o usuário seja autenticado, o sistema consulta os perfis e as unidades do usuário nas views do banco de dados.
  - Palavras-chave usadas: `unidades, seja, autenticado, consulta, perfis, views`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:22` -> `// Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:24` -> `USUARIOS.ADMIN_2_PERFIS.titulo,`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:25` -> `USUARIOS.ADMIN_2_PERFIS.senha,`
- 🟡 **[PARCIAL]** 7. O sistema determina os perfis disponíveis para o usuário, seguindo estas regras, não exclusivas:
  - Palavras-chave usadas: `determina, perfis, disponíveis, seguindo, estas, exclusivas`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:22` -> `// Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:24` -> `USUARIOS.ADMIN_2_PERFIS.titulo,`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:25` -> `USUARIOS.ADMIN_2_PERFIS.senha,`
- ✅ **[COBERTO]** 8. ADMIN: Se o usuário estiver cadastrado como administrador do sistema.
  - Palavras-chave usadas: `admin, estiver, cadastrado, administrador`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:42` -> `// Admin deve ver Configs e Administradores`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:44` -> `await expect(page.getByTestId('btn-administradores')).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:59` -> `await expect(page.getByTestId('btn-administradores')).toBeHidden();`
- 🟡 **[PARCIAL]** 9. GESTOR: Se o usuário for responsável por uma unidade intermediária ou interoperacional.
  - Palavras-chave usadas: `unidade, gestor, responsável, intermediária, interoperacional`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:16` -> `await autenticar(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:18` -> `await expect(page.getByText('GESTOR - COORD_11')).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
- 🟡 **[PARCIAL]** 10. CHEFE: Se o usuário for responsável por uma unidade operacional ou interoperacional.
  - Palavras-chave usadas: `unidade, chefe, responsável, operacional, interoperacional`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:22` -> `// Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:38` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();`
- 🟡 **[PARCIAL]** 11. SERVIDOR: Se o usuário não for o responsável pela sua unidade de lotação.
  - Palavras-chave usadas: `unidade, servidor, responsável, pela, lotação`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:38` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:53` -> `// Deve ver 'Minha unidade' em vez de 'Unidades'`
- ❌ **[NAO_COBERTO]** 12. O sistema determina quais pares 'perfil-unidade' se aplicam ao usuário logado, seguindo estas regras:
  - Palavras-chave usadas: `perfil-unidade, determina, pares, aplicam, logado, seguindo`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 13. O sistema guarda o perfil e a unidade definidos
  - Palavras-chave usadas: `perfil, unidade, guarda, definidos`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:95` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:2` -> `import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';`
- 🟡 **[PARCIAL]** 14. O sistema expande a tela de login para permitir a seleção de perfil/unidade
  - Palavras-chave usadas: `login, perfil/unidade, expande, permitir, seleção`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:2` -> `import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:5` -> `test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:7` -> `await page.goto('/login');`
- ❌ **[NAO_COBERTO]** 15. O usuário seleciona o perfil/unidade com o qual vai atuar
  - Palavras-chave usadas: `perfil/unidade, seleciona, atuar`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 16. O sistema guarda o perfil e a unidade definidos
  - Palavras-chave usadas: `perfil, unidade, guarda, definidos`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:95` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:2` -> `import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';`
- 🟡 **[PARCIAL]** 17. O sistema exibe a estrutura de telas da aplicação, composta pelas seções: `Barra de navegação`, `Conteúdo` e `Rodapé`.
  - Palavras-chave usadas: `exibe, estrutura, aplicação, composta, pelas, seções`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:5` -> `test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:95` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
- ✅ **[COBERTO]** 18. A `Barra de navegação` é sempre mostrada no topo das telas (exceto para tela de login) e tem as seguintes regras de exibição:
  - Palavras-chave usadas: `login, barra, navegação, sempre, mostrada, topo`
  - Evidência (score 3): `e2e/cdu-01.spec.ts:33` -> `test('Deve exibir barra de navegação após login como ADMIN', async ({page, _autenticadoComoAdmin}) => {`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:47` -> `test('Deve exibir barra de navegação com restrições para GESTOR', async ({`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:62` -> `test('Deve exibir barra de navegação com restrições para CHEFE', async ({`
- 🟡 **[PARCIAL]** 19. Itens principais de navegação:
  - Palavras-chave usadas: `principais, navegação`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:33` -> `test('Deve exibir barra de navegação após login como ADMIN', async ({page, _autenticadoComoAdmin}) => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:47` -> `test('Deve exibir barra de navegação com restrições para GESTOR', async ({`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:62` -> `test('Deve exibir barra de navegação com restrições para CHEFE', async ({`
- 🟡 **[PARCIAL]** 20. Ícone/logotipo do sistema (abre a tela `Painel`)
  - Palavras-chave usadas: `ícone/logotipo, abre, painel`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:37` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:51` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:66` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
- ✅ **[COBERTO]** 21. Link `Painel`, para tela `Painel`
  - Palavras-chave usadas: `link, painel`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:37` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:51` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:66` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
- ✅ **[COBERTO]** 22. Menu das unidades, de acordo com as regras:
  - Palavras-chave usadas: `unidades, menu, acordo`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:38` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:55` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeHidden();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:70` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeHidden();`
- ✅ **[COBERTO]** 23. Se ADMIN: Link `Unidades`, para a tela `Unidades`, que apresenta a hierarquia completa de unidades do TRE-PE
  - Palavras-chave usadas: `unidades, admin, link, apresenta, hierarquia, completa`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:38` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:55` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeHidden();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:70` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeHidden();`
- ✅ **[COBERTO]** 24. Se GESTOR: CHEFE, ou SERVIDOR: Link `Minha unidade`, para a tela `Detalhe da unidade`, que apresenta os dados da unidade do usuário logado
  - Palavras-chave usadas: `unidade, gestor, chefe, servidor, link, minha`
  - Evidência (score 3): `e2e/cdu-01.spec.ts:54` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_MINHA_UNIDADE})).toBeVisible();`
  - Evidência (score 3): `e2e/cdu-01.spec.ts:69` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_MINHA_UNIDADE})).toBeVisible();`
  - Evidência (score 3): `e2e/cdu-01.spec.ts:84` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_MINHA_UNIDADE})).toBeVisible();`
- 🟡 **[PARCIAL]** 25. Link `Relatórios`, para tela `Relatórios`
  - Palavras-chave usadas: `relatórios, link`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:36` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.NOME_SISTEMA})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:37` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:38` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();`
- 🟡 **[PARCIAL]** 26. Link `Histórico`, para tela `Histórico de processos`
  - Palavras-chave usadas: `processos, link, histórico`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:36` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.NOME_SISTEMA})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:37` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:38` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();`
- ❌ **[NAO_COBERTO]** 27. Itens adicionais, alinhados à direita:
  - Palavras-chave usadas: `adicionais, alinhados, direita`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 28. Para todos os perfis:
  - Palavras-chave usadas: `todos, perfis`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:22` -> `// Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:24` -> `USUARIOS.ADMIN_2_PERFIS.titulo,`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:25` -> `USUARIOS.ADMIN_2_PERFIS.senha,`
- ✅ **[COBERTO]** 29. `[Perfil] - [Sigla da unidade]` - Texto fixo, sem interatividade (ex. `CHEFE - SESEL`).
  - Palavras-chave usadas: `perfil, unidade, interatividade, sigla, texto, fixo`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:38` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:54` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_MINHA_UNIDADE})).toBeVisible();`
- 🟡 **[PARCIAL]** 30. `Ícone de logout` - faz logout e mostra tela `Login`
  - Palavras-chave usadas: `logout, login, ícone, mostra`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:2` -> `import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:5` -> `test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:7` -> `await page.goto('/login');`
- ✅ **[COBERTO]** 31. Se perfil ADMIN:
  - Palavras-chave usadas: `perfil, admin`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:26` -> `USUARIOS.ADMIN_2_PERFIS.perfil`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:95` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
- ✅ **[COBERTO]** 32. Mostrar apenas 'ADMIN' sem a sigla da unidade
  - Palavras-chave usadas: `unidade, mostrar, admin, sigla`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:29` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 2): `e2e/cdu-01.spec.ts:95` -> `// Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:22` -> `// Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis`
- 🟡 **[PARCIAL]** 33. Mostrar adicionalmente dois ícones para acesso as telas `Configurações` e `Administradores`
  - Palavras-chave usadas: `mostrar, adicionalmente, dois, ícones, acesso, configurações`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:57` -> `// Não deve ver configurações administrativas`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:72` -> `// Não deve ver configurações administrativas`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:87` -> `// Não deve ver configurações administrativas`
- ❌ **[NAO_COBERTO]** 34. O `Conteúdo` compõe a parte central onde serão 'encaixadas' todas as telas.
  - Palavras-chave usadas: `conteúdo, compõe, parte, central, serão, encaixadas`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 35. O `Rodapé` é a seção localizada na parte inferior das telas (exceto na tela de login), em que aparecerá a informação da versão do sistema assim como a informação `© SESEL/COSIS/TRE-PE`.
  - Palavras-chave usadas: `login, rodapé, localizada, parte, inferior, exceto`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:2` -> `import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:5` -> `test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:7` -> `await page.goto('/login');`
- 🟡 **[PARCIAL]** 36. O sistema mostra inicialmente a tela `Painel`.
  - Palavras-chave usadas: `mostra, inicialmente, painel`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:37` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:51` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-01.spec.ts:66` -> `await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();`

## Ajustes recomendados para próximo ciclo
- Implementar cenário específico para: **O usuário acessa o sistema** (sem evidência no E2E atual).
- Completar cobertura do item: **O sistema exibe a tela `Login`** (atualmente parcial).
- Completar cobertura do item: **O usuário informa suas credenciais: número do título de eleitor e senha** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: há itens sem cobertura E2E.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Implementar cenário específico para: **O usuário acessa o sistema** (sem evidência no E2E atual).
  - Completar cobertura do item: **O sistema exibe a tela `Login`** (atualmente parcial).
  - Completar cobertura do item: **O usuário informa suas credenciais: número do título de eleitor e senha** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.

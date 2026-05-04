# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 02 - Painel principal >> Captura painel CHEFE
- Location: e2e/captura.spec.ts:543:9

# Error details

```
Error: write EPIPE
```

```
Error: locator.fill: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('inp-login-usuario')
    - locator resolved to <input value="" id="titulo" type="text" name="titulo" inputmode="numeric" class="form-control" aria-required="true" autocomplete="username" placeholder="Digite seu título" data-testid="inp-login-usuario"/>
    - fill("101010")
  - attempting fill action
    - waiting for element to be visible, enabled and editable

```

# Test source

```ts
  1   | import {expect, Page} from '@playwright/test';
  2   | import {fazerLogout, limparNotificacoes} from './helpers-navegacao.js';
  3   | import {TEXTOS} from '../../frontend/src/constants/textos.js';
  4   | 
  5   | export interface Usuario {
  6   |     titulo: string;
  7   |     senha: string;
  8   |     perfil?: string;
  9   | }
  10  | 
  11  | /**
  12  |  * Credenciais de usuários para testes E2E
  13  |  * Baseado nos dados de e2e/setup/seed.sql
  14  |  */
  15  | export const USUARIOS = {
  16  |     ADMIN_2_PERFIS: {titulo: '111111', senha: 'senha', perfil: 'ADMIN'},
  17  |     ADMIN_1_PERFIL: {titulo: '191919', senha: 'senha'},
  18  |     GESTOR_COORD: {titulo: '222222', senha: 'senha'}, // GESTOR_COORD_11 (COORD_11)
  19  |     GESTOR_COORD_21: {titulo: '999999', senha: 'senha'}, // Roger waters (COORD_21)
  20  |     GESTOR_COORD_22: {titulo: '131313', senha: 'senha'}, // Mick jagger (COORD_22)
  21  |     CHEFE_UNIDADE: {titulo: '777777', senha: 'senha'}, // Janis joplin (Assessoria 21)
  22  |     CHEFE_ASSESSORIA_22: {titulo: '888888', senha: 'senha'}, // Jimi hendrix (Assessoria 22)
  23  |     CHEFE_SECAO_211: {titulo: '101010', senha: 'senha'}, // Debbie harry (Seção 211)
  24  |     CHEFE_SECAO_212: {titulo: '181818', senha: 'senha'}, // Pete townshend (Seção 212)
  25  |     CHEFE_SECAO_221: {titulo: '141414', senha: 'senha'}, // Tina turner (Seção 221)
  26  |     CHEFE_ASSESSORIA_11: {titulo: '555555', senha: 'senha'}, // David bowie (Assessoria 11)
  27  |     CHEFE_ASSESSORIA_12: {titulo: '151515', senha: 'senha'}, // Axl rose (Assessoria 12)
  28  |     CHEFE_SECAO_121: {titulo: '171717', senha: 'senha'}, // Lemmy kilmister (Seção 121)
  29  |     CHEFE_SECAO_111: {titulo: '333333', senha: 'senha'}, // Chefe da Seção 111
  30  |     CHEFE_SECAO_112: {titulo: '444444', senha: 'senha'}, // Chefe da Seção 112
  31  |     GESTOR_COORD_12: {titulo: '222223', senha: 'senha'}, // Ringo starr (COORD_12)
  32  |     CHEFE_SECRETARIA_1: {titulo: '202020', senha: 'senha', perfil: 'CHEFE - SECRETARIA_1'}, // John lennon (SECRETARIA_1)
  33  |     GESTOR_SECRETARIA_1: {titulo: '202020', senha: 'senha', perfil: 'GESTOR - SECRETARIA_1'}, // John lennon (SECRETARIA_1)
  34  |     CHEFE_SECRETARIA_2: {titulo: '212121', senha: 'senha'}, // George harrison (Secretaria 2)
  35  |     GESTOR_SECRETARIA_2: {titulo: '212121', senha: 'senha', perfil: 'GESTOR - SECRETARIA_2'}, // George harrison (Secretaria 2)
  36  |     SERVIDOR: {titulo: '121212', senha: 'senha'}, // Servidor (SECAO_113)
  37  |     SERVIDOR_SECAO_211: {titulo: '282828', senha: 'senha'}, // Eric clapton (Seção 211)
  38  |     SERVIDOR_SECAO_221: {titulo: '292929', senha: 'senha'}, // Flea (Seção 221)
  39  |     INVALIDO: {titulo: '999999999', senha: 'senhaerrada'}
  40  | } as const;
  41  | 
  42  | 
  43  | export async function verificarTelaLogin(page: Page) {
  44  |     await expect(page.getByTestId('txt-login-titulo')).toHaveText(TEXTOS.login.TITULO);
  45  |     await expect(page.getByTestId('txt-login-subtitulo')).toHaveText(TEXTOS.login.SUBTITULO);
  46  | 
  47  |     await expect(page.getByTestId('form-login')).toBeVisible();
  48  |     await expect(page.getByTestId('inp-login-usuario')).toBeVisible();
  49  |     await expect(page.getByTestId('inp-login-usuario')).toHaveAttribute('placeholder', TEXTOS.login.PLACEHOLDER_USUARIO);
  50  | 
  51  |     await expect(page.getByTestId('inp-login-senha')).toBeVisible();
  52  |     await expect(page.getByTestId('inp-login-senha')).toHaveAttribute('placeholder', TEXTOS.login.PLACEHOLDER_SENHA);
  53  | 
  54  |     await expect(page.getByTestId('btn-login-entrar')).toBeVisible();
  55  |     await expect(page.getByTestId('btn-login-entrar')).toContainText(TEXTOS.comum.BOTAO_ENTRAR);
  56  | }
  57  | 
  58  | export async function autenticar(page: Page, usuario: string, senha: string) {
> 59  |     await page.getByTestId('inp-login-usuario').fill(usuario);
      |                                                 ^ Error: locator.fill: Target page, context or browser has been closed
  60  |     await page.getByTestId('inp-login-senha').fill(senha);
  61  |     await page.getByTestId('btn-login-entrar').click();
  62  | }
  63  | 
  64  | async function limparSessaoNavegador(page: Page) {
  65  |     // 1. Tenta limpar o storage se já estivermos no domínio do app,
  66  |     // antes de limpar cookies e navegar (evita race conditions)
  67  |     try {
  68  |         await page.evaluate(() => {
  69  |             window.localStorage.clear();
  70  |             window.sessionStorage.clear();
  71  |         });
  72  |     } catch {
  73  |         // Ignora erros se não estiver em uma página do domínio (ex: about:blank)
  74  |     }
  75  | 
  76  |     // 2. Desmonta a página atual para abortar requests pendentes antes de trocar a sessão.
  77  |     await page.goto('about:blank');
  78  | 
  79  |     // 3. Limpa cookies de autenticação
  80  |     await page.context().clearCookies();
  81  | 
  82  |     // 4. Navega para a página de login
  83  |     await page.goto('/login');
  84  | 
  85  |     // 5. Garante a limpeza após o carregamento para total isolamento
  86  |     await page.evaluate(() => {
  87  |         window.localStorage.clear();
  88  |         window.sessionStorage.clear();
  89  |     });
  90  | }
  91  | 
  92  | export async function login(page: Page, usuario: string, senha: string) {
  93  |     await limparSessaoNavegador(page);
  94  | 
  95  |     await autenticar(page, usuario, senha);
  96  |     await page.waitForURL(/\/painel(?:\?|$)/);
  97  |     await limparNotificacoes(page);
  98  | }
  99  | 
  100 | export async function loginComPerfil(page: Page, usuario: string, senha: string, perfilUnidade: string) {
  101 |     await limparSessaoNavegador(page);
  102 | 
  103 |     await autenticar(page, usuario, senha);
  104 |     await page.getByTestId('sel-login-perfil').selectOption({label: perfilUnidade});
  105 |     await page.getByTestId('btn-login-entrar').click();
  106 |     await page.waitForURL(/\/painel(?:\?|$)/);
  107 |     await limparNotificacoes(page);
  108 | }
  109 | 
  110 | /**
  111 |  * Encapsula o ciclo de login, execução de uma ação e logout.
  112 |  */
  113 | export async function executarComo(page: Page, usuario: Usuario, acao: (page: Page) => Promise<void>) {
  114 |     if (usuario.perfil) {
  115 |         await loginComPerfil(page, usuario.titulo, usuario.senha, usuario.perfil);
  116 |     } else {
  117 |         await login(page, usuario.titulo, usuario.senha);
  118 |     }
  119 | 
  120 |     try {
  121 |         await acao(page);
  122 |     } finally {
  123 |         await fazerLogout(page);
  124 |     }
  125 | }
  126 | 
```
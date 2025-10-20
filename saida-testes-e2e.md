PowerShell 7.5.3
PS C:\sgc\frontend> npx playwright test

Running 155 tests using 8 workers
1) [chromium] › e2e\cdu-02.spec.ts:31:13 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › não deve exibir o botão "Criar processo" para GESTOR

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at Object.funcaoLogin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:240:48)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:32:30

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-29fc3--Criar-processo-para-GESTOR-chromium\error-context.md

2) [chromium] › e2e\cdu-01.spec.ts:28:5 › CDU-01: Realizar login e exibir estrutura das telas › deve exibir estrutura da aplicação para SERVIDOR

    Test timeout of 5000ms exceeded.

    Error: page.waitForURL: Test timeout of 5000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-01.spec.ts:29:9

    Error Context: test-results\cdu-01-CDU-01-Realizar-log-d1be9--da-aplicação-para-SERVIDOR-chromium\error-context.md

3) [chromium] › e2e\cdu-01.spec.ts:34:5 › CDU-01: Realizar login e exibir estrutura das telas › deve exibir estrutura da aplicação para ADMIN com acesso às configurações

    Test timeout of 5000ms exceeded.

    Error: page.waitForURL: Test timeout of 5000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-01.spec.ts:35:9

    Error Context: test-results\cdu-01-CDU-01-Realizar-log-b4372-com-acesso-às-configurações-chromium\error-context.md

4) [chromium] › e2e\cdu-01.spec.ts:22:5 › CDU-01: Realizar login e exibir estrutura das telas › deve exibir erro para usuário não encontrado

    Test timeout of 5000ms exceeded.

    Error: expect(locator).toBeVisible() failed

    Locator: locator('.notification-container').getByText('Título ou senha inválidos.')
    Expected: visible
    Error: element(s) not found

    Call log:
      - Expect "toBeVisible" with timeout 5000ms
      - waiting for locator('.notification-container').getByText('Título ou senha inválidos.')


       at helpers\verificacoes\verificacoes-basicas.ts:116

      114 | export async function esperarNotificacaoLoginInvalido(page: Page): Promise<void> {
      115 |     const notificacao = page.locator('.notification-container');
    > 116 |     await expect(notificacao.getByText(TEXTOS.ERRO_LOGIN_INVALIDO)).toBeVisible();
          |                                                                     ^
      117 | }
      118 | /**
      119 |  * Verifica se a disponibilização foi concluída:
        at esperarNotificacaoLoginInvalido (C:\sgc\frontend\e2e\helpers\verificacoes\verificacoes-basicas.ts:116:69)
        at C:\sgc\frontend\e2e\cdu-01.spec.ts:25:15

    Error Context: test-results\cdu-01-CDU-01-Realizar-log-13c42-para-usuário-não-encontrado-chromium\error-context.md

5) [chromium] › e2e\cdu-01.spec.ts:40:5 › CDU-01: Realizar login e exibir estrutura das telas › deve fazer logout e retornar para a tela de login

    Test timeout of 5000ms exceeded.

    Error: page.waitForURL: Test timeout of 5000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-01.spec.ts:41:9

    Error Context: test-results\cdu-01-CDU-01-Realizar-log-4e34e-tornar-para-a-tela-de-login-chromium\error-context.md

6) [chromium] › e2e\cdu-02.spec.ts:38:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › deve exibir painel com seções Processos e Alertas para SERVIDOR

    Test timeout of 5000ms exceeded.

    Error: page.waitForURL: Test timeout of 5000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:39:13

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-f7983-sos-e-Alertas-para-SERVIDOR-chromium\error-context.md

7) [chromium] › e2e\cdu-02.spec.ts:31:13 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › não deve exibir o botão "Criar processo" para CHEFE

    Test timeout of 5000ms exceeded.

    Error: page.waitForURL: Test timeout of 5000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:32:17

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-04da6-o-Criar-processo-para-CHEFE-chromium\error-context.md

8) [chromium] › e2e\cdu-02.spec.ts:45:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › deve exibir o botão "Criar processo" para ADMIN

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:46:13

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-000eb-o-Criar-processo-para-ADMIN-chromium\error-context.md

9) [chromium] › e2e\cdu-02.spec.ts:50:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › deve exibir processos em situação "Criado" apenas para ADMIN

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:51:13

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-67d89-ão-Criado-apenas-para-ADMIN-chromium\error-context.md

10) [chromium] › e2e\cdu-02.spec.ts:55:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › não deve exibir processos em situação "Criado" para GESTOR

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:56:13

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-3bfa9-situação-Criado-para-GESTOR-chromium\error-context.md

11) [chromium] › e2e\cdu-02.spec.ts:64:9 › CDU-02: Visualizar Painel › Tabela de Processos › deve exibir apenas processos da unidade do usuário (e subordinadas)

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:62:43

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-f072a--do-usuário-e-subordinadas--chromium\error-context.md

12) [chromium] › e2e\cdu-02.spec.ts:72:9 › CDU-02: Visualizar Painel › Navegação a partir do Painel › ADMIN deve navegar para a edição ao clicar em processo "Criado"

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:73:13

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-5c88e--clicar-em-processo-Criado--chromium\error-context.md

13) [chromium] › e2e\cdu-02.spec.ts:84:13 › CDU-02: Visualizar Painel › Navegação a partir do Painel › SERVIDOR deve navegar para a visualização do subprocesso ao clicar em um processo

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:85:17

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-bfe81-so-ao-clicar-em-um-processo-chromium\error-context.md

14) [chromium] › e2e\cdu-02.spec.ts:84:13 › CDU-02: Visualizar Painel › Navegação a partir do Painel › CHEFE deve navegar para a visualização do subprocesso ao clicar em um processo

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:85:17

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-279de-so-ao-clicar-em-um-processo-chromium\error-context.md

15) [chromium] › e2e\cdu-02.spec.ts:91:9 › CDU-02: Visualizar Painel › Navegação a partir do Painel › GESTOR deve navegar para os detalhes do processo e interagir com a árvore de unidades

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:92:13

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-70c4a-ir-com-a-árvore-de-unidades-chromium\error-context.md

16) [chromium] › e2e\cdu-02.spec.ts:104:9 › CDU-02: Visualizar Painel › Tabela de Alertas › deve mostrar alertas na tabela com as colunas corretas

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:102:43

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-257dc-ela-com-as-colunas-corretas-chromium\error-context.md

17) [chromium] › e2e\cdu-02.spec.ts:111:9 › CDU-02: Visualizar Painel › Tabela de Alertas › deve exibir alertas ordenados por data/hora decrescente inicialmente

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-02.spec.ts:102:43

    Error Context: test-results\cdu-02-CDU-02-Visualizar-P-f55d1-ra-decrescente-inicialmente-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:34:5 › CDU-03: Manter processo › deve acessar tela de criação de processo
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
   ⚠️  Teste executado com 2 erro(s) crítico(s)
   [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
   📍 Page Console
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
[chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
3. Failed to load resource: net::ERR_FAILED
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
   ==========
   [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
   📍 Page Console
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades

📋 CONSOLE (3):
[chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição

📋 JAVASCRIPT (1):
[chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
1. AxiosError
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
   📍 Page Console
   [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição

📋 NETWORK (1):
[chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
2. Erro ao autenticar: AxiosError
   [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
1. net::ERR_FAILED
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
   📍 Page Console
   [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
   🔗 http://localhost:10000/api/usuarios/autenticar
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
3. Failed to load resource: net::ERR_FAILED
   [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição

📋 VUE (1):
[chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
📍 Page Console
[chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>
   [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades

📋 JAVASCRIPT (1):
[chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição

==========

[chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
18) [chromium] › e2e\cdu-03.spec.ts:34:5 › CDU-03: Manter processo › deve acessar tela de criação de processo

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-2dac3-tela-de-criação-de-processo-chromium\error-context.md

1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

19) [chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição

    Test timeout of 5000ms exceeded while running "beforeEach" hook.

      30 |
      31 | test.describe('CDU-03: Manter processo', () => {
    > 32 |     test.beforeEach(async ({page}) => await loginComoAdmin(page));
         |          ^
      33 |
      34 |     test('deve acessar tela de criação de processo', async ({page}) => {
      35 |         await navegarParaCriacaoProcesso(page);
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:10

    Error: page.waitForURL: Test timeout of 5000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-5bcf2-para-processo-sem-descrição-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:51:5 › CDU-03: Manter processo › deve permitir visualizar processo existente
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

20) [chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-40e55--para-processo-sem-unidades-chromium\error-context.md

21) [chromium] › e2e\cdu-03.spec.ts:51:5 › CDU-03: Manter processo › deve permitir visualizar processo existente

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-05d7d-sualizar-processo-existente-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:56:5 › CDU-03: Manter processo › deve mostrar erro ao tentar criar processo de revisão/diagnóstico com unidade sem mapa vigente
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

22) [chromium] › e2e\cdu-03.spec.ts:56:5 › CDU-03: Manter processo › deve mostrar erro ao tentar criar processo de revisão/diagnóstico com unidade sem mapa vigente

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-30058-om-unidade-sem-mapa-vigente-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:64:5 › CDU-03: Manter processo › deve selecionar automaticamente unidades filhas ao clicar em unidade intermediária
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

23) [chromium] › e2e\cdu-03.spec.ts:64:5 › CDU-03: Manter processo › deve selecionar automaticamente unidades filhas ao clicar em unidade intermediária

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-5d63b-ar-em-unidade-intermediária-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:70:5 › CDU-03: Manter processo › deve selecionar nó raiz da subárvore se todas as unidades filhas forem selecionadas
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

24) [chromium] › e2e\cdu-03.spec.ts:70:5 › CDU-03: Manter processo › deve selecionar nó raiz da subárvore se todas as unidades filhas forem selecionadas

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-af86d-s-filhas-forem-selecionadas-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:76:5 › CDU-03: Manter processo › deve colocar nó raiz em estado intermediário ao desmarcar uma unidade filha
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

25) [chromium] › e2e\cdu-03.spec.ts:76:5 › CDU-03: Manter processo › deve colocar nó raiz em estado intermediário ao desmarcar uma unidade filha

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-6800b-desmarcar-uma-unidade-filha-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:82:5 › CDU-03: Manter processo › deve permitir marcar e desmarcar unidades independentemente
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-03.spec.ts:88:5 › CDU-03: Manter processo › deve permitir selecionar unidade interoperacional sem selecionar subordinadas
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

26) [chromium] › e2e\cdu-03.spec.ts:82:5 › CDU-03: Manter processo › deve permitir marcar e desmarcar unidades independentemente

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-43a33--unidades-independentemente-chromium\error-context.md

27) [chromium] › e2e\cdu-03.spec.ts:88:5 › CDU-03: Manter processo › deve permitir selecionar unidade interoperacional sem selecionar subordinadas

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-06921-sem-selecionar-subordinadas-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:93:5 › CDU-03: Manter processo › deve criar processo com sucesso e redirecionar para o Painel
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-03.spec.ts:99:5 › CDU-03: Manter processo › deve editar processo com sucesso e refletir as alterações no Painel
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

28) [chromium] › e2e\cdu-03.spec.ts:99:5 › CDU-03: Manter processo › deve editar processo com sucesso e refletir as alterações no Painel

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-84051-tir-as-alterações-no-Painel-chromium\error-context.md

29) [chromium] › e2e\cdu-03.spec.ts:93:5 › CDU-03: Manter processo › deve criar processo com sucesso e redirecionar para o Painel

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-aab2c--redirecionar-para-o-Painel-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:118:5 › CDU-03: Manter processo › deve remover processo com sucesso após confirmação
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

30) [chromium] › e2e\cdu-03.spec.ts:118:5 › CDU-03: Manter processo › deve remover processo com sucesso após confirmação

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-f8d8b-om-sucesso-após-confirmação-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:135:5 › CDU-03: Manter processo › deve cancelar a remoção do processo
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

31) [chromium] › e2e\cdu-03.spec.ts:135:5 › CDU-03: Manter processo › deve cancelar a remoção do processo

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-94eb2-celar-a-remoção-do-processo-chromium\error-context.md

[chromium] › e2e\cdu-03.spec.ts:155:5 › CDU-03: Manter processo › deve permitir preencher a data limite da etapa 1
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

32) [chromium] › e2e\cdu-03.spec.ts:155:5 › CDU-03: Manter processo › deve permitir preencher a data limite da etapa 1

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-03.spec.ts:32:39

    Error Context: test-results\cdu-03-CDU-03-Manter-proce-d9c0d-er-a-data-limite-da-etapa-1-chromium\error-context.md

[chromium] › e2e\cdu-04.spec.ts:21:5 › CDU-04: Iniciar processo de mapeamento › deve iniciar processo de mapeamento
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

33) [chromium] › e2e\cdu-04.spec.ts:21:5 › CDU-04: Iniciar processo de mapeamento › deve iniciar processo de mapeamento

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-04.spec.ts:19:39

    Error Context: test-results\cdu-04-CDU-04-Iniciar-proc-d61d8-ciar-processo-de-mapeamento-chromium\error-context.md

[chromium] › e2e\cdu-04.spec.ts:41:5 › CDU-04: Iniciar processo de mapeamento › deve cancelar o início do processo
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-05.spec.ts:21:5 › CDU-05: Iniciar processo de revisão › deve iniciar processo de revisão com sucesso
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

34) [chromium] › e2e\cdu-04.spec.ts:41:5 › CDU-04: Iniciar processo de mapeamento › deve cancelar o início do processo

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-04.spec.ts:19:39

    Error Context: test-results\cdu-04-CDU-04-Iniciar-proc-6d045-ncelar-o-início-do-processo-chromium\error-context.md

35) [chromium] › e2e\cdu-05.spec.ts:21:5 › CDU-05: Iniciar processo de revisão › deve iniciar processo de revisão com sucesso

    Test timeout of 5000ms exceeded while running "beforeEach" hook.

      17 |
      18 | test.describe('CDU-05: Iniciar processo de revisão', () => {
    > 19 |     test.beforeEach(async ({page}) => await loginComoAdmin(page));
         |          ^
      20 |
      21 |     test('deve iniciar processo de revisão com sucesso', async ({page}) => {
      22 |         const nomeProcesso = `PROCESSO REVISAO TESTE - ${Date.now()}`;
        at C:\sgc\frontend\e2e\cdu-05.spec.ts:19:10

    Error: page.waitForURL: Test timeout of 5000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-05.spec.ts:19:39

    Error Context: test-results\cdu-05-CDU-05-Iniciar-proc-e9d54-esso-de-revisão-com-sucesso-chromium\error-context.md

[chromium] › e2e\cdu-05.spec.ts:41:5 › CDU-05: Iniciar processo de revisão › deve cancelar o início do processo de revisão
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-06.spec.ts:22:5 › CDU-06: Detalhar processo › deve mostrar detalhes do processo para ADMIN
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

36) [chromium] › e2e\cdu-05.spec.ts:41:5 › CDU-05: Iniciar processo de revisão › deve cancelar o início do processo de revisão

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-05.spec.ts:19:39

    Error Context: test-results\cdu-05-CDU-05-Iniciar-proc-08fce-ício-do-processo-de-revisão-chromium\error-context.md

37) [chromium] › e2e\cdu-06.spec.ts:22:5 › CDU-06: Detalhar processo › deve mostrar detalhes do processo para ADMIN

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-06.spec.ts:16:9

    Error Context: test-results\cdu-06-CDU-06-Detalhar-pro-d68dc-lhes-do-processo-para-ADMIN-chromium\error-context.md

[chromium] › e2e\cdu-06.spec.ts:27:5 › CDU-06: Detalhar processo › deve permitir clicar em unidade
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

38) [chromium] › e2e\cdu-06.spec.ts:27:5 › CDU-06: Detalhar processo › deve permitir clicar em unidade

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-06.spec.ts:16:9

    Error Context: test-results\cdu-06-CDU-06-Detalhar-pro-6e51b--permitir-clicar-em-unidade-chromium\error-context.md

[chromium] › e2e\cdu-07.spec.ts:7:5 › CDU-07: Detalhar subprocesso › deve mostrar detalhes do subprocesso para CHEFE
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

39) [chromium] › e2e\cdu-07.spec.ts:7:5 › CDU-07: Detalhar subprocesso › deve mostrar detalhes do subprocesso para CHEFE

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-07.spec.ts:5:39

    Error Context: test-results\cdu-07-CDU-07-Detalhar-sub-48b1b-s-do-subprocesso-para-CHEFE-chromium\error-context.md

40) [chromium] › e2e\cdu-08.spec.ts:24:5 › CDU-08 - Manter cadastro de atividades e conhecimentos › deve adicionar, editar e remover atividades e conhecimentos

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-08.spec.ts:25:30

    Error Context: test-results\cdu-08-CDU-08---Manter-cad-92b6d--atividades-e-conhecimentos-chromium\error-context.md

41) [chromium] › e2e\cdu-09.spec.ts:26:5 › CDU-09: Disponibilizar cadastro de atividades › deve avisar sobre atividades sem conhecimentos e depois disponibilizar com sucesso

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-09.spec.ts:27:30

    Error Context: test-results\cdu-09-CDU-09-Disponibiliz-f7cc7--disponibilizar-com-sucesso-chromium\error-context.md

42) [chromium] › e2e\cdu-09.spec.ts:62:5 › CDU-09: Disponibilizar cadastro de atividades › deve exibir o histórico de análise após devolução

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-09.spec.ts:63:30

    Error Context: test-results\cdu-09-CDU-09-Disponibiliz-da385-o-de-análise-após-devolução-chromium\error-context.md

43) [chromium] › e2e\cdu-10.spec.ts:23:5 › CDU-10: Disponibilizar revisão do cadastro › deve disponibilizar a revisão com sucesso após corrigir atividades incompletas

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-10.spec.ts:24:30

    Error Context: test-results\cdu-10-CDU-10-Disponibiliz-a2019-igir-atividades-incompletas-chromium\error-context.md

44) [chromium] › e2e\cdu-10.spec.ts:49:5 › CDU-10: Disponibilizar revisão do cadastro › deve exibir o histórico de análise após a devolução de um cadastro em revisão

    Test timeout of 5000ms exceeded.

    Error: page.fill: Test timeout of 5000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-10.spec.ts:51:30

    Error Context: test-results\cdu-10-CDU-10-Disponibiliz-234f4-o-de-um-cadastro-em-revisão-chromium\error-context.md

45) [chromium] › e2e\cdu-11.spec.ts:42:5 › CDU-11: Visualizar cadastro de atividades (somente leitura) › ADMIN deve visualizar cadastro em modo somente leitura

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-11.spec.ts:28:25

    Error Context: test-results\cdu-11-CDU-11-Visualizar-c-fbba1-tro-em-modo-somente-leitura-chromium\error-context.md

46) [chromium] › e2e\cdu-11.spec.ts:53:5 › CDU-11: Visualizar cadastro de atividades (somente leitura) › GESTOR da unidade superior deve visualizar cadastro em modo somente leitura

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-11.spec.ts:28:25

    Error Context: test-results\cdu-11-CDU-11-Visualizar-c-ee04a-tro-em-modo-somente-leitura-chromium\error-context.md

47) [chromium] › e2e\cdu-11.spec.ts:64:5 › CDU-11: Visualizar cadastro de atividades (somente leitura) › CHEFE de outra unidade não deve ver os botões de edição

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-11.spec.ts:28:25

    Error Context: test-results\cdu-11-CDU-11-Visualizar-c-4b915-eve-ver-os-botões-de-edição-chromium\error-context.md

48) [chromium] › e2e\cdu-12.spec.ts:22:5 › CDU-12: Verificar impactos no mapa de competências › deve exibir mensagem de "Nenhum impacto" quando não houver divergências

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-12.spec.ts:20:39

    Error Context: test-results\cdu-12-CDU-12-Verificar-im-4e49d-ndo-não-houver-divergências-chromium\error-context.md

49) [chromium] › e2e\cdu-12.spec.ts:28:5 › CDU-12: Verificar impactos no mapa de competências › deve exibir modal com impactos quando houver divergências

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-12.spec.ts:20:39

    Error Context: test-results\cdu-12-CDU-12-Verificar-im-a8704--quando-houver-divergências-chromium\error-context.md

50) [chromium] › e2e\cdu-13.spec.ts:24:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › deve exibir modal de Histórico de análise

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-13.spec.ts:25:9

    Error Context: test-results\cdu-13-CDU-13-Analisar-cad-7c779-dal-de-Histórico-de-análise-chromium\error-context.md

51) [chromium] › e2e\cdu-13.spec.ts:35:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › GESTOR deve conseguir devolver cadastro para ajustes

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-13.spec.ts:36:9

    Error Context: test-results\cdu-13-CDU-13-Analisar-cad-2f45e-olver-cadastro-para-ajustes-chromium\error-context.md

52) [chromium] › e2e\cdu-13.spec.ts:45:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › ADMIN deve conseguir devolver cadastro para ajustes

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-13.spec.ts:46:9

    Error Context: test-results\cdu-13-CDU-13-Analisar-cad-e8e36-olver-cadastro-para-ajustes-chromium\error-context.md

53) [chromium] › e2e\cdu-13.spec.ts:55:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › GESTOR deve conseguir registrar aceite do cadastro

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-13.spec.ts:56:9

    Error Context: test-results\cdu-13-CDU-13-Analisar-cad-3ea92-egistrar-aceite-do-cadastro-chromium\error-context.md

54) [chromium] › e2e\cdu-13.spec.ts:65:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › ADMIN deve conseguir homologar o cadastro

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-13.spec.ts:66:9

    Error Context: test-results\cdu-13-CDU-13-Analisar-cad-0d1d3-seguir-homologar-o-cadastro-chromium\error-context.md

[chromium] › e2e\cdu-14.spec.ts:27:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve apresentar ações adequadas para cada perfil
⚠️  Teste executado com 3 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 5 erro(s) encontrado(s)
==========

📋 CONSOLE (1):
1. Failed to load resource: the server responded with a status of 500 (Internal Server Error)
   📍 Page Console

📋 NETWORK (1):
1. net::ERR_ABORTED
   🔗 http://localhost:5173/src/views/CadProcesso.vue

📋 VUE (3):
1. [VUE] ⚠️  VUE WARNING: [Vue Router warn]: uncaught error during route navigation:
2. [VUE] TypeError: Failed to fetch dynamically imported module: http://localhost:5173/src/views/CadProcesso.vue
3. [VUE] ⚠️  VUE WARNING: [Vue Router warn]: Unexpected error when starting the router: TypeError: Failed to fetch dynamically imported module: http://localhost:5173/src/views/CadProcesso.vue

==========

55) [chromium] › e2e\cdu-14.spec.ts:27:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve apresentar ações adequadas para cada perfil

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-14.spec.ts:24:20

    Error Context: test-results\cdu-14-CDU-14-Analisar-rev-8de4a--adequadas-para-cada-perfil-chromium\error-context.md

[chromium] › e2e\cdu-14.spec.ts:35:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve permitir devolver e registrar aceite da revisão
⚠️  Teste executado com 3 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 5 erro(s) encontrado(s)
==========

📋 CONSOLE (1):
1. Failed to load resource: the server responded with a status of 500 (Internal Server Error)
   📍 Page Console

📋 NETWORK (1):
1. net::ERR_ABORTED
   🔗 http://localhost:5173/src/views/CadProcesso.vue

📋 VUE (3):
1. [VUE] ⚠️  VUE WARNING: [Vue Router warn]: uncaught error during route navigation:
2. [VUE] TypeError: Failed to fetch dynamically imported module: http://localhost:5173/src/views/CadProcesso.vue
3. [VUE] ⚠️  VUE WARNING: [Vue Router warn]: Unexpected error when starting the router: TypeError: Failed to fetch dynamically imported module: http://localhost:5173/src/views/CadProcesso.vue

==========

56) [chromium] › e2e\cdu-14.spec.ts:35:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve permitir devolver e registrar aceite da revisão

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-14.spec.ts:24:20

    Error Context: test-results\cdu-14-CDU-14-Analisar-rev-1fa64-registrar-aceite-da-revisão-chromium\error-context.md

[chromium] › e2e\cdu-14.spec.ts:45:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve exibir histórico de análise
⚠️  Teste executado com 3 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 5 erro(s) encontrado(s)
==========

📋 CONSOLE (1):
1. Failed to load resource: the server responded with a status of 500 (Internal Server Error)
   📍 Page Console

📋 NETWORK (1):
1. net::ERR_ABORTED
   🔗 http://localhost:5173/src/views/CadProcesso.vue

📋 VUE (3):
1. [VUE] ⚠️  VUE WARNING: [Vue Router warn]: uncaught error during route navigation:
2. [VUE] TypeError: Failed to fetch dynamically imported module: http://localhost:5173/src/views/CadProcesso.vue
3. [VUE] ⚠️  VUE WARNING: [Vue Router warn]: Unexpected error when starting the router: TypeError: Failed to fetch dynamically imported module: http://localhost:5173/src/views/CadProcesso.vue

==========

57) [chromium] › e2e\cdu-14.spec.ts:45:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve exibir histórico de análise

    TimeoutError: page.fill: Timeout 2000ms exceeded.
    Call log:
      - waiting for locator('#descricao')


       at helpers\acoes\acoes-processo.ts:31

      29 |     sticChecked: boolean = false
      30 | ): Promise<void> {
    > 31 |     await page.fill(SELETORES_CSS.CAMPO_DESCRICAO, descricao);
         |                ^
      32 |     await page.selectOption(SELETORES_CSS.CAMPO_TIPO, tipo);
      33 |
      34 |     if (dataLimite) {
        at preencherFormularioProcesso (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:31:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:83:11)
        at C:\sgc\frontend\e2e\cdu-14.spec.ts:24:20

    Error Context: test-results\cdu-14-CDU-14-Analisar-rev-3117b-exibir-histórico-de-análise-chromium\error-context.md

[chromium] › e2e\cdu-15.spec.ts:29:5 › CDU-15: Manter mapa de competências › deve exibir tela de edição de mapa com elementos corretos
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-15.spec.ts:34:5 › CDU-15: Manter mapa de competências › deve criar competência e alterar situação do subprocesso
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

58) [chromium] › e2e\cdu-15.spec.ts:29:5 › CDU-15: Manter mapa de competências › deve exibir tela de edição de mapa com elementos corretos

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-15.spec.ts:19:5)
        at C:\sgc\frontend\e2e\cdu-15.spec.ts:26:9

    Error Context: test-results\cdu-15-CDU-15-Manter-mapa--cdef0-mapa-com-elementos-corretos-chromium\error-context.md

59) [chromium] › e2e\cdu-15.spec.ts:34:5 › CDU-15: Manter mapa de competências › deve criar competência e alterar situação do subprocesso

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-15.spec.ts:19:5)
        at C:\sgc\frontend\e2e\cdu-15.spec.ts:26:9

    Error Context: test-results\cdu-15-CDU-15-Manter-mapa--62dcf-rar-situação-do-subprocesso-chromium\error-context.md

[chromium] › e2e\cdu-15.spec.ts:42:5 › CDU-15: Manter mapa de competências › deve editar competência existente
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-15.spec.ts:54:5 › CDU-15: Manter mapa de competências › deve excluir competência com confirmação
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

60) [chromium] › e2e\cdu-15.spec.ts:42:5 › CDU-15: Manter mapa de competências › deve editar competência existente

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-15.spec.ts:19:5)
        at C:\sgc\frontend\e2e\cdu-15.spec.ts:26:9

    Error Context: test-results\cdu-15-CDU-15-Manter-mapa--b68bc-ditar-competência-existente-chromium\error-context.md

61) [chromium] › e2e\cdu-15.spec.ts:54:5 › CDU-15: Manter mapa de competências › deve excluir competência com confirmação

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-15.spec.ts:19:5)
        at C:\sgc\frontend\e2e\cdu-15.spec.ts:26:9

    Error Context: test-results\cdu-15-CDU-15-Manter-mapa--90129-competência-com-confirmação-chromium\error-context.md

[chromium] › e2e\cdu-16.spec.ts:30:5 › CDU-16: Ajustar mapa de competências › deve exibir botão "Impacto no mapa" para ADMIN em processo de Revisão
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

62) [chromium] › e2e\cdu-16.spec.ts:30:5 › CDU-16: Ajustar mapa de competências › deve exibir botão "Impacto no mapa" para ADMIN em processo de Revisão

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapaRevisao (C:\sgc\frontend\e2e\cdu-16.spec.ts:24:5)
        at C:\sgc\frontend\e2e\cdu-16.spec.ts:31:9

    Error Context: test-results\cdu-16-CDU-16-Ajustar-mapa-2ec07-DMIN-em-processo-de-Revisão-chromium\error-context.md

[chromium] › e2e\cdu-16.spec.ts:37:5 › CDU-16: Ajustar mapa de competências › deve abrir modal de impactos no mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

63) [chromium] › e2e\cdu-16.spec.ts:37:5 › CDU-16: Ajustar mapa de competências › deve abrir modal de impactos no mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapaRevisao (C:\sgc\frontend\e2e\cdu-16.spec.ts:24:5)
        at C:\sgc\frontend\e2e\cdu-16.spec.ts:38:9

    Error Context: test-results\cdu-16-CDU-16-Ajustar-mapa-0e8fa-r-modal-de-impactos-no-mapa-chromium\error-context.md

[chromium] › e2e\cdu-16.spec.ts:49:5 › CDU-16: Ajustar mapa de competências › deve permitir criação de competências
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

64) [chromium] › e2e\cdu-16.spec.ts:49:5 › CDU-16: Ajustar mapa de competências › deve permitir criação de competências

    Test timeout of 5000ms exceeded.

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapaRevisao (C:\sgc\frontend\e2e\cdu-16.spec.ts:24:5)
        at C:\sgc\frontend\e2e\cdu-16.spec.ts:50:9

    Error Context: test-results\cdu-16-CDU-16-Ajustar-mapa-3aa20-tir-criação-de-competências-chromium\error-context.md

[chromium] › e2e\cdu-16.spec.ts:59:5 › CDU-16: Ajustar mapa de competências › deve permitir edição de competências
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-16.spec.ts:71:5 › CDU-16: Ajustar mapa de competências › deve permitir exclusão de competências
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-16.spec.ts:81:5 › CDU-16: Ajustar mapa de competências › deve validar associação de todas as atividades
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

65) [chromium] › e2e\cdu-16.spec.ts:59:5 › CDU-16: Ajustar mapa de competências › deve permitir edição de competências

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapaRevisao (C:\sgc\frontend\e2e\cdu-16.spec.ts:24:5)
        at C:\sgc\frontend\e2e\cdu-16.spec.ts:63:9

    Error Context: test-results\cdu-16-CDU-16-Ajustar-mapa-39f76-itir-edição-de-competências-chromium\error-context.md

66) [chromium] › e2e\cdu-16.spec.ts:81:5 › CDU-16: Ajustar mapa de competências › deve validar associação de todas as atividades

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapaRevisao (C:\sgc\frontend\e2e\cdu-16.spec.ts:24:5)
        at C:\sgc\frontend\e2e\cdu-16.spec.ts:82:9

    Error Context: test-results\cdu-16-CDU-16-Ajustar-mapa-1fee4-ação-de-todas-as-atividades-chromium\error-context.md

67) [chromium] › e2e\cdu-16.spec.ts:71:5 › CDU-16: Ajustar mapa de competências › deve permitir exclusão de competências

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapaRevisao (C:\sgc\frontend\e2e\cdu-16.spec.ts:24:5)
        at C:\sgc\frontend\e2e\cdu-16.spec.ts:74:9

    Error Context: test-results\cdu-16-CDU-16-Ajustar-mapa-dde4c-ir-exclusão-de-competências-chromium\error-context.md

[chromium] › e2e\cdu-16.spec.ts:88:5 › CDU-16: Ajustar mapa de competências › deve integrar com disponibilização de mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-17.spec.ts:26:5 › CDU-17: Disponibilizar mapa de competências › deve exibir modal com título e campos corretos
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

68) [chromium] › e2e\cdu-16.spec.ts:88:5 › CDU-16: Ajustar mapa de competências › deve integrar com disponibilização de mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapaRevisao (C:\sgc\frontend\e2e\cdu-16.spec.ts:24:5)
        at C:\sgc\frontend\e2e\cdu-16.spec.ts:89:9

    Error Context: test-results\cdu-16-CDU-16-Ajustar-mapa-db5dc-om-disponibilização-de-mapa-chromium\error-context.md

69) [chromium] › e2e\cdu-17.spec.ts:26:5 › CDU-17: Disponibilizar mapa de competências › deve exibir modal com título e campos corretos

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-17.spec.ts:20:5)
        at C:\sgc\frontend\e2e\cdu-17.spec.ts:27:9

    Error Context: test-results\cdu-17-CDU-17-Disponibiliz-f1b20-om-título-e-campos-corretos-chromium\error-context.md

[chromium] › e2e\cdu-17.spec.ts:34:5 › CDU-17: Disponibilizar mapa de competências › deve preencher observações no modal
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

70) [chromium] › e2e\cdu-17.spec.ts:34:5 › CDU-17: Disponibilizar mapa de competências › deve preencher observações no modal

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-17.spec.ts:20:5)
        at C:\sgc\frontend\e2e\cdu-17.spec.ts:35:9

    Error Context: test-results\cdu-17-CDU-17-Disponibiliz-40ccf-encher-observações-no-modal-chromium\error-context.md

[chromium] › e2e\cdu-17.spec.ts:43:5 › CDU-17: Disponibilizar mapa de competências › deve validar data obrigatória
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

71) [chromium] › e2e\cdu-17.spec.ts:43:5 › CDU-17: Disponibilizar mapa de competências › deve validar data obrigatória

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-17.spec.ts:20:5)
        at C:\sgc\frontend\e2e\cdu-17.spec.ts:44:9

    Error Context: test-results\cdu-17-CDU-17-Disponibiliz-cdda1-ve-validar-data-obrigatória-chromium\error-context.md

[chromium] › e2e\cdu-17.spec.ts:54:5 › CDU-17: Disponibilizar mapa de competências › deve validar campos obrigatórios do modal
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

72) [chromium] › e2e\cdu-17.spec.ts:54:5 › CDU-17: Disponibilizar mapa de competências › deve validar campos obrigatórios do modal

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-17.spec.ts:20:5)
        at C:\sgc\frontend\e2e\cdu-17.spec.ts:55:9

    Error Context: test-results\cdu-17-CDU-17-Disponibiliz-6a50b-ampos-obrigatórios-do-modal-chromium\error-context.md

[chromium] › e2e\cdu-17.spec.ts:69:5 › CDU-17: Disponibilizar mapa de competências › deve processar disponibilização
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-17.spec.ts:82:5 › CDU-17: Disponibilizar mapa de competências › deve cancelar disponibilização
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-18.spec.ts:19:5 › CDU-18: Visualizar mapa de competências › ADMIN: navegar até visualização do mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

73) [chromium] › e2e\cdu-17.spec.ts:69:5 › CDU-17: Disponibilizar mapa de competências › deve processar disponibilização

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-17.spec.ts:20:5)
        at C:\sgc\frontend\e2e\cdu-17.spec.ts:70:9

    Error Context: test-results\cdu-17-CDU-17-Disponibiliz-201fa--processar-disponibilização-chromium\error-context.md

74) [chromium] › e2e\cdu-17.spec.ts:82:5 › CDU-17: Disponibilizar mapa de competências › deve cancelar disponibilização

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at navegarParaMapa (C:\sgc\frontend\e2e\cdu-17.spec.ts:20:5)
        at C:\sgc\frontend\e2e\cdu-17.spec.ts:83:9

    Error Context: test-results\cdu-17-CDU-17-Disponibiliz-826e3-e-cancelar-disponibilização-chromium\error-context.md

75) [chromium] › e2e\cdu-18.spec.ts:19:5 › CDU-18: Visualizar mapa de competências › ADMIN: navegar até visualização do mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-18.spec.ts:20:9

    Error Context: test-results\cdu-18-CDU-18-Visualizar-m-bc427-ar-até-visualização-do-mapa-chromium\error-context.md

[chromium] › e2e\cdu-18.spec.ts:26:5 › CDU-18: Visualizar mapa de competências › CHEFE: navegar direto para subprocesso e visualizar mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-18.spec.ts:33:5 › CDU-18: Visualizar mapa de competências › deve verificar elementos obrigatórios da visualização do mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

76) [chromium] › e2e\cdu-18.spec.ts:26:5 › CDU-18: Visualizar mapa de competências › CHEFE: navegar direto para subprocesso e visualizar mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-18.spec.ts:27:9

    Error Context: test-results\cdu-18-CDU-18-Visualizar-m-f9c7b-bprocesso-e-visualizar-mapa-chromium\error-context.md

77) [chromium] › e2e\cdu-18.spec.ts:33:5 › CDU-18: Visualizar mapa de competências › deve verificar elementos obrigatórios da visualização do mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-18.spec.ts:34:9

    Error Context: test-results\cdu-18-CDU-18-Visualizar-m-8d149-ios-da-visualização-do-mapa-chromium\error-context.md

[chromium] › e2e\cdu-18.spec.ts:42:5 › CDU-18: Visualizar mapa de competências › SERVIDOR: não exibe controles de ação na visualização
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

78) [chromium] › e2e\cdu-18.spec.ts:42:5 › CDU-18: Visualizar mapa de competências › SERVIDOR: não exibe controles de ação na visualização

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-18.spec.ts:43:9

    Error Context: test-results\cdu-18-CDU-18-Visualizar-m-7ce33-les-de-ação-na-visualização-chromium\error-context.md

[chromium] › e2e\cdu-19.spec.ts:21:5 › CDU-19: Validar mapa de competências › deve exibir botões Apresentar sugestões e Validar para CHEFE
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

79) [chromium] › e2e\cdu-19.spec.ts:21:5 › CDU-19: Validar mapa de competências › deve exibir botões Apresentar sugestões e Validar para CHEFE

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-19.spec.ts:19:39

    Error Context: test-results\cdu-19-CDU-19-Validar-mapa-2d6fa-estões-e-Validar-para-CHEFE-chromium\error-context.md

[chromium] › e2e\cdu-19.spec.ts:28:5 › CDU-19: Validar mapa de competências › deve exibir botão Histórico de análise e abrir modal
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

80) [chromium] › e2e\cdu-19.spec.ts:28:5 › CDU-19: Validar mapa de competências › deve exibir botão Histórico de análise e abrir modal

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-19.spec.ts:19:39

    Error Context: test-results\cdu-19-CDU-19-Validar-mapa-5af99-co-de-análise-e-abrir-modal-chromium\error-context.md

[chromium] › e2e\cdu-19.spec.ts:35:5 › CDU-19: Validar mapa de competências › deve permitir apresentar sugestões
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-19.spec.ts:41:5 › CDU-19: Validar mapa de competências › deve permitir validar mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-19.spec.ts:47:5 › CDU-19: Validar mapa de competências › deve cancelar apresentação de sugestões
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

81) [chromium] › e2e\cdu-19.spec.ts:35:5 › CDU-19: Validar mapa de competências › deve permitir apresentar sugestões

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-19.spec.ts:19:39

    Error Context: test-results\cdu-19-CDU-19-Validar-mapa-661ca-rmitir-apresentar-sugestões-chromium\error-context.md

82) [chromium] › e2e\cdu-19.spec.ts:41:5 › CDU-19: Validar mapa de competências › deve permitir validar mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-19.spec.ts:19:39

    Error Context: test-results\cdu-19-CDU-19-Validar-mapa-0eef7--deve-permitir-validar-mapa-chromium\error-context.md

83) [chromium] › e2e\cdu-19.spec.ts:47:5 › CDU-19: Validar mapa de competências › deve cancelar apresentação de sugestões

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-19.spec.ts:19:39

    Error Context: test-results\cdu-19-CDU-19-Validar-mapa-483e9-r-apresentação-de-sugestões-chromium\error-context.md

[chromium] › e2e\cdu-19.spec.ts:55:5 › CDU-19: Validar mapa de competências › deve cancelar validação de mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-20.spec.ts:22:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve exibir botões para GESTOR analisar mapa validado
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

84) [chromium] › e2e\cdu-19.spec.ts:55:5 › CDU-19: Validar mapa de competências › deve cancelar validação de mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-19.spec.ts:19:39

    Error Context: test-results\cdu-19-CDU-19-Validar-mapa-61f0d--cancelar-validação-de-mapa-chromium\error-context.md

85) [chromium] › e2e\cdu-20.spec.ts:22:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve exibir botões para GESTOR analisar mapa validado

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:20:43

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-1b9b7-STOR-analisar-mapa-validado-chromium\error-context.md

[chromium] › e2e\cdu-20.spec.ts:27:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve permitir devolver para ajustes
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

86) [chromium] › e2e\cdu-20.spec.ts:27:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve permitir devolver para ajustes

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:20:43

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-3113d-mitir-devolver-para-ajustes-chromium\error-context.md

[chromium] › e2e\cdu-20.spec.ts:33:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve permitir registrar aceite
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

87) [chromium] › e2e\cdu-20.spec.ts:33:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve permitir registrar aceite

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:20:43

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-77e2e-e-permitir-registrar-aceite-chromium\error-context.md

[chromium] › e2e\cdu-20.spec.ts:39:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve cancelar devolução
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

88) [chromium] › e2e\cdu-20.spec.ts:39:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve cancelar devolução

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:20:43

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-f0835-TOR-deve-cancelar-devolução-chromium\error-context.md

[chromium] › e2e\cdu-20.spec.ts:64:9 › CDU-20: Analisar validação de mapa de competências › Ver sugestões › deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-20.spec.ts:56:9 › CDU-20: Analisar validação de mapa de competências › ADMIN › deve permitir homologar mapa
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

[chromium] › e2e\cdu-20.spec.ts:51:9 › CDU-20: Analisar validação de mapa de competências › ADMIN › deve exibir botão Homologar para ADMIN
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

89) [chromium] › e2e\cdu-20.spec.ts:56:9 › CDU-20: Analisar validação de mapa de competências › ADMIN › deve permitir homologar mapa

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:49:43

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-950cb-eve-permitir-homologar-mapa-chromium\error-context.md

90) [chromium] › e2e\cdu-20.spec.ts:64:9 › CDU-20: Analisar validação de mapa de competências › Ver sugestões › deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:65:13

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-f79e6-ção-for-Mapa-com-sugestões--chromium\error-context.md

91) [chromium] › e2e\cdu-20.spec.ts:51:9 › CDU-20: Analisar validação de mapa de competências › ADMIN › deve exibir botão Homologar para ADMIN

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:49:43

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-57b82--botão-Homologar-para-ADMIN-chromium\error-context.md

[chromium] › e2e\cdu-20.spec.ts:73:9 › CDU-20: Analisar validação de mapa de competências › Histórico de análise › deve exibir histórico de análise
⚠️  Teste executado com 2 erro(s) crítico(s)

🚨 RELATÓRIO DE ERROS - 6 erro(s) encontrado(s)
==========

📋 CONSOLE (3):
1. Access to XMLHttpRequest at 'http://localhost:10000/api/usuarios/autenticar' from origin 'http://localhost:5173' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   📍 Page Console
2. Erro ao autenticar: AxiosError
   📍 Page Console
3. Failed to load resource: net::ERR_FAILED
   📍 Page Console

📋 JAVASCRIPT (1):
1. AxiosError

📋 NETWORK (1):
1. net::ERR_FAILED
   🔗 http://localhost:10000/api/usuarios/autenticar

📋 VUE (1):
1. [VUE] ⚠️  VUE WARNING: [Vue warn]: Unhandled error during execution of native event handler
   at <Login onVnodeUnmounted=fn<onVnodeUnmounted> ref=Ref< Proxy(Object) > >
   at <RouterView>
   at <App>

==========

92) [chromium] › e2e\cdu-20.spec.ts:73:9 › CDU-20: Analisar validação de mapa de competências › Histórico de análise › deve exibir histórico de análise

    TimeoutError: page.waitForURL: Timeout 2000ms exceeded.
    =========================== logs ===========================
    waiting for navigation to "/painel" until "load"
    ============================================================

       at helpers\navegacao\navegacao.ts:235

      233 |     }
      234 |
    > 235 |     await page.waitForURL(URLS.PAINEL);
          |                ^
      236 |     await expect(page).toHaveURL(/\/painel/);
      237 | }
      238 |
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:235:16)
        at C:\sgc\frontend\e2e\cdu-20.spec.ts:74:13

    Error Context: test-results\cdu-20-CDU-20-Analisar-val-effe3-exibir-histórico-de-análise-chromium\error-context.md

93) [chromium] › e2e\cdu-21.spec.ts:29:9 › CDU-21: Finalizar processo › Administrador › deve finalizar processo com sucesso

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/processo/cadastro", waiting until "load"


       at helpers\navegacao\navegacao.ts:21

      19 |  */
      20 | export async function navegarParaCriacaoProcesso(page: Page): Promise<void> {
    > 21 |     await page.goto(URLS.PROCESSO_CADASTRO);
         |                ^
      22 | }
      23 |
      24 | /**
        at navegarParaCriacaoProcesso (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:21:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:82:11)
        at setupProcessoEmAndamento (C:\sgc\frontend\e2e\cdu-21.spec.ts:22:32)
        at C:\sgc\frontend\e2e\cdu-21.spec.ts:30:52

94) [chromium] › e2e\cdu-21.spec.ts:42:9 › CDU-21: Finalizar processo › Administrador › deve cancelar a finalização e permanecer na tela do processo

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/processo/cadastro", waiting until "load"


       at helpers\navegacao\navegacao.ts:21

      19 |  */
      20 | export async function navegarParaCriacaoProcesso(page: Page): Promise<void> {
    > 21 |     await page.goto(URLS.PROCESSO_CADASTRO);
         |                ^
      22 | }
      23 |
      24 | /**
        at navegarParaCriacaoProcesso (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:21:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:82:11)
        at setupProcessoEmAndamento (C:\sgc\frontend\e2e\cdu-21.spec.ts:22:32)
        at C:\sgc\frontend\e2e\cdu-21.spec.ts:43:38

95) [chromium] › e2e\visual\01-auth.spec.ts:5:5 › Captura de Telas - Autenticação › 01 - Login Page

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


      4 | test.describe('Captura de Telas - Autenticação', () => {
      5 |     test('01 - Login Page', async ({page}) => {
    > 6 |         await page.goto(URLS.LOGIN);
        |                    ^
      7 |         await page.waitForLoadState('networkidle');
      8 |         await page.screenshot({path: 'screenshots/01-login-page.png', fullPage: true});
      9 |     });
        at C:\sgc\frontend\e2e\visual\01-auth.spec.ts:6:20

96) [chromium] › e2e\cdu-21.spec.ts:56:9 › CDU-21: Finalizar processo › Restrições de perfil › não deve exibir botão Finalizar para perfil Gestor

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/processo/cadastro", waiting until "load"


       at helpers\navegacao\navegacao.ts:21

      19 |  */
      20 | export async function navegarParaCriacaoProcesso(page: Page): Promise<void> {
    > 21 |     await page.goto(URLS.PROCESSO_CADASTRO);
         |                ^
      22 | }
      23 |
      24 | /**
        at navegarParaCriacaoProcesso (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:21:16)
        at criarProcessoCompleto (C:\sgc\frontend\e2e\helpers\acoes\acoes-processo.ts:82:11)
        at setupProcessoEmAndamento (C:\sgc\frontend\e2e\cdu-21.spec.ts:22:32)
        at C:\sgc\frontend\e2e\cdu-21.spec.ts:57:38

97) [chromium] › e2e\visual\02-painel.spec.ts:5:5 › Captura de Telas - Painel › 03 - Painel - ADMIN

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\02-painel.spec.ts:6:15

98) [chromium] › e2e\visual\01-auth.spec.ts:11:5 › Captura de Telas - Autenticação › 02 - Login Page - Erro de Credenciais

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


      10 |
      11 |     test('02 - Login Page - Erro de Credenciais', async ({page}) => {
    > 12 |         await page.goto(URLS.LOGIN);
         |                    ^
      13 |         await page.getByLabel('Título eleitoral').fill('0000000000');
      14 |         await page.getByLabel('Senha').fill('senha-invalida');
      15 |         await page.getByRole('button', {name: TEXTOS.ENTRAR}).click();
        at C:\sgc\frontend\e2e\visual\01-auth.spec.ts:12:20

99) [chromium] › e2e\visual\02-painel.spec.ts:11:5 › Captura de Telas - Painel › 04 - Painel - GESTOR

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoGestor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:240:48)
        at C:\sgc\frontend\e2e\visual\02-painel.spec.ts:12:15

100) [chromium] › e2e\visual\02-painel.spec.ts:17:5 › Captura de Telas - Painel › 05 - Painel - CHEFE

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\02-painel.spec.ts:18:15

101) [chromium] › e2e\visual\02-painel.spec.ts:23:5 › Captura de Telas - Painel › 06 - Painel - SERVIDOR

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoServidor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:243:50)
        at C:\sgc\frontend\e2e\visual\02-painel.spec.ts:24:15

102) [chromium] › e2e\visual\02-painel.spec.ts:29:5 › Captura de Telas - Painel › 07 - Painel - Tabela de Processos Ordenada (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\02-painel.spec.ts:30:15

103) [chromium] › e2e\visual\02-painel.spec.ts:38:5 › Captura de Telas - Painel › 08 - Painel - Alertas Visíveis (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\02-painel.spec.ts:39:15

104) [chromium] › e2e\visual\03-processos.spec.ts:22:5 › Captura de Telas - Processos › 09 - Cadastro de Processo - Formulário Vazio (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

105) [chromium] › e2e\visual\03-processos.spec.ts:28:5 › Captura de Telas - Processos › 10 - Cadastro de Processo - Erro Descrição Vazia (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

106) [chromium] › e2e\visual\03-processos.spec.ts:35:5 › Captura de Telas - Processos › 11 - Cadastro de Processo - Unidades Selecionadas (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

107) [chromium] › e2e\visual\03-processos.spec.ts:44:5 › Captura de Telas - Processos › 12 - Edição de Processo (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

108) [chromium] › e2e\visual\03-processos.spec.ts:50:5 › Captura de Telas - Processos › 13 - Modal Confirmação Início de Processo (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

109) [chromium] › e2e\visual\03-processos.spec.ts:58:5 › Captura de Telas - Processos › 14 - Detalhes de Processo (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

110) [chromium] › e2e\visual\03-processos.spec.ts:64:5 › Captura de Telas - Processos › 15 - Detalhes de Processo (GESTOR)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

111) [chromium] › e2e\visual\03-processos.spec.ts:71:5 › Captura de Telas - Processos › 16 - Modal Aceitar em Bloco (GESTOR)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

112) [chromium] › e2e\visual\03-processos.spec.ts:83:5 › Captura de Telas - Processos › 17 - Modal Homologar em Bloco (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

113) [chromium] › e2e\visual\03-processos.spec.ts:94:5 › Captura de Telas - Processos › 18 - Modal Finalização de Processo (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\03-processos.spec.ts:17:15

114) [chromium] › e2e\visual\04-atividades.spec.ts:18:5 › Captura de Telas - Atividades › 19 - Detalhes de Subprocesso (CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:19:15

115) [chromium] › e2e\visual\04-atividades.spec.ts:25:5 › Captura de Telas - Atividades › 20 - Detalhes de Subprocesso (SERVIDOR)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoServidor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:243:50)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:26:15

116) [chromium] › e2e\visual\04-atividades.spec.ts:32:5 › Captura de Telas - Atividades › 21 - Modal Alterar Data Limite (Subprocesso)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:33:15

117) [chromium] › e2e\visual\04-atividades.spec.ts:45:5 › Captura de Telas - Atividades › 22 - Cadastro de Atividades - Vazio (CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:46:15

118) [chromium] › e2e\visual\04-atividades.spec.ts:52:5 › Captura de Telas - Atividades › 23 - Cadastro de Atividades - Com Atividade e Conhecimento (CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:53:15

119) [chromium] › e2e\visual\04-atividades.spec.ts:65:5 › Captura de Telas - Atividades › 24 - Cadastro de Atividades - Botão Impacto no Mapa (CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:66:15

120) [chromium] › e2e\visual\04-atividades.spec.ts:72:5 › Captura de Telas - Atividades › 25 - Modal Importar Atividades (CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:73:15

121) [chromium] › e2e\visual\04-atividades.spec.ts:81:5 › Captura de Telas - Atividades › 26 - Modal Histórico de Análise (Cadastro Atividades - CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:82:15

122) [chromium] › e2e\visual\04-atividades.spec.ts:91:5 › Captura de Telas - Atividades › 27 - Modal Confirmação Disponibilização (Cadastro Atividades - CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:92:15

123) [chromium] › e2e\visual\04-atividades.spec.ts:106:5 › Captura de Telas - Atividades › 28 - Visualização de Atividades - ADMIN (Somente Leitura)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:107:15

124) [chromium] › e2e\visual\04-atividades.spec.ts:113:5 › Captura de Telas - Atividades › 29 - Visualização de Atividades - GESTOR (Somente Leitura)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoGestor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:240:48)
        at C:\sgc\frontend\e2e\visual\04-atividades.spec.ts:114:15

125) [chromium] › e2e\visual\05-mapas.spec.ts:17:5 › Captura de Telas - Mapas › 30 - Mapa de Competências - Edição (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:18:15

126) [chromium] › e2e\visual\05-mapas.spec.ts:24:5 › Captura de Telas - Mapas › 31 - Mapa de Competências - Edição com Competência Criada (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:25:15

127) [chromium] › e2e\visual\05-mapas.spec.ts:33:5 › Captura de Telas - Mapas › 32 - Modal Impactos no Mapa (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:34:15

128) [chromium] › e2e\visual\05-mapas.spec.ts:52:5 › Captura de Telas - Mapas › 33 - Modal Exclusão de Competência (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:53:15

129) [chromium] › e2e\visual\05-mapas.spec.ts:65:5 › Captura de Telas - Mapas › 34 - Modal Disponibilização do Mapa (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:66:15

130) [chromium] › e2e\visual\05-mapas.spec.ts:76:5 › Captura de Telas - Mapas › 35 - Visualização de Mapa - ADMIN/GESTOR

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:77:15

131) [chromium] › e2e\visual\05-mapas.spec.ts:83:5 › Captura de Telas - Mapas › 36 - Visualização de Mapa - CHEFE/SERVIDOR

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:84:15

132) [chromium] › e2e\visual\05-mapas.spec.ts:90:5 › Captura de Telas - Mapas › 37 - Modal Apresentar Sugestões (Visualização Mapa - CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefeSedia (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:242:52)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:91:15

133) [chromium] › e2e\visual\05-mapas.spec.ts:103:5 › Captura de Telas - Mapas › 38 - Modal Validação do Mapa (Visualização Mapa - CHEFE)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefeSedia (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:242:52)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:104:15

134) [chromium] › e2e\visual\05-mapas.spec.ts:117:5 › Captura de Telas - Mapas › 39 - Modal Devolução (Visualização Mapa - GESTOR)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoGestor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:240:48)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:118:15

135) [chromium] › e2e\visual\05-mapas.spec.ts:129:5 › Captura de Telas - Mapas › 40 - Modal Aceite (Visualização Mapa - GESTOR)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoGestor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:240:48)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:130:15

136) [chromium] › e2e\visual\05-mapas.spec.ts:142:5 › Captura de Telas - Mapas › 41 - Modal Homologação (Visualização Mapa - ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:143:15

137) [chromium] › e2e\visual\05-mapas.spec.ts:155:5 › Captura de Telas - Mapas › 42 - Modal Ver Sugestões (Visualização Mapa - GESTOR)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoGestor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:240:48)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:156:15

138) [chromium] › e2e\visual\05-mapas.spec.ts:168:5 › Captura de Telas - Mapas › 43 - Modal Histórico de Análise (Visualização Mapa - GESTOR)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoGestor (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:240:48)
        at C:\sgc\frontend\e2e\visual\05-mapas.spec.ts:169:15

139) [chromium] › e2e\visual\06-unidades.spec.ts:5:5 › Captura de Telas - Unidades › 50 - Detalhes da Unidade (STIC - ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\06-unidades.spec.ts:6:15

140) [chromium] › e2e\visual\06-unidades.spec.ts:12:5 › Captura de Telas - Unidades › 51 - Detalhes da Unidade (SESEL - ADMIN, sem subordinadas)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\06-unidades.spec.ts:13:15

141) [chromium] › e2e\visual\06-unidades.spec.ts:19:5 › Captura de Telas - Unidades › 52 - Detalhes da Unidade (Inexistente - ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\06-unidades.spec.ts:20:15

142) [chromium] › e2e\visual\06-unidades.spec.ts:26:5 › Captura de Telas - Unidades › 53 - Cadastro de Atribuição Temporária (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\06-unidades.spec.ts:27:15

143) [chromium] › e2e\visual\07-admin.spec.ts:5:5 › Captura de Telas - Admin › 44 - Página de Configurações (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\07-admin.spec.ts:6:15

144) [chromium] › e2e\visual\07-admin.spec.ts:12:5 › Captura de Telas - Admin › 46 - Página de Relatórios (ADMIN)

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\07-admin.spec.ts:13:15

145) [chromium] › e2e\visual\08-navegacao.spec.ts:5:5 › Captura de Telas - Navegação › 54 - Breadcrumbs - Processo > Unidade > Mapa

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\08-navegacao.spec.ts:6:15

146) [chromium] › e2e\visual\08-navegacao.spec.ts:12:5 › Captura de Telas - Navegação › 55 - Breadcrumbs - Unidade

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoAdmin (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:239:47)
        at C:\sgc\frontend\e2e\visual\08-navegacao.spec.ts:13:15

147) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:15:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 30 - Fluxo Completo: Criação de Atividade e Conhecimento

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:16:15

148) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:51:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 31 - Estados de Hover nos Botões de Ação

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:52:15

149) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:78:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 32 - Novo Modal de Edição de Conhecimento - Estados

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:79:15

150) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:118:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 33 - Modal de Edição - Estados de Validação

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:119:15

151) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:160:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 34 - Múltiplas Atividades e Conhecimentos - Layout Complexo

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:161:15

152) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:200:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 35 - Fluxo de Edição de Múltiplos Conhecimentos

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:201:15

153) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:247:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 36 - Modal de Edição - Keyboard Shortcuts

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:248:15

154) [chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:296:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 37 - Comparação: Antes e Depois da Implementação Modal

    TimeoutError: page.goto: Timeout 2000ms exceeded.
    Call log:
      - navigating to "http://localhost:5173/login", waiting until "load"


       at helpers\navegacao\navegacao.ts:218

      216 |     const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
      217 |
    > 218 |     await page.goto(URLS.LOGIN);
          |                ^
      219 |     await page.waitForLoadState('networkidle');
      220 |
      221 |     await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(dadosUsuario.idServidor);
        at fazerLoginComo (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:218:16)
        at loginComoChefe (C:\sgc\frontend\e2e\helpers\navegacao\navegacao.ts:241:47)
        at C:\sgc\frontend\e2e\visual\09-edicao-modal-conhecimentos.spec.ts:297:15

154 failed
[chromium] › e2e\cdu-01.spec.ts:22:5 › CDU-01: Realizar login e exibir estrutura das telas › deve exibir erro para usuário não encontrado
[chromium] › e2e\cdu-01.spec.ts:28:5 › CDU-01: Realizar login e exibir estrutura das telas › deve exibir estrutura da aplicação para SERVIDOR
[chromium] › e2e\cdu-01.spec.ts:34:5 › CDU-01: Realizar login e exibir estrutura das telas › deve exibir estrutura da aplicação para ADMIN com acesso às configurações
[chromium] › e2e\cdu-01.spec.ts:40:5 › CDU-01: Realizar login e exibir estrutura das telas › deve fazer logout e retornar para a tela de login
[chromium] › e2e\cdu-02.spec.ts:31:13 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › não deve exibir o botão "Criar processo" para GESTOR
[chromium] › e2e\cdu-02.spec.ts:31:13 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › não deve exibir o botão "Criar processo" para CHEFE
[chromium] › e2e\cdu-02.spec.ts:38:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › deve exibir painel com seções Processos e Alertas para SERVIDOR
[chromium] › e2e\cdu-02.spec.ts:45:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › deve exibir o botão "Criar processo" para ADMIN
[chromium] › e2e\cdu-02.spec.ts:50:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › deve exibir processos em situação "Criado" apenas para ADMIN
[chromium] › e2e\cdu-02.spec.ts:55:9 › CDU-02: Visualizar Painel › Visibilidade de Componentes por Perfil › não deve exibir processos em situação "Criado" para GESTOR
[chromium] › e2e\cdu-02.spec.ts:64:9 › CDU-02: Visualizar Painel › Tabela de Processos › deve exibir apenas processos da unidade do usuário (e subordinadas)
[chromium] › e2e\cdu-02.spec.ts:72:9 › CDU-02: Visualizar Painel › Navegação a partir do Painel › ADMIN deve navegar para a edição ao clicar em processo "Criado"
[chromium] › e2e\cdu-02.spec.ts:84:13 › CDU-02: Visualizar Painel › Navegação a partir do Painel › SERVIDOR deve navegar para a visualização do subprocesso ao clicar em um processo
[chromium] › e2e\cdu-02.spec.ts:84:13 › CDU-02: Visualizar Painel › Navegação a partir do Painel › CHEFE deve navegar para a visualização do subprocesso ao clicar em um processo
[chromium] › e2e\cdu-02.spec.ts:91:9 › CDU-02: Visualizar Painel › Navegação a partir do Painel › GESTOR deve navegar para os detalhes do processo e interagir com a árvore de unidades
[chromium] › e2e\cdu-02.spec.ts:104:9 › CDU-02: Visualizar Painel › Tabela de Alertas › deve mostrar alertas na tabela com as colunas corretas
[chromium] › e2e\cdu-02.spec.ts:111:9 › CDU-02: Visualizar Painel › Tabela de Alertas › deve exibir alertas ordenados por data/hora decrescente inicialmente
[chromium] › e2e\cdu-03.spec.ts:34:5 › CDU-03: Manter processo › deve acessar tela de criação de processo
[chromium] › e2e\cdu-03.spec.ts:39:5 › CDU-03: Manter processo › deve mostrar erro para processo sem descrição
[chromium] › e2e\cdu-03.spec.ts:45:5 › CDU-03: Manter processo › deve mostrar erro para processo sem unidades
[chromium] › e2e\cdu-03.spec.ts:51:5 › CDU-03: Manter processo › deve permitir visualizar processo existente
[chromium] › e2e\cdu-03.spec.ts:56:5 › CDU-03: Manter processo › deve mostrar erro ao tentar criar processo de revisão/diagnóstico com unidade sem mapa vigente
[chromium] › e2e\cdu-03.spec.ts:64:5 › CDU-03: Manter processo › deve selecionar automaticamente unidades filhas ao clicar em unidade intermediária
[chromium] › e2e\cdu-03.spec.ts:70:5 › CDU-03: Manter processo › deve selecionar nó raiz da subárvore se todas as unidades filhas forem selecionadas
[chromium] › e2e\cdu-03.spec.ts:76:5 › CDU-03: Manter processo › deve colocar nó raiz em estado intermediário ao desmarcar uma unidade filha
[chromium] › e2e\cdu-03.spec.ts:82:5 › CDU-03: Manter processo › deve permitir marcar e desmarcar unidades independentemente
[chromium] › e2e\cdu-03.spec.ts:88:5 › CDU-03: Manter processo › deve permitir selecionar unidade interoperacional sem selecionar subordinadas
[chromium] › e2e\cdu-03.spec.ts:93:5 › CDU-03: Manter processo › deve criar processo com sucesso e redirecionar para o Painel
[chromium] › e2e\cdu-03.spec.ts:99:5 › CDU-03: Manter processo › deve editar processo com sucesso e refletir as alterações no Painel
[chromium] › e2e\cdu-03.spec.ts:118:5 › CDU-03: Manter processo › deve remover processo com sucesso após confirmação
[chromium] › e2e\cdu-03.spec.ts:135:5 › CDU-03: Manter processo › deve cancelar a remoção do processo
[chromium] › e2e\cdu-03.spec.ts:155:5 › CDU-03: Manter processo › deve permitir preencher a data limite da etapa 1
[chromium] › e2e\cdu-04.spec.ts:21:5 › CDU-04: Iniciar processo de mapeamento › deve iniciar processo de mapeamento
[chromium] › e2e\cdu-04.spec.ts:41:5 › CDU-04: Iniciar processo de mapeamento › deve cancelar o início do processo
[chromium] › e2e\cdu-05.spec.ts:21:5 › CDU-05: Iniciar processo de revisão › deve iniciar processo de revisão com sucesso
[chromium] › e2e\cdu-05.spec.ts:41:5 › CDU-05: Iniciar processo de revisão › deve cancelar o início do processo de revisão
[chromium] › e2e\cdu-06.spec.ts:22:5 › CDU-06: Detalhar processo › deve mostrar detalhes do processo para ADMIN
[chromium] › e2e\cdu-06.spec.ts:27:5 › CDU-06: Detalhar processo › deve permitir clicar em unidade
[chromium] › e2e\cdu-07.spec.ts:7:5 › CDU-07: Detalhar subprocesso › deve mostrar detalhes do subprocesso para CHEFE
[chromium] › e2e\cdu-08.spec.ts:24:5 › CDU-08 - Manter cadastro de atividades e conhecimentos › deve adicionar, editar e remover atividades e conhecimentos
[chromium] › e2e\cdu-09.spec.ts:26:5 › CDU-09: Disponibilizar cadastro de atividades › deve avisar sobre atividades sem conhecimentos e depois disponibilizar com sucesso
[chromium] › e2e\cdu-09.spec.ts:62:5 › CDU-09: Disponibilizar cadastro de atividades › deve exibir o histórico de análise após devolução
[chromium] › e2e\cdu-10.spec.ts:23:5 › CDU-10: Disponibilizar revisão do cadastro › deve disponibilizar a revisão com sucesso após corrigir atividades incompletas
[chromium] › e2e\cdu-10.spec.ts:49:5 › CDU-10: Disponibilizar revisão do cadastro › deve exibir o histórico de análise após a devolução de um cadastro em revisão
[chromium] › e2e\cdu-11.spec.ts:42:5 › CDU-11: Visualizar cadastro de atividades (somente leitura) › ADMIN deve visualizar cadastro em modo somente leitura
[chromium] › e2e\cdu-11.spec.ts:53:5 › CDU-11: Visualizar cadastro de atividades (somente leitura) › GESTOR da unidade superior deve visualizar cadastro em modo somente leitura
[chromium] › e2e\cdu-11.spec.ts:64:5 › CDU-11: Visualizar cadastro de atividades (somente leitura) › CHEFE de outra unidade não deve ver os botões de edição
[chromium] › e2e\cdu-12.spec.ts:22:5 › CDU-12: Verificar impactos no mapa de competências › deve exibir mensagem de "Nenhum impacto" quando não houver divergências
[chromium] › e2e\cdu-12.spec.ts:28:5 › CDU-12: Verificar impactos no mapa de competências › deve exibir modal com impactos quando houver divergências
[chromium] › e2e\cdu-13.spec.ts:24:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › deve exibir modal de Histórico de análise
[chromium] › e2e\cdu-13.spec.ts:35:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › GESTOR deve conseguir devolver cadastro para ajustes
[chromium] › e2e\cdu-13.spec.ts:45:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › ADMIN deve conseguir devolver cadastro para ajustes
[chromium] › e2e\cdu-13.spec.ts:55:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › GESTOR deve conseguir registrar aceite do cadastro
[chromium] › e2e\cdu-13.spec.ts:65:5 › CDU-13: Analisar cadastro de atividades e conhecimentos › ADMIN deve conseguir homologar o cadastro
[chromium] › e2e\cdu-14.spec.ts:27:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve apresentar ações adequadas para cada perfil
[chromium] › e2e\cdu-14.spec.ts:35:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve permitir devolver e registrar aceite da revisão
[chromium] › e2e\cdu-14.spec.ts:45:5 › CDU-14: Analisar revisão de cadastro de atividades e conhecimentos › deve exibir histórico de análise
[chromium] › e2e\cdu-15.spec.ts:29:5 › CDU-15: Manter mapa de competências › deve exibir tela de edição de mapa com elementos corretos
[chromium] › e2e\cdu-15.spec.ts:34:5 › CDU-15: Manter mapa de competências › deve criar competência e alterar situação do subprocesso
[chromium] › e2e\cdu-15.spec.ts:42:5 › CDU-15: Manter mapa de competências › deve editar competência existente
[chromium] › e2e\cdu-15.spec.ts:54:5 › CDU-15: Manter mapa de competências › deve excluir competência com confirmação
[chromium] › e2e\cdu-16.spec.ts:30:5 › CDU-16: Ajustar mapa de competências › deve exibir botão "Impacto no mapa" para ADMIN em processo de Revisão
[chromium] › e2e\cdu-16.spec.ts:37:5 › CDU-16: Ajustar mapa de competências › deve abrir modal de impactos no mapa
[chromium] › e2e\cdu-16.spec.ts:49:5 › CDU-16: Ajustar mapa de competências › deve permitir criação de competências
[chromium] › e2e\cdu-16.spec.ts:59:5 › CDU-16: Ajustar mapa de competências › deve permitir edição de competências
[chromium] › e2e\cdu-16.spec.ts:71:5 › CDU-16: Ajustar mapa de competências › deve permitir exclusão de competências
[chromium] › e2e\cdu-16.spec.ts:81:5 › CDU-16: Ajustar mapa de competências › deve validar associação de todas as atividades
[chromium] › e2e\cdu-16.spec.ts:88:5 › CDU-16: Ajustar mapa de competências › deve integrar com disponibilização de mapa
[chromium] › e2e\cdu-17.spec.ts:26:5 › CDU-17: Disponibilizar mapa de competências › deve exibir modal com título e campos corretos
[chromium] › e2e\cdu-17.spec.ts:34:5 › CDU-17: Disponibilizar mapa de competências › deve preencher observações no modal
[chromium] › e2e\cdu-17.spec.ts:43:5 › CDU-17: Disponibilizar mapa de competências › deve validar data obrigatória
[chromium] › e2e\cdu-17.spec.ts:54:5 › CDU-17: Disponibilizar mapa de competências › deve validar campos obrigatórios do modal
[chromium] › e2e\cdu-17.spec.ts:69:5 › CDU-17: Disponibilizar mapa de competências › deve processar disponibilização
[chromium] › e2e\cdu-17.spec.ts:82:5 › CDU-17: Disponibilizar mapa de competências › deve cancelar disponibilização
[chromium] › e2e\cdu-18.spec.ts:19:5 › CDU-18: Visualizar mapa de competências › ADMIN: navegar até visualização do mapa
[chromium] › e2e\cdu-18.spec.ts:26:5 › CDU-18: Visualizar mapa de competências › CHEFE: navegar direto para subprocesso e visualizar mapa
[chromium] › e2e\cdu-18.spec.ts:33:5 › CDU-18: Visualizar mapa de competências › deve verificar elementos obrigatórios da visualização do mapa
[chromium] › e2e\cdu-18.spec.ts:42:5 › CDU-18: Visualizar mapa de competências › SERVIDOR: não exibe controles de ação na visualização
[chromium] › e2e\cdu-19.spec.ts:21:5 › CDU-19: Validar mapa de competências › deve exibir botões Apresentar sugestões e Validar para CHEFE
[chromium] › e2e\cdu-19.spec.ts:28:5 › CDU-19: Validar mapa de competências › deve exibir botão Histórico de análise e abrir modal
[chromium] › e2e\cdu-19.spec.ts:35:5 › CDU-19: Validar mapa de competências › deve permitir apresentar sugestões
[chromium] › e2e\cdu-19.spec.ts:41:5 › CDU-19: Validar mapa de competências › deve permitir validar mapa
[chromium] › e2e\cdu-19.spec.ts:47:5 › CDU-19: Validar mapa de competências › deve cancelar apresentação de sugestões
[chromium] › e2e\cdu-19.spec.ts:55:5 › CDU-19: Validar mapa de competências › deve cancelar validação de mapa
[chromium] › e2e\cdu-20.spec.ts:22:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve exibir botões para GESTOR analisar mapa validado
[chromium] › e2e\cdu-20.spec.ts:27:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve permitir devolver para ajustes
[chromium] › e2e\cdu-20.spec.ts:33:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve permitir registrar aceite
[chromium] › e2e\cdu-20.spec.ts:39:9 › CDU-20: Analisar validação de mapa de competências › GESTOR › deve cancelar devolução
[chromium] › e2e\cdu-20.spec.ts:51:9 › CDU-20: Analisar validação de mapa de competências › ADMIN › deve exibir botão Homologar para ADMIN
[chromium] › e2e\cdu-20.spec.ts:56:9 › CDU-20: Analisar validação de mapa de competências › ADMIN › deve permitir homologar mapa
[chromium] › e2e\cdu-20.spec.ts:64:9 › CDU-20: Analisar validação de mapa de competências › Ver sugestões › deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"
[chromium] › e2e\cdu-20.spec.ts:73:9 › CDU-20: Analisar validação de mapa de competências › Histórico de análise › deve exibir histórico de análise
[chromium] › e2e\cdu-21.spec.ts:29:9 › CDU-21: Finalizar processo › Administrador › deve finalizar processo com sucesso
[chromium] › e2e\cdu-21.spec.ts:42:9 › CDU-21: Finalizar processo › Administrador › deve cancelar a finalização e permanecer na tela do processo
[chromium] › e2e\cdu-21.spec.ts:56:9 › CDU-21: Finalizar processo › Restrições de perfil › não deve exibir botão Finalizar para perfil Gestor
[chromium] › e2e\visual\01-auth.spec.ts:5:5 › Captura de Telas - Autenticação › 01 - Login Page
[chromium] › e2e\visual\01-auth.spec.ts:11:5 › Captura de Telas - Autenticação › 02 - Login Page - Erro de Credenciais
[chromium] › e2e\visual\02-painel.spec.ts:5:5 › Captura de Telas - Painel › 03 - Painel - ADMIN
[chromium] › e2e\visual\02-painel.spec.ts:11:5 › Captura de Telas - Painel › 04 - Painel - GESTOR
[chromium] › e2e\visual\02-painel.spec.ts:17:5 › Captura de Telas - Painel › 05 - Painel - CHEFE
[chromium] › e2e\visual\02-painel.spec.ts:23:5 › Captura de Telas - Painel › 06 - Painel - SERVIDOR
[chromium] › e2e\visual\02-painel.spec.ts:29:5 › Captura de Telas - Painel › 07 - Painel - Tabela de Processos Ordenada (ADMIN)
[chromium] › e2e\visual\02-painel.spec.ts:38:5 › Captura de Telas - Painel › 08 - Painel - Alertas Visíveis (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:22:5 › Captura de Telas - Processos › 09 - Cadastro de Processo - Formulário Vazio (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:28:5 › Captura de Telas - Processos › 10 - Cadastro de Processo - Erro Descrição Vazia (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:35:5 › Captura de Telas - Processos › 11 - Cadastro de Processo - Unidades Selecionadas (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:44:5 › Captura de Telas - Processos › 12 - Edição de Processo (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:50:5 › Captura de Telas - Processos › 13 - Modal Confirmação Início de Processo (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:58:5 › Captura de Telas - Processos › 14 - Detalhes de Processo (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:64:5 › Captura de Telas - Processos › 15 - Detalhes de Processo (GESTOR)
[chromium] › e2e\visual\03-processos.spec.ts:71:5 › Captura de Telas - Processos › 16 - Modal Aceitar em Bloco (GESTOR)
[chromium] › e2e\visual\03-processos.spec.ts:83:5 › Captura de Telas - Processos › 17 - Modal Homologar em Bloco (ADMIN)
[chromium] › e2e\visual\03-processos.spec.ts:94:5 › Captura de Telas - Processos › 18 - Modal Finalização de Processo (ADMIN)
[chromium] › e2e\visual\04-atividades.spec.ts:18:5 › Captura de Telas - Atividades › 19 - Detalhes de Subprocesso (CHEFE)
[chromium] › e2e\visual\04-atividades.spec.ts:25:5 › Captura de Telas - Atividades › 20 - Detalhes de Subprocesso (SERVIDOR)
[chromium] › e2e\visual\04-atividades.spec.ts:32:5 › Captura de Telas - Atividades › 21 - Modal Alterar Data Limite (Subprocesso)
[chromium] › e2e\visual\04-atividades.spec.ts:45:5 › Captura de Telas - Atividades › 22 - Cadastro de Atividades - Vazio (CHEFE)
[chromium] › e2e\visual\04-atividades.spec.ts:52:5 › Captura de Telas - Atividades › 23 - Cadastro de Atividades - Com Atividade e Conhecimento (CHEFE)
[chromium] › e2e\visual\04-atividades.spec.ts:65:5 › Captura de Telas - Atividades › 24 - Cadastro de Atividades - Botão Impacto no Mapa (CHEFE)
[chromium] › e2e\visual\04-atividades.spec.ts:72:5 › Captura de Telas - Atividades › 25 - Modal Importar Atividades (CHEFE)
[chromium] › e2e\visual\04-atividades.spec.ts:81:5 › Captura de Telas - Atividades › 26 - Modal Histórico de Análise (Cadastro Atividades - CHEFE)
[chromium] › e2e\visual\04-atividades.spec.ts:91:5 › Captura de Telas - Atividades › 27 - Modal Confirmação Disponibilização (Cadastro Atividades - CHEFE)
[chromium] › e2e\visual\04-atividades.spec.ts:106:5 › Captura de Telas - Atividades › 28 - Visualização de Atividades - ADMIN (Somente Leitura)
[chromium] › e2e\visual\04-atividades.spec.ts:113:5 › Captura de Telas - Atividades › 29 - Visualização de Atividades - GESTOR (Somente Leitura)
[chromium] › e2e\visual\05-mapas.spec.ts:17:5 › Captura de Telas - Mapas › 30 - Mapa de Competências - Edição (ADMIN)
[chromium] › e2e\visual\05-mapas.spec.ts:24:5 › Captura de Telas - Mapas › 31 - Mapa de Competências - Edição com Competência Criada (ADMIN)
[chromium] › e2e\visual\05-mapas.spec.ts:33:5 › Captura de Telas - Mapas › 32 - Modal Impactos no Mapa (ADMIN)
[chromium] › e2e\visual\05-mapas.spec.ts:52:5 › Captura de Telas - Mapas › 33 - Modal Exclusão de Competência (ADMIN)
[chromium] › e2e\visual\05-mapas.spec.ts:65:5 › Captura de Telas - Mapas › 34 - Modal Disponibilização do Mapa (ADMIN)
[chromium] › e2e\visual\05-mapas.spec.ts:76:5 › Captura de Telas - Mapas › 35 - Visualização de Mapa - ADMIN/GESTOR
[chromium] › e2e\visual\05-mapas.spec.ts:83:5 › Captura de Telas - Mapas › 36 - Visualização de Mapa - CHEFE/SERVIDOR
[chromium] › e2e\visual\05-mapas.spec.ts:90:5 › Captura de Telas - Mapas › 37 - Modal Apresentar Sugestões (Visualização Mapa - CHEFE)
[chromium] › e2e\visual\05-mapas.spec.ts:103:5 › Captura de Telas - Mapas › 38 - Modal Validação do Mapa (Visualização Mapa - CHEFE)
[chromium] › e2e\visual\05-mapas.spec.ts:117:5 › Captura de Telas - Mapas › 39 - Modal Devolução (Visualização Mapa - GESTOR)
[chromium] › e2e\visual\05-mapas.spec.ts:129:5 › Captura de Telas - Mapas › 40 - Modal Aceite (Visualização Mapa - GESTOR)
[chromium] › e2e\visual\05-mapas.spec.ts:142:5 › Captura de Telas - Mapas › 41 - Modal Homologação (Visualização Mapa - ADMIN)
[chromium] › e2e\visual\05-mapas.spec.ts:155:5 › Captura de Telas - Mapas › 42 - Modal Ver Sugestões (Visualização Mapa - GESTOR)
[chromium] › e2e\visual\05-mapas.spec.ts:168:5 › Captura de Telas - Mapas › 43 - Modal Histórico de Análise (Visualização Mapa - GESTOR)
[chromium] › e2e\visual\06-unidades.spec.ts:5:5 › Captura de Telas - Unidades › 50 - Detalhes da Unidade (STIC - ADMIN)
[chromium] › e2e\visual\06-unidades.spec.ts:12:5 › Captura de Telas - Unidades › 51 - Detalhes da Unidade (SESEL - ADMIN, sem subordinadas)
[chromium] › e2e\visual\06-unidades.spec.ts:19:5 › Captura de Telas - Unidades › 52 - Detalhes da Unidade (Inexistente - ADMIN)
[chromium] › e2e\visual\06-unidades.spec.ts:26:5 › Captura de Telas - Unidades › 53 - Cadastro de Atribuição Temporária (ADMIN)
[chromium] › e2e\visual\07-admin.spec.ts:5:5 › Captura de Telas - Admin › 44 - Página de Configurações (ADMIN)
[chromium] › e2e\visual\07-admin.spec.ts:12:5 › Captura de Telas - Admin › 46 - Página de Relatórios (ADMIN)
[chromium] › e2e\visual\08-navegacao.spec.ts:5:5 › Captura de Telas - Navegação › 54 - Breadcrumbs - Processo > Unidade > Mapa
[chromium] › e2e\visual\08-navegacao.spec.ts:12:5 › Captura de Telas - Navegação › 55 - Breadcrumbs - Unidade
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:15:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 30 - Fluxo Completo: Criação de Atividade e Conhecimento
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:51:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 31 - Estados de Hover nos Botões de Ação
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:78:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 32 - Novo Modal de Edição de Conhecimento - Estados
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:118:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 33 - Modal de Edição - Estados de Validação
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:160:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 34 - Múltiplas Atividades e Conhecimentos - Layout Complexo
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:200:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 35 - Fluxo de Edição de Múltiplos Conhecimentos
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:247:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 36 - Modal de Edição - Keyboard Shortcuts
[chromium] › e2e\visual\09-edicao-modal-conhecimentos.spec.ts:296:5 › Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos › 37 - Comparação: Antes e Depois da Implementação Modal
1 passed (3.4m)

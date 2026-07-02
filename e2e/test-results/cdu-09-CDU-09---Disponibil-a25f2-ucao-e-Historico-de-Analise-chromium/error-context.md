# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-09.spec.ts >> CDU-09 - Disponibilizar cadastro de atividades e conhecimentos >> Cenario 3: Devolucao e Historico de Analise
- Location: e2e\cdu-09.spec.ts:111:5

# Error details

```
Error: page.evaluate: Target page, context or browser has been closed
```

```
Error: browserContext.close: Target page, context or browser has been closed
```

```
Error: page.goto: Target page, context or browser has been closed
```

# Test source

```ts
  18  |     GESTOR_COORD: {titulo: '222222', senha: 'senha'}, // GESTOR_COORD_11 (COORD_11)
  19  |     GESTOR_COORD_21: {titulo: '999999', senha: 'senha'}, // Roger waters (COORD_21)
  20  |     GESTOR_COORD_22: {titulo: '131313', senha: 'senha'}, // Mick jagger (COORD_22)
  21  |     CHEFE_UNIDADE: {titulo: '777777', senha: 'senha'}, // Janis joplin (Assessoria 21)
  22  |     CHEFE_ASSESSORIA_22: {titulo: '888888', senha: 'senha'}, // Jimi hendrix (Assessoria 22)
  23  |     CHEFE_SECAO_211: {titulo: '101010', senha: 'senha'}, // Debbie harry (Seção 211)
  24  |     CHEFE_SECAO_212: {titulo: '181818', senha: 'senha'}, // Pete townshend (Seção 212)
  25  |     CHEFE_SECAO_221: {titulo: '141414', senha: 'senha'}, // Tina turner (Seção 221)
  26  |     CHEFE_ASSESSORIA_11: {titulo: '555555', senha: 'senha'}, // David bowie (Assessoria 11)
  27  |     CHEFE_ASSESSORIA_12: {titulo: '151515', senha: 'senha'}, // Ana Beatriz de Albuquerque e Souza (Assessoria 12)
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
  58  | async function aguardarFormularioLoginPronto(page: Page) {
  59  |     await verificarTelaLogin(page);
  60  |     await expect(page.getByTestId('inp-login-usuario')).toBeEditable();
  61  |     await expect(page.getByTestId('inp-login-senha')).toBeEditable();
  62  |     await expect(page.getByTestId('btn-login-entrar')).toBeEnabled();
  63  | }
  64  | 
  65  | export async function autenticar(page: Page, usuario: string, senha: string) {
  66  |     await aguardarFormularioLoginPronto(page);
  67  |     await page.getByTestId('inp-login-usuario').fill(usuario);
  68  |     await page.getByTestId('inp-login-senha').fill(senha);
  69  |     await page.getByTestId('btn-login-entrar').click();
  70  | }
  71  | 
  72  | async function clicarEntrarAposEstabilizar(page: Page) {
  73  |     const seletorPerfil = page.getByTestId('sel-login-perfil');
  74  |     const botaoEntrar = page.getByTestId('btn-login-entrar');
  75  |     await expect(seletorPerfil).toBeVisible();
  76  |     await expect(botaoEntrar).toBeVisible();
  77  |     await expect(botaoEntrar).toBeEnabled();
  78  | 
  79  |     // A troca de perfil expande/recolhe o bloco de autorização com animação.
  80  |     // Esperamos o botão parar de se mover antes do clique real.
  81  |     let caixaAnterior = await botaoEntrar.boundingBox();
  82  |     for (let tentativa = 0; tentativa < 10; tentativa++) {
  83  |         await page.waitForTimeout(100);
  84  |         const caixaAtual = await botaoEntrar.boundingBox();
  85  |         if (caixaAnterior && caixaAtual) {
  86  |             const mesmoX = Math.abs(caixaAtual.x - caixaAnterior.x) < 0.5;
  87  |             const mesmoY = Math.abs(caixaAtual.y - caixaAnterior.y) < 0.5;
  88  |             const mesmaLargura = Math.abs(caixaAtual.width - caixaAnterior.width) < 0.5;
  89  |             const mesmaAltura = Math.abs(caixaAtual.height - caixaAnterior.height) < 0.5;
  90  |             if (mesmoX && mesmoY && mesmaLargura && mesmaAltura) {
  91  |                 break;
  92  |             }
  93  |         }
  94  |         caixaAnterior = caixaAtual;
  95  |     }
  96  | 
  97  |     await botaoEntrar.click();
  98  | }
  99  | 
  100 | async function finalizarLoginNoPainel(page: Page) {
  101 |     await expect(page).toHaveURL(/\/painel(?:\?|$)/);
  102 |     await limparNotificacoes(page);
  103 | }
  104 | 
  105 | async function limparSessaoNavegador(page: Page) {
  106 |     // 1. Tenta limpar o storage se já estivermos no domínio do app,
  107 |     // antes de limpar cookies e navegar (evita race conditions)
  108 |     try {
  109 |         await page.evaluate(() => {
  110 |             window.localStorage.clear();
  111 |             window.sessionStorage.clear();
  112 |         });
  113 |     } catch {
  114 |         // Ignora erros se não estiver em uma página do domínio (ex: about:blank)
  115 |     }
  116 | 
  117 |     // 2. Desmonta a página atual para abortar requests pendentes antes de trocar a sessão.
> 118 |     await page.goto('about:blank', {waitUntil: 'commit'});
      |                ^ Error: page.goto: Target page, context or browser has been closed
  119 | 
  120 |     // 3. Limpa cookies de autenticação
  121 |     await page.context().clearCookies();
  122 | 
  123 |     // 4. Navega para a página de login
  124 |     await page.goto('/login');
  125 |     await page.waitForLoadState('domcontentloaded');
  126 | 
  127 |     // 5. Garante a limpeza após o carregamento para total isolamento
  128 |     await page.evaluate(() => {
  129 |         window.localStorage.clear();
  130 |         window.sessionStorage.clear();
  131 |     });
  132 |     await aguardarFormularioLoginPronto(page);
  133 | }
  134 | 
  135 | export async function login(page: Page, usuario: string, senha: string) {
  136 |     await limparSessaoNavegador(page);
  137 | 
  138 |     await autenticar(page, usuario, senha);
  139 |     await finalizarLoginNoPainel(page);
  140 | }
  141 | 
  142 | export async function loginComPerfil(page: Page, usuario: string, senha: string, perfilUnidade: string) {
  143 |     await limparSessaoNavegador(page);
  144 | 
  145 |     await autenticar(page, usuario, senha);
  146 |     await page.getByTestId('sel-login-perfil').selectOption({label: perfilUnidade});
  147 |     await clicarEntrarAposEstabilizar(page);
  148 |     await finalizarLoginNoPainel(page);
  149 | }
  150 | 
  151 | export async function reloginSemLimparSpa(page: Page, usuario: string, senha: string) {
  152 |     await fazerLogout(page);
  153 |     await autenticar(page, usuario, senha);
  154 |     await finalizarLoginNoPainel(page);
  155 | }
  156 | 
  157 | export async function reloginComPerfilSemLimparSpa(page: Page, usuario: string, senha: string, perfilUnidade: string) {
  158 |     await fazerLogout(page);
  159 |     await autenticar(page, usuario, senha);
  160 |     await page.getByTestId('sel-login-perfil').selectOption({label: perfilUnidade});
  161 |     await clicarEntrarAposEstabilizar(page);
  162 |     await finalizarLoginNoPainel(page);
  163 | }
  164 | 
  165 | /**
  166 |  * Encapsula o ciclo de login, execução de uma ação e logout.
  167 |  */
  168 | export async function executarComo(page: Page, usuario: Usuario, acao: (page: Page) => Promise<void>) {
  169 |     if (usuario.perfil) {
  170 |         await loginComPerfil(page, usuario.titulo, usuario.senha, usuario.perfil);
  171 |     } else {
  172 |         await login(page, usuario.titulo, usuario.senha);
  173 |     }
  174 | 
  175 |     try {
  176 |         await acao(page);
  177 |     } finally {
  178 |         await fazerLogout(page);
  179 |     }
  180 | }
  181 | 
```
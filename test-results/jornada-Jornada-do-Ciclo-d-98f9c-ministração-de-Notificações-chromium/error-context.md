# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: jornada.spec.ts >> Jornada do Ciclo de Vida Completo do SGC >> Fase 0: Administração de Notificações
- Location: e2e\jornada.spec.ts:18:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('sec-notificacoes-pendentes')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('sec-notificacoes-pendentes')

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - main [ref=e6]:
      - generic [ref=e10]:
        - heading "SGC" [level=1] [ref=e11]
        - paragraph [ref=e12]: Sistema de Gestão de Competências
        - generic [ref=e13]:
          - group [ref=e14]:
            - generic [ref=e15]:
              - generic [ref=e16]: 
              - text: Título eleitoral *
            - textbox "Título eleitoral" [active] [ref=e17]:
              - /placeholder: Digite seu título
          - group [ref=e18]:
            - generic [ref=e19]:
              - generic [ref=e20]: 
              - text: Senha *
            - group [ref=e21]:
              - textbox "Senha" [ref=e22]:
                - /placeholder: Digite sua senha
              - button "Mostrar senha" [ref=e23] [cursor=pointer]:
                - generic [ref=e24]: 
          - button "Entrar" [ref=e25] [cursor=pointer]:
            - generic [ref=e26]: 
            - text: Entrar
```

# Test source

```ts
  1   | /* eslint-disable playwright/expect-expect */
  2   | import {expect, Page, test} from '@playwright/test';
  3   | import * as AuthHelpers from './helpers/helpers-auth.js';
  4   | import * as ProcessoHelpers from './helpers/helpers-processos.js';
  5   | import * as AtividadeHelpers from './helpers/helpers-atividades.js';
  6   | import * as MapaHelpers from './helpers/helpers-mapas.js';
  7   | import * as AnaliseHelpers from './helpers/helpers-analise.js';
  8   | import {limparNotificacoes, verificarAppAlert, verificarToast} from './helpers/helpers-navegacao.js';
  9   | import {TEXTOS} from "../frontend/src/constants/textos.js";
  10  | 
  11  | test.describe.serial('Jornada do Ciclo de Vida Completo do SGC', () => {
  12  |     test.beforeAll(async ({request}) => {
  13  |         // Reset do banco de dados UMA VEZ para iniciar a jornada
  14  |         const response = await request.post('/e2e/reset-database');
  15  |         expect(response.ok()).toBeTruthy();
  16  |     });
  17  | 
  18  |     test('Fase 0: Administração de Notificações', async ({page}) => {
  19  |         await validarAcessoRestritoNotificacoes(page);
  20  |         await validarPainelNotificacoesAdmin(page);
  21  |     });
  22  | 
  23  |     test('Fase 1: Mapeamento Inicial - Cadastro de Atividades', async ({page}) => {
  24  |         await criarProcessoMapeamento(page);
  25  |         await preencherAtividadesChefe(page);
  26  |         await realizarAceiteGestor(page);
  27  |         await homologarMapeamentoAdmin(page);
  28  |     });
  29  | 
  30  |     test('Fase 2: Elaboração e Homologação do Mapa', async ({page}) => {
  31  |         await criarMapaAdmin(page);
  32  |         await validarMapaChefe(page);
  33  |         await aceitarMapaGestor(page);
  34  |         await homologarMapaEFinalizarAdmin(page);
  35  |     });
  36  | 
  37  |     test('Fase 3: Ciclo de Revisão e Manutenção', async ({page}) => {
  38  |         await criarProcessoRevisaoAdmin(page);
  39  |         await realizarRevisaoChefe(page);
  40  |         await realizarAceiteRevisaoGestor(page);
  41  |         await homologarRevisaoAdmin(page);
  42  |     });
  43  | 
  44  |     const timestamp = Date.now();
  45  |     const descricaoMapeamento = `Mapeamento Ciclo Completo ${timestamp}`;
  46  |     const descricaoRevisao = `Revisão Ciclo Completo ${timestamp}`;
  47  |     const siglaUnidade = 'ASSESSORIA_11'; // Unidade alvo
  48  | 
  49  |     const ADMIN = AuthHelpers.USUARIOS.ADMIN_1_PERFIL;
  50  |     const CHEFE = AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11;
  51  |     const GESTOR = AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1;
  52  | 
  53  |     const validarAcessoRestritoNotificacoes = async (page: Page) => {
  54  |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  55  |             await expect(page.getByTestId('nav-link-notificacoes')).toBeHidden();
  56  |             await page.goto('/administracao/notificacoes');
  57  |             await expect(page).toHaveURL(/\/painel/);
  58  |         });
  59  |         await expect(page).toHaveURL(/\/login/);
  60  |     };
  61  | 
  62  |     const validarPainelNotificacoesAdmin = async (page: Page) => {
  63  |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  64  |             await expect(page.getByTestId('nav-link-notificacoes')).toBeVisible();
  65  |             await page.getByTestId('nav-link-notificacoes').click();
  66  |             await expect(page).toHaveURL(/\/administracao\/notificacoes/);
  67  | 
  68  |             const secaoPendentes = page.getByTestId('sec-notificacoes-pendentes');
  69  |             const secaoConcluidas = page.getByTestId('sec-notificacoes-concluidas');
> 70  |             await expect(secaoPendentes).toBeVisible();
      |                                          ^ Error: expect(locator).toBeVisible() failed
  71  |             await expect(secaoConcluidas).toBeVisible();
  72  | 
  73  |             const tabelaPendentes = page.getByTestId('tbl-notificacoes-pendentes');
  74  |             await expect(tabelaPendentes).toBeVisible();
  75  |             await expect(tabelaPendentes).toContainText('SECAO_321');
  76  |             await expect(tabelaPendentes).toContainText('Mapeamento Secão 321');
  77  |             await expect(tabelaPendentes).toContainText('Falha definitiva');
  78  |             await expect(tabelaPendentes).toContainText(/Falha simulada no seed/i);
  79  |             const btnReenviarPendente = tabelaPendentes.locator('button[title="Tentar reenviar e-mail"]').first();
  80  |             await expect(btnReenviarPendente).toBeVisible();
  81  | 
  82  |             const tabelaConcluidas = page.getByTestId('tbl-notificacoes-concluidas');
  83  |             await expect(tabelaConcluidas).toBeVisible();
  84  |             await expect(tabelaConcluidas).toContainText('SECAO_311');
  85  |             await expect(tabelaConcluidas).toContainText('Mapeamento Secão 311');
  86  |             await expect(tabelaConcluidas).toContainText('Enviado');
  87  |             await expect(tabelaConcluidas.locator('button[title="Tentar reenviar e-mail"]')).toHaveCount(0);
  88  | 
  89  |             await btnReenviarPendente.click();
  90  |             await expect(page.getByTestId('txt-notificacoes-reenviar-confirmacao')).toContainText(/Confirma o reenvio/i);
  91  |             await page.getByTestId('btn-notificacoes-reenviar-confirmar').click();
  92  | 
  93  |             await verificarAppAlert(page, /recolocad[oa] na fila/i);
  94  |             await expect(tabelaPendentes).toContainText('Pendente');
  95  |             await expect(tabelaPendentes.locator('button[title="Tentar reenviar e-mail"]')).toHaveCount(0);
  96  |         });
  97  |         await expect(page).toHaveURL(/\/login/);
  98  |     };
  99  | 
  100 |     const criarProcessoMapeamento = async (page: Page) => {
  101 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  102 |             await ProcessoHelpers.criarProcesso(page, {
  103 |                 descricao: descricaoMapeamento,
  104 |                 tipo: 'MAPEAMENTO',
  105 |                 unidade: [siglaUnidade],
  106 |                 expandir: ['SECRETARIA_1'],
  107 |                 iniciar: true
  108 |             });
  109 |         });
  110 |         await expect(page).toHaveURL(/\/login/);
  111 |     };
  112 | 
  113 |     const preencherAtividadesChefe = async (page: Page) => {
  114 |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  115 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  116 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  117 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  118 |             await AtividadeHelpers.navegarParaAtividades(page);
  119 |             const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  120 |             await expect(btnDisponibilizar).toBeVisible();
  121 |             await btnDisponibilizar.click();
  122 |             await expect(page.getByText(TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO)).toBeVisible();
  123 |             await limparNotificacoes(page);
  124 | 
  125 |             await AtividadeHelpers.importarAtividadesVazia(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);
  126 |             await AtividadeHelpers.disponibilizarCadastro(page);
  127 | 
  128 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  129 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
  130 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  131 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  132 |         });
  133 |         await expect(page).toHaveURL(/\/login/);
  134 |     };
  135 | 
  136 |     const realizarAceiteGestor = async (page: Page) => {
  137 |         await AuthHelpers.executarComo(page, GESTOR, async () => {
  138 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
  139 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  140 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  141 |             await AtividadeHelpers.navegarParaAtividades(page);
  142 |             await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
  143 |             await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
  144 |             await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
  145 |             await AnaliseHelpers.aceitarCadastroMapeamento(page, 'Cadastro aceito pelo Gestor.');
  146 | 
  147 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
  148 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
  149 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  150 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  151 |         });
  152 |         await expect(page).toHaveURL(/\/login/);
  153 |     };
  154 | 
  155 |     const homologarMapeamentoAdmin = async (page: Page) => {
  156 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  157 |             await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
  158 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  159 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  160 |             await AtividadeHelpers.navegarParaAtividades(page);
  161 |             await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
  162 |             await expect(page.getByTestId('btn-acao-analisar-principal')).toBeVisible();
  163 |             await expect(page.getByTestId('btn-acao-analisar-principal')).toBeEnabled();
  164 |             await AnaliseHelpers.homologarCadastroMapeamento(page);
  165 | 
  166 |             await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  167 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
  168 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  169 |             await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
  170 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
```
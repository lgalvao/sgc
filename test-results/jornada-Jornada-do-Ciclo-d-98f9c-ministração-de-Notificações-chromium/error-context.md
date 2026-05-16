# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: jornada.spec.ts >> Jornada do Ciclo de Vida Completo do SGC >> Fase 0: Administração de Notificações
- Location: e2e/jornada.spec.ts:20:5

# Error details

```
Error: expect(locator).toContainText(expected) failed

Locator: getByTestId('tbl-notificacoes')
Expected substring: "Mapeamento Secão 321"
Received string:    "Destinatário Click to sort ascendingTipo Click to sort ascendingAssunto Click to sort ascendingSituação Click to sort ascendingQuando Click to sort ascending SECAO_321Mapa disponibilizadoMapa de competências disponibilizado - SECAO_321Falha Definitiva16/05/2026 19:05SECAO_311Cadastro homologadoCadastro de atividades homologadoEnviado15/05/2026 04:50"
Timeout: 5000ms

Call log:
  - Expect "toContainText" with timeout 5000ms
  - waiting for getByTestId('tbl-notificacoes')
    14 × locator resolved to <div data-v-1881cf85="" class="table-responsive" data-testid="tbl-notificacoes">…</div>
       - unexpected value "Destinatário Click to sort ascendingTipo Click to sort ascendingAssunto Click to sort ascendingSituação Click to sort ascendingQuando Click to sort ascending SECAO_321Mapa disponibilizadoMapa de competências disponibilizado - SECAO_321Falha Definitiva16/05/2026 19:05SECAO_311Cadastro homologadoCadastro de atividades homologadoEnviado15/05/2026 04:50"

```

```yaml
- table:
  - rowgroup:
    - row "Destinatário Click to sort ascending Tipo Click to sort ascending Assunto Click to sort ascending Situação Click to sort ascending Quando Click to sort ascending":
      - columnheader "Destinatário Click to sort ascending"
      - columnheader "Tipo Click to sort ascending"
      - columnheader "Assunto Click to sort ascending"
      - columnheader "Situação Click to sort ascending"
      - columnheader "Quando Click to sort ascending"
      - columnheader
  - rowgroup:
    - row "SECAO_321 Mapa disponibilizado Mapa de competências disponibilizado - SECAO_321 Falha Definitiva 16/05/2026 19:05":
      - cell "SECAO_321"
      - cell "Mapa disponibilizado"
      - cell "Mapa de competências disponibilizado - SECAO_321"
      - cell "Falha Definitiva"
      - cell "16/05/2026 19:05"
      - cell:
        - button "Detalhes"
        - button "Ver conteúdo do e-mail"
        - button "Tentar reenviar e-mail"
    - row "SECAO_311 Cadastro homologado Cadastro de atividades homologado Enviado 15/05/2026 04:50":
      - cell "SECAO_311"
      - cell "Cadastro homologado"
      - cell "Cadastro de atividades homologado"
      - cell "Enviado"
      - cell "15/05/2026 04:50"
      - cell:
        - button "Detalhes"
        - button "Ver conteúdo do e-mail"
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
  12  |     test.setTimeout(120_000);
  13  | 
  14  |     test.beforeAll(async ({request}) => {
  15  |         // Reset do banco de dados UMA VEZ para iniciar a jornada
  16  |         const response = await request.post('/e2e/reset-database');
  17  |         expect(response.ok()).toBeTruthy();
  18  |     });
  19  | 
  20  |     test('Fase 0: Administração de Notificações', async ({page}) => {
  21  |         await validarAcessoRestritoNotificacoes(page);
  22  |         await validarPainelNotificacoesAdmin(page);
  23  |     });
  24  | 
  25  |     test('Fase 1: Mapeamento Inicial - Cadastro de Atividades', async ({page}) => {
  26  |         await criarProcessoMapeamento(page);
  27  |         await preencherAtividadesChefe(page);
  28  |         await realizarAceiteGestor(page);
  29  |         await homologarMapeamentoAdmin(page);
  30  |     });
  31  | 
  32  |     test('Fase 2: Elaboração e Homologação do Mapa', async ({page}) => {
  33  |         await criarMapaAdmin(page);
  34  |         await validarMapaChefe(page);
  35  |         await aceitarMapaGestor(page);
  36  |         await homologarMapaEFinalizarAdmin(page);
  37  |     });
  38  | 
  39  |     test('Fase 3: Ciclo de Revisão e Manutenção', async ({page}) => {
  40  |         await criarProcessoRevisaoAdmin(page);
  41  |         await realizarRevisaoChefe(page);
  42  |         await realizarAceiteRevisaoGestor(page);
  43  |         await homologarRevisaoAdmin(page);
  44  |     });
  45  | 
  46  |     const timestamp = Date.now();
  47  |     const descricaoMapeamento = `Mapeamento Ciclo Completo ${timestamp}`;
  48  |     const descricaoRevisao = `Revisão Ciclo Completo ${timestamp}`;
  49  |     const siglaUnidade = 'ASSESSORIA_11'; // Unidade alvo
  50  | 
  51  |     const ADMIN = AuthHelpers.USUARIOS.ADMIN_1_PERFIL;
  52  |     const CHEFE = AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11;
  53  |     const GESTOR = AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1;
  54  | 
  55  |     const validarAcessoRestritoNotificacoes = async (page: Page) => {
  56  |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  57  |             await expect(page.getByTestId('nav-link-notificacoes')).toBeHidden();
  58  |             await page.goto('/administracao/notificacoes');
  59  |             await expect(page).toHaveURL(/\/painel/);
  60  |         });
  61  |         await expect(page).toHaveURL(/\/login/);
  62  | 
  63  |         // SERVIDOR também não tem acesso ao painel de notificações nem ao menu de Unidades
  64  |         const SERVIDOR = AuthHelpers.USUARIOS.SERVIDOR;
  65  |         await AuthHelpers.executarComo(page, SERVIDOR, async () => {
  66  |             await expect(page.getByTestId('nav-link-notificacoes')).toBeHidden();
  67  |             await expect(page.getByRole('link', {name: /Unidades/i})).toBeHidden();
  68  |             await expect(page.getByTestId('btn-painel-criar-processo')).toBeHidden();
  69  |             await page.goto('/administracao/notificacoes');
  70  |             await expect(page).toHaveURL(/\/painel/);
  71  |         });
  72  |         await expect(page).toHaveURL(/\/login/);
  73  |     };
  74  | 
  75  |     const validarPainelNotificacoesAdmin = async (page: Page) => {
  76  |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  77  |             await expect(page.getByTestId('nav-link-notificacoes')).toBeVisible();
  78  |             await page.getByTestId('nav-link-notificacoes').click();
  79  |             await expect(page).toHaveURL(/\/administracao\/notificacoes/);
  80  | 
  81  |             const secaoNotificacoes = page.getByTestId('sec-notificacoes');
  82  |             await expect(secaoNotificacoes).toBeVisible();
  83  | 
  84  |             const tabelaNotificacoes = page.getByTestId('tbl-notificacoes');
  85  |             await expect(tabelaNotificacoes).toBeVisible();
  86  |             await expect(tabelaNotificacoes).toContainText('SECAO_321');
> 87  |             await expect(tabelaNotificacoes).toContainText('Mapeamento Secão 321');
      |                                              ^ Error: expect(locator).toContainText(expected) failed
  88  |             await expect(tabelaNotificacoes).toContainText('Falha Definitiva');
  89  |             await expect(tabelaNotificacoes).toContainText('SECAO_311');
  90  |             await expect(tabelaNotificacoes).toContainText('Mapeamento Secão 311');
  91  |             await expect(tabelaNotificacoes).toContainText('Enviado');
  92  |             const btnDetalhesFalha = tabelaNotificacoes.locator('[data-testid^="btn-detalhes-"]').first();
  93  |             await expect(btnDetalhesFalha).toBeVisible();
  94  |             await btnDetalhesFalha.click();
  95  |             const modalDetalhes = page.getByTestId('modal-detalhes-notificacao');
  96  |             await expect(modalDetalhes).toBeVisible();
  97  |             await expect(modalDetalhes).toContainText(/Falha simulada no seed/i);
  98  |             await page.getByRole('button', {name: /Fechar/i}).click();
  99  |             await expect(modalDetalhes).toBeHidden();
  100 |             const btnReenviarPendente = tabelaNotificacoes.locator('[data-testid^="btn-notificacoes-reenviar-"]').first();
  101 |             await expect(btnReenviarPendente).toBeVisible();
  102 | 
  103 |             await btnReenviarPendente.click();
  104 |             await expect(page.getByTestId('txt-notificacoes-reenviar-confirmacao')).toContainText(/Confirma o reenvio/i);
  105 |             await page.getByTestId('btn-notificacoes-reenviar-confirmar').click();
  106 | 
  107 |             await verificarAppAlert(page, /recolocad[oa] na fila/i);
  108 |             await expect(tabelaNotificacoes).toContainText('Pendente');
  109 |             await expect(tabelaNotificacoes.locator('[data-testid^="btn-notificacoes-reenviar-"]')).toHaveCount(0);
  110 |         });
  111 |         await expect(page).toHaveURL(/\/login/);
  112 |     };
  113 | 
  114 |     const criarProcessoMapeamento = async (page: Page) => {
  115 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  116 |             await ProcessoHelpers.criarProcesso(page, {
  117 |                 descricao: descricaoMapeamento,
  118 |                 tipo: 'MAPEAMENTO',
  119 |                 unidade: [siglaUnidade],
  120 |                 expandir: ['SECRETARIA_1'],
  121 |                 iniciar: true
  122 |             });
  123 |         });
  124 |         await expect(page).toHaveURL(/\/login/);
  125 |     };
  126 | 
  127 |     const preencherAtividadesChefe = async (page: Page) => {
  128 |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  129 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  130 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  131 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  132 |             await AtividadeHelpers.navegarParaCadastro(page);
  133 |             const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  134 |             await expect(btnDisponibilizar).toBeVisible();
  135 |             await expect(btnDisponibilizar).toBeDisabled();
  136 | 
  137 |             // Importar atividade do Seed e adicionar um conhecimento extra antes de disponibilizar
  138 |             await AtividadeHelpers.importarAtividades(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);
  139 |             await AtividadeHelpers.adicionarConhecimento(page, 'Atividade 1', 'Conhecimento Adicional');
  140 |             await expect(page.getByText('Conhecimento Adicional')).toBeVisible();
  141 |             await AtividadeHelpers.disponibilizarCadastro(page);
  142 | 
  143 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  144 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
  145 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  146 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  147 |         });
  148 |         await expect(page).toHaveURL(/\/login/);
  149 |     };
  150 | 
  151 |     const realizarAceiteGestor = async (page: Page) => {
  152 |         // Primeiro: GESTOR devolve o cadastro para o CHEFE corrigir
  153 |         await AuthHelpers.executarComo(page, GESTOR, async () => {
  154 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
  155 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  156 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  157 |             await AtividadeHelpers.navegarParaCadastro(page);
  158 |             await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
  159 |                 rotuloPrincipal: /Registrar aceite/i,
  160 |                 principalHabilitado: true,
  161 |                 devolverHabilitado: true
  162 |             });
  163 | 
  164 |             // Abrir histórico de análise antes de devolver
  165 |             const modalHistorico = await AnaliseHelpers.abrirHistoricoAnalise(page);
  166 |             await expect(modalHistorico).toBeVisible();
  167 |             await AnaliseHelpers.fecharHistoricoAnalise(page);
  168 | 
  169 |             // Devolver com observação
  170 |             await AnaliseHelpers.devolverCadastroMapeamento(page, 'Detalhes insuficientes. Por favor, revise as atividades.');
  171 |         });
  172 |         await expect(page).toHaveURL(/\/login/);
  173 | 
  174 |         // CHEFE recebe a devolução, abre o histórico e re-disponibiliza
  175 |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  176 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  177 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);
  178 |             await AtividadeHelpers.navegarParaCadastro(page);
  179 | 
  180 |             // Verificar no histórico que há uma devolução registrada
  181 |             const modalHistorico = await AnaliseHelpers.abrirHistoricoAnalise(page);
  182 |             await expect(modalHistorico).toBeVisible();
  183 |             await expect(modalHistorico.getByTestId('cell-resultado-0')).toHaveText(/Devolu/i);
  184 |             await AnaliseHelpers.fecharHistoricoAnalise(page);
  185 | 
  186 |             await AtividadeHelpers.disponibilizarCadastro(page);
  187 |         });
```
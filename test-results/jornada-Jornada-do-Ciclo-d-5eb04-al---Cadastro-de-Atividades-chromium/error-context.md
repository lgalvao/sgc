# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: jornada.spec.ts >> Jornada do Ciclo de Vida Completo do SGC >> Fase 1: Mapeamento Inicial - Cadastro de Atividades
- Location: e2e/jornada.spec.ts:23:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('btn-cad-atividades-disponibilizar')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('btn-cad-atividades-disponibilizar')

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
  - text: 
```

# Test source

```ts
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
  68  |             const secaoNotificacoes = page.getByTestId('sec-notificacoes');
  69  |             await expect(secaoNotificacoes).toBeVisible();
  70  | 
  71  |             const tabelaNotificacoes = page.getByTestId('tbl-notificacoes');
  72  |             await expect(tabelaNotificacoes).toBeVisible();
  73  |             await expect(tabelaNotificacoes).toContainText('SECAO_321');
  74  |             await expect(tabelaNotificacoes).toContainText('Mapeamento Secão 321');
  75  |             await expect(tabelaNotificacoes).toContainText('Falha definitiva');
  76  |             await expect(tabelaNotificacoes).toContainText('SECAO_311');
  77  |             await expect(tabelaNotificacoes).toContainText('Mapeamento Secão 311');
  78  |             await expect(tabelaNotificacoes).toContainText('Enviado');
  79  |             const btnDetalhesFalha = tabelaNotificacoes.locator('[data-testid^="btn-detalhes-"]').first();
  80  |             await expect(btnDetalhesFalha).toBeVisible();
  81  |             await btnDetalhesFalha.click();
  82  |             const modalDetalhes = page.getByTestId('modal-detalhes-notificacao');
  83  |             await expect(modalDetalhes).toBeVisible();
  84  |             await expect(modalDetalhes).toContainText(/Falha simulada no seed/i);
  85  |             await page.getByRole('button', {name: /Fechar/i}).click();
  86  |             await expect(modalDetalhes).toBeHidden();
  87  |             const btnReenviarPendente = tabelaNotificacoes.locator('[data-testid^="btn-notificacoes-reenviar-"]').first();
  88  |             await expect(btnReenviarPendente).toBeVisible();
  89  | 
  90  |             await btnReenviarPendente.click();
  91  |             await expect(page.getByTestId('txt-notificacoes-reenviar-confirmacao')).toContainText(/Confirma o reenvio/i);
  92  |             await page.getByTestId('btn-notificacoes-reenviar-confirmar').click();
  93  | 
  94  |             await verificarAppAlert(page, /recolocad[oa] na fila/i);
  95  |             await expect(tabelaNotificacoes).toContainText('Pendente');
  96  |             await expect(tabelaNotificacoes.locator('[data-testid^="btn-notificacoes-reenviar-"]')).toHaveCount(0);
  97  |         });
  98  |         await expect(page).toHaveURL(/\/login/);
  99  |     };
  100 | 
  101 |     const criarProcessoMapeamento = async (page: Page) => {
  102 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  103 |             await ProcessoHelpers.criarProcesso(page, {
  104 |                 descricao: descricaoMapeamento,
  105 |                 tipo: 'MAPEAMENTO',
  106 |                 unidade: [siglaUnidade],
  107 |                 expandir: ['SECRETARIA_1'],
  108 |                 iniciar: true
  109 |             });
  110 |         });
  111 |         await expect(page).toHaveURL(/\/login/);
  112 |     };
  113 | 
  114 |     const preencherAtividadesChefe = async (page: Page) => {
  115 |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  116 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  117 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  118 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  119 |             await AtividadeHelpers.navegarParaCadastro(page);
  120 |             const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
> 121 |             await expect(btnDisponibilizar).toBeVisible();
      |                                             ^ Error: expect(locator).toBeVisible() failed
  122 |             await expect(btnDisponibilizar).toBeDisabled();
  123 | 
  124 |             await AtividadeHelpers.importarAtividadesVazia(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);
  125 |             await AtividadeHelpers.disponibilizarCadastro(page);
  126 | 
  127 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  128 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
  129 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  130 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  131 |         });
  132 |         await expect(page).toHaveURL(/\/login/);
  133 |     };
  134 | 
  135 |     const realizarAceiteGestor = async (page: Page) => {
  136 |         await AuthHelpers.executarComo(page, GESTOR, async () => {
  137 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
  138 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  139 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  140 |             await AtividadeHelpers.navegarParaCadastro(page);
  141 |             await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
  142 |                 rotuloPrincipal: /Registrar aceite/i,
  143 |                 principalHabilitado: true,
  144 |                 devolverHabilitado: true
  145 |             });
  146 |             await AnaliseHelpers.aceitarCadastroMapeamento(page, 'Cadastro aceito pelo Gestor.');
  147 | 
  148 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
  149 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);
  150 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  151 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  152 |         });
  153 |         await expect(page).toHaveURL(/\/login/);
  154 |     };
  155 | 
  156 |     const homologarMapeamentoAdmin = async (page: Page) => {
  157 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  158 |             await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
  159 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  160 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  161 |             await AtividadeHelpers.navegarParaCadastro(page);
  162 |             await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
  163 |                 rotuloPrincipal: /Homologar/i,
  164 |                 principalHabilitado: true,
  165 |                 devolverHabilitado: true
  166 |             });
  167 |             await AnaliseHelpers.homologarCadastroMapeamento(page);
  168 | 
  169 |             await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  170 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
  171 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  172 |             await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
  173 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  174 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  175 |         });
  176 |         await expect(page).toHaveURL(/\/login/);
  177 |     };
  178 | 
  179 |     const criarMapaAdmin = async (page: Page) => {
  180 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  181 |             await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
  182 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  183 |             await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
  184 |             await MapaHelpers.navegarParaMapa(page);
  185 |             await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();
  186 |             await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  187 |             await (await MapaHelpers.abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar')).click();
  188 |             await expect(page.getByText(TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS)).toBeVisible();
  189 |             await limparNotificacoes(page);
  190 | 
  191 |             await MapaHelpers.criarCompetencia(page, 'Competência Técnica Básica', ['Atividade 1']);
  192 |             await MapaHelpers.disponibilizarMapa(page);
  193 | 
  194 |             await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
  195 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
  196 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  197 |             await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
  198 |         });
  199 |         await expect(page).toHaveURL(/\/login/);
  200 |     };
  201 | 
  202 |     const validarMapaChefe = async (page: Page) => {
  203 |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  204 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  205 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  206 |             await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
  207 |             await MapaHelpers.navegarParaMapa(page);
  208 |             await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  209 |             await MapaHelpers.abrirValidacaoMapa(page);
  210 |             await page.getByTestId('btn-validar-mapa-confirmar').click();
  211 |             await page.waitForURL(/\/painel$/);
  212 | 
  213 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
  214 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
  215 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  216 |             await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
  217 |         });
  218 |         await expect(page).toHaveURL(/\/login/);
  219 |     };
  220 | 
  221 |     const aceitarMapaGestor = async (page: Page) => {
```
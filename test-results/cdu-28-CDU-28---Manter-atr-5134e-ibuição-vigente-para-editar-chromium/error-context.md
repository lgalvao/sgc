# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-28.spec.ts >> CDU-28 - Manter atribuição temporária >> Cenario 6: ADMIN acessa atribuição vigente para editar
- Location: e2e/cdu-28.spec.ts:214:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId(/opcao-usuario-232323/).filter({ hasText: 'Bon Jovi' }).first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId(/opcao-usuario-232323/).filter({ hasText: 'Bon Jovi' }).first()

```

```yaml
- heading "SGC" [level=1]
- link "Pular para o conteúdo principal":
  - /url: "#main-content"
- navigation:
  - link "SGC":
    - /url: /painel
  - list:
    - listitem:
      - link "Painel":
        - /url: /painel
    - listitem:
      - link "Unidades":
        - /url: /unidades
    - listitem:
      - link "Relatórios":
        - /url: /relatorios
    - listitem:
      - link "Histórico":
        - /url: /historico
  - list:
    - listitem:
      - link "ADMIN":
        - /url: "#"
    - listitem "Notificações":
      - link "Notificações":
        - /url: /administracao/notificacoes
    - listitem "Configurações":
      - link "Configurações":
        - /url: /configuracoes
    - listitem "Administradores do sistema":
      - link "Administradores":
        - /url: /administradores
    - listitem:
      - button "Ações Especiais"
    - listitem "Ativar modo escuro":
      - link "Ativar modo escuro":
        - /url: "#"
    - listitem "Sair":
      - link "Sair":
        - /url: "#"
- button "Voltar"
- navigation "breadcrumb":
  - list:
    - listitem:
      - link "Início":
        - /url: /painel
    - listitem:
      - text: ›
      - link "ASSESSORIA_11":
        - /url: /unidade/3
    - listitem: › Atribuição temporária
- main:
  - heading "Atribuição temporária" [level=2]
  - paragraph: ASSESSORIA_11
  - button " Voltar"
  - alert:
    - paragraph: Atribuição removida
    - button "Close"
  - group:
    - text: Usuário
    - textbox "Usuário":
      - /placeholder: Selecione um usuário
      - text: "32323"
    - text: Nenhum usuário encontrado. Pesquise por nome ou título eleitoral.
  - group:
    - text: Data de início
    - group:
      - textbox "Data de início"
      - button "Abrir calendário"
  - group:
    - text: Data de término
    - group:
      - textbox "Data de término"
      - button "Abrir calendário"
  - group:
    - text: Justificativa
    - toolbar:
      - button "Negrito"
      - button "Itálico"
      - button "Lista"
      - button "Lista numerada"
    - textbox:
      - paragraph
    - text: 0/500
  - button "Cancelar"
  - button "Criar"
- contentinfo: Versão 1.0.4 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  14  | ];
  15  | const SIGLAS_SUBARVORE_SECRETARIA_2 = [
  16  |     'ASSESSORIA_21',
  17  |     'ASSESSORIA_22',
  18  |     'COORD_21',
  19  |     'COORD_22'
  20  | ];
  21  | const SIGLAS_SUBARVORE_SECRETARIA_3 = [
  22  |     'ASSESSORIA_31',
  23  |     'ASSESSORIA_32',
  24  |     'COORD_31',
  25  |     'COORD_32'
  26  | ];
  27  | 
  28  | function formatarDataInput(data: Date) {
  29  |     return data.toISOString().slice(0, 10);
  30  | }
  31  | 
  32  | function obterPeriodoVigente() {
  33  |     const inicio = new Date();
  34  |     const termino = new Date();
  35  |     termino.setDate(termino.getDate() + 30);
  36  |     return {
  37  |         dataInicio: formatarDataInput(inicio),
  38  |         dataTermino: formatarDataInput(termino),
  39  |     };
  40  | }
  41  | 
  42  | async function validarRamoUnidade(
  43  |     page: import('@playwright/test').Page,
  44  |     siglaRamo: string,
  45  |     siglasFilhas: string[]
  46  | ) {
  47  |     const tabela = page.getByTestId('tbl-tree');
  48  |     await expect(tabela.getByText(new RegExp(String.raw`^${siglaRamo}\s+-\s+`)).first()).toBeVisible();
  49  |     for (const siglaFilha of siglasFilhas) {
  50  |         await expect(tabela.getByText(new RegExp(String.raw`^${siglaFilha}\s+-\s+`)).first()).toBeVisible();
  51  |     }
  52  | }
  53  | 
  54  | async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {
  55  |     const tabela = page.getByTestId('tbl-tree');
  56  |     await expect(tabela.getByText(/^SECRETARIA_1\s+-\s+/).first()).toBeVisible();
  57  |     await expect(tabela.getByText(/^SECRETARIA_2\s+-\s+/).first()).toBeVisible();
  58  |     const textoUnidade = tabela.getByText(new RegExp(String.raw`^${SIGLA_UNIDADE}\s+-\s+`)).first();
  59  |     await expect(textoUnidade).toBeVisible();
  60  |     await textoUnidade.click();
  61  | }
  62  | 
  63  | async function garantirSemAtribuicaoVigente(page: import('@playwright/test').Page) {
  64  |     await acessarUnidadeAlvo(page);
  65  |     await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
  66  |     await expect(page.getByTestId('unidade-view__titulo')).toHaveText(SIGLA_UNIDADE);
  67  | 
  68  |     const textoBotaoAtribuicao = page.getByTestId('unidade-view__btn-atribuicao-texto');
  69  |     if (await textoBotaoAtribuicao.isVisible().catch(() => false) && await textoBotaoAtribuicao.textContent() === 'Editar atribuição') {
  70  |         await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
  71  |         await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
  72  |         await expect(page.getByTestId('btn-remover-atribuicao')).toBeVisible();
  73  | 
  74  |         await page.getByTestId('btn-remover-atribuicao').click();
  75  |         const modal = page.getByRole('dialog');
  76  |         await expect(modal).toBeVisible();
  77  |         await modal.getByRole('button', {name: 'Remover'}).click();
  78  |         await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO_REMOCAO).first()).toBeVisible();
  79  | 
  80  |         await page.getByTestId('btn-cancelar-atribuicao').click();
  81  |         await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
  82  |     }
  83  | 
  84  |     await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Criar atribuição');
  85  | }
  86  | 
  87  | async function abrirTelaCriacaoAtribuicao(page: import('@playwright/test').Page) {
  88  |     await garantirSemAtribuicaoVigente(page);
  89  |     await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
  90  |     await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
  91  | }
  92  | 
  93  | async function criarAtribuicaoVigente(page: import('@playwright/test').Page, justificativa = 'Cobertura de férias') {
  94  |     const {dataInicio, dataTermino} = obterPeriodoVigente();
  95  | 
  96  |     await abrirTelaCriacaoAtribuicao(page);
  97  |     await selecionarUsuarioAlvo(page);
  98  |     await page.getByTestId('input-data-inicio').fill(dataInicio);
  99  |     await page.getByTestId('input-data-termino').fill(dataTermino);
  100 |     await page.getByTestId('textarea-justificativa').fill(justificativa);
  101 |     await page.getByTestId('cad-atribuicao__btn-salvar-atribuicao').click();
  102 |     await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO).first()).toBeVisible();
  103 | }
  104 | 
  105 | async function selecionarUsuarioAlvo(page: import('@playwright/test').Page) {
  106 |     const inputBusca = page.getByTestId('input-busca-usuario');
  107 |     await inputBusca.click();
  108 |     await inputBusca.pressSequentially(TITULO_USUARIO_ALVO, {delay: 100});
  109 | 
  110 |     const listaResultados = page.getByTestId('lista-usuarios-pesquisa');
  111 |     await expect(listaResultados).toBeVisible();
  112 | 
  113 |     const opcaoUsuario = page.getByTestId(new RegExp(`opcao-usuario-${TITULO_USUARIO_ALVO}`)).filter({hasText: NOME_USUARIO_ALVO}).first();
> 114 |     await expect(opcaoUsuario).toBeVisible();
      |                                ^ Error: expect(locator).toBeVisible() failed
  115 |     await opcaoUsuario.click();
  116 |     await expect(listaResultados).toBeHidden();
  117 | }
  118 | 
  119 | test.describe.serial('CDU-28 - Manter atribuição temporária', () => {
  120 | 
  121 |     test.beforeEach(async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
  122 |         await page.getByRole('link', {name: /Unidades/i}).click();
  123 |         await expect(page).toHaveURL(/\/unidades/);
  124 |         await expect(page.getByRole('heading', {name: TEXTOS.unidades.TITULO})).toBeVisible();
  125 |         await expect(page.getByTestId('btn-unidades-expandir-todas')).toBeVisible();
  126 |         await page.getByTestId('btn-unidades-expandir-todas').click();
  127 |         await expect(page.getByTestId('tbl-tree')).toBeVisible();
  128 |     });
  129 | 
  130 |     test('Cenario 1: ADMIN navega pela árvore e acessa detalhes da unidade', async ({
  131 |                                                                                         _resetAutomatico,
  132 |                                                                                         _autenticadoComoAdmin,
  133 |                                                                                         page
  134 |                                                                                     }) => {
  135 |         await validarRamoUnidade(page, 'SECRETARIA_1', SIGLAS_SUBARVORE_SECRETARIA_1);
  136 |         await validarRamoUnidade(page, 'SECRETARIA_2', SIGLAS_SUBARVORE_SECRETARIA_2);
  137 |         await validarRamoUnidade(page, 'SECRETARIA_3', SIGLAS_SUBARVORE_SECRETARIA_3);
  138 | 
  139 |         const tabela = page.getByTestId('tbl-tree');
  140 |         await expect(tabela.getByText(/^SECAO_111\s+-\s+/).first()).toBeVisible();
  141 |         await expect(tabela.getByText(/^SECAO_112\s+-\s+/).first()).toBeVisible();
  142 |         await expect(tabela.getByText(/^SECAO_113\s+-\s+/).first()).toBeVisible();
  143 | 
  144 |         await garantirSemAtribuicaoVigente(page);
  145 | 
  146 |         await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Criar atribuição');
  147 |     });
  148 | 
  149 |     test('Cenario 2: tela de atribuição expõe os campos e ações exigidos pelo requisito', async ({
  150 |                                                                                                      _resetAutomatico,
  151 |                                                                                                      _autenticadoComoAdmin,
  152 |                                                                                                      page
  153 |                                                                                                  }) => {
  154 |         await abrirTelaCriacaoAtribuicao(page);
  155 | 
  156 |         await expect(page.getByTestId('atribuicao-view__titulo')).toHaveText('Atribuição temporária');
  157 |         await expect(page.getByTestId('atribuicao-view__sigla')).toHaveText(SIGLA_UNIDADE);
  158 |         await expect(page.getByText(TEXTOS.atribuicaoTemporaria.AJUDA_PESQUISA_USUARIO)).toBeVisible();
  159 | 
  160 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_USUARIO)).toBeVisible();
  161 |         await expect(page.getByTestId('input-busca-usuario')).toHaveAttribute('placeholder', TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO);
  162 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_INICIO)).toBeVisible();
  163 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_TERMINO)).toBeVisible();
  164 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_JUSTIFICATIVA)).toBeVisible();
  165 | 
  166 |         await expect(page.getByTestId('btn-cancelar-atribuicao')).toBeVisible();
  167 |         await expect(page.getByTestId('cad-atribuicao__btn-salvar-atribuicao')).toBeVisible();
  168 |         await expect(page.getByTestId('cad-atribuicao__btn-salvar-atribuicao')).toHaveText('Criar');
  169 |         await expect(page.getByTestId('btn-remover-atribuicao')).toBeHidden();
  170 |     });
  171 | 
  172 |     test('Cenario 3: o formulário exibe erros de validação ao tentar criar incompleto', async ({
  173 |                                                                                                    _resetAutomatico,
  174 |                                                                                                    _autenticadoComoAdmin,
  175 |                                                                                                    page
  176 |                                                                                                }) => {
  177 |         await abrirTelaCriacaoAtribuicao(page);
  178 | 
  179 |         const btnCriar = page.getByTestId('cad-atribuicao__btn-salvar-atribuicao');
  180 |         await btnCriar.click();
  181 | 
  182 |         await expect(page.getByText(TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO)).toBeVisible();
  183 |         await expect(page.getByText('Informe a data de início.')).toBeVisible();
  184 |         await expect(page.getByText('Informe a data de término.')).toBeVisible();
  185 |         await expect(page.getByText('Informe a justificativa.')).toBeVisible();
  186 |     });
  187 | 
  188 |     test('Cenario 4: ADMIN cancela criação e retorna para detalhes da unidade', async ({
  189 |                                                                                            _resetAutomatico,
  190 |                                                                                            _autenticadoComoAdmin,
  191 |                                                                                            page
  192 |                                                                                        }) => {
  193 |         await abrirTelaCriacaoAtribuicao(page);
  194 | 
  195 |         await page.getByTestId('btn-cancelar-atribuicao').click();
  196 |         await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
  197 |         await expect(page.getByTestId('unidade-view__titulo')).toHaveText(SIGLA_UNIDADE);
  198 |     });
  199 | 
  200 |     test('Cenario 5: ADMIN cria atribuição e usuário destino recebe perfil temporário', async ({
  201 |                                                                                                    _resetAutomatico,
  202 |                                                                                                    _autenticadoComoAdmin,
  203 |                                                                                                    page
  204 |                                                                                                }) => {
  205 |         await criarAtribuicaoVigente(page);
  206 |         await page.getByTestId('btn-cancelar-atribuicao').click();
  207 |         await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Editar atribuição');
  208 |         await expect(page.getByText(/Atrib\. temporária/)).toBeVisible();
  209 | 
  210 |         await loginComPerfil(page, TITULO_USUARIO_ALVO, 'senha', PERFIL_TEMPORARIO);
  211 |         await expect(page.locator('.user-info-text')).toContainText(PERFIL_TEMPORARIO);
  212 |     });
  213 | 
  214 |     test('Cenario 6: ADMIN acessa atribuição vigente para editar', async ({
```
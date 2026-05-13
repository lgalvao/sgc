# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-28.spec.ts >> CDU-28 - Manter atribuição temporária >> Cenario 6: ADMIN acessa atribuição vigente para editar
- Location: e2e\cdu-28.spec.ts:213:5

# Error details

```
Error: expect(locator).toHaveText(expected) failed

Locator:  getByTestId('unidade-view__btn-atribuicao-texto')
Expected: "Criar atribuição"
Received: "Editar atribuição"
Timeout:  5000ms

Call log:
  - Expect "toHaveText" with timeout 5000ms
  - waiting for getByTestId('unidade-view__btn-atribuicao-texto')
    9 × locator resolved to <span data-testid="unidade-view__btn-atribuicao-texto">Editar atribuição</span>
      - unexpected value "Editar atribuição"

```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - generic [ref=e5]:
      - generic [ref=e6]:
        - navigation [ref=e7]:
          - generic [ref=e8]:
            - link "SGC" [ref=e9] [cursor=pointer]:
              - /url: /painel
            - generic [ref=e10]:
              - list [ref=e11]:
                - listitem [ref=e12]:
                  - link "Painel" [ref=e13] [cursor=pointer]:
                    - /url: /painel
                    - generic [ref=e14]: 
                    - text: Painel
                - listitem [ref=e15]:
                  - link "Unidades" [ref=e16] [cursor=pointer]:
                    - /url: /unidades
                    - generic [ref=e17]: 
                    - text: Unidades
                - listitem [ref=e18]:
                  - link "Relatórios" [ref=e19] [cursor=pointer]:
                    - /url: /relatorios
                    - generic [ref=e20]: 
                    - text: Relatórios
                - listitem [ref=e21]:
                  - link "Histórico" [ref=e22] [cursor=pointer]:
                    - /url: /historico
                    - generic [ref=e23]: 
                    - text: Histórico
              - list [ref=e24]:
                - listitem [ref=e25]:
                  - link "ADMIN" [ref=e26] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e27]:
                      - generic [ref=e28]: 
                      - generic [ref=e29]: ADMIN
                - listitem "Notificações" [ref=e30]:
                  - link "Notificações" [ref=e31] [cursor=pointer]:
                    - /url: /administracao/notificacoes
                    - generic [ref=e32]: Notificações
                    - generic [ref=e33]: 
                - listitem "Configurações" [ref=e34]:
                  - link "Configurações" [ref=e35] [cursor=pointer]:
                    - /url: /configuracoes
                    - generic [ref=e36]: Configurações
                    - generic [ref=e37]: 
                - listitem "Administradores do sistema" [ref=e38]:
                  - link "Administradores" [ref=e39] [cursor=pointer]:
                    - /url: /administradores
                    - generic [ref=e40]: Administradores
                    - generic [ref=e41]: 
                - listitem [ref=e42]:
                  - generic [ref=e43]:
                    - button "Ações Especiais" [ref=e44] [cursor=pointer]:
                      - generic [ref=e45]: Ações Especiais
                      - generic [ref=e46]: 
                    - text:  
                - listitem "Ativar modo escuro" [ref=e47]:
                  - link "Ativar modo escuro" [ref=e48] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e49]: Ativar modo escuro
                    - generic [ref=e50]: 
                - listitem "Sair" [ref=e51]:
                  - link "Sair" [ref=e52] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e53]: Sair
                    - generic [ref=e54]: 
        - generic [ref=e57]:
          - button "Voltar" [ref=e58] [cursor=pointer]:
            - generic [ref=e59]: 
          - navigation "breadcrumb" [ref=e60]:
            - list [ref=e61]:
              - listitem [ref=e62]:
                - link "Início" [ref=e63] [cursor=pointer]:
                  - /url: /painel
                  - generic [ref=e64]: 
                  - generic [ref=e65]: Início
              - listitem [ref=e66]:
                - text: ›
                - generic [ref=e67]: ASSESSORIA_11
      - main [ref=e68]:
        - generic [ref=e70]:
          - generic [ref=e71]:
            - generic [ref=e72]:
              - heading "ASSESSORIA_11" [level=2] [ref=e73]
              - paragraph [ref=e74]: Assessoria 11
            - button "Editar atribuição" [ref=e76] [cursor=pointer]
          - generic [ref=e78]:
            - generic [ref=e79]:
              - paragraph [ref=e80]:
                - strong [ref=e81]: "Titular:"
                - generic [ref=e82]: David Bowie
              - paragraph [ref=e83]:
                - generic [ref=e84]:
                  - generic [ref=e85]: 
                  - text: "2003"
                - generic [ref=e86]:
                  - generic [ref=e87]: 
                  - link "Enviar e-mail para david.bowie@tre-pe.jus.br" [ref=e88] [cursor=pointer]:
                    - /url: mailto:david.bowie@tre-pe.jus.br
                    - text: david.bowie@tre-pe.jus.br
            - generic [ref=e89]:
              - paragraph [ref=e90]:
                - strong [ref=e91]: "Responsável:"
                - generic [ref=e92]: Bon Jovi
              - paragraph [ref=e93]: Atrib. temporária (até 12/06/2026)
              - paragraph [ref=e94]:
                - generic [ref=e95]:
                  - generic [ref=e96]: 
                  - text: "2023"
                - generic [ref=e97]:
                  - generic [ref=e98]: 
                  - link "Enviar e-mail para bon.jovi@tre-pe.jus.br" [ref=e99] [cursor=pointer]:
                    - /url: mailto:bon.jovi@tre-pe.jus.br
                    - text: bon.jovi@tre-pe.jus.br
      - contentinfo [ref=e100]:
        - generic [ref=e101]:
          - generic [ref=e102]: Versão 1.0.4
          - generic [ref=e103]: © SESEL/COSIS/TRE-PE
  - button "Enviar feedback" [ref=e104] [cursor=pointer]:
    - generic [ref=e105]: 
  - text:    
```

# Test source

```ts
  1   | import {expect, test} from './fixtures/complete-fixtures.js';
  2   | import {loginComPerfil} from './helpers/helpers-auth.js';
  3   | import {TEXTOS} from '../frontend/src/constants/textos.js';
  4   | 
  5   | test.describe.serial('CDU-28 - Manter atribuição temporária', () => {
  6   |     const SIGLA_UNIDADE = 'ASSESSORIA_11';
  7   |     const TITULO_USUARIO_ALVO = '232323';
  8   |     const NOME_USUARIO_ALVO = 'Bon Jovi';
  9   |     const PERFIL_TEMPORARIO = 'CHEFE - ASSESSORIA_11';
  10  |     const SIGLAS_SUBARVORE_SECRETARIA_1 = [
  11  |         'ASSESSORIA_11',
  12  |         'ASSESSORIA_12',
  13  |         'COORD_11',
  14  |         'COORD_12'
  15  |     ];
  16  |     const SIGLAS_SUBARVORE_SECRETARIA_2 = [
  17  |         'ASSESSORIA_21',
  18  |         'ASSESSORIA_22',
  19  |         'COORD_21',
  20  |         'COORD_22'
  21  |     ];
  22  |     const SIGLAS_SUBARVORE_SECRETARIA_3 = [
  23  |         'ASSESSORIA_31',
  24  |         'ASSESSORIA_32',
  25  |         'COORD_31',
  26  |         'COORD_32'
  27  |     ];
  28  | 
  29  |     function formatarDataInput(data: Date) {
  30  |         return data.toISOString().slice(0, 10);
  31  |     }
  32  | 
  33  |     function obterPeriodoVigente() {
  34  |         const inicio = new Date();
  35  |         const termino = new Date();
  36  |         termino.setDate(termino.getDate() + 30);
  37  |         return {
  38  |             dataInicio: formatarDataInput(inicio),
  39  |             dataTermino: formatarDataInput(termino),
  40  |         };
  41  |     }
  42  | 
  43  |     async function validarRamoUnidade(
  44  |         page: import('@playwright/test').Page,
  45  |         siglaRamo: string,
  46  |         siglasFilhas: string[]
  47  |     ) {
  48  |         const tabela = page.getByTestId('tbl-tree');
  49  |         await expect(tabela.getByText(new RegExp(String.raw`^${siglaRamo}\s+-\s+`)).first()).toBeVisible();
  50  |         for (const siglaFilha of siglasFilhas) {
  51  |             await expect(tabela.getByText(new RegExp(String.raw`^${siglaFilha}\s+-\s+`)).first()).toBeVisible();
  52  |         }
  53  |     }
  54  | 
  55  |     async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {
  56  |         const tabela = page.getByTestId('tbl-tree');
  57  |         await expect(tabela.getByText(/^SECRETARIA_1\s+-\s+/).first()).toBeVisible();
  58  |         await expect(tabela.getByText(/^SECRETARIA_2\s+-\s+/).first()).toBeVisible();
  59  |         const textoUnidade = tabela.getByText(new RegExp(String.raw`^${SIGLA_UNIDADE}\s+-\s+`)).first();
  60  |         await expect(textoUnidade).toBeVisible();
  61  |         await textoUnidade.click();
  62  |     }
  63  | 
  64  |     async function garantirSemAtribuicaoVigente(page: import('@playwright/test').Page) {
  65  |         await acessarUnidadeAlvo(page);
  66  |         await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
  67  |         await expect(page.getByTestId('unidade-view__titulo')).toHaveText(SIGLA_UNIDADE);
  68  | 
  69  |         const textoBotaoAtribuicao = page.getByTestId('unidade-view__btn-atribuicao-texto');
  70  |         if (await textoBotaoAtribuicao.isVisible().catch(() => false) && await textoBotaoAtribuicao.textContent() === 'Editar atribuição') {
  71  |             await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
  72  |             await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
  73  |             await expect(page.getByTestId('btn-remover-atribuicao')).toBeVisible();
  74  | 
  75  |             await page.getByTestId('btn-remover-atribuicao').click();
  76  |             const modal = page.getByRole('dialog');
  77  |             await expect(modal).toBeVisible();
  78  |             await modal.getByRole('button', {name: 'Remover'}).click();
  79  |             await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO_REMOCAO).first()).toBeVisible();
  80  | 
  81  |             await page.getByTestId('btn-cancelar-atribuicao').click();
  82  |             await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
  83  |         }
  84  | 
> 85  |         await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Criar atribuição');
      |                                                                              ^ Error: expect(locator).toHaveText(expected) failed
  86  |     }
  87  | 
  88  |     async function abrirTelaCriacaoAtribuicao(page: import('@playwright/test').Page) {
  89  |         await garantirSemAtribuicaoVigente(page);
  90  |         await page.getByTestId('unidade-view__btn-criar-atribuicao').click();
  91  |         await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
  92  |     }
  93  | 
  94  |     async function criarAtribuicaoVigente(page: import('@playwright/test').Page, justificativa = 'Cobertura de férias') {
  95  |         const {dataInicio, dataTermino} = obterPeriodoVigente();
  96  | 
  97  |         await abrirTelaCriacaoAtribuicao(page);
  98  |         await selecionarUsuarioAlvo(page);
  99  |         await page.getByTestId('input-data-inicio').fill(dataInicio);
  100 |         await page.getByTestId('input-data-termino').fill(dataTermino);
  101 |         await page.getByTestId('textarea-justificativa').fill(justificativa);
  102 |         await page.getByTestId('cad-atribuicao__btn-salvar-atribuicao').click();
  103 |         await expect(page.getByText(TEXTOS.atribuicaoTemporaria.SUCESSO).first()).toBeVisible();
  104 |     }
  105 | 
  106 |     async function selecionarUsuarioAlvo(page: import('@playwright/test').Page) {
  107 |         const inputBusca = page.getByTestId('input-busca-usuario');
  108 |         await inputBusca.click();
  109 |         await inputBusca.pressSequentially(TITULO_USUARIO_ALVO, {delay: 100});
  110 | 
  111 |         const listaResultados = page.getByTestId('lista-usuarios-pesquisa');
  112 |         await expect(listaResultados).toBeVisible();
  113 | 
  114 |         const opcaoUsuario = page.getByTestId(new RegExp(`opcao-usuario-${TITULO_USUARIO_ALVO}`)).filter({hasText: NOME_USUARIO_ALVO}).first();
  115 |         await expect(opcaoUsuario).toBeVisible();
  116 |         await opcaoUsuario.click();
  117 |         await expect(listaResultados).toBeHidden();
  118 |     }
  119 | 
  120 |     test.beforeEach(async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {
  121 |         await page.getByRole('link', {name: /Unidades/i}).click();
  122 |         await expect(page).toHaveURL(/\/unidades/);
  123 |         await expect(page.getByRole('heading', {name: TEXTOS.unidades.TITULO})).toBeVisible();
  124 |         await expect(page.getByTestId('btn-unidades-expandir-todas')).toBeVisible();
  125 |         await page.getByTestId('btn-unidades-expandir-todas').click();
  126 |         await expect(page.getByTestId('tbl-tree')).toBeVisible();
  127 |     });
  128 | 
  129 |     test('Cenario 1: ADMIN navega pela árvore e acessa detalhes da unidade', async ({
  130 |                                                                                         _resetAutomatico,
  131 |                                                                                         _autenticadoComoAdmin,
  132 |                                                                                         page
  133 |                                                                                     }) => {
  134 |         await validarRamoUnidade(page, 'SECRETARIA_1', SIGLAS_SUBARVORE_SECRETARIA_1);
  135 |         await validarRamoUnidade(page, 'SECRETARIA_2', SIGLAS_SUBARVORE_SECRETARIA_2);
  136 |         await validarRamoUnidade(page, 'SECRETARIA_3', SIGLAS_SUBARVORE_SECRETARIA_3);
  137 | 
  138 |         const tabela = page.getByTestId('tbl-tree');
  139 |         await expect(tabela.getByText(/^SECAO_111\s+-\s+/).first()).toBeVisible();
  140 |         await expect(tabela.getByText(/^SECAO_112\s+-\s+/).first()).toBeVisible();
  141 |         await expect(tabela.getByText(/^SECAO_113\s+-\s+/).first()).toBeVisible();
  142 | 
  143 |         await garantirSemAtribuicaoVigente(page);
  144 | 
  145 |         await expect(page.getByTestId('unidade-view__btn-atribuicao-texto')).toHaveText('Criar atribuição');
  146 |     });
  147 | 
  148 |     test('Cenario 2: tela de atribuição expõe os campos e ações exigidos pelo requisito', async ({
  149 |                                                                                                      _resetAutomatico,
  150 |                                                                                                      _autenticadoComoAdmin,
  151 |                                                                                                      page
  152 |                                                                                                  }) => {
  153 |         await abrirTelaCriacaoAtribuicao(page);
  154 | 
  155 |         await expect(page.getByTestId('atribuicao-view__titulo')).toHaveText('Atribuição temporária');
  156 |         await expect(page.getByTestId('atribuicao-view__sigla')).toHaveText(SIGLA_UNIDADE);
  157 |         await expect(page.getByText(TEXTOS.atribuicaoTemporaria.AJUDA_PESQUISA_USUARIO)).toBeVisible();
  158 | 
  159 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_USUARIO)).toBeVisible();
  160 |         await expect(page.getByTestId('input-busca-usuario')).toHaveAttribute('placeholder', TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO);
  161 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_INICIO)).toBeVisible();
  162 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_DATA_TERMINO)).toBeVisible();
  163 |         await expect(page.getByLabel(TEXTOS.atribuicaoTemporaria.LABEL_JUSTIFICATIVA)).toBeVisible();
  164 | 
  165 |         await expect(page.getByTestId('btn-cancelar-atribuicao')).toBeVisible();
  166 |         await expect(page.getByTestId('cad-atribuicao__btn-salvar-atribuicao')).toBeVisible();
  167 |         await expect(page.getByTestId('cad-atribuicao__btn-salvar-atribuicao')).toHaveText('Criar');
  168 |         await expect(page.getByTestId('btn-remover-atribuicao')).toBeHidden();
  169 |     });
  170 | 
  171 |     test('Cenario 3: o formulário exibe erros de validação ao tentar criar incompleto', async ({
  172 |                                                                                                    _resetAutomatico,
  173 |                                                                                                    _autenticadoComoAdmin,
  174 |                                                                                                    page
  175 |                                                                                                }) => {
  176 |         await abrirTelaCriacaoAtribuicao(page);
  177 | 
  178 |         const btnCriar = page.getByTestId('cad-atribuicao__btn-salvar-atribuicao');
  179 |         await btnCriar.click();
  180 | 
  181 |         await expect(page.getByText(TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO)).toBeVisible();
  182 |         await expect(page.getByText('Informe a data de início.')).toBeVisible();
  183 |         await expect(page.getByText('Informe a data de término.')).toBeVisible();
  184 |         await expect(page.getByText('Informe a justificativa.')).toBeVisible();
  185 |     });
```
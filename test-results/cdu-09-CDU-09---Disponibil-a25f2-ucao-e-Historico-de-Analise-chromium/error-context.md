# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-09.spec.ts >> CDU-09 - Disponibilizar cadastro de atividades e conhecimentos >> Cenario 3: Devolucao e Historico de Analise
- Location: e2e/cdu-09.spec.ts:102:5

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('mdl-historico-analise').getByTestId('btn-modal-fechar')
    - locator resolved to <button type="button" data-v-f7b0b57f="" data-testid="btn-modal-fechar" class="btn btn-md btn-link text-decoration-none text-secondary fw-medium btn-cancelar-link"> Fechar </button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not stable
    - retrying click action
    - waiting 20ms
    - waiting for element to be visible, enabled and stable
    - element is not stable
  - retrying click action
    - waiting 100ms
    - waiting for element to be visible, enabled and stable
  - element was detached from the DOM, retrying
    - locator resolved to <button type="button" data-v-f7b0b57f="" data-testid="btn-modal-fechar" class="btn btn-md btn-link text-decoration-none text-secondary fw-medium btn-cancelar-link"> Fechar </button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
      - waiting 100ms
    33 × waiting for element to be visible, enabled and stable
       - element is not visible
     - retrying click action
       - waiting 500ms

```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - generic [ref=e5]:
      - navigation [ref=e6]:
        - generic [ref=e7]:
          - link "SGC" [ref=e8] [cursor=pointer]:
            - /url: /painel
          - generic [ref=e9]:
            - list [ref=e10]:
              - listitem [ref=e11]:
                - link "Painel" [ref=e12] [cursor=pointer]:
                  - /url: /painel
                  - generic [ref=e13]: 
                  - text: Painel
              - listitem [ref=e14]:
                - link "Minha unidade" [ref=e15] [cursor=pointer]:
                  - /url: /unidade/18
                  - generic [ref=e16]: 
                  - text: Minha unidade
              - listitem [ref=e17]:
                - link "Histórico" [ref=e18] [cursor=pointer]:
                  - /url: /historico
                  - generic [ref=e19]: 
                  - text: Histórico
            - list [ref=e20]:
              - listitem [ref=e21]:
                - link "CHEFE - SECAO_221" [ref=e22] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e23]:
                    - generic [ref=e24]: 
                    - generic [ref=e25]: CHEFE - SECAO_221
              - listitem "Sair" [ref=e26]:
                - link "Sair" [ref=e27] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e28]: Sair
                  - generic [ref=e29]: 
      - generic [ref=e32]:
        - button "Voltar" [ref=e33] [cursor=pointer]:
          - generic [ref=e34]: 
        - navigation "breadcrumb" [ref=e35]:
          - list [ref=e36]:
            - listitem [ref=e37]:
              - link "Início" [ref=e38] [cursor=pointer]:
                - /url: /painel
                - generic [ref=e39]: 
                - generic [ref=e40]: Início
            - listitem [ref=e41]:
              - text: ›
              - link "SECAO_221" [ref=e42] [cursor=pointer]:
                - /url: /processo/400/SECAO_221
            - listitem [ref=e43]:
              - text: ›
              - generic [ref=e44]: Atividades e conhecimentos
      - main [ref=e45]:
        - generic [ref=e46]:
          - generic [ref=e47]:
            - generic [ref=e48]:
              - heading "Atividades e conhecimentos" [level=2] [ref=e49]
              - paragraph [ref=e50]: SECAO_221
            - generic [ref=e51]:
              - generic [ref=e52]:
                - button "Histórico de análise" [ref=e53] [cursor=pointer]:
                  - generic [ref=e54]: 
                  - text: Histórico de análise
                - button "Disponibilizar" [ref=e55] [cursor=pointer]:
                  - generic [ref=e56]: 
                  - text: Disponibilizar
              - button "Importar" [ref=e58] [cursor=pointer]:
                - generic [ref=e59]: 
                - text: Importar
          - generic [ref=e60]:
            - textbox "Nova atividade" [ref=e62]
            - button "Adicionar atividade" [ref=e64] [cursor=pointer]:
              - generic [ref=e65]: 
          - generic [ref=e68]:
            - 'heading "Editar Remover atividade: Atividade validada 1777732929039 Atividade validada 1777732929039" [level=4] [ref=e69]':
              - generic [ref=e70]:
                - generic [ref=e71]:
                  - button "Editar" [ref=e72] [cursor=pointer]:
                    - generic [ref=e73]: 
                  - 'button "Remover atividade: Atividade validada 1777732929039" [ref=e74] [cursor=pointer]':
                    - generic [ref=e75]: 
                - strong [ref=e77]: Atividade validada 1777732929039
            - generic [ref=e78]:
              - generic [ref=e80]:
                - textbox "Novo conhecimento" [ref=e82]
                - button "Adicionar conhecimento" [ref=e84] [cursor=pointer]:
                  - generic [ref=e85]: 
              - generic [ref=e87]:
                - generic [ref=e88]:
                  - button "Editar" [ref=e89] [cursor=pointer]:
                    - generic [ref=e90]: 
                  - 'button "Remover conhecimento: Conhecimento valido" [ref=e91] [cursor=pointer]':
                    - generic [ref=e92]: 
                - generic [ref=e93]: Conhecimento valido
          - generic [ref=e96]:
            - 'heading "Editar Remover atividade: Atividade incompleta 1777732927820 Atividade incompleta 1777732927820" [level=4] [ref=e97]':
              - generic [ref=e98]:
                - generic [ref=e99]:
                  - button "Editar" [ref=e100] [cursor=pointer]:
                    - generic [ref=e101]: 
                  - 'button "Remover atividade: Atividade incompleta 1777732927820" [ref=e102] [cursor=pointer]':
                    - generic [ref=e103]: 
                - strong [ref=e105]: Atividade incompleta 1777732927820
            - generic [ref=e106]:
              - generic [ref=e108]:
                - textbox "Novo conhecimento" [ref=e110]
                - button "Adicionar conhecimento" [ref=e112] [cursor=pointer]:
                  - generic [ref=e113]: 
              - generic [ref=e115]:
                - generic [ref=e116]:
                  - button "Editar" [ref=e117] [cursor=pointer]:
                    - generic [ref=e118]: 
                  - 'button "Remover conhecimento: Conhecimento corretivo" [ref=e119] [cursor=pointer]':
                    - generic [ref=e120]: 
                - generic [ref=e121]: Conhecimento corretivo
      - contentinfo [ref=e122]:
        - generic [ref=e123]:
          - generic [ref=e124]: Versão 1.0.0
          - generic [ref=e125]: © SESEL/COSIS/TRE-PE
  - text:  
```

# Test source

```ts
  3   | import {TEXTOS} from '../../frontend/src/constants/textos.js';
  4   | 
  5   | /**
  6   |  * Acessa subprocesso como GESTOR (via lista de unidades)
  7   |  */
  8   | export async function acessarSubprocessoGestor(page: Page, descricaoProcesso: string, siglaUnidade: string) {
  9   |     await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  10  | 
  11  |     const row = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso});
  12  |     await expect(row).toBeVisible();
  13  |     await row.click();
  14  | 
  15  |     await navegarParaSubprocesso(page, siglaUnidade);
  16  |     await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  17  | }
  18  | 
  19  | /**
  20  |  * Acessa subprocesso como CHEFE (vai direto ao subprocesso)
  21  |  */
  22  | export async function acessarSubprocessoChefeDireto(page: Page, descricaoProcesso: string, siglaUnidade: string) {
  23  |     await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  24  | 
  25  |     const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricaoProcesso)});
  26  |     await expect(linhaProcesso).toBeVisible();
  27  |     await linhaProcesso.click();
  28  | 
  29  |     await navegarParaSubprocesso(page, siglaUnidade);
  30  |     await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  31  | }
  32  | 
  33  | /**
  34  |  * Acessa subprocesso como ADMIN (via lista de unidades)
  35  |  */
  36  | export async function acessarSubprocessoAdmin(page: Page, descricaoProcesso: string, siglaUnidade: string) {
  37  |     await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  38  | 
  39  |     await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();
  40  |     await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
  41  | 
  42  |     await navegarParaSubprocesso(page, siglaUnidade);
  43  |     await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  44  | }
  45  | 
  46  | /**
  47  |  * Abre modal de histórico de análise na tela unificada de atividades
  48  |  */
  49  | export async function abrirHistoricoAnalise(page: Page) {
  50  |     const itemHistorico = page.getByTestId('btn-cad-atividades-historico');
  51  |     await expect(itemHistorico).toBeVisible();
  52  |     await itemHistorico.click();
  53  | 
  54  |     const modal = page.getByTestId('mdl-historico-analise');
  55  |     await expect(modal).toBeVisible();
  56  |     return modal;
  57  | }
  58  | 
  59  | export async function verificarAcoesAnaliseCadastro(page: Page, options: {
  60  |     rotuloPrincipal: string | RegExp;
  61  |     principalHabilitado: boolean;
  62  |     devolverHabilitado: boolean;
  63  | }) {
  64  |     const botaoHistorico = page.getByTestId('btn-cad-atividades-historico');
  65  |     const dropdown = page.getByTestId('btn-cadastro-acoes');
  66  |     const usaDropdown = await dropdown.count() > 0;
  67  | 
  68  |     const botaoDevolver = usaDropdown
  69  |         ? page.getByTestId('btn-cadastro-acao-devolver')
  70  |         : page.getByTestId('btn-acao-devolver');
  71  |     const botaoPrincipal = usaDropdown
  72  |         ? page.getByTestId('btn-cadastro-acao-principal')
  73  |         : page.getByTestId('btn-acao-analisar-principal');
  74  | 
  75  |     await expect(botaoHistorico).toBeVisible();
  76  |     if (usaDropdown) {
  77  |         await dropdown.click();
  78  |     }
  79  |     await expect(botaoDevolver).toBeVisible();
  80  |     await expect(botaoPrincipal).toBeVisible();
  81  |     await expect(botaoPrincipal).toHaveText(options.rotuloPrincipal);
  82  | 
  83  |     if (options.devolverHabilitado) {
  84  |         await expect(botaoDevolver).toBeEnabled();
  85  |     } else {
  86  |         await expect(botaoDevolver).toBeDisabled();
  87  |     }
  88  | 
  89  |     if (options.principalHabilitado) {
  90  |         await expect(botaoPrincipal).toBeEnabled();
  91  |     } else {
  92  |         await expect(botaoPrincipal).toBeDisabled();
  93  |     }
  94  | }
  95  | 
  96  | /**
  97  |  * Fecha modal de histórico de análise
  98  |  */
  99  | export async function fecharHistoricoAnalise(page: Page) {
  100 |     const modal = page.getByTestId('mdl-historico-analise');
  101 |     const btnFechar = modal.getByTestId('btn-modal-fechar');
  102 |     await expect(btnFechar).toBeVisible();
> 103 |     await btnFechar.click();
      |                     ^ Error: locator.click: Target page, context or browser has been closed
  104 |     await expect(modal).toBeHidden();
  105 | }
  106 | 
  107 | async function abrirAcaoCadastro(page: Page, testIdDireto: string, testIdMenu: string) {
  108 |     const dropdown = page.getByTestId('btn-cadastro-acoes');
  109 |     if (await dropdown.count() > 0) {
  110 |         await dropdown.click();
  111 |         const acaoMenu = page.getByTestId(testIdMenu);
  112 |         await expect(acaoMenu).toBeVisible();
  113 |         return acaoMenu;
  114 |     }
  115 | 
  116 |     const acaoDireta = page.getByTestId(testIdDireto);
  117 |     await expect(acaoDireta).toBeVisible();
  118 |     return acaoDireta;
  119 | }
  120 | 
  121 | export async function abrirAcaoCadastroDevolver(page: Page) {
  122 |     return abrirAcaoCadastro(page, 'btn-acao-devolver', 'btn-cadastro-acao-devolver');
  123 | }
  124 | 
  125 | export async function abrirAcaoCadastroPrincipal(page: Page) {
  126 |     return abrirAcaoCadastro(page, 'btn-acao-analisar-principal', 'btn-cadastro-acao-principal');
  127 | }
  128 | 
  129 | // Funções de Devolução
  130 | 
  131 | /**
  132 |  * Função genérica para devolução de cadastro/revisão
  133 |  */
  134 | async function realizarDevolucao(page: Page, observacao: string = '') {
  135 |     await limparNotificacoes(page);
  136 |     const btnDevolver = await abrirAcaoCadastroDevolver(page);
  137 |     await expect(btnDevolver).toBeEnabled();
  138 |     await btnDevolver.click();
  139 |     const modal = page.locator('.modal.show');
  140 |     await expect(modal).toBeVisible();
  141 | 
  142 |     if (observacao) {
  143 |         await modal.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
  144 |     }
  145 | 
  146 |     await modal.getByTestId('btn-devolucao-cadastro-confirmar').click();
  147 |     await verificarPaginaPainel(page);
  148 | }
  149 | 
  150 | /**
  151 |  * Devolve cadastro de mapeamento para ajustes (CDU-13)
  152 |  */
  153 | export async function devolverCadastroMapeamento(page: Page, observacao: string = '') {
  154 |     await realizarDevolucao(page, observacao);
  155 | }
  156 | 
  157 | /**
  158 |  * Devolve revisão para ajustes (CDU-14)
  159 |  */
  160 | export async function devolverRevisao(page: Page, observacao: string = '') {
  161 |     await realizarDevolucao(page, observacao);
  162 | }
  163 | 
  164 | /**
  165 |  * Cancela devolução de cadastro
  166 |  */
  167 | export async function cancelarDevolucao(page: Page) {
  168 |     const btnDevolver = await abrirAcaoCadastroDevolver(page);
  169 |     await expect(btnDevolver).toBeEnabled();
  170 |     await btnDevolver.click();
  171 | 
  172 |     // Verificar modal de devolução
  173 |     const modal = page.locator('.modal.show');
  174 |     await expect(modal).toBeVisible();
  175 | 
  176 |     await modal.getByTestId('btn-modal-confirmacao-cancelar').click();
  177 | 
  178 |     // Verificar que modal fechou
  179 |     await expect(page.getByRole('dialog')).toBeHidden();
  180 | }
  181 | 
  182 | // Funções de Aceite (GESTOR)
  183 | 
  184 | /**
  185 |  * Função genérica para aceite de cadastro/revisão (GESTOR)
  186 |  */
  187 | async function realizarAceite(page: Page, observacao: string = '') {
  188 |     await limparNotificacoes(page);
  189 |     const btnAceitar = await abrirAcaoCadastroPrincipal(page);
  190 |     await expect(btnAceitar).toBeEnabled();
  191 |     await btnAceitar.click();
  192 |     const modal = page.locator('.modal.show');
  193 |     await expect(modal).toBeVisible();
  194 |     await expect(modal.getByText(/Confirma o aceite/i)).toBeVisible();
  195 | 
  196 |     const obsToSend = observacao || 'Aceite sem ressalvas';
  197 |     await modal.getByTestId('inp-aceite-cadastro-obs').fill(obsToSend);
  198 | 
  199 |     await modal.getByTestId('btn-aceite-cadastro-confirmar').click();
  200 |     await verificarPaginaPainel(page);
  201 | }
  202 | 
  203 | /**
```
# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-13.spec.ts >> CDU-13 - Analisar cadastro de atividades e conhecimentos >> Cenarios CDU-13: Hierarquia aceita e ADMIN homologa
- Location: e2e/cdu-13.spec.ts:93:5

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-cadastro-acao-principal')
    - locator resolved to <button role="menu" type="button" class="dropdown-item" data-testid="btn-cadastro-acao-principal">Registrar aceite</button>
  - attempting click action
    - waiting for element to be visible, enabled and stable
  - element was detached from the DOM, retrying
    - waiting for" http://localhost:5173/processo/400/SECAO_211/cadastro?codSubprocesso=400" navigation to finish...
    - navigated to "http://localhost:5173/processo/400/SECAO_211/cadastro?codSubprocesso=400"
    - locator resolved to <button role="menu" type="button" class="dropdown-item" data-testid="btn-cadastro-acao-principal">Registrar aceite</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
      - waiting 100ms
    13 × waiting for element to be visible, enabled and stable
       - element is not visible
     - retrying click action
       - waiting 500ms
    2 × waiting for" http://localhost:5173/processo/400/SECAO_211/cadastro?codSubprocesso=400" navigation to finish...
      - navigated to "http://localhost:5173/processo/400/SECAO_211/cadastro?codSubprocesso=400"
    - waiting for element to be visible, enabled and stable
  - element was detached from the DOM, retrying
    - locator resolved to <button role="menu" type="button" class="dropdown-item" data-testid="btn-cadastro-acao-principal">Registrar aceite</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
      - waiting 100ms
    23 × waiting for element to be visible, enabled and stable
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
                  - /url: /unidade/14
                  - generic [ref=e16]: 
                  - text: Minha unidade
              - listitem [ref=e17]:
                - link "Relatórios" [ref=e18] [cursor=pointer]:
                  - /url: /relatorios
                  - generic [ref=e19]: 
                  - text: Relatórios
              - listitem [ref=e20]:
                - link "Histórico" [ref=e21] [cursor=pointer]:
                  - /url: /historico
                  - generic [ref=e22]: 
                  - text: Histórico
            - list [ref=e23]:
              - listitem [ref=e24]:
                - link "GESTOR - COORD_21" [ref=e25] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e26]:
                    - generic [ref=e27]: 
                    - generic [ref=e28]: GESTOR - COORD_21
              - listitem "Sair" [ref=e29]:
                - link "Sair" [ref=e30] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e31]: Sair
                  - generic [ref=e32]: 
      - generic [ref=e35]:
        - button "Voltar" [ref=e36] [cursor=pointer]:
          - generic [ref=e37]: 
        - navigation "breadcrumb" [ref=e38]:
          - list [ref=e39]:
            - listitem [ref=e40]:
              - link "Início" [ref=e41] [cursor=pointer]:
                - /url: /painel
                - generic [ref=e42]: 
                - generic [ref=e43]: Início
            - listitem [ref=e44]:
              - text: ›
              - link "Detalhes do processo" [ref=e45] [cursor=pointer]:
                - /url: /processo/400
            - listitem [ref=e46]:
              - text: ›
              - link "SECAO_211" [ref=e47] [cursor=pointer]:
                - /url: /processo/400/SECAO_211
            - listitem [ref=e48]:
              - text: ›
              - generic [ref=e49]: Atividades e conhecimentos
      - main [ref=e50]:
        - generic [ref=e51]:
          - generic [ref=e52]:
            - generic [ref=e53]:
              - heading "Atividades e conhecimentos" [level=2] [ref=e54]
              - paragraph [ref=e55]: SECAO_211
            - generic [ref=e57]:
              - button "Histórico de análise" [ref=e58] [cursor=pointer]:
                - generic [ref=e59]: 
                - text: Histórico de análise
              - button "Ações" [ref=e61] [cursor=pointer]
          - generic [ref=e64]:
            - heading "Atividade fixture - 400" [level=4] [ref=e65]:
              - strong [ref=e68]: Atividade fixture - 400
            - generic [ref=e72]: Conhecimento fixture - 400
      - contentinfo [ref=e73]:
        - generic [ref=e74]:
          - generic [ref=e75]: Versão 1.0.0
          - generic [ref=e76]: © SESEL/COSIS/TRE-PE
  - text: 
```

# Test source

```ts
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
  103 |     await btnFechar.click();
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
> 191 |     await btnAceitar.click();
      |                      ^ Error: locator.click: Target page, context or browser has been closed
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
  204 |  * Aceita cadastro de mapeamento (GESTOR - CDU-13)
  205 |  */
  206 | export async function aceitarCadastroMapeamento(page: Page, observacao: string = '') {
  207 |     await realizarAceite(page, observacao);
  208 | }
  209 | 
  210 | /**
  211 |  * Aceita revisão (GESTOR - CDU-14)
  212 |  */
  213 | export async function aceitarRevisao(page: Page, observacao: string = '') {
  214 |     await realizarAceite(page, observacao);
  215 | }
  216 | 
  217 | /**
  218 |  * Homologa cadastro (ADMIN) - Mapeamento
  219 |  */
  220 | export async function homologarCadastroMapeamento(page: Page, observacao: string = 'Homologado sem ressalvas') {
  221 |     const btnHomologar = await abrirAcaoCadastroPrincipal(page);
  222 |     await expect(btnHomologar).toBeEnabled();
  223 |     await btnHomologar.click();
  224 | 
  225 |     // Modal: "Homologação do cadastro"
  226 |     await expect(page.getByRole('dialog')).toBeVisible();
  227 |     await expect(page.getByText(TEXTOS.atividades.MODAL_HOMOLOGAR_TEXTO)).toBeVisible();
  228 | 
  229 |     await page.getByTestId('inp-aceite-cadastro-obs').fill(observacao);
  230 | 
  231 |     await page.getByTestId('btn-aceite-cadastro-confirmar').click();
  232 |     
  233 |     // Aguarda o redirecionamento para a tela do subprocesso
  234 |     await expect(page).toHaveURL(/\/processo\/\d+\/(\w+)(?:\?.*)?$/);
  235 | }
  236 | export {fazerLogout, verificarPaginaPainel} from './helpers-navegacao.js';
  237 | 
```
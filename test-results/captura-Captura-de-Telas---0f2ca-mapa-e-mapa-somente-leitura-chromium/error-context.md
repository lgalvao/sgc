# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 12 - Modais de Gestão do Mapa >> Captura devolução do mapa e mapa somente leitura
- Location: e2e/captura.spec.ts:1599:9

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-mapa-acao-devolver')
    - locator resolved to <button disabled role="menu" type="button" class="dropdown-item disabled" data-testid="btn-mapa-acao-devolver">Devolver </button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
      - waiting 100ms
    33 × waiting for element to be visible, enabled and stable
       - element is not enabled
     - retrying click action
       - waiting 500ms

```

# Test source

```ts
  92  |     if (novasAtividades) {
  93  |         for (const atividade of novasAtividades) {
  94  |             await modal.getByLabel(atividade, {exact: true}).click();
  95  |         }
  96  |     }
  97  | 
  98  |     await page.getByTestId('btn-criar-competencia-salvar').click();
  99  |     await expect(modal).toBeHidden();
  100 | 
  101 |     await expect(page.getByText(novaDescricao)).toBeVisible();
  102 | }
  103 | 
  104 | export async function removerAtividadeAssociada(page: Page, descricaoCompetencia: string, descricaoAtividade: string) {
  105 |     const card = page.getByTestId('cad-mapa__card-competencia')
  106 |         .filter({has: page.getByText(descricaoCompetencia, {exact: true})});
  107 |     await expect(card).toBeVisible();
  108 | 
  109 |     const atividadeAssociada = card.locator('.atividade-associada-card-item')
  110 |         .filter({hasText: descricaoAtividade})
  111 |         .first();
  112 |     await expect(atividadeAssociada).toBeVisible();
  113 | 
  114 |     const botaoRemover = atividadeAssociada.getByTestId('btn-remover-atividade-associada');
  115 |     await expect(botaoRemover).toBeVisible();
  116 |     await botaoRemover.click({force: true});
  117 | 
  118 |     await expect(atividadeAssociada).toBeHidden();
  119 | }
  120 | 
  121 | /**
  122 |  * Exclui competência confirmando a ação no modal
  123 |  */
  124 | export async function excluirCompetenciaConfirmando(page: Page, descricao: string) {
  125 |     const card = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(descricao, {exact: true})});
  126 |     await card.hover();
  127 |     await card.getByTestId('btn-excluir-competencia').click();
  128 | 
  129 |     const modal = page.getByTestId('mdl-excluir-competencia');
  130 |     await expect(modal).toBeVisible();
  131 |     await expect(modal).toContainText(descricao);
  132 | 
  133 |     const btnConfirmar = modal.getByTestId('btn-confirmar-exclusao-competencia');
  134 |     await btnConfirmar.waitFor({state: 'visible'});
  135 |     await btnConfirmar.scrollIntoViewIfNeeded();
  136 |     await btnConfirmar.click();
  137 |     await expect(modal).toBeHidden();
  138 |     await expect(page.getByText(descricao, {exact: true})).toBeHidden();
  139 | }
  140 | 
  141 | /**
  142 |  * Exclui competência cancelando a ação no modal
  143 |  */
  144 | export async function excluirCompetenciaCancelando(page: Page, descricao: string) {
  145 |     const card = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(descricao, {exact: true})});
  146 |     await card.hover();
  147 |     await card.getByTestId('btn-excluir-competencia').click();
  148 | 
  149 |     const modal = page.getByTestId('mdl-excluir-competencia');
  150 |     await expect(modal).toBeVisible();
  151 |     await expect(modal).toContainText(descricao);
  152 | 
  153 |     await modal.getByTestId('btn-modal-confirmacao-cancelar').click();
  154 |     await expect(modal).toBeHidden();
  155 |     await expect(page.getByText(descricao, {exact: true})).toBeVisible();
  156 | }
  157 | 
  158 | export async function verificarCompetenciaNoMapa(page: Page, descricao: string, atividades: string[]) {
  159 |     const card = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(descricao, {exact: true})});
  160 |     await expect(card).toBeVisible();
  161 | 
  162 |     for (const atividade of atividades) {
  163 |         await expect(card.getByText(atividade)).toBeVisible();
  164 |     }
  165 | }
  166 | 
  167 | export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
  168 |     await expect(page.getByTestId('txt-badge-situacao')).toHaveText(new RegExp(situacao, 'i'));
  169 | }
  170 | 
  171 | export async function abrirAcaoMapa(page: Page, testIdAcao: string) {
  172 |     const btnAcoes = page.getByTestId('btn-mapa-acoes');
  173 |     await expect(btnAcoes).toBeVisible();
  174 |     await btnAcoes.click();
  175 |     const acao = page.getByTestId(testIdAcao);
  176 |     await expect(acao).toBeVisible();
  177 |     return acao;
  178 | }
  179 | 
  180 | export async function abrirSugestoesMapa(page: Page) {
  181 |     const acao = await abrirAcaoMapa(page, 'btn-mapa-acao-sugestoes');
  182 |     await acao.click();
  183 | }
  184 | 
  185 | export async function abrirValidacaoMapa(page: Page) {
  186 |     const acao = await abrirAcaoMapa(page, 'btn-mapa-acao-validar');
  187 |     await acao.click();
  188 | }
  189 | 
  190 | export async function abrirDevolucaoMapa(page: Page) {
  191 |     const acao = await abrirAcaoMapa(page, 'btn-mapa-acao-devolver');
> 192 |     await acao.click();
      |                ^ Error: locator.click: Target page, context or browser has been closed
  193 | }
  194 | 
  195 | export async function abrirAcaoPrincipalMapa(page: Page) {
  196 |     const acao = await abrirAcaoMapa(page, 'btn-mapa-acao-homologar-aceite');
  197 |     await acao.click();
  198 | }
  199 | 
  200 | export async function disponibilizarMapa(page: Page, dataLimite?: string) {
  201 |     const data = dataLimite || calcularDataLimite(30);
  202 | 
  203 |     const btnDisponibilizar = await abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar');
  204 |     await btnDisponibilizar.click();
  205 |     const modal = page.getByTestId('mdl-disponibilizar-mapa');
  206 |     await expect(modal).toBeVisible();
  207 | 
  208 |     await page.getByTestId('inp-disponibilizar-mapa-data').fill(data);
  209 |     await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
  210 | 
  211 |     await expect(modal).toBeHidden();
  212 |     await verificarPaginaPainel(page);
  213 |     await verificarToast(page, TEXTOS.sucesso.MAPA_DISPONIBILIZADO);
  214 | }
  215 | 
  216 | /**
  217 |  * Realiza o aceite ou homologação do mapa a partir da tela de mapa do subprocesso.
  218 |  */
  219 | export async function aceitarOuHomologarMapa(page: Page, observacao: string) {
  220 |     await navegarParaMapa(page);
  221 |     await abrirAcaoPrincipalMapa(page);
  222 |     const modal = page.getByTestId('body-aceite-mapa');
  223 |     await expect(modal).toBeVisible();
  224 |     await page.getByTestId('inp-aceite-mapa-observacao').fill(observacao);
  225 |     await page.getByTestId('btn-aceite-mapa-confirmar').click();
  226 |     await expect(modal).toBeHidden();
  227 |     await verificarPaginaPainel(page);
  228 |     await verificarToast(page, new RegExp(`${TEXTOS.sucesso.ACEITE_REGISTRADO}|${TEXTOS.mapa.SUCESSO_HOMOLOGACAO}`, 'i'));
  229 | }
  230 | 
```
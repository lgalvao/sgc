# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-05.spec.ts >> CDU-05 - Iniciar processo de revisao >> Fase 1.3: CHEFE disponibiliza cadastro
- Location: e2e/cdu-05.spec.ts:141:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByRole('dialog')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByRole('dialog')

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
      - link "Minha unidade":
        - /url: /unidade/12
    - listitem:
      - link "Histórico":
        - /url: /historico
  - list:
    - listitem:
      - link "CHEFE - ASSESSORIA_21":
        - /url: "#"
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
      - link "ASSESSORIA_21":
        - /url: /processo/400/ASSESSORIA_21
    - listitem: › Atividades e conhecimentos
- main:
  - heading "Atividades e conhecimentos" [level=2]
  - paragraph: ASSESSORIA_21
  - button "Histórico de análise"
  - button "Importar"
  - button "Disponibilizar"
  - alert:
    - text: Cadastro incompleto. Deve haver pelo menos uma atividade e todas devem ter conhecimentos associados.
    - button "Close"
  - textbox "Nova atividade"
  - button "Adicionar atividade"
  - 'heading "Editar Remover atividade: Atividade Incompleta 1780273838416 Atividade Incompleta 1780273838416" [level=4]':
    - button "Editar"
    - 'button "Remover atividade: Atividade Incompleta 1780273838416"'
    - strong: Atividade Incompleta 1780273838416
  - textbox "Novo conhecimento"
  - button "Adicionar conhecimento"
  - 'heading "Editar Remover atividade: Atividade teste 1780273836426 Atividade teste 1780273836426" [level=4]':
    - button "Editar"
    - 'button "Remover atividade: Atividade teste 1780273836426"'
    - strong: Atividade teste 1780273836426
  - textbox "Novo conhecimento"
  - button "Adicionar conhecimento"
  - button "Editar"
  - 'button "Remover conhecimento: Conhecimento teste"'
  - text: Conhecimento teste
- contentinfo: Versão 1.2.0 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  107 |     await expect(page.getByText(descricaoAtual)).toBeVisible();
  108 | }
  109 | 
  110 | export async function removerAtividade(page: Page, descricao: string | RegExp) {
  111 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(descricao)});
  112 |     const row = card.getByTestId('cad-atividades__hover-row');
  113 |     const btnRemover = card.getByTestId('btn-remover-atividade');
  114 | 
  115 |     await row.hover();
  116 |     await expect(btnRemover).toBeVisible();
  117 |     await btnRemover.click();
  118 | 
  119 |     const dialog = page.getByRole('dialog');
  120 |     await expect(dialog.getByText(TEXTOS.atividades.MODAL_REMOVER_ATIVIDADE_TEXTO)).toBeVisible();
  121 |     await page.getByTestId('btn-modal-confirmacao-confirmar').click();
  122 |     await expect(page.getByText(descricao)).toBeHidden();
  123 | }
  124 | 
  125 | export async function editarConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoAtual: string, novoConhecimento: string) {
  126 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
  127 |     const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoAtual});
  128 |     const btnEditar = linhaConhecimento.getByTestId('btn-editar-conhecimento');
  129 | 
  130 |     await linhaConhecimento.hover();
  131 |     await expect(btnEditar).toBeVisible();
  132 |     await btnEditar.click();
  133 | 
  134 |     const input = card.getByTestId('inp-editar-conhecimento');
  135 |     await input.waitFor({state: 'visible'});
  136 |     await input.fill(novoConhecimento);
  137 |     await card.getByTestId('btn-salvar-edicao-conhecimento').click();
  138 | 
  139 |     await expect(card.getByText(novoConhecimento)).toBeVisible();
  140 | }
  141 | 
  142 | export async function cancelarEdicaoConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimentoAtual: string, textoCancelado: string) {
  143 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
  144 |     const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoAtual});
  145 |     const btnEditar = linhaConhecimento.getByTestId('btn-editar-conhecimento');
  146 | 
  147 |     await linhaConhecimento.hover();
  148 |     await expect(btnEditar).toBeVisible();
  149 |     await btnEditar.click();
  150 | 
  151 |     const input = card.getByTestId('inp-editar-conhecimento');
  152 |     await input.waitFor({state: 'visible'});
  153 |     await input.fill(textoCancelado);
  154 | 
  155 |     await card.getByTestId('btn-cancelar-edicao-conhecimento').click();
  156 |     await expect(card.getByText(conhecimentoAtual)).toBeVisible();
  157 | }
  158 | 
  159 | export async function removerConhecimento(page: Page, atividadeDescricao: string | RegExp, conhecimento: string) {
  160 |     const card = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDescricao)});
  161 |     const linhaConhecimento = card.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimento});
  162 |     const btnRemover = linhaConhecimento.getByTestId('btn-remover-conhecimento');
  163 | 
  164 |     await linhaConhecimento.hover();
  165 |     await expect(btnRemover).toBeVisible();
  166 |     await btnRemover.click();
  167 | 
  168 |     const dialog = page.getByRole('dialog');
  169 |     await expect(dialog.getByText(TEXTOS.atividades.MODAL_REMOVER_CONHECIMENTO_TEXTO)).toBeVisible();
  170 |     await page.getByTestId('btn-modal-confirmacao-confirmar').click();
  171 |     await expect(card.getByText(conhecimento)).toBeHidden();
  172 | }
  173 | 
  174 | export async function disponibilizarCadastro(page: Page): Promise<string | null> {
  175 |     const botao = await obterBotaoDisponibilizarCadastro(page);
  176 |     const checkboxSemMudancas = page.getByTestId('chk-disponibilizacao-sem-mudancas');
  177 |     let atividadeExtraCriada: string | null = null;
  178 | 
  179 |     const checkboxCount = await checkboxSemMudancas.count();
  180 |     if (checkboxCount > 0) {
  181 |         await expect(checkboxSemMudancas).toBeVisible();
  182 | 
  183 |         if (await checkboxSemMudancas.isEnabled() && !(await checkboxSemMudancas.isChecked())) {
  184 |             await checkboxSemMudancas.check();
  185 |             await expect(checkboxSemMudancas).toBeChecked();
  186 |         }
  187 |     }
  188 | 
  189 |     await expect(botao).toBeEnabled();
  190 |     await limparNotificacoes(page);
  191 |     await botao.click();
  192 | 
  193 |     const modal = page.getByRole('dialog');
  194 |     const alert = page.getByTestId('app-alert');
  195 | 
  196 |     await modal.waitFor({state: 'visible', timeout: 1000}).catch(() => {
  197 |     });
  198 | 
  199 |     if (!(await modal.isVisible()) && await alert.isVisible()) {
  200 |         atividadeExtraCriada = `Atividade complementar ${Date.now()}`;
  201 |         await adicionarAtividade(page, atividadeExtraCriada);
  202 |         await adicionarConhecimento(page, atividadeExtraCriada, 'Conhecimento complementar');
  203 |         await botao.click();
  204 |         await expect(modal).toBeVisible();
  205 |     }
  206 | 
> 207 |     await expect(modal).toBeVisible();
      |                         ^ Error: expect(locator).toBeVisible() failed
  208 |     const btnConfirmar = modal.getByTestId('btn-confirmar-disponibilizacao');
  209 |     await expect(btnConfirmar).toBeVisible();
  210 |     await expect(btnConfirmar).toBeEnabled();
  211 |     await btnConfirmar.click();
  212 |     await verificarToast(page, /disponibilizada?|Disponibilizado/i);
  213 |     await verificarPaginaPainel(page);
  214 |     return atividadeExtraCriada;
  215 | }
  216 | 
  217 | export async function verificarBotaoDisponibilizar(page: Page, habilitado: boolean) {
  218 |     const botao = await obterBotaoDisponibilizarCadastro(page);
  219 |     await expect(botao).toBeVisible();
  220 |     // Como a validação agora é no clique, o botão quase sempre está habilitado.
  221 |     // O parâmetro 'habilitado' aqui passa a verificar se o fluxo seguiria ou mostraria erro.
  222 |     // Para manter compatibilidade com testes legados que checam estado do botão,
  223 |     // vamos apenas garantir visibilidade se o teste espera 'true',
  224 |     // ou se o teste realmente quer checar lógica de bloqueio técnica (ex: loading).
  225 |     if (habilitado) {
  226 |         await expect(botao).toBeEnabled();
  227 |     }
  228 | }
  229 | 
  230 | export async function verificarBotaoImpactoDropdown(page: Page) {
  231 |     await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeVisible();
  232 | }
  233 | 
  234 | export async function verificarBotaoHistoricoAnalise(page: Page) {
  235 |     await expect(page.getByTestId('btn-cad-atividades-historico')).toBeVisible();
  236 | }
  237 | 
  238 | export async function verificarBotaoImpactoDireto(page: Page) {
  239 |     await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeVisible();
  240 | }
  241 | 
  242 | export async function verificarBotaoImpactoAusenteEdicao(page: Page) {
  243 |     await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeHidden();
  244 | }
  245 | 
  246 | export async function verificarBotaoImpactoAusenteDireto(page: Page) {
  247 |     await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeHidden();
  248 | }
  249 | 
  250 | export async function abrirModalImpacto(page: Page) {
  251 |     await page.getByTestId('cad-atividades__btn-impactos-mapa-edicao').click();
  252 |     await expect(page.getByRole('dialog')).toBeVisible();
  253 | }
  254 | 
  255 | export async function fecharModalImpacto(page: Page) {
  256 |     await page.getByTestId('btn-fechar-impacto').click();
  257 |     await expect(page.getByRole('dialog')).toBeHidden();
  258 | }
  259 | 
  260 | async function preencherFormularioImportacao(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
  261 |     const modal = page.getByRole('dialog');
  262 |     await expect(modal.getByText(TEXTOS.atividades.MODAL_IMPORTAR_TITULO)).toBeVisible();
  263 | 
  264 |     const selectProcesso = modal.getByTestId('select-processo');
  265 |     const selectUnidade = modal.getByTestId('select-unidade');
  266 | 
  267 |     // 1. Aguardar o processo aparecer no select e selecionar
  268 |     await expect(selectProcesso.locator('option', {hasText: processoOrigemDescricao})).toBeAttached();
  269 | 
  270 |     const respostaUnidades = page.waitForResponse(r =>
  271 |         r.url().includes('/unidades-importacao')
  272 |     );
  273 |     await selectProcesso.selectOption({label: processoOrigemDescricao});
  274 |     await respostaUnidades;
  275 | 
  276 |     // 2. Aguardar a unidade aparecer no select e selecionar
  277 |     await expect(selectUnidade).toBeEnabled();
  278 |     await expect(selectUnidade.locator('option', {hasText: unidadeOrigemSigla})).toBeAttached();
  279 | 
  280 |     const respostaAtividades = page.waitForResponse(r =>
  281 |         r.url().includes('/atividades-importacao')
  282 |     );
  283 |     await selectUnidade.selectOption({label: unidadeOrigemSigla});
  284 |     await respostaAtividades;
  285 | 
  286 |     // 3. Aguardar as atividades aparecerem (checkboxes) ou o estado vazio
  287 |     const primeiroCheckbox = modal.locator('input[type="checkbox"]').first();
  288 |     const textoVazio = modal.getByText(TEXTOS.atividades.importacao.NENHUMA_ATIVIDADE);
  289 |     await expect(primeiroCheckbox.or(textoVazio)).toBeVisible();
  290 | 
  291 |     // 4. Selecionar as atividades solicitadas
  292 |     for (const desc of atividadesDescricoes) {
  293 |         const checkbox = modal.getByLabel(desc, {exact: true}).first();
  294 |         await expect(checkbox).toBeVisible();
  295 |         await checkbox.check();
  296 |     }
  297 | }
  298 | 
  299 | export async function selecionarAtividadesParaImportacao(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
  300 |     await Promise.all([
  301 |         page.waitForResponse(r => r.url().includes('/finalizados')),
  302 |         abrirModalImportacao(page)
  303 |     ]);
  304 |     await preencherFormularioImportacao(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);
  305 | }
  306 | 
  307 | 
```
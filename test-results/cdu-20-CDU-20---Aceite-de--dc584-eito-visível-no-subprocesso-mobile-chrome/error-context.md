# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-20.spec.ts >> CDU-20 - Aceite de mapa com sugestões >> GESTOR devolve validação do mapa e sistema registra efeito visível no subprocesso
- Location: e2e/cdu-20.spec.ts:205:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByRole('columnheader', { name: 'Data/hora' })
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByRole('columnheader', { name: 'Data/hora' })

```

```yaml
- heading "SGC" [level=1]
- link "Pular para o conteúdo principal":
  - /url: "#main-content"
- navigation:
  - link "SGC":
    - /url: /painel
  - button "Toggle navigation"
- button "Voltar"
- navigation "breadcrumb":
  - list:
    - listitem:
      - link "Início":
        - /url: /painel
    - listitem: › ASSESSORIA_11
- main:
  - heading "ASSESSORIA_11" [level=2]
  - paragraph: Assessoria 11
  - paragraph:
    - strong: "Processo:"
    - text: Processo CDU-20 Devolucao 1779407699060
  - paragraph: Situação:Mapa disponibilizado
  - paragraph: Localização atual:ASSESSORIA_11
  - paragraph: Prazo para conclusão da etapa atual:20/06/2026
  - paragraph:
    - strong: "Titular:"
    - text: David Bowie
  - paragraph:
    - text: "2003"
    - link "david.bowie@tre-pe.jus.br":
      - /url: mailto:david.bowie@tre-pe.jus.br
  - button "Atividades e conhecimentos Cadastro de atividades e conhecimentos da unidade":
    - heading "Atividades e conhecimentos" [level=4]
    - paragraph: Cadastro de atividades e conhecimentos da unidade
  - button "Mapa de competências Mapa de competências técnicas da unidade":
    - heading "Mapa de competências" [level=4]
    - paragraph: Mapa de competências técnicas da unidade
  - heading "Movimentações" [level=4]
  - table:
    - rowgroup:
      - row "Data/hora 21/05/2026 20:55 Origem SECRETARIA_1 Destino ASSESSORIA_11 Descrição Validação do mapa devolvida para ajustes":
        - cell "Data/hora 21/05/2026 20:55"
        - cell "Origem SECRETARIA_1"
        - cell "Destino ASSESSORIA_11"
        - cell "Descrição Validação do mapa devolvida para ajustes"
      - row "Data/hora 21/05/2026 20:54 Origem ASSESSORIA_11 Destino SECRETARIA_1 Descrição Movimentação automática via fixture":
        - cell "Data/hora 21/05/2026 20:54"
        - cell "Origem ASSESSORIA_11"
        - cell "Destino SECRETARIA_1"
        - cell "Descrição Movimentação automática via fixture"
      - row "Data/hora 21/05/2026 20:54 Origem ADMIN Destino ASSESSORIA_11 Descrição Processo iniciado":
        - cell "Data/hora 21/05/2026 20:54"
        - cell "Origem ADMIN"
        - cell "Destino ASSESSORIA_11"
        - cell "Descrição Processo iniciado"
- contentinfo: Versão 1.1.0 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  144 | 
  145 |         // Conteúdo do modal exibe as sugestões
  146 |         const txtSugestoes = page.getByTestId('txt-ver-sugestoes-mapa-html');
  147 |         await expect(txtSugestoes).toBeVisible();
  148 |         await expect(txtSugestoes).toContainText('Sugestão de ajuste na competência via fixture E2E');
  149 | 
  150 |         // Fecha o modal
  151 |         await modal.getByTestId('btn-visualizacao-texto-formatado-fechar').click();
  152 |         await expect(modal).toBeHidden();
  153 |     });
  154 | });
  155 | 
  156 | test.describe.serial('CDU-20 - Aceite de mapa com sugestões', () => {
  157 |     const UNIDADE_ALVO = 'ASSESSORIA_11';
  158 |     const TEXTO_SUGESTAO = 'Sugestão do chefe para ajuste no mapa';
  159 | 
  160 |     const timestamp = Date.now();
  161 |     const descProcesso = `Processo CDU-20 Aceite Sugestoes ${timestamp}`;
  162 | 
  163 |     test('Setup data', async ({_resetAutomatico, request}) => {
  164 |         await resetDatabase(request);
  165 |         const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
  166 |             unidade: UNIDADE_ALVO,
  167 |             descricao: descProcesso
  168 |         });
  169 |         validarProcessoFixture(processo, descProcesso);
  170 |     });
  171 | 
  172 |     test('CHEFE apresenta sugestões e GESTOR registra aceite', async ({_resetAutomatico, page}) => {
  173 |         await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
  174 |         await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
  175 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
  176 |         await navegarParaMapa(page);
  177 | 
  178 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  179 |         await abrirSugestoesMapa(page);
  180 |         await expect(page.getByRole('dialog')).toBeVisible();
  181 |         await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
  182 |         await page.getByTestId('btn-sugestoes-mapa-confirmar').click();
  183 | 
  184 |         await verificarPaginaPainel(page);
  185 | 
  186 |         await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
  187 |         await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
  188 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
  189 |         await navegarParaMapa(page);
  190 | 
  191 |         await expect(page.getByTestId('btn-mapa-ver-sugestoes')).toBeVisible();
  192 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  193 | 
  194 |         await page.getByTestId('btn-mapa-ver-sugestoes').click();
  195 |         await expect(page.getByTestId('txt-ver-sugestoes-mapa-html')).toContainText(TEXTO_SUGESTAO);
  196 |         await page.getByRole('dialog').getByTestId('btn-visualizacao-texto-formatado-fechar').click();
  197 | 
  198 |         await abrirAcaoPrincipalMapa(page);
  199 |         await page.getByTestId('btn-aceite-mapa-confirmar').click();
  200 | 
  201 |         await verificarPaginaPainel(page);
  202 |         await expect(page.getByText(TEXTOS.sucesso.ACEITE_REGISTRADO).first()).toBeVisible();
  203 |     });
  204 | 
  205 |     test('GESTOR devolve validação do mapa e sistema registra efeito visível no subprocesso', async ({
  206 |                                                                                                          _resetAutomatico,
  207 |                                                                                                          request,
  208 |                                                                                                          page
  209 |                                                                                                      }) => {
  210 |         await resetDatabase(request);
  211 |         const processo = await criarProcessoMapaValidadoFixture(request, {
  212 |             unidade: UNIDADE_ALVO,
  213 |             descricao: `Processo CDU-20 Devolucao ${Date.now()}`
  214 |         });
  215 |         validarProcessoFixture(processo, processo.descricao);
  216 | 
  217 |         await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
  218 |         await acessarSubprocessoGestor(page, processo.descricao, UNIDADE_ALVO);
  219 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);
  220 |         await navegarParaMapa(page);
  221 | 
  222 |         await abrirDevolucaoMapa(page);
  223 |         const modal = page.locator('.modal.show').filter({hasText: 'Devolver mapa'});
  224 |         await expect(modal).toBeVisible();
  225 |         await expect(modal).toContainText('Devolver mapa');
  226 |         await expect(modal).toContainText('Confirma a devolução da validação do mapa para ajustes?');
  227 |         await expect(modal.getByTestId('btn-devolucao-mapa-confirmar')).toBeEnabled();
  228 |         await modal.getByTestId('btn-devolucao-mapa-confirmar').click();
  229 |         await expect(page.getByText('A justificativa é obrigatória para a devolução.')).toBeVisible();
  230 | 
  231 |         await page.getByTestId('inp-devolucao-mapa-obs').fill('Necessário rever competências');
  232 |         await expect(modal.getByTestId('btn-devolucao-mapa-confirmar')).toBeEnabled();
  233 |         await expect(page.getByText('A justificativa é obrigatória para a devolução.')).toBeHidden();
  234 |         await modal.getByTestId('btn-devolucao-mapa-confirmar').click();
  235 | 
  236 |         await verificarPaginaPainel(page);
  237 |         await expect(page.getByText(TEXTOS.sucesso.DEVOLUCAO_REALIZADA).first()).toBeVisible();
  238 | 
  239 |         await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
  240 |         await acessarSubprocessoChefeDireto(page, processo.descricao, UNIDADE_ALVO);
  241 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
  242 |         await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  243 |         await expect(page.getByTestId('tbl-movimentacoes')).toBeVisible();
> 244 |         await expect(page.getByRole('columnheader', {name: 'Data/hora'})).toBeVisible();
      |                                                                           ^ Error: expect(locator).toBeVisible() failed
  245 |         await expect(page.getByRole('columnheader', {name: 'Origem'})).toBeVisible();
  246 |         await expect(page.getByRole('columnheader', {name: 'Destino'})).toBeVisible();
  247 |         await expect(page.getByRole('columnheader', {name: 'Descrição'})).toBeVisible();
  248 | 
  249 |         const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
  250 |             .locator('tr', {hasText: 'Validação do mapa devolvida para ajustes'})
  251 |             .first();
  252 |         await expect(linhaMovimentacao).toBeVisible();
  253 | 
  254 |         const dataHora = await linhaMovimentacao.locator('td').nth(0).innerText();
  255 |         validarDataHoraBrasileira(dataHora);
  256 |         await expect(linhaMovimentacao.locator('td').nth(1)).toHaveText('SECRETARIA_1');
  257 |         await expect(linhaMovimentacao.locator('td').nth(2)).toHaveText(UNIDADE_ALVO);
  258 | 
  259 |         await navegarParaMapa(page);
  260 |         await esperarMapaSomenteLeitura(page);
  261 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  262 | 
  263 |     });
  264 | });
  265 | 
  266 | test.describe.serial('CDU-20 - ADMIN deve ver botões de edição com mapa com sugestões (Bug #1376)', () => {
  267 |     const UNIDADE_ALVO = 'ASSESSORIA_11';
  268 | 
  269 |     const timestamp = Date.now();
  270 |     const descProcesso = `Processo CDU-20 Bug1376 EditarMapa ${timestamp}`;
  271 | 
  272 |     test('Setup data', async ({_resetAutomatico, request}) => {
  273 |         await resetDatabase(request);
  274 |         const processo = await criarProcessoMapaComSugestoesFixture(request, {
  275 |             unidade: UNIDADE_ALVO,
  276 |             descricao: descProcesso
  277 |         });
  278 |         validarProcessoFixture(processo, descProcesso);
  279 |     });
  280 | 
  281 |     test('ADMIN vê card de edição de mapa quando situação é Mapa com sugestões', async ({_resetAutomatico, page}) => {
  282 |         await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  283 |         await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
  284 | 
  285 |         await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  286 |         await navegarParaMapa(page);
  287 |         await esperarMapaSomenteLeitura(page);
  288 |     });
  289 | });
  290 | 
  291 | test.describe.serial('CDU-20 - ADMIN homologa mapa após GESTOR aceitar com sugestões (Bug #1376)', () => {
  292 |     const UNIDADE_ALVO = 'ASSESSORIA_11';
  293 |     const TEXTO_SUGESTAO = 'Sugestão para ajuste no mapa - Bug 1376';
  294 | 
  295 |     const timestamp = Date.now();
  296 |     const descProcesso = `Processo CDU-20 Bug1376 Homologar ${timestamp}`;
  297 | 
  298 |     test('Setup data', async ({_resetAutomatico, request}) => {
  299 |         await resetDatabase(request);
  300 |         const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
  301 |             unidade: UNIDADE_ALVO,
  302 |             descricao: descProcesso
  303 |         });
  304 |         validarProcessoFixture(processo, descProcesso);
  305 |     });
  306 | 
  307 |     test('CHEFE apresenta sugestões e GESTOR registra aceite', async ({_resetAutomatico, page}) => {
  308 |         await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
  309 |         await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
  310 |         await navegarParaMapa(page);
  311 | 
  312 |         await abrirSugestoesMapa(page);
  313 |         await expect(page.getByRole('dialog')).toBeVisible();
  314 |         await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
  315 |         await page.getByTestId('btn-sugestoes-mapa-confirmar').click();
  316 |         await verificarPaginaPainel(page);
  317 | 
  318 |         await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
  319 |         await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
  320 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
  321 |         await navegarParaMapa(page);
  322 | 
  323 |         await abrirAcaoPrincipalMapa(page);
  324 |         await page.getByTestId('btn-aceite-mapa-confirmar').click();
  325 |         await verificarPaginaPainel(page);
  326 |         await expect(page.getByText(TEXTOS.sucesso.ACEITE_REGISTRADO).first()).toBeVisible();
  327 |     });
  328 | 
  329 |     test('ADMIN vê ações corretas quando o mapa permanece com sugestões após aceite do GESTOR', async ({
  330 |                                                                                                            _resetAutomatico,
  331 |                                                                                                            page
  332 |                                                                                                        }) => {
  333 |         await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  334 |         await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
  335 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
  336 |         await navegarParaMapa(page);
  337 | 
  338 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  339 |         await page.getByTestId('btn-mapa-acoes').click();
  340 | 
  341 |         await expect(page.getByTestId('btn-mapa-acao-disponibilizar')).toBeVisible();
  342 |         await expect(page.getByTestId('btn-mapa-acao-disponibilizar')).toBeEnabled();
  343 |         await expect(page.getByTestId('btn-mapa-acao-devolver')).toBeVisible();
  344 |         await expect(page.getByTestId('btn-mapa-acao-devolver')).toBeEnabled();
```
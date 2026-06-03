# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-41.spec.ts >> CDU-41 - Iniciar processo de diagnóstico >> ADMIN inicia diagnóstico, cria subprocesso e notificação inicial
- Location: e2e/cdu-41.spec.ts:8:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('tbl-processos').locator('tr').filter({ hasText: 'Diagnóstico CDU-41 1780524966483' })
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('tbl-processos').locator('tr').filter({ hasText: 'Diagnóstico CDU-41 1780524966483' })

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
    - listitem "Ativar modo escuro":
      - link "Ativar modo escuro":
        - /url: "#"
    - listitem:
      - button "Ações especiais"
    - listitem "Sair":
      - link "Sair":
        - /url: "#"
- main:
  - heading "Painel" [level=1]
  - heading "Processos" [level=2]
  - button "Criar processo"
  - table:
    - rowgroup:
      - row "Descrição Click to sort descending Tipo Click to sort ascending Unidades Situação Click to sort ascending":
        - columnheader "Descrição Click to sort descending"
        - columnheader "Tipo Click to sort ascending"
        - columnheader "Unidades"
        - columnheader "Situação Click to sort ascending"
    - rowgroup:
      - row "Mapeamento Secão 311 Mapeamento COORD_31 Em andamento":
        - cell "Mapeamento Secão 311"
        - cell "Mapeamento"
        - cell "COORD_31"
        - cell "Em andamento"
      - row "Mapeamento Secão 321 Mapeamento SECAO_321 Em andamento":
        - cell "Mapeamento Secão 321"
        - cell "Mapeamento"
        - cell "SECAO_321"
        - cell "Em andamento"
      - row "Processo 99 Mapeamento ASSESSORIA_12 Finalizado":
        - cell "Processo 99"
        - cell "Mapeamento"
        - cell "ASSESSORIA_12"
        - cell "Finalizado"
      - row "Processo Seed 200 Mapeamento SECRETARIA_1 Finalizado":
        - cell "Processo Seed 200"
        - cell "Mapeamento"
        - cell "SECRETARIA_1"
        - cell "Finalizado"
  - heading "Alertas" [level=2]
  - table:
    - rowgroup:
      - row "Data/Hora Descrição Processo Origem":
        - columnheader "Data/Hora"
        - columnheader "Descrição"
        - columnheader "Processo"
        - columnheader "Origem"
    - rowgroup:
      - 'row "02/06/2026 08:16 Não lido: Mapa de competências homologado Processo 99 SECRETARIA_1"':
        - cell "02/06/2026 08:16"
        - 'cell "Não lido: Mapa de competências homologado"'
        - cell "Processo 99"
        - cell "SECRETARIA_1"
      - 'row "02/06/2026 03:16 Não lido: Cadastro homologado Mapeamento Secão 311 ADMIN"':
        - cell "02/06/2026 03:16"
        - 'cell "Não lido: Cadastro homologado"'
        - cell "Mapeamento Secão 311"
        - cell "ADMIN"
      - 'row "01/06/2026 09:16 Não lido: Mapa de competências homologado Processo Seed 200 SECRETARIA_1"':
        - cell "01/06/2026 09:16"
        - 'cell "Não lido: Mapa de competências homologado"'
        - cell "Processo Seed 200"
        - cell "SECRETARIA_1"
      - row "29/05/2026 15:16 Alerta antigo seed 201 Processo Histórico Seed 201 SECRETARIA_1":
        - cell "29/05/2026 15:16"
        - cell "Alerta antigo seed 201"
        - cell "Processo Histórico Seed 201"
        - cell "SECRETARIA_1"
- contentinfo: Versão 1.2.0 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  294 | }
  295 | 
  296 | export async function verificarDetalhesSubprocesso(page: Page, dados: {
  297 |     sigla: string,
  298 |     nomeUnidade?: string,
  299 |     situacao: string,
  300 |     prazo?: string | RegExp,
  301 |     localizacao?: string,
  302 |     titular?: string,
  303 |     ramalTitular?: string,
  304 |     emailTitular?: string,
  305 |     responsavel?: string,
  306 |     tipoResponsabilidade?: string,
  307 |     ramalResponsavel?: string,
  308 |     emailResponsavel?: string
  309 | }) {
  310 |     const header = page.getByTestId('header-subprocesso');
  311 |     await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(dados.sigla);
  312 |     if (dados.nomeUnidade) {
  313 |         await expect(header).toContainText(dados.nomeUnidade);
  314 |     }
  315 |     if (dados.titular) {
  316 |         await expect(page.getByText(`Titular: ${dados.titular}`).first()).toBeVisible();
  317 |     }
  318 |     if (dados.ramalTitular) {
  319 |         await expect(header.getByText(dados.ramalTitular)).toBeVisible();
  320 |     }
  321 |     if (dados.emailTitular) {
  322 |         await expect(header.getByRole('link', {name: dados.emailTitular})).toBeVisible();
  323 |     }
  324 |     if (dados.responsavel) {
  325 |         await expect(page.getByText(`Responsável: ${dados.responsavel}`).first()).toBeVisible();
  326 |     }
  327 |     if (dados.tipoResponsabilidade) {
  328 |         await expect(page.getByText(`- ${dados.tipoResponsabilidade}`).first()).toBeVisible();
  329 |     }
  330 |     if (dados.ramalResponsavel) {
  331 |         await expect(header.getByText(dados.ramalResponsavel)).toBeVisible();
  332 |     }
  333 |     if (dados.emailResponsavel) {
  334 |         await expect(header.getByRole('link', {name: dados.emailResponsavel})).toBeVisible();
  335 |     }
  336 |     if (dados.localizacao) {
  337 |         await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toContainText(dados.localizacao);
  338 |     }
  339 |     if (dados.prazo) {
  340 |         const campoPrazo = page.locator('span:has-text("Prazo para conclusão da etapa atual:")').first();
  341 |         await expect(campoPrazo).toContainText(dados.prazo);
  342 |     }
  343 |     await expect(page.getByTestId('subprocesso-header__txt-situacao')).toContainText(dados.situacao);
  344 | }
  345 | 
  346 | /**
  347 |  * Extrai o código do processo da URL atual.
  348 |  * Suporta múltiplos formatos de URL do sistema:
  349 |  * - /processo/cadastro/{codigo}
  350 |  * - codProcesso={codigo}
  351 |  * - /processo/{codigo}
  352 |  *
  353 |  * @throws {Error} Se não for possível extrair o código da URL atual
  354 |  */
  355 | export async function extrairProcessoCodigo(page: Page): Promise<number> {
  356 |     const url = page.url();
  357 | 
  358 |     const patterns = [
  359 |         /\/processo\/cadastro\/(\d+)/,
  360 |         /codProcesso=(\d+)/,
  361 |         /\/processo\/(\d+)/
  362 |     ];
  363 | 
  364 |     for (const pattern of patterns) {
  365 |         const match = new RegExp(pattern).exec(url);
  366 |         if (match?.[1]) {
  367 |             return Number.parseInt(match[1]);
  368 |         }
  369 |     }
  370 | 
  371 |     throw new Error(
  372 |         `Não foi possível extrair código do processo da URL: ${url}`
  373 |     );
  374 | }
  375 | 
  376 | export async function obterAcaoBloco(page: Page, testId: string): Promise<Locator> {
  377 |     const acao = page.getByTestId(testId);
  378 |     if (await acao.isVisible().catch(() => false)) {
  379 |         return acao;
  380 |     }
  381 | 
  382 |     const botaoMenu = page.getByRole('button', {name: new RegExp(`^${TEXTOS.processo.ACOES_EM_BLOCO}$`, 'i')}).first();
  383 |     await expect(botaoMenu).toBeVisible();
  384 |     await botaoMenu.click();
  385 |     await expect(acao).toBeVisible();
  386 |     return acao;
  387 | }
  388 | 
  389 | /**
  390 |  * Acessa a tela de detalhes de um processo a partir do painel.
  391 |  */
  392 | export async function acessarDetalhesProcesso(page: Page, descricao: string) {
  393 |     const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {hasText: descricao});
> 394 |     await expect(linhaProcesso).toBeVisible();
      |                                 ^ Error: expect(locator).toBeVisible() failed
  395 |     await linhaProcesso.focus();
  396 |     await linhaProcesso.press('Enter');
  397 |     await expect(page).toHaveURL(/\/processo\/(?:cadastro\?codProcesso=)?\d+(?:\/[A-Z0-9_]+)?(?:\?.*)?$/);
  398 | }
  399 | 
  400 | 
  401 | /**
  402 |  * Finaliza o processo atual a partir da tela de detalhes do processo.
  403 |  */
  404 | export async function finalizarProcesso(page: Page) {
  405 |     const botaoFinalizar = page.getByTestId('btn-processo-finalizar');
  406 |     await expect(botaoFinalizar).toBeEnabled();
  407 |     await botaoFinalizar.click();
  408 | 
  409 |     // 1. Instanciar promessa para o POST de finalização
  410 |     const postFinalizarPromise = page.waitForResponse(r =>
  411 |         r.url().match(/\/api\/processos\/\d+\/finalizar/) !== null &&
  412 |         r.request().method() === 'POST' &&
  413 |         r.ok()
  414 |     );
  415 | 
  416 |     // 2. Clicar no botão e aguardar o POST concluir
  417 |     await page.getByTestId('btn-finalizar-processo-confirmar').click();
  418 |     await postFinalizarPromise;
  419 | 
  420 |     const bootstrapPromise = page.waitForResponse(
  421 |         r => r.url().includes('/api/painel/bootstrap') && r.ok(),
  422 |         {timeout: 3000}
  423 |     ).catch(async () => {
  424 |         // Fallback: se o Vue não disparou o refresh() no onActivated, forçamos o reload.
  425 |         await page.reload();
  426 |     });
  427 | 
  428 |     await page.waitForURL(/\/painel(?:\?.*)?$/);
  429 |     await bootstrapPromise;
  430 | }
  431 | 
```
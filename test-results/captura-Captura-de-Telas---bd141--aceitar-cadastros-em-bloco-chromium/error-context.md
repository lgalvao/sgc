# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 09 - Operações em Bloco >> Captura fluxo de aceitar cadastros em bloco
- Location: e2e\captura.spec.ts:1221:9

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByRole('button', { name: /^Ações em bloco$/i }).first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByRole('button', { name: /^Ações em bloco$/i }).first()

```

# Test source

```ts
  243 | 
  244 | export async function verificarUnidadeParticipante(page: Page, unidade: UnidadeParticipante) {
  245 |     const row = page.locator('tr').filter({hasText: new RegExp(String.raw`^\s*${unidade.sigla}\b`, 'i')}).first();
  246 |     await expect(row).toBeVisible();
  247 |     await expect(row).toContainText(unidade.situacao);
  248 | 
  249 |     if (unidade.dataLimite instanceof RegExp) {
  250 |         await expect(row).toHaveText(unidade.dataLimite);
  251 |     } else {
  252 |         await expect(row).toContainText(unidade.dataLimite);
  253 |     }
  254 | }
  255 | 
  256 | export async function verificarDetalhesSubprocesso(page: Page, dados: {
  257 |     sigla: string,
  258 |     nomeUnidade?: string,
  259 |     situacao: string,
  260 |     prazo?: string | RegExp,
  261 |     localizacao?: string,
  262 |     titular?: string,
  263 |     ramalTitular?: string,
  264 |     emailTitular?: string,
  265 |     responsavel?: string,
  266 |     tipoResponsabilidade?: string,
  267 |     ramalResponsavel?: string,
  268 |     emailResponsavel?: string
  269 | }) {
  270 |     const header = page.getByTestId('header-subprocesso');
  271 |     await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(dados.sigla);
  272 |     if (dados.nomeUnidade) {
  273 |         await expect(header).toContainText(dados.nomeUnidade);
  274 |     }
  275 |     if (dados.titular) {
  276 |         await expect(page.getByText(`Titular: ${dados.titular}`).first()).toBeVisible();
  277 |     }
  278 |     if (dados.ramalTitular) {
  279 |         await expect(header.getByText(dados.ramalTitular)).toBeVisible();
  280 |     }
  281 |     if (dados.emailTitular) {
  282 |         await expect(header.getByRole('link', {name: dados.emailTitular})).toBeVisible();
  283 |     }
  284 |     if (dados.responsavel) {
  285 |         await expect(page.getByText(`Responsável: ${dados.responsavel}`).first()).toBeVisible();
  286 |     }
  287 |     if (dados.tipoResponsabilidade) {
  288 |         await expect(page.getByText(`- ${dados.tipoResponsabilidade}`).first()).toBeVisible();
  289 |     }
  290 |     if (dados.ramalResponsavel) {
  291 |         await expect(header.getByText(dados.ramalResponsavel)).toBeVisible();
  292 |     }
  293 |     if (dados.emailResponsavel) {
  294 |         await expect(header.getByRole('link', {name: dados.emailResponsavel})).toBeVisible();
  295 |     }
  296 |     if (dados.localizacao) {
  297 |         await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toContainText(dados.localizacao);
  298 |     }
  299 |     if (dados.prazo) {
  300 |         const campoPrazo = page.locator('span:has-text("Prazo para conclusão da etapa atual:")').first();
  301 |         await expect(campoPrazo).toContainText(dados.prazo);
  302 |     }
  303 |     await expect(page.getByTestId('subprocesso-header__txt-situacao')).toContainText(dados.situacao);
  304 | }
  305 | 
  306 | /**
  307 |  * Extrai o código do processo da URL atual.
  308 |  * Suporta múltiplos formatos de URL do sistema:
  309 |  * - /processo/cadastro/{codigo}
  310 |  * - codProcesso={codigo}
  311 |  * - /processo/{codigo}
  312 |  *
  313 |  * @throws {Error} Se não for possível extrair o código da URL atual
  314 |  */
  315 | export async function extrairProcessoCodigo(page: Page): Promise<number> {
  316 |     const url = page.url();
  317 | 
  318 |     const patterns = [
  319 |         /\/processo\/cadastro\/(\d+)/,
  320 |         /codProcesso=(\d+)/,
  321 |         /\/processo\/(\d+)/
  322 |     ];
  323 | 
  324 |     for (const pattern of patterns) {
  325 |         const match = new RegExp(pattern).exec(url);
  326 |         if (match?.[1]) {
  327 |             return Number.parseInt(match[1]);
  328 |         }
  329 |     }
  330 | 
  331 |     throw new Error(
  332 |         `Não foi possível extrair código do processo da URL: ${url}`
  333 |     );
  334 | }
  335 | 
  336 | export async function obterAcaoBloco(page: Page, testId: string): Promise<Locator> {
  337 |     const acao = page.getByTestId(testId);
  338 |     if (await acao.isVisible().catch(() => false)) {
  339 |         return acao;
  340 |     }
  341 | 
  342 |     const botaoMenu = page.getByRole('button', {name: new RegExp(`^${TEXTOS.processo.ACOES_EM_BLOCO}$`, 'i')}).first();
> 343 |     await expect(botaoMenu).toBeVisible();
      |                             ^ Error: expect(locator).toBeVisible() failed
  344 |     await botaoMenu.click();
  345 |     await expect(acao).toBeVisible();
  346 |     return acao;
  347 | }
  348 | 
  349 | /**
  350 |  * Acessa a tela de detalhes de um processo a partir do painel.
  351 |  */
  352 | export async function acessarDetalhesProcesso(page: Page, descricao: string) {
  353 |     const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {hasText: descricao});
  354 |     await expect(linhaProcesso).toBeVisible();
  355 |     await linhaProcesso.click();
  356 |     await expect(page).toHaveURL(/\/processo\/(?:cadastro\?codProcesso=)?\d+(?:\/[A-Z0-9_]+)?(?:\?.*)?$/);
  357 | }
  358 | 
  359 | 
  360 | /**
  361 |  * Finaliza o processo atual a partir da tela de detalhes do processo.
  362 |  */
  363 | export async function finalizarProcesso(page: Page) {
  364 |     await page.getByTestId('btn-processo-finalizar').click();
  365 |     await page.getByTestId('btn-finalizar-processo-confirmar').click();
  366 |     await page.waitForURL(/\/painel(?:\?.*)?$/);
  367 | }
  368 | 
```
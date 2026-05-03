# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 10 - Gestão de Subprocessos >> Captura modais de gestão de subprocesso
- Location: e2e/captura.spec.ts:1299:9

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('btn-reabrir-cadastro')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('btn-reabrir-cadastro')

```

# Test source

```ts
  100 |                 mensagem.includes('intercepts pointer events')
  101 |                 || mensagem.includes('another element would receive the click')
  102 |                 || mensagem.includes('timeout')
  103 |             ) {
  104 |                 await limparNotificacoes(page);
  105 |                 await botaoLogout.click({force: true, timeout: 2_000});
  106 |             } else {
  107 |                 throw e;
  108 |             }
  109 |         }
  110 |         await page.waitForURL(/\/login/);
  111 | 
  112 |         // Limpar possíveis toasts de "Não autorizado" ou "Sessão expirada" que aparecem no teardown após o logout
  113 |         await limparNotificacoes(page);
  114 |     } catch (e: any) {
  115 |         if (e.message?.includes('closed') || e.message?.includes('Target page, context or browser has been closed')) {
  116 |             return;
  117 |         }
  118 |         throw e;
  119 |     }
  120 | }
  121 | 
  122 | /**
  123 |  * Verifica que está na página do painel principal.
  124 |  */
  125 | export async function verificarPaginaPainel(page: Page): Promise<void> {
  126 |     await expect(page).toHaveURL(/\/painel/);
  127 | }
  128 | 
  129 | /**
  130 |  * Aguarda a navegação para a página de painel.
  131 |  */
  132 | export async function esperarPaginaPainel(page: Page): Promise<void> {
  133 |     await page.waitForURL(/\/painel/);
  134 | }
  135 | 
  136 | /**
  137 |  * Aguarda a navegação para a página de cadastro de processo (novo ou edição).
  138 |  */
  139 | export async function esperarPaginaCadastroProcesso(page: Page): Promise<void> {
  140 |     await page.waitForURL(/\/processo\/cadastro/);
  141 | }
  142 | 
  143 | /**
  144 |  * Aguarda a navegação para a página de detalhes de um processo.
  145 |  */
  146 | export async function esperarPaginaDetalhesProcesso(page: Page, codigo?: number): Promise<void> {
  147 |     const pattern = codigo 
  148 |         ? String.raw`\/processo\/(?:cadastro\?codProcesso=)?${codigo}(?:\?.*)?$`
  149 |         : String.raw`\/processo\/(?:cadastro\?codProcesso=)?\d+(?:\?.*)?$`;
  150 |     await page.waitForURL(new RegExp(pattern));
  151 | }
  152 | 
  153 | 
  154 | /**
  155 |  * Aguarda a navegação para a página de detalhes de um subprocesso.
  156 |  */
  157 | export async function esperarPaginaSubprocesso(page: Page, siglaUnidade?: string): Promise<void> {
  158 |     const regex = siglaUnidade 
  159 |         ? new RegExp(String.raw`\/processo\/\d+\/${siglaUnidade}(?:\?.*)?$`) 
  160 |         : /\/processo\/\d+\/[A-Z0-9_]+(?:\?.*)?$/;
  161 |     await page.waitForURL(regex);
  162 | }
  163 | 
  164 | /**
  165 |  * Navega para um subprocesso clicando na célula da unidade na tabela TreeTable.
  166 |  * Se já estiver na página do subprocesso (redirecionamento direto), apenas valida.
  167 |  */
  168 | export async function navegarParaSubprocesso(
  169 |     page: Page,
  170 |     siglaUnidade: string
  171 | ): Promise<void> {
  172 |     // Aguardar qualquer transição de rota antes de checar a URL
  173 |     await page.waitForURL(/\/processo\/\d+/);
  174 | 
  175 |     const urlSubprocesso = new RegExp(String.raw`/processo/\d+/${siglaUnidade}(?:\?.*)?$`);
  176 |     if (urlSubprocesso.test(page.url())) return;
  177 | 
  178 |     await expect(page.getByText('Carregando detalhes do processo...').first()).toBeHidden();
  179 |     const info = page.getByTestId('processo-info');
  180 |     await expect(info).toBeVisible();
  181 | 
  182 |     const tabela = page.getByTestId('tbl-tree');
  183 |     await expect(tabela).toBeVisible();
  184 | 
  185 |     const celula = tabela.getByRole('cell', {name: new RegExp(String.raw`^${siglaUnidade}\b`)}).first();
  186 |     await expect(celula).toBeVisible();
  187 |     await celula.click();
  188 | 
  189 |     await expect(page).toHaveURL(urlSubprocesso);
  190 | }
  191 | 
  192 | export async function obterAcaoCabecalhoSubprocesso(page: Page, testIdAcao: string) {
  193 |     const dropdown = page.getByTestId('btn-subprocesso-acoes');
  194 |     if (await dropdown.count() > 0) {
  195 |         const acaoMenu = page.getByTestId(testIdAcao);
  196 |         if (await acaoMenu.count() === 0 || !(await acaoMenu.isVisible())) {
  197 |             await expect(dropdown).toBeVisible();
  198 |             await dropdown.click();
  199 |         }
> 200 |         await expect(acaoMenu).toBeVisible();
      |                                ^ Error: expect(locator).toBeVisible() failed
  201 |         return acaoMenu;
  202 |     }
  203 | 
  204 |     const acaoDireta = page.getByTestId(testIdAcao);
  205 |     await expect(acaoDireta).toBeVisible();
  206 |     return acaoDireta;
  207 | }
  208 | 
```
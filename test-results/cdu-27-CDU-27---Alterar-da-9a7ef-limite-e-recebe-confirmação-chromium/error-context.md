# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-27.spec.ts >> CDU-27 - Alterar data limite de subprocesso >> Cenario 3: ADMIN altera data limite e recebe confirmação
- Location: e2e\cdu-27.spec.ts:122:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator:  getByTestId('btn-alterar-data-limite')
Expected: visible
Received: hidden
Timeout:  5000ms

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('btn-alterar-data-limite')
    14 × locator resolved to <button role="menu" type="button" class="dropdown-item" data-testid="btn-alterar-data-limite">…</button>
       - unexpected value "hidden"

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
      - link "Detalhes do processo":
        - /url: /processo/400
    - listitem: › SECAO_221
- main:
  - heading "SECAO_221" [level=2]
  - paragraph: Seção 221
  - button "Ações"
  - paragraph:
    - strong: "Processo:"
    - text: Mapeamento CDU-27 1778761534070
  - paragraph: Situação:Não iniciado
  - paragraph: Localização atual:SECAO_221
  - paragraph: Prazo para conclusão da etapa atual:13/06/2026
  - paragraph:
    - strong: "Titular:"
    - text: Tina Turner
  - paragraph:
    - text: "2018"
    - link "tina.turner@tre-pe.jus.br":
      - /url: mailto:tina.turner@tre-pe.jus.br
  - heading "Atividades e conhecimentos" [level=4]
  - paragraph: Cadastro de atividades e conhecimentos da unidade
  - heading "Mapa de competências" [level=4]
  - paragraph: Mapa de competências técnicas da unidade
  - heading "Movimentações" [level=4]
  - table:
    - rowgroup:
      - row "Data/hora Origem Destino Descrição":
        - columnheader "Data/hora"
        - columnheader "Origem"
        - columnheader "Destino"
        - columnheader "Descrição"
    - rowgroup:
      - row "14/05/2026 09:25 ADMIN SECAO_221 Processo iniciado":
        - cell "14/05/2026 09:25"
        - cell "ADMIN"
        - cell "SECAO_221"
        - cell "Processo iniciado"
- contentinfo: Versão 1.0.4 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  148 | 
  149 | /**
  150 |  * Verifica que está na página do painel principal.
  151 |  */
  152 | export async function verificarPaginaPainel(page: Page): Promise<void> {
  153 |     await expect(page).toHaveURL(/\/painel/);
  154 | }
  155 | 
  156 | /**
  157 |  * Aguarda a navegação para a página de painel.
  158 |  */
  159 | export async function esperarPaginaPainel(page: Page): Promise<void> {
  160 |     await page.waitForURL(/\/painel/);
  161 | }
  162 | 
  163 | /**
  164 |  * Aguarda a navegação para a página de cadastro de processo (novo ou edição).
  165 |  */
  166 | export async function esperarPaginaCadastroProcesso(page: Page): Promise<void> {
  167 |     await page.waitForURL(/\/processo\/cadastro/);
  168 | }
  169 | 
  170 | /**
  171 |  * Aguarda a navegação para a página de detalhes de um processo.
  172 |  */
  173 | export async function esperarPaginaDetalhesProcesso(page: Page, codigo?: number): Promise<void> {
  174 |     const pattern = codigo
  175 |         ? String.raw`\/processo\/(?:cadastro\?codProcesso=)?${codigo}(?:\?.*)?$`
  176 |         : String.raw`\/processo\/(?:cadastro\?codProcesso=)?\d+(?:\?.*)?$`;
  177 |     await page.waitForURL(new RegExp(pattern));
  178 | }
  179 | 
  180 | 
  181 | /**
  182 |  * Aguarda a navegação para a página de detalhes de um subprocesso.
  183 |  */
  184 | export async function esperarPaginaSubprocesso(page: Page, siglaUnidade?: string): Promise<void> {
  185 |     const regex = siglaUnidade
  186 |         ? new RegExp(String.raw`\/processo\/\d+\/${siglaUnidade}(?:\?.*)?$`)
  187 |         : /\/processo\/\d+\/[A-Z0-9_]+(?:\?.*)?$/;
  188 |     await page.waitForURL(regex);
  189 | }
  190 | 
  191 | /**
  192 |  * Navega para um subprocesso a partir da tela de detalhes do processo.
  193 |  * Suporta tanto a árvore de subprocessos quanto a tabela simples exibida em alguns perfis/fluxos.
  194 |  * Se já estiver na página do subprocesso (redirecionamento direto), apenas valida.
  195 |  */
  196 | export async function navegarParaSubprocesso(
  197 |     page: Page,
  198 |     siglaUnidade: string
  199 | ): Promise<void> {
  200 |     // Aguardar qualquer transição de rota antes de checar a URL
  201 |     await page.waitForURL(/\/processo\/\d+/);
  202 | 
  203 |     const urlSubprocesso = new RegExp(String.raw`/processo/\d+/${siglaUnidade}(?:\?.*)?$`);
  204 |     if (urlSubprocesso.test(page.url())) return;
  205 | 
  206 |     const info = page.getByTestId('processo-info');
  207 |     await expect(info).toBeVisible();
  208 | 
  209 |     const padraoUnidade = new RegExp(String.raw`^${siglaUnidade}\b`, 'i');
  210 |     const tabelaArvore = page.getByTestId('tbl-tree');
  211 |     if (await tabelaArvore.count() > 0 && await tabelaArvore.isVisible()) {
  212 |         const celula = tabelaArvore.getByRole('cell', {name: padraoUnidade}).first();
  213 |         await expect(celula).toBeVisible();
  214 |         await celula.click();
  215 |         await expect(page).toHaveURL(urlSubprocesso);
  216 |         return;
  217 |     }
  218 | 
  219 |     const tabelaProcessos = page.getByTestId('tbl-processos');
  220 |     if (await tabelaProcessos.count() > 0 && await tabelaProcessos.isVisible()) {
  221 |         const linhaProcesso = tabelaProcessos.locator('tr').filter({hasText: padraoUnidade}).first();
  222 |         await expect(linhaProcesso).toBeVisible();
  223 |         await linhaProcesso.click();
  224 |         await expect(page).toHaveURL(urlSubprocesso);
  225 |         return;
  226 |     }
  227 | 
  228 |     const linhaGenerica = page.locator('main table tr').filter({hasText: padraoUnidade}).first();
  229 |     await expect(linhaGenerica).toBeVisible();
  230 |     await linhaGenerica.click();
  231 | 
  232 |     await expect(page).toHaveURL(urlSubprocesso);
  233 | }
  234 | 
  235 | export async function obterAcaoCabecalhoSubprocesso(page: Page, testIdAcao: string) {
  236 |     const dropdown = page.getByTestId('btn-subprocesso-acoes');
  237 |     if (await dropdown.count() > 0) {
  238 |         const acaoMenu = page.getByTestId(testIdAcao);
  239 |         if (await acaoMenu.count() === 0 || !(await acaoMenu.isVisible())) {
  240 |             await expect(dropdown).toBeVisible();
  241 |             await dropdown.click();
  242 |         }
  243 |         await expect(acaoMenu).toBeVisible();
  244 |         return acaoMenu;
  245 |     }
  246 | 
  247 |     const acaoDireta = page.getByTestId(testIdAcao);
> 248 |     await expect(acaoDireta).toBeVisible();
      |                              ^ Error: expect(locator).toBeVisible() failed
  249 |     return acaoDireta;
  250 | }
  251 | 
```
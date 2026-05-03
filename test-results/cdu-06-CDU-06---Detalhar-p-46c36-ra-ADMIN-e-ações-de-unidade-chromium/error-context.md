# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-06.spec.ts >> CDU-06 - Detalhar processo >> Fase 1: Deve exibir detalhes do processo para ADMIN e ações de unidade
- Location: e2e/cdu-06.spec.ts:28:5

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

# Page snapshot

```yaml
- generic [ref=e1]:
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
                - link "Unidades" [ref=e15] [cursor=pointer]:
                  - /url: /unidades
                  - generic [ref=e16]: 
                  - text: Unidades
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
                - link "ADMIN" [ref=e25] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e26]:
                    - generic [ref=e27]: 
                    - generic [ref=e28]: ADMIN
              - listitem "Notificações" [ref=e29]:
                - link "Notificações" [ref=e30] [cursor=pointer]:
                  - /url: /administracao/notificacoes
                  - generic [ref=e31]: Notificações
                  - generic [ref=e32]: 
              - listitem "Configurações" [ref=e33]:
                - link "Configurações" [ref=e34] [cursor=pointer]:
                  - /url: /configuracoes
                  - generic [ref=e35]: Configurações
                  - generic [ref=e36]: 
              - listitem "Administradores do sistema" [ref=e37]:
                - link "Administradores" [ref=e38] [cursor=pointer]:
                  - /url: /administradores
                  - generic [ref=e39]: Administradores
                  - generic [ref=e40]: 
              - listitem [ref=e41]:
                - generic [ref=e42]:
                  - button "Ações Especiais" [ref=e43] [cursor=pointer]:
                    - generic [ref=e44]: Ações Especiais
                    - generic [ref=e45]: 
                  - text: 
              - listitem "Sair" [ref=e46]:
                - link "Sair" [ref=e47] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e48]: Sair
                  - generic [ref=e49]: 
      - generic [ref=e52]:
        - button "Voltar" [ref=e53] [cursor=pointer]:
          - generic [ref=e54]: 
        - navigation "breadcrumb" [ref=e55]:
          - list [ref=e56]:
            - listitem [ref=e57]:
              - link "Início" [ref=e58] [cursor=pointer]:
                - /url: /painel
                - generic [ref=e59]: 
                - generic [ref=e60]: Início
            - listitem [ref=e61]:
              - text: ›
              - link "Detalhes do processo" [ref=e62] [cursor=pointer]:
                - /url: /processo/400
            - listitem [ref=e63]:
              - text: ›
              - generic [ref=e64]: ASSESSORIA_12
      - main [ref=e65]:
        - generic [ref=e67]:
          - generic [ref=e68]:
            - generic [ref=e69]:
              - generic [ref=e70]:
                - heading "ASSESSORIA_12" [level=2] [ref=e71]
                - paragraph [ref=e72]: Assessoria 12
              - generic [ref=e74]:
                - button "Ações" [active] [ref=e75] [cursor=pointer]
                - text:  
            - generic [ref=e77]:
              - paragraph [ref=e78]:
                - strong [ref=e79]: "Processo:"
                - text: Processo CDU-06 1777767104359
              - paragraph [ref=e80]: Situação:Não iniciado
              - paragraph [ref=e81]: Localização atual:ASSESSORIA_12
              - paragraph [ref=e82]: Prazo para conclusão da etapa atual:01/06/2026
              - paragraph [ref=e83]:
                - strong [ref=e84]: "Titular:"
                - text: Axl Rose
              - paragraph [ref=e85]:
                - generic [ref=e86]:
                  - generic [ref=e87]: 
                  - text: "2004"
                - generic [ref=e88]:
                  - generic [ref=e89]: 
                  - link "axl.rose@tre-pe.jus.br" [ref=e90] [cursor=pointer]:
                    - /url: mailto:axl.rose@tre-pe.jus.br
          - generic [ref=e91]:
            - generic [ref=e95]:
              - heading "Atividades e conhecimentos" [level=4] [ref=e96]
              - paragraph [ref=e97]: Cadastro de atividades e conhecimentos da unidade
            - generic [ref=e101]:
              - heading "Mapa de competências" [level=4] [ref=e102]
              - paragraph [ref=e103]: Mapa de competências técnicas da unidade
          - generic [ref=e104]:
            - heading "Movimentações" [level=4] [ref=e105]
            - table [ref=e107]:
              - rowgroup [ref=e108]:
                - row "Data/hora Origem Destino Descrição" [ref=e109]:
                  - columnheader "Data/hora" [ref=e110]
                  - columnheader "Origem" [ref=e111]
                  - columnheader "Destino" [ref=e112]
                  - columnheader "Descrição" [ref=e113]
              - rowgroup [ref=e114]:
                - row "02/05/2026 21:11 ADMIN ASSESSORIA_12 Processo iniciado" [ref=e115]:
                  - cell "02/05/2026 21:11" [ref=e116]
                  - cell "ADMIN" [ref=e117]
                  - cell "ASSESSORIA_12" [ref=e118]
                  - cell "Processo iniciado" [ref=e119]
      - contentinfo [ref=e120]:
        - generic [ref=e121]:
          - generic [ref=e122]: Versão 1.0.0
          - generic [ref=e123]: © SESEL/COSIS/TRE-PE
  - text: 
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
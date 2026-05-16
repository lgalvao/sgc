# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: jornada.spec.ts >> Jornada do Ciclo de Vida Completo do SGC >> Fase 0: Administração de Notificações
- Location: e2e/jornada.spec.ts:20:5

# Error details

```
Test timeout of 120000ms exceeded.
```

```
Error: page.waitForURL: Test timeout of 120000ms exceeded.
=========================== logs ===========================
waiting for navigation until "load"
============================================================
```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - generic [ref=e5]:
      - navigation [ref=e7]:
        - generic [ref=e8]:
          - link "SGC" [ref=e9] [cursor=pointer]:
            - /url: /painel
          - generic [ref=e10]:
            - list [ref=e11]:
              - listitem [ref=e12]:
                - link "Painel" [ref=e13] [cursor=pointer]:
                  - /url: /painel
                  - generic [ref=e14]: 
                  - text: Painel
              - listitem [ref=e15]:
                - link "Unidades" [ref=e16] [cursor=pointer]:
                  - /url: /unidades
                  - generic [ref=e17]: 
                  - text: Unidades
              - listitem [ref=e18]:
                - link "Relatórios" [ref=e19] [cursor=pointer]:
                  - /url: /relatorios
                  - generic [ref=e20]: 
                  - text: Relatórios
              - listitem [ref=e21]:
                - link "Histórico" [ref=e22] [cursor=pointer]:
                  - /url: /historico
                  - generic [ref=e23]: 
                  - text: Histórico
            - list [ref=e24]:
              - listitem [ref=e25]:
                - link "ADMIN" [ref=e26] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e27]:
                    - generic [ref=e28]: 
                    - generic [ref=e29]: ADMIN
              - listitem "Notificações" [ref=e30]:
                - link "Notificações" [ref=e31] [cursor=pointer]:
                  - /url: /administracao/notificacoes
                  - generic [ref=e32]: Notificações
                  - generic [ref=e33]: 
              - listitem "Configurações" [ref=e34]:
                - link "Configurações" [ref=e35] [cursor=pointer]:
                  - /url: /configuracoes
                  - generic [ref=e36]: Configurações
                  - generic [ref=e37]: 
              - listitem "Administradores do sistema" [ref=e38]:
                - link "Administradores" [ref=e39] [cursor=pointer]:
                  - /url: /administradores
                  - generic [ref=e40]: Administradores
                  - generic [ref=e41]: 
              - listitem [ref=e42]:
                - generic [ref=e43]:
                  - button "Ações Especiais" [ref=e44] [cursor=pointer]:
                    - generic [ref=e45]: Ações Especiais
                    - generic [ref=e46]: 
                  - text:  
              - listitem "Ativar modo escuro" [ref=e47]:
                - link "Ativar modo escuro" [ref=e48] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e49]: Ativar modo escuro
                  - generic [ref=e50]: 
              - listitem "Sair" [ref=e51]:
                - link "Sair" [ref=e52] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e53]: Sair
                  - generic [ref=e54]: 
      - main [ref=e55]:
        - generic [ref=e56]:
          - heading "Painel" [level=1] [ref=e57]
          - generic [ref=e58]:
            - generic [ref=e59]:
              - heading "Processos" [level=2] [ref=e61]
              - button "Criar processo" [ref=e63] [cursor=pointer]:
                - generic [ref=e64]: 
                - text: Criar processo
            - generic "Lista de processos cadastrados" [ref=e66]:
              - table [ref=e67]:
                - rowgroup [ref=e68]:
                  - row "Descrição Click to sort descending Tipo Click to sort ascending Unidades Situação Click to sort ascending" [ref=e69]:
                    - columnheader "Descrição Click to sort descending" [ref=e70] [cursor=pointer]:
                      - text: Descrição
                      - generic [ref=e71]: Click to sort descending
                    - columnheader "Tipo Click to sort ascending" [ref=e72] [cursor=pointer]:
                      - text: Tipo
                      - generic [ref=e73]: Click to sort ascending
                    - columnheader "Unidades" [ref=e74]
                    - columnheader "Situação Click to sort ascending" [ref=e75] [cursor=pointer]:
                      - text: Situação
                      - generic [ref=e76]: Click to sort ascending
                - rowgroup [ref=e77]:
                  - row "Mapeamento Secão 311 Mapeamento COORD_31 Em andamento" [ref=e78] [cursor=pointer]:
                    - cell "Mapeamento Secão 311" [ref=e79]
                    - cell "Mapeamento" [ref=e80]
                    - cell "COORD_31" [ref=e81]
                    - cell "Em andamento" [ref=e82]:
                      - generic [ref=e83]: Em andamento
                  - row "Mapeamento Secão 321 Mapeamento SECAO_321 Em andamento" [ref=e84] [cursor=pointer]:
                    - cell "Mapeamento Secão 321" [ref=e85]
                    - cell "Mapeamento" [ref=e86]
                    - cell "SECAO_321" [ref=e87]
                    - cell "Em andamento" [ref=e88]:
                      - generic [ref=e89]: Em andamento
                  - row "Processo 99 Mapeamento ASSESSORIA_12 Finalizado" [ref=e90] [cursor=pointer]:
                    - cell "Processo 99" [ref=e91]
                    - cell "Mapeamento" [ref=e92]
                    - cell "ASSESSORIA_12" [ref=e93]
                    - cell "Finalizado" [ref=e94]:
                      - generic [ref=e95]: Finalizado
                  - row "Processo Seed 200 Mapeamento SECRETARIA_1 Finalizado" [ref=e96] [cursor=pointer]:
                    - cell "Processo Seed 200" [ref=e97]
                    - cell "Mapeamento" [ref=e98]
                    - cell "SECRETARIA_1" [ref=e99]
                    - cell "Finalizado" [ref=e100]:
                      - generic [ref=e101]: Finalizado
          - generic [ref=e102]:
            - heading "Alertas" [level=2] [ref=e105]
            - generic "Alertas" [ref=e107]:
              - table [ref=e108]:
                - rowgroup [ref=e109]:
                  - row "Data/Hora Descrição Processo Origem" [ref=e110]:
                    - columnheader "Data/Hora" [ref=e111]
                    - columnheader "Descrição" [ref=e112]
                    - columnheader "Processo" [ref=e113]
                    - columnheader "Origem" [ref=e114]
                - rowgroup [ref=e115]:
                  - 'row "15/05/2026 02:31 Não lido: Mapa de competências homologado Processo 99 SECRETARIA_1" [ref=e116]':
                    - cell "15/05/2026 02:31" [ref=e117]
                    - 'cell "Não lido: Mapa de competências homologado" [ref=e118]':
                      - generic [ref=e119]: "Não lido:"
                      - text: Mapa de competências homologado
                    - cell "Processo 99" [ref=e120]
                    - cell "SECRETARIA_1" [ref=e121]
                  - 'row "14/05/2026 21:31 Não lido: Cadastro homologado Mapeamento Secão 311 ADMIN" [ref=e122]':
                    - cell "14/05/2026 21:31" [ref=e123]
                    - 'cell "Não lido: Cadastro homologado" [ref=e124]':
                      - generic [ref=e125]: "Não lido:"
                      - text: Cadastro homologado
                    - cell "Mapeamento Secão 311" [ref=e126]
                    - cell "ADMIN" [ref=e127]
                  - 'row "14/05/2026 03:31 Não lido: Mapa de competências homologado Processo Seed 200 SECRETARIA_1" [ref=e128]':
                    - cell "14/05/2026 03:31" [ref=e129]
                    - 'cell "Não lido: Mapa de competências homologado" [ref=e130]':
                      - generic [ref=e131]: "Não lido:"
                      - text: Mapa de competências homologado
                    - cell "Processo Seed 200" [ref=e132]
                    - cell "SECRETARIA_1" [ref=e133]
      - contentinfo [ref=e134]:
        - generic [ref=e135]:
          - generic [ref=e136]: Versão 1.0.4
          - generic [ref=e137]: © SESEL/COSIS/TRE-PE
  - button "Enviar feedback" [ref=e138] [cursor=pointer]:
    - generic [ref=e139]: 
  - text:    
```

# Test source

```ts
  37  |                 ) {
  38  |                     continue;
  39  |                 }
  40  |                 throw e;
  41  |             }
  42  |         }
  43  |     } catch (e: any) {
  44  |         // Ignorar erros se a página ou contexto foram fechados durante a limpeza (comum em timeouts)
  45  |         if (e.message?.includes('closed') || e.message?.includes('Target page, context or browser has been closed')) {
  46  |             return;
  47  |         }
  48  |         throw e;
  49  |     }
  50  | }
  51  | 
  52  | /**
  53  |  * Verifica toast exibido pelo BOrchestrator (notificação transitória).
  54  |  * Use em fluxos com navegação após ação mutante.
  55  |  */
  56  | export async function verificarToast(page: Page, mensagem?: string | RegExp) {
  57  |     const toast = page.locator('.orchestrator-container .toast').first();
  58  |     await expect(toast).toBeVisible();
  59  |     if (mensagem) {
  60  |         await expect(toast).toContainText(mensagem);
  61  |     }
  62  | }
  63  | 
  64  | /**
  65  |  * Verifica alerta inline do componente AppAlert.
  66  |  * Use em fluxos sem navegação (erros/avisos persistentes na própria tela).
  67  |  */
  68  | export async function verificarAppAlert(page: Page, mensagem?: string | RegExp): Promise<void> {
  69  |     const alerta = page.getByTestId('app-alert').first();
  70  |     await expect(alerta).toBeVisible();
  71  |     if (mensagem) {
  72  |         await expect(alerta).toContainText(mensagem);
  73  |     }
  74  | }
  75  | 
  76  | /**
  77  |  * Verifica mensagem persistente na tabela de alertas do painel.
  78  |  */
  79  | export async function verificarAlertaPainel(page: Page, mensagem: string | RegExp): Promise<void> {
  80  |     const tabelaAlertas = page.getByTestId('tbl-alertas');
  81  |     await expect(tabelaAlertas).toBeVisible();
  82  |     await expect(tabelaAlertas).toContainText(mensagem);
  83  | }
  84  | 
  85  | /**
  86  |  * Faz logout do sistema clicando no link "Sair".
  87  |  */
  88  | export async function fazerLogout(page: Page): Promise<void> {
  89  |     try {
  90  |         if (/\/login(?:\?.*)?$/.test(page.url())) {
  91  |             return;
  92  |         }
  93  | 
  94  |         // Limpar notificações que possam estar sobrepondo o menu ou botões
  95  |         await limparNotificacoes(page);
  96  | 
  97  |         const candidatosLogout = [
  98  |             page.getByTestId('btn-logout'),
  99  |             page.getByTitle('Sair'),
  100 |             page.getByRole('link', {name: /^sair$/i}),
  101 |             page.getByRole('button', {name: /^sair$/i})
  102 |         ];
  103 | 
  104 |         let botaoLogout: Locator | null = null;
  105 |         for (const candidato of candidatosLogout) {
  106 |             if (await candidato.count() > 0 && await candidato.first().isVisible()) {
  107 |                 botaoLogout = candidato.first();
  108 |                 break;
  109 |             }
  110 |         }
  111 | 
  112 |         if (!botaoLogout) {
  113 |             await page.waitForURL(/\/login(?:\?.*)?$/, {timeout: 2_000}).catch(() => null);
  114 |             if (/\/login(?:\?.*)?$/.test(page.url())) {
  115 |                 return;
  116 |             }
  117 |             throw new Error('Botão de logout não encontrado na navegação atual.');
  118 |         }
  119 | 
  120 |         await botaoLogout.scrollIntoViewIfNeeded();
  121 | 
  122 |         try {
  123 |             await botaoLogout.click({timeout: 2_000});
  124 |         } catch (e: any) {
  125 |             const mensagem = (e.message ?? '').toLowerCase();
  126 |             if (
  127 |                 mensagem.includes('intercepts pointer events')
  128 |                 || mensagem.includes('another element would receive the click')
  129 |                 || mensagem.includes('timeout')
  130 |             ) {
  131 |                 await limparNotificacoes(page);
  132 |                 await botaoLogout.click({force: true, timeout: 2_000});
  133 |             } else {
  134 |                 throw e;
  135 |             }
  136 |         }
> 137 |         await page.waitForURL(/\/login/);
      |                    ^ Error: page.waitForURL: Test timeout of 120000ms exceeded.
  138 | 
  139 |         // Limpar possíveis toasts de "Não autorizado" ou "Sessão expirada" que aparecem no teardown após o logout
  140 |         await limparNotificacoes(page);
  141 |     } catch (e: any) {
  142 |         if (e.message?.includes('closed') || e.message?.includes('Target page, context or browser has been closed')) {
  143 |             return;
  144 |         }
  145 |         throw e;
  146 |     }
  147 | }
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
  237 |     await expect(dropdown).toBeVisible();
```
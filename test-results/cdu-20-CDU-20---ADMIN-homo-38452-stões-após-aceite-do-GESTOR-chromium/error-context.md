# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-20.spec.ts >> CDU-20 - ADMIN homologa mapa após GESTOR aceitar com sugestões (Bug #1376) >> ADMIN vê ações corretas quando o mapa permanece com sugestões após aceite do GESTOR
- Location: e2e/cdu-20.spec.ts:319:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('btn-mapa-acao-homologar-aceite')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('btn-mapa-acao-homologar-aceite')

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
              - link "ASSESSORIA_11" [ref=e64] [cursor=pointer]:
                - /url: /processo/400/ASSESSORIA_11
            - listitem [ref=e65]:
              - text: ›
              - generic [ref=e66]: Mapa de competências
      - main [ref=e67]:
        - generic [ref=e68]:
          - generic [ref=e69]:
            - generic [ref=e70]:
              - heading "Mapa de competências técnicas" [level=2] [ref=e71]
              - paragraph [ref=e72]:
                - generic [ref=e73]: ASSESSORIA_11
            - generic [ref=e75]:
              - button "Ver sugestões" [ref=e76] [cursor=pointer]
              - button "Histórico de análise" [ref=e77] [cursor=pointer]
              - generic [ref=e78]:
                - button "Ações" [expanded] [active] [ref=e79] [cursor=pointer]
                - menu "Ações" [ref=e80]:
                  - menu [ref=e81] [cursor=pointer]: Disponibilizar
                  - menu [ref=e82] [cursor=pointer]: Devolver para ajustes
          - generic [ref=e83]:
            - button "Criar competência" [ref=e85] [cursor=pointer]:
              - generic [ref=e86]: 
              - text: Criar competência
            - generic [ref=e88]:
              - generic [ref=e89]:
                - heading "Competência fixture - 400" [level=4] [ref=e90]:
                  - strong [ref=e91]: Competência fixture - 400
                - generic [ref=e92]:
                  - button "Editar competência Competência fixture - 400" [ref=e93] [cursor=pointer]:
                    - generic [ref=e94]: 
                  - button "Excluir competência Competência fixture - 400" [ref=e95] [cursor=pointer]:
                    - generic [ref=e96]: 
              - generic [ref=e100]:
                - generic [ref=e101]:
                  - generic [ref=e102]: Atividade fixture - 400
                  - button "Remover atividade Atividade fixture - 400" [ref=e103] [cursor=pointer]:
                    - generic [ref=e104]: 
                - list [ref=e106]:
                  - listitem [ref=e107]: Conhecimento fixture - 400
      - contentinfo [ref=e108]:
        - generic [ref=e109]:
          - generic [ref=e110]: Versão 1.0.0
          - generic [ref=e111]: © SESEL/COSIS/TRE-PE
  - text:   
```

# Test source

```ts
  232 |         await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  233 |         await expect(page.getByTestId('tbl-movimentacoes')).toBeVisible();
  234 |         await expect(page.getByRole('columnheader', {name: 'Data/hora'})).toBeVisible();
  235 |         await expect(page.getByRole('columnheader', {name: 'Origem'})).toBeVisible();
  236 |         await expect(page.getByRole('columnheader', {name: 'Destino'})).toBeVisible();
  237 |         await expect(page.getByRole('columnheader', {name: 'Descrição'})).toBeVisible();
  238 | 
  239 |         const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
  240 |             .locator('tr', {hasText: 'Devolução da validação do mapa de competências para ajustes'})
  241 |             .first();
  242 |         await expect(linhaMovimentacao).toBeVisible();
  243 | 
  244 |         const dataHora = await linhaMovimentacao.locator('td').nth(0).innerText();
  245 |         validarDataHoraBrasileira(dataHora);
  246 |         await expect(linhaMovimentacao.locator('td').nth(1)).toHaveText('SECRETARIA_1');
  247 |         await expect(linhaMovimentacao.locator('td').nth(2)).toHaveText(UNIDADE_ALVO);
  248 | 
  249 |         await navegarParaMapa(page);
  250 |         await esperarMapaSomenteLeitura(page);
  251 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  252 | 
  253 |     });
  254 | });
  255 | 
  256 | test.describe.serial('CDU-20 - ADMIN deve ver botões de edição com mapa com sugestões (Bug #1376)', () => {
  257 |     const UNIDADE_ALVO = 'ASSESSORIA_11';
  258 | 
  259 |     const timestamp = Date.now();
  260 |     const descProcesso = `Processo CDU-20 Bug1376 EditarMapa ${timestamp}`;
  261 | 
  262 |     test('Setup data', async ({_resetAutomatico, request}) => {
  263 |         await resetDatabase(request);
  264 |         const processo = await criarProcessoMapaComSugestoesFixture(request, {
  265 |             unidade: UNIDADE_ALVO,
  266 |             descricao: descProcesso
  267 |         });
  268 |         validarProcessoFixture(processo, descProcesso);
  269 |     });
  270 | 
  271 |     test('ADMIN vê card de edição de mapa quando situação é Mapa com sugestões', async ({_resetAutomatico, page}) => {
  272 |         await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  273 |         await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
  274 | 
  275 |         await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  276 |         await navegarParaMapa(page);
  277 |         await esperarMapaEditavel(page);
  278 |     });
  279 | });
  280 | 
  281 | test.describe.serial('CDU-20 - ADMIN homologa mapa após GESTOR aceitar com sugestões (Bug #1376)', () => {
  282 |     const UNIDADE_ALVO = 'ASSESSORIA_11';
  283 |     const TEXTO_SUGESTAO = 'Sugestão para ajuste no mapa - Bug 1376';
  284 | 
  285 |     const timestamp = Date.now();
  286 |     const descProcesso = `Processo CDU-20 Bug1376 Homologar ${timestamp}`;
  287 | 
  288 |     test('Setup data', async ({_resetAutomatico, request}) => {
  289 |         await resetDatabase(request);
  290 |         const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
  291 |             unidade: UNIDADE_ALVO,
  292 |             descricao: descProcesso
  293 |         });
  294 |         validarProcessoFixture(processo, descProcesso);
  295 |     });
  296 | 
  297 |     test('CHEFE apresenta sugestões e GESTOR registra aceite', async ({_resetAutomatico, page}) => {
  298 |         await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
  299 |         await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
  300 |         await navegarParaMapa(page);
  301 | 
  302 |         await abrirSugestoesMapa(page);
  303 |         await expect(page.getByRole('dialog')).toBeVisible();
  304 |         await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
  305 |         await page.getByTestId('btn-sugestoes-mapa-confirmar').click();
  306 |         await verificarPaginaPainel(page);
  307 | 
  308 |         await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
  309 |         await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
  310 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
  311 |         await navegarParaMapa(page);
  312 | 
  313 |         await abrirAcaoPrincipalMapa(page);
  314 |         await page.getByTestId('btn-aceite-mapa-confirmar').click();
  315 |         await verificarPaginaPainel(page);
  316 |         await expect(page.getByText(TEXTOS.sucesso.ACEITE_REGISTRADO).first()).toBeVisible();
  317 |     });
  318 | 
  319 |     test('ADMIN vê ações corretas quando o mapa permanece com sugestões após aceite do GESTOR', async ({_resetAutomatico, page}) => {
  320 |         await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  321 |         await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
  322 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
  323 |         await navegarParaMapa(page);
  324 | 
  325 |         await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  326 |         await page.getByTestId('btn-mapa-acoes').click();
  327 | 
  328 |         await expect(page.getByTestId('btn-mapa-acao-disponibilizar')).toBeVisible();
  329 |         await expect(page.getByTestId('btn-mapa-acao-disponibilizar')).toBeEnabled();
  330 |         await expect(page.getByTestId('btn-mapa-acao-devolver')).toBeVisible();
  331 |         await expect(page.getByTestId('btn-mapa-acao-devolver')).toBeEnabled();
> 332 |         await expect(page.getByTestId('btn-mapa-acao-homologar-aceite')).toBeVisible();
      |                                                                          ^ Error: expect(locator).toBeVisible() failed
  333 |         await expect(page.getByTestId('btn-mapa-acao-homologar-aceite')).toBeDisabled();
  334 |     });
  335 | });
  336 | 
```
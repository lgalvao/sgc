# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: jornada.spec.ts >> Jornada do Ciclo de Vida Completo do SGC >> Fase 3: Ciclo de Revisão e Manutenção
- Location: e2e/jornada.spec.ts:37:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('card-subprocesso-mapa')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('card-subprocesso-mapa')

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - main [ref=e6]:
      - generic [ref=e10]:
        - heading "SGC" [level=1] [ref=e11]
        - paragraph [ref=e12]: Sistema de Gestão de Competências
        - generic [ref=e13]:
          - group [ref=e14]:
            - generic [ref=e15]:
              - generic [ref=e16]: 
              - text: Título eleitoral *
            - group [ref=e17]:
              - textbox "Título eleitoral" [active] [ref=e18]:
                - /placeholder: Digite seu título
          - group [ref=e19]:
            - generic [ref=e20]:
              - generic [ref=e21]: 
              - text: Senha *
            - group [ref=e22]:
              - textbox "Senha" [ref=e23]:
                - /placeholder: Digite sua senha
              - button "Mostrar senha" [ref=e24] [cursor=pointer]:
                - generic [ref=e25]: 
          - button "Entrar" [ref=e26] [cursor=pointer]:
            - generic [ref=e27]: 
            - text: Entrar
  - button "Enviar feedback" [ref=e28] [cursor=pointer]:
    - generic [ref=e29]: 
  - text:              
```

# Test source

```ts
  336 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i);
  337 |             await MapaHelpers.navegarParaMapa(page);
  338 |             await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeHidden();
  339 |             await expect(page.getByText('Competência Técnica Básica')).toBeVisible();
  340 |         });
  341 |         await expect(page).toHaveURL(/\/login/);
  342 |     };
  343 | 
  344 |     const criarProcessoRevisaoAdmin = async (page: Page) => {
  345 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  346 |             await ProcessoHelpers.criarProcesso(page, {
  347 |                 descricao: descricaoRevisao,
  348 |                 tipo: 'REVISAO',
  349 |                 unidade: [siglaUnidade],
  350 |                 expandir: ['SECRETARIA_1'],
  351 |                 iniciar: true
  352 |             });
  353 |         });
  354 |         await expect(page).toHaveURL(/\/login/);
  355 |     };
  356 | 
  357 |     const realizarRevisaoChefe = async (page: Page) => {
  358 |         await AuthHelpers.executarComo(page, CHEFE, async () => {
  359 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoRevisao, siglaUnidade);
  360 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  361 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  362 |             await AtividadeHelpers.navegarParaCadastro(page);
  363 |             const btnDisponibilizar = page.getByTestId('btn-cad-atividades-disponibilizar');
  364 |             await expect(btnDisponibilizar).toBeVisible();
  365 |             await expect(btnDisponibilizar).toBeDisabled();
  366 | 
  367 |             await expect(page.getByText('Atividade 1')).toBeVisible();
  368 |             // Verificar botão de impacto (mostra diferenças em relação ao mapa vigente)
  369 |             const btnImpacto = page.getByTestId('cad-atividades__btn-impactos-mapa-edicao');
  370 |             if (await btnImpacto.isVisible()) {
  371 |                 await btnImpacto.click();
  372 |                 const modalImpacto = page.getByRole('dialog');
  373 |                 await expect(modalImpacto).toBeVisible();
  374 |                 await page.getByTestId('btn-fechar-impacto').click();
  375 |                 await expect(modalImpacto).toBeHidden();
  376 |             }
  377 | 
  378 |             await AtividadeHelpers.adicionarConhecimento(page, 'Atividade 1', 'Conhecimento Revisado');
  379 |             await AtividadeHelpers.disponibilizarCadastro(page);
  380 | 
  381 |             await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoRevisao, siglaUnidade);
  382 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro disponibilizada/i);
  383 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  384 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  385 |         });
  386 |         await expect(page).toHaveURL(/\/login/);
  387 |     };
  388 | 
  389 |     const realizarAceiteRevisaoGestor = async (page: Page) => {
  390 |         await AuthHelpers.executarComo(page, GESTOR, async () => {
  391 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
  392 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  393 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  394 |             await AtividadeHelpers.navegarParaCadastro(page);
  395 |             await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
  396 |                 rotuloPrincipal: /Registrar aceite/i,
  397 |                 principalHabilitado: true,
  398 |                 devolverHabilitado: true
  399 |             });
  400 |             await AnaliseHelpers.aceitarRevisao(page, 'Revisão aceita.');
  401 | 
  402 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
  403 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
  404 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  405 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  406 |         });
  407 |         await expect(page).toHaveURL(/\/login/);
  408 |     };
  409 | 
  410 |     const homologarRevisaoAdmin = async (page: Page) => {
  411 |         await AuthHelpers.executarComo(page, ADMIN, async () => {
  412 |             await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoRevisao, siglaUnidade);
  413 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  414 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  415 |             await AtividadeHelpers.navegarParaCadastro(page);
  416 |             await AnaliseHelpers.verificarAcoesAnaliseCadastro(page, {
  417 |                 rotuloPrincipal: /Homologar/i,
  418 |                 principalHabilitado: true,
  419 |                 devolverHabilitado: true
  420 |             });
  421 |             await AnaliseHelpers.homologarCadastroMapeamento(page, 'Revisão homologada. Ciclo de manutenção completo.');
  422 | 
  423 |             await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  424 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro homologada/i);
  425 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  426 |             await expect(page.getByTestId('card-subprocesso-mapa')).toContainText('Mapa de competências técnicas da unidade');
  427 |             await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  428 |             await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  429 |         });
  430 |         await expect(page).toHaveURL(/\/login/);
  431 | 
  432 |         // GESTOR confere que o mapa da revisão permanece somente leitura
  433 |         await AuthHelpers.executarComo(page, GESTOR, async () => {
  434 |             await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
  435 |             await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão do cadastro homologada/i);
> 436 |             await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
      |                                                                     ^ Error: expect(locator).toBeVisible() failed
  437 |         });
  438 |         await expect(page).toHaveURL(/\/login/);
  439 |     };
  440 | 
  441 | });
  442 | 
```
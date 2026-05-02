# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 02 - Painel principal >> Captura painel ADMIN
- Location: e2e/captura.spec.ts:453:9

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-arvore-expand-SECRETARIA_1')

```

# Test source

```ts
  373 |         }
  374 |     });
  375 | 
  376 |     if (!response.ok()) {
  377 |         throw new Error(`Falha ao criar fixture de mapa homologado: ${response.status()} ${await response.text()}`);
  378 |     }
  379 |     const processo = await response.json() as { codigo: number };
  380 |     cleanup.registrar(processo.codigo);
  381 |     return processo.codigo;
  382 | }
  383 | 
  384 | test.describe('Captura de Telas - Sistema SGC', () => {
  385 |     let cleanup: ReturnType<typeof useProcessoCleanup>;
  386 | 
  387 |     test.beforeAll(async ({request}) => {
  388 |         await resetDatabase(request);
  389 |     });
  390 | 
  391 |     test.beforeEach(async () => {
  392 |         cleanup = useProcessoCleanup();
  393 |     });
  394 | 
  395 |     test.afterEach(async ({request}) => {
  396 |         if (cleanup) {
  397 |             await cleanup.limpar(request);
  398 |         }
  399 |     });
  400 | 
  401 |     test.afterAll(async () => {
  402 |         if (capturasMetadata.length > 0) {
  403 |             fs.writeFileSync(METADATA_PATH, JSON.stringify(montarDocumentoCapturasMetadata(), null, 2));
  404 |         }
  405 |     });
  406 | 
  407 |     test.describe('01 - Autenticação', () => {
  408 |         test('Captura telas de login', async ({page}) => {
  409 |             await page.goto('/login');
  410 |             // Tela de login inicial
  411 |             await capturarTela(page, 'seguranca', 'login-inicial', {
  412 |                 tags: ['login', 'inicial']
  413 |             });
  414 |             await page.getByTestId('inp-login-usuario').click();
  415 |             await capturarTela(page, 'seguranca', 'login-campo-usuario-foco', {
  416 |                 tags: ['login', 'interacao']
  417 |             });
  418 | 
  419 |             // Erro de credenciais inválidas
  420 |             await page.getByTestId('inp-login-usuario').fill(USUARIOS.INVALIDO.titulo);
  421 |             await capturarTela(page, 'seguranca', 'login-usuario-preenchido', {
  422 |                 tags: ['login', 'preenchimento']
  423 |             });
  424 |             await page.getByTestId('inp-login-senha').fill(USUARIOS.INVALIDO.senha);
  425 |             await page.getByTestId('btn-login-entrar').click();
  426 |             await verificarAppAlert(page);
  427 |             await capturarTela(page, 'seguranca', 'login-erro-credenciais', {
  428 |                 tags: ['login', 'erro', 'validacao'],
  429 |                 extra: { erro: 'Credenciais inválidas' }
  430 |             });
  431 | 
  432 |             // Limpar e fazer login com múltiplos perfis
  433 |             await page.getByTestId('inp-login-usuario').fill(USUARIOS.ADMIN_2_PERFIS.titulo);
  434 |             await page.getByTestId('inp-login-senha').fill(USUARIOS.ADMIN_2_PERFIS.senha);
  435 |             await page.getByTestId('btn-login-entrar').click();
  436 |             await expect(page.getByTestId('sel-login-perfil')).toBeVisible();
  437 |             await capturarTela(page, 'seguranca', 'login-selecao-perfil', {
  438 |                 tags: ['login', 'multi-perfil'],
  439 |                 extra: { usuario: USUARIOS.ADMIN_2_PERFIS.titulo }
  440 |             });
  441 | 
  442 |             // Login com perfil selecionado
  443 |             await loginComPerfil(page, USUARIOS.ADMIN_2_PERFIS.titulo, USUARIOS.ADMIN_2_PERFIS.senha, USUARIOS.ADMIN_2_PERFIS.perfil);
  444 |             await capturarTela(page, 'seguranca', 'painel-apos-login', {
  445 |                 fullPage: true,
  446 |                 tags: ['login', 'sucesso', 'dashboard'],
  447 |                 extra: { perfil: USUARIOS.ADMIN_2_PERFIS.perfil }
  448 |             });
  449 |         });
  450 |     });
  451 | 
  452 |     test.describe('02 - Painel principal', () => {
  453 |         test('Captura painel ADMIN', async ({page}) => {
  454 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  455 | 
  456 |             // Criar um processo para popular o painel
  457 |             const descricaoProcesso = `Processo captura ${Date.now()}`;
  458 |             await page.getByTestId('btn-painel-criar-processo').click();
  459 |             await expect(page).toHaveURL(/\/processo\/cadastro/);
  460 |             await capturarTela(page, 'painel', 'criar-processo-form-vazio', {
  461 |                 extra: { perfil: 'ADMIN' },
  462 |                 tags: ['form', 'vazio']
  463 |             });
  464 | 
  465 |             await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
  466 |             await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
  467 |             const dataLimite = new Date();
  468 |             dataLimite.setDate(dataLimite.getDate() + 30);
  469 |             await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
  470 | 
  471 |             // Expandir árvore de unidades
  472 |             await expect(page.getByText('Carregando unidades...')).toBeHidden();
> 473 |             await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
      |                                                                      ^ Error: locator.click: Target page, context or browser has been closed
  474 |             await expect(page.getByTestId('btn-arvore-expand-COORD_11')).toBeVisible();
  475 |             await capturarTela(page, 'painel', 'arvore-unidades-expandida', {
  476 |                 extra: { perfil: 'ADMIN', acao: 'expandir-arvore' }
  477 |             });
  478 | 
  479 |             // Expandir COORD_11 para acessar SECAO_111
  480 |             await page.getByTestId('btn-arvore-expand-COORD_11').click();
  481 |             await expect(page.getByTestId('chk-arvore-unidade-SECAO_111')).toBeVisible();
  482 | 
  483 |             // Selecionar múltiplas unidades
  484 |             await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
  485 |             await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click();
  486 |             await page.getByTestId('chk-arvore-unidade-SECAO_111').click();
  487 |             await capturarTela(page, 'painel', 'arvore-unidades-selecionada', {
  488 |                 tags: ['selecao', 'unidades']
  489 |             });
  490 | 
  491 |             await page.getByTestId('btn-processo-salvar-rodape').click();
  492 |             await expect(page).toHaveURL(/\/painel/);
  493 |             await expect(page.getByTestId('tbl-processos')).toBeVisible();
  494 |             await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();
  495 | 
  496 |             // Capturar ID para cleanup
  497 |             await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();
  498 |             await expect(page).toHaveURL(/codProcesso=\d+/);
  499 |             const codProcesso = await extrairProcessoCodigo(page);
  500 |             registrarProcessoParaCleanup(cleanup, codProcesso);
  501 |             await page.goto('/painel');
  502 | 
  503 |             // Painel com processo criado
  504 |             await capturarTela(page, 'painel', 'painel-admin-com-processo', {
  505 |                 fullPage: true,
  506 |                 extra: { perfil: 'ADMIN' }
  507 |             });
  508 | 
  509 |             // Hover em linha da tabela
  510 |             await page.getByText(descricaoProcesso).hover();
  511 |             await capturarTela(page, 'painel', 'painel-hover-processo', {
  512 |                 tags: ['interacao', 'hover']
  513 |             });
  514 |         });
  515 | 
  516 |         test('Captura painel GESTOR', async ({page}) => {
  517 |             // Criar processo para o Gestor primeiro como ADMIN
  518 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  519 |             const desc = `Processo gestor ${Date.now()}`;
  520 |             await criarProcesso(page, {
  521 |                 descricao: desc,
  522 |                 tipo: 'MAPEAMENTO',
  523 |                 diasLimite: 30,
  524 |                 unidade: 'COORD_11', // Unidade do Gestor COORD_11
  525 |                 expandir: ['SECRETARIA_1'],
  526 |                 iniciar: true
  527 |             });
  528 | 
  529 |             await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
  530 |             await expect(page.getByTestId('tbl-processos')).toBeVisible();
  531 |             await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
  532 |             await capturarTela(page, 'painel', 'painel-gestor', {
  533 |                 fullPage: true,
  534 |                 extra: { perfil: 'GESTOR', unidade: 'COORD_11' }
  535 |             });
  536 |         });
  537 | 
  538 |         test('Captura painel CHEFE', async ({page, request}) => {
  539 |             const desc = `Processo chefe ${Date.now()}`;
  540 |             await criarProcessoMapeamentoIniciadoPorFixture(request, cleanup, desc, 'SECAO_211');
  541 | 
  542 |             await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
  543 |             await expect(page.getByTestId('tbl-processos')).toBeVisible();
  544 |             await expect(page.getByTestId('tbl-processos').getByText(desc).first()).toBeVisible();
  545 |             await capturarTela(page, 'painel', 'painel-chefe', {
  546 |                 fullPage: true,
  547 |                 extra: { perfil: 'CHEFE', unidade: 'SECAO_211' }
  548 |             });
  549 |         });
  550 |     });
  551 | 
  552 |     test.describe('03 - Fluxo de Processo', () => {
  553 |         test('Captura criação e detalhamento de processo', async ({page}) => {
  554 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  555 | 
  556 |             const timestamp = Date.now();
  557 |             const UNIDADE_ALVO = 'ASSESSORIA_12';
  558 |             const descricao = `Processo detalhado ${timestamp}`;
  559 | 
  560 |             await criarProcesso(page, {
  561 |                 descricao,
  562 |                 tipo: 'MAPEAMENTO',
  563 |                 diasLimite: 30,
  564 |                 unidade: UNIDADE_ALVO,
  565 |                 expandir: ['SECRETARIA_1']
  566 |             });
  567 | 
  568 |             // Capturar ID para cleanup
  569 |             await page.getByTestId('tbl-processos').getByText(descricao).first().click();
  570 |             const codProcesso = await extrairProcessoCodigo(page);
  571 |             registrarProcessoParaCleanup(cleanup, codProcesso);
  572 | 
  573 |             // Tela de edição de processo
```
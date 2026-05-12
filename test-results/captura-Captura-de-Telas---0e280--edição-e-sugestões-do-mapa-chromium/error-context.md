# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 12 - Modais de Gestão do Mapa >> Captura modais de exclusão, edição e sugestões do mapa
- Location: e2e/captura.spec.ts:1527:9

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-cadastro-acao-principal')
    - locator resolved to <button disabled role="menu" type="button" class="dropdown-item disabled" data-testid="btn-cadastro-acao-principal">Homologar</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
      - waiting 100ms
    35 × waiting for element to be visible, enabled and stable
       - element is not enabled
     - retrying click action
       - waiting 500ms
    - waiting for element to be visible, enabled and stable

```

# Test source

```ts
  1441 |             await capturarTela(page, 'relatorios', 'botao-gerar-pdf', {tags: ['ui-element', 'pdf']});
  1442 |         });
  1443 |     });
  1444 | 
  1445 |     // SEÇÃO 11 - HISTÓRICO DE ANÁLISE E DEVOLUÇÃO DE CADASTRO
  1446 |     test.describe('11 - Histórico de Análise e Devolução', () => {
  1447 |         test('Captura modal de histórico e fluxo de devolução de cadastro', async ({page, request}) => {
  1448 |             const unidadeAlvo = 'SECAO_221';
  1449 |             const descricao = `Proc devolucao ${Date.now()}`;
  1450 | 
  1451 |             const processo = await criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
  1452 |                 request, cleanup, descricao, unidadeAlvo
  1453 |             );
  1454 | 
  1455 |             // Gestor acessa cadastro disponibilizado para ver o histórico
  1456 |             await login(
  1457 |                 page,
  1458 |                 USUARIOS.GESTOR_COORD_22.titulo,
  1459 |                 USUARIOS.GESTOR_COORD_22.senha
  1460 |             );
  1461 |             await page.goto(`/processo/${processo}/${unidadeAlvo}`);
  1462 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo}/${unidadeAlvo}(?:\?.*)?$`));
  1463 |             await navegarParaCadastro(page);
  1464 |             await capturarTela(page, 'historico-analise', 'cadastro-disponibilizado-gestor', {
  1465 |                 fullPage: true,
  1466 |                 tags: ['analise', 'historico']
  1467 |             });
  1468 | 
  1469 |             // Abrir modal de histórico de análise
  1470 |             const modalHistorico = await abrirHistoricoAnalise(page);
  1471 |             await expect(modalHistorico).toBeVisible();
  1472 |             await capturarTela(page, 'historico-analise', 'modal-historico-analise', {
  1473 |                 tags: ['modal', 'historico'],
  1474 |                 extra: {perfil: 'GESTOR'}
  1475 |             });
  1476 |             await fecharHistoricoAnalise(page);
  1477 | 
  1478 |             // Iniciar devolução de cadastro - abrir modal
  1479 |             const dropdown = page.getByTestId('btn-cadastro-acoes');
  1480 |             if (await dropdown.count() > 0) {
  1481 |                 await dropdown.click();
  1482 |                 await page.getByTestId('btn-cadastro-acao-devolver').click();
  1483 |             } else {
  1484 |                 await page.getByTestId('btn-acao-devolver').click();
  1485 |             }
  1486 |             const modalDevolucao = page.locator('.modal.show');
  1487 |             await expect(modalDevolucao).toBeVisible();
  1488 |             await capturarTela(page, 'historico-analise', 'modal-devolucao-cadastro', {
  1489 |                 tags: ['modal', 'devolucao'],
  1490 |                 extra: {perfil: 'GESTOR', acao: 'devolver-cadastro'}
  1491 |             });
  1492 | 
  1493 |             // Preencher observação e confirmar devolução
  1494 |             await modalDevolucao.getByTestId('inp-devolucao-cadastro-obs').fill('Dados incompletos para a Secretaria');
  1495 |             await capturarTela(page, 'historico-analise', 'modal-devolucao-preenchido', {
  1496 |                 tags: ['modal', 'devolucao', 'preenchido']
  1497 |             });
  1498 |             await modalDevolucao.getByTestId('btn-devolucao-cadastro-confirmar').click();
  1499 |             await expect(page).toHaveURL(/\/painel/);
  1500 | 
  1501 |             // Chefe vê o cadastro devolvido com situação "em andamento"
  1502 |             await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
  1503 |             await page.goto(`/processo/${processo}/${unidadeAlvo}`);
  1504 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo}/${unidadeAlvo}(?:\?.*)?$`));
  1505 |             await capturarTela(page, 'historico-analise', 'subprocesso-apos-devolucao', {
  1506 |                 fullPage: true,
  1507 |                 tags: ['devolucao', 'chefe'],
  1508 |                 extra: {estado: 'CADASTRO_EM_ANDAMENTO'}
  1509 |             });
  1510 | 
  1511 |             // Chefe acessa cadastro e vê histórico com registro de devolução
  1512 |             await navegarParaCadastro(page);
  1513 |             const modalHistoricoChefe = await abrirHistoricoAnalise(page);
  1514 |             await expect(modalHistoricoChefe).toBeVisible();
  1515 |             await expect(modalHistoricoChefe.getByTestId('cell-resultado-0')).toHaveText(/Devolu/i);
  1516 |             await expect(modalHistoricoChefe).toContainText('Dados incompletos para a Secretaria');
  1517 |             await capturarTela(page, 'historico-analise', 'historico-com-devolucao', {
  1518 |                 tags: ['modal', 'historico', 'devolucao'],
  1519 |                 extra: {perfil: 'CHEFE', mostra: 'registro-devolucao'}
  1520 |             });
  1521 |             await fecharHistoricoAnalise(page);
  1522 |         });
  1523 |     });
  1524 | 
  1525 |     // SEÇÃO 12 - MODAIS DE GESTÃO DO MAPA
  1526 |     test.describe('12 - Modais de Gestão do Mapa', () => {
  1527 |         test('Captura modais de exclusão, edição e sugestões do mapa', async ({page, request}) => {
  1528 |             const unidadeAlvo = 'SECAO_112';
  1529 |             const descricao = `Proc mapa modais ${Date.now()}`;
  1530 |             const competencia1 = 'Competência para excluir';
  1531 |             const competencia2 = 'Competência para editar';
  1532 | 
  1533 |             const processoCodigo = await criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
  1534 |                 request, cleanup, descricao, unidadeAlvo
  1535 |             );
  1536 | 
  1537 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  1538 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  1539 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1540 |             await navegarParaCadastro(page);
> 1541 |             await (await abrirAcaoCadastroPrincipal(page)).click();
       |                                                            ^ Error: locator.click: Target page, context or browser has been closed
  1542 |             await expect(page.getByRole('dialog')).toBeVisible();
  1543 |             await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado para teste de mapa');
  1544 |             await page.getByTestId('btn-aceite-cadastro-confirmar').click();
  1545 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1546 | 
  1547 |             await navegarParaMapa(page);
  1548 | 
  1549 |             // Criar duas competências para testar edição e exclusão
  1550 |             await MapaHelpers.criarCompetencia(page, competencia1, []);
  1551 |             await MapaHelpers.criarCompetencia(page, competencia2, []);
  1552 |             await capturarTela(page, 'mapa-modais', 'mapa-com-duas-competencias', {
  1553 |                 fullPage: true,
  1554 |                 tags: ['mapa', 'competencias']
  1555 |             });
  1556 | 
  1557 |             // Modal de exclusão - abrir e cancelar
  1558 |             const cardExcluir = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(competencia1, {exact: true})});
  1559 |             await cardExcluir.hover();
  1560 |             await cardExcluir.getByTestId('btn-excluir-competencia').click();
  1561 |             const modalExclusao = page.getByTestId('mdl-excluir-competencia');
  1562 |             await expect(modalExclusao).toBeVisible();
  1563 |             await capturarTela(page, 'mapa-modais', 'modal-excluir-competencia', {
  1564 |                 tags: ['modal', 'exclusao'],
  1565 |                 extra: {competencia: competencia1}
  1566 |             });
  1567 |             await modalExclusao.getByTestId('btn-modal-confirmacao-cancelar').click();
  1568 |             await expect(modalExclusao).toBeHidden();
  1569 |             await expect(page.getByText(competencia1, {exact: true})).toBeVisible();
  1570 |             await capturarTela(page, 'mapa-modais', 'competencia-mantida-apos-cancelar-exclusao', {
  1571 |                 tags: ['cancelamento']
  1572 |             });
  1573 | 
  1574 |             // Modal de exclusão - confirmar
  1575 |             await excluirCompetenciaConfirmando(page, competencia1);
  1576 |             await capturarTela(page, 'mapa-modais', 'competencia-excluida', {
  1577 |                 tags: ['exclusao', 'confirmado']
  1578 |             });
  1579 | 
  1580 |             // Modal de edição de competência
  1581 |             const cardEditar = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(competencia2, {exact: true})});
  1582 |             await cardEditar.hover();
  1583 |             await cardEditar.getByTestId('btn-editar-competencia').click();
  1584 |             const modalEdicao = page.getByTestId('mdl-criar-competencia');
  1585 |             await expect(modalEdicao).toBeVisible();
  1586 |             await capturarTela(page, 'mapa-modais', 'modal-editar-competencia', {
  1587 |                 tags: ['modal', 'edicao'],
  1588 |                 extra: {competencia: competencia2}
  1589 |             });
  1590 |             await page.getByTestId('inp-criar-competencia-descricao').fill(`${competencia2} Editada`);
  1591 |             await capturarTela(page, 'mapa-modais', 'modal-editar-competencia-preenchido', {
  1592 |                 tags: ['modal', 'edicao', 'preenchido']
  1593 |             });
  1594 |             await page.getByTestId('btn-criar-competencia-salvar').click();
  1595 |             await expect(modalEdicao).toBeHidden();
  1596 |             await expect(page.getByText(`${competencia2} Editada`, {exact: true})).toBeVisible();
  1597 |         });
  1598 | 
  1599 |         test('Captura devolução do mapa e mapa somente leitura', async ({page, request}) => {
  1600 |             const unidadeAlvo = 'ASSESSORIA_11';
  1601 |             const descricao = `Proc devolucao mapa ${Date.now()}`;
  1602 | 
  1603 |             const processoCodigo = await criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
  1604 |                 request, cleanup, descricao, unidadeAlvo
  1605 |             );
  1606 | 
  1607 |             // CHEFE valida o mapa
  1608 |             await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
  1609 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  1610 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1611 |             await navegarParaMapa(page);
  1612 | 
  1613 |             // Abrir ações do mapa para o chefe
  1614 |             await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
  1615 |             await page.getByTestId('btn-mapa-acoes').click();
  1616 |             await capturarTela(page, 'mapa-modais', 'menu-acoes-mapa-chefe', {
  1617 |                 tags: ['menu', 'acoes-mapa', 'chefe']
  1618 |             });
  1619 |             await page.keyboard.press('Escape');
  1620 | 
  1621 |             // GESTOR abre mapa e usa devolução
  1622 |             await loginComPerfil(
  1623 |                 page,
  1624 |                 USUARIOS.GESTOR_SECRETARIA_1.titulo,
  1625 |                 USUARIOS.GESTOR_SECRETARIA_1.senha,
  1626 |                 USUARIOS.GESTOR_SECRETARIA_1.perfil
  1627 |             );
  1628 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  1629 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1630 |             await navegarParaMapa(page);
  1631 |             await capturarTela(page, 'mapa-modais', 'mapa-disponibilizado-gestor', {
  1632 |                 fullPage: true,
  1633 |                 tags: ['mapa', 'analise'],
  1634 |                 extra: {perfil: 'GESTOR', estado: 'DISPONIBILIZADO'}
  1635 |             });
  1636 | 
  1637 |             // Abrir modal de devolução do mapa
  1638 |             await abrirDevolucaoMapa(page);
  1639 |             const modalDevolucaoMapa = page.getByRole('dialog');
  1640 |             await expect(modalDevolucaoMapa).toBeVisible();
  1641 |             await capturarTela(page, 'mapa-modais', 'modal-devolver-mapa', {
```
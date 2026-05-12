# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> Admin - Consultas e Configurações >> Captura telas administrativas (Unidades, Histórico, Configurações e Relatórios)
- Location: e2e/captura.spec.ts:1362:9

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByText('SECAO_121').first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByText('SECAO_121').first()

```

# Test source

```ts
  1273 |             await expect(page.getByTestId('processo-info')).toBeVisible();
  1274 | 
  1275 |             const btnAceitarBlocoSec2 = await obterAcaoBloco(page, 'btn-processo-aceitar-bloco');
  1276 |             await expect(btnAceitarBlocoSec2).toBeVisible();
  1277 |             await btnAceitarBlocoSec2.click();
  1278 |             await page.getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
  1279 |             await expect(page).toHaveURL(/\/painel/);
  1280 | 
  1281 |             // Login como Admin para homologar em bloco
  1282 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  1283 | 
  1284 |             await page.getByTestId('tbl-processos').getByText(descricao).first().click();
  1285 |             await expect(page.getByTestId('processo-info')).toBeVisible();
  1286 | 
  1287 |             // Capturar botão de homologar cadastro em bloco (CDU-23)
  1288 |             const btnHomologarBloco = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
  1289 |             await expect(btnHomologarBloco).toBeVisible();
  1290 |             await btnHomologarBloco.click();
  1291 |             await expect(page.getByRole('dialog')).toBeVisible();
  1292 |             await capturarTela(page, 'operacoes-bloco', 'modal-homologar-cadastro-bloco', {extra: {perfil: 'ADMIN'}});
  1293 |             await page.getByRole('button', {name: /Cancelar/i}).click();
  1294 | 
  1295 |             // Capturar botão de disponibilizar mapas em bloco (CDU-24)
  1296 |             const btnDisponibilizarMapaBloco = await obterAcaoBloco(page, 'btn-processo-disponibilizar-bloco');
  1297 |             await expect(btnDisponibilizarMapaBloco).toBeVisible();
  1298 |             await expect(btnDisponibilizarMapaBloco).toBeDisabled();
  1299 | 
  1300 |             // Capturar botões de aceitar/homologar mapa em bloco (CDU-25 e CDU-26 - se visíveis)
  1301 |             const btnAceitarMapaBloco = page.getByRole('button', {name: /Aceitar.*mapa.*Bloco/i});
  1302 |             await expect(btnAceitarMapaBloco).toBeHidden();
  1303 | 
  1304 |             const btnHomologarMapaBloco = await obterAcaoBloco(page, 'btn-processo-homologar-mapas-bloco');
  1305 |             await expect(btnHomologarMapaBloco).toBeVisible();
  1306 |             await expect(btnHomologarMapaBloco).toBeDisabled();
  1307 |         });
  1308 |     });
  1309 | 
  1310 |     // SEÇÃO 10 - GESTÃO DE SUBPROCESSOS (CDUs 27, 32-34)
  1311 |     test.describe('10 - Gestão de Subprocessos', () => {
  1312 |         test('Captura modais de gestão de subprocesso', async ({page}) => {
  1313 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  1314 | 
  1315 |             const descricao = `Processo gestão ${Date.now()}`;
  1316 |             await criarProcesso(page, {
  1317 |                 descricao,
  1318 |                 tipo: 'MAPEAMENTO',
  1319 |                 diasLimite: 30,
  1320 |                 unidade: 'SECAO_121',
  1321 |                 expandir: ['SECRETARIA_1', 'COORD_12'],
  1322 |                 iniciar: true
  1323 |             });
  1324 | 
  1325 |             // Navegar para a página de detalhes do processo (estamos em /painel após criar)
  1326 |             await page.getByTestId('tbl-processos').getByText(descricao).first().click();
  1327 |             await expect(page).toHaveURL(/\/processo\/\d+/);
  1328 | 
  1329 |             // Registrar para cleanup
  1330 |             const codProcesso = await extrairProcessoCodigo(page);
  1331 |             registrarProcessoParaCleanup(cleanup, codProcesso);
  1332 | 
  1333 |             await page.getByRole('row', {name: /SECAO_121/i}).click();
  1334 |             await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_121/);
  1335 |             await capturarTela(page, 'gestao-subprocessos', 'detalhes-subprocesso-admin', {
  1336 |                 fullPage: true,
  1337 |                 extra: {perfil: 'ADMIN'}
  1338 |             });
  1339 | 
  1340 |             // Modal de alterar data limite (CDU-27)
  1341 |             const btnAlterarData = await obterAcaoCabecalhoSubprocesso(page, 'btn-alterar-data-limite');
  1342 |             await expect(btnAlterarData).toBeVisible();
  1343 |             await btnAlterarData.click();
  1344 |             await expect(page.getByRole('dialog')).toBeVisible();
  1345 |             await capturarTela(page, 'gestao-subprocessos', 'modal-alterar-data-limite', {tags: ['modal', 'gestao']});
  1346 |             await page.getByRole('button', {name: /Cancelar/i}).click();
  1347 | 
  1348 |             // Modal de reabrir cadastro (CDU-32)
  1349 |             const btnReabrirCadastro = await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-cadastro');
  1350 |             await expect(btnReabrirCadastro).toBeVisible();
  1351 |             await expect(btnReabrirCadastro).toBeDisabled();
  1352 | 
  1353 |             // CDU-34: Botão de enviar lembrete (ação direta, sem modal)
  1354 |             const btnEnviarLembrete = await obterAcaoCabecalhoSubprocesso(page, 'btn-enviar-lembrete');
  1355 |             await expect(btnEnviarLembrete).toBeVisible();
  1356 |             await capturarTela(page, 'gestao-subprocessos', 'botao-enviar-lembrete', {tags: ['acao-direta']});
  1357 |         });
  1358 |     });
  1359 | 
  1360 |     // ADMIN - CONSULTAS E CONFIGURAÇÕES
  1361 |     test.describe('Admin - Consultas e Configurações', () => {
  1362 |         test('Captura telas administrativas (Unidades, Histórico, Configurações e Relatórios)', async ({page}) => {
  1363 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  1364 | 
  1365 |             const linkUnidades = page.getByRole('link', {name: /Unidades/i});
  1366 |             await expect(linkUnidades).toBeVisible();
  1367 |             await linkUnidades.click();
  1368 |             await expect(page).toHaveURL(/\/unidades/);
  1369 |             await capturarTela(page, 'unidades', 'arvore-unidades', {fullPage: true});
  1370 | 
  1371 |             // A árvore de unidades agora vem expandida por padrão
  1372 |             await expect(page.getByText('SECRETARIA_1').first()).toBeVisible();
> 1373 |             await expect(page.getByText('SECAO_121').first()).toBeVisible();
       |                                                               ^ Error: expect(locator).toBeVisible() failed
  1374 | 
  1375 |             await capturarTela(page, 'unidades', 'arvore-unidades-expandida', {fullPage: true});
  1376 | 
  1377 |             const unidade = page.getByText('SECAO_121').first();
  1378 |             await expect(unidade).toBeVisible();
  1379 |             await unidade.click();
  1380 |             await expect(page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i})).toBeVisible();
  1381 |             await capturarTela(page, 'unidades', 'detalhes-unidade', {fullPage: true, extra: {unidade: 'SECAO_121'}});
  1382 | 
  1383 |             const btnCriarAtribuicao = page.getByRole('button', {name: /Criar atribuição|Nova atribuição/i});
  1384 |             await expect(btnCriarAtribuicao).toBeVisible();
  1385 |             await btnCriarAtribuicao.click();
  1386 |             await expect(page).toHaveURL(/\/unidade\/\d+\/atribuicao(?:\?.*)?$/);
  1387 |             await expect(page.getByRole('heading', {name: TEXTOS.atribuicaoTemporaria.TITULO})).toBeVisible();
  1388 |             await expect(page.getByTestId('input-busca-usuario')).toBeVisible();
  1389 |             await capturarTela(page, 'unidades', 'tela-criar-atribuicao', {tags: ['tela', 'atribuicao']});
  1390 |             await page.getByTestId('btn-cancelar-atribuicao').click();
  1391 |             await expect(page).toHaveURL(/\/unidade\/\d+(?:\?.*)?$/);
  1392 | 
  1393 |             const linkHistorico = page.getByRole('link', {name: /Histórico/i});
  1394 |             await expect(linkHistorico).toBeVisible();
  1395 |             await linkHistorico.click();
  1396 |             await expect(page.locator('table').first()).toBeVisible();
  1397 |             await capturarTela(page, 'historico', 'pagina-historico', {fullPage: true});
  1398 | 
  1399 |             const tabela = page.locator('table');
  1400 |             await expect(tabela).toBeVisible();
  1401 |             await capturarTela(page, 'historico', 'tabela-processos-finalizados', {fullPage: true});
  1402 | 
  1403 |             await page.getByTestId('btn-configuracoes').click();
  1404 |             await expect(page.getByLabel(/Dias para inativação de processos/i)).toBeVisible();
  1405 |             await capturarTela(page, 'configuracoes', 'pagina-configuracoes', {fullPage: true});
  1406 | 
  1407 |             const inputDiasInativacao = page.getByLabel(/Dias para inativação de processos/i);
  1408 |             await expect(inputDiasInativacao).toBeVisible();
  1409 |             await capturarTela(page, 'configuracoes', 'config-sistema', {tags: ['config']});
  1410 | 
  1411 |             await page.getByTestId('btn-administradores').click();
  1412 |             await expect(page.getByRole('button', {name: /Adicionar|Novo/i})).toBeVisible();
  1413 |             await capturarTela(page, 'configuracoes', 'lista-administradores', {fullPage: true});
  1414 | 
  1415 |             const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
  1416 |             await expect(btnAdicionar).toBeVisible();
  1417 |             await btnAdicionar.click();
  1418 |             await expect(page.getByRole('dialog')).toBeVisible();
  1419 |             await capturarTela(page, 'configuracoes', 'modal-adicionar-administrador', {tags: ['modal', 'admin']});
  1420 |             await page.getByRole('button', {name: /Cancelar/i}).click();
  1421 | 
  1422 |             const linkRelatorios = page.getByRole('link', {name: /Relatórios/i});
  1423 |             await expect(linkRelatorios).toBeVisible();
  1424 |             await linkRelatorios.click();
  1425 |             await expect(page.getByTestId('card-relatorio-andamento')).toBeVisible();
  1426 |             await capturarTela(page, 'relatorios', 'pagina-relatorios', {fullPage: true});
  1427 | 
  1428 |             await page.getByTestId('card-relatorio-andamento').click();
  1429 |             await expect(page.getByTestId('select-processo-andamento')).toBeVisible();
  1430 |             await capturarTela(page, 'relatorios', 'relatorio-andamento', {extra: {relatorio: 'andamento'}});
  1431 | 
  1432 |             await expect(page.getByTestId('btn-gerar-andamento')).toBeVisible();
  1433 |             await capturarTela(page, 'relatorios', 'botao-gerar-relatorio', {tags: ['ui-element']});
  1434 | 
  1435 |             await page.goto('/relatorios');
  1436 |             await page.getByTestId('card-relatorio-mapas').click();
  1437 |             await expect(page.getByTestId('container-arvore-unidades-mapas')).toBeVisible();
  1438 |             await capturarTela(page, 'relatorios', 'relatorio-mapas', {extra: {relatorio: 'mapas'}});
  1439 | 
  1440 |             await expect(page.getByTestId('btn-gerar-mapas')).toBeVisible();
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
```
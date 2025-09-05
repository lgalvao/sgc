import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsChefe, loginAsGestor, loginAsServidor} from '~/utils/auth';
import {SELECTORS, URLS} from './test-constants';
import {expectNotVisible, expectUrl, expectVisible} from './test-helpers';

test.describe('CDU-02: Visualizar Painel', () => {
  test('deve exibir tela Painel com seções Processos e Alertas para SERVIDOR', async ({ page }) => {
    // 1. Usuário faz login como SERVIDOR
    await loginAsServidor(page);

    // 2. Sistema exibe tela Painel
    await expectUrl(page, `**${URLS.PAINEL}`);
    await expectVisible(page, SELECTORS.TITULO_PROCESSOS);

    // 3. Com seções Processos e Alertas
    await expectVisible(page, SELECTORS.TITULO_PROCESSOS);
    await expectVisible(page, SELECTORS.TITULO_ALERTAS);

    // 4. Tabela de processos com cabeçalhos corretos
    await expectVisible(page, SELECTORS.TABELA_PROCESSOS);
    await expectVisible(page, SELECTORS.COLUNA_DESCRICAO);
    await expectVisible(page, SELECTORS.COLUNA_TIPO);
    await expectVisible(page, SELECTORS.COLUNA_UNIDADES);
    await expectVisible(page, SELECTORS.COLUNA_SITUACAO);

    // 5. SERVIDOR não vê botão Criar processo
    await expectNotVisible(page, SELECTORS.BTN_CRIAR_PROCESSO);

    // 6. Seção Alertas
    await expectVisible(page, SELECTORS.TABELA_ALERTAS);
  });

  test('deve exibir tela Painel para GESTOR sem botão Criar processo', async ({ page }) => {
    // 1. Usuário faz login como GESTOR
    await loginAsGestor(page);

    // 2. Sistema exibe tela Painel
    await expect(page).toHaveURL(/\/painel$/);
    await expect(page.getByTestId('titulo-processos')).toBeVisible();

    // 3. GESTOR não vê botão Criar processo
    await expect(page.getByTestId('btn-criar-processo')).not.toBeVisible();

    // 4. Mas vê as seções Processos e Alertas
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
    await expect(page.getByTestId('titulo-alertas')).toBeVisible();
  });

  test('deve exibir tela Painel para CHEFE', async ({ page }) => {
    // 1. Usuário faz login como CHEFE
    await loginAsChefe(page);

    // 2. Sistema exibe tela Painel diretamente
    await expect(page).toHaveURL(/\/painel$/);
    await expect(page.getByTestId('titulo-processos')).toBeVisible();

    // 3. CHEFE não vê botão Criar processo
    await expect(page.getByTestId('btn-criar-processo')).not.toBeVisible();

    // 4. Mas vê as seções Processos e Alertas
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
    await expect(page.getByTestId('titulo-alertas')).toBeVisible();
  });

  test('deve permitir ordenação de processos por descrição', async ({ page }) => {
    // 1. Login usando função que já funciona
    await loginAsAdmin(page);

    // 2. Clicar no cabeçalho Descrição da tabela de processos
    await page.getByTestId('coluna-descricao').click();

    // 3. Verificar que a tabela ainda está visível após ordenação
    await expect(page.getByTestId('tabela-processos')).toBeVisible();
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
  });

  test('deve permitir ordenação de processos por tipo', async ({ page }) => {
    // 1. Login usando função que já funciona
    await loginAsAdmin(page);

    // 2. Clicar no cabeçalho Tipo da tabela de processos
    await page.getByTestId('coluna-tipo').click();

    // 3. Verificar que a tabela ainda está visível após ordenação
    await expect(page.getByTestId('tabela-processos')).toBeVisible();
  });

  test('deve permitir clicar em processo SERVIDOR e navegar para detalhes', async ({ page }) => {
    // 1. Login como SERVIDOR
    await loginAsServidor(page);

    // 2. Clicar na primeira linha de processo
    await page.locator('table tbody tr').first().click();

    // 3. SERVIDOR deve navegar diretamente para subprocesso (não para processo principal)
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await expect(page.getByTestId('subprocesso-header')).toBeVisible();
    await expect(page.getByTestId('processo-info')).toBeVisible();
  });

  test('deve permitir clicar em processo GESTOR e navegar para detalhes', async ({ page }) => {
    // 1. Login como GESTOR
    await loginAsGestor(page);

    // 2. Clicar na primeira linha de processo
    await page.locator('table tbody tr').first().click();

    // 3. Deve navegar para processo principal (GESTOR vai para /processo/:id)
    await expect(page).toHaveURL(/.*\/processo\/\d+$/);
    await expect(page.getByTestId('processo-info')).toBeVisible();

    // 4. Aguardar TreeTable carregar e expandir todas
    await page.waitForSelector('[data-testid="tree-table-row"]');
    await page.getByTestId('btn-expandir-todas').click();
    
    // 5. Clicar na primeira unidade operacional (STIC)
    await page.locator('[data-testid="tree-table-row"]').filter({ hasText: 'STIC' }).click();

    // 6. Deve navegar para subprocesso
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await expect(page.getByTestId('subprocesso-header')).toBeVisible();
    await expect(page.getByTestId('processo-info')).toBeVisible();
  });

  test('deve permitir clicar em processo CHEFE e navegar para subprocesso', async ({ page }) => {
    // 1. Login como CHEFE
    await loginAsChefe(page);

    // 2. Clicar na primeira linha de processo
    await page.locator('table tbody tr').first().click();

    // 3. CHEFE deve navegar diretamente para subprocesso da sua unidade
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await expect(page.getByTestId('subprocesso-header')).toBeVisible();
    await expect(page.getByTestId('processo-info')).toBeVisible();
  });

  test('deve mostrar alertas na tabela', async ({ page }) => {
    // 1. Login usando função que já funciona
    await loginAsAdmin(page);

    // 2. Verificar seção de alertas
    await expect(page.getByTestId('titulo-alertas')).toBeVisible();
    await expect(page.getByTestId('tabela-alertas')).toBeVisible();

    // 3. Verificar cabeçalhos da tabela de alertas
    await expect(page.getByTestId('tabela-alertas')).toContainText('Data/Hora');
    await expect(page.getByTestId('tabela-alertas')).toContainText('Descrição');
    await expect(page.getByTestId('tabela-alertas')).toContainText('Processo');
    await expect(page.getByTestId('tabela-alertas')).toContainText('Origem');
  });
});
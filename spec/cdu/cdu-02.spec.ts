import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsAdmin, loginAsChefe, loginAsGestor, loginAsServidor} from '~/utils/auth';
import {SELECTORS, URLS} from './test-constants';
import {
    clickAndVerifyProcessTableSort,
    expectCommonPainelElements,
    expectNotVisible,
    expectUrl,
    expectVisible,
    loginAndClickFirstProcess
} from './test-helpers';

test.describe('CDU-02: Visualizar Painel', () => {
  test('deve exibir tela Painel com seções Processos e Alertas para SERVIDOR', async ({ page }) => {
    // 1. Usuário faz login como SERVIDOR
    await loginAsServidor(page);

    // 2. Sistema exibe tela Painel
    await expectUrl(page, `**${URLS.PAINEL}`);
      await expectCommonPainelElements(page);

    // 5. SERVIDOR não vê botão Criar processo
    await expectNotVisible(page, SELECTORS.BTN_CRIAR_PROCESSO);

    // 6. Seção Alertas
    await expectVisible(page, SELECTORS.TABELA_ALERTAS);
  });

  test('deve exibir tela Painel para GESTOR sem botão Criar processo', async ({ page }) => {
    // 1. Usuário faz login como GESTOR
    await loginAsGestor(page);

    // 2. Sistema exibe tela Painel
      await expectUrl(page, `**${URLS.PAINEL}`);
      await expectCommonPainelElements(page);

    // 3. GESTOR não vê botão Criar processo
      await expectNotVisible(page, SELECTORS.BTN_CRIAR_PROCESSO);
  });

  test('deve exibir tela Painel para CHEFE', async ({ page }) => {
    // 1. Usuário faz login como CHEFE
    await loginAsChefe(page);

    // 2. Sistema exibe tela Painel diretamente
      await expectUrl(page, `**${URLS.PAINEL}`);
      await expectCommonPainelElements(page);

    // 3. CHEFE não vê botão Criar processo
      await expectNotVisible(page, SELECTORS.BTN_CRIAR_PROCESSO);
  });

  test('deve permitir ordenação de processos por descrição', async ({ page }) => {
    // 1. Login usando função que já funciona
    await loginAsAdmin(page);

      // 2. Clicar no cabeçalho Descrição da tabela de processos e verificar visibilidade
      await clickAndVerifyProcessTableSort(page, SELECTORS.COLUNA_DESCRICAO);
  });

  test('deve permitir ordenação de processos por tipo', async ({ page }) => {
    // 1. Login usando função que já funciona
    await loginAsAdmin(page);

      // 2. Clicar no cabeçalho Tipo da tabela de processos e verificar visibilidade
      await clickAndVerifyProcessTableSort(page, SELECTORS.COLUNA_TIPO);
  });

  test('deve permitir clicar em processo SERVIDOR e navegar para detalhes', async ({ page }) => {
      // 1. Login como SERVIDOR e clicar na primeira linha de processo
      await loginAndClickFirstProcess(page, loginAsServidor);

    // 3. SERVIDOR deve navegar diretamente para subprocesso (não para processo principal)
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
      await expectVisible(page, SELECTORS.SUBPROCESSO_HEADER);
      await expectVisible(page, SELECTORS.PROCESSO_INFO);
  });

    test.slow();
  test('deve permitir clicar em processo GESTOR e navegar para detalhes', async ({ page }) => {
      // 1. Login como GESTOR e clicar na primeira linha de processo
      await loginAndClickFirstProcess(page, loginAsGestor);

    // 3. Deve navegar para processo principal (GESTOR vai para /processo/:id)
      await expectVisible(page, SELECTORS.PROCESSO_INFO);

    // 4. Aguardar TreeTable carregar e expandir todas
      await page.locator(SELECTORS.TREE_TABLE_ROW).first().waitFor();
    await page.getByTestId('btn-expandir-todas').click();
    
    // 5. Clicar na primeira unidade operacional (STIC)
      await page.locator(SELECTORS.TREE_TABLE_ROW).filter({hasText: 'STIC'}).click();

    // 6. Deve navegar para subprocesso
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
      await expectVisible(page, SELECTORS.SUBPROCESSO_HEADER);
      await expectVisible(page, SELECTORS.PROCESSO_INFO);
  });

  test('deve permitir clicar em processo CHEFE e navegar para subprocesso', async ({ page }) => {
      // 1. Login como CHEFE e clicar na primeira linha de processo
      await loginAndClickFirstProcess(page, loginAsChefe);

    // 3. CHEFE deve navegar diretamente para subprocesso da sua unidade
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
      await expectVisible(page, SELECTORS.SUBPROCESSO_HEADER);
      await expectVisible(page, SELECTORS.PROCESSO_INFO);
  });

  test('deve mostrar alertas na tabela', async ({ page }) => {
    // 1. Login usando função que já funciona
    await loginAsAdmin(page);

    // 2. Verificar seção de alertas
      await expectVisible(page, SELECTORS.TITULO_ALERTAS);
      await expectVisible(page, SELECTORS.TABELA_ALERTAS);

    // 3. Verificar cabeçalhos da tabela de alertas
      await expect(page.getByTestId(SELECTORS.TABELA_ALERTAS)).toContainText('Data/Hora');
      await expect(page.getByTestId(SELECTORS.TABELA_ALERTAS)).toContainText('Descrição');
      await expect(page.getByTestId(SELECTORS.TABELA_ALERTAS)).toContainText('Processo');
      await expect(page.getByTestId(SELECTORS.TABELA_ALERTAS)).toContainText('Origem');
  });
});
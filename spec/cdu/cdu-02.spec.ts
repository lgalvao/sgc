import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {
    esperarElementoInvisivel,
    esperarElementoVisivel,
    esperarUrl,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor
} from './auxiliares-teste';
import {clicarPrimeiroProcesso} from './auxiliares-navegacao';
import {SELETORES, TEXTOS, URLS} from './constantes-teste';

test.describe('CDU-02: Visualizar Painel', () => {
  test('deve exibir painel com seções Processos e Alertas para SERVIDOR', async ({ page }) => {
    await loginComoServidor(page);
    await esperarUrl(page, URLS.PAINEL);
    
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
    await esperarElementoInvisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
    await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);
  });

  test('deve exibir painel para GESTOR sem botão Criar processo', async ({ page }) => {
    await loginComoGestor(page);
    await esperarUrl(page, URLS.PAINEL);
    
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
    await esperarElementoInvisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
  });

  test('deve exibir painel para CHEFE', async ({ page }) => {
    await loginComoChefe(page);
    await esperarUrl(page, URLS.PAINEL);
    
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
    await esperarElementoInvisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
  });

  test('deve permitir ordenação de processos por descrição', async ({ page }) => {
    await loginComoAdmin(page);
    
    await page.click(`[data-testid="${SELETORES.COLUNA_DESCRICAO}"]`);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
  });

  test('deve permitir ordenação de processos por tipo', async ({ page }) => {
    await loginComoAdmin(page);
    
    await page.click(`[data-testid="${SELETORES.COLUNA_TIPO}"]`);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
  });

  test('deve permitir SERVIDOR navegar para subprocesso', async ({ page }) => {
    await loginComoServidor(page);
    await clicarPrimeiroProcesso(page);

    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
  });

  test('deve permitir GESTOR navegar para processo e depois subprocesso', async ({ page }) => {
    test.slow();
    await loginComoGestor(page);
    await clicarPrimeiroProcesso(page);

    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
    
    await page.locator(SELETORES.TREE_TABLE_ROW).first().waitFor();
    await page.getByTestId('btn-expandir-todas').click();
    
    await page.locator(SELETORES.TREE_TABLE_ROW).filter({ hasText: 'STIC' }).click();

    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
  });

  test('deve permitir CHEFE navegar para subprocesso', async ({ page }) => {
    await loginComoChefe(page);
    await clicarPrimeiroProcesso(page);

    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
  });

  test('deve mostrar alertas na tabela', async ({ page }) => {
    await loginComoAdmin(page);

    await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
    await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);

    const tabelaAlertas = page.getByTestId(SELETORES.TABELA_ALERTAS);
    await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DATA_HORA);
    await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DESCRICAO);
    await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_PROCESSO);
    await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_ORIGEM);
  });
});
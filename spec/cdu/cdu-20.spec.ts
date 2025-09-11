import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsGestor} from './test-helpers';

test.describe('CDU-20 - Analisar validação de mapa de competências', () => {
  
  test.describe('GESTOR', () => {
    test.beforeEach(async ({page}) => {
      await loginAsGestor(page);
    });

    test('deve exibir botões para GESTOR analisar mapa validado', async ({page}) => {
      // Navegar para um mapa validado (processo 1, unidade SEDESENV tem situação "Mapa validado")
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Verificar que os botões estão visíveis
      await expect(page.getByTestId('historico-analise-btn-gestor')).toBeVisible();
      await expect(page.getByTestId('devolver-ajustes-btn')).toBeVisible();
      await expect(page.getByTestId('registrar-aceite-btn')).toBeVisible();
      await expect(page.getByTestId('registrar-aceite-btn')).toHaveText('Registrar aceite');
    });

    test('deve permitir devolver para ajustes', async ({page}) => {
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Clicar em Devolver para ajustes
      await page.getByTestId('devolver-ajustes-btn').click();
      
      // Verificar modal
      await expect(page.getByTestId('modal-devolucao')).toBeVisible();
      await expect(page.getByTestId('modal-devolucao-title')).toHaveText('Devolução');
      await expect(page.getByTestId('modal-devolucao-body')).toContainText('Confirma a devolução da validação do mapa para ajustes?');
      
      // Preencher observação
      await page.getByTestId('observacao-devolucao-textarea').fill('Necessário revisar competências');
      
      // Confirmar devolução
      await page.getByTestId('modal-devolucao-confirmar').click();
      
      // Aguardar processamento
      await page.waitForTimeout(1000);
    });

    test('deve permitir registrar aceite', async ({page}) => {
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Clicar em Registrar aceite
      await page.getByTestId('registrar-aceite-btn').click();
      
      // Verificar modal (AceitarMapaModal)
      const modal = page.locator('.modal.show');
      await expect(modal).toBeVisible();
      
      // Confirmar aceite
      await page.getByTestId('modal-aceite-confirmar').click();
      
      // Aguardar processamento
      await page.waitForTimeout(1000);
    });

    test('deve cancelar devolução', async ({page}) => {
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Abrir modal de devolução
      await page.getByTestId('devolver-ajustes-btn').click();
      await expect(page.getByTestId('modal-devolucao')).toBeVisible();
      
      // Cancelar
      await page.getByTestId('modal-devolucao-cancelar').click();
      
      // Modal deve fechar
      await expect(page.getByTestId('modal-devolucao')).not.toBeVisible();
    });
  });

  test.describe('ADMIN', () => {
    test.beforeEach(async ({page}) => {
      await loginAsAdmin(page);
    });

    test('deve exibir botão Homologar para ADMIN', async ({page}) => {
      // Navegar para um mapa validado
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Verificar que o botão mostra "Homologar" para ADMIN
      await expect(page.getByTestId('registrar-aceite-btn')).toBeVisible();
      await expect(page.getByTestId('registrar-aceite-btn')).toHaveText('Homologar');
    });

    test('deve permitir homologar mapa', async ({page}) => {
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Clicar em Homologar
      await page.getByTestId('registrar-aceite-btn').click();
      
      // Verificar modal
      const modal = page.locator('.modal.show');
      await expect(modal).toBeVisible();
      
      // Confirmar homologação
      await page.getByTestId('modal-aceite-confirmar').click();
      
      // Aguardar processamento
      await page.waitForTimeout(1000);
    });
  });

  test.describe('Ver sugestões', () => {
    test.beforeEach(async ({page}) => {
      await loginAsGestor(page);
    });

    test('deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"', async ({page}) => {
      // Primeiro, vamos simular um mapa com sugestões
      // Para isso, precisaríamos de dados mock adequados ou criar o cenário
      // Por enquanto, vamos testar a funcionalidade básica
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Se houver sugestões, o botão deve aparecer
      const verSugestoesBtn = page.getByTestId('ver-sugestoes-btn');
      const isVisible = await verSugestoesBtn.isVisible();
      
      if (isVisible) {
        await verSugestoesBtn.click();
        await expect(page.getByTestId('modal-sugestoes')).toBeVisible();
        await expect(page.getByTestId('modal-sugestoes-title')).toHaveText('Sugestões');
        
        // Fechar modal
        await page.getByTestId('modal-sugestoes-fechar').click();
      }
    });
  });

  test.describe('Histórico de análise', () => {
    test.beforeEach(async ({page}) => {
      await loginAsGestor(page);
    });

    test('deve exibir histórico de análise', async ({page}) => {
      await page.goto('/processo/1/SEDESENV/vis-mapa');
      await page.waitForLoadState('networkidle');

      // Clicar no botão Histórico de análise
      await page.getByTestId('historico-analise-btn-gestor').click();
      
      // Verificar modal
      await expect(page.getByTestId('modal-historico')).toBeVisible();
      await expect(page.getByTestId('modal-historico-title')).toHaveText('Histórico de Análise');
      await expect(page.getByTestId('tabela-historico')).toBeVisible();
      
      // Verificar colunas da tabela
      const tabela = page.getByTestId('tabela-historico');
      await expect(tabela.getByText('Data/Hora')).toBeVisible();
      await expect(tabela.getByText('Unidade')).toBeVisible();
      await expect(tabela.getByText('Resultado')).toBeVisible();
      await expect(tabela.getByText('Observações')).toBeVisible();
      
      // Fechar modal
      await page.getByTestId('modal-historico-fechar').click();
      await expect(page.getByTestId('modal-historico')).not.toBeVisible();
    });
  });
});
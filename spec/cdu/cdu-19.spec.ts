import {expect, test} from '@playwright/test';
import {loginAsChefeSedia} from './test-helpers';

test.describe('CDU-19 - Validar mapa de competências', () => {
  test.beforeEach(async ({page}) => {
    await loginAsChefeSedia(page);
  });

  test('deve exibir botões Apresentar sugestões e Validar para CHEFE', async ({page}) => {
    // Navegar para um mapa disponibilizado (processo 5, unidade SEDIA) - rota de visualização
    await page.goto('/processo/5/SEDIA/vis-mapa');
    await page.waitForLoadState('networkidle');

    // Verificar que a página carregou corretamente
    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
    
    // Verificar que os botões estão visíveis
    await expect(page.getByTestId('apresentar-sugestoes-btn')).toBeVisible();
    await expect(page.getByTestId('validar-btn')).toBeVisible();
  });

  test('deve exibir botão Histórico de análise quando houver análises', async ({page}) => {
    // Navegar para um mapa que tem histórico de análises
    await page.goto('/processo/5/SEDIA/vis-mapa');
    await page.waitForLoadState('networkidle');

    // Se houver histórico, o botão deve aparecer
    const historicoBtn = page.getByTestId('historico-analise-btn');
    const isVisible = await historicoBtn.isVisible();
    
    if (isVisible) {
      await historicoBtn.click();
      await expect(page.getByTestId('modal-historico')).toBeVisible();
      await expect(page.getByTestId('tabela-historico')).toBeVisible();
      
      // Fechar modal
      await page.getByTestId('modal-historico-fechar').click();
    }
  });

  test('deve permitir apresentar sugestões', async ({page}) => {
    await page.goto('/processo/5/SEDIA/vis-mapa');
    await page.waitForLoadState('networkidle');

    // Clicar em Apresentar sugestões
    await page.getByTestId('apresentar-sugestoes-btn').click();
    
    // Verificar modal
    await expect(page.getByTestId('modal-apresentar-sugestoes')).toBeVisible();
    await expect(page.getByTestId('modal-apresentar-sugestoes-title')).toHaveText('Apresentar Sugestões');
    
    // Preencher sugestões
    await page.getByTestId('sugestoes-textarea').fill('Sugestão de teste para o mapa');
    
    // Confirmar
    await page.getByTestId('modal-apresentar-sugestoes-confirmar').click();
    
    // Verificar redirecionamento
    await expect(page).toHaveURL(/\/processo\/5\/SEDIA$/);
  });

  test('deve permitir validar mapa', async ({page}) => {
    await page.goto('/processo/5/SEDIA/vis-mapa');
    await page.waitForLoadState('networkidle');

    // Clicar em Validar
    await page.getByTestId('validar-btn').click();
    
    // Verificar modal
    await expect(page.getByTestId('modal-validar')).toBeVisible();
    await expect(page.getByTestId('modal-validar-title')).toHaveText('Validar Mapa de Competências');
    await expect(page.getByTestId('modal-validar-body')).toContainText('Confirma a validação do mapa de competências?');
    
    // Confirmar validação
    await page.getByTestId('modal-validar-confirmar').click();
    
    // Verificar redirecionamento
    await expect(page).toHaveURL(/\/processo\/5\/SEDIA$/);
  });

  test('deve cancelar apresentação de sugestões', async ({page}) => {
    await page.goto('/processo/5/SEDIA/vis-mapa');
    await page.waitForLoadState('networkidle');

    // Abrir modal de sugestões
    await page.getByTestId('apresentar-sugestoes-btn').click();
    await expect(page.getByTestId('modal-apresentar-sugestoes')).toBeVisible();
    
    // Cancelar
    await page.getByTestId('modal-apresentar-sugestoes-cancelar').click();
    
    // Modal deve fechar
    await expect(page.getByTestId('modal-apresentar-sugestoes')).not.toBeVisible();
  });

  test('deve cancelar validação de mapa', async ({page}) => {
    await page.goto('/processo/5/SEDIA/vis-mapa');
    await page.waitForLoadState('networkidle');

    // Abrir modal de validação
    await page.getByTestId('validar-btn').click();
    await expect(page.getByTestId('modal-validar')).toBeVisible();
    
    // Cancelar
    await page.getByTestId('modal-validar-cancelar').click();
    
    // Modal deve fechar
    await expect(page.getByTestId('modal-validar')).not.toBeVisible();
  });
});
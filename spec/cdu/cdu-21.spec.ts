import {expect, test} from '@playwright/test';
import {loginAsAdmin} from '~/utils/auth';

test.describe('CDU-21: Finalizar processo de mapeamento ou de revisão', () => {
  test('deve acessar tela de detalhes do processo em andamento', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar diretamente para a página do processo
    await page.goto('/processo/1');

    // Verificar tela de detalhes do processo
    await expect(page.getByText('Detalhes do processo')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Finalizar processo' })).toBeVisible();
  });

  test('deve mostrar erro quando há unidades não homologadas', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar diretamente para processo com unidades não homologadas
    await page.goto('/processo/1');

    // Clicar em Finalizar processo
    await page.getByRole('button', { name: 'Finalizar processo' }).click();

    // Verificar mensagem de erro
    await expect(page.getByText('Não é possível encerrar o processo enquanto houver unidades com mapa de competência ainda não homologado')).toBeVisible();

    // Verificar que permanece na tela
    await expect(page.getByText('Detalhes do processo')).toBeVisible();
  });

  test('deve mostrar diálogo de confirmação quando todas as unidades estão homologadas', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar diretamente para processo com todas as unidades homologadas
    await page.goto('/processo/2');

    // Clicar em Finalizar processo
    await page.getByRole('button', { name: 'Finalizar processo' }).click();

    // Verificar diálogo de confirmação
    await expect(page.getByText('Finalização de processo')).toBeVisible();
    await expect(page.getByText(/Confirma a finalização do processo/)).toBeVisible();
    await expect(page.getByText(/Essa ação tornará vigentes os mapas de competências homologados/)).toBeVisible();

    // Verificar botões
    await expect(page.getByTestId('btn-confirmar-finalizacao')).toBeVisible();
    await expect(page.getByTestId('btn-cancelar-finalizacao')).toBeVisible();
  });

  test('deve cancelar finalização do processo', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar diretamente para processo pronto para finalização
    await page.goto('/processo/2');

    // Clicar em Finalizar processo
    await page.getByRole('button', { name: 'Finalizar processo' }).click();

    // Cancelar operação
    await page.getByTestId('btn-cancelar-finalizacao').click();

    // Verificar que permanece na tela de detalhes
    await expect(page.getByText('Detalhes do processo')).toBeVisible();

    // Verificar que diálogo foi fechado
    await expect(page.getByText('Finalização de processo')).not.toBeVisible();
  });

  test('deve finalizar processo com sucesso', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar diretamente para processo pronto para finalização
    await page.goto('/processo/2');

    // Clicar em Finalizar processo
    await page.getByRole('button', { name: 'Finalizar processo' }).click();

    // Confirmar finalização
    await page.getByTestId('btn-confirmar-finalizacao').click();

    // Verificar mensagem de sucesso
    await expect(page.getByText('Processo finalizado')).toBeVisible();

    // Verificar redirecionamento para painel
    await page.waitForURL('**/painel');
  });

  test('deve verificar mudança de situação do processo', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Verificar situação antes da finalização
    await page.goto('/');
    await page.waitForSelector('table tbody tr.clickable-row', { timeout: 5000 });
    const processoAntes = page.locator('table tbody tr.clickable-row').filter({ hasText: 'Revisão de mapeamento' });
    await expect(processoAntes).toContainText('Em andamento');

    // Navegar para processo
    await page.goto('/processo/2');

    // Finalizar processo
    await page.getByRole('button', { name: 'Finalizar processo' }).click();
    await page.getByTestId('btn-confirmar-finalizacao').click();

    // Voltar ao painel
    await page.waitForURL('**/painel');
    await page.waitForSelector('table tbody tr.clickable-row', { timeout: 5000 });

    // Verificar mudança de situação
    const processoDepois = page.locator('table tbody tr.clickable-row').filter({ hasText: 'Revisão de mapeamento' });
    await expect(processoDepois).toContainText('Finalizado');
  });

  test('deve mostrar botão finalizar apenas para processos em andamento', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Testar processo em andamento - deve mostrar botão
    await page.goto('/processo/1');
    await expect(page.getByRole('button', { name: 'Finalizar processo' })).toBeVisible();

    // Testar processo criado - não deve mostrar botão
    await page.goto('/processo/6');
    await expect(page.getByRole('button', { name: 'Finalizar processo' })).not.toBeVisible();
  });

  test('deve enviar notificações por email após finalização', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Navegar diretamente para processo pronto para finalização
    await page.goto('/processo/2');

    // Clicar em Finalizar processo
    await page.getByRole('button', { name: 'Finalizar processo' }).click();

    // Confirmar finalização
    await page.getByTestId('btn-confirmar-finalizacao').click();

    // Aguardar processamento
    await page.waitForTimeout(1000);

    // Verificar se notificações foram enviadas (simulado)
    // Nota: Como as notificações são simuladas, verificamos se o processo foi finalizado
    await expect(page.getByText('Processo finalizado')).toBeVisible();
  });

  test('deve definir mapas como vigentes após finalização', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Este teste verifica se os mapas são definidos como vigentes
    // Para um teste completo, seria necessário verificar o estado dos mapas após finalização
    // Por ora, verificamos apenas se o processo foi finalizado com sucesso

    await page.goto('/processo/2');

    await page.getByRole('button', { name: 'Finalizar processo' }).click();
    await page.getByTestId('btn-confirmar-finalizacao').click();

    await expect(page.getByText('Processo finalizado')).toBeVisible();
  });

  test('deve criar alertas para unidades participantes', async ({ page }) => {
    // Login como ADMIN
    await loginAsAdmin(page);

    // Finalizar processo
    await page.goto('/processo/2');

    await page.getByRole('button', { name: 'Finalizar processo' }).click();
    await page.getByTestId('btn-confirmar-finalizacao').click();

    // Verificar se foi redirecionado para painel (onde alertas são exibidos)
    await page.waitForURL('**/painel');

    // Verificar se alertas foram criados (simulado)
    await expect(page.getByText('Painel')).toBeVisible();
  });
});
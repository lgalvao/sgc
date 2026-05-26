import {expect, test} from './fixtures/auth-fixtures.js';

test.describe('Feedback Control - Widget de Feedback', () => {
    test.beforeEach(async ({page, _autenticadoComoAdmin}) => {
        await page.goto('/painel');
    });

    test('Deve exibir o botão flutuante de feedback', async ({page}) => {
        await expect(page.getByTestId('feedback-btn')).toBeVisible();
    });

    test('Deve abrir o modal ao clicar no botão de feedback', async ({page}) => {
        await page.getByTestId('feedback-btn').click();

        await expect(page.getByTestId('feedback-modal')).toBeVisible();
        await expect(page.getByTestId('feedback-modal-title')).toBeVisible();
        await expect(page.getByTestId('feedback-nota')).toBeVisible();
    });

    test('Deve validar nota mínima de 10 caracteres', async ({page}) => {
        await page.getByTestId('feedback-btn').click();

        await page.getByTestId('feedback-nota').fill('curto');
        await page.getByTestId('feedback-btn-enviar').click();

        await expect(page.locator('.invalid-feedback')).toContainText('Descreva o problema com pelo menos 10 caracteres.');
    });

    test('Deve enviar feedback com sucesso', async ({page}) => {
        await page.getByTestId('feedback-btn').click();
        await expect(page.getByTestId('feedback-thumbnail')).toBeVisible();

        await page.getByTestId('feedback-nota').fill('Teste de feedback E2E com screenshot');
        await page.getByTestId('feedback-tipo-sugestao').check();
        await page.getByTestId('feedback-btn-enviar').click();

        await expect(page.getByText('Feedback enviado')).toBeVisible();
        await expect(page.getByTestId('feedback-modal')).toBeHidden();
    });

    test('Deve permitir remover captura antes de enviar', async ({page}) => {
        await page.getByTestId('feedback-btn').click();
        await expect(page.getByTestId('feedback-thumbnail')).toBeVisible();

        await page.getByTestId('feedback-btn-remover-captura').click();
        await expect(page.getByTestId('feedback-thumbnail')).toBeHidden();

        await page.getByTestId('feedback-nota').fill('Feedback sem captura de tela');
        await page.getByTestId('feedback-btn-enviar').click();

        await expect(page.getByText('Feedback enviado')).toBeVisible();
    });
});

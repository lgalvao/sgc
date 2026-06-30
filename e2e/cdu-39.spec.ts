import {expect, test} from './fixtures/complete-fixtures.js';

test.describe('CDU-39 - Enviar feedback contextual', () => {
    test('usuário autenticado valida a descrição e envia feedback pela tela atual', async ({
                                                                                               _resetAutomatico,
                                                                                               page,
                                                                                               _autenticadoComoAdmin
                                                                                           }) => {
        await page.goto('/painel');

        await page.getByTestId('feedback-btn').click();
        const modal = page.getByTestId('feedback-modal');
        await expect(modal).toBeVisible();
        await expect(page.getByTestId('feedback-modal-title')).toHaveText('Enviar feedback');
        await expect(page.getByTestId('feedback-tipo-bug')).toBeChecked();
        await expect(page.getByTestId('feedback-btn-enviar')).toBeVisible();

        await page.getByTestId('feedback-nota').fill('curto');
        await page.getByTestId('feedback-btn-enviar').click();
        await expect(modal).toContainText('Descreva o problema com pelo menos 10 caracteres.');

        await page.getByTestId('feedback-tipo-sugestao').check();
        await page.getByTestId('feedback-nota').fill('Sugestão detalhada para melhorar o painel.');
        await Promise.all([
            page.waitForResponse(res => res.url().includes('/api/feedback') && res.request().method() === 'POST' && res.ok()),
            page.getByTestId('feedback-btn-enviar').click()
        ]);

        await expect(modal).toBeHidden();
        await expect(page.locator('.orchestrator-container .toast').first()).toContainText('Feedback enviado');
    });
});

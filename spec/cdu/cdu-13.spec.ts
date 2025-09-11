import {expect, test} from '@playwright/test';
import {expectTextVisible, loginAsAdmin, loginAsGestor} from './test-helpers';
import {navigateToActivityVisualization} from './navigation-helpers';

test.describe('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    test('Deve exibir o modal de Histórico de análise', async ({page}) => {
        await loginAsGestor(page);
        await navigateToActivityVisualization(page, 1, 'STIC');

        await page.waitForLoadState('networkidle');
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await page.getByRole('button', {name: 'Histórico de análise'}).click();

        // Verificar se o modal de histórico de análise está visível usando um seletor CSS mais genérico
        await expect(page.locator('div.modal.fade.show')).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Histórico de Análise'})).toBeVisible(); // Manter a verificação do título também
        await expect(page.getByRole('heading', {name: 'Histórico de Análise'})).toBeVisible();
        await expectTextVisible(page, 'Observação de teste para histórico.'); // Verificar conteúdo da análise mockada

        // Fechar o modal
        await page.getByRole('button', {name: 'Fechar'}).click();
        await expect(page.getByRole('heading', {name: 'Histórico de Análise'})).not.toBeVisible();
    });

    // Teste para o fluxo de Devolução (GESTOR)
    test('GESTOR deve conseguir devolver o cadastro para ajustes', async ({page}) => {
        await loginAsGestor(page);
        await navigateToActivityVisualization(page, 1, 'STIC');

        await page.waitForLoadState('networkidle');
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await page.getByRole('button', {name: 'Devolver para ajustes'}).click();

        // Verificar se o modal de devolução está visível
        await expect(page.getByRole('heading', {name: 'Devolução do cadastro'})).toBeVisible();
        await expectTextVisible(page, 'Confirma a devolução do cadastro para ajustes?');

        // Preencher observação (opcional)
        await page.getByLabel('Observação (opcional)').fill('Devolução para correção de detalhes.');

        // Confirmar devolução
        await page.getByRole('button', {name: 'Confirmar'}).click();

        // Verificar mensagem de sucesso
        const notificationLocator = page.locator('.notification.notification-success');
        await expect(notificationLocator).toBeVisible({timeout: 5000});
        await expect(notificationLocator).toContainText('O cadastro foi devolvido para ajustes!');

        // Verificar redirecionamento para o Painel
        await expect(page).toHaveURL(new RegExp('/painel'));
    });

    // Teste para o fluxo de Devolução (ADMIN)
    test('ADMIN deve conseguir devolver o cadastro para ajustes', async ({page}) => {
        await loginAsAdmin(page);
        await navigateToActivityVisualization(page, 1, 'STIC');

        await page.waitForLoadState('networkidle');
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await page.getByRole('button', {name: 'Devolver para ajustes'}).click();

        // Verificar se o modal de devolução está visível
        await expect(page.getByRole('heading', {name: 'Devolução do cadastro'})).toBeVisible();

        // Confirmar devolução (sem observação)
        await page.getByRole('button', {name: 'Confirmar'}).click();

        // Verificar mensagem de sucesso
        const notificationLocator = page.locator('.notification.notification-success');
        await expect(notificationLocator).toBeVisible({timeout: 5000});
        await expect(notificationLocator).toContainText('O cadastro foi devolvido para ajustes!');

        // Verificar redirecionamento para o Painel
        await expect(page).toHaveURL(new RegExp('/painel'));
    });

    // Teste para o fluxo de Aceite (GESTOR)
    test('GESTOR deve conseguir registrar aceite do cadastro', async ({page}) => {
        await loginAsGestor(page);
        await navigateToActivityVisualization(page, 1, 'STIC');

        await page.waitForLoadState('networkidle');
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();

        const acceptButton = page.getByRole('button', {name: 'Registrar aceite'});
        const validateButton = page.getByRole('button', {name: 'Validar'});
        
        if (await acceptButton.isVisible()) {
            await acceptButton.click();
        } else {
            await validateButton.click();
        }

        // Verificar se o modal de validação está visível
        await expect(page.getByRole('heading', {name: 'Validação do cadastro'})).toBeVisible();
        await expectTextVisible(page, 'Confirma o aceite do cadastro de atividades?');

        // Preencher observação (opcional)
        await page.getByLabel('Observação (opcional)').fill('Aceite do cadastro de atividades.');

        // Confirmar aceite
        await page.getByRole('button', {name: 'Confirmar'}).click();

        // Verificar mensagem de sucesso
        const notificationLocator = page.locator('.notification.notification-success');
        await expect(notificationLocator).toBeVisible({timeout: 5000});
        await expect(notificationLocator).toContainText('A análise foi registrada com sucesso!');

        // Verificar redirecionamento para o Painel
        await expect(page).toHaveURL(new RegExp('/painel'));
    });

    // Teste para o fluxo de Homologação (ADMIN)
    test('ADMIN deve conseguir homologar o cadastro', async ({page}) => {
        await loginAsAdmin(page);
        await navigateToActivityVisualization(page, 1, 'STIC');

        await page.waitForLoadState('networkidle');
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();

        const homologateButton = page.getByRole('button', {name: 'Homologar'});
        const validateButton = page.getByRole('button', {name: 'Validar'});
        
        if (await homologateButton.isVisible()) {
            await homologateButton.click();
        } else {
            await validateButton.click();
        }

        // Verificar se o modal de homologação está visível
        await expect(page.getByRole('heading', {name: 'Homologação do cadastro de atividades e conhecimentos'})).toBeVisible();
        await expectTextVisible(page, 'Confirma a homologação do cadastro de atividades e conhecimentos?');

        // Confirmar homologação
        await page.getByRole('button', {name: 'Confirmar'}).click();

        // Verificar mensagem de sucesso
        const notificationLocator = page.locator('.notification.notification-success');
        await expect(notificationLocator).toBeVisible({timeout: 5000});
        await expect(notificationLocator).toContainText('O cadastro foi homologado com sucesso!');

        // Verificar redirecionamento para Detalhes do subprocesso
        await expect(page).toHaveURL(new RegExp(`/processo/1/subprocesso/30`));
    });
});
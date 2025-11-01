import {vueTest as test} from './support/vue-specific-setup';
import { expect } from '@playwright/test';

import {
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    selecionarUnidadesPorSigla,
    limparProcessos,
    criarProcesso,
    SELETORES
} from './helpers';

test.describe('CDU-04: Iniciar processo (com preparação)', () => {

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test.afterEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve abrir modal de confirmação e iniciar processo', async ({page}) => {
        const descricao = `Processo Iniciar ${Date.now()}`;
        const processoId = await criarProcesso(page, 'MAPEAMENTO', descricao, ['SGP']);

        await page.goto('/painel');
        await page.getByText(descricao).first().click();
        await page.waitForURL(new RegExp(`/processo/cadastro\\?idProcesso=${processoId}`));

        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Iniciar processo/i)).toBeVisible();

        await modal.getByRole('button', {name: /confirmar/i}).click();
        await page.waitForURL(/\/painel/);

        await expect(page.getByText(descricao).first()).toBeVisible();
    });

    test('deve cancelar iniciação e permanecer na tela', async ({page}) => {
        const descricao = `Processo Cancelar ${Date.now()}`;
        const processoId = await criarProcesso(page, 'MAPEAMENTO', descricao, ['STIC']);

        await page.goto('/painel');
        await page.getByText(descricao).first().click();
        await page.waitForURL(new RegExp(`/processo/cadastro\\?idProcesso=${processoId}`));

        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();

        await modal.getByRole('button', {name: /cancelar/i}).click();
        await expect(modal).not.toBeVisible();
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
    });

    test('não deve permitir editar processo após iniciado', async ({page}) => {
        const descricao = `Processo Bloqueio ${Date.now()}`;
        const processoId = await criarProcesso(page, 'MAPEAMENTO', descricao, ['COEDE']);

        await page.goto('/painel');
        await page.getByText(descricao).first().click();
        await page.waitForURL(new RegExp(`/processo/cadastro\\?idProcesso=${processoId}`));

        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        await page.locator('.modal.show').getByRole('button', {name: /confirmar/i}).click();
        await page.waitForURL(/\/painel/);

        await page.getByText(descricao).first().click();

        await page.waitForURL(new RegExp(`/processo/${processoId}`), {timeout: 15000});

        await expect(page.getByRole('button', {name: /salvar/i})).not.toBeVisible();
        await expect(page.getByRole('button', {name: /remover/i})).not.toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).not.toBeVisible();
    });
});

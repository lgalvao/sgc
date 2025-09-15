import {vueTest as test} from '../../tests/vue-specific-setup';
import {expect} from '@playwright/test';
import {
    esperarMensagemSucesso,
    esperarUrl,
    loginComoAdmin,
    navegarParaCriacaoProcesso
} from './auxiliares-verificacoes';
import {ROTULOS, SELETORES_CSS, TEXTOS, URLS} from './constantes-teste';

test.describe('CDU-04: Iniciar processo de mapeamento', () => {
    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
    });

    test('deve iniciar processo de mapeamento', async ({page}) => {
        await navegarParaCriacaoProcesso(page);

        await page.getByLabel(ROTULOS.DESCRICAO).fill('Processo de Mapeamento Teste');
        await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
        await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');

        await page.waitForSelector('input[type="checkbox"]');
        const firstCheckbox = page.locator('input[type="checkbox"]').first();
        await firstCheckbox.check();

        await page.getByText(TEXTOS.INICIAR_PROCESSO).click();
        await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
        await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
        await expect(page.getByText(TEXTOS.NOTIFICACAO_EMAIL)).toBeVisible();
        await page.getByText(TEXTOS.CONFIRMAR).click();
        //await page.waitForLoadState('networkidle');

        await esperarUrl(page, URLS.PAINEL);
        await esperarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
    });

    test('deve cancelar o início do processo', async ({page}) => {
        await navegarParaCriacaoProcesso(page);

        await page.getByLabel(ROTULOS.DESCRICAO).fill('Processo de Mapeamento Teste Cancelar');
        await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
        await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');

        await page.waitForSelector('input[type="checkbox"]');
        const firstCheckbox = page.locator('input[type="checkbox"]').first();
        await firstCheckbox.check();

        await page.getByText(TEXTOS.INICIAR_PROCESSO).click();
        await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
        await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
        await expect(page.getByText(TEXTOS.NOTIFICACAO_EMAIL)).toBeVisible();
        await page.getByRole('button', {name: TEXTOS.CANCELAR}).click();

        await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).not.toBeVisible();
        await esperarUrl(page, URLS.PROCESSO_CADASTRO);
    });
});
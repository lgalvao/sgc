import {vueTest as test} from '../support/vue-specific-setup';
import {expect} from '@playwright/test';

import {loginComoAdmin, navegarParaCriacaoProcesso, selecionarUnidadesPorSigla, SELETORES} from '~/helpers';

/**
 * CDU-04: Iniciar processo de mapeamento
 */
test.describe('CDU-04: Iniciar processo', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve abrir modal de confirmação e iniciar processo', async ({page}) => {
        // 1. Criar processo com STIC
        const descricao = `Processo Iniciar ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
        await page.selectOption(SELETORES.CAMPO_TIPO, 'MAPEAMENTO');
        await page.fill(SELETORES.CAMPO_DATA_LIMITE, '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['STIC']);
        await page.getByRole('button', {name: /salvar/i}).click();
        await page.waitForURL(/\/painel/);

        // 2. Abrir processo e aguardar carregamento completo
        await page.getByText(descricao).first().click();
        await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);

        // Aguardar carregamento das unidades
        await page.waitForSelector('.form-check-input[type="checkbox"]', {state: 'visible', timeout: 5000});

        // 3. Clicar em Iniciar Processo → Abre modal
        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Iniciar processo/i)).toBeVisible();
        await expect(modal.getByText(/não será mais possível/i)).toBeVisible();

        // 4. Confirmar → Processo iniciado
        await modal.getByRole('button', {name: /confirmar/i}).click();
        await page.waitForURL(/\/painel/);

        // 5. Verificar que processo aparece no painel
        await expect(page.getByText(descricao).first()).toBeVisible();
    });

    test('deve cancelar iniciação e permanecer na tela', async ({page}) => {
        // 1. Criar processo com SGP
        const descricao = `Processo Cancelar ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
        await page.selectOption(SELETORES.CAMPO_TIPO, 'MAPEAMENTO');
        await page.fill(SELETORES.CAMPO_DATA_LIMITE, '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SGP']);
        await page.getByRole('button', {name: /salvar/i}).click();
        await page.waitForURL(/\/painel/);

        // 2. Abrir e clicar em Iniciar
        await page.getByText(descricao).first().click();
        await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();

        // 3. Cancelar → Modal fecha e permanece na tela
        await modal.getByRole('button', {name: /cancelar/i}).click();
        await expect(modal).not.toBeVisible();
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).toBeVisible();
    });

    test('não deve permitir editar processo após iniciado', async ({page}) => {
        // 1. Criar e iniciar processo com COEDE
        const descricao = `Processo Bloqueio ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
        await page.selectOption(SELETORES.CAMPO_TIPO, 'MAPEAMENTO');
        await page.fill(SELETORES.CAMPO_DATA_LIMITE, '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['COEDE']);
        await page.getByRole('button', {name: /salvar/i}).click();
        await page.waitForURL(/\/painel/);

        await page.getByText(descricao).first().click();
        await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
        await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(descricao);
        await page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO).click();
        await page.locator('.modal.show').getByRole('button', {name: /confirmar/i}).click();
        await page.waitForURL(/\/painel/);

        // 2. Abrir processo iniciado → botões Editar/Remover/Iniciar não aparecem
        await page.getByText(descricao).first().click();

        // Deve ir para tela Processo (não CadProcesso)
        await page.waitForURL(/\/processo\/\d+/, {timeout: 1500});

        // Verificar que não está na tela de cadastro (sem botões de edição)
        await expect(page.getByRole('button', {name: /salvar/i})).not.toBeVisible();
        await expect(page.getByRole('button', {name: /remover/i})).not.toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).not.toBeVisible();
    });
});

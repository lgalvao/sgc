import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Detalhes do Processo - Unidades', () => {
    test.beforeEach(async ({page}) => {
        await login(page);
        await page.goto(`/processo/1`);
    });

    test('deve exibir os detalhes do processo e a tabela de unidades participantes', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Mapeamento de competências - 2025'})).toBeVisible();
        await expect(page.getByText('Tipo: Mapeamento')).toBeVisible();
        await expect(page.getByText('Situação: Em andamento')).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Unidades participantes'})).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Unidade', exact: true})).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Situação'})).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Data limite'})).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Unidade Atual'})).toBeVisible();
    });
});
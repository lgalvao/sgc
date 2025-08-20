import {expect, test} from "@playwright/test";
import {login} from "../utils/auth";

test.describe('Histórico de Processos', () => {
    test.setTimeout(5000);

    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página de histórico
        await page.goto(`/historico`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título da página e a tabela de histórico', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Histórico de processos'})).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Descrição ↑'})).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Tipo'})).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Unidades participantes'})).toBeVisible();
        await expect(page.getByRole('cell', {name: 'Finalizado em'})).toBeVisible();
    });

    test('deve exibir processos no histórico', async ({page}) => {
        // Verifica se há pelo menos uma linha de dados na tabela (excluindo o cabeçalho)
        const rows = page.locator('tbody').getByRole('row');
        await expect(rows).toHaveCount(3); // Assumindo que há 3 processos no snapshot
    });

    test('deve navegar para os detalhes do processo ao clicar em uma linha', async ({page}) => {
        // Clicar na primeira linha da tabela (que é uma clickable-row)
        await page.locator('tbody tr.clickable-row').first().click();

        // Esperar que a navegação para a página de detalhes do processo ocorra
        // A URL deve ser algo como /processo/1, /processo/2, etc.
        await page.waitForURL(/\/processo\/\d+/);

        // Verificar se um elemento específico da página de processo é visível
        await expect(page.getByText('Processo', {exact: true}).first()).toBeVisible();
    });
});
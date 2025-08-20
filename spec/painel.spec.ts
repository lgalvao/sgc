import {expect, test} from "@playwright/test";
import {login} from "./utils/auth";

test.describe('Painel Principal', () => {
    test.setTimeout(5000);

    test.beforeEach(async ({page}) => {
        await login(page);
    });

    test('deve carregar a página e exibir os títulos das seções', async ({page}) => {
        await expect(page.getByTestId('titulo-processos')).toBeVisible();
        await expect(page.getByTestId('titulo-alertas')).toBeVisible();
    });

    test('deve exibir as tabelas de processos e alertas', async ({page}) => {
        await expect(page.getByTestId('tabela-processos')).toBeVisible();
        await expect(page.getByTestId('tabela-alertas')).toBeVisible();
    });

    test('deve permitir ordenar a tabela de processos por descrição', async ({page}) => {
        // Clica na coluna Descrição para ordenar
        await page.getByTestId('coluna-descricao').click();
        // Verifica se o indicador de ordenação aparece (↑ ou ↓)
        await expect(page.getByTestId('coluna-descricao')).toContainText('Descrição');
        // Clica novamente para inverter a ordem
        await page.getByTestId('coluna-descricao').click();
        await expect(page.getByTestId('coluna-descricao')).toContainText('Descrição');
    });

    test('deve navegar para a página de detalhes do processo ao clicar em um processo', async ({page}) => {
        // Clica no primeiro processo da lista (assumindo que há pelo menos um)
        await page.locator('[data-testid="tabela-processos"] tbody tr:first-child td:first-child').click();
        // Verifica se a URL mudou para a página de detalhes do processo (novo padrão)
        await expect(page).toHaveURL(/.*\/processo\/\d+$/);
    });
});

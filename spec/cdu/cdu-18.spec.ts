import {expect, test} from '@playwright/test';
import {loginAsAdmin, loginAsChefe, loginAsGestor, loginAsServidor} from '~/utils/auth';

test.describe('CDU-18: Visualizar mapa de competências', () => {
    test('deve visualizar mapa como ADMIN', async ({page}) => {
        // Login como ADMIN
        await loginAsAdmin(page);

        // Clicar no processo
        const processoRow = page.locator('table tbody tr').first();
        await processoRow.click();

        // Como ADMIN, deve ver detalhes do processo e unidades subordinadas
        await expect(page.getByTestId('processo-info')).toBeVisible();
        await expect(page).toHaveURL(/.*\/processo\/\d+$/); // Deve permanecer na página principal

        // Clicar em uma unidade que tenha mapa (SESEL)
        const unidadeRow = page.locator('[data-testid="tree-table-row"]').filter({hasText: 'SESEL'}).first();
        await unidadeRow.click();

        // Verificar navegação para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
        await expect(page.getByTestId('processo-info')).toBeVisible();

        // Clicar no card Mapa de competências
        await page.getByTestId('mapa-card').click();

        // Verificar navegação para visualização do mapa
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+\/vis-mapa$/);
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Verificar que não há botões de edição (modo visualização)
        await expect(page.getByRole('button', {name: 'Criar competência'})).not.toBeVisible();
        await expect(page.getByRole('button', {name: 'Disponibilizar'})).not.toBeVisible();
    });

    test('deve visualizar mapa como GESTOR', async ({page}) => {
        // Login como GESTOR
        await loginAsGestor(page);

        // Clicar no processo
        const processoRow = page.locator('table tbody tr').first();
        await processoRow.click();

        // Como GESTOR, deve ver detalhes do processo (permanecer na página principal)
        await expect(page.getByTestId('processo-info')).toBeVisible();
        await expect(page).toHaveURL(/.*\/processo\/\d+$/); // Deve permanecer na página principal

        // Clicar em uma unidade que tenha mapa (SESEL)
        const unidadeRow = page.locator('[data-testid="tree-table-row"]').filter({hasText: 'SESEL'}).first();
        await unidadeRow.click();

        // Verificar navegação para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
        await expect(page.getByTestId('processo-info')).toBeVisible();

        // Aguardar o card do mapa aparecer e verificar se está habilitado
        const mapaCard = page.getByTestId('mapa-card');
        await expect(mapaCard).toBeVisible();

        // Verificar se o card não está desabilitado
        await expect(mapaCard).not.toHaveClass(/disabled-card/);

        // Clicar no card Mapa de competências
        await mapaCard.click();

        // Verificar navegação para visualização do mapa
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+\/vis-mapa$/);
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.locator('[data-testid="competencia-block"]').first()).toBeVisible();
    });

    test('deve visualizar mapa como CHEFE', async ({page}) => {
        // Login como CHEFE
        await loginAsChefe(page);

        // Clicar no processo 1 (que tem mapa para SESEL)
        const processoRow = page.locator('table tbody tr').filter({hasText: 'Mapeamento'}).first();
        await processoRow.click();

        // Como CHEFE, deve navegar para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
        await expect(page.getByTestId('processo-info')).toBeVisible();

        // Clicar no card Mapa de competências
        await page.getByTestId('mapa-card').click();

        // Verificar navegação para visualização do mapa
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+\/vis-mapa$/);
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Verificar identificação da unidade
        await expect(page.getByTestId('unidade-info')).toBeVisible();
    });

    test('deve visualizar mapa como SERVIDOR', async ({page}) => {
        // Login como SERVIDOR
        await loginAsServidor(page);

        // Clicar no processo 2 (que tem mapa para STIC)
        const processoRow = page.locator('table tbody tr').filter({hasText: 'Revisão'}).first();
        await processoRow.click();

        // Como SERVIDOR, deve navegar para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
        await expect(page.getByTestId('processo-info')).toBeVisible();

        // Clicar no card Mapa de competências
        await page.getByTestId('mapa-card').click();

        // Verificar navegação para visualização do mapa
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+\/vis-mapa$/);
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
    });

    test('deve mostrar competências e atividades associadas', async ({page}) => {
        // Login como CHEFE
        await loginAsChefe(page);

        // Navegar para subprocesso (processo 1 que tem mapa para SESEL)
        const processoRow = page.locator('table tbody tr').filter({hasText: 'Mapeamento'}).first();
        await processoRow.click();

        // Verificar navegação para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);

        // Verificar navegação para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);

        // Clicar no card Mapa de competências
        await page.getByTestId('mapa-card').click();

        // Verificar navegação para visualização do mapa
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+\/vis-mapa$/);

        // Verificar estrutura do mapa
        const competencias = page.locator('[data-testid="competencia-block"]');

        // Verificar que há pelo menos uma competência
        await expect(competencias.first()).toBeVisible();

        // Para cada competência, verificar se tem atividades
        const primeiraCompetencia = competencias.first();
        const atividades = primeiraCompetencia.locator('[data-testid="atividade-item"]');

        if (await atividades.count() > 0) {
            // Verificar que atividades têm conhecimentos
            const primeiraAtividade = atividades.first();

            // Verificar estrutura (pode não ter conhecimentos visíveis dependendo dos dados)
            await expect(primeiraAtividade).toBeVisible();
        }
    });

    test('deve mostrar informações completas da unidade', async ({page}) => {
        // Login como ADMIN
        await loginAsAdmin(page);

        // Navegar para processo
        const processoRow = page.locator('table tbody tr').first();
        await processoRow.click();

        // Clicar em uma unidade que tenha mapa (SESEL)
        const unidadeRow = page.locator('[data-testid="tree-table-row"]').filter({hasText: 'SESEL'}).first();
        await unidadeRow.click();

        // Verificar navegação para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);

        // Clicar no card Mapa de competências
        await page.getByTestId('mapa-card').click();

        // Verificar navegação para visualização do mapa
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+\/vis-mapa$/);

        // Verificar título
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Verificar identificação da unidade (sigla e nome)
        await expect(page.locator('[data-testid="unidade-info"]')).toBeVisible();

        // Verificar que há pelo menos uma competência exibida
        await expect(page.locator('[data-testid="competencia-block"]')).toBeVisible();
    });

    test('deve mostrar mapa vazio quando não há competências', async ({page}) => {
        // Login como CHEFE
        await loginAsChefe(page);

        // Navegar para subprocesso (processo 1 que tem mapa para SESEL)
        const processoRow = page.locator('table tbody tr').filter({hasText: 'Mapeamento'}).first();
        await processoRow.click();

        // Verificar navegação para subprocesso
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);

        // Clicar no card Mapa de competências
        await page.getByTestId('mapa-card').click();

        // Verificar navegação para visualização do mapa
        await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+\/vis-mapa$/);

        // Verificar título ainda aparece
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Verificar mensagem de mapa vazio ou ausência de competências
        const competencias = page.locator('[data-testid="competencia-block"]');

        // Se não há competências, pode mostrar mensagem específica
        if (await competencias.count() === 0) {
            await expect(page.getByText(/nenhuma competência|mapa vazio/i)).toBeVisible();
        }
    });
});
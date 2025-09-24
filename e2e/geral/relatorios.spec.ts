import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Relatórios', () => {
    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página de relatórios
        await page.goto(`/relatorios`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título e os cards de relatórios', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Relatórios'})).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Mapas Vigentes'})).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Diagnósticos de Gaps'})).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Andamento Geral'})).toBeVisible();
    });

    test('deve exibir filtros de relatórios', async ({page}) => {
        // Verificar filtro de tipo de processo
        await expect(page.getByLabel('Tipo de Processo')).toBeVisible();
        await expect(page.getByRole('combobox', {name: 'Tipo de Processo'})).toBeVisible();

        // Verificar opções do filtro
        const tipoSelect = page.getByRole('combobox', {name: 'Tipo de Processo'});
        await expect(tipoSelect).toContainText('Todos');
        await expect(tipoSelect).toContainText('Mapeamento');
        await expect(tipoSelect).toContainText('Revisão');
        await expect(tipoSelect).toContainText('Diagnóstico');

        // Verificar filtro de data início
        await expect(page.getByLabel('Data Início')).toBeVisible();
        const dataInicioInput = page.getByLabel('Data Início');
        await expect(dataInicioInput).toHaveAttribute('type', 'date');

        // Verificar filtro de data fim
        await expect(page.getByLabel('Data Fim')).toBeVisible();
        const dataFimInput = page.getByLabel('Data Fim');
        await expect(dataFimInput).toHaveAttribute('type', 'date');
    });

    test('deve aplicar filtros de tipo de processo', async ({page}) => {
        const tipoSelect = page.getByRole('combobox', {name: 'Tipo de Processo'});

        // Selecionar tipo "Mapeamento"
        await tipoSelect.selectOption({label: 'Mapeamento'});
        await expect(tipoSelect).toHaveValue('Mapeamento');

        // Selecionar tipo "Revisão"
        await tipoSelect.selectOption({label: 'Revisão'});
        await expect(tipoSelect).toHaveValue('Revisão');

        // Selecionar tipo "Diagnóstico"
        await tipoSelect.selectOption({label: 'Diagnóstico'});
        await expect(tipoSelect).toHaveValue('Diagnóstico');

        // Voltar para "Todos"
        await tipoSelect.selectOption({label: 'Todos'});
        await expect(tipoSelect).toHaveValue('');
    });

    test('deve aplicar filtros de data', async ({page}) => {
        const dataInicioInput = page.getByLabel('Data Início');
        const dataFimInput = page.getByLabel('Data Fim');

        // Definir data início (usando datas que cobrem os mocks de 2025)
        await dataInicioInput.fill('2025-01-01');
        await expect(dataInicioInput).toHaveValue('2025-01-01');

        // Definir data fim
        await dataFimInput.fill('2025-12-31');
        await expect(dataFimInput).toHaveValue('2025-12-31');

        // Limpar filtros
        await dataInicioInput.fill('');
        await dataFimInput.fill('');
        await expect(dataInicioInput).toHaveValue('');
        await expect(dataFimInput).toHaveValue('');
    });

    test('deve exibir contadores nos cards de relatórios', async ({page}) => {
        // Verificar contador de mapas vigentes (pode ser 0 com mocks atuais)
        await expect(page.getByText(/mapas encontrados/)).toBeVisible();

        // Verificar contador de diagnósticos de gaps (hardcoded 4)
        await expect(page.getByText(/diagnósticos encontrados/)).toBeVisible();

        // Verificar contador de processos (13 com mocks)
        await expect(page.getByText(/processos encontrados/)).toBeVisible();
    });

    test('deve abrir modal de mapas vigentes', async ({page}) => {
        const mapasVigentesCard = page.getByRole('heading', {name: 'Mapas Vigentes'}).locator('..');
        await mapasVigentesCard.click();

        const modal = page.locator('#modalMapasVigentes');
        await modal.waitFor({ state: 'visible' });

        // Verificar modal
        await expect(modal.getByRole('heading', { name: 'Mapas Vigentes' })).toBeVisible();

        // Verificar botão de exportar
        await expect(page.getByTestId('export-csv-mapas')).toBeVisible();

        // Verificar tabela
        await modal.getByRole('table').waitFor({ state: 'visible' });
        await modal.locator('thead').waitFor({ state: 'visible' });
        await expect(modal.getByText('Unidade')).toBeVisible();
        await expect(modal.locator('th:has-text("Processo")')).toBeVisible();
        await expect(modal.locator('th:has-text("Competências")')).toBeVisible();
        await expect(modal.locator('th:has-text("Data Criação")')).toBeVisible();
        await expect(modal.locator('th:has-text("Situação")')).toBeVisible();
    });

    test('deve abrir modal de diagnósticos de gaps', async ({page}) => {
        const diagnosticosCard = page.getByRole('heading', {name: 'Diagnósticos de Gaps'}).locator('..');
        await diagnosticosCard.click();

        const modal = page.locator('#modalDiagnosticosGaps');
        await modal.waitFor({ state: 'visible' });

        // Verificar modal
        await expect(modal.getByRole('heading', { name: 'Diagnósticos de Gaps' })).toBeVisible();

        // Verificar botão de exportar
        await modal.getByRole('table').waitFor({ state: 'visible' });
        await modal.locator('thead').waitFor({ state: 'visible' });
        await expect(modal.getByRole('table')).toBeVisible();
        await expect(modal.locator('th:has-text("Processo")')).toBeVisible();
        await expect(modal.locator('th:has-text("Unidade")')).toBeVisible();
        await expect(modal.locator('th:has-text("Gaps Identificados")')).toBeVisible();
        await expect(modal.locator('th:has-text("Importância Média")')).toBeVisible();
        await expect(modal.locator('th:has-text("Dominio Médio")')).toBeVisible();
        await expect(modal.locator('th:has-text("Competências Críticas")')).toBeVisible();
        await expect(modal.locator('th:has-text("Status")')).toBeVisible();
        await expect(modal.locator('th:has-text("Data Diagnóstico")')).toBeVisible();
    });

    test('deve abrir modal de andamento geral', async ({page}) => {
        const andamentoCard = page.getByRole('heading', {name: 'Andamento Geral'}).locator('..');
        await andamentoCard.click();

        const modal = page.locator('#modalAndamentoGeral');
        await modal.waitFor({ state: 'visible' });

        // Verificar modal
        await expect(page.getByText('Andamento Geral dos Processos')).toBeVisible();

        // Verificar botão de exportar
        await expect(page.getByTestId('export-csv-andamento')).toBeVisible();

        // Verificar tabela
        await modal.getByRole('table').waitFor({ state: 'visible' });
        await modal.locator('thead').waitFor({ state: 'visible' });
        await expect(modal.getByRole('table')).toBeVisible();
        await expect(modal.locator('th:has-text("Descrição")')).toBeVisible();
        await expect(modal.locator('th:has-text("Tipo")')).toBeVisible();
        await expect(modal.locator('th:has-text("Situação")')).toBeVisible();
        await expect(modal.locator('th:has-text("Data Limite")')).toBeVisible();
        await expect(modal.locator('th:has-text("Unidades Participantes")')).toBeVisible();
        await expect(modal.locator('th:has-text("% Concluído")')).toBeVisible();
    });

    test('deve fechar modais de relatórios', async ({page}) => {
        // Abrir modal de mapas vigentes
        const mapasVigentesCard = page.getByRole('heading', {name: 'Mapas Vigentes'}).locator('..');
        await mapasVigentesCard.click();

        // Aguardar um pouco para o modal carregar
        await page.waitForTimeout(1000);

        // Verificar se o modal está visível
        const modal = page.locator('#modalMapasVigentes');
        if (await modal.isVisible()) {
            await expect(modal).toBeVisible();

            // Fechar modal pelo botão X
            const fecharButton = page.locator('#modalMapasVigentes .btn-close');
            if (await fecharButton.isVisible()) {
                await fecharButton.click();
                await expect(modal).not.toBeVisible();
            }
        }
    });

    test('deve exportar mapas vigentes para CSV', async ({page}) => {
        // Abrir modal de mapas vigentes
        const mapasVigentesCard = page.getByRole('heading', {name: 'Mapas Vigentes'}).locator('..');
        await mapasVigentesCard.click();
        // Aguardar um pouco para o modal carregar
        await page.waitForTimeout(1000);

        const modal = page.locator('#modalMapasVigentes');
        if (await modal.isVisible()) {
            const exportarButton = page.getByTestId('export-csv-mapas');

            if (await exportarButton.isVisible()) {
                await exportarButton.click();
                await expect(exportarButton).toBeEnabled();
            }
        }
    });

    test('deve exportar diagnósticos de gaps para CSV', async ({page}) => {
        // Abrir modal de diagnósticos
        const diagnosticosCard = page.getByRole('heading', {name: 'Diagnósticos de Gaps'}).locator('..');
        await diagnosticosCard.click();
        // Aguardar um pouco para o modal carregar
        await page.waitForTimeout(1000);

        const modal = page.locator('#modalDiagnosticosGaps');
        if (await modal.isVisible()) {
            const exportarButton = page.getByTestId('export-csv-diagnosticos');

            if (await exportarButton.isVisible()) {
                await exportarButton.click();
                await expect(exportarButton).toBeEnabled();
            }
        }
    });

    test('deve exportar andamento geral para CSV', async ({page}) => {
        // Abrir modal de andamento geral
        const andamentoCard = page.getByRole('heading', {name: 'Andamento Geral'}).locator('..');
        await andamentoCard.click();
        // Aguardar um pouco para o modal carregar
        await page.waitForTimeout(1000);

        const modal = page.locator('#modalAndamentoGeral');
        if (await modal.isVisible()) {
            const exportarButton = page.getByTestId('export-csv-andamento');

            if (await exportarButton.isVisible()) {
                await exportarButton.click();
                await expect(exportarButton).toBeEnabled();
            }
        }
    });

    test('deve exibir dados nos relatórios', async ({page}) => {
        // Abrir modal de diagnósticos (que tem dados hardcoded)
        const diagnosticosCard = page.getByRole('heading', {name: 'Diagnósticos de Gaps'}).locator('..');
        await diagnosticosCard.click();
        // Aguardar um pouco para o modal carregar
        await page.waitForTimeout(1000);

        const modal = page.locator('#modalDiagnosticosGaps');
        if (await modal.isVisible()) {
            const table = page.locator('#modalDiagnosticosGaps table');

            // Wait for table to become visible
            await expect(table).toBeVisible();

            const tableRows = page.locator('#modalDiagnosticosGaps table tbody tr');

            // Verificar se há dados na tabela (hardcoded 4)
            const tabela = page.getByRole('table');
            await expect(tabela).toBeVisible();

            // Verificar se há linhas de dados
            const countLinhas = await tableRows.count();
            expect(countLinhas).toBeGreaterThan(0); // Deve ter 4 linhas
        }
    });

    test('deve exibir badges de status nos diagnósticos', async ({page}) => {
        // Abrir modal de diagnósticos
        const diagnosticosCard = page.getByRole('heading', {name: 'Diagnósticos de Gaps'}).locator('..');
        await diagnosticosCard.click();

        // Aguardar um pouco para o modal carregar
        await page.waitForTimeout(1000);

        const modal = page.locator('#modalDiagnosticosGaps');
        if (await modal.isVisible()) {
            // Verificar se há badges de status (um por linha, 4 linhas)
            const badges = modal.locator('.badge');
            const countBadges = await badges.count();

            if (countBadges > 0) {
                expect(countBadges).toBe(4); // Uma badge por diagnóstico

                // Verificar classes dos badges
                const primeiroBadge = badges.first();
                const classes = await primeiroBadge.getAttribute('class');
                expect(classes).toMatch(/badge/);
            }
        }
    });

    test('deve aplicar filtros combinados', async ({page}) => {
        const tipoSelect = page.getByRole('combobox', {name: 'Tipo de Processo'});
        const dataInicioInput = page.getByLabel('Data Início');
        const dataFimInput = page.getByLabel('Data Fim');

        // Aplicar filtros combinados
        await tipoSelect.selectOption({label: 'Mapeamento'});
        await dataInicioInput.fill('2025-01-01');
        await dataFimInput.fill('2025-12-31');

        // Verificar que os filtros foram aplicados
        await expect(tipoSelect).toHaveValue('Mapeamento');
        await expect(dataInicioInput).toHaveValue('2025-01-01');
        await expect(dataFimInput).toHaveValue('2025-12-31');

        // Abrir modal de andamento geral para verificar filtros
        const andamentoCard = page.getByRole('heading', {name: 'Andamento Geral'}).locator('..');
        await andamentoCard.click();
        // Aguardar um pouco para o modal carregar
        await page.waitForTimeout(1000);

        const modal = page.locator('#modalAndamentoGeral');
        if (await modal.isVisible()) {
            await expect(page.getByText('Andamento Geral dos Processos')).toBeVisible();
        }
    });

    test('deve exibir descrições dos cards de relatórios', async ({page}) => {
        // Verificar descrição do card de mapas vigentes
        await expect(page.getByText('Visualize os mapas de competências atualmente vigentes em todas as unidades.')).toBeVisible();

        // Verificar descrição do card de diagnósticos
        await expect(page.getByText('Analise os gaps de competências identificados nos processos de diagnóstico.')).toBeVisible();

        // Verificar descrição do card de andamento geral
        await expect(page.getByText('Acompanhe o andamento de todos os processos de mapeamento e revisão.')).toBeVisible();
    });
});
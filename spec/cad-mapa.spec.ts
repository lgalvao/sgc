import {expect, test} from '@playwright/test';

test.describe('CadMapa', () => {
    const unidadeSigla = 'SESEL';
    const processoId = 1;
    const baseUrl = `http://localhost:5173/mapa/${unidadeSigla}?processoId=${processoId}`;

    test.beforeEach(async ({page}) => {
        test.setTimeout(5000);
        await page.route('**/api/unidades*', route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([{
                    id: 1,
                    sigla: 'SESEL',
                    nome: 'Seção de Sistemas Eleitorais',
                    filhas: []
                }])
            });
        });

        await page.route('**/api/atividades*', route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([
                    {id: 101, descricao: 'Atividade A', processoUnidadeId: 1},
                    {id: 102, descricao: 'Atividade B', processoUnidadeId: 1},
                    {id: 103, descricao: 'Atividade C', processoUnidadeId: 1},
                ])
            });
        });

        await page.route('**/api/processosUnidade*', route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([
                    {id: 1, processoId: 1, unidade: 'UNID1'}
                ])
            });
        });

        await page.route('**/api/mapas*', route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([]) // Começa sem mapas para facilitar a criação
            });
        });

        await page.goto(baseUrl);
    });

    test('deve exibir o título e o nome da unidade', async ({page}) => {
        await expect(page.locator('.display-6')).toHaveText('Mapa de competências técnicas');
        await expect(page.locator('.fs-5')).toHaveText(`${unidadeSigla} - Unidade de Teste 1`);
    });

    test('deve exibir mensagem de nenhuma competência cadastrada inicialmente', async ({page}) => {
        await expect(page.getByText('Nenhuma competência cadastrada ainda.')).toBeVisible();
    });

    test('deve criar uma nova competência com sucesso', async ({page}) => {
        await page.getByRole('button', {name: 'Criar competência'}).click();

        const modal = page.locator('.modal.show');
        await expect(modal).toBeVisible();
        await expect(modal.locator('.modal-title')).toHaveText('Criação de competência');

        await modal.locator('[data-testid="input-nova-competencia"]').fill('Nova Competência de Teste');
        await modal.locator('input[type="checkbox"][value="101"]').click(); // Seleciona Atividade A
        await modal.locator('input[type="checkbox"][value="102"]').click(); // Seleciona Atividade B

        await expect(modal.locator('[data-testid="btn-criar-competencia"]')).toBeEnabled();
        await modal.locator('[data-testid="btn-criar-competencia"]').click();

        await expect(modal).not.toBeVisible(); // Modal deve fechar

        const competenciaItem = page.locator('[data-testid="competencia-item"]');
        await expect(competenciaItem).toBeVisible();
        await expect(competenciaItem.locator('[data-testid="competencia-descricao"]')).toHaveText('Nova Competência de Teste');
        await expect(page.getByText('Atividade A')).toBeVisible();
        await expect(page.getByText('Atividade B')).toBeVisible();
    });

    test('deve editar uma competência existente', async ({page}) => {
        // Primeiro, cria uma competência para poder editá-la
        await page.getByRole('button', {name: 'Criar competência'}).click();
        const modalCriacao = page.locator('.modal.show');
        await modalCriacao.locator('[data-testid="input-nova-competencia"]').fill('Competência Original');
        await modalCriacao.locator('input[type="checkbox"][value="101"]').click();
        await modalCriacao.locator('[data-testid="btn-criar-competencia"]').click();
        await expect(page.locator('[data-testid="competencia-item"]')).toBeVisible();

        // Agora, edita a competência
        await page.locator('[data-testid="btn-editar-competencia"]').click();

        const modalEdicao = page.locator('.modal.show');
        await expect(modalEdicao).toBeVisible();
        await expect(modalEdicao.locator('.modal-title')).toHaveText('Edição de competência');
        await expect(modalEdicao.locator('[data-testid="input-nova-competencia"]')).toHaveValue('Competência Original');
        await expect(modalEdicao.locator('input[type="checkbox"][value="101"]')).toBeChecked();

        await modalEdicao.locator('[data-testid="input-nova-competencia"]').fill('Competência Editada');
        await modalEdicao.locator('input[type="checkbox"][value="101"]').click(); // Desseleciona Atividade A
        await modalEdicao.locator('input[type="checkbox"][value="103"]').click(); // Seleciona Atividade C

        await modalEdicao.locator('[data-testid="btn-criar-competencia"]').click();
        await expect(modalEdicao).not.toBeVisible();

        const competenciaItem = page.locator('[data-testid="competencia-item"]');
        await expect(competenciaItem.locator('[data-testid="competencia-descricao"]')).toHaveText('Competência Editada');
        await expect(page.getByText('Atividade A')).not.toBeVisible();
        await expect(page.getByText('Atividade C')).toBeVisible();
    });

    test('deve excluir uma competência', async ({page}) => {
        // Primeiro, cria uma competência para poder excluí-la
        await page.getByRole('button', {name: 'Criar competência'}).click();
        const modalCriacao = page.locator('.modal.show');
        await modalCriacao.locator('[data-testid="input-nova-competencia"]').fill('Competência para Excluir');
        await modalCriacao.locator('input[type="checkbox"][value="101"]').click();
        await modalCriacao.locator('[data-testid="btn-criar-competencia"]').click();
        await expect(page.locator('[data-testid="competencia-item"]')).toBeVisible();

        // Agora, exclui a competência
        await page.locator('[data-testid="btn-excluir-competencia"]').click();
        await expect(page.locator('[data-testid="competencia-item"]')).not.toBeVisible();
        await expect(page.getByText('Nenhuma competência cadastrada ainda.')).toBeVisible();
    });

    test('deve remover uma atividade associada de uma competência', async ({page}) => {
        // Primeiro, cria uma competência com atividades associadas
        await page.getByRole('button', {name: 'Criar competência'}).click();
        const modalCriacao = page.locator('.modal.show');
        await modalCriacao.locator('[data-testid="input-nova-competencia"]').fill('Competência com Atividades');
        await modalCriacao.locator('input[type="checkbox"][value="101"]').click();
        await modalCriacao.locator('input[type="checkbox"][value="102"]').click();
        await modalCriacao.locator('[data-testid="btn-criar-competencia"]').click();
        await expect(page.getByText('Atividade A')).toBeVisible();
        await expect(page.getByText('Atividade B')).toBeVisible();

        // Remove a Atividade A
        await page.locator('.group-atividade-associada:has-text("Atividade A") .botao-acao-inline').click();
        await expect(page.getByText('Atividade A')).not.toBeVisible();
        await expect(page.getByText('Atividade B')).toBeVisible(); // Atividade B deve permanecer
    });

    test('deve disponibilizar o mapa com sucesso', async ({page}) => {
        // Primeiro, cria uma competência para habilitar o botão "Disponibilizar"
        await page.getByRole('button', {name: 'Criar competência'}).click();
        const modalCriacao = page.locator('.modal.show');
        await modalCriacao.locator('[data-testid="input-nova-competencia"]').fill('Competência para Disponibilizar');
        await modalCriacao.locator('input[type="checkbox"][value="101"]').click();
        await modalCriacao.locator('[data-testid="btn-criar-competencia"]').click();

        await page.getByRole('button', {name: 'Disponibilizar'}).click();

        const modalDisponibilizar = page.locator('.modal.show');
        await expect(modalDisponibilizar).toBeVisible();
        await expect(modalDisponibilizar.locator('.modal-title')).toHaveText('Disponibilizar Mapa');

        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);
        const tomorrowFormatted = tomorrow.toISOString().split('T')[0]; // YYYY-MM-DD

        await modalDisponibilizar.locator('#dataLimite').fill(tomorrowFormatted);
        await expect(modalDisponibilizar.getByRole('button', {name: 'Disponibilizar'})).toBeEnabled();
        await modalDisponibilizar.getByRole('button', {name: 'Disponibilizar'}).click();

        // Verifica a notificação de disponibilização
        const notificacao = page.locator('.alert.alert-info');
        await expect(notificacao).toBeVisible();
        const expectedDate = `${tomorrow.getDate().toString().padStart(2, '0')}/${(tomorrow.getMonth() + 1).toString().padStart(2, '0')}/${tomorrow.getFullYear()}`;
        await expect(notificacao).toHaveText(new RegExp(`O mapa de competências da unidade ${unidadeSigla}\s+foi disponibilizado para validação até ${expectedDate}. \(Simulação\)`));
    });

    test('botão "Salvar" da competência deve estar desabilitado se descrição ou atividades estiverem vazias', async ({page}) => {
        await page.getByRole('button', {name: 'Criar competência'}).click();
        const modal = page.locator('.modal.show');

        // Descrição vazia, atividades vazias
        await expect(modal.locator('[data-testid="btn-criar-competencia"]')).toBeDisabled();

        // Descrição preenchida, atividades vazias
        await modal.locator('[data-testid="input-nova-competencia"]').fill('Apenas descrição');
        await expect(modal.locator('[data-testid="btn-criar-competencia"]')).toBeDisabled();

        // Descrição vazia, atividades selecionadas
        await modal.locator('[data-testid="input-nova-competencia"]').clear();
        await modal.locator('input[type="checkbox"][value="101"]').click();
        await expect(modal.locator('[data-testid="btn-criar-competencia"]')).toBeDisabled();

        // Ambos preenchidos
        await modal.locator('[data-testid="input-nova-competencia"]').fill('Descrição e atividades');
        await expect(modal.locator('[data-testid="btn-criar-competencia"]')).toBeEnabled();
    });

    test('botão "Disponibilizar" do modal deve estar desabilitado se a data limite estiver vazia', async ({page}) => {
        // Primeiro, cria uma competência para habilitar o botão "Disponibilizar"
        await page.getByRole('button', {name: 'Criar competência'}).click();
        const modalCriacao = page.locator('.modal.show');
        await modalCriacao.locator('[data-testid="input-nova-competencia"]').fill('Competência para Disponibilizar');
        await modalCriacao.locator('input[type="checkbox"][value="101"]').click();
        await modalCriacao.locator('[data-testid="btn-criar-competencia"]').click();

        await page.getByRole('button', {name: 'Disponibilizar'}).click();

        const modalDisponibilizar = page.locator('.modal.show');
        await expect(modalDisponibilizar.getByRole('button', {name: 'Disponibilizar'})).toBeDisabled();

        await modalDisponibilizar.locator('#dataLimite').fill('2025-12-31');
        await expect(modalDisponibilizar.getByRole('button', {name: 'Disponibilizar'})).toBeEnabled();
    });
});

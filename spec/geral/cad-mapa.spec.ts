import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsAdmin} from "~/utils/auth";
import {waitForNotification} from '../cdu/test-helpers';

test.describe('Cadastro de Mapa de Competências', () => {
    test.beforeEach(async ({page}) => {
        await loginAsAdmin(page);
        await page.waitForURL('**/painel');

        // Navega diretamente para o novo endpoint padronizado
        await page.goto('/processo/4/SESEL/mapa');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForLoadState('networkidle');
    });

    test('deve criar uma nova competência com sucesso', async ({page}) => {
        await page.getByTestId('btn-abrir-criar-competencia').waitFor({state: 'visible'});
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('input-nova-competencia').waitFor({state: 'visible'});

        const competenciaDescricao = 'Nova Competência de Teste ' + Date.now();
        await page.getByTestId('input-nova-competencia').fill(competenciaDescricao);

        // Selecionar as duas primeiras atividades
        await page.locator('[data-testid="atividade-nao-associada"] label').nth(0).click();
        await page.locator('[data-testid="atividade-nao-associada"] label').nth(1).click();
        await page.waitForLoadState('domcontentloaded');

        await page.getByTestId('btn-criar-competencia').click();

        // Verificar se a nova competência aparece na lista
        await expect(page.getByText(competenciaDescricao)).toBeVisible();
    });

    test('deve editar uma competência existente', async ({page}) => {
        // Criar uma competência para editar
        await page.getByTestId('btn-abrir-criar-competencia').waitFor({state: 'visible'});
        await page.getByTestId('btn-abrir-criar-competencia').click();
        const competenciaOriginal = 'Competência para Edição ' + Date.now();
        await page.getByTestId('input-nova-competencia').fill(competenciaOriginal);
        await page.locator('[data-testid="atividade-nao-associada"] label').nth(0).click();
        await page.getByTestId('btn-criar-competencia').click();
        await expect(page.getByText(competenciaOriginal)).toBeVisible();

        // Clicar no botão de editar da competência recém-criada
        const competenciaItem = page.locator('.competencia-card', {hasText: competenciaOriginal});
        await competenciaItem.hover();
        await competenciaItem.getByTestId('btn-editar-competencia').click();

        const competenciaEditada = 'Competência Editada ' + Date.now();
        await page.getByTestId('input-nova-competencia').fill(competenciaEditada);

        // Desmarcar a primeira atividade e marcar a segunda
        await page.locator('[data-testid="atividade-nao-associada"] label').nth(0).click(); // Desmarcar
        await page.locator('[data-testid="atividade-nao-associada"] label').nth(1).click(); // Marcar

        await page.getByTestId('btn-criar-competencia').click(); // O mesmo botão "Salvar"

        // Verificar se a competência editada aparece e a original não
        await expect(page.getByText(competenciaEditada)).toBeVisible();
        await expect(page.getByText(competenciaOriginal)).not.toBeVisible();
    });

    test('deve excluir uma competência', async ({page}) => {
        // Criar uma competência para excluir
        await page.getByTestId('btn-abrir-criar-competencia').waitFor({state: 'visible'});
        await page.getByTestId('btn-abrir-criar-competencia').click();
        const competenciaParaExcluir = 'Competência para Excluir ' + Date.now();
        await page.getByTestId('input-nova-competencia').fill(competenciaParaExcluir);
        await page.locator('[data-testid="atividade-nao-associada"] label').nth(0).click();
        await page.getByTestId('btn-criar-competencia').click();
        await expect(page.getByText(competenciaParaExcluir)).toBeVisible();

        // Clicar no botão de excluir da competência recém-criada
        const competenciaItem = page.getByTestId('competencia-item').filter({hasText: competenciaParaExcluir});
        await competenciaItem.hover();
        await competenciaItem.getByTestId('btn-excluir-competencia').click();

        // Verificar se a competência não está mais visível
        await expect(page.getByText(competenciaParaExcluir)).not.toBeVisible();
    });

    test('deve disponibilizar o mapa com sucesso', async ({page}) => {
        // Criar uma competência abrangente que associe TODAS as atividades disponíveis
        await page.getByTestId('btn-abrir-criar-competencia').waitFor({state: 'visible'});
        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('input-nova-competencia').waitFor({state: 'visible'}); // Esperar o modal abrir completamente

        await page.getByTestId('input-nova-competencia').fill('Competência Completa para Teste ' + Date.now());

        // Selecionar TODAS as atividades disponíveis para passar nas validações
        const atividadeCards = page.locator('[data-testid="atividade-nao-associada"] label');

        while (await atividadeCards.count() > 0) {
            await atividadeCards.first().click();
        }

        await page.getByTestId('btn-criar-competencia').click();

        // Clicar no botão "Disponibilizar" principal
        await page.getByRole('button', {name: 'Disponibilizar'}).click();

        // Preencher a data limite (ex: 31/12/2025)
        await page.locator('#dataLimite').fill('2025-12-31');

        // Clicar no botão "Disponibilizar" dentro do modal
        await page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', {name: 'Disponibilizar'}).click();

        // Verificar a notificação de sucesso
        await waitForNotification(page, 'info', 'Mapa de competências da unidade SESEL foi disponibilizado para validação até 31/12/2025.', 'notificacao-disponibilizacao');
    });
});

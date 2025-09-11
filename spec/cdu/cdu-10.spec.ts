import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsChefe} from '~/utils/auth';
import {TEXTS, URLS} from './test-constants';
import {
    clickButton,
    expectConfirmationModal,
    expectErrorMessage,
    expectSuccessMessage,
    expectTextVisible,
    fillFormField,
    MODAL_SELECTOR
} from './test-helpers';

// Funções auxiliares para o teste
async function adicionarAtividadeComConhecimento(page: Page, atividadeDesc: string, conhecimentoDesc: string) {
    await fillFormField(page, 'Nova atividade', atividadeDesc);
    await page.getByTestId('btn-adicionar-atividade').click(); // Usar data-testid
    const atividadeCard = page.locator('.atividade-card', {hasText: atividadeDesc});
    await expect(atividadeCard).toBeVisible();

    await atividadeCard.locator('[data-testid="input-novo-conhecimento"]').fill(conhecimentoDesc);
    await atividadeCard.locator('[data-testid="btn-adicionar-conhecimento"]').click();
    await expect(atividadeCard.locator('.group-conhecimento', {hasText: conhecimentoDesc})).toBeVisible();
}

test.describe('CDU-10: Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {

    test.beforeEach(async ({page}) => {
        await loginAsChefe(page);
        await page.goto(`/processo/2/STIC/cadastro`);
        await page.waitForLoadState('networkidle');
        await page.reload();
        await expectTextVisible(page, TEXTS.CADASTRO_ATIVIDADES_CONHECIMENTOS);
    });

    test('deve permitir a disponibilização da revisão do cadastro com sucesso', async ({page}) => {
        await adicionarAtividadeComConhecimento(page, `Atividade Sucesso ${Date.now()}`, `Conhecimento Sucesso ${Date.now()}`);
        await clickButton(page, TEXTS.DISPONIBILIZAR);
        await expectConfirmationModal(page, TEXTS.DISPONIBILIZACAO_CADASTRO, TEXTS.CONFIRMA_DISPONIBILIZACAO_REVISAO);
        
        const modal = page.locator(MODAL_SELECTOR);
        await clickButton(page, TEXTS.CONFIRMAR);
        await expect(modal).not.toBeVisible();
        await expectSuccessMessage(page, 'Revisão do cadastro de atividades disponibilizada');
        await expect(page).toHaveURL(URLS.PAINEL);
    });

    async function expectDisponibilizarError(page: Page, errorTitle: string, errorMessage: string) {
        await clickButton(page, TEXTS.DISPONIBILIZAR);
        const modal = page.locator(MODAL_SELECTOR);
        await expect(modal).not.toBeVisible();
        await expectErrorMessage(page, errorTitle);
        await expectErrorMessage(page, errorMessage);
    }

    test('não deve permitir a disponibilização se houver atividades sem conhecimento', async ({page}) => {
        const atividadeDesc = `Atividade Sem Conhecimento ${Date.now()}`;
        await fillFormField(page, 'Nova atividade', atividadeDesc);
        await page.getByTestId('btn-adicionar-atividade').click();
        await expect(page.locator('.atividade-card', {hasText: atividadeDesc})).toBeVisible();

        await expectDisponibilizarError(page, 'Atividades sem Conhecimento', 'As seguintes atividades não têm conhecimentos associados e precisam ser ajustadas antes da disponibilização:');
        await expectErrorMessage(page, `- Atividade Sem Conhecimento`);
    });

    test('não deve permitir a disponibilização se o subprocesso não estiver na situação correta', async ({page}) => {
        await page.goto(`/processo/1/STIC/cadastro`);
        await page.waitForLoadState('networkidle');
        await expectTextVisible(page, TEXTS.CADASTRO_ATIVIDADES_CONHECIMENTOS);

        await expectDisponibilizarError(page, 'Erro na Disponibilização', 'A disponibilização só pode ser feita quando o subprocesso está na situação "Revisão do cadastro em andamento".');
    });

    test('deve exibir o botão Histórico de análise e abrir o modal', async ({page}) => {
        await page.getByText('Histórico de análise').click();
        const modal = page.locator(MODAL_SELECTOR);
        await expect(modal).toBeVisible();
        await expect(page.getByTestId('modal-historico-analise-titulo')).toBeVisible();
        await page.getByRole('button', {name: 'Fechar'}).click();
        await expect(modal).not.toBeVisible();
    });

});

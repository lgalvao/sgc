import {expect, Page, test} from '@playwright/test';
import {loginAsChefe} from '~/utils/auth';
import {TEXTS, URLS} from './test-constants';
import {clickButton, expectErrorMessage, expectSuccessMessage, expectTextVisible, fillFormField} from './test-helpers';

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
        // Navegar para a página de cadastro de atividades de um processo de Revisão com situação 'Revisão do cadastro em andamento'
        // Usaremos o processo 2, unidade STIC, que já configuramos no mock para essa situação
        await page.goto(`/processo/2/STIC/cadastro`);
        await page.waitForLoadState('networkidle');
        await expectTextVisible(page, TEXTS.CADASTRO_ATIVIDADES_CONHECIMENTOS);
    });

    test('deve permitir a disponibilização da revisão do cadastro com sucesso', async ({page}) => {
        // Garante que há pelo menos uma atividade com conhecimento para evitar a validação de atividades sem conhecimento
        await adicionarAtividadeComConhecimento(page, `Atividade Sucesso ${Date.now()}`, `Conhecimento Sucesso ${Date.now()}`);

        // Clicar no botão "Disponibilizar"
        await clickButton(page, TEXTS.DISPONIBILIZAR);

        // Verificar que o modal de confirmação está visível
        const modalConfirmacao = page.locator('.modal.show');
        await expect(modalConfirmacao).toBeVisible();
        await expectTextVisible(page, TEXTS.DISPONIBILIZACAO_CADASTRO);
        await expectTextVisible(page, TEXTS.CONFIRMA_DISPONIBILIZACAO_REVISAO);

        // Confirmar a disponibilização
        await clickButton(page, TEXTS.CONFIRMAR);
        await expect(modalConfirmacao).not.toBeVisible(); // Esperar o modal fechar

        // Verificar a mensagem de sucesso
        await expectSuccessMessage(page, 'Revisão do cadastro de atividades disponibilizada');

        // Verificar redirecionamento para o painel
        await expect(page).toHaveURL(URLS.PAINEL);
    });

    test('não deve permitir a disponibilização se houver atividades sem conhecimento', async ({page}) => {
        // Adicionar uma atividade SEM conhecimento
        const atividadeDesc = `Atividade Sem Conhecimento ${Date.now()}`;
        await fillFormField(page, 'Nova atividade', atividadeDesc);
        await page.getByTestId('btn-adicionar-atividade').click();
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeDesc});
        await expect(atividadeCard).toBeVisible();

        // Clicar no botão "Disponibilizar"
        await clickButton(page, TEXTS.DISPONIBILIZAR);

        // Verificar que o modal de confirmação NÃO aparece
        const modalConfirmacao = page.locator('.modal.show');
        await expect(modalConfirmacao).not.toBeVisible();

        // Verificar a mensagem de erro
        await expectErrorMessage(page, 'Atividades sem Conhecimento');
        await expectErrorMessage(page, 'As seguintes atividades não têm conhecimentos associados e precisam ser ajustadas antes da disponibilização:');
        await expectErrorMessage(page, `- Atividade Sem Conhecimento`); // Verifica parte da descrição da atividade
    });

    test('não deve permitir a disponibilização se o subprocesso não estiver na situação correta', async ({page}) => {
        // Navegar para um processo com situação diferente (ex: Processo 1, Unidade STIC - "Cadastro em andamento")
        await page.goto(`/processo/1/STIC/cadastro`);
        await page.waitForLoadState('networkidle');
        await expectTextVisible(page, TEXTS.CADASTRO_ATIVIDADES_CONHECIMENTOS);

        // Clicar no botão "Disponibilizar"
        await clickButton(page, TEXTS.DISPONIBILIZAR);

        // Verificar que o modal de confirmação NÃO aparece
        const modalConfirmacao = page.locator('.modal.show');
        await expect(modalConfirmacao).not.toBeVisible();

        // Verificar a mensagem de erro
        await expectErrorMessage(page, 'Erro na Disponibilização');
        await expectErrorMessage(page, 'A disponibilização só pode ser feita quando o subprocesso está na situação "Revisão do cadastro em andamento".');
    });

    // Teste para o botão "Histórico de análise" (já coberto pela CDU-09, mas podemos adicionar um básico aqui)
    test('deve exibir o botão Histórico de análise e abrir o modal', async ({page}) => {
        // O mock do processo 2/STIC já tem análises, então o botão deve estar visível
        const botaoHistoricoAnalise = page.getByText('Histórico de análise');
        await expect(botaoHistoricoAnalise).toBeVisible();
        await botaoHistoricoAnalise.click();

        const modalHistorico = page.locator('.modal.show');
        await expect(modalHistorico).toBeVisible();
        await expect(page.getByTestId('modal-historico-analise-titulo')).toBeVisible();

        await page.getByRole('button', {name: 'Fechar'}).click();
        await expect(modalHistorico).not.toBeVisible();
    });

});

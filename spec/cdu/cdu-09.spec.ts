import {expect, test} from '@playwright/test';
import {loginAsChefe} from '~/utils/auth';
import {TEXTS} from './test-constants';
import {expectTextVisible, fillFormField} from './test-helpers';

test.describe('CDU-09: Disponibilizar cadastro de atividades e conhecimentos', () => {
    test('deve mostrar botão Histórico de análise quando houver análises e permitir disponibilização do cadastro', async ({page}) => {
        // Login como CHEFE
        await loginAsChefe(page);

        // Navegar diretamente para a página de cadastro de atividades de um processo de Revisão com análises (Processo ID 2, Unidade STIC)
        await page.goto('/processo/2/STIC/cadastro');

        // Verificar que estamos na página de cadastro de atividades
        await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+\/cadastro/);
        await expectTextVisible(page, TEXTS.CADASTRO_ATIVIDADES_CONHECIMENTOS);

        // Verificar que o botão "Histórico de análise" está visível (condição para quando há análises)
        // Como estamos simulando um cenário com análises, o botão deve aparecer
        const botaoHistoricoAnalise = page.getByText('Histórico de análise');
        await expect(botaoHistoricoAnalise).toBeVisible();

        // Clicar no botão "Histórico de análise"
        await botaoHistoricoAnalise.click();

        // Verificar que o modal de histórico de análise está visível
        const modalHistorico = page.locator('.modal.show');
        await expect(modalHistorico).toBeVisible();
        await expect(page.getByTestId('modal-historico-analise-titulo')).toBeVisible();

        // Verificar que a tabela de análises está presente
        const tabelaAnalises = modalHistorico.locator('table');
        await expect(tabelaAnalises).toBeVisible();

        // Verificar colunas da tabela
        const historicoAnaliseTable = modalHistorico.getByTestId('historico-analise-tabela'); // Scoped within modalHistorico
        await expect(historicoAnaliseTable.getByText('Data/Hora')).toBeVisible();
        await expect(historicoAnaliseTable.getByText('Unidade')).toBeVisible();
        await expect(historicoAnaliseTable.getByText('Resultado')).toBeVisible();
        await expect(historicoAnaliseTable.getByText('Observações')).toBeVisible();

        // Fechar o modal
        await page.getByRole('button', {name: 'Fechar'}).click();
        await expect(modalHistorico).not.toBeVisible();

        // Adicionar uma atividade para teste
        await fillFormField(page, 'Nova atividade', 'Atividade de teste para CDU-09');
        await page.getByTestId('btn-adicionar-atividade').click();

        // Adicionar um conhecimento para a atividade
        const inputConhecimento = page.getByTestId('input-novo-conhecimento').first();
        await inputConhecimento.fill('Conhecimento de teste para CDU-09');
        const btnAdicionarConhecimento = page.getByTestId('btn-adicionar-conhecimento').first();
        await btnAdicionarConhecimento.click();

        // Clicar no botão "Disponibilizar"
        await page.getByRole('button', {name: TEXTS.DISPONIBILIZAR}).click();

        // Verificar que o modal de confirmação está visível
        const modalConfirmacao = page.locator('.modal.show');
        await expect(modalConfirmacao).toBeVisible();
        // Esperar que o texto dentro do modal esteja visível
        await page.waitForSelector('.modal.show p:has-text("Confirma a finalização da revisão e a disponibilização do cadastro?")');
        await expectTextVisible(page, TEXTS.DISPONIBILIZACAO_CADASTRO);
        await expectTextVisible(page, TEXTS.CONFIRMA_DISPONIBILIZACAO_REVISAO);

        // Confirmar a disponibilização
        await page.getByRole('button', {name: TEXTS.CONFIRMAR}).click();
        await expect(modalConfirmacao).not.toBeVisible(); // Esperar o modal fechar

        // Verificar redirecionamento para o painel
        await expect(page).toHaveURL(/\/painel/);
    });

    test('não deve mostrar botão Histórico de análise quando não houver análises', async ({page}) => {
        // Login como CHEFE
        await loginAsChefe(page);

        // Navegar para um processo do tipo Mapeamento (que não tem análises)
        // Primeiro vamos para o painel
        await page.goto('/painel');

        // Encontrar e clicar em um processo do tipo "Mapeamento"
        const processoMapeamento = page.locator('table tbody tr').filter({hasText: 'Mapeamento'}).first();
        await processoMapeamento.click();

        // Verificar que estamos na página do subprocesso
        await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+/);

        // Clicar no card "Atividades e conhecimentos" usando o data-testid
        await page.getByTestId('atividades-card').click();

        // Verificar que o botão "Histórico de análise" não está visível
        const botaoHistoricoAnalise = page.getByText('Histórico de análise');
        await expect(botaoHistoricoAnalise).not.toBeVisible();

        // Verificar que o botão "Disponibilizar" está visível
        const botaoDisponibilizar = page.getByRole('button', {name: TEXTS.DISPONIBILIZAR});
        await expect(botaoDisponibilizar).toBeVisible();
    });
});
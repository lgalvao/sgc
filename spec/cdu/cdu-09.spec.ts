import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {
    adicionarAtividade,
    adicionarConhecimento,
    esperarUrl,
    loginComoChefe,
    navegarParaCadastroAtividades
} from './auxiliares-verificacoes';
import {disponibilizarCadastro} from './auxiliares-acoes';
import {DADOS_TESTE, SELETORES_CSS, TEXTOS, URLS} from './constantes-teste';

test.describe('CDU-09: Disponibilizar cadastro de atividades e conhecimentos', () => {
    test.beforeEach(async ({page}) => await loginComoChefe(page));

    test('deve mostrar botão Histórico de análise e permitir disponibilização', async ({page}) => {
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        const botaoHistorico = page.getByText('Histórico de análise');
        await expect(botaoHistorico).toBeVisible();

        await botaoHistorico.click();

        const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
        await expect(modal).toBeVisible();
        await expect(page.getByTestId('modal-historico-analise-titulo')).toBeVisible();

        const tabela = modal.locator('table');
        await expect(tabela).toBeVisible();

        const tabelaAnalises = modal.getByTestId('historico-analise-tabela');
        await expect(tabelaAnalises.getByText('Data/Hora')).toBeVisible();
        await expect(tabelaAnalises.getByText('Unidade')).toBeVisible();
        await expect(tabelaAnalises.getByText('Resultado')).toBeVisible();
        await expect(tabelaAnalises.getByText('Observações')).toBeVisible();

        await page.getByRole('button', {name: 'Fechar'}).click();
        await expect(modal).not.toBeVisible();

        const nomeAtividade = `Atividade de teste para CDU-09 ${Date.now()}`;
        await adicionarAtividade(page, nomeAtividade);

        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        const nomeConhecimento = `Conhecimento de teste para CDU-09 ${Date.now()}`;
        await adicionarConhecimento(cardAtividade, nomeConhecimento);

        // Assegura que o botão está habilitado antes da ação
        const botaoDisponibilizar = page.getByRole('button', {name: TEXTOS.DISPONIBILIZAR});
        await expect(botaoDisponibilizar).toBeEnabled();

        await disponibilizarCadastro(page);
        await esperarUrl(page, URLS.PAINEL);
    });
});
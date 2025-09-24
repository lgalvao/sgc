import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarBotao,
    esperarElementoVisivel,
    esperarMensagemErro,
    esperarMensagemSucesso,
    esperarUrl,
    loginComoChefe,
    navegarParaCadastroAtividades
} from './auxiliares-verificacoes';
import {disponibilizarCadastro} from './auxiliares-acoes';
import {DADOS_TESTE, SELETORES_CSS, TEXTOS, URLS} from './constantes-teste';

async function adicionarAtividadeComConhecimento(page: Page, atividadeDesc: string, conhecimentoDesc: string) {
    await adicionarAtividade(page, atividadeDesc);
    const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: atividadeDesc});
    await adicionarConhecimento(cardAtividade, conhecimentoDesc);
}

test.describe('CDU-10: Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
    test.beforeEach(async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
    });

    test('deve permitir disponibilização da revisão com sucesso', async ({page}) => {
        const nomeAtividade = `Atividade Sucesso ${Date.now()}`;
        const nomeConhecimento = `Conhecimento Sucesso ${Date.now()}`;

        await adicionarAtividadeComConhecimento(page, nomeAtividade, nomeConhecimento);

        await disponibilizarCadastro(page);

        await esperarMensagemSucesso(page, 'Revisão do cadastro de atividades disponibilizada');
        await esperarUrl(page, URLS.PAINEL);
    });

    test('não deve permitir disponibilização se houver atividades sem conhecimento', async ({page}) => {
        const nomeAtividade = `Atividade Sem Conhecimento ${Date.now()}`;
        await adicionarAtividade(page, nomeAtividade);

        await clicarBotao(page, TEXTOS.DISPONIBILIZAR);

        const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
        await expect(modal).not.toBeVisible();

        await esperarMensagemErro(page, 'Atividades sem Conhecimento');
        await esperarMensagemErro(page, 'As seguintes atividades não têm conhecimentos associados');
    });

    test('não deve permitir disponibilização se subprocesso não estiver na situação correta', async ({page}) => {
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await clicarBotao(page, TEXTOS.DISPONIBILIZAR);

        const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
        await expect(modal).not.toBeVisible();

        await esperarMensagemErro(page, 'Erro na Disponibilização');
        await esperarMensagemErro(page, 'A disponibilização só pode ser feita quando o subprocesso está na situação');
    });

    test('deve exibir botão Histórico de análise e abrir modal', async ({page}) => {
        await page.getByText('Histórico de análise').click();

        const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
        await expect(modal).toBeVisible();
        await esperarElementoVisivel(page, 'modal-historico-analise-titulo');

        await page.getByRole('button', {name: 'Fechar'}).click();
        await expect(modal).not.toBeVisible();
    });
});
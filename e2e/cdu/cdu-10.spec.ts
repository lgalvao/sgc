import {Page} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarBotao,
    DADOS_TESTE,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaCadastroAtividades,
    SELETORES_CSS,
    TEXTOS,
    URLS,
    verificarModalFechado,
    verificarModalHistoricoAnaliseAberto
} from './helpers';
import {esperarMensagemErro, esperarMensagemSucesso, esperarUrl} from "./helpers/verificacoes/verificacoes-basicas";

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
        const nomeAtividade = gerarNomeUnico("Atividade Sucesso");
        const nomeConhecimento = gerarNomeUnico("Conhecimento Sucesso");

        await adicionarAtividadeComConhecimento(page, nomeAtividade, nomeConhecimento);
        await disponibilizarCadastro(page);

        await esperarMensagemSucesso(page, 'Revisão do cadastro de atividades disponibilizada');
        await esperarUrl(page, URLS.PAINEL);
    });

    test('não deve permitir disponibilização se houver atividades sem conhecimento', async ({page}) => {
        const nomeAtividade = gerarNomeUnico("Atividade Sem Conhecimento");

        await adicionarAtividade(page, nomeAtividade);
        await clicarBotao(page, TEXTOS.DISPONIBILIZAR);
        await verificarModalFechado(page);

        await esperarMensagemErro(page, 'Atividades sem Conhecimento');
        await esperarMensagemErro(page, 'As seguintes atividades não têm conhecimentos associados');
    });

    test('não deve permitir disponibilização se subprocesso não estiver na situação correta', async ({page}) => {
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await clicarBotao(page, TEXTOS.DISPONIBILIZAR);
        await verificarModalFechado(page);

        await esperarMensagemErro(page, 'Erro na Disponibilização');
        await esperarMensagemErro(page, 'A disponibilização só pode ser feita quando o subprocesso está na situação');
    });

    test('deve exibir botão Histórico de análise e abrir modal', async ({page}) => {
        await page.getByText('Histórico de análise').click();

        await verificarModalHistoricoAnaliseAberto(page);

        await page.getByRole('button', {name: 'Fechar'}).click();
        await verificarModalFechado(page);
    });
});
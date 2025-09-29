import {test} from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    DADOS_TESTE,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaCadastroAtividades,
    SELETORES_CSS,
    verificarAtividadeVisivel,
    verificarBotaoDisponibilizarHabilitado,
    verificarBotaoHistoricoAnaliseVisivel,
    verificarConhecimentoVisivel,
    verificarModalFechado,
    verificarModalHistoricoAnaliseAberto,
    verificarUrlDoPainel
} from './helpers';

test.describe('CDU-09: Disponibilizar cadastro de atividades e conhecimentos', () => {
    const PROCESSO_REVISAO = DADOS_TESTE.PROCESSOS.REVISAO_STIC;
    const UNIDADE_STIC = DADOS_TESTE.UNIDADES.STIC;

    test.beforeEach(async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, PROCESSO_REVISAO.id, UNIDADE_STIC);
    });

    test('deve exibir e interagir com o modal de histórico de análise', async ({page}) => {
        await verificarBotaoHistoricoAnaliseVisivel(page);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);
        await cancelarModal(page);
        await verificarModalFechado(page);
    });

    test('deve permitir a disponibilização do cadastro após preenchimento', async ({page}) => {
        const nomeAtividade = gerarNomeUnico('Atividade para CDU-09');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        const nomeConhecimento = gerarNomeUnico('Conhecimento para CDU-09');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);

        await verificarBotaoDisponibilizarHabilitado(page);
        await disponibilizarCadastro(page);

        await verificarUrlDoPainel(page);
    });
});

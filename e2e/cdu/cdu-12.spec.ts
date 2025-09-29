import {test} from '@playwright/test';
import {
    adicionarConhecimentoPrimeiraAtividade,
    clicarBotaoImpactosMapa,
    DADOS_TESTE,
    fecharModalImpactos,
    gerarNomeUnico,
    irParaMapaCompetencias,
    loginComoAdmin,
    navegarParaCadastroAtividades,
    verificarMensagemNenhumImpacto,
    verificarModalImpactosAberto,
    verificarModalImpactosFechado,
} from './helpers';

test.describe('CDU-12: Verificar impactos no mapa de competências', () => {
    const PROCESSO_REVISAO = DADOS_TESTE.PROCESSOS.REVISAO_STIC;
    const UNIDADE_SESEL = 'SESEL';

    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve exibir mensagem de "Nenhum impacto" quando não houver divergências', async ({page}) => {
        await irParaMapaCompetencias(page, PROCESSO_REVISAO.id, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);
        await verificarMensagemNenhumImpacto(page);
    });

    test('deve exibir modal com impactos quando houver divergências', async ({page}) => {
        // Setup: Adiciona um conhecimento para gerar um impacto
        await navegarParaCadastroAtividades(page, PROCESSO_REVISAO.id, UNIDADE_SESEL);
        await adicionarConhecimentoPrimeiraAtividade(page, gerarNomeUnico('Conhecimento Impacto'));

        // Ação: Navega para o mapa e abre o modal de impactos
        await irParaMapaCompetencias(page, PROCESSO_REVISAO.id, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);

        // Verificação: Confere se o modal de impactos está correto
        await verificarModalImpactosAberto(page);

        // Limpeza: Fecha o modal
        await fecharModalImpactos(page);
        await verificarModalImpactosFechado(page);
    });
});

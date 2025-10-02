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
    const UNIDADE_SESEL = 'SESEL';
    const ID_PROC_REV_STIC = DADOS_TESTE.PROCESSOS.REVISAO_STIC.id;

    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve exibir mensagem de "Nenhum impacto" quando não houver divergências', async ({page}) => {
        await irParaMapaCompetencias(page, ID_PROC_REV_STIC, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);
        await verificarMensagemNenhumImpacto(page);
    });

    test('deve exibir modal com impactos quando houver divergências', async ({page}) => {
        // Adiciona um conhecimento para gerar um impacto
        await navegarParaCadastroAtividades(page, ID_PROC_REV_STIC, UNIDADE_SESEL);
        await adicionarConhecimentoPrimeiraAtividade(page, gerarNomeUnico('Conhecimento Impacto'));

        // Navega para o mapa e abre o modal de impactos
        await irParaMapaCompetencias(page, ID_PROC_REV_STIC, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);

        // Confere se o modal de impactos está correto
        await verificarModalImpactosAberto(page);

        // Fecha o modal
        await fecharModalImpactos(page);
        await verificarModalImpactosFechado(page);
    });
});

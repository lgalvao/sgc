import {vueTest as test} from './support/vue-specific-setup';
import {
    adicionarConhecimentoPrimeiraAtividade,
    clicarBotaoImpactosMapa,
    fecharModalImpactos,
    gerarNomeUnico,
    irParaMapaCompetencias,
    loginComoAdmin,
    navegarParaCadastroAtividades,
    verificarMensagemNenhumImpacto,
    verificarModalImpactosAberto,
    verificarModalImpactosFechado,
    criarProcessoCompleto,
    iniciarProcesso
} from './helpers';

test.describe('CDU-12: Verificar impactos no mapa de competências', () => {
    const UNIDADE_SESEL = 'SESEL';
    let processo: any;

    test.beforeEach(async ({ page }) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-12');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'REVISAO', '2025-12-31', [10]); // Unidade 10 = SESEL
        await iniciarProcesso(page);
        await loginComoAdmin(page);
    });

    test('deve exibir mensagem de "Nenhum impacto" quando não houver divergências', async ({page}) => {
        await irParaMapaCompetencias(page, processo.processo.codigo, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);
        await verificarMensagemNenhumImpacto(page);
    });

    test('deve exibir modal com impactos quando houver divergências', async ({page}) => {
        // Adiciona um conhecimento para gerar um impacto
        await navegarParaCadastroAtividades(page, processo.processo.codigo, UNIDADE_SESEL);
        await adicionarConhecimentoPrimeiraAtividade(page, gerarNomeUnico('Conhecimento Impacto'));

        // Navega para o mapa e abre o modal de impactos
        await irParaMapaCompetencias(page, processo.processo.codigo, UNIDADE_SESEL);
        await clicarBotaoImpactosMapa(page);

        // Confere se o modal de impactos está correto
        await verificarModalImpactosAberto(page);

        // Fecha o modal
        await fecharModalImpactos(page);
        await verificarModalImpactosFechado(page);
    });
});

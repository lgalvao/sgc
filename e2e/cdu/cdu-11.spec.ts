import {vueTest as test} from '../support/vue-specific-setup';
import {
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    navegarParaProcessoPorId,
    verificarAtividadeVisivel,
    verificarConhecimentoNaAtividade,
    verificarModoSomenteLeitura,
} from '../helpers';

test.describe('CDU-11: Visualizar cadastro de atividades (somente leitura)', () => {
    let processo: any;
    const nomeAtividade = gerarNomeUnico('Atividade para Visualizar');
    const nomeConhecimento = gerarNomeUnico('Conhecimento para Visualizar');

    async function setup(page) {
        // Setup: Cria um processo, adiciona dados e disponibiliza o cadastro
        const context = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-11'), 'MAPEAMENTO', '2025-12-31', [1]);
        processo = context.processo;

        // Adiciona uma atividade e conhecimento via UI para simular o fluxo real
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await disponibilizarCadastro(page);
    }

    test.beforeEach(async ({page}) => {
        await setup(page);
    });

    test('ADMIN deve visualizar cadastro em modo somente leitura', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });

    test('GESTOR da unidade superior deve visualizar cadastro em modo somente leitura', async ({page}) => {
        await loginComoGestor(page); // Gestor da SGP (unidade superior à STIC)
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });

    test('CHEFE de outra unidade não deve ver os botões de edição', async ({page}) => {
        // Loga como chefe de uma unidade que não é a STIC, mas está no processo
        await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-11 OUTRA UNIDADE'), 'MAPEAMENTO', '2025-12-31', [3]); // Adiciona a unidade 3 (SESEL)
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });
});

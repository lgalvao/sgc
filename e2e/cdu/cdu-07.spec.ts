import {vueTest as test} from '../support/vue-specific-setup';
import {
    aceitarCadastro,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    criarProcessoCompleto,
    disponibilizarCadastro,
    esperarElementoVisivel,
    gerarNomeUnico,
    irParaSubprocesso,
    loginComoChefe,
    loginComoGestor,
    navegarParaCadastroAtividades,
    SELETORES,
    verificarCardAcaoInvisivel,
    verificarCardAcaoVisivel,
} from '~/helpers';

test.describe('CDU-07: Detalhar subprocesso', () => {
    let processo: any;
    const siglaUnidade = 'STIC';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO CDU-07');
        // Alterado [1] para ['STIC']
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', ['STIC']);
    });

    test('deve mostrar detalhes do subprocesso para CHEFE', async ({page}) => {
        await loginComoChefe(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
        await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
    });

    test('CHEFE deve ver card de Cadastro de atividades em CADASTRO_EM_ANDAMENTO', async ({page}) => {
        await loginComoChefe(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await verificarCardAcaoVisivel(page, 'Cadastro de atividades');
        await verificarCardAcaoInvisivel(page, 'Mapa de competências');
    });

    test('CHEFE deve ver card de Mapa de competências em MAPA_EM_ANDAMENTO', async ({page}) => {
        // CHEFE disponibiliza o cadastro
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processo.processo.codigo, siglaUnidade);
        const nomeAtividade = 'Atividade de Teste';
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, 'Conhecimento de Teste');
        await disponibilizarCadastro(page);

        // GESTOR aceita o cadastro
        await loginComoGestor(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);
        await aceitarCadastro(page, 'Teste');

        // CHEFE verifica o card de Mapa de competências
        await loginComoChefe(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await verificarCardAcaoInvisivel(page, 'Cadastro de atividades');
        await verificarCardAcaoVisivel(page, 'Mapa de competências');
    });

    test('GESTOR deve ver card de Análise de cadastro em CADASTRO_DISPONIBILIZADO', async ({page}) => {
        // CHEFE disponibiliza o cadastro
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processo.processo.codigo, siglaUnidade);
        const nomeAtividade = 'Atividade de Teste';
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, 'Conhecimento de Teste');
        await disponibilizarCadastro(page);

        // GESTOR verifica o card de Análise de cadastro
        await loginComoGestor(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await verificarCardAcaoVisivel(page, 'Análise de cadastro');
        await verificarCardAcaoInvisivel(page, 'Cadastro de atividades');
        await verificarCardAcaoInvisivel(page, 'Mapa de competências');
    });
});

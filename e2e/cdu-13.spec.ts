import {vueTest as test} from './support/vue-specific-setup';
import { loginComoAdmin, loginComoGestor, loginComoChefe } from './helpers/auth';
import {
    navegarParaVisualizacaoAtividades,
    navegarParaCadastroAtividades,
} from './helpers/navegacao/navegacao';
import {
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
} from './helpers/acoes/acoes-atividades';
import {
    esperarMensagemSucesso,
    verificarUrl,
    verificarUrlDoPainel,
} from './helpers/verificacoes/verificacoes-basicas';
import {
    verificarModalHistoricoAnaliseAberto,
} from './helpers/verificacoes/verificacoes-ui';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';
import { TEXTOS } from './helpers/dados/constantes-teste';

test.describe('CDU-13: Analisar cadastro de atividades e conhecimentos', () => {
    let processo: any;
    const SIGLA_STIC = 'STIC';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-13');
        const processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['2']); // Unidade 2 = STIC
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };

        // Chefe da STIC preenche e disponibiliza o cadastro
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processo.processo.codigo, SIGLA_STIC);
        const nomeAtividade = gerarNomeUnico('Atividade CDU-13');
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, gerarNomeUnico('Conhecimento CDU-13'));
        await page.getByRole('button', { name: 'Disponibilizar' }).click();
    });

    test('deve exibir modal de Histórico de análise', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await page.getByRole('button', { name: 'Histórico de Análise' }).click();
        await verificarModalHistoricoAnaliseAberto(page);

        await page.getByRole('button', { name: 'Cancelar' }).click();
    });

    test('GESTOR deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await page.getByRole('button', { name: 'Devolver' }).click();

        await esperarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('ADMIN deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await page.getByRole('button', { name: 'Devolver' }).click();

        await esperarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('GESTOR deve conseguir registrar aceite do cadastro', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await page.getByRole('button', { name: 'Aceitar' }).click();

        await esperarMensagemSucesso(page, TEXTOS.ANALISE_REGISTRADA_SUCESSO);
        await verificarUrlDoPainel(page);
    });

    test('ADMIN deve conseguir homologar o cadastro', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await page.getByRole('button', { name: 'Homologar' }).click();

        await esperarMensagemSucesso(page, TEXTOS.CADASTRO_HOMOLOGADO_SUCESSO);
        await verificarUrl(page, `/processo/${processo.processo.codigo}/${SIGLA_STIC}`);
    });
});

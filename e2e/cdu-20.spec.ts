import {vueTest as test} from './support/vue-specific-setup';
import { loginComoAdmin, loginComoGestor } from './helpers/auth';
import { irParaVisualizacaoMapa } from './helpers/navegacao/navegacao';
import {
    cancelarModal,
} from './helpers/acoes/acoes-modais';
import {
    verificarAcaoHomologarVisivel,
    verificarAcoesAnaliseGestor,
    verificarModalHistoricoAnaliseAberto,
} from './helpers/verificacoes/verificacoes-ui';
import {
    verificarAceiteRegistradoComSucesso,
    verificarCadastroDevolvidoComSucesso,
} from './helpers/verificacoes/verificacoes-processo';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';

test.describe('CDU-20: Analisar validação de mapa de competências', () => {
    let processo: any;
    const siglaUnidade = 'SEDESENV';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-20');
        const processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['8']); // Unidade 8 = SEDESENV
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };
    });

    test.describe('GESTOR', () => {
        test.beforeEach(async ({page}) => await loginComoGestor(page));

        test('deve exibir botões para GESTOR analisar mapa validado', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await verificarAcoesAnaliseGestor(page);
        });

        test('deve permitir devolver para ajustes', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await page.getByRole('button', { name: 'Devolver' }).click();
            await verificarCadastroDevolvidoComSucesso(page);
        });

        test('deve permitir registrar aceite', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await page.getByRole('button', { name: 'Aceitar' }).click();
            await verificarAceiteRegistradoComSucesso(page);
        });

        test('deve cancelar devolução', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);

            // Abrir diálogo de devolução e cancelar
            await page.getByRole('button', { name: 'Devolver' }).click();
            await cancelarModal(page);
        });
    });

    test.describe('ADMIN', () => {
        test.beforeEach(async ({page}) => await loginComoAdmin(page));

        test('deve exibir botão Homologar para ADMIN', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await verificarAcaoHomologarVisivel(page);
        });

        test('deve permitir homologar mapa', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await page.getByRole('button', { name: 'Homologar' }).click();
            await verificarAceiteRegistradoComSucesso(page);
        });
    });

    test.describe('Ver sugestões', () => {
        test('deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"', async ({page}) => {
            await loginComoGestor(page);
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await page.getByRole('button', { name: 'Histórico de Análise' }).click();
            await verificarModalHistoricoAnaliseAberto(page);
        });
    });

    test.describe('Histórico de análise', () => {
        test('deve exibir histórico de análise', async ({page}) => {
            await loginComoGestor(page);
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await page.getByRole('button', { name: 'Histórico de Análise' }).click();
            await verificarModalHistoricoAnaliseAberto(page);
        });
    });
});

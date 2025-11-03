import {vueTest as test} from './support/vue-specific-setup';
import {
    acessarAnaliseRevisaoComoAdmin,
    acessarAnaliseRevisaoComoGestor,
} from './helpers/navegacao/navegacao';
import {
    verificarAcaoHomologarVisivel,
    verificarAcoesAnaliseGestor,
    verificarModalHistoricoAnaliseAberto,
} from './helpers/verificacoes/verificacoes-ui';
import {
    verificarAceiteRegistradoComSucesso,
    verificarCadastroDevolvidoComSucesso,
} from './helpers/verificacoes/verificacoes-processo';
import { esperarUrl } from './helpers/verificacoes/verificacoes-basicas';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';

test.describe('CDU-14: Analisar revisão de cadastro de atividades e conhecimentos', () => {
    let processo: any;

    test.beforeEach(async ({ page }) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO CDU-14');
        const processoId = await criarProcesso(page, 'REVISAO', nomeProcesso, ['1']);
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };
    });

    test('deve apresentar ações adequadas para cada perfil', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await verificarAcoesAnaliseGestor(page);

        await acessarAnaliseRevisaoComoAdmin(page, processo.processo.codigo, 'STIC');
        await verificarAcaoHomologarVisivel(page);
    });

    test('deve permitir devolver e registrar aceite da revisão', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await page.getByRole('button', { name: 'Devolver' }).click();
        await verificarCadastroDevolvidoComSucesso(page);

        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await page.getByRole('button', { name: 'Aceitar' }).click();
        await verificarAceiteRegistradoComSucesso(page);
    });

    test('deve exibir histórico de análise', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await page.getByRole('button', { name: 'Histórico de Análise' }).click();
        await verificarModalHistoricoAnaliseAberto(page);

        await page.getByRole('button', { name: 'Cancelar' }).click();
    });

    test('ADMIN deve homologar revisão do cadastro', async ({page}) => {
        // Primeiro, o gestor precisa aceitar para que o ADMIN possa homologar
        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await page.getByRole('button', { name: 'Aceitar' }).click();
        await verificarAceiteRegistradoComSucesso(page);

        // Agora o ADMIN pode homologar
        await acessarAnaliseRevisaoComoAdmin(page, processo.processo.codigo, 'STIC');
        await page.getByRole('button', { name: 'Homologar' }).click();
        await esperarUrl(page, new RegExp(`/processo/${processo.processo.codigo}/STIC`));
    });
});

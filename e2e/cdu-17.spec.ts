import {vueTest as test} from './support/vue-specific-setup';
import {
    criarCompetencia,
} from './helpers/acoes/acoes-mapa';
import {
    esperarTextoVisivel,
} from './helpers/verificacoes/verificacoes-basicas';
import {
    verificarBotaoDisponibilizarVisivel,
} from './helpers/verificacoes/verificacoes-ui';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';
import { navegarParaMapaMapeamento } from './helpers/navegacao/navegacao';
import { TEXTOS } from './helpers/dados/constantes-teste';

let processo: any;
const siglaUnidade = 'SESEL';

test.describe('CDU-17: Disponibilizar mapa de competências', () => {
    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-17');
        const processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['10']); // Unidade 10 = SESEL
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };
    });

    test('deve exibir modal com título e campos corretos', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await page.getByRole('button', { name: 'Disponibilizar' }).click();
    });

    test('deve preencher observações no modal', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await page.getByRole('button', { name: 'Disponibilizar' }).click();
    });

    test('deve validar data obrigatória', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await page.getByRole('button', { name: 'Disponibilizar' }).click();
    });

    test('deve validar campos obrigatórios do modal', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência para Validação', []);

        await page.getByRole('button', { name: 'Disponibilizar' }).click();
    });

    test('deve processar disponibilização', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência para Disponibilizar', []);

        await page.getByRole('button', { name: 'Disponibilizar' }).click();

        await page.getByRole('button', { name: 'Confirmar' }).click();
    });

    test('deve cancelar disponibilização', async ({page}) => {
        await navegarParaMapaMapeamento(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Teste', []);

        await page.getByRole('button', { name: 'Disponibilizar' }).click();
        await page.getByRole('button', { name: 'Cancelar' }).click();

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });
});

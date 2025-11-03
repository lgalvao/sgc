import {vueTest as test} from './support/vue-specific-setup';
import {
    clicarBotaoImpactosMapa,
    criarCompetencia,
    editarCompetencia,
    excluirCompetencia,
} from './helpers/acoes/acoes-mapa';
import {
    esperarElementoVisivel,
    esperarTextoVisivel,
    verificarDisponibilizacaoConcluida,
} from './helpers/verificacoes/verificacoes-basicas';
import {
    verificarCompetenciaNaoVisivel,
    verificarCompetenciaVisivel,
    verificarImpactosNoMapa,
} from './helpers/verificacoes/verificacoes-ui';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';
import { navegarParaMapaRevisao } from './helpers/navegacao/navegacao';
import { SELETORES, TEXTOS } from './helpers/dados/constantes-teste';

let processo: any;
const siglaUnidade = 'SESEL';

test.describe('CDU-16: Ajustar mapa de competências', () => {
    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-16');
        const processoId = await criarProcesso(page, 'REVISAO', nomeProcesso, ['10']); // Unidade 10 = SESEL
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };
    });

    test('deve exibir botão "Impacto no mapa" para ADMIN em processo de Revisão', async ({page}) => {
        await navegarParaMapaRevisao(page, processo.processo.codigo, siglaUnidade);

        await esperarElementoVisivel(page, SELETORES.BTN_IMPACTOS_MAPA);
        await esperarTextoVisivel(page, TEXTOS.IMPACTO_NO_MAPA);
    });

    test('deve abrir modal de impactos no mapa', async ({page}) => {
        await navegarParaMapaRevisao(page, processo.processo.codigo, siglaUnidade);
        await clicarBotaoImpactosMapa(page);
        await verificarImpactosNoMapa(page);
    });

    test('deve permitir criação de competências', async ({page}) => {
        await navegarParaMapaRevisao(page, processo.processo.codigo, siglaUnidade);
        const nome = 'Competência CDU-16';
        await criarCompetencia(page, nome, []);

        await verificarCompetenciaVisivel(page, nome);
        await esperarElementoVisivel(page, SELETORES.EDITAR_COMPETENCIA);
        await esperarElementoVisivel(page, SELETORES.EXCLUIR_COMPETENCIA);
    });

    test('deve permitir edição de competências', async ({page}) => {
        const competenciaOriginal = 'Competência Original';
        const competenciaEditada = 'Competência Editada';

        await navegarParaMapaRevisao(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, competenciaOriginal, []);
        await editarCompetencia(page, competenciaOriginal, competenciaEditada, []);

        await verificarCompetenciaVisivel(page, competenciaEditada);
        await verificarCompetenciaNaoVisivel(page, competenciaOriginal);
    });

    test('deve permitir exclusão de competências', async ({page}) => {
        const nome = 'Competência para Excluir';

        await navegarParaMapaRevisao(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, nome, []);

        await excluirCompetencia(page, nome);
        await verificarCompetenciaNaoVisivel(page, nome);
    });

    test('deve validar associação de todas as atividades', async ({page}) => {
        await navegarParaMapaRevisao(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Parcial', []);
        await page.getByRole('button', { name: 'Disponibilizar' }).click();
        await verificarDisponibilizacaoConcluida(page);
    });

    test('deve integrar com disponibilização de mapa', async ({page}) => {
        await navegarParaMapaRevisao(page, processo.processo.codigo, siglaUnidade);
        await criarCompetencia(page, 'Competência Completa', []);
        await page.getByRole('button', { name: 'Disponibilizar' }).click();
        await verificarCompetenciaVisivel(page, 'Competência Completa');
    });
});

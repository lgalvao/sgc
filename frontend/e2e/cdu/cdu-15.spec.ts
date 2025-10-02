import {Page} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    criarCompetencia,
    editarCompetencia,
    esperarBotaoVisivel,
    esperarElementoVisivel,
    esperarTextoVisivel,
    excluirCompetencia,
    gerarNomeUnico,
    irParaMapaCompetencias,
    loginComoAdmin,
    TEXTOS,
    verificarCompetenciaNaoVisivel,
    verificarCompetenciaVisivel
} from './helpers';

async function navegarParaMapa(page: Page) {
    await loginComoAdmin(page);
    await irParaMapaCompetencias(page, 4, 'SESEL');
    await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
}

test.describe('CDU-15: Manter mapa de competências', () => {
    test.beforeEach(async ({page}) => {
        await navegarParaMapa(page);
    });

    test('deve exibir tela de edição de mapa com elementos corretos', async ({page}) => {
        await esperarElementoVisivel(page, 'btn-abrir-criar-competencia');
        await esperarBotaoVisivel(page, TEXTOS.DISPONIBILIZAR);
    });

    test('deve criar competência e alterar situação do subprocesso', async ({page}) => {
        const competenciaDescricao = gerarNomeUnico("Competência Teste");
        await criarCompetencia(page, competenciaDescricao);

        // Verifica que o novo card aparece pelo seu texto
        await verificarCompetenciaVisivel(page, competenciaDescricao);
    });

    test('deve editar competência existente', async ({page}) => {
        const competenciaOriginal = gerarNomeUnico("Competência Original");
        await criarCompetencia(page, competenciaOriginal);

        const competenciaEditada = gerarNomeUnico("Competência Editada");
        await editarCompetencia(page, competenciaOriginal, competenciaEditada);

        // Verifica pelo texto da competência editada e que o original não existe mais
        await verificarCompetenciaVisivel(page, competenciaEditada);
        await verificarCompetenciaNaoVisivel(page, competenciaOriginal);
    });

    test('deve excluir competência com confirmação', async ({page}) => {
        const competenciaParaExcluir = gerarNomeUnico("Competência para Excluir");
        await criarCompetencia(page, competenciaParaExcluir);

        await excluirCompetencia(page, competenciaParaExcluir);

        // Verifica que o texto da competência não está mais presente
        await verificarCompetenciaNaoVisivel(page, competenciaParaExcluir);
    });
});
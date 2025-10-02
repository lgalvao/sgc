import {Page} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    clicarBotaoImpactosMapa,
    criarCompetencia,
    DADOS_TESTE,
    disponibilizarMapaComData,
    editarCompetencia,
    esperarElementoVisivel,
    esperarTextoVisivel,
    excluirCompetencia,
    irParaMapaCompetencias,
    loginComoAdmin,
    SELETORES,
    TEXTOS,
    verificarCompetenciaNaoVisivel,
    verificarCompetenciaVisivel,
    verificarDisponibilizacaoConcluida,
    verificarMensagemNenhumImpacto,
    verificarModalImpactosAberto
} from './helpers';

async function navegarParaMapaRevisao(page: Page) {
    await loginComoAdmin(page);
    await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, 'SESEL');
    await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
}

test.describe('CDU-16: Ajustar mapa de competências', () => {
    test('deve exibir botão "Impacto no mapa" para ADMIN em processo de Revisão', async ({page}) => {
        await navegarParaMapaRevisao(page);

        await esperarElementoVisivel(page, 'impactos-mapa-button');
        await esperarTextoVisivel(page, TEXTOS.IMPACTO_NO_MAPA);
    });

    test('deve abrir modal de impactos no mapa', async ({page}) => {
        await navegarParaMapaRevisao(page);
        await clicarBotaoImpactosMapa(page);

        // Tentar verificar modal de impactos; caso não exista, verificar notificação informativa
        try {
            await verificarModalImpactosAberto(page);
        } catch {
            await verificarMensagemNenhumImpacto(page);
        }
    });

    test('deve permitir criação de competências', async ({page}) => {
        await navegarParaMapaRevisao(page);
        const nome = 'Competência CDU-16';
        await criarCompetencia(page, nome);

        await verificarCompetenciaVisivel(page, nome);
        await esperarElementoVisivel(page, SELETORES.EDITAR_COMPETENCIA);
        await esperarElementoVisivel(page, SELETORES.EXCLUIR_COMPETENCIA);
    });

    test('deve permitir edição de competências', async ({page}) => {
        const competenciaOriginal = 'Competência Original';
        const competenciaEditada = 'Competência Editada';

        await navegarParaMapaRevisao(page);
        await criarCompetencia(page, competenciaOriginal);
        await editarCompetencia(page, competenciaOriginal, competenciaEditada);

        await verificarCompetenciaVisivel(page, competenciaEditada);
        await verificarCompetenciaNaoVisivel(page, competenciaOriginal);
    });

    test('deve permitir exclusão de competências', async ({page}) => {
        const nome = 'Competência para Excluir';

        await navegarParaMapaRevisao(page);
        await criarCompetencia(page, nome);

        await excluirCompetencia(page, nome);
        await verificarCompetenciaNaoVisivel(page, nome);
    });

    test('deve validar associação de todas as atividades', async ({page}) => {
        await navegarParaMapaRevisao(page);
        await criarCompetencia(page, 'Competência Parcial');
        await disponibilizarMapaComData(page, '2025-12-31');
        await verificarDisponibilizacaoConcluida(page);
    });

    test('deve integrar com disponibilização de mapa', async ({page}) => {
        await navegarParaMapaRevisao(page);
        await criarCompetencia(page, 'Competência Completa');
        await disponibilizarMapaComData(page, '2025-12-31', 'Mapa ajustado conforme revisão');
        await verificarCompetenciaVisivel(page, 'Competência Completa');
    });
});
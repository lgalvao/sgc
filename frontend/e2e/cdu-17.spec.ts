import {Page} from '@playwright/test';
import {vueTest as test} from './support/vue-specific-setup';
import {
    abrirModalDisponibilizacao,
    criarCompetencia,
    esperarTextoVisivel,
    irParaMapaCompetencias,
    loginComoAdmin,
    preencherDataModal,
    preencherObservacoesModal,
    SELETORES,
    TEXTOS,
    verificarBotaoDisponibilizarHabilitado,
    verificarCampoObservacoesValor,
    verificarModalDisponibilizacaoVisivel,
    verificarModalFechado,
    criarProcessoCompleto,
    iniciarProcesso,
    gerarNomeUnico
} from './helpers';

let processo: any;
const siglaUnidade = 'SESEL';

async function navegarParaMapa(page: Page) {
    await loginComoAdmin(page);
    await irParaMapaCompetencias(page, processo.processo.codigo, siglaUnidade);
    await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
}

test.describe('CDU-17: Disponibilizar mapa de competências', () => {
    test.beforeEach(async ({ page }) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-17');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [10]); // Unidade 10 = SESEL
        await iniciarProcesso(page);
    });

    test('deve exibir modal com título e campos corretos', async ({page}) => {
        await navegarParaMapa(page);
        await criarCompetencia(page, 'Competência Teste');

        await abrirModalDisponibilizacao(page);
        await verificarModalDisponibilizacaoVisivel(page);
    });

    test('deve preencher observações no modal', async ({page}) => {
        await navegarParaMapa(page);
        await criarCompetencia(page, 'Competência Teste');

        await abrirModalDisponibilizacao(page);
        await preencherObservacoesModal(page, 'Observações de teste para CDU-17');
        await verificarCampoObservacoesValor(page, 'Observações de teste para CDU-17');
    });

    test('deve validar data obrigatória', async ({page}) => {
        await navegarParaMapa(page);
        await criarCompetencia(page, 'Competência Teste');

        await abrirModalDisponibilizacao(page);
        await verificarBotaoDisponibilizarHabilitado(page, false);

        await preencherDataModal(page, '2025-12-31');
        await verificarBotaoDisponibilizarHabilitado(page, true);
    });

    test('deve validar campos obrigatórios do modal', async ({page}) => {
        await navegarParaMapa(page);
        await criarCompetencia(page, 'Competência para Validação');

        await abrirModalDisponibilizacao(page);
        await verificarModalDisponibilizacaoVisivel(page);

        await verificarBotaoDisponibilizarHabilitado(page, false);
        await preencherDataModal(page, '2025-12-31');
        await verificarBotaoDisponibilizarHabilitado(page, true);

        await preencherObservacoesModal(page, 'Teste de observações');
        await verificarCampoObservacoesValor(page, 'Teste de observações');
    });

    test('deve processar disponibilização', async ({page}) => {
        await navegarParaMapa(page);
        await criarCompetencia(page, 'Competência para Disponibilizar');

        await abrirModalDisponibilizacao(page);
        await preencherDataModal(page, '2025-12-31');
        await preencherObservacoesModal(page, 'Observações de teste CDU-17');

        // confirmar disponibilização (usar test-id dentro do modal para evitar ambiguidade)
        await verificarBotaoDisponibilizarHabilitado(page, true);
        await page.getByTestId(SELETORES.BTN_DISPONIBILIZAR_PAGE).first().click();
    });

    test('deve cancelar disponibilização', async ({page}) => {
        await navegarParaMapa(page);
        await criarCompetencia(page, 'Competência Teste');

        await abrirModalDisponibilizacao(page);
        await page.getByRole('button', {name: TEXTOS.CANCELAR}).click();

        await verificarModalFechado(page);
        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });
});

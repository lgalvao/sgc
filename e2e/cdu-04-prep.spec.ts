import { vueTest as test } from './support/vue-specific-setup';
import {
    loginComoAdmin,
    limparProcessos,
    criarProcesso,
    iniciarProcesso,
    cancelarInicioProcesso,
    verificarRedirecionamentoParaPainel,
    verificarProcessoVisivelNoPainel,
    verificarPaginaEdicaoProcesso,
    verificarModalInicioProcessoVisivel,
    verificarModalInicioProcessoNaoVisivel,
    verificarBotoesEdicaoNaoVisiveis,
    clicarProcesso,
    verificarEfeitosBackendInicioProcesso,
} from './helpers';

test.describe('CDU-04: Iniciar processo (com preparação e verificação de backend)', () => {

    test.beforeEach(async ({ page }) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve iniciar processo e verificar efeitos no backend', async ({ page }) => {
        const descricao = `Processo Iniciar ${Date.now()}`;
        const unidadesParticipantes = ['SGP', 'STIC'];
        const processoId = await criarProcesso(page, 'MAPEAMENTO', descricao, unidadesParticipantes);

        await page.goto(`/processo/cadastro?idProcesso=${processoId}`);

        await iniciarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);

        await verificarEfeitosBackendInicioProcesso(page, processoId, descricao, unidadesParticipantes);

        await clicarProcesso(page, descricao);
        await verificarBotoesEdicaoNaoVisiveis(page);
    });

    test('deve cancelar iniciação e permanecer na tela com estado inalterado', async ({ page }) => {
        const descricao = `Processo Cancelar ${Date.now()}`;
        const processoId = await criarProcesso(page, 'MAPEAMENTO', descricao, ['STIC']);

        await page.goto(`/processo/cadastro?idProcesso=${processoId}`);

        await cancelarInicioProcesso(page);

        await verificarModalInicioProcessoNaoVisivel(page);
        await verificarPaginaEdicaoProcesso(page);
    });
});

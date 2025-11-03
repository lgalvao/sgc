import { vueTest as test } from './support/vue-specific-setup';
import {
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    preencherFormularioProcesso,
    salvarProcesso,
    iniciarProcesso,
    cancelarInicioProcesso,
    verificarRedirecionamentoParaPainel,
    verificarProcessoVisivelNoPainel,
    verificarPaginaEdicaoProcesso,
    verificarModalInicioProcessoVisivel,
    verificarModalInicioProcessoNaoVisivel,
    verificarBotoesEdicaoNaoVisiveis,
    clicarProcesso,
} from './helpers';

test.describe('CDU-04: Iniciar processo', () => {
    test.beforeAll(async ({ request }) => {
        await request.post('http://localhost:10000/api/e2e/reset');
    });

    test.beforeEach(async ({ page }) => {
        await loginComoAdmin(page);
    });

    test('deve abrir modal de confirmação e iniciar processo', async ({ page }) => {
        const descricao = `Processo Iniciar ${Date.now()}`;

        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricao, 'MAPEAMENTO', '2025-12-31', ['STIC']);
        await salvarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);

        await clicarProcesso(page, descricao);
        await verificarPaginaEdicaoProcesso(page);

        await iniciarProcesso(page);

        await verificarRedirecionamentoParaPainel(page);
        await verificarProcessoVisivelNoPainel(page, descricao);
    });

    test('deve cancelar iniciação e permanecer na tela', async ({ page }) => {
        const descricao = `Processo Cancelar ${Date.now()}`;

        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricao, 'MAPEAMENTO', '2025-12-31', ['SGP']);
        await salvarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);

        await clicarProcesso(page, descricao);
        await verificarPaginaEdicaoProcesso(page);

        await cancelarInicioProcesso(page);

        await verificarModalInicioProcessoNaoVisivel(page);
        await verificarPaginaEdicaoProcesso(page);
    });

    test('não deve permitir editar processo após iniciado', async ({ page }) => {
        const descricao = `Processo Bloqueio ${Date.now()}`;

        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricao, 'MAPEAMENTO', '2025-12-31', ['COEDE']);
        await salvarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);

        await clicarProcesso(page, descricao);
        await verificarPaginaEdicaoProcesso(page);
        await iniciarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);

        await clicarProcesso(page, descricao);

        await verificarBotoesEdicaoNaoVisiveis(page);
    });
});

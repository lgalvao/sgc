import { vueTest as test } from './support/vue-specific-setup';
import {
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    preencherFormularioProcesso,
    salvarProcesso,
    iniciarProcesso,
    cancelarInicioProcesso,
    abrirProcessoPorNome,
    verificarModalInicioProcessoVisivel,
    verificarModalInicioProcessoNaoVisivel,
    verificarPaginaEdicaoProcesso,
    verificarBotaoIniciarProcessoVisivel,
    verificarRedirecionamentoParaPainel,
    verificarProcessoVisivelNoPainel,
    verificarBotoesEdicaoNaoVisiveis,
    verificarSubprocessosCriados,
    verificarAlertasCriados,
} from './helpers';

test.describe('CDU-05: Iniciar processo de revisão', () => {
    test.beforeEach(async ({ page }) => {
        await loginComoAdmin(page);
    });

    test('deve exibir modal de confirmação ao clicar em Iniciar processo', async ({ page }) => {
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');
        await page.getByRole('button', { name: /Iniciar processo/i }).click();
        await verificarModalInicioProcessoVisivel(page);
    });

    test('deve cancelar iniciação do processo ao clicar em Cancelar no modal', async ({ page }) => {
        await abrirProcessoPorNome(page, 'Processo teste revisão CDU-05');
        await page.getByRole('button', { name: /Iniciar processo/i }).click();
        await cancelarInicioProcesso(page);
        await verificarModalInicioProcessoNaoVisivel(page);
        await verificarPaginaEdicaoProcesso(page);
        await verificarBotaoIniciarProcessoVisivel(page);
    });

    test('deve iniciar processo de revisão e mudar situação para EM_ANDAMENTO', async ({ page }) => {
        const descricaoProcesso = `Processo Revisão Teste ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricaoProcesso, 'REVISAO', '2025-12-31', ['ADMIN-UNIT']);
        await salvarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);

        await abrirProcessoPorNome(page, descricaoProcesso);
        await iniciarProcesso(page);
        
        await page.goto('http://localhost:5173/painel');
        await verificarProcessoVisivelNoPainel(page, descricaoProcesso);
        
        await abrirProcessoPorNome(page, descricaoProcesso);
        await verificarBotoesEdicaoNaoVisiveis(page);
    });

    test('deve criar subprocessos para unidades participantes ao iniciar processo', async ({ page }) => {
        const descricaoProcesso = `Processo Multi-Unidade ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricaoProcesso, 'REVISAO', '2025-12-31', ['ADMIN-UNIT']);
        await salvarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);
        
        await abrirProcessoPorNome(page, descricaoProcesso);
        const url = page.url();
        const match = url.match(/idProcesso=(\d+)/);
        const processoId = match ? match[1] : null;
        
        await iniciarProcesso(page);
        
        await verificarSubprocessosCriados(page, processoId);
    });

    test('deve criar alertas para unidades participantes ao iniciar processo', async ({ page }) => {
        const descricaoProcesso = `Processo Alertas ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricaoProcesso, 'REVISAO', '2025-12-31', ['ADMIN-UNIT']);
        await salvarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);
        
        await abrirProcessoPorNome(page, descricaoProcesso);
        await iniciarProcesso(page);
        
        await verificarAlertasCriados(page);
    });
});

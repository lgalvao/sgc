import {vueTest as test} from './support/vue-specific-setup';
import {
    cancelarIniciacaoProcesso,
    clicarBotaoIniciarProcesso,
    clicarProcessoNaTabela,
    confirmarIniciacaoProcesso,
    criarProcessoBasico,
    limparProcessosEmAndamento,
    loginComoAdmin,
    verificarBotaoIniciarProcessoVisivel,
    verificarCriacaoSubprocessos,
    verificarMapaVazioCriadoParaSubprocesso,
    verificarModalConfirmacaoIniciacaoProcesso,
    verificarMovimentacaoInicialSubprocesso,
    verificarModalFechado,
    verificarPaginaDetalheProcesso,
    verificarPaginaEdicaoProcesso,
    verificarProcessoBloqueadoParaEdicao,
    verificarProcessoIniciadoComSucesso,
    verificarValorCampoDescricao,
    TEXTOS
} from '~/helpers';

/**
 * CDU-04: Iniciar processo de mapeamento
 *
 * NOTA: Usa test.describe.serial() porque os testes criam e modificam estados de processos.
 * Limpa apenas processos em EM_ANDAMENTO antes de cada teste (não toca em dados estáticos).
 */
test.describe.serial('CDU-04: Iniciar processo', () => {
    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessosEmAndamento(page);
    });

    test('deve abrir modal de confirmação, iniciar processo, criar subprocessos, mapas e movimentações', async ({page}) => {
        const descricao = `Processo Iniciar ${Date.now()}`;

        // 1. Criar processo com STIC
        const processoId = await criarProcessoBasico(page, descricao, 'MAPEAMENTO', ['STIC']);

        // 2. Abrir processo e aguardar carregamento completo
        await clicarProcessoNaTabela(page, descricao);
        await verificarPaginaEdicaoProcesso(page);
        await verificarValorCampoDescricao(page, descricao);

        // 3. Clicar em Iniciar Processo → Abre modal
        await clicarBotaoIniciarProcesso(page);
        await verificarModalConfirmacaoIniciacaoProcesso(page);

        // 4. Confirmar → Processo iniciado
        const iniciarResponsePromise = page.waitForResponse(response =>
            response.url().includes(`/api/processos/${processoId}/iniciar`) &&
            response.request().method() === 'POST' &&
            response.status() === 200
        );
        await confirmarIniciacaoProcesso(page);
        await iniciarResponsePromise; // Wait for the API call to complete successfully

        // 5. Verificar que processo aparece no painel
        await verificarProcessoIniciadoComSucesso(page, TEXTOS.PROCESSO_INICIADO);

        // 6. Verificar que subprocessos foram criados
        const subprocessos = await verificarCriacaoSubprocessos(page, String(processoId));

        // 7. Verificar criações para cada subprocesso
        for (const subprocesso of subprocessos) {
            // 7.1 Mapa vazio
            await verificarMapaVazioCriadoParaSubprocesso(page, subprocesso.codigo);
            // 7.2 Movimentação inicial
            await verificarMovimentacaoInicialSubprocesso(page, subprocesso.codigo);
        }
    });

    test('deve cancelar iniciação e permanecer na tela', async ({page}) => {
        const descricao = `Processo Cancelar ${Date.now()}`;

        // 1. Criar processo com SEDESENV (unidade operacional)
        await criarProcessoBasico(page, descricao, 'MAPEAMENTO', ['SEDESENV']);

        // 2. Abrir e clicar em Iniciar
        await clicarProcessoNaTabela(page, descricao);
        await verificarPaginaEdicaoProcesso(page);
        await verificarValorCampoDescricao(page, descricao);

        await clicarBotaoIniciarProcesso(page);
        await verificarModalConfirmacaoIniciacaoProcesso(page);

        // 3. Cancelar → Modal fecha e permanece na tela
        await cancelarIniciacaoProcesso(page);
        await verificarModalFechado(page);
        await verificarValorCampoDescricao(page, descricao);
        await verificarBotaoIniciarProcessoVisivel(page);
    });

    test('não deve permitir editar processo após iniciado', async ({page}) => {
        const descricao = `Processo Bloqueio ${Date.now()}`;

        // 1. Criar e iniciar processo com SEDOC (unidade operacional)
        await criarProcessoBasico(page, descricao, 'MAPEAMENTO', ['SEDOC']);

        // Iniciar processo
        await clicarProcessoNaTabela(page, descricao);
        await verificarPaginaEdicaoProcesso(page);
        await clicarBotaoIniciarProcesso(page);
        const iniciarResponsePromise = page.waitForResponse(response =>
            response.url().includes(`/api/processos`) &&
            response.request().method() === 'POST' &&
            response.status() === 200
        );
        await confirmarIniciacaoProcesso(page);
        await iniciarResponsePromise;
        await verificarProcessoIniciadoComSucesso(page, TEXTOS.PROCESSO_INICIADO);

        // 2. Abrir processo iniciado → botões Editar/Remover/Iniciar não aparecem
        await clicarProcessoNaTabela(page, descricao);

        // Deve ir para tela Processo (não CadProcesso)
        await verificarPaginaDetalheProcesso(page);

        // Verificar que não está na tela de cadastro (sem botões de edição)
        await verificarProcessoBloqueadoParaEdicao(page);
    });
});
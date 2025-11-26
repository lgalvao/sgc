import { vueTest as test } from './support/vue-specific-setup';
import { expect } from '@playwright/test';
import {
    abrirProcessoPorNome,
    aguardarTabelaProcessosCarregada,
    cancelarIniciacaoProcesso,
    clicarBotaoIniciarProcesso,
    clicarBotaoSalvar,
    clicarProcessoNaTabela,
    confirmarNoModal,
    criarProcessoBasico,
    gerarNomeUnico,
    limparProcessosEmAndamento,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    navegarParaHome,
    preencherFormularioProcesso,
    selecionarUnidadesPorSigla,
    verificarAlertaNaTabela,
    verificarBotaoIniciarProcessoInvisivel,
    verificarBotaoIniciarProcessoVisivel,
    verificarBotaoRemoverInvisivel,
    verificarCamposProcessoDesabilitados,
    verificarCriacaoSubprocessos,
    verificarModalConfirmacaoIniciacaoProcesso,
    verificarMovimentacaoInicialSubprocesso,
    verificarModalFechado,
    verificarPaginaCadastroProcesso,
    verificarPaginaDetalheProcesso,
    verificarPaginaEdicaoProcesso,
    verificarSituacaoProcesso,
    verificarUrlDoPainel,
    loginComo,
    USUARIOS
} from '~/helpers';

/**
 * CDU-05: Iniciar processo de revisão
 *
 * Ator: ADMIN
 *
 * Objetivo: Validar a iniciação de um processo de revisão, incluindo:
 * - Abertura do modal de confirmação
 * - Criação de subprocessos para unidades participantes
 * - Envio de notificações e criação de alertas
 * - Mudança de situação do processo para EM_ANDAMENTO
 *
 * Cobertura:
 * ✅ Fluxo principal completo (passos 1-13)
 * ✅ Fluxo alternativo (cancelamento - passo 5)
 * ✅ Validações de estado e criação de dados
 */
test.describe('CDU-05: Iniciar processo de revisão', () => {
    let nomeProcessoRevisao: string;

    test.beforeEach(async ({ page }) => {
        // Loga como admin e limpa processos anteriores
        await loginComoAdmin(page);
        await limparProcessosEmAndamento(page);

        // Gera nome único e cria processo base
        nomeProcessoRevisao = `Processo teste revisão CDU-05 ${Date.now()}`;
        await criarProcessoBasico(page, nomeProcessoRevisao, 'REVISAO', ['SESEL']);
    });

    test('deve exibir modal de confirmação ao clicar em Iniciar processo', async ({ page }) => {
        // Abrir processo de revisão em situação CRIADO
        await clicarProcessoNaTabela(page, nomeProcessoRevisao);

        // Clicar em Iniciar processo
        await clicarBotaoIniciarProcesso(page);

        // Verificar modal de confirmação
        await verificarModalConfirmacaoIniciacaoProcesso(page);
    });

    test('deve cancelar iniciação do processo ao clicar em Cancelar no modal', async ({ page }) => {
        // Abrir processo e clicar em Iniciar
        await clicarProcessoNaTabela(page, nomeProcessoRevisao);
        await clicarBotaoIniciarProcesso(page);

        // Cancelar no modal
        await cancelarIniciacaoProcesso(page);

        // Verificar que modal fechou e processo não foi iniciado
        await verificarModalFechado(page);
        await verificarPaginaEdicaoProcesso(page);
        await verificarBotaoIniciarProcessoVisivel(page);
    });

    test('deve iniciar processo de revisão e mudar situação para EM_ANDAMENTO', async ({ page }) => {
        // Criar novo processo
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Revisão Teste ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SEDIA']);
        await clicarBotaoSalvar(page);
        await verificarUrlDoPainel(page);

        // Abrir e iniciar processo
        await clicarProcessoNaTabela(page, nomeProcesso);
        await clicarBotaoIniciarProcesso(page);
        await confirmarNoModal(page);

        // Verificar mudança de situação para EM_ANDAMENTO
        await navegarParaHome(page);
        await aguardarTabelaProcessosCarregada(page);
        await verificarSituacaoProcesso(page, nomeProcesso, /EM_ANDAMENTO/i);

        // Verificar que botão Iniciar não está mais disponível
        await clicarProcessoNaTabela(page, nomeProcesso);
        await verificarPaginaDetalheProcesso(page);
        await verificarBotaoIniciarProcessoInvisivel(page);
    });

    test('deve criar subprocessos para unidades participantes ao iniciar processo', async ({ page }) => {
        // Criar processo com unidade que tem subunidades
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Multi-Unidade ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SESEL']);
        await clicarBotaoSalvar(page);
        await verificarUrlDoPainel(page);

        // Abrir processo e capturar ID da URL
        await clicarProcessoNaTabela(page, nomeProcesso);
        const url = page.url();
        const match = url.match(/codProcesso=(\d+)/);
        const processoId = match ? match[1] : null;
        expect(processoId).toBeTruthy();

        // Interceptar e logar requisições de iniciar processo
        let iniciarProcessoResponse;
        page.route('**/api/processos/*/iniciar', async route => {
            const request = route.request();
            if (request.method() === 'POST') {
                iniciarProcessoResponse = await page.request.fetch(request);
                console.log('Intercepted Iniciar Processo Request:');
                console.log('  URL:', request.url());
                console.log('  Method:', request.method());
                console.log('  Headers:', request.headers());
                console.log('  PostData:', request.postDataJSON());
                console.log('Intercepted Iniciar Processo Response:');
                console.log('  Status:', iniciarProcessoResponse.status());
                console.log('  Body:', await iniciarProcessoResponse.json());
            }
            route.continue();
        });

        // Iniciar processo
        await clicarBotaoIniciarProcesso(page);
        await verificarModalConfirmacaoIniciacaoProcesso(page);
        await confirmarNoModal(page);

        // Verificar criação de subprocessos via API
        const subprocessos = await verificarCriacaoSubprocessos(page, processoId!);

        // Validar campos e movimentações dos subprocessos (Reqs 9 e 11)
        for (const sub of subprocessos) {
            // 9.2. Situação: 'Não iniciado'
            expect(sub.situacao).toBe('NAO_INICIADO');

            // 9.1. Data limite etapa 1: Copiada do processo (2025-12-31)
            expect(sub.dataLimiteEtapa1).toContain('2025-12-31');

            // 11. Movimentação inicial 'Processo iniciado'
            await verificarMovimentacaoInicialSubprocesso(page, sub.codigo);
        }
    });

    test('deve criar alertas para unidades participantes ao iniciar processo', async ({ page }) => {
        // Criar e iniciar processo
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Alertas ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SEDESENV']);
        await clicarBotaoSalvar(page);
        await verificarUrlDoPainel(page);

        // Abrir e iniciar
        await clicarProcessoNaTabela(page, nomeProcesso);
        await clicarBotaoIniciarProcesso(page);
        await confirmarNoModal(page);
        await verificarUrlDoPainel(page);

        // Logar como Chefe da SEDESENV (Chefe Teste) para ver o alerta
        await navegarParaHome(page);
        await loginComo(page, USUARIOS.CHEFE_TESTE);

        // Verificar criação de alertas
        await verificarAlertaNaTabela(page, /Início do processo/i);
    });

    test('deve preservar dados do processo após iniciação (somente leitura)', async ({ page }) => {
        // Criar processo
        await navegarParaCriacaoProcesso(page);
        const nomeProcesso = `Processo Somente Leitura ${gerarNomeUnico('CDU-05')}`;
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SEDESENV']);
        await clicarBotaoSalvar(page);
        await verificarUrlDoPainel(page);

        // Abrir e iniciar
        await clicarProcessoNaTabela(page, nomeProcesso);
        await clicarBotaoIniciarProcesso(page);
        await confirmarNoModal(page);
        await verificarUrlDoPainel(page);

        // Reabrir processo e verificar somente leitura
        await navegarParaHome(page);
        await aguardarTabelaProcessosCarregada(page);
        await clicarProcessoNaTabela(page, nomeProcesso);

        // Verificar que campos estão desabilitados
        await verificarCamposProcessoDesabilitados(page);

        // Botão Remover não deve estar visível
        await verificarBotaoRemoverInvisivel(page);

        // Botão Iniciar não deve estar visível (já foi iniciado)
        await verificarBotaoIniciarProcessoInvisivel(page);
    });

    test('deve validar que apenas processos CRIADO podem ser iniciados', async ({ page }) => {
        // 1. Criar e iniciar um processo para que ele fique EM_ANDAMENTO
        const nomeProcessoEmAndamento = `Processo JÁ EM ANDAMENTO ${gerarNomeUnico('CDU-05')}`;
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, nomeProcessoEmAndamento, 'REVISAO', '2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SEDESENV']);
        await clicarBotaoSalvar(page);
        await verificarUrlDoPainel(page);

        await clicarProcessoNaTabela(page, nomeProcessoEmAndamento);
        await clicarBotaoIniciarProcesso(page);
        await confirmarNoModal(page);
        await verificarUrlDoPainel(page);

        // 2. Abrir o processo que está EM_ANDAMENTO
        await clicarProcessoNaTabela(page, nomeProcessoEmAndamento);
        await verificarPaginaDetalheProcesso(page);

        // 3. Verificar que o botão "Iniciar processo" não está visível
        await verificarBotaoIniciarProcessoInvisivel(page);
    });

    test('deve exibir informações corretas no modal de confirmação', async ({ page }) => {
        // Abrir processo
        await clicarProcessoNaTabela(page, nomeProcessoRevisao);
        await verificarPaginaEdicaoProcesso(page);

        // Clicar e verificar modal
        await clicarBotaoIniciarProcesso(page);
        await verificarModalConfirmacaoIniciacaoProcesso(page);
    });
});

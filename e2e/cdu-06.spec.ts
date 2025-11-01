import {vueTest as test} from './support/vue-specific-setup';
import {
    aguardarProcessoNoPainel,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    loginComoAdmin,
    navegarParaProcessoNaTabela,
    verificarElementosDetalhesProcessoVisiveis,
    verificarNavegacaoPaginaSubprocesso,
    loginComoGestor,
    iniciarProcesso,
    SELETORES
} from './helpers';
import {expect} from '@playwright/test';

test.describe('CDU-06: Detalhar processo', () => {
    const nomeProcesso = 'Processo de Detalhes Teste';

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1, 2]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
    });

    test('deve mostrar detalhes do processo para ADMIN', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve mostrar detalhes do processo para GESTOR participante', async ({page}) => {
        await loginComoGestor(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve exibir o botão "Iniciar Processo" quando o processo está em estado CRIADO', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_FINALIZAR_PROCESSO)).not.toBeVisible();
    });

    test('deve exibir o botão "Finalizar Processo" quando o processo está EM ANDAMENTO', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).not.toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_FINALIZAR_PROCESSO)).toBeVisible();
    });

    test('deve permitir clicar em unidade', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarNavegacaoPaginaSubprocesso(page);
    });
});
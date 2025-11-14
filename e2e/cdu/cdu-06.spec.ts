import {vueTest as test} from '../support/vue-specific-setup';
import {
    aguardarProcessoNoPainel,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    iniciarProcesso,
    limparProcessosEmAndamento,
    loginComoAdmin,
    loginComoGestor,
    navegarParaProcessoNaTabela,
    SELETORES,
    verificarElementosDetalhesProcessoVisiveis,
    verificarNavegacaoPaginaSubprocesso
} from '~/helpers';
import {expect} from '@playwright/test';

test.describe.serial('CDU-06: Detalhar processo', () => {
    test('deve mostrar detalhes do processo para ADMIN', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - ADMIN';
        await limparProcessosEmAndamento(page);
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1, 2]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await page.waitForSelector(SELETORES.CAMPO_DESCRICAO);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        // Now it's in EM_ANDAMENTO and should show the detail view
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve mostrar detalhes do processo para GESTOR participante', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - GESTOR';
        await limparProcessosEmAndamento(page);
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1, 2]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await page.waitForSelector(SELETORES.CAMPO_DESCRICAO);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await loginComoGestor(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve exibir o botão "Iniciar Processo" quando o processo está em estado CRIADO', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - CRIADO';
        await limparProcessosEmAndamento(page);
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [3, 4]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        // Should be in CadProcesso (CRIADO state)
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_FINALIZAR_PROCESSO)).not.toBeVisible();
    });

    test('deve exibir o botão "Finalizar Processo" quando o processo está EM ANDAMENTO', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - EM ANDAMENTO';
        await limparProcessosEmAndamento(page);
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [5, 6]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await page.waitForSelector(SELETORES.CAMPO_DESCRICAO);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        // Now it's EM_ANDAMENTO
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).not.toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_FINALIZAR_PROCESSO)).toBeVisible();
    });

    test('deve permitir clicar em unidade', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - UNIDADE';
        await limparProcessosEmAndamento(page);
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [7, 8]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await page.waitForSelector(SELETORES.CAMPO_DESCRICAO);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await clicarUnidadeNaTabelaDetalhes(page, 'COEDE');
        await verificarNavegacaoPaginaSubprocesso(page);
    });
});
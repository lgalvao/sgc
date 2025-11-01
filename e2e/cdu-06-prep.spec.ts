import {vueTest as test} from './support/vue-specific-setup';
import {
    submeterProcesso,
    criarProcesso,
    limparProcessos,
    loginComoAdmin,
    abrirProcessoPorDescricao,
    clicarUnidadeNaTabelaDetalhes,
    verificarElementosDetalhesProcessoVisiveis,
    verificarNavegacaoPaginaSubprocesso,
    loginComoGestor,
    iniciarProcesso,
    SELETORES
} from './helpers';
import {expect} from '@playwright/test';

test.describe('CDU-06: Detalhar processo (com preparação)', () => {
    let processoId: number;
    const nomeProcesso = `Processo de Detalhes ${Date.now()}`;

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['SGP', 'STIC']);
    });

    test.afterEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve mostrar detalhes do processo para ADMIN', async ({page}) => {
        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, nomeProcesso);
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve mostrar detalhes do processo para GESTOR participante', async ({page}) => {
        await loginComoGestor(page);
        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, nomeProcesso);
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve exibir o botão "Iniciar Processo" quando o processo está em estado CRIADO', async ({page}) => {
        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, nomeProcesso);
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_FINALIZAR_PROCESSO)).not.toBeVisible();
    });

    test('deve exibir o botão "Finalizar Processo" quando o processo está EM ANDAMENTO', async ({page}) => {
        await submeterProcesso(page, processoId);
        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, nomeProcesso);
        await expect(page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO)).not.toBeVisible();
        await expect(page.getByTestId(SELETORES.BTN_FINALIZAR_PROCESSO)).toBeVisible();
    });

    test('deve permitir navegar para o subprocesso', async ({page}) => {
        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, nomeProcesso);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarNavegacaoPaginaSubprocesso(page);
    });
});

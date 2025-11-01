import {vueTest as test} from './support/vue-specific-setup';
import {
    submeterProcesso,
    criarProcesso,
    limparProcessos,
    loginComoAdmin,
    abrirProcessoPorDescricao,
    clicarUnidadeNaTabelaDetalhes,
    verificarElementosDetalhesProcessoVisiveis,
    verificarNavegacaoPaginaSubprocesso
} from './helpers';

test.describe('CDU-06: Detalhar processo (com preparação)', () => {

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test.afterEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve mostrar detalhes do processo e permitir navegar para o subprocesso', async ({page}) => {
        const nomeProcesso = `Processo de Detalhes ${Date.now()}`;
        const processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['SGP', 'STIC']);
        await submeterProcesso(page, processoId);

        await page.goto('/painel');
        await abrirProcessoPorDescricao(page, nomeProcesso);

        // Verifica se os elementos da página de detalhes estão visíveis
        await verificarElementosDetalhesProcessoVisiveis(page);

        // Clica na unidade 'STIC' na tabela de detalhes
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        // Verifica se a navegação para a página do subprocesso ocorreu corretamente
        await verificarNavegacaoPaginaSubprocesso(page);
    });
});

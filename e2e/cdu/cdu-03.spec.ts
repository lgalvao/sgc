import { vueTest as test } from '../support/vue-specific-setup';
import { PaginaPainel, PaginaProcesso } from '~/helpers';

test.describe('CDU-03: Manter processo', () => {
    let paginaPainel: PaginaPainel;
    let paginaProcesso: PaginaProcesso;

    test.beforeEach(async ({ page }) => {
        paginaPainel = new PaginaPainel(page);
        paginaProcesso = new PaginaProcesso(page);
        await paginaPainel.loginComoAdmin();
    });

    // ===== CRIAÇÃO DE PROCESSO =====

    test('deve criar processo e redirecionar para o Painel', async () => {
        const descricao = `Processo E2E ${Date.now()}`;

        await paginaPainel.irParaCriacaoDeProcesso();
        
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.selecionarUnidadesPorSigla(['STIC']);
        await paginaProcesso.clicarBotaoSalvar();

        await paginaPainel.aguardarProcessoNoPainel(descricao);
    });

    test('deve validar descrição obrigatória', async () => {
        await paginaPainel.irParaCriacaoDeProcesso();

        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.selecionarUnidadesPorSigla(['STIC']);
        await paginaProcesso.clicarBotaoSalvar();

        await paginaProcesso.verificarPaginaDeCadastro(); // Deve permanecer na página
    });

    test('deve validar ao menos uma unidade selecionada', async () => {
        await paginaPainel.irParaCriacaoDeProcesso();

        await paginaProcesso.preencherDescricao('Processo sem unidades');
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.clicarBotaoSalvar();

        await paginaProcesso.verificarPaginaDeCadastro(); // Deve permanecer na página
    });

    // ===== EDIÇÃO DE PROCESSO =====

    test('deve abrir processo para edição e modificar descrição', async () => {
        const descricaoOriginal = `Processo para Editar ${Date.now()}`;
        const novaDescricao = `Processo Editado ${Date.now()}`;

        // Criação
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.preencherDescricao(descricaoOriginal);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricaoOriginal);

        // Edição
        await paginaPainel.clicarProcessoNaTabela(descricaoOriginal);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.verificarValorDescricao(descricaoOriginal);
        
        await paginaProcesso.preencherDescricao(novaDescricao);
        await paginaProcesso.verificarValorDescricao(novaDescricao);
        
        await paginaProcesso.clicarBotaoSalvar();
        await paginaProcesso.verificarPermanenciaNaPaginaDeEdicao(novaDescricao);
    });

    test('deve exibir botão Remover apenas em modo de edição', async () => {
        // Na criação, o botão não deve existir
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.verificarBotaoRemoverVisivel(false);

        // Após criar e abrir para edição, o botão deve aparecer
        const descricao = `Processo com Botão Remover ${Date.now()}`;
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricao);
        
        await paginaPainel.clicarProcessoNaTabela(descricao);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.verificarBotaoRemoverVisivel(true);
    });

    // ===== REMOÇÃO DE PROCESSO =====

    test('deve cancelar remoção e permanecer na tela de edição', async () => {
        const descricao = `Processo para Cancelar Remoção ${Date.now()}`;
        
        // Criação
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricao);
        
        // Ação de cancelar
        await paginaPainel.clicarProcessoNaTabela(descricao);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.clicarBotaoRemover();
        await paginaProcesso.verificarDialogoConfirmacaoRemocao(descricao);
        await paginaProcesso.cancelarNoModal();

        // Verificação
        await paginaProcesso.verificarModalFechado();
        await paginaProcesso.verificarPaginaDeEdicao();
    });

    test('deve remover processo após confirmação', async () => {
        const descricao = `Processo para Remover ${Date.now()}`;

        // Criação
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricao);

        // Ação de remover
        await paginaPainel.clicarProcessoNaTabela(descricao);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.clicarBotaoRemover();
        await paginaProcesso.verificarDialogoConfirmacaoRemocao(descricao);
        await paginaProcesso.confirmarNoModal();

        // Verificação
        await paginaPainel.verificarUrlDoPainel();
        await paginaPainel.verificarProcessoNaoVisivel(descricao);
    });
});


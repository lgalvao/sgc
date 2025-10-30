import {vueTest as test} from './support/vue-specific-setup';
import {
    abrirDialogoRemocaoProcesso,
    aguardarProcessoNoPainel,
    cancelarNoModal,
    clicarPrimeiroProcessoTabela,
    criarProcessoCompleto,
    criarProcessoSemUnidades,
    editarDescricaoProcesso,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    navegarParaProcessoNaTabela,
    preencherFormularioProcesso,
    removerProcessoComConfirmacao,
    selecionarPrimeiraUnidade,
    tentarSalvarProcessoVazio,
    verificarCamposObrigatoriosFormulario,
    verificarComportamentoCheckboxInteroperacional,
    verificarComportamentoMarcacaoCheckbox,
    verificarDialogoConfirmacaoFechado,
    verificarDialogoConfirmacaoRemocao,
    verificarNavegacaoPaginaDetalhesProcesso,
    verificarNotificacaoErro,
    verificarPaginaEdicaoProcesso,
    verificarPermanenciaFormularioEdicao,
    verificarProcessoEditado,
    verificarProcessoRemovidoComSucesso,
    verificarSelecaoArvoreCheckboxes
} from './helpers';

test.describe('CDU-03: Manter processo', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve acessar tela de criação de processo', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await verificarCamposObrigatoriosFormulario(page);
    });

    test('deve mostrar erro para processo sem descrição', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await tentarSalvarProcessoVazio(page);
        await verificarNotificacaoErro(page);
    });

    test('deve mostrar erro para processo sem unidades', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await criarProcessoSemUnidades(page, 'Processo Teste', 'Mapeamento');
        await verificarNotificacaoErro(page);
    });

    test('deve permitir visualizar processo existente', async ({page}) => {
        await clicarPrimeiroProcessoTabela(page);
        await verificarNavegacaoPaginaDetalhesProcesso(page);
    });

    test('deve mostrar erro ao tentar criar processo de revisão/diagnóstico com unidade sem mapa vigente', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, 'Processo de Revisão - Unidade sem Mapa', 'Revisão');
        await selecionarPrimeiraUnidade(page);
        await tentarSalvarProcessoVazio(page);
        await verificarNotificacaoErro(page);
    });

    test('deve selecionar automaticamente unidades filhas ao clicar em unidade intermediária', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await selecionarPrimeiraUnidade(page);
        await verificarSelecaoArvoreCheckboxes(page);
    });

    test('deve selecionar nó raiz da subárvore se todas as unidades filhas forem selecionadas', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await selecionarPrimeiraUnidade(page);
        await verificarSelecaoArvoreCheckboxes(page);
    });

    test('deve colocar nó raiz em estado intermediário ao desmarcar uma unidade filha', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await selecionarPrimeiraUnidade(page);
        await verificarComportamentoMarcacaoCheckbox(page);
    });

    test('deve permitir marcar e desmarcar unidades independentemente', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await selecionarPrimeiraUnidade(page);
        await verificarComportamentoMarcacaoCheckbox(page);
    });

    test('deve permitir selecionar unidade interoperacional sem selecionar subordinadas', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await verificarComportamentoCheckboxInteroperacional(page);
    });

    test('deve criar processo com sucesso e redirecionar para o Painel', async ({page}) => {
        const descricaoProcesso = 'Novo Processo de Mapeamento Teste';
        await criarProcessoCompleto(page, descricaoProcesso, 'MAPEAMENTO', '2025-12-31', [1]);
        await aguardarProcessoNoPainel(page, descricaoProcesso);
    });

    test('deve editar processo com sucesso e refletir as alterações no Painel', async ({page}) => {
        // Pré-condição: Criar um processo para ser editado
        const descricaoOriginal = 'Processo para Edição';
        const descricaoEditada = 'Processo Editado com Sucesso';

        await criarProcessoCompleto(page, descricaoOriginal, 'MAPEAMENTO', '2025-12-31', [1]);
        await aguardarProcessoNoPainel(page, descricaoOriginal);

        // Clicar na linha do processo para edição
        await navegarParaProcessoNaTabela(page, descricaoOriginal);
        await verificarPaginaEdicaoProcesso(page);

        // Modificar a descrição e salvar
        await editarDescricaoProcesso(page, descricaoEditada);

        // Verificar se a descrição editada aparece na listagem e a original não
        await verificarProcessoEditado(page, descricaoOriginal, descricaoEditada);
    });

    test('deve remover processo com sucesso após confirmação', async ({ page }) => {
        // Pré-condição: Criar um processo para ser removido
        const descricaoProcessoRemover = 'Processo para Remoção';
        await criarProcessoCompleto(page, descricaoProcessoRemover, 'MAPEAMENTO', '2025-12-31', [1]);
        await aguardarProcessoNoPainel(page, descricaoProcessoRemover);

        // Clicar na linha do processo para edição/remoção
        await navegarParaProcessoNaTabela(page, descricaoProcessoRemover);
        await verificarPaginaEdicaoProcesso(page);

        // Remover o processo
        await removerProcessoComConfirmacao(page);

        // Verificar se o processo foi removido
        await verificarProcessoRemovidoComSucesso(page, descricaoProcessoRemover);
    });

    test('deve cancelar a remoção do processo', async ({page}) => {
        // Pré-condição: Criar um processo para tentar remover
        const descricaoProcessoCancelarRemocao = 'Processo para Cancelar Remoção';
        await criarProcessoCompleto(page, descricaoProcessoCancelarRemocao, 'MAPEAMENTO', '2025-12-31', [1]);
        await aguardarProcessoNoPainel(page, descricaoProcessoCancelarRemocao);

        // Clicar na linha do processo para edição/remoção
        await navegarParaProcessoNaTabela(page, descricaoProcessoCancelarRemocao);
        await verificarPaginaEdicaoProcesso(page);

        // Abrir diálogo e cancelar remoção
        await abrirDialogoRemocaoProcesso(page);
        await verificarDialogoConfirmacaoRemocao(page, descricaoProcessoCancelarRemocao);
        await cancelarNoModal(page);

        // Verificar que o diálogo foi fechado e permanece na tela de edição
        await verificarDialogoConfirmacaoFechado(page, descricaoProcessoCancelarRemocao);
        await verificarPermanenciaFormularioEdicao(page, descricaoProcessoCancelarRemocao);
    });

    test('deve permitir preencher a data limite da etapa 1', async ({page}) => {
        const descricaoProcessoData = 'Processo com Data Limite';
        await navegarParaCriacaoProcesso(page);
        await criarProcessoCompleto(page, descricaoProcessoData, 'MAPEAMENTO', '2025-12-31', [1]);

        // Verificar se o processo aparece na listagem
        await aguardarProcessoNoPainel(page, descricaoProcessoData);
    });
});
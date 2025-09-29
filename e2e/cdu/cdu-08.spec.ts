import {test} from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    cancelarEdicaoAtividade,
    clicarBotaoImportarAtividades,
    DADOS_TESTE,
    editarAtividade,
    editarConhecimento,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaCadastroAtividades,
    removerAtividade,
    removerConhecimento,
    SELETORES_CSS,
    tentarAdicionarAtividadeVazia,
    verificarAtividadeNaoVisivel,
    verificarAtividadeVisivel,
    verificarBotaoDisponibilizarVisivel,
    verificarBotaoImpactoVisivel,
    verificarConhecimentoNaoVisivel,
    verificarConhecimentoVisivel,
    verificarContadorAtividades,
    verificarModalImportacaoVisivel,
    verificarPaginaCadastroAtividades,
} from './helpers';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    test.beforeEach(async ({page}) => await loginComoChefe(page));

    const PROCESSO_MAPEAMENTO = DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC;
    const PROCESSO_REVISAO = DADOS_TESTE.PROCESSOS.REVISAO_STIC;
    const UNIDADE_STIC = DADOS_TESTE.UNIDADES.STIC;

    test('deve navegar e exibir a página de cadastro de atividades', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_MAPEAMENTO.id, UNIDADE_STIC);
        await verificarPaginaCadastroAtividades(page);
    });

    test('deve exibir botão Impacto no mapa para processos de revisão', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_REVISAO.id, UNIDADE_STIC);
        await verificarBotaoImpactoVisivel(page);
    });

    test('deve adicionar uma atividade e um conhecimento', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_MAPEAMENTO.id, UNIDADE_STIC);

        const nomeAtividade = gerarNomeUnico('Atividade Teste');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        const nomeConhecimento = gerarNomeUnico('Conhecimento Teste');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);
    });

    test('deve editar e remover uma atividade', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_MAPEAMENTO.id, UNIDADE_STIC);

        const nomeOriginal = gerarNomeUnico('Atividade para Editar');
        await adicionarAtividade(page, nomeOriginal);
        await verificarAtividadeVisivel(page, nomeOriginal);

        const nomeEditado = gerarNomeUnico('Atividade Editada');
        await editarAtividade(page, nomeOriginal, nomeEditado);
        await verificarAtividadeVisivel(page, nomeEditado);

        await removerAtividade(page, nomeEditado);
        await verificarAtividadeNaoVisivel(page, nomeEditado);
    });

    test('deve editar e remover um conhecimento', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_REVISAO.id, UNIDADE_STIC);

        const nomeAtividade = gerarNomeUnico('Atividade para Conhecimento');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        const nomeOriginal = gerarNomeUnico('Conhecimento Original');
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, nomeOriginal);
        await verificarConhecimentoVisivel(cardAtividade, nomeOriginal);

        const nomeEditado = gerarNomeUnico('Conhecimento Editado');
        await editarConhecimento(page, nomeAtividade, nomeOriginal, nomeEditado);
        await verificarConhecimentoVisivel(page, nomeAtividade, nomeEditado);

        await removerConhecimento(page, nomeAtividade, nomeEditado);
        await verificarConhecimentoNaoVisivel(page, nomeAtividade, nomeEditado);
    });

    test('deve abrir modal para importar atividades', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_MAPEAMENTO.id, UNIDADE_STIC);
        await clicarBotaoImportarAtividades(page);
        await verificarModalImportacaoVisivel(page);
    });

    test('deve exibir botão de disponibilizar após adicionar item', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_MAPEAMENTO.id, UNIDADE_STIC);

        const nomeAtividade = gerarNomeUnico('Atividade para Disponibilizar');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, 'Conhecimento qualquer');
        await verificarConhecimentoVisivel(cardAtividade, 'Conhecimento qualquer');

        await verificarBotaoDisponibilizarVisivel(page);
    });

    test('não deve adicionar atividade com campos vazios', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_MAPEAMENTO.id, UNIDADE_STIC);

        const contadorAntes = await page.locator(SELETORES_CSS.CARD_ATIVIDADE).count();
        await tentarAdicionarAtividadeVazia(page);
        await verificarContadorAtividades(page, contadorAntes);
    });

    test('deve cancelar a edição de uma atividade', async ({page}) => {
        await navegarParaCadastroAtividades(page, PROCESSO_MAPEAMENTO.id, UNIDADE_STIC);

        const nomeOriginal = gerarNomeUnico('Atividade para Cancelar Edição');
        await adicionarAtividade(page, nomeOriginal);
        await verificarAtividadeVisivel(page, nomeOriginal);

        await cancelarEdicaoAtividade(page, nomeOriginal, 'Texto que será descartado');

        await verificarAtividadeVisivel(page, nomeOriginal);
        await verificarAtividadeNaoVisivel(page, 'Texto que será descartado');
    });
});
/**
 * ÍNDICE DE AÇÕES
 * Re-exporta todas as ações organizadas por domínio
 */

// Ações de processo
export {
    preencherFormularioProcesso,
    selecionarPrimeiraUnidade,
    clicarPrimeiroProcessoTabela,
    criarProcessoCompleto,
    tentarSalvarProcessoVazio,
    criarProcessoSemUnidades,
    navegarParaProcessoNaTabela,
    editarDescricaoProcesso,
    criarProcessoMapeamentoCompleto,
    finalizarProcesso,
    disponibilizarCadastro,
    aceitarCadastro,
    homologarCadastro,
    devolverParaAjustes,
    iniciarProcesso,
    removerProcessoComConfirmacao,
    cancelarRemocaoProcesso,
    confirmarInicializacaoProcesso,
    removerProcessoConfirmandoNoModal,
    clicarUnidadeNaTabelaDetalhes,
    clicarBotaoIniciarProcesso,
    clicarProcessoNaTabela
} from './acoes-processo';

// Ações de modais
export {
    cancelarNoModal,
    confirmarNoModal,
    confirmarRemocaoNoModal,
    abrirDialogoRemocaoProcesso,
    clicarIniciarProcesso,
    abrirModalInicializacaoProcesso,
    confirmarInicializacaoNoModal,
    cancelarModal,
    clicarBotaoHistoricoAnalise
} from './acoes-modais';

// Ações de atividades
export {
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    removerAtividade,
    editarConhecimento,
    removerConhecimento,
    criarCompetencia,
    clicarBotaoImportarAtividades,
    clicarBotaoDisponibilizar,
    tentarAdicionarAtividadeVazia,
    cancelarEdicaoAtividade,
    adicionarConhecimentoPrimeiraAtividade
} from './acoes-atividades';

// Ações de mapa
export {
    validarMapa,
    clicarBotaoImpactosMapa,
    fecharModalImpactos
} from './acoes-mapa';
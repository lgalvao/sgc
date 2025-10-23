/**
 * ÍNDICE DE AÇÕES
 * Re-exporta todas as ações organizadas por domínio
 */

// Ações de processo
export {
    preencherFormularioProcesso,
    selecionarPrimeiraUnidade,
    clicarPrimeiroProcessoTabela,
    selecionarPrimeiroProcessoPorSituacao,
    criarProcessoCompleto,
    tentarSalvarProcessoVazio,
    criarProcessoSemUnidades,
    navegarParaProcessoNaTabela,
    editarDescricaoProcesso,
    criarProcessoMapeamentoCompleto,
    clicarBotaoFinalizarProcesso,
    abrirModalFinalizacaoProcesso,
    confirmarFinalizacaoNoModal,
    finalizarProcesso,
    disponibilizarCadastro,
    aceitarCadastro,
    registrarAceiteRevisao,
    homologarCadastro,
    devolverParaAjustes,
    devolverCadastro,
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
    editarCompetencia,
    excluirCompetencia,
    clicarBotaoImportarAtividades,
    clicarBotaoDisponibilizar,
    abrirModalDisponibilizacao,
    preencherDataModal,
    preencherObservacoesModal,
    disponibilizarMapaComData,
    tentarAdicionarAtividadeVazia,
    cancelarEdicaoAtividade,
    adicionarConhecimentoPrimeiraAtividade
} from './acoes-atividades';

 // Ações de mapa
 export {
     fecharModalImpactos
 } from './acoes-mapa';
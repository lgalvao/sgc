/**
 * ÍNDICE DE VERIFICAÇÕES
 * Re-exporta todas as verificações organizadas por domínio
 */

// Verificações básicas
export {
    esperarMensagemSucesso as verificarMensagemSucesso,
    esperarMensagemErro as verificarMensagemErro,
    esperarTextoVisivel,
    esperarElementoVisivel,
    esperarElementoInvisivel,
    verificarUrl,
    esperarUrl as verificarUrlPainel,
    verificarModalVisivel,
    verificarModalFechado
} from './verificacoes-basicas';

// Verificações de processo
export {
    verificarPaginaEdicaoProcesso,
    verificarPaginaCadastroProcesso,
    verificarCamposObrigatoriosFormulario,
    verificarNotificacaoErro,
    aguardarProcessoNoPainel,
    verificarProcessoEditado,
    verificarDialogoConfirmacaoRemocao,
    verificarProcessoRemovidoComSucesso,
    verificarDialogoConfirmacaoFechado,
    verificarProcessoIniciadoComSucesso,
    verificarPermanenciaFormularioEdicao,
    verificarConfirmacaoInicializacao,
    verificarModalConfirmacaoInicializacao,
    verificarProcessoInicializadoComSucesso,
    verificarValorCampoDescricao,
    verificarBotaoIniciarProcessoVisivel,
    verificarModalConfirmacaoIniciarProcessoVisivel,
    verificarModalConfirmacaoIniciarProcessoInvisivel
} from './verificacoes-processo';

// Verificações de UI
export {
    verificarElementosPainel,
    verificarAusenciaBotaoCriarProcesso,
    verificarVisibilidadeProcesso,
    verificarSelecaoArvoreCheckboxes,
    verificarComportamentoMarcacaoCheckbox,
    verificarComportamentoCheckboxInteroperacional,
    verificarTituloProcessos,
    verificarElementosDetalhesProcessoVisiveis
} from './verificacoes-ui';
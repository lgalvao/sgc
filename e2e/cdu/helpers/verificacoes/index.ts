/**
 * ÍNDICE DE VERIFICAÇÕES
 * Re-exporta todas as verificações organizadas por domínio
 */

// Verificações básicas
export {
    esperarMensagemSucesso,
    esperarMensagemErro,
    esperarTextoVisivel,
    esperarElementoVisivel,
    esperarElementoInvisivel,
    verificarUrl,
    esperarUrl,
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
    verificarProcessoInicializadoComSucesso
} from './verificacoes-processo';

// Verificações de UI
export {
    verificarElementosPainel,
    verificarAusenciaBotaoCriarProcesso,
    verificarVisibilidadeProcesso,
    verificarSelecaoArvoreCheckboxes,
    verificarComportamentoMarcacaoCheckbox,
    verificarComportamentoCheckboxInteroperacional
} from './verificacoes-ui';
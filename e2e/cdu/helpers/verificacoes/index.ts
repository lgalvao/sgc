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
    esperarBotaoVisivel,
    verificarUrl,
    verificarUrlDoPainel, // Corrigido: removido alias e adicionada função específica
    verificarModalVisivel,
    verificarModalFechado,
    esperarNotificacaoLoginInvalido
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
    verificarAceiteRegistradoComSucesso,
    verificarCadastroDevolvidoComSucesso,
    verificarProcessoRemovidoComSucesso,
    verificarModalFinalizacaoProcesso,
    verificarModalFinalizacaoFechado,
    verificarFinalizacaoBloqueada,
    verificarProcessoFinalizadoNoPainel,
    verificarMensagemFinalizacaoSucesso,
    verificarMapasVigentesNotificacao,
    verificarEmailFinalizacaoEnviado,
    verificarBotaoFinalizarProcessoVisivel,
    verificarBotaoFinalizarProcessoInvisivel,
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
    verificarPaginaLogin,
    verificarPainelBasico,
    verificarPainelSemCriacao,
    verificarPainelComCriacao,
    verificarPainelComAlertas,
    verificarAusenciaBotaoCriarProcesso,
    verificarVisibilidadeProcesso,
    verificarSelecaoArvoreCheckboxes,
    verificarComportamentoMarcacaoCheckbox,
    verificarComportamentoCheckboxInteroperacional,
    verificarTituloProcessos,
    verificarElementosDetalhesProcessoVisiveis,
    verificarCamposLogin,
    verificarEstruturaAdmin,
    verificarEstruturaServidor,
    verificarPaginaCadastroAtividades,
    verificarBotaoImpactoVisivel,
    verificarModalImportacaoVisivel,
    verificarBotaoDisponibilizarVisivel,
    verificarAtividadeVisivel,
    verificarAtividadeNaoVisivel,
    verificarConhecimentoVisivel,
    verificarConhecimentoNaoVisivel,
    verificarContadorAtividades,
    verificarColunasTabelaAlertas,
    verificarAlertasOrdenadosPorDataHora,
    verificarBotaoHistoricoAnaliseVisivel,
    verificarModalHistoricoAnaliseAberto,
    verificarAcoesAnaliseGestor,
    verificarAcaoHomologarVisivel,
    verificarBotaoDisponibilizarHabilitado,
    verificarMensagemNenhumImpacto,
    verificarModalImpactosAberto,
    verificarModalImpactosFechado,
    verificarListagemAtividadesEConhecimentos,
    verificarModoSomenteLeitura,
    verificarCabecalhoUnidade
} from './verificacoes-ui';

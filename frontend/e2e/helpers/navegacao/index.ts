/**
 * ÍNDICE DE NAVEGAÇÃO E LOGIN
 * Re-exporta todas as funções de navegação e login
 */
export {
    navegarParaCriacaoProcesso,
    navegarParaDetalhesProcesso,
    navegarParaCadastroAtividades,
    navegarParaVisualizacaoAtividades,
    acessarAnaliseRevisaoCadastro,
    acessarAnaliseRevisaoComoGestor,
    acessarAnaliseRevisaoComoAdmin,
    irParaMapaCompetencias,
    navegarParaEdicaoMapa,
    irParaVisualizacaoMapa,
    irParaSubprocesso,
    irParaProcessoPorTexto,
    navegarParaProcessoPorId,
    verificarNavegacaoPaginaSubprocesso,
    verificarNavegacaoPaginaCadastroProcesso,
    verificarNavegacaoPaginaDetalhesProcesso,
    clicarPrimeiroProcesso,
    clicarProcesso,
    ordenarTabelaProcessosPorColuna,
    expandirTodasAsUnidades,
    clicarUnidade,
    navegarParaHome,
    clicarBotaoEntrar,
    clicarBotaoSair
} from './navegacao';

export * from '../auth';

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
    homologarItem,
    devolverParaAjustes,
    iniciarProcesso,
    removerProcessoComConfirmacao,
    cancelarRemocaoProcesso,
    confirmarInicializacaoProcesso,
    removerProcessoConfirmandoNoModal
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
    cancelarModal
} from './acoes-modais';

// Ações de atividades
export {
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    removerAtividade,
    editarConhecimento,
    removerConhecimento,
    criarCompetencia
} from './acoes-atividades';

// Ações de mapa
export {
    validarMapa
} from './acoes-mapa';
import {TEXTOS_COMUM} from "./textos-comum.js";
import {TEXTOS_ALERTA_ATIVIDADES, TEXTOS_ATIVIDADES, TEXTOS_SUCESSO_ATIVIDADES} from "./textos-atividades.js";
import {TEXTOS_ADMINISTRACAO, TEXTOS_ATRIBUICAO_TEMPORARIA, TEXTOS_CONFIGURACOES} from "./textos-administracao.js";
import {TEXTOS_ERRO_GERAL} from "./textos-erro-geral.js";
import {TEXTOS_LOGIN} from "./textos-login.js";
import {TEXTOS_MAPA, TEXTOS_MAPA_VISUALIZACAO, TEXTOS_SUCESSO_MAPA} from "./textos-mapa.js";
import {TEXTOS_PAINEL} from "./textos-painel.js";
import {
    TEXTOS_ACAO_BLOCO,
    TEXTOS_HISTORICO,
    TEXTOS_PROCESSO,
    TEXTOS_SUCESSO_PROCESSO,
    TEXTOS_TABELA_PROCESSOS
} from "./textos-processo.js";
import {TEXTOS_MOVIMENTACAO, TEXTOS_SUBPROCESSO, TEXTOS_SUCESSO_SUBPROCESSO} from "./textos-subprocesso.js";
import {TEXTOS_TREE_TABLE, TEXTOS_UNIDADE, TEXTOS_UNIDADES} from "./textos-unidades.js";

/**
 * Agregador temporário de textos estáticos do sistema.
 *
 * A migração para arquivos por domínio será feita em cortes pequenos.
 */
export const TEXTOS = {
    comum: TEXTOS_COMUM,
    login: TEXTOS_LOGIN,
    erroGeral: TEXTOS_ERRO_GERAL,
    painel: TEXTOS_PAINEL,
    mapa: TEXTOS_MAPA,
    atribuicaoTemporaria: TEXTOS_ATRIBUICAO_TEMPORARIA,
    unidades: TEXTOS_UNIDADES,
    subprocesso: TEXTOS_SUBPROCESSO,
    tabelaProcessos: TEXTOS_TABELA_PROCESSOS,
    historico: TEXTOS_HISTORICO,
    treeTable: TEXTOS_TREE_TABLE,
    mapaVisualizacao: TEXTOS_MAPA_VISUALIZACAO,
    administracao: TEXTOS_ADMINISTRACAO,
    atividades: TEXTOS_ATIVIDADES,
    unidade: TEXTOS_UNIDADE,
    // Compatibilidade temporaria para testes e2e ainda nao migrados.
    sucesso: {
        ...TEXTOS_SUCESSO_SUBPROCESSO,
        ...TEXTOS_SUCESSO_ATIVIDADES,
        ...TEXTOS_SUCESSO_MAPA,
        ...TEXTOS_SUCESSO_PROCESSO,
    },
    alerta: TEXTOS_ALERTA_ATIVIDADES,
    movimentacao: TEXTOS_MOVIMENTACAO,
    processo: TEXTOS_PROCESSO,
    acaoBloco: TEXTOS_ACAO_BLOCO,
    configuracoes: TEXTOS_CONFIGURACOES,
} as const;

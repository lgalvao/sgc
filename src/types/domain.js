/**
 * @typedef {string} ISODate - Data no formato AAAA-MM-DD.
 */

/**
 * @typedef {string} ISODateTime - Data e hora no formato AAAA-MM-DDTHH:mm:ss.
 */

/**
 * Enum para os tipos de processo.
 * @readonly
 * @enum {string}
 */
export const ProcessoTipo = {
    MAPEAMENTO: 'Mapeamento',
    REVISAO: 'Revisão',
    DIAGNOSTICO: 'Diagnóstico',
};

/**
 * @typedef {object} Processo
 * @property {number} id - O identificador único do processo.
 * @property {string} descricao - A descrição do processo.
 * @property {ProcessoTipo} tipo - O tipo do processo.
 * @property {number[]} processosUnidade - Os IDs dos processos de unidade envolvidos no processo.
 * @property {ISODate} dataLimite - A data limite para o processo (formato AAAA-MM-DD).
 * @property {string} situacao - A situação atual do processo (ex: Em andamento, Criado, Finalizado).
 * @property {ISODate|null } dataFinalizacao - A data de finalização do processo (formato AAAA-MM-DD). Opcional.
 */

/**
 * @typedef {object} ProcessoUnidade
 * @property {number} id - O identificador único da relação processo-unidade.
 * @property {ISODate} dataLimite - A data limite para esta unidade no processo.
 * @property {string} unidadeId - A sigla da unidade.
 * @property {number} processoId - O ID do processo.
 * @property {string} situacao - A situação atual da unidade no processo.
 * @property {string|null} unidadeAtual - O ID da unidade atual onde o processo se encontra (opcional).
 * @property {string|null} unidadeAnterior - O ID da unidade anterior onde o processo se encontrava (opcional).
 */

/**
 * @typedef {object} Unidade
 * @property {string} sigla - A sigla da unidade.
 * @property {string} tipo - O tipo da unidade (ex: INTERMEDIARIA, OPERACIONAL).
 * @property {string} nome - O nome completo da unidade.
 * @property {number} titular - O ID do servidor titular da unidade.
 * @property {number|null} responsavel - O ID do servidor responsável pela unidade (opcional).
 * @property {Unidade[]} filhas - A lista de unidades filhas.
 */

/**
 * @typedef {object} Servidor
 * @property {number} id - O identificador único do servidor.
 * @property {string} nome - O nome do servidor.
 * @property {string} unidade - A sigla da unidade do servidor.
 * @property {string|null} email - O email do servidor.
 * @property {string|null} ramal - O ramal do servidor.
 */

/**
 * @typedef {object} Mapa
 * @property {number} id - O identificador único do mapa.
 * @property {string} unidade - A sigla da unidade do mapa.  // TODO Deveria estar ligado ao processoUnidade
 * @property {string} situacao - A situação do mapa (ex: em_andamento). // TODO Deveria ser dedutivel da situacao do processo
 * @property {Competencia[]} competencias - A lista de competências do mapa.
 * @property {ISODate} dataCriacao - A data de criação do mapa (formato AAAA-MM-DD).
 * @property {ISODate|null} dataDisponibilizacao - A data de disponibilização do mapa (formato AAAA-MM-DD).
 * @property {ISODate|null} dataFinalizacao - A data de finalização do mapa (formato AAAA-MM-DD).
 */

/**
 * @typedef {object} Competencia
 * @property {number} id - O identificador único da competência.
 * @property {string} descricao - A descrição da competência.
 * @property {number[]} atividadesAssociadas - A lista de IDs de atividades associadas.
 */

/**
 * @typedef {object} Conhecimento
 * @property {number} id - O identificador único do conhecimento.
 * @property {string} descricao - A descrição do conhecimento.
 */

/**
 * @typedef {object} Atividade
 * @property {number} id - O identificador único da atividade.
 * @property {string} descricao - A descrição da atividade.
 * @property {number} processoUnidadeId - O identificador único do processo de unidade.
 * @property {Conhecimento[]} conhecimentos - A lista de conhecimentos associados à atividade.
 */

/**
 * @typedef {object} Alerta
 * @property {number} id - O identificador único do alerta.
 * @property {string} unidadeOrigem - A sigla da unidade de origem
 * @property {string} unidadeDestino - A sigla da unidade de destino
 * @property {ISODateTime} dataHora - A data e hora do alerta (formato AAAA-MM-DDTHH:MM:SS).
 * @property {number} processoId - O ID do processo relacionado ao alerta.
 * @property {string} descricao - A descrição do alerta.
 */
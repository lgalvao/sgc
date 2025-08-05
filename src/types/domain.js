/**
 * @typedef {object} ProcessoUnidade
 * @property {number} id - O identificador único da relação processo-unidade.
 * @property {string} dataLimite - A data limite para esta unidade no processo.
 * @property {string} unidadeId - A sigla da unidade.
 * @property {number} processoId - O ID do processo.
 * @property {string} [unidadeAtual] - O ID da unidade atual onde o processo se encontra (opcional).
 * @property {string} [unidadeAnterior] - O ID da unidade anterior onde o processo se encontrava (opcional).
 */

/**
 * @typedef {object} Processo
 * @property {number} id - O identificador único do processo.
 * @property {string} descricao - A descrição do processo.
 * @property {string} tipo - O tipo do processo (ex: Mapeamento, Revisão, Diagnóstico).
 * @property {number[]} processosUnidade - Os IDs dos processos de unidade envolvidos no processo.
 * @property {string} dataLimite - A data limite para o processo (formato DD/MM/AAAA).
 * @property {string} situacao - A situação atual do processo (ex: Em andamento, Criado, Finalizado).
 * @property {string} [dataFinalizacao] - A data de finalização do processo (formato DD/MM/AAAA). Opcional.
 */

/**
 * @typedef {object} Unidade
 * @property {string} sigla - A sigla da unidade.
 * @property {string} nome - O nome completo da unidade.
 * @property {number} titular - O ID do servidor titular da unidade.
 * @property {number} [responsavel] - O ID do servidor responsável pela unidade (opcional).
 * @property {string} [tipo] - O tipo da unidade (ex: INTERMEDIARIA, OPERACIONAL).
 * @property {Unidade[]} filhas - A lista de unidades filhas.
 */

/**
 * @typedef {object} Servidor
 * @property {number} id - O identificador único do servidor.
 * @property {string} nome - O nome do servidor.
 * @property {string} unidade - A sigla da unidade do servidor.
 * @property {string} email - O email do servidor.
 * @property {string} ramal - O ramal do servidor.
 */

/**
 * @typedef {object} Competencia
 * @property {number} id - O identificador único da competência.
 * @property {string} descricao - A descrição da competência.
 * @property {number[]} atividadesAssociadas - A lista de IDs de atividades associadas.
 */

/**
 * @typedef {object} Mapa
 * @property {number} id - O identificador único do mapa.
 * @property {string} unidade - A sigla da unidade do mapa.
 * @property {string} situacao - A situação do mapa (ex: em_andamento).
 * @property {Competencia[]} competencias - A lista de competências do mapa.
 * @property {string} dataCriacao - A data de criação do mapa (formato AAAA-MM-DD).
 * @property {string|null} dataDisponibilizacao - A data de disponibilização do mapa (formato AAAA-MM-DD).
 * @property {string|null} dataFinalizacao - A data de finalização do mapa (formato AAAA-MM-DD).
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
 * @property {string} processoUnidadeId - O identificador único do processo de unidade.
 * @property {Conhecimento[]} conhecimentos - A lista de conhecimentos associados à atividade.
 */

/**
 * @typedef {object} Alerta
 * @property {number} id - O identificador único do alerta.
 * @property {string} dataHora - A data e hora do alerta (formato AAAA-MM-DDTHH:MM:SS).
 * @property {number} processoUnidadeId - O ID do processo de unidade relacionado ao alerta.
 * @property {string} descricao - A descrição do alerta.
 */

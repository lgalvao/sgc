/**
 * Constantes para situações de processos e mapas no SGC
 */

// Situações de subprocesso
export const SITUACOES_SUBPROCESSO = {
    AGUARDANDO: "Aguardando",
    EM_ANDAMENTO: "Em andamento",
    AGUARDANDO_VALIDACAO: "Aguardando validação",
    FINALIZADO: "Finalizado",
    VALIDADO: "Validado",
    DEVOLVIDO: "Devolvido",
    CADASTRO_EM_ANDAMENTO: "Cadastro em andamento",
    CADASTRO_DISPONIBILIZADO: "Cadastro disponibilizado",
    REVISAO_CADASTRO_DISPONIBILIZADA: "Revisão do cadastro disponibilizada",
    REVISAO_CADASTRO_EM_ANDAMENTO: "Revisão do cadastro em andamento",
    MAPA_DISPONIBILIZADO: "Mapa disponibilizado",
    MAPA_EM_ANDAMENTO: "Mapa em andamento",
    REVISAO_MAPA_DISPONIBILIZADA: "Revisão do mapa disponibilizada",
    REVISAO_MAPA_EM_ANDAMENTO: "Revisão do mapa em andamento",
    MAPA_VALIDADO: "Mapa validado",
    MAPA_COM_SUGESTOES: "Mapa com sugestões",
    MAPA_HOMOLOGADO: "Mapa homologado",
    REVISAO_CADASTRO_HOMOLOGADA: "Revisão do cadastro homologada",
    CADASTRO_HOMOLOGADO: "Cadastro homologado",
    CADASTRO_ACEITO: "Cadastro aceito",
    REVISAO_CADASTRO_ACEITA: "Revisão do cadastro aceita",
    MAPA_AJUSTADO: "Mapa ajustado",
    MAPA_CRIADO: "Mapa criado",
    NAO_INICIADO: "Não iniciado",
} as const;

// Situações de mapa
export const SITUACOES_MAPA = {
    VIGENTE: "vigente",
    DISPONIBILIZADO: "disponibilizado",
    EM_ANDAMENTO: "em_andamento",
    DISPONIVEL_VALIDACAO: "disponivel_validacao",
} as const;

// Labels para exibição
export const LABELS_SITUACAO = {
    [SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO]: "Cadastro disponibilizado",
    [SITUACOES_SUBPROCESSO.MAPA_VALIDADO]: "Mapa validado",
    [SITUACOES_SUBPROCESSO.MAPA_HOMOLOGADO]: "Mapa homologado",
    [SITUACOES_SUBPROCESSO.MAPA_CRIADO]: "Mapa criado",
    [SITUACOES_MAPA.VIGENTE]: "Vigente",
    [SITUACOES_MAPA.DISPONIBILIZADO]: "Disponibilizado",
    [SITUACOES_MAPA.EM_ANDAMENTO]: "Em andamento",
    [SITUACOES_MAPA.DISPONIVEL_VALIDACAO]: "Disponibilizado",
    CADASTRO_EM_ANDAMENTO: SITUACOES_SUBPROCESSO.CADASTRO_EM_ANDAMENTO,
    NAO_DISPONIBILIZADO: "Não disponibilizado",
} as const;

// Classes CSS para badges de situação
export const CLASSES_BADGE_SITUACAO = {
    [SITUACOES_SUBPROCESSO.AGUARDANDO]: "bg-warning text-dark",
    [SITUACOES_SUBPROCESSO.EM_ANDAMENTO]: "bg-warning text-dark",
    [SITUACOES_SUBPROCESSO.AGUARDANDO_VALIDACAO]: "bg-warning text-dark",
    [SITUACOES_SUBPROCESSO.FINALIZADO]: "bg-success",
    [SITUACOES_SUBPROCESSO.VALIDADO]: "bg-success",
    [SITUACOES_SUBPROCESSO.DEVOLVIDO]: "bg-danger",
    [SITUACOES_SUBPROCESSO.REVISAO_MAPA_EM_ANDAMENTO]: "bg-warning text-dark",
    [SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_EM_ANDAMENTO]: "bg-warning text-dark",
    [SITUACOES_SUBPROCESSO.MAPA_VALIDADO]: "bg-success",
    [SITUACOES_SUBPROCESSO.MAPA_COM_SUGESTOES]: "bg-warning text-dark",
    [SITUACOES_SUBPROCESSO.MAPA_HOMOLOGADO]: "bg-success",
    [SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_HOMOLOGADA]: "bg-success",
    [SITUACOES_SUBPROCESSO.CADASTRO_HOMOLOGADO]: "bg-success",
    [SITUACOES_SUBPROCESSO.CADASTRO_ACEITO]: "bg-success",
    [SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_ACEITA]: "bg-success",
    [SITUACOES_SUBPROCESSO.MAPA_AJUSTADO]: "bg-info",
    [SITUACOES_SUBPROCESSO.MAPA_CRIADO]: "bg-info",
    // Mapas (estados do mapa)
    [SITUACOES_MAPA.VIGENTE]: "bg-success",
    [SITUACOES_MAPA.DISPONIBILIZADO]: "bg-warning text-dark",
    [SITUACOES_MAPA.EM_ANDAMENTO]: "bg-warning text-dark",
} as const;

/**
 * Constantes de textos estáticos do sistema.
 *
 * Prioriza a centralização das mensagens de sucesso exibidas em toast, conforme
 * o plano descrito em msg-report.md.
 */
export const TEXTOS = {
  sucesso: {
    ACEITE_REGISTRADO: "Aceite registrado",
    ACAO_EM_BLOCO_REALIZADA: "Ação em bloco realizada",
    ACEITES_REGISTRADOS_EM_BLOCO: "Aceites registrados em bloco",
    CADASTRO_ATIVIDADES_DISPONIBILIZADO: "Cadastro de atividades disponibilizado",
    CADASTROS_ACEITOS_EM_BLOCO: "Cadastros aceitos em bloco",
    CADASTROS_HOMOLOGADOS_EM_BLOCO: "Cadastros homologados em bloco",
    DEVOLUCAO_REALIZADA: "Devolução realizada",
    HOMOLOGACAO_EFETIVADA: "Homologação efetivada",
    HOMOLOGACOES_REGISTRADAS_EM_BLOCO: "Homologações registradas em bloco",
    MAPA_DISPONIBILIZADO: "Disponibilização do mapa de competências efetuada",
    MAPA_SUBMETIDO_COM_SUGESTOES: "Mapa submetido com sugestões para análise da unidade superior",
    MAPA_VALIDADO_SUBMETIDO: "Mapa validado e submetido para análise à unidade superior",
    MAPAS_ACEITOS_EM_BLOCO: "Mapas aceitos em bloco",
    MAPAS_DISPONIBILIZADOS_EM_BLOCO: "Mapas de competências disponibilizados em bloco",
    MAPAS_HOMOLOGADOS_EM_BLOCO: "Mapas de competências homologados em bloco",
    PROCESSO_ALTERADO: "Processo alterado.",
    PROCESSO_CRIADO: "Processo criado.",
    PROCESSO_FINALIZADO: "Processo finalizado",
    PROCESSO_INICIADO: "Processo iniciado",
    PROCESSO_REMOVIDO: (descricao: string) => `Processo ${descricao} removido`,
    REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA: "Revisão do cadastro de atividades disponibilizada",
  },
} as const;

export type Textos = typeof TEXTOS;

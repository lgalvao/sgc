/**
 * Constantes de textos estáticos do sistema.
 *
 * Centraliza mensagens de sucesso, além de textos recorrentes de views e fluxos
 * de ação em bloco, conforme o plano descrito em msg-report.md.
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
  processo: {
    CONCLUIDO: "Processo concluído.",
    FINALIZAR: "Finalizar processo",
    CARREGANDO_DETALHES: "Carregando detalhes do processo...",
    FINALIZACAO_TITULO: "Finalização de processo",
    FINALIZACAO_CONFIRMACAO_PREFIXO: "Confirma a finalização do processo",
    FINALIZACAO_CONFIRMACAO_COMPLEMENTO:
      "Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades participantes do processo.",
    ERRO_PADRAO: "Ocorreu um erro",
    ERRO_ACAO_BLOCO: "Erro ao executar ação em bloco",
  },
  acaoBloco: {
    aceitar: {
      ROTULO_CADASTRO: "Aceitar cadastro em bloco",
      ROTULO_VALIDACAO: "Aceitar mapas em bloco",
      ROTULO_MISTO: "Registrar aceite em bloco",
      TITULO_CADASTRO: "Aceite de cadastro em bloco",
      TITULO_VALIDACAO: "Aceite de mapas em bloco",
      TITULO_MISTO: "Aceite em bloco",
      TEXTO_CADASTRO: "Selecione as unidades cujos cadastros deverão ser aceitos:",
      TEXTO_VALIDACAO: "Selecione as unidades para aceite dos mapas correspondentes",
      TEXTO_MISTO: "Selecione as unidades para registrar o aceite correspondente.",
      BOTAO: "Registrar aceite",
    },
    homologar: {
      ROTULO_CADASTRO: "Homologar em bloco",
      ROTULO_VALIDACAO: "Homologar mapa de competências em bloco",
      ROTULO_MISTO: "Homologar em bloco",
      TITULO_CADASTRO: "Homologação de cadastro em bloco",
      TITULO_VALIDACAO: "Homologação de mapa em bloco",
      TITULO_MISTO: "Homologação em bloco",
      TEXTO_CADASTRO: "Selecione abaixo as unidades cujos cadastros deverão ser homologados:",
      TEXTO_VALIDACAO: "Selecione abaixo as unidades cujos mapas deverão ser homologados:",
      TEXTO_MISTO: "Selecione as unidades para homologação em bloco.",
      BOTAO: "Homologar",
    },
    disponibilizar: {
      ROTULO: "Disponibilizar mapas em bloco",
      TITULO: "Disponibilização de mapa em bloco",
      TEXTO: "Selecione abaixo as unidades cujos mapas deverão ser disponibilizados:",
      BOTAO: "Disponibilizar",
    },
  },
} as const;

export type Textos = typeof TEXTOS;

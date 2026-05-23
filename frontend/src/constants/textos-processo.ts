export const TEXTOS_TABELA_PROCESSOS = {
    EMPTY_TITLE: "Sem processos",
    EMPTY_DESCRIPTION: "Processos ativos aparecem aqui.",
} as const;

export const TEXTOS_HISTORICO = {
    EMPTY_TITLE: "Sem processos inativos",
    EMPTY_DESCRIPTION: (dias: number) => `Processos finalizados há mais de ${dias} dias aparecem aqui.`,
} as const;

export const TEXTOS_PROCESSO = {
    FINALIZAR: "Finalizar",
    ACOES_EM_BLOCO: "Ações em bloco",
    CARREGANDO_DETALHES: "Carregando detalhes do processo...",
    ERRO_PADRAO: "Ocorreu um erro",
    FINALIZACAO_TITULO: "Finalização de processo",
    FINALIZACAO_CONFIRMACAO_PREFIXO: "Confirma a finalização do processo",
    FINALIZACAO_CONFIRMACAO_COMPLEMENTO:
        "Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades participantes do processo.",
    ERRO_ACAO_BLOCO: "Erro ao executar ação em bloco",
    INFO_TIPO: "Tipo",
    INFO_SITUACAO: "Situação",
    INFO_DATA_LIMITE: "Data limite",
    INFO_UNIDADES: "Unidades participantes",
    cadastro: {
        TITULO: "Cadastro de processo",
        BOTAO_INICIAR: "Iniciar",
        BOTAO_SALVAR: "Salvar",
        BOTAO_REMOVER: "Remover",
        BOTAO_CANCELAR: "Cancelar",
        INICIAR_TITULO: "Iniciar processo",
        INICIAR_CONFIRMACAO: "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes serão notificadas por e-mail.",
        REMOVER_TITULO: "Remover processo",
        REMOVER_CONFIRMACAO: (desc: string) => `Remover o processo '${desc}'? Esta ação não poderá ser desfeita.`,
        ERRO_CARREGAR_DETALHES: "Não foi possível carregar os detalhes do processo.",
        ERRO_CARREGAR_UNIDADES: "Não foi possível carregar as unidades disponíveis.",
        ERRO_CRIAR_PARA_INICIAR: "Não foi possível criar o processo para iniciá-lo.",
        ERRO_INICIAR_PROCESSO: "Não foi possível iniciar o processo. Tente novamente.",
        ERRO_REMOVER_PROCESSO: "Não foi possível remover o processo. Tente novamente.",
    },
} as const;

export const TEXTOS_ACAO_BLOCO = {
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
        ROTULO_VALIDACAO: "Homologar mapas em bloco",
        ROTULO_MISTO: "Homologar em bloco",
        TITULO_CADASTRO: "Homologação de cadastro em bloco",
        TITULO_VALIDACAO: "Homologação de mapa em bloco",
        TITULO_MISTO: "Homologação em bloco",
        TEXTO_CADASTRO: "Selecione as unidades cujos cadastros deverão ser homologados:",
        TEXTO_VALIDACAO: "Selecione as unidades cujos mapas deverão ser homologados:",
        TEXTO_MISTO: "Selecione as unidades para homologação em bloco.",
        BOTAO: "Homologar",
    },
    disponibilizar: {
        ROTULO: "Disponibilizar em bloco",
        TITULO: "Disponibilização de mapa em bloco",
        TEXTO: "Selecione as unidades cujos mapas deverão ser disponibilizados:",
        BOTAO: "Disponibilizar",
    },
} as const;

export const TEXTOS_SUCESSO_PROCESSO = {
    CADASTROS_ACEITOS_EM_BLOCO: "Cadastros aceitos em bloco",
    CADASTROS_HOMOLOGADOS_EM_BLOCO: "Cadastros homologados em bloco",
    MAPAS_ACEITOS_EM_BLOCO: "Mapas aceitos em bloco",
    MAPAS_DISPONIBILIZADOS_EM_BLOCO: "Mapas disponibilizados em bloco",
    MAPAS_HOMOLOGADOS_EM_BLOCO: "Mapas de competências homologados em bloco",
    PROCESSO_ALTERADO: "Processo alterado.",
    PROCESSO_CRIADO: "Processo criado.",
    PROCESSO_FINALIZADO: "Processo finalizado",
    PROCESSO_INICIADO: "Processo iniciado",
    PROCESSO_REMOVIDO: (descricao: string) => `Processo ${descricao} removido`,
} as const;

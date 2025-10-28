package sgc.subprocesso.modelo;

// TODO esse enum nao esta compativel com o especificado em /reqs/_situacoes.md
public enum SituacaoSubprocesso {
    NAO_INICIADO,
    CADASTRO_EM_ANDAMENTO,
    CADASTRO_DISPONIBILIZADO,
    CADASTRO_HOMOLOGADO,
    MAPA_CRIADO,
    MAPA_DISPONIBILIZADO,
    MAPA_COM_SUGESTOES,
    MAPA_VALIDADO,
    MAPA_HOMOLOGADO,
    REVISAO_CADASTRO_EM_ANDAMENTO,
    REVISAO_CADASTRO_DISPONIBILIZADA,
    AGUARDANDO_HOMOLOGACAO_CADASTRO,
    REVISAO_CADASTRO_HOMOLOGADA,
    MAPA_AJUSTADO;

    public boolean isFinalizado() {
        return this == MAPA_HOMOLOGADO;
    }
}

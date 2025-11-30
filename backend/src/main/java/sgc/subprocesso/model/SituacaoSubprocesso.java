package sgc.subprocesso.model;

import lombok.Getter;

@Getter
public enum SituacaoSubprocesso {
    NAO_INICIADO("Não Iniciado"),
    CADASTRO_EM_ANDAMENTO("Cadastro em Andamento"),
    CADASTRO_DISPONIBILIZADO("Cadastro Disponibilizado"),
    CADASTRO_HOMOLOGADO("Cadastro Homologado"),
    MAPA_CRIADO("Mapa Criado"),
    MAPA_ELABORADO("Mapa Elaborado"),
    MAPA_DISPONIBILIZADO("Mapa disponibilizado"),
    MAPA_COM_SUGESTOES("Mapa com Sugestões"),
    MAPA_VALIDADO("Mapa Validado"),
    MAPA_HOMOLOGADO("Mapa Homologado"),
    REVISAO_CADASTRO_EM_ANDAMENTO("Revisão do Cadastro em Andamento"),
    REVISAO_CADASTRO_DISPONIBILIZADA("Revisão do Cadastro Disponibilizada"),
    REVISAO_CADASTRO_HOMOLOGADA("Revisão do Cadastro Homologada"),
    MAPA_AJUSTADO("Mapa Ajustado"),
    CONCLUIDO("Concluído"),
    ATIVIDADES_HOMOLOGADAS("Atividades Homologadas");

    private final String descricao;

    SituacaoSubprocesso(String descricao) {
        this.descricao = descricao;
    }

}

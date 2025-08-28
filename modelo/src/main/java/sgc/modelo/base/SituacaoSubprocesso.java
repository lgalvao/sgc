package sgc.modelo.base;

import lombok.Getter;

@Getter
public enum SituacaoSubprocesso {
    MAPEAMENTO_NAO_INICIADO("Não iniciado"),
    MAPEAMENTO_CADASTRO_EM_ANDAMENTO("Cadastro em andamento"),
    MAPEAMENTO_CADASTRO_DISPONIBILIZADO("Cadastro disponibilizado"),
    MAPEAMENTO_CADASTRO_HOMOLOGADO("Cadastro homologado"),
    MAPEAMENTO_MAPA_CRIADO("Mapa criado"),
    MAPEAMENTO_MAPA_DISPONIBILIZADO("Mapa disponibilizado"),
    MAPEAMENTO_MAPA_COM_SUGESTOES("Mapa com sugestões"),
    MAPEAMENTO_MAPA_VALIDADO("Mapa validado"),
    MAPEAMENTO_MAPA_HOMOLOGADO("Mapa homologado"),

    REVISAO_NAO_INICIADO("Não iniciado"),
    REVISAO_CADASTRO_EM_ANDAMENTO("Revisão do cadastro em andamento"),
    REVISAO_CADASTRO_DISPONIBILIZADO("Revisão do cadastro disponibilizada"),
    REVISAO_CADASTRO_HOMOLOGADO("Revisão do cadastro homologada"),
    REVISAO_MAPA_AJUSTADO("Mapa ajustado"),
    REVISAO_MAPA_DISPONIBILIZADO("Mapa disponibilizado"),
    REVISAO_MAPA_COM_SUGESTOES("Mapa com sugestões"),
    REVISAO_MAPA_VALIDADO("Mapa validado"),
    REVISAO_MAPA_HOMOLOGADO("Mapa homologado"),

    DIAGNOSTICO_NAO_INICIADO("Não iniciado"),
    DIAGNOSTICO_EM_ANDAMENTO("Diagnóstico em andamento"),
    DIAGNOSTICO_FINALIZADO("Diagnóstico finalizado");

    final String descricao;

    SituacaoSubprocesso(String descricao) {
        this.descricao = descricao;
    }
}
package sgc.subprocesso.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SituacaoSubprocesso {
    NAO_INICIADO("Não Iniciado"),
    EM_ANDAMENTO("Em Andamento"),
    ENVIADO("Enviado"),
    CONCLUIDO("Concluído"),
    EM_REVISAO("Em Revisão"),
    DEVOLVIDO_CADASTRO("Devolvido para Cadastro"),
    DEVOLVIDO_REVISAO_CADASTRO("Devolvido para Revisão de Cadastro"),
    REVISAO_CADASTRO_HOMOLOGADA("Revisão de Cadastro Homologada"),
    MAPA_AJUSTADO("Mapa Ajustado"),
    MAPA_DISPONIBILIZADO("Mapa Disponibilizado"),
    EM_HOMOLOGACAO("Em Homologação"),
    HOMOLOGADO("Homologado"),
    MAPA_HOMOLOGADO("Mapa Homologado"),
    CADASTRO_DISPONIBILIZADO("Cadastro Disponibilizado"),
    ATIVIDADES_HOMOLOGADAS("Atividades Homologadas"),
    CADASTRO_EM_ANDAMENTO("Cadastro em Andamento"),
    CADASTRO_HOMOLOGADO("Cadastro Homologado"),
    MAPA_COM_SUGESTOES("Mapa com Sugestões"),
    MAPA_CRIADO("Mapa Criado"),
    MAPA_VALIDADO("Mapa Validado"),
    REVISAO_CADASTRO_DISPONIBILIZADA("Revisão de Cadastro Disponibilizada"),
    REVISAO_CADASTRO_EM_ANDAMENTO("Revisão de Cadastro em Andamento");

    private final String descricao;
}

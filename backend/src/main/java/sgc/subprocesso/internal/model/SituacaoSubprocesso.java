package sgc.subprocesso.internal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SituacaoSubprocesso {
    NAO_INICIADO("Não Iniciado"),

    // Mapeamento
    MAPEAMENTO_CADASTRO_EM_ANDAMENTO("Cadastro em Andamento"),
    MAPEAMENTO_CADASTRO_DISPONIBILIZADO("Cadastro Disponibilizado"),
    MAPEAMENTO_CADASTRO_HOMOLOGADO("Cadastro Homologado"),
    MAPEAMENTO_MAPA_CRIADO("Mapa Criado"),
    MAPEAMENTO_MAPA_DISPONIBILIZADO("Mapa Disponibilizado"),
    MAPEAMENTO_MAPA_COM_SUGESTOES("Mapa com Sugestões"),
    MAPEAMENTO_MAPA_VALIDADO("Mapa Validado"),
    MAPEAMENTO_MAPA_HOMOLOGADO("Mapa Homologado"),

    // Revisão
    REVISAO_CADASTRO_EM_ANDAMENTO("Revisão de Cadastro em Andamento"),
    REVISAO_CADASTRO_DISPONIBILIZADA("Revisão de Cadastro Disponibilizada"),
    REVISAO_CADASTRO_HOMOLOGADA("Revisão de Cadastro Homologada"),
    REVISAO_MAPA_AJUSTADO("Mapa Ajustado"),
    REVISAO_MAPA_DISPONIBILIZADO("Mapa Disponibilizado"),
    REVISAO_MAPA_COM_SUGESTOES("Mapa com Sugestões"),
    REVISAO_MAPA_VALIDADO("Mapa Validado"),
    REVISAO_MAPA_HOMOLOGADO("Mapa Homologado"),

    // Diagnóstico
    DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO("Autoavaliação em Andamento"),
    DIAGNOSTICO_MONITORAMENTO("Monitoramento"),
    DIAGNOSTICO_CONCLUIDO("Concluído");

    private final String descricao;
}

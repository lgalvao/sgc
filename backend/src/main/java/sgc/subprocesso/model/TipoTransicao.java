package sgc.subprocesso.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Define os tipos de transição de subprocesso e seus metadados para comunicação.
 */
@Getter
@RequiredArgsConstructor
public enum TipoTransicao {
    CADASTRO_DISPONIBILIZADO(
            "Disponibilização do cadastro de atividades",
            "Cadastro de atividades/conhecimentos da unidade %s disponibilizado para análise",
            "cadastro-disponibilizado",
            "cadastro-disponibilizado-superior"
    ),

    CADASTRO_DEVOLVIDO(
            "Devolução do cadastro de atividades para ajustes",
            "Cadastro de atividades da unidade %s devolvido para ajustes",
            "cadastro-devolvido",
            "cadastro-devolvido-superior"
    ),

    CADASTRO_ACEITO(
            "Cadastro de atividades e conhecimentos aceito",
            "Cadastro de atividades da unidade %s submetido para análise",
            "aceite-cadastro",
            "aceite-cadastro-superior"
    ),

    CADASTRO_HOMOLOGADO(
            "Cadastro de atividades e conhecimentos homologado",
            null,
            null,
            null
    ),

    CADASTRO_REABERTO(
            "Reabertura de cadastro de atividades",
            "Cadastro de atividades da unidade %s reaberto para ajustes",
            "cadastro-reaberto",
            "cadastro-reaberto-superior"
    ),

    REVISAO_CADASTRO_DISPONIBILIZADA(
            "Disponibilização da revisão do cadastro de atividades",
            "Revisão do cadastro da unidade %s disponibilizada para análise",
            "disponibilizacao-revisao-cadastro",
            "disponibilizacao-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_DEVOLVIDA(
            "Devolução da revisão do cadastro para ajustes",
            "Revisão do cadastro da unidade %s devolvida para ajustes",
            "devolucao-revisao-cadastro",
            "devolucao-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_ACEITA(
            "Revisão do cadastro de atividades e conhecimentos aceita",
            "Revisão do cadastro da unidade %s submetida para análise",
            "aceite-revisao-cadastro",
            "aceite-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_HOMOLOGADA(
            "Revisão do cadastro homologada",
            null,
            null,
            null
    ),

    REVISAO_CADASTRO_REABERTA(
            "Reabertura de revisão de cadastro de atividades",
            "Revisão do cadastro da unidade %s reaberta para ajustes",
            "revisao-cadastro-reaberta",
            "revisao-cadastro-reaberta-superior"
    ),

    MAPA_DISPONIBILIZADO(
            "Disponibilização do mapa de competências para validação",
            "Mapa de competências da unidade %s disponibilizado para validação",
            "mapa-disponibilizado",
            "mapa-disponibilizado-superior"
    ),

    MAPA_SUGESTOES_APRESENTADAS(
            "Sugestões apresentadas para o mapa de competências",
            "Sugestões para o mapa de competências da unidade %s aguardando análise",
            "sugestoes-mapa",
            "sugestoes-mapa-superior"
    ),

    MAPA_VALIDADO(
            "Validação do mapa de competências",
            "Validação do mapa de competências da unidade %s aguardando análise",
            "validacao-mapa",
            "validacao-mapa-superior"
    ),

    MAPA_VALIDACAO_DEVOLVIDA(
            "Devolução da validação do mapa de competências para ajustes",
            "Validação do mapa da unidade %s devolvida para ajustes",
            "devolucao-validacao",
            "devolucao-validacao-superior"
    ),

    MAPA_VALIDACAO_ACEITA(
            "Validação do mapa aceita",
            "Validação do mapa da unidade %s submetida para análise",
            "aceite-validacao",
            "aceite-validacao-superior"
    ),

    MAPA_HOMOLOGADO(
            "Mapa de competências homologado",
            null,
            null,
            null
    ),

    PROCESSO_INICIADO(
            "Processo iniciado",
            "Início do processo",
            "processo-iniciado",
            null
    );

    private final String descMovimentacao;
    private final String templateAlerta;
    private final String templateEmail;
    private final String templateEmailSuperior;

    public String formatarAlerta(String siglaUnidade) {
        return templateAlerta != null ? templateAlerta.formatted(siglaUnidade) : "";
    }

    public boolean geraAlerta() {
        return templateAlerta != null;
    }

    public boolean enviaEmail() {
        return templateEmail != null;
    }

    public boolean notificacaoSuperior() {
        return templateEmailSuperior != null;
    }
}

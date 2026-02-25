package sgc.subprocesso.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Define os tipos de transição de subprocesso e seus metadados para comunicação.
 *
 * <p>Cada tipo contém:
 * <ul>
 *   <li>Descrição para a movimentação (trilha de auditoria)</li>
 *   <li>Template para o alerta (pode ser null se não gera alerta)</li>
 *   <li>Template Thymeleaf para e-mail (pode ser null se não envia e-mail)</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum TipoTransicao {
    CADASTRO_DISPONIBILIZADO(
            "Disponibilização do cadastro de atividades",
            "Cadastro de atividades/conhecimentos da unidade %s disponibilizado para análise",
            "cadastro-disponibilizado"
    ),

    CADASTRO_DEVOLVIDO(
            "Devolução do cadastro de atividades para ajustes",
            "Cadastro de atividades da unidade %s devolvido para ajustes",
            "cadastro-devolvido"
    ),

    CADASTRO_ACEITO(
            "Cadastro de atividades e conhecimentos aceito",
            "Cadastro de atividades da unidade %s submetido para análise",
            "aceite-cadastro"
    ),

    CADASTRO_HOMOLOGADO(
            "Cadastro de atividades e conhecimentos homologado",
            null,  // Não gera alerta
            null   // Não envia e-mail
    ),

    CADASTRO_REABERTO(
            "Reabertura de cadastro de atividades",
            "Cadastro de atividades da unidade %s reaberto para ajustes",
            "cadastro-reaberto"
    ),

    REVISAO_CADASTRO_DISPONIBILIZADA(
            "Disponibilização da revisão do cadastro de atividades",
            "Revisão do cadastro da unidade %s disponibilizada para análise",
            "disponibilizacao-revisao-cadastro"
    ),

    REVISAO_CADASTRO_DEVOLVIDA(
            "Devolução da revisão do cadastro para ajustes",
            "Revisão do cadastro da unidade %s devolvida para ajustes",
            "devolucao-revisao-cadastro"
    ),

    REVISAO_CADASTRO_ACEITA(
            "Revisão do cadastro de atividades e conhecimentos aceita",
            "Revisão do cadastro da unidade %s submetida para análise",
            "aceite-revisao-cadastro"
    ),

    REVISAO_CADASTRO_HOMOLOGADA(
            "Revisão do cadastro homologada",
            null,
            null
    ),

    REVISAO_CADASTRO_REABERTA(
            "Reabertura de revisão de cadastro de atividades",
            "Revisão do cadastro da unidade %s reaberta para ajustes",
            "revisao-cadastro-reaberta"
    ),

    MAPA_DISPONIBILIZADO(
            "Disponibilização do mapa de competências para validação",
            "Mapa de competências da unidade %s disponibilizado para validação",
            "mapa-disponibilizado"
    ),

    MAPA_SUGESTOES_APRESENTADAS(
            "Sugestões apresentadas para o mapa de competências",
            "Sugestões para o mapa de competências da unidade %s aguardando análise",
            "sugestoes-mapa"
    ),

    MAPA_VALIDADO(
            "Validação do mapa de competências",
            "Validação do mapa de competências da unidade %s aguardando análise",
            "validacao-mapa"
    ),

    MAPA_VALIDACAO_DEVOLVIDA(
            "Devolução da validação do mapa de competências para ajustes",
            "Validação do mapa da unidade %s devolvida para ajustes",
            "devolucao-validacao"
    ),

    MAPA_VALIDACAO_ACEITA(
            "Validação do mapa aceita",
            "Validação do mapa da unidade %s submetida para análise",
            "aceite-validacao"
    ),

    MAPA_HOMOLOGADO(
            "Mapa de competências homologado",
            null,
            null
    ),

    PROCESSO_INICIADO(
            "Processo iniciado",
            "Início do processo",
            "processo-iniciado"
    );

    private final String descricaoMovimentacao;
    private final @Nullable String templateAlerta;  // null = não gera alerta
    private final @Nullable String templateEmail;   // null = não envia e-mail

    /**
     * Formata a descrição do alerta substituindo %s pela sigla da unidade.
     */
    public String formatarAlerta(String siglaUnidade) {
        return templateAlerta != null ? templateAlerta.formatted(siglaUnidade) : "";
    }

    public boolean geraAlerta() {
        return templateAlerta != null;
    }

    public boolean enviaEmail() {
        return templateEmail != null;
    }
}

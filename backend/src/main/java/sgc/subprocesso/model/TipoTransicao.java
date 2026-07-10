package sgc.subprocesso.model;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.*;

/**
 * Define os tipos de transição de subprocesso e seus metadados para comunicação.
 */
@Getter
@RequiredArgsConstructor
public enum TipoTransicao {
    CADASTRO_DISPONIBILIZADO(
            Mensagens.HIST_CADASTRO_DISPONIBILIZADO,
            Mensagens.ALERTA_CADASTRO_DISPONIBILIZADO,
            "cadastro-disponibilizado"
    ),

    CADASTRO_DEVOLVIDO(
            Mensagens.HIST_CADASTRO_DEVOLVIDO,
            Mensagens.ALERTA_CADASTRO_DEVOLVIDO,
            "cadastro-devolvido"
    ),

    CADASTRO_ACEITO(
            Mensagens.HIST_CADASTRO_ACEITO,
            Mensagens.ALERTA_CADASTRO_ACEITO,
            "aceite-cadastro"
    ),

    CADASTRO_HOMOLOGADO(
            Mensagens.HIST_CADASTRO_HOMOLOGADO,
            null,
            null
    ),

    CADASTRO_REABERTO(
            Mensagens.HIST_CADASTRO_REABERTO,
            null,
            "cadastro-reaberto"
    ),

    REVISAO_CADASTRO_DISPONIBILIZADA(
            Mensagens.HIST_REVISAO_DISPONIBILIZADA,
            Mensagens.ALERTA_REVISAO_DISPONIBILIZADA,
            "disponibilizacao-revisao-cadastro"
    ),

    REVISAO_CADASTRO_DEVOLVIDA(
            Mensagens.HIST_REVISAO_DEVOLVIDA,
            Mensagens.ALERTA_REVISAO_DEVOLVIDA,
            "devolucao-revisao-cadastro"
    ),

    REVISAO_CADASTRO_ACEITA(
            Mensagens.HIST_REVISAO_ACEITA,
            Mensagens.ALERTA_REVISAO_ACEITA,
            "aceite-revisao-cadastro"
    ),

    REVISAO_CADASTRO_HOMOLOGADA(
            Mensagens.HIST_REVISAO_HOMOLOGADA,
            null,
            null
    ),

    REVISAO_CADASTRO_REABERTA(
            Mensagens.HIST_REVISAO_REABERTA,
            null,
            "revisao-cadastro-reaberta"
    ),

    MAPA_DISPONIBILIZADO(
            Mensagens.HIST_MAPA_DISPONIBILIZADO,
            Mensagens.ALERTA_MAPA_DISPONIBILIZADO,
            "mapa-disponibilizado"
    ),

    MAPA_SUGESTOES_APRESENTADAS(
            Mensagens.HIST_MAPA_SUGESTOES_APRESENTADAS,
            Mensagens.ALERTA_MAPA_SUGESTOES,
            "sugestoes-mapa"
    ),

    MAPA_VALIDADO(
            Mensagens.HIST_MAPA_VALIDADO,
            Mensagens.ALERTA_MAPA_VALIDACAO_PENDENTE,
            "validacao-mapa"
    ),

    MAPA_VALIDACAO_DEVOLVIDA(
            Mensagens.HIST_MAPA_VALIDACAO_DEVOLVIDA,
            Mensagens.ALERTA_MAPA_VALIDACAO_DEVOLVIDA,
            "devolucao-validacao"
    ),

    MAPA_VALIDACAO_ACEITA(
            Mensagens.HIST_MAPA_VALIDACAO_ACEITA,
            Mensagens.ALERTA_MAPA_VALIDACAO_ACEITA,
            "aceite-validacao"
    ),

    MAPA_HOMOLOGADO(
            Mensagens.HIST_MAPA_HOMOLOGADO,
            null,
            null
    ),

    DIAGNOSTICO_CONCLUIDO(
            Mensagens.HIST_DIAGNOSTICO_CONCLUIDO,
            Mensagens.ALERTA_DIAGNOSTICO_CONCLUIDO,
            "diagnostico-concluido"
    ),

    DIAGNOSTICO_DEVOLVIDO(
            Mensagens.HIST_DIAGNOSTICO_DEVOLVIDO,
            Mensagens.ALERTA_DIAGNOSTICO_DEVOLVIDO,
            "diagnostico-devolvido"
    ),

    DIAGNOSTICO_ACEITO(
            Mensagens.HIST_DIAGNOSTICO_ACEITO,
            Mensagens.ALERTA_DIAGNOSTICO_ACEITO,
            "diagnostico-aceito"
    ),

    DIAGNOSTICO_HOMOLOGADO(
            Mensagens.HIST_DIAGNOSTICO_HOMOLOGADO,
            null,
            null
    );

    private final String descMovimentacao;
    private final @Nullable String templateAlerta;
    private final @Nullable String templateEmail;

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

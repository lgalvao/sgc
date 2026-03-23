package sgc.subprocesso.model;

import lombok.*;
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
            "cadastro-disponibilizado",
            "cadastro-disponibilizado-superior"
    ),

    CADASTRO_DEVOLVIDO(
            Mensagens.HIST_CADASTRO_DEVOLVIDO,
            Mensagens.ALERTA_CADASTRO_DEVOLVIDO,
            "cadastro-devolvido",
            "cadastro-devolvido-superior"
    ),

    CADASTRO_ACEITO(
            Mensagens.HIST_CADASTRO_ACEITO,
            Mensagens.ALERTA_CADASTRO_ACEITO,
            "aceite-cadastro",
            "aceite-cadastro-superior"
    ),

    CADASTRO_HOMOLOGADO(
            Mensagens.HIST_CADASTRO_HOMOLOGADO,
            null,
            null,
            null
    ),

    CADASTRO_REABERTO(
            Mensagens.HIST_CADASTRO_REABERTO,
            Mensagens.ALERTA_CADASTRO_REABERTO,
            "cadastro-reaberto",
            "cadastro-reaberto-superior"
    ),

    REVISAO_CADASTRO_DISPONIBILIZADA(
            Mensagens.HIST_REVISAO_DISPONIBILIZADA,
            Mensagens.ALERTA_REVISAO_DISPONIBILIZADA,
            "disponibilizacao-revisao-cadastro",
            "disponibilizacao-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_DEVOLVIDA(
            Mensagens.HIST_REVISAO_DEVOLVIDA,
            Mensagens.ALERTA_REVISAO_DEVOLVIDA,
            "devolucao-revisao-cadastro",
            "devolucao-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_ACEITA(
            Mensagens.HIST_REVISAO_ACEITA,
            Mensagens.ALERTA_REVISAO_ACEITA,
            "aceite-revisao-cadastro",
            "aceite-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_HOMOLOGADA(
            Mensagens.HIST_REVISAO_HOMOLOGADA,
            null,
            null,
            null
    ),

    REVISAO_CADASTRO_REABERTA(
            Mensagens.HIST_REVISAO_REABERTA,
            Mensagens.ALERTA_REVISAO_REABERTA,
            "revisao-cadastro-reaberta",
            "revisao-cadastro-reaberta-superior"
    ),

    MAPA_DISPONIBILIZADO(
            Mensagens.HIST_MAPA_DISPONIBILIZADO,
            Mensagens.ALERTA_MAPA_DISPONIBILIZADO,
            "mapa-disponibilizado",
            "mapa-disponibilizado-superior"
    ),

    MAPA_SUGESTOES_APRESENTADAS(
            Mensagens.HIST_MAPA_SUGESTOES_APRESENTADAS,
            Mensagens.ALERTA_MAPA_SUGESTOES,
            "sugestoes-mapa",
            "sugestoes-mapa-superior"
    ),

    MAPA_VALIDADO(
            Mensagens.HIST_MAPA_VALIDADO,
            Mensagens.ALERTA_MAPA_VALIDACAO_PENDENTE,
            "validacao-mapa",
            "validacao-mapa-superior"
    ),

    MAPA_VALIDACAO_DEVOLVIDA(
            Mensagens.HIST_MAPA_VALIDACAO_DEVOLVIDA,
            Mensagens.ALERTA_MAPA_VALIDACAO_DEVOLVIDA,
            "devolucao-validacao",
            "devolucao-validacao-superior"
    ),

    MAPA_VALIDACAO_ACEITA(
            Mensagens.HIST_MAPA_VALIDACAO_ACEITA,
            Mensagens.ALERTA_MAPA_VALIDACAO_ACEITA,
            "aceite-validacao",
            "aceite-validacao-superior"
    ),

    MAPA_HOMOLOGADO(
            Mensagens.HIST_MAPA_HOMOLOGADO,
            null,
            null,
            null
    ),

    PROCESSO_INICIADO(
            Mensagens.HIST_PROCESSO_INICIADO,
            Mensagens.ALERTA_PROCESSO_INICIADO,
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

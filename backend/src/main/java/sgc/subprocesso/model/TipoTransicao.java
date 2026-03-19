package sgc.subprocesso.model;

import lombok.*;
import sgc.comum.SgcMensagens;

/**
 * Define os tipos de transição de subprocesso e seus metadados para comunicação.
 */
@Getter
@RequiredArgsConstructor
public enum TipoTransicao {
    CADASTRO_DISPONIBILIZADO(
            SgcMensagens.HIST_CADASTRO_DISPONIBILIZADO,
            SgcMensagens.ALERTA_CADASTRO_DISPONIBILIZADO,
            "cadastro-disponibilizado",
            "cadastro-disponibilizado-superior"
    ),

    CADASTRO_DEVOLVIDO(
            SgcMensagens.HIST_CADASTRO_DEVOLVIDO,
            SgcMensagens.ALERTA_CADASTRO_DEVOLVIDO,
            "cadastro-devolvido",
            "cadastro-devolvido-superior"
    ),

    CADASTRO_ACEITO(
            SgcMensagens.HIST_CADASTRO_ACEITO,
            SgcMensagens.ALERTA_CADASTRO_ACEITO,
            "aceite-cadastro",
            "aceite-cadastro-superior"
    ),

    CADASTRO_HOMOLOGADO(
            SgcMensagens.HIST_CADASTRO_HOMOLOGADO,
            null,
            null,
            null
    ),

    CADASTRO_REABERTO(
            SgcMensagens.HIST_CADASTRO_REABERTO,
            SgcMensagens.ALERTA_CADASTRO_REABERTO,
            "cadastro-reaberto",
            "cadastro-reaberto-superior"
    ),

    REVISAO_CADASTRO_DISPONIBILIZADA(
            SgcMensagens.HIST_REVISAO_DISPONIBILIZADA,
            SgcMensagens.ALERTA_REVISAO_DISPONIBILIZADA,
            "disponibilizacao-revisao-cadastro",
            "disponibilizacao-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_DEVOLVIDA(
            SgcMensagens.HIST_REVISAO_DEVOLVIDA,
            SgcMensagens.ALERTA_REVISAO_DEVOLVIDA,
            "devolucao-revisao-cadastro",
            "devolucao-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_ACEITA(
            SgcMensagens.HIST_REVISAO_ACEITA,
            SgcMensagens.ALERTA_REVISAO_ACEITA,
            "aceite-revisao-cadastro",
            "aceite-revisao-cadastro-superior"
    ),

    REVISAO_CADASTRO_HOMOLOGADA(
            SgcMensagens.HIST_REVISAO_HOMOLOGADA,
            null,
            null,
            null
    ),

    REVISAO_CADASTRO_REABERTA(
            SgcMensagens.HIST_REVISAO_REABERTA,
            SgcMensagens.ALERTA_REVISAO_REABERTA,
            "revisao-cadastro-reaberta",
            "revisao-cadastro-reaberta-superior"
    ),

    MAPA_DISPONIBILIZADO(
            SgcMensagens.HIST_MAPA_DISPONIBILIZADO,
            SgcMensagens.ALERTA_MAPA_DISPONIBILIZADO,
            "mapa-disponibilizado",
            "mapa-disponibilizado-superior"
    ),

    MAPA_SUGESTOES_APRESENTADAS(
            SgcMensagens.HIST_MAPA_SUGESTOES_APRESENTADAS,
            SgcMensagens.ALERTA_MAPA_SUGESTOES,
            "sugestoes-mapa",
            "sugestoes-mapa-superior"
    ),

    MAPA_VALIDADO(
            SgcMensagens.HIST_MAPA_VALIDADO,
            SgcMensagens.ALERTA_MAPA_VALIDACAO_PENDENTE,
            "validacao-mapa",
            "validacao-mapa-superior"
    ),

    MAPA_VALIDACAO_DEVOLVIDA(
            SgcMensagens.HIST_MAPA_VALIDACAO_DEVOLVIDA,
            SgcMensagens.ALERTA_MAPA_VALIDACAO_DEVOLVIDA,
            "devolucao-validacao",
            "devolucao-validacao-superior"
    ),

    MAPA_VALIDACAO_ACEITA(
            SgcMensagens.HIST_MAPA_VALIDACAO_ACEITA,
            SgcMensagens.ALERTA_MAPA_VALIDACAO_ACEITA,
            "aceite-validacao",
            "aceite-validacao-superior"
    ),

    MAPA_HOMOLOGADO(
            SgcMensagens.HIST_MAPA_HOMOLOGADO,
            null,
            null,
            null
    ),

    PROCESSO_INICIADO(
            SgcMensagens.HIST_PROCESSO_INICIADO,
            SgcMensagens.ALERTA_PROCESSO_INICIADO,
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

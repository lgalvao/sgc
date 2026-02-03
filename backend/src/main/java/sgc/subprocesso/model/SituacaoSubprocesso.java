package sgc.subprocesso.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SituacaoSubprocesso {
    NAO_INICIADO("Não iniciado"),

    // Mapeamento
    MAPEAMENTO_CADASTRO_EM_ANDAMENTO("Cadastro em andamento"),
    MAPEAMENTO_CADASTRO_DISPONIBILIZADO("Cadastro disponibilizado"),
    MAPEAMENTO_CADASTRO_HOMOLOGADO("Cadastro homologado"),
    MAPEAMENTO_MAPA_CRIADO("Mapa criado"),
    MAPEAMENTO_MAPA_DISPONIBILIZADO("Mapa disponibilizado"),
    MAPEAMENTO_MAPA_COM_SUGESTOES("Mapa com sugestões"),
    MAPEAMENTO_MAPA_VALIDADO("Mapa validado"),
    MAPEAMENTO_MAPA_HOMOLOGADO("Mapa homologado"),

    // Revisão
    REVISAO_CADASTRO_EM_ANDAMENTO("Revisão de cadastro em andamento"),
    REVISAO_CADASTRO_DISPONIBILIZADA("Revisão de cadastro disponibilizada"),
    REVISAO_CADASTRO_HOMOLOGADA("Revisão de cadastro homologada"),
    REVISAO_MAPA_AJUSTADO("Mapa ajustado"),
    REVISAO_MAPA_DISPONIBILIZADO("Mapa disponibilizado"),
    REVISAO_MAPA_COM_SUGESTOES("Mapa com sugestões"),
    REVISAO_MAPA_VALIDADO("Mapa validado"),
    REVISAO_MAPA_HOMOLOGADO("Mapa homologado"),

    // Diagnóstico
    DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO("Autoavaliação em andamento"),
    DIAGNOSTICO_MONITORAMENTO("Monitoramento"),
    DIAGNOSTICO_CONCLUIDO("Concluído");

    private final String descricao;

    public boolean podeTransicionarPara(SituacaoSubprocesso nova, sgc.processo.model.TipoProcesso tipo) {
        if (this == nova) return true;

        // Garantir que não misturamos situações de tipos diferentes
        if (this != NAO_INICIADO && nova != NAO_INICIADO) {
            if (this.name().startsWith("MAPEAMENTO") && !nova.name().startsWith("MAPEAMENTO")) return false;
            if (this.name().startsWith("REVISAO") && !nova.name().startsWith("REVISAO")) return false;
            if (this.name().startsWith("DIAGNOSTICO") && !nova.name().startsWith("DIAGNOSTICO")) return false;
        }

        return switch (this) {
            case NAO_INICIADO ->
                    (tipo == sgc.processo.model.TipoProcesso.MAPEAMENTO && nova == MAPEAMENTO_CADASTRO_EM_ANDAMENTO) ||
                    (tipo == sgc.processo.model.TipoProcesso.REVISAO && nova == REVISAO_CADASTRO_EM_ANDAMENTO) ||
                    (tipo == sgc.processo.model.TipoProcesso.DIAGNOSTICO && nova == DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);

            // --- MAPEAMENTO ---
            case MAPEAMENTO_CADASTRO_EM_ANDAMENTO ->
                    nova == MAPEAMENTO_CADASTRO_DISPONIBILIZADO;

            case MAPEAMENTO_CADASTRO_DISPONIBILIZADO ->
                    nova == MAPEAMENTO_CADASTRO_EM_ANDAMENTO ||
                    nova == MAPEAMENTO_CADASTRO_HOMOLOGADO;

            case MAPEAMENTO_CADASTRO_HOMOLOGADO ->
                    nova == MAPEAMENTO_MAPA_CRIADO ||
                    nova == MAPEAMENTO_MAPA_DISPONIBILIZADO ||
                    nova == MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

            case MAPEAMENTO_MAPA_CRIADO ->
                    nova == MAPEAMENTO_MAPA_DISPONIBILIZADO ||
                    nova == MAPEAMENTO_CADASTRO_HOMOLOGADO;

            case MAPEAMENTO_MAPA_DISPONIBILIZADO ->
                    nova == MAPEAMENTO_MAPA_COM_SUGESTOES ||
                    nova == MAPEAMENTO_MAPA_VALIDADO ||
                    nova == MAPEAMENTO_MAPA_CRIADO;

            case MAPEAMENTO_MAPA_COM_SUGESTOES ->
                    nova == MAPEAMENTO_MAPA_DISPONIBILIZADO ||
                    nova == MAPEAMENTO_MAPA_CRIADO;

            case MAPEAMENTO_MAPA_VALIDADO ->
                    nova == MAPEAMENTO_MAPA_HOMOLOGADO ||
                    nova == MAPEAMENTO_MAPA_DISPONIBILIZADO;

            case MAPEAMENTO_MAPA_HOMOLOGADO, REVISAO_MAPA_HOMOLOGADO, DIAGNOSTICO_CONCLUIDO -> false;

            // --- REVISÃO ---
            case REVISAO_CADASTRO_EM_ANDAMENTO ->
                    nova == REVISAO_CADASTRO_DISPONIBILIZADA;

            case REVISAO_CADASTRO_DISPONIBILIZADA ->
                    nova == REVISAO_CADASTRO_EM_ANDAMENTO ||
                    nova == REVISAO_CADASTRO_HOMOLOGADA ||
                    nova == REVISAO_MAPA_HOMOLOGADO;

            case REVISAO_CADASTRO_HOMOLOGADA ->
                    nova == REVISAO_MAPA_AJUSTADO ||
                    nova == REVISAO_MAPA_DISPONIBILIZADO ||
                    nova == REVISAO_CADASTRO_EM_ANDAMENTO;

            case REVISAO_MAPA_AJUSTADO ->
                    nova == REVISAO_MAPA_DISPONIBILIZADO ||
                    nova == REVISAO_CADASTRO_HOMOLOGADA;

            case REVISAO_MAPA_DISPONIBILIZADO ->
                    nova == REVISAO_MAPA_COM_SUGESTOES ||
                    nova == REVISAO_MAPA_VALIDADO ||
                    nova == REVISAO_MAPA_AJUSTADO;

            case REVISAO_MAPA_COM_SUGESTOES ->
                    nova == REVISAO_MAPA_DISPONIBILIZADO ||
                    nova == REVISAO_MAPA_AJUSTADO;

            case REVISAO_MAPA_VALIDADO ->
                    nova == REVISAO_MAPA_HOMOLOGADO ||
                    nova == REVISAO_MAPA_DISPONIBILIZADO;

            // --- DIAGNÓSTICO ---
            case DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO ->
                    nova == DIAGNOSTICO_MONITORAMENTO;

            case DIAGNOSTICO_MONITORAMENTO ->
                    nova == DIAGNOSTICO_CONCLUIDO;

        };
    }
}

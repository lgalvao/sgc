package sgc.subprocesso.model;

import lombok.*;
import sgc.processo.model.*;

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

    private static final String PREFIXO_MAPEAMENTO = "MAPEAMENTO";
    private static final String PREFIXO_REVISAO = "REVISAO";
    private static final String PREFIXO_DIAGNOSTICO = "DIAGNOSTICO";

    public boolean podeTransicionarPara(SituacaoSubprocesso nova, TipoProcesso tipo) {
        if (this == nova) return true;

        if (this == NAO_INICIADO) {
            return podeIniciar(nova, tipo);
        }

        if (!isSituacaoCompativel(nova)) return false;

        if (this.name().startsWith(PREFIXO_MAPEAMENTO)) {
            return transicaoMapeamento(nova);
        } else if (this.name().startsWith(PREFIXO_REVISAO)) {
            return transicaoRevisao(nova);
        } else {
            return transicaoDiagnostico(nova);
        }
    }

    private boolean isSituacaoCompativel(SituacaoSubprocesso nova) {
        if (nova == NAO_INICIADO) return true;
        if (this.name().startsWith(PREFIXO_MAPEAMENTO) && !nova.name().startsWith(PREFIXO_MAPEAMENTO)) return false;
        if (this.name().startsWith(PREFIXO_REVISAO) && !nova.name().startsWith(PREFIXO_REVISAO)) return false;
        return !this.name().startsWith(PREFIXO_DIAGNOSTICO) || nova.name().startsWith(PREFIXO_DIAGNOSTICO);
    }

    private boolean podeIniciar(SituacaoSubprocesso nova, TipoProcesso tipo) {
        return (tipo == TipoProcesso.MAPEAMENTO && nova == MAPEAMENTO_CADASTRO_EM_ANDAMENTO) ||
               (tipo == TipoProcesso.REVISAO && nova == REVISAO_CADASTRO_EM_ANDAMENTO) ||
               (tipo == TipoProcesso.DIAGNOSTICO && nova == DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
    }

    private boolean transicaoMapeamento(SituacaoSubprocesso nova) {
        return switch (this) {
            case MAPEAMENTO_CADASTRO_EM_ANDAMENTO ->
                    nova == MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
            case MAPEAMENTO_CADASTRO_DISPONIBILIZADO ->
                    nova == MAPEAMENTO_CADASTRO_EM_ANDAMENTO || nova == MAPEAMENTO_CADASTRO_HOMOLOGADO;
            case MAPEAMENTO_CADASTRO_HOMOLOGADO ->
                    nova == MAPEAMENTO_MAPA_CRIADO || nova == MAPEAMENTO_MAPA_DISPONIBILIZADO || nova == MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
            case MAPEAMENTO_MAPA_CRIADO ->
                    nova == MAPEAMENTO_MAPA_DISPONIBILIZADO || nova == MAPEAMENTO_CADASTRO_HOMOLOGADO;
            case MAPEAMENTO_MAPA_DISPONIBILIZADO ->
                    nova == MAPEAMENTO_MAPA_COM_SUGESTOES || nova == MAPEAMENTO_MAPA_VALIDADO || nova == MAPEAMENTO_MAPA_CRIADO;
            case MAPEAMENTO_MAPA_COM_SUGESTOES ->
                    nova == MAPEAMENTO_MAPA_DISPONIBILIZADO || nova == MAPEAMENTO_MAPA_CRIADO;
            case MAPEAMENTO_MAPA_VALIDADO ->
                    nova == MAPEAMENTO_MAPA_HOMOLOGADO || nova == MAPEAMENTO_MAPA_DISPONIBILIZADO;
            default -> false;
        };
    }

    private boolean transicaoRevisao(SituacaoSubprocesso nova) {
        return switch (this) {
            case REVISAO_CADASTRO_EM_ANDAMENTO ->
                    nova == REVISAO_CADASTRO_DISPONIBILIZADA;
            case REVISAO_CADASTRO_DISPONIBILIZADA ->
                    nova == REVISAO_CADASTRO_EM_ANDAMENTO || nova == REVISAO_CADASTRO_HOMOLOGADA || nova == REVISAO_MAPA_HOMOLOGADO;
            case REVISAO_CADASTRO_HOMOLOGADA ->
                    nova == REVISAO_MAPA_AJUSTADO || nova == REVISAO_MAPA_DISPONIBILIZADO || nova == REVISAO_CADASTRO_EM_ANDAMENTO;
            case REVISAO_MAPA_AJUSTADO ->
                    nova == REVISAO_MAPA_DISPONIBILIZADO || nova == REVISAO_CADASTRO_HOMOLOGADA;
            case REVISAO_MAPA_DISPONIBILIZADO ->
                    nova == REVISAO_MAPA_COM_SUGESTOES || nova == REVISAO_MAPA_VALIDADO || nova == REVISAO_MAPA_AJUSTADO;
            case REVISAO_MAPA_COM_SUGESTOES ->
                    nova == REVISAO_MAPA_DISPONIBILIZADO || nova == REVISAO_MAPA_AJUSTADO;
            case REVISAO_MAPA_VALIDADO ->
                    nova == REVISAO_MAPA_HOMOLOGADO || nova == REVISAO_MAPA_DISPONIBILIZADO;
            default -> false;
        };
    }

    private boolean transicaoDiagnostico(SituacaoSubprocesso nova) {
        return switch (this) {
            case DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO ->
                    nova == DIAGNOSTICO_MONITORAMENTO;
            case DIAGNOSTICO_MONITORAMENTO ->
                    nova == DIAGNOSTICO_CONCLUIDO;
            default -> false;
        };
    }
}

package sgc.alerta;

import sgc.subprocesso.model.*;

import java.util.*;

public final class AssuntosNotificacao {
    private AssuntosNotificacao() {
    }

    public static String inicioProcesso(String tipoProcesso, boolean participante) {
        String descricaoTipoProcesso = switch (tipoProcesso) {
            case "MAPEAMENTO" -> "mapeamento de competências";
            case "REVISAO" -> "revisão do mapa de competências";
            case "DIAGNOSTICO" -> "diagnóstico";
            default -> tipoProcesso.toLowerCase(Locale.ROOT);
        };
        return participante
                ? "SGC: Início de processo de %s".formatted(descricaoTipoProcesso)
                : "SGC: Início de processo de %s em unidades subordinadas".formatted(descricaoTipoProcesso);
    }

    public static String processoFinalizado(String nomeProcesso) {
        return "SGC: Finalização do processo %s".formatted(nomeProcesso);
    }

    public static String processoFinalizadoUnidadesSubordinadas(String nomeProcesso) {
        return "%s em unidades subordinadas".formatted(processoFinalizado(nomeProcesso));
    }

    public static String lembretePrazo(String nomeProcesso) {
        return "SGC: Lembrete de prazo - %s".formatted(nomeProcesso);
    }

    public static String atribuicaoPerfilChefe(String siglaUnidade) {
        return "SGC: Atribuição de perfil CHEFE na unidade %s".formatted(siglaUnidade);
    }

    public static String subprocesso(TipoTransicao tipo, String siglaUnidade, boolean paraSuperior) {
        String base = switch (tipo) {
            case CADASTRO_ACEITO -> "Cadastro de atividades e conhecimentos da %s submetido para análise"
                    .formatted(siglaUnidade);
            case CADASTRO_DEVOLVIDO -> "Cadastro de atividades e conhecimentos da %s devolvido para ajustes"
                    .formatted(siglaUnidade);
            case CADASTRO_HOMOLOGADO -> "Cadastro de atividades homologado";
            case CADASTRO_DISPONIBILIZADO -> "Cadastro de atividades e conhecimentos disponibilizado";
            case CADASTRO_REABERTO -> "Reabertura de cadastro de atividades";
            case MAPA_HOMOLOGADO -> "Mapa de competências homologado";
            case MAPA_DISPONIBILIZADO -> "Mapa de competências disponibilizado";
            case MAPA_SUGESTOES_APRESENTADAS -> "Sugestões apresentadas para o mapa de competências da %s"
                    .formatted(siglaUnidade);
            case MAPA_VALIDADO -> "Validação do mapa de competências da %s submetida para análise"
                    .formatted(siglaUnidade);
            case MAPA_VALIDACAO_DEVOLVIDA -> "Validação do mapa da %s devolvida para ajustes"
                    .formatted(siglaUnidade);
            case MAPA_VALIDACAO_ACEITA -> "Validação do mapa de competências da %s submetida para análise"
                    .formatted(siglaUnidade);
            case REVISAO_CADASTRO_ACEITA -> "Revisão do cadastro de atividades e conhecimentos da %s submetido para análise"
                    .formatted(siglaUnidade);
            case REVISAO_CADASTRO_DEVOLVIDA -> "Revisão do cadastro de atividades e conhecimentos da %s devolvida para ajustes"
                    .formatted(siglaUnidade);
            case REVISAO_CADASTRO_DISPONIBILIZADA -> "Revisão do cadastro de atividades e conhecimentos disponibilizada: %s"
                    .formatted(siglaUnidade);
            case REVISAO_CADASTRO_REABERTA -> "Reabertura de revisão de cadastro";
            default -> tipo.getDescMovimentacao();
        };

        boolean incluirSigla = paraSuperior
                || tipo == TipoTransicao.CADASTRO_DISPONIBILIZADO
                || tipo == TipoTransicao.CADASTRO_REABERTO
                || tipo == TipoTransicao.REVISAO_CADASTRO_REABERTA;

        return incluirSigla
                ? "SGC: %s - %s".formatted(base, siglaUnidade)
                : "SGC: %s".formatted(base);
    }

    public static String cadastroAceitoBloco(boolean revisao) {
        return revisao
                ? "SGC: Revisões de cadastro de atividades e conhecimentos submetidas para análise"
                : "SGC: Cadastros de atividades e conhecimentos submetidos para análise";
    }

    public static String disponibilizacaoMapaBloco() {
        return "SGC: Mapas de competências disponibilizados";
    }

    public static String aceiteValidacaoBlocoDireto(String siglaUnidade) {
        return "SGC: Validação do mapa de competências da %s submetida para análise".formatted(siglaUnidade);
    }

    public static String aceiteValidacaoBlocoSuperior() {
        return "SGC: Validação de mapas de competências submetida para análise";
    }
}

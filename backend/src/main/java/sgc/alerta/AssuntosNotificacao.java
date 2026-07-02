package sgc.alerta;

import sgc.processo.model.*;
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

    public static String processoFinalizado(TipoProcesso tipoProcesso) {
        return "SGC: Finalização de processo de %s".formatted(descricaoTipoProcessoFinalizacao(tipoProcesso));
    }

    public static String processoFinalizadoUnidadesSubordinadas(TipoProcesso tipoProcesso) {
        return "%s em unidades subordinadas".formatted(processoFinalizado(tipoProcesso));
    }

    public static String lembretePrazo(String nomeProcesso) {
        return "SGC: Lembrete de prazo - %s".formatted(nomeProcesso);
    }

    public static final String dataLimiteAlterada = "SGC: Data limite alterada";
    public static final String ACEITE_VALIDACAO_BLOCO_SUPERIOR = "SGC: Validação de mapas de competências submetida para análise";
    public static final String DIAGNOSTICO_CONSENSO_DISPONIVEL = "SGC: Avaliação de consenso criada";
    public static final String DIAGNOSTICOS_ACEITOS_EM_BLOCO = "SGC: Diagnósticos submetidos para análise";

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
            case MAPA_SUGESTOES_APRESENTADAS ->
                    "Sugestões apresentadas para o mapa de competências da %s".formatted(siglaUnidade);
            case MAPA_VALIDADO, MAPA_VALIDACAO_ACEITA ->
                    "Validação do mapa de competências da %s submetida para análise".formatted(siglaUnidade);
            case MAPA_VALIDACAO_DEVOLVIDA -> "Validação do mapa da %s devolvida para ajustes"
                    .formatted(siglaUnidade);
            case REVISAO_CADASTRO_ACEITA ->
                    "Revisão do cadastro de atividades e conhecimentos da %s submetido para análise".formatted(siglaUnidade);
            case REVISAO_CADASTRO_DEVOLVIDA ->
                    "Revisão do cadastro de atividades e conhecimentos da %s devolvida para ajustes".formatted(siglaUnidade);
            case REVISAO_CADASTRO_DISPONIBILIZADA ->
                    "Revisão do cadastro de atividades e conhecimentos disponibilizada: %s".formatted(siglaUnidade);
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

    public final static String disponibilizacaoMapaBloco = "SGC: Mapas de competências disponibilizados";

    private static String descricaoTipoProcessoFinalizacao(TipoProcesso tipoProcesso) {
        return switch (tipoProcesso) {
            case MAPEAMENTO -> "mapeamento";
            case REVISAO -> "revisão";
            case DIAGNOSTICO -> "diagnóstico";
        };
    }

    public static String aceiteValidacaoBlocoDireto(String siglaUnidade) {
        return "SGC: Validação do mapa de competências da %s submetida para análise".formatted(siglaUnidade);
    }

    public static String diagnosticoAutoavaliacaoConcluida(String nomeServidor) {
        return "SGC: Autoavaliação concluída: %s".formatted(nomeServidor);
    }

    public static String diagnosticoConsensoAprovado(String nomeServidor) {
        return "SGC: Avaliação de consenso aprovada: %s".formatted(nomeServidor);
    }

    public static String diagnosticoConcluido(String siglaUnidade) {
        return "SGC: Diagnóstico da unidade %s submetido para análise".formatted(siglaUnidade);
    }

    public static String diagnosticoDevolvido(String siglaUnidade) {
        return "SGC: Diagnóstico da unidade %s devolvido para ajustes".formatted(siglaUnidade);
    }

    public static String diagnosticoAceito(String siglaUnidade) {
        return "SGC: Diagnóstico da unidade %s aceito".formatted(siglaUnidade);
    }

    public static String diagnosticoHomologado(String siglaUnidade) {
        return "SGC: Diagnóstico da unidade %s homologado".formatted(siglaUnidade);
    }
}

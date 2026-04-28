package sgc.subprocesso.dto;

/**
 * DTO para retorno do texto de sugestões de um subprocesso.
 *
 * @param sugestoes texto com as sugestões ou string vazia quando não houver.
 */
public record SugestoesDto(String sugestoes) {

    public static SugestoesDto vazia() {
        return new SugestoesDto("");
    }

    public static SugestoesDto de(String sugestoes) {
        return new SugestoesDto(sugestoes);
    }
}

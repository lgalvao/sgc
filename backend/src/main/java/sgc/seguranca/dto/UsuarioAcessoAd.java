package sgc.seguranca.dto;

import lombok.*;

/**
 * DTO para representar um usuário retornado pelo serviço AcessoAD.
 *
 * <p>Usa @Setter pois é deserializado pelo Jackson via setters.
 * Caso excepcional onde mutabilidade é necessária para integração externa.
 */
@Getter
@Setter
@SuppressWarnings("NullAway.Init")
public class UsuarioAcessoAd {
    private String login;
    private String nome;
    private LotacaoAd lotacao;
    private Integer nivel;
    private String nomeNivel;
    private String tipo;
    private String email;

    @Getter
    @Setter
    @SuppressWarnings("NullAway.Init")
    public static class LotacaoAd {
        private Integer codigo;
        private String sigla;
        private String nome;
    }
}

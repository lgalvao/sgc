package sgc.seguranca.login.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para representar um usuário retornado pelo serviço AcessoAD.
 *
 * <p>Usa @Setter pois é deserializado pelo Jackson via setters.
 * Caso excepcional onde mutabilidade é necessária para integração externa.
 */
@Getter
@Setter
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
    public static class LotacaoAd {
        private Integer codigo;
        private String sigla;
        private String nome;
    }
}

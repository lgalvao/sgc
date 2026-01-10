package sgc.seguranca.login.dto;

import lombok.Data;

/**
 * DTO para representar um usuário retornado pelo serviço AcessoAD.
 */
@Data
public class UsuarioAcessoAd {
    private String login;
    private String nome;
    private LotacaoAd lotacao;
    private Integer nivel;
    private String nomeNivel;
    private String tipo;
    private String email;

    @Data
    public static class LotacaoAd {
        private Integer codigo;
        private String sigla;
        private String nome;
    }
}

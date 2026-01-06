package sgc.seguranca.erros.autenticacao;

import lombok.Data;

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

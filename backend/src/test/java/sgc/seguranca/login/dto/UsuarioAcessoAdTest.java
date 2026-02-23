package sgc.seguranca.login.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.seguranca.dto.UsuarioAcessoAd;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Cobertura para UsuarioAcessoAd")
class UsuarioAcessoAdTest {

    @Test
    @DisplayName("Deve permitir getters e setters")
    void devePermitirGettersESetters() {
        UsuarioAcessoAd ad = new UsuarioAcessoAd();
        ad.setLogin("login");
        ad.setNome("nome");
        ad.setEmail("email");
        ad.setNivel(1);
        ad.setNomeNivel("nivel");
        ad.setTipo("tipo");

        UsuarioAcessoAd.LotacaoAd lot = new UsuarioAcessoAd.LotacaoAd();
        lot.setCodigo(100);
        lot.setSigla("SIGLA");
        lot.setNome("Nome Lotacao");

        ad.setLotacao(lot);

        assertThat(ad.getLogin()).isEqualTo("login");
        assertThat(ad.getNome()).isEqualTo("nome");
        assertThat(ad.getEmail()).isEqualTo("email");
        assertThat(ad.getNivel()).isEqualTo(1);
        assertThat(ad.getNomeNivel()).isEqualTo("nivel");
        assertThat(ad.getTipo()).isEqualTo("tipo");
        assertThat(ad.getLotacao()).isSameAs(lot);

        assertThat(lot.getCodigo()).isEqualTo(100);
        assertThat(lot.getSigla()).isEqualTo("SIGLA");
        assertThat(lot.getNome()).isEqualTo("Nome Lotacao");
    }
}

package sgc.organizacao.model;

import org.junit.jupiter.api.*;
import org.springframework.security.core.authority.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Usuario")
class UsuarioTest {

    @Test
    @DisplayName("deve expor unidade ativa e dados de autenticacao")
    void deveExporUnidadeAtivaEDadosDeAutenticacao() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(7L);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setUnidadeLotacao(unidade);
        usuario.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(usuario.getUnidadeCodigo()).isEqualTo(7L);
        assertThat(usuario.getUsername()).isEqualTo("123");
        assertThat(usuario.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(usuario.getPassword()).isNull();
    }

    @Test
    @DisplayName("deve usar titulo eleitoral como identidade")
    void deveUsarTituloEleitoralComoIdentidade() {
        Usuario primeiro = new Usuario();
        primeiro.setTituloEleitoral("123");

        Usuario segundo = new Usuario();
        segundo.setTituloEleitoral("123");

        Usuario terceiro = new Usuario();
        terceiro.setTituloEleitoral("999");

        assertThat(primeiro).isEqualTo(segundo);
        assertThat(primeiro).isNotEqualTo(terceiro);
        assertThat(primeiro.hashCode()).isEqualTo(segundo.hashCode());
        assertThat(new Usuario().getAuthorities()).isEmpty();
    }
}

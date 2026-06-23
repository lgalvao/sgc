package sgc.seguranca.login;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListaNegraJwt")
class ListaNegraJwtTest {

    private final ListaNegraJwt listaNegraJwt = new ListaNegraJwt();

    @Test
    @DisplayName("revogar deve ignorar entradas inválidas e registrar token válido")
    void revogarDeveIgnorarEntradasInvalidasERegistrarTokenValido() {
        Instant expiracao = Instant.now().plusSeconds(120);

        listaNegraJwt.revogar(null, expiracao);
        listaNegraJwt.revogar("   ", expiracao);
        listaNegraJwt.revogar("token-sem-expiracao", null);
        listaNegraJwt.revogar("token-valido", expiracao);

        assertThat(listaNegraJwt.estaRevogado(null)).isFalse();
        assertThat(listaNegraJwt.estaRevogado("   ")).isFalse();
        assertThat(listaNegraJwt.estaRevogado("token-sem-expiracao")).isFalse();
        assertThat(listaNegraJwt.estaRevogado("token-valido")).isTrue();
    }

    @Test
    @DisplayName("estaRevogado deve remover token expirado e retornar falso para token inexistente")
    void estaRevogadoDeveRemoverTokenExpiradoERetornarFalsoParaTokenInexistente() {
        listaNegraJwt.revogar("token-expirado", Instant.now().minusSeconds(1));

        assertThat(listaNegraJwt.estaRevogado("inexistente")).isFalse();
        assertThat(listaNegraJwt.estaRevogado("token-expirado")).isFalse();
        assertThat(listaNegraJwt.estaRevogado("token-expirado")).isFalse();
    }

    @Test
    @DisplayName("limpar deve remover apenas tokens expirados")
    void limparDeveRemoverApenasTokensExpirados() {
        listaNegraJwt.revogar("expirado", Instant.now().minusSeconds(5));
        listaNegraJwt.revogar("no-limite", Instant.now());
        listaNegraJwt.revogar("valido", Instant.now().plusSeconds(300));

        listaNegraJwt.limpar();

        assertThat(listaNegraJwt.estaRevogado("expirado")).isFalse();
        assertThat(listaNegraJwt.estaRevogado("no-limite")).isFalse();
        assertThat(listaNegraJwt.estaRevogado("valido")).isTrue();
    }
}

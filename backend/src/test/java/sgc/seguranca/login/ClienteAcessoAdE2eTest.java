package sgc.seguranca.login;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ClienteAcessoAdE2e")
class ClienteAcessoAdE2eTest {

    @Test
    @DisplayName("deve autenticar com cliente nulo sem lancar erro")
    void deveAutenticarComClienteNuloSemLancarErro() {
        ClienteAcessoAdE2e cliente = new ClienteAcessoAdE2e(null);

        assertThatCode(() -> cliente.autenticar("123456789012", "senha"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve mascarar valores curtos e longos")
    void deveMascararValoresCurtosELongos() throws Exception {
        ClienteAcessoAdE2e cliente = new ClienteAcessoAdE2e(null);
        Method metodo = ClienteAcessoAdE2e.class.getDeclaredMethod("mascarar", String.class);
        metodo.setAccessible(true);

        assertThat((String) metodo.invoke(cliente, "1234")).isEqualTo("***");
        assertThat((String) metodo.invoke(cliente, "123456")).isEqualTo("***3456");
    }
}

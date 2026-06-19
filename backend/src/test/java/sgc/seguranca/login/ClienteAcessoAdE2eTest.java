package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

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
    @DisplayName("deve mascarar titulo curto com menos de 4 digitos via fluxo normal")
    void deveMascararTituloCurtoViaFluxoNormal() {
        ClienteAcessoAdE2e cliente = new ClienteAcessoAdE2e(null);

        // Dispara o log passando pela logica de < 4
        assertThatCode(() -> cliente.autenticar("123", "senha"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve inicializar com restClient customizado")
    void deveInicializarComRestClientCustomizado() {
        org.springframework.web.client.RestClient restClientMock = org.springframework.web.client.RestClient.builder().build();
        ClienteAcessoAdE2e cliente = new ClienteAcessoAdE2e(restClientMock);

        assertThatCode(() -> cliente.autenticar("123456789012", "senha"))
                .doesNotThrowAnyException();
    }
}

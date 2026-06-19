package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import sgc.comum.erros.ErroAutenticacao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteAcessoAdTest {

    @InjectMocks
    private ClienteAcessoAd clienteAcessoAd;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    @DisplayName("autenticar - Sucesso")
    void autenticar_Sucesso() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("OK");

        clienteAcessoAd.autenticar("123", "senha");
        verify(responseSpec).body(String.class);
    }

    @Test
    @DisplayName("autenticar - Exception inesperada")
    void autenticar_ExceptionInesperada() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("Erro inesperado"));

        assertThatThrownBy(() -> clienteAcessoAd.autenticar("123", "senha"))
                .isInstanceOf(ErroAutenticacao.class);
    }

    @Test
    @DisplayName("autenticar - ErroAutenticacao Direto")
    void autenticar_ErroAutenticacaoDireto() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenThrow(new ErroAutenticacao("Falha simulada"));

        assertThatThrownBy(() -> clienteAcessoAd.autenticar("123", "senha"))
                .isInstanceOf(ErroAutenticacao.class)
                .hasMessage("Falha simulada");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("autenticar - Valida logica de onStatus")
    void autenticar_ValidaOnStatus() throws IOException {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        ArgumentCaptor<Predicate<HttpStatusCode>> predicateCaptor = ArgumentCaptor.forClass(Predicate.class);
        ArgumentCaptor<RestClient.ResponseSpec.ErrorHandler> handlerCaptor = ArgumentCaptor.forClass(RestClient.ResponseSpec.ErrorHandler.class);

        when(responseSpec.onStatus(predicateCaptor.capture(), handlerCaptor.capture())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("OK");

        clienteAcessoAd.autenticar("123", "senha");

        assertThat(predicateCaptor.getValue().test(HttpStatusCode.valueOf(400))).isTrue();
        assertThat(predicateCaptor.getValue().test(HttpStatusCode.valueOf(500))).isTrue();
        assertThat(predicateCaptor.getValue().test(HttpStatusCode.valueOf(200))).isFalse();

        HttpRequest request = mock(HttpRequest.class);
        try (ClientHttpResponse response = mock(ClientHttpResponse.class)) {
            when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
            when(response.getBody()).thenReturn(new ByteArrayInputStream("Erro detalhado".getBytes(StandardCharsets.UTF_8)));

            var errorHandler = handlerCaptor.getValue();
            assertThatThrownBy(() -> errorHandler.handle(request, response))
                    .isInstanceOf(ErroAutenticacao.class)
                    .hasMessageContaining("Falha na autenticação externa.");
        }
    }
}

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        boolean resultado = clienteAcessoAd.autenticar("123", "senha");
        assertTrue(resultado);
    }

    @Test
    @DisplayName("autenticar - Exception Inesperada")
    void autenticar_ExceptionInesperada() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("Erro inesperado"));

        var exception = assertThrows(ErroAutenticacao.class, () -> clienteAcessoAd.autenticar("123", "senha"));
        assertNotNull(exception);
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

        var exception = assertThrows(ErroAutenticacao.class, () -> clienteAcessoAd.autenticar("123", "senha"));
        assertNotNull(exception);
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

        // Valida Predicate
        assertTrue(predicateCaptor.getValue().test(HttpStatusCode.valueOf(400)));
        assertTrue(predicateCaptor.getValue().test(HttpStatusCode.valueOf(500)));
        // Valida que 2xx retorna false
        assertTrue(!predicateCaptor.getValue().test(HttpStatusCode.valueOf(200)));

        // Valida ErrorHandler
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
        when(response.getBody()).thenReturn(new ByteArrayInputStream("Erro detalhado".getBytes(StandardCharsets.UTF_8)));

        var errorHandler = handlerCaptor.getValue();
        var exception = assertThrows(ErroAutenticacao.class, () ->
            errorHandler.handle(request, response)
        );
        assertNotNull(exception);
    }
}

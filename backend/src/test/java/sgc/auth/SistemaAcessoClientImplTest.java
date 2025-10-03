package sgc.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import sgc.auth.exceptions.ExternalServiceException;
import sgc.auth.exceptions.InvalidCredentialsException;
import sgc.dto.PerfilUnidadeDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class SistemaAcessoClientImplTest {
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private SistemaAcessoClientImpl client;

    @BeforeEach
    void setup() {
        // RestTemplate que será interceptado pelo MockRestServiceServer
        this.restTemplate = new RestTemplate();
        this.server = MockRestServiceServer.createServer(this.restTemplate);

        // RestTemplateBuilder que retorna a instância acima quando build() for chamado
        RestTemplateBuilder builder = new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };

        SistemaAcessoProperties props = new SistemaAcessoProperties();
        props.setUrl("http://localhost:8080");
        props.setApiKey("test-key");
        props.setMaxRetries(2);
        props.setBaseBackoffMs(1);
        props.setMaxBackoffMs(10);
        props.setConnectTimeoutMs(1000);
        props.setReadTimeoutMs(1000);

        ObjectMapper objectMapper = new ObjectMapper();

        this.client = new SistemaAcessoClientImpl(builder, props, objectMapper);
    }

    @Test
    void authenticate_success_returns_true() {
        server.expect(requestTo("http://localhost:8080/api/authenticate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"authenticated\":true}", MediaType.APPLICATION_JSON));

        boolean ok = client.authenticate("user", "pass");
        assertTrue(ok);
        server.verify();
    }

    @Test
    void authenticate_unauthorized_throws_invalid_credentials() {
        server.expect(requestTo("http://localhost:8080/api/authenticate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(InvalidCredentialsException.class, () -> client.authenticate("user", "bad"));
        server.verify();
    }

    @Test
    void authenticate_server_error_retries_and_fails_with_external_exception() {
        // duas respostas 500 (props.maxRetries = 2) -> deve estourar ExternalServiceException
        server.expect(requestTo("http://localhost:8080/api/authenticate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        server.expect(requestTo("http://localhost:8080/api/authenticate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThrows(ExternalServiceException.class, () -> client.authenticate("user", "pass"));
        server.verify();
    }

    @Test
    void fetchPerfis_success_parses_list() {
        String json = "[{\"perfil\":\"CHEFE\",\"unidadeCodigo\":1,\"sigla\":\"SESEL\"}]";
        server.expect(requestTo("http://localhost:8080/api/users/myuser/perfis"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<PerfilUnidadeDTO> perfis = client.fetchPerfis("myuser");
        assertNotNull(perfis);
        assertEquals(1, perfis.size());
        assertEquals("CHEFE", perfis.getFirst().getPerfil());
        server.verify();
    }

    @Test
    void fetchPerfis_5xx_retries_then_fails() {
        server.expect(requestTo("http://localhost:8080/api/users/foo/perfis"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());
        server.expect(requestTo("http://localhost:8080/api/users/foo/perfis"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(ExternalServiceException.class, () -> client.fetchPerfis("foo"));
        server.verify();
    }
}
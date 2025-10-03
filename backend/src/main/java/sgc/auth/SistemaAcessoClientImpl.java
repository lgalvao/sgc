package sgc.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import sgc.auth.exceptions.ExternalServiceException;
import sgc.auth.exceptions.InvalidCredentialsException;
import sgc.dto.PerfilUnidadeDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementação HTTP do client para o Sistema Acesso.
 * Esta implementação é usada por padrão (todos os profiles exceto "test").
 * <p>
 * Observações:
 * - URL e credenciais são lidas de SistemaAcessoProperties.
 * - Retentativas simples com backoff aplicadas em caso de 5xx.
 * - Não logar valores sensíveis (apiKey / senha).
 */
@Service
@Profile("!test")
public class SistemaAcessoClientImpl implements SistemaAcessoClient {
    private final RestTemplate restTemplate;
    private final SistemaAcessoProperties props;
    private final ObjectMapper objectMapper;

    public SistemaAcessoClientImpl(RestTemplateBuilder builder,
                                   SistemaAcessoProperties props,
                                   ObjectMapper objectMapper) {
        // Build RestTemplate from builder first to allow tests to provide an anonymous builder
        // that overrides build() and returns a preconfigured RestTemplate (used by unit tests).
        this.restTemplate = builder.build();
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean authenticate(String titulo, String senha) {
        if (titulo == null || senha == null) {
            return false;
        }

        String url = props.getUrl();
        String endpoint = url.endsWith("/") ? url + "api/authenticate" : url + "/api/authenticate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            headers.set("X-API-KEY", props.getApiKey());
        }

        Map<String, String> body = Map.of("titulo", titulo, "senha", senha);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        int attempts = 0;
        while (true) {
            attempts++;
            try {
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // espera-se resposta JSON como {"authenticated": true} ou similar
                    Map<String, Object> map = objectMapper.readValue(response.getBody(), new TypeReference<>() {
                    });
                    Object auth = map.get("authenticated");
                    if (auth instanceof Boolean) {
                        return (Boolean) auth;
                    }
                    // fallback: se 200, considerar autenticado quando campo ausente (compatibilidade)
                    return true;
                } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new InvalidCredentialsException("Título ou senha inválidos");
                } else if (response.getStatusCode().is4xxClientError()) {
                    throw new ExternalServiceException("Erro de cliente ao autenticar: " + response.getStatusCode());
                } else if (response.getStatusCode().is5xxServerError()) {
                    // tratar no catch para retry
                    throw new ExternalServiceException("Erro de servidor (" + response.getStatusCode() + ")");
                } else {
                    return false;
                }
            } catch (HttpStatusCodeException ex) {
                int statusCode = ex.getRawStatusCode();
                if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                    throw new InvalidCredentialsException("Título ou senha inválidos");
                } else if (statusCode >= 500 && statusCode < 600) {
                    if (attempts >= props.getMaxRetries()) {
                        throw new ExternalServiceException("Máximo de tentativas atingido ao autenticar: " + statusCode, ex);
                    }
                    backoffSleep(attempts);
                } else {
                    throw new ExternalServiceException("Erro ao comunicar com Sistema Acesso: " + statusCode, ex);
                }
            } catch (InvalidCredentialsException | ExternalServiceException e) {
                throw e;
            } catch (Exception e) {
                // Erro genérico (parsing, IO, etc.) -> tratar como external
                if (attempts >= props.getMaxRetries()) {
                    throw new ExternalServiceException("Falha ao autenticar no Sistema Acesso", e);
                }
                backoffSleep(attempts);
            }
        }
    }

    @Override
    public List<PerfilUnidadeDTO> fetchPerfis(String titulo) {
        if (titulo == null) {
            return Collections.emptyList();
        }

        String url = props.getUrl();
        String endpoint = url.endsWith("/") ? url + "api/users/" + titulo + "/perfis" : url + "/api/users/" + titulo + "/perfis";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            headers.set("X-API-KEY", props.getApiKey());
        }

        HttpEntity<Void> request = new HttpEntity<>(headers);

        int attempts = 0;
        while (true) {
            attempts++;
            try {
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                    });
                } else if (response.getStatusCode().is4xxClientError()) {
                    throw new ExternalServiceException("Erro de cliente ao recuperar perfis: " + response.getStatusCode());
                } else if (response.getStatusCode().is5xxServerError()) {
                    throw new ExternalServiceException("Erro de servidor ao recuperar perfis: " + response.getStatusCode());
                } else {
                    return Collections.emptyList();
                }
            } catch (HttpStatusCodeException ex) {
                int statusCode = ex.getRawStatusCode();
                if (statusCode >= 500 && statusCode < 600) {
                    if (attempts >= props.getMaxRetries()) {
                        throw new ExternalServiceException("Máximo de tentativas atingido ao buscar perfis: " + statusCode, ex);
                    }
                    backoffSleep(attempts);
                } else {
                    throw new ExternalServiceException("Erro ao comunicar com Sistema Acesso: " + statusCode, ex);
                }
            } catch (Exception e) {
                if (attempts >= props.getMaxRetries()) {
                    throw new ExternalServiceException("Falha ao buscar perfis no Sistema Acesso", e);
                }
                backoffSleep(attempts);
            }
        }
    }

    private void backoffSleep(int attempt) {
        try {
            long delay = Math.min(props.getBaseBackoffMs() * (1L << (attempt - 1)), props.getMaxBackoffMs());
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
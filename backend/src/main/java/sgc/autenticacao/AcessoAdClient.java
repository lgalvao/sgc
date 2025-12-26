package sgc.autenticacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sgc.comum.erros.ErroAutenticacao;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test & !e2e")
public class AcessoAdClient {
    private final RestClient acessoAdRestClient;

    @SuppressWarnings("SameReturnValue")
    public boolean autenticar(String titulo, String senha) {
        try {
            AutenticarRequest request = new AutenticarRequest(titulo, senha);
            
            log.debug("Autenticando usuário {} na API do AD", titulo);
            
            String response = acessoAdRestClient.post()
                    .uri("/auth/autenticar")
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                        (req, res) -> {
                            String body = new String(res.getBody().readAllBytes());
                            log.error("Erro HTTP {}: {}", res.getStatusCode(), body);
                            throw new ErroAutenticacao("Erro na autenticação: " + body);
                        })
                    .body(String.class);

            log.info("Usuário {} autenticado com sucesso no AD. Resposta: {}", titulo, response);
            return true;
        } catch (ErroAutenticacao e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao autenticar usuário {} no AD: {}", titulo, e.getMessage(), e);
            throw new ErroAutenticacao("Falha na autenticação: " + e.getMessage());
        }
    }

    private record AutenticarRequest(String titulo, String senha) {}
}

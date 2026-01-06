package sgc.seguranca.autenticacao;

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
            
            acessoAdRestClient.post()
                    .uri("/auth/autenticar")
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                        (req, res) -> {
                            // SENTINEL: Ler o corpo apenas para log interno, nunca propagar para o cliente
                            String body = new String(res.getBody().readAllBytes());
                            log.error("Erro HTTP {} na autenticação AD: {}", res.getStatusCode(), body);
                            // Mensagem genérica para o usuário final para evitar vazamento de dados internos
                            throw new ErroAutenticacao("Falha na autenticação externa.");
                        })
                    .body(String.class);

            // SENTINEL: Log apenas do sucesso, sem expor o corpo da resposta que pode conter dados sensíveis
            log.info("Usuário {} autenticado com sucesso no AD.", titulo);
            return true;
        } catch (ErroAutenticacao e) {
            throw e;
        } catch (Exception e) {
            // Log detalhado para admin/dev
            log.error("Erro ao autenticar usuário {} no AD: {}", titulo, e.getMessage(), e);
            // Mensagem genérica para o usuário final
            throw new ErroAutenticacao("Ocorreu um erro inesperado durante a autenticação.");
        }
    }

    private record AutenticarRequest(String titulo, String senha) {}
}

package sgc.seguranca.login;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;
import sgc.comum.erros.*;

import java.nio.charset.*;

/**
 * Cliente para integração com o serviço AcessoAD (autenticação via AD)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test & !e2e")
public class ClienteAcessoAd {
    private final RestClient acessoAdRestClient;

    /**
     * Autentica um usuário no serviço AcessoAD.
     */
    public boolean autenticar(String titulo, String senha) {
        try {
            AutenticarRequest request = new AutenticarRequest(titulo, senha);
            acessoAdRestClient.post()
                    .uri("/auth/autenticar")
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, res) -> {
                                String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                log.error("Erro HTTP {} na autenticação AD: {}", res.getStatusCode(), body);
                                throw new ErroAutenticacao("Falha na autenticação externa.");
                            })
                    .body(String.class);

            log.info("Usuário {} autenticado no AD.", titulo);
            return true;
        } catch (ErroAutenticacao e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao autenticar usuário {} no AD: {}", titulo, e.getMessage(), e);
            throw new ErroAutenticacao("Ocorreu um erro inesperado durante a autenticação.");
        }
    }

    private record AutenticarRequest(String titulo, String senha) {
    }
}

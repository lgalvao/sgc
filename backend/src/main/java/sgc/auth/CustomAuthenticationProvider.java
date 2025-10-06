package sgc.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import sgc.auth.dto.LoginAcesso;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

/**
 * Provider customizado de autenticação que integra com o serviço AD do TRE-PE.
 * Responsável por validar credenciais dos usuários através do sistema Acesso.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Value("${aplicacao.ambiente-testes:true}")
    private boolean ambienteTestes;

    @Value("${aplicacao.url-acesso-hom}")
    private String urlAcessoHom;

    @Value("${aplicacao.url-acesso-prod}")
    private String urlAcessoProd;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Autentica o usuário através do serviço AD.
     *
     * @param authentication Objeto contendo credenciais do usuário
     * @return Authentication autenticado se credenciais válidas
     * @throws AuthenticationException se credenciais inválidas ou erro de comunicação
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String titulo = authentication.getName();
        String senha = authentication.getCredentials().toString();

        String urlAcesso = ambienteTestes ? urlAcessoHom : urlAcessoProd;
        String urlCompleta = urlAcesso + "/autenticar";

        log.debug("Tentando autenticar usuário: {} no ambiente: {}", titulo, ambienteTestes ? "TESTES" : "PRODUÇÃO");

        try {
            LoginAcesso login = new LoginAcesso(titulo, senha, !ambienteTestes);
            String json = objectMapper.writeValueAsString(login);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlCompleta))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = cliente.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Usuário autenticado com sucesso: {}", titulo);
                return new UsernamePasswordAuthenticationToken(
                        titulo,
                        senha,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            } else {
                log.warn("Falha na autenticação para usuário: {}. Status: {}", titulo, response.statusCode());
                throw new BadCredentialsException("Usuário ou senha inválidos");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Erro de comunicação ao tentar autenticar o usuário: {}", titulo, e);
            throw new BadCredentialsException("Erro de comunicação com o serviço de autenticação");
        }
    }

    /**
     * Indica que este provider suporta autenticação via UsernamePasswordAuthenticationToken.
     *
     * @param authentication Tipo de autenticação
     * @return true se suporta UsernamePasswordAuthenticationToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
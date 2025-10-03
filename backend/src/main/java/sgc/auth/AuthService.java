package sgc.auth;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sgc.auth.events.LoginSuccessEvent;
import sgc.dto.LoginRequest;
import sgc.dto.LoginResponse;
import sgc.dto.PerfilUnidadeDTO;

import java.util.List;

/**
 * Serviço responsável por autenticar credenciais via SistemaAcessoClient,
 * gerar token via TokenService e publicar evento de sucesso de login.
 */
@Service
public class AuthService {
    private final SistemaAcessoClient sistemaAcessoClient;
    private final TokenService tokenService;
    private final ApplicationEventPublisher publisher;

    public AuthService(SistemaAcessoClient sistemaAcessoClient,
                       TokenService tokenService,
                       ApplicationEventPublisher publisher) {

        this.sistemaAcessoClient = sistemaAcessoClient;
        this.tokenService = tokenService;
        this.publisher = publisher;
    }

    /**
     * Autentica o usuário e retorna LoginResponse com token e perfis/unidades.
     * Em caso de credenciais inválidas lança 401.
     */
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getTitulo() == null || request.getSenha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requisição inválida");
        }

        boolean ok = sistemaAcessoClient.authenticate(request.getTitulo(), request.getSenha());
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Título ou senha inválidos.");
        }

        List<PerfilUnidadeDTO> perfis = sistemaAcessoClient.fetchPerfis(request.getTitulo());
        String token = tokenService.generateToken(request.getTitulo());

        // Publica evento de login bem-sucedido para auditoria/processing assíncrono
        publisher.publishEvent(new LoginSuccessEvent(this, request.getTitulo(), perfis));

        // Retorna perfis e unidades (simplificação: unidades = perfis para contrato inicial)
        return new LoginResponse(token, perfis, perfis);
    }
}

/**
 * Serviço simples de geração de token (stub).
 * Implementação padrão está disponível como bean e pode ser substituída em testes.
 */
interface TokenService {
    String generateToken(String titulo);
}

@Service
class TokenServiceImpl implements TokenService {
    @Override
    public String generateToken(String titulo) {
        // Geração simples de token para uso inicial; substituir por JWT real mais tarde.
        return "token:%s:%d".formatted(titulo == null ? "anonymous" : titulo, System.currentTimeMillis());
    }
}
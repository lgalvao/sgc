package sgc.seguranca.login;

import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.concurrent.*;

/**
 * Blacklist in-memory de tokens JWT revogados.
 * Mantém o JTI (JWT ID) de tokens invalidados por logout até que expirem naturalmente.
 */
@Component
@Slf4j
public class BlacklistJwt {
    private final ConcurrentHashMap<String, Instant> tokensBloqueados = new ConcurrentHashMap<>();

    public void revogar(String jti, Instant expiracao) {
        tokensBloqueados.put(jti, expiracao);
        log.debug("Token JTI revogado: {}", jti);
        limparExpirados();
    }

    public boolean estaRevogado(String jti) {
        Instant expiracao = tokensBloqueados.get(jti);
        if (expiracao == null) {
            return false;
        }
        if (Instant.now().isAfter(expiracao)) {
            tokensBloqueados.remove(jti);
            return false;
        }
        return true;
    }

    private void limparExpirados() {
        Instant agora = Instant.now();
        tokensBloqueados.entrySet().removeIf(entrada -> agora.isAfter(entrada.getValue()));
    }
}

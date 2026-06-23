package sgc.seguranca.login;

import org.jspecify.annotations.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.concurrent.*;

@Component
public class ListaNegraJwt {
    private final ConcurrentHashMap<String, Instant> tokensRevogados = new ConcurrentHashMap<>();

    public void revogar(@Nullable String jti, Instant expiracao) {
        if (jti == null || jti.isBlank() || expiracao == null) {
            return;
        }
        tokensRevogados.put(jti, expiracao);
    }

    public boolean estaRevogado(@Nullable String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Instant expiracao = tokensRevogados.get(jti);
        if (expiracao == null) {
            return false;
        }
        if (!expiracao.isAfter(Instant.now())) {
            tokensRevogados.remove(jti, expiracao);
            return false;
        }
        return true;
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void limpar() {
        Instant agora = Instant.now();
        tokensRevogados.entrySet().removeIf(entry -> !entry.getValue().isAfter(agora));
    }
}

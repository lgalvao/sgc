package sgc.seguranca.login;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ListaNegraJwt {
    private final ConcurrentHashMap<String, Instant> tokensRevogados = new ConcurrentHashMap<>();

    public void revogar(String jti, Instant expiracao) {
        if (jti == null || jti.isBlank() || expiracao == null) {
            return;
        }
        tokensRevogados.put(jti, expiracao);
    }

    public boolean estaRevogado(String jti) {
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

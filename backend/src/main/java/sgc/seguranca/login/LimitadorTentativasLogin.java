package sgc.seguranca.login;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.env.*;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import sgc.comum.erros.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Componente que limita tentativas de login por IP para prevenir ataques de
 * força bruta.
 */
@Component
@Slf4j
public class LimitadorTentativasLogin {
    private static final int MAX_TENTATIVAS = 5;
    private static final int JANELA_MINUTOS = 1;
    private static final int DEFAULT_MAX_CACHE_ENTRIES = 1000;
    private final Environment environment;
    private final int maxCacheEntries;
    private final Map<String, Deque<LocalDateTime>> tentativasPorIp = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    public LimitadorTentativasLogin(Environment environment, Clock clock) {
        this.environment = environment;
        this.clock = clock;
        this.maxCacheEntries = DEFAULT_MAX_CACHE_ENTRIES;
    }

    LimitadorTentativasLogin(Environment environment, int maxCacheEntries, Clock clock) {
        this.environment = environment;
        this.maxCacheEntries = maxCacheEntries;
        this.clock = clock;
    }

    public void verificar(String ip) {
        if (isLimiterDesabilitado())
            return;

        if (tentativasPorIp.size() >= maxCacheEntries) {
            limparCachePeriodico();
            if (tentativasPorIp.size() >= maxCacheEntries) {
                if (!tentativasPorIp.containsKey(ip)) {
                    log.warn("Limitador de login cheio. Rejeitando novo IP: {}", ip);
                    throw new ErroMuitasTentativas("Muitas tentativas de login no sistema. Tente novamente mais tarde.");
                }
            }
        }

        limparTentativasAntigas(ip);

        Deque<LocalDateTime> tentativas = tentativasPorIp.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        if (tentativas.size() >= MAX_TENTATIVAS) {
            throw new ErroMuitasTentativas("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }
        tentativas.add(LocalDateTime.now(clock));
    }

    private boolean isLimiterDesabilitado() {
        boolean ambienteTestes = environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false);
        if (ambienteTestes) {
            return true;
        }

        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("test") || profile.equalsIgnoreCase("e2e"));
    }

    private void limparTentativasAntigas(String ip) {
        Deque<LocalDateTime> tentativas = tentativasPorIp.get(ip);
        if (tentativas == null)
            return;

        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(JANELA_MINUTOS);

        LocalDateTime tentativaAntiga;
        while ((tentativaAntiga = tentativas.peekFirst()) != null && tentativaAntiga.isBefore(limite)) {
            tentativas.pollFirst();
        }
        if (tentativas.isEmpty()) {
            tentativasPorIp.remove(ip);
        }
    }

    @Scheduled(fixedRate = 600000)
    public void limparCachePeriodico() {
        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(JANELA_MINUTOS);

        tentativasPorIp.entrySet().removeIf(entry -> {
            Deque<LocalDateTime> tentativas = entry.getValue();
            LocalDateTime tentativaAntiga;
            while ((tentativaAntiga = tentativas.peekFirst()) != null && tentativaAntiga.isBefore(limite)) {
                tentativas.pollFirst();
            }
            return tentativas.isEmpty();
        });
    }

    int getCacheSize() {
        return tentativasPorIp.size();
    }

    public static class ErroMuitasTentativas extends ErroNegocioBase {
        public ErroMuitasTentativas(String message) {
            super(message, "MUITAS_TENTATIVAS", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}

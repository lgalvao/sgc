package sgc.seguranca.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sgc.comum.erros.ErroNegocioBase;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Componente que limita tentativas de login por IP para prevenir ataques de
 * força bruta.
 */
@Component
@Slf4j
public class LimitadorTentativasLogin {
    private final Environment environment;

    private static final int MAX_TENTATIVAS = 5;
    private static final int JANELA_MINUTOS = 1;
    private static final int DEFAULT_MAX_CACHE_ENTRIES = 1000;

    private final int maxCacheEntries;
    private final Map<String, Deque<LocalDateTime>> tentativasPorIp = new ConcurrentHashMap<>();
    private final java.time.Clock clock;

    @Autowired
    public LimitadorTentativasLogin(Environment environment, java.time.Clock clock) {
        this.environment = environment;
        this.clock = clock;
        this.maxCacheEntries = DEFAULT_MAX_CACHE_ENTRIES;
    }

    LimitadorTentativasLogin(Environment environment, int maxCacheEntries, java.time.Clock clock) {
        this.environment = environment;
        this.maxCacheEntries = maxCacheEntries;
        this.clock = clock;
    }

    public void verificar(String ip) {
        if (isPerfilTesteAtivo())
            return;

        if (tentativasPorIp.size() >= maxCacheEntries) {
            limparCachePeriodico();

            if (tentativasPorIp.size() >= maxCacheEntries) {
                // Se ainda estiver cheio após limpeza, estamos sob ataque ou carga pesada.
                // Não limpamos o cache (Fail Safe), pois isso permitiria brute-force.
                // Em vez disso, rejeitamos novos IPs.
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

    private boolean isPerfilTesteAtivo() {
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

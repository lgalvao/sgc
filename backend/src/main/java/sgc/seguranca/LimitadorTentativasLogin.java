package sgc.seguranca;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sgc.comum.erros.ErroNegocioBase;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Deque;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Slf4j
public class LimitadorTentativasLogin {
    private final Environment environment;

    // Máximo de tentativas permitidas por IP
    private static final int MAX_TENTATIVAS = 5;

    // Janela de tempo em minutos
    private static final int JANELA_MINUTOS = 1;

    // Limite padrão de entradas no cache
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

    // Construtor para testes permitindo configurar o limite
    LimitadorTentativasLogin(Environment environment, int maxCacheEntries, java.time.Clock clock) {
        this.environment = environment;
        this.maxCacheEntries = maxCacheEntries;
        this.clock = clock;
    }

    public void verificar(String ip) {
        if (ip == null || isPerfilTesteAtivo()) return;

        // Proteção contra DoS: Se o mapa estiver muito cheio, limpa tudo.
        // Medida de emergência para evitar OutOfMemoryError.
        if (tentativasPorIp.size() >= maxCacheEntries) {
             // Tenta limpar entradas antigas primeiro
             limparCachePeriodico();

             // Se ainda estiver cheio (ataque ativo ou tráfego muito alto), reseta o cache.
             // Fail-open para rate limiting é preferível a travar o servidor.
             if (tentativasPorIp.size() >= maxCacheEntries) {
                 log.warn("Cache de limitador de login atingiu {} entradas. " +
                         "Limpando cache para prevenir exaustão de memória.", tentativasPorIp.size());
                 tentativasPorIp.clear();
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
        if (tentativas == null) return;

        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(JANELA_MINUTOS);

        // Loop seguro contra condição de corrida onde peekFirst() pode retornar null
        LocalDateTime tentativaAntiga;
        while ((tentativaAntiga = tentativas.peekFirst()) != null && tentativaAntiga.isBefore(limite)) {
            tentativas.pollFirst();
        }
        if (tentativas.isEmpty()) {
            tentativasPorIp.remove(ip);
        }
    }

    /**
     * Remove IPs que não têm tentativas recentes para liberar memória.
     * Executa a cada 10 minutos.
     */
    @Scheduled(fixedRate = 600000)
    public void limparCachePeriodico() {
        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(JANELA_MINUTOS);

        tentativasPorIp.entrySet().removeIf(entry -> {
            Deque<LocalDateTime> tentativas = entry.getValue();
            // Remove tentativas antigas
            LocalDateTime tentativaAntiga;
            while ((tentativaAntiga = tentativas.peekFirst()) != null && tentativaAntiga.isBefore(limite)) {
                tentativas.pollFirst();
            }
            // Se ficou vazio, remove a entrada do mapa
            return tentativas.isEmpty();
        });
    }

    // Para testes
    int getCacheSize() {
        return tentativasPorIp.size();
    }

    public static class ErroMuitasTentativas extends ErroNegocioBase {
        public ErroMuitasTentativas(String message) {
            super(message, "MUITAS_TENTATIVAS", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}

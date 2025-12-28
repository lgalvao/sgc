package sgc.seguranca;

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
public class LimitadorTentativasLogin {

    private final Environment environment;

    // Máximo de tentativas permitidas
    private static final int MAX_TENTATIVAS = 5;
    // Janela de tempo em minutos
    private static final int JANELA_MINUTOS = 1;

    private final Map<String, Deque<LocalDateTime>> tentativasPorIp = new ConcurrentHashMap<>();

    public LimitadorTentativasLogin(Environment environment) {
        this.environment = environment;
    }

    public void verificar(String ip) {
        if (ip == null || isPerfilTesteAtivo()) return;

        limparTentativasAntigas(ip);

        Deque<LocalDateTime> tentativas = tentativasPorIp.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        if (tentativas.size() >= MAX_TENTATIVAS) {
            throw new ErroMuitasTentativas("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }

        tentativas.add(LocalDateTime.now());
    }

    private boolean isPerfilTesteAtivo() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("test") || profile.equalsIgnoreCase("e2e"));
    }

    private void limparTentativasAntigas(String ip) {
        Deque<LocalDateTime> tentativas = tentativasPorIp.get(ip);
        if (tentativas == null) return;

        LocalDateTime limite = LocalDateTime.now().minusMinutes(JANELA_MINUTOS);

        // SENTINEL: Loop seguro contra condição de corrida onde peekFirst() pode retornar null
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
        LocalDateTime limite = LocalDateTime.now().minusMinutes(JANELA_MINUTOS);

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

    public static class ErroMuitasTentativas extends ErroNegocioBase {
        public ErroMuitasTentativas(String message) {
            super(message, "MUITAS_TENTATIVAS", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}

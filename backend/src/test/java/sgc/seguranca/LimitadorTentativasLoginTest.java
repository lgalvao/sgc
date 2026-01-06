package sgc.seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import sgc.seguranca.LimitadorTentativasLogin.ErroMuitasTentativas;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LimitadorTentativasLoginTest {

    private LimitadorTentativasLogin limitador;
    private Environment environment;
    private java.time.Clock clock;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        clock = mock(java.time.Clock.class);
        when(clock.getZone()).thenReturn(java.time.ZoneId.systemDefault());
        when(clock.instant()).thenReturn(java.time.Instant.now());
        
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        limitador = new LimitadorTentativasLogin(environment, clock);
    }

    @Test
    void devePermitirAteLimiteDeTentativas() {
        String ip = "192.168.1.1";

        // 5 tentativas permitidas
        IntStream.range(0, 5).forEach(i ->
            assertDoesNotThrow(() -> limitador.verificar(ip))
        );
    }
    
    @Test
    void deveBloquearAposLimiteExcedido() {
        String ip = "192.168.1.2";

        // Consome as 5 tentativas
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip));

        // 6ª tentativa deve falhar
        assertThrows(ErroMuitasTentativas.class, () -> limitador.verificar(ip));
    }

    @Test
    void deveIgnorarIpNulo() {
        assertDoesNotThrow(() -> limitador.verificar(null));
    }

    @Test
    void naoDeveBloquearIpsDiferentes() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // Bloqueia ip1
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip1));
        assertThrows(ErroMuitasTentativas.class, () -> limitador.verificar(ip1));

        // ip2 deve continuar livre
        assertDoesNotThrow(() -> limitador.verificar(ip2));
    }

    @Test
    void naoDeveBloquearSePerfilDeTesteEstiverAtivo() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        String ip = "192.168.1.4";

        // Com perfil 'test', não deve bloquear nunca
        IntStream.range(0, 10).forEach(i ->
            assertDoesNotThrow(() -> limitador.verificar(ip))
        );
    }

    @Test
    void naoDeveBloquearSePerfilE2eEstiverAtivo() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"e2e"});

        String ip = "192.168.1.5";

        // Com perfil 'e2e', não deve bloquear nunca
        IntStream.range(0, 10).forEach(i ->
            assertDoesNotThrow(() -> limitador.verificar(ip))
        );
    }

    @Test
    void deveLimparCacheAoAtingirLimiteMaximoEntradas() {
        // Configura um limitador com cache pequeno (100 entradas) para teste rápido
        int limiteTeste = 100;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);

        // Adiciona entradas únicas até o limite
        for (int i = 0; i < limiteTeste; i++) {
            limitadorTeste.verificar("10.0.0." + i);
        }

        // Verifica se atingiu o limite
        assertEquals(limiteTeste, limitadorTeste.getCacheSize());

        // Avança o tempo para além da janela (2 minutos)
        when(clock.instant()).thenReturn(java.time.Instant.now().plusSeconds(120));

        // Adiciona mais uma entrada, deve acionar a limpeza
        // Como o tempo passou, ele deve limpar as antigas e NÃO precisar limpar tudo (clear)
        limitadorTeste.verificar("10.0.0." + limiteTeste);

        // O tamanho deve ser 1 (apenas a nova, as velhas expiraram)
        assertEquals(1, limitadorTeste.getCacheSize());
    }

    @Test
    void deveLimparTudoSeCacheCheioEEntradasRecentes() {
        // Cache minúsculo
        int limiteTeste = 5;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);
        
        // Enche o cache
        for(int i=0; i<limiteTeste; i++) {
            limitadorTeste.verificar("Ip" + i);
        }
        assertEquals(limiteTeste, limitadorTeste.getCacheSize());
        
        // Tenta adicionar mais um, SEM avançar o tempo
        // limparCachePeriodico roda, mas nao remove nada.
        // Entao deve cair no fallback de limpar tudo.
        limitadorTeste.verificar("IpNovo");
        
        // Deve ter limpado tudo e adicionado o novo. Size = 1.
        assertEquals(1, limitadorTeste.getCacheSize());
    }
}

package sgc.seguranca.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import sgc.seguranca.login.LimitadorTentativasLogin.ErroMuitasTentativas;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LimitadorTentativasLogin - Testes Unitários")
class LimitadorTentativasLoginTest {
    private LimitadorTentativasLogin limitador;
    private Environment environment;
    private Clock clock;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        clock = mock(Clock.class);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(Instant.now());

        when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(false);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(false);
        limitador = new LimitadorTentativasLogin(environment, clock);
    }

    @Test
    void devePermitirAteLimiteDeTentativas() {
        String ip = "192.168.1.1";

        // 5 tentativas permitidas
        IntStream.range(0, 5).forEach(i -> assertDoesNotThrow(() -> limitador.verificar(ip)));
    }

    @Test
    void deveBloquearAposLimiteExcedido() {
        String ip = "192.168.1.2";

        // Consome as 5 tentativas
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip));

        // 6ª tentativa deve falhar
        var exception = assertThrows(ErroMuitasTentativas.class, () -> limitador.verificar(ip));
        assertNotNull(exception);
    }

    @Test
    void naoDeveBloquearIpsDiferentes() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // Bloqueia ip1
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip1));
        var exception = assertThrows(ErroMuitasTentativas.class, () -> limitador.verificar(ip1));
        assertNotNull(exception);

        // ip2 deve continuar livre
        assertDoesNotThrow(() -> limitador.verificar(ip2));
    }

    @Test
    void naoDeveBloquearSePerfilDeTesteEstiverAtivo() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        String ip = "192.168.1.4";

        // Com perfil 'test', não deve bloquear nunca
        IntStream.range(0, 10).forEach(i -> assertDoesNotThrow(() -> limitador.verificar(ip)));
    }

    @Test
    void naoDeveBloquearSePerfilE2eEstiverAtivo() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"e2e"});

        String ip = "192.168.1.5";

        // Com perfil 'e2e', não deve bloquear nunca
        IntStream.range(0, 10).forEach(i -> assertDoesNotThrow(() -> limitador.verificar(ip)));
    }

    @Test
    void deveLimparTentativasAntigasAoVerificarNovamente() {
        String ip = "192.168.1.10";
        // 1. Adiciona uma tentativa
        limitador.verificar(ip);

        // 2. Avança o tempo para expirar a tentativa (11 minutos, janela é 10)
        when(clock.instant()).thenReturn(Instant.now().plusSeconds(660));

        // 3. Verifica novamente o mesmo IP.
        // Isso deve chamar limparTentativasAntigas(ip), remover a antiga,
        // esvaziar a lista desse IP, remover do mapa, e adicionar a nova.
        assertDoesNotThrow(() -> limitador.verificar(ip));
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
        when(clock.instant()).thenReturn(Instant.now().plusSeconds(120));

        // Adiciona mais uma entrada, deve acionar a limpeza
        // Como o tempo passou, ele deve limpar as antigas e NÃO precisar limpar tudo
        // (clear)
        limitadorTeste.verificar("10.0.0." + limiteTeste);

        // O tamanho deve ser 1 (apenas a nova, as velhas expiraram)
        assertEquals(1, limitadorTeste.getCacheSize());
    }

    @Test
    void deveBloquearNovosIpsSeCacheCheioEEntradasRecentes() {
        // Cache minúsculo
        int limiteTeste = 5;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);

        // Enche o cache
        for (int i = 0; i < limiteTeste; i++) {
            limitadorTeste.verificar("Ip" + i);
        }
        assertEquals(limiteTeste, limitadorTeste.getCacheSize());

        // Tenta adicionar mais um, SEM avançar o tempo
        // Deve lançar erro indicando sobrecarga
        var exception = assertThrows(ErroMuitasTentativas.class, () -> limitadorTeste.verificar("IpNovo"));
        assertNotNull(exception);

        // O cache NÃO deve ter sido limpo. Size = limiteTeste.
        assertEquals(limiteTeste, limitadorTeste.getCacheSize());
    }

    @Test
    void devePermitirIpJaExistenteMesmoComCacheCheio() {
        int limiteTeste = 5;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);

        // Enche o cache
        for (int i = 0; i < limiteTeste; i++) {
            limitadorTeste.verificar("Ip" + i);
        }

        // Tenta verificar um IP JÁ existente no cache ("Ip0")
        // Deve permitir (não lançar erro) pois ele já está sendo rastreado
        assertDoesNotThrow(() -> limitadorTeste.verificar("Ip0"));

        // Verifica se contou a tentativa (Ip0 agora deve ter 2 tentativas)
        // Se eu chamar mais 3 vezes (total 5), deve bloquear na 6ª.
        IntStream.range(0, 3).forEach(i -> limitadorTeste.verificar("Ip0")); // Total 5
        var exception = assertThrows(ErroMuitasTentativas.class, () -> limitadorTeste.verificar("Ip0")); // 6ª
        assertNotNull(exception);
    }

    @Test
    void devePermitirSePropriedadeAmbienteTestesTrue() {
        when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(true);
        String ip = "192.168.1.15";

        // Deve permitir infinitamente
        IntStream.range(0, 10).forEach(i -> assertDoesNotThrow(() -> limitador.verificar(ip)));
    }

    @Test
    void deveBloquearSePerfilProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        String ip = "192.168.1.16";

        // Consome 5
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip));
        // 6ª falha
        var exception = assertThrows(ErroMuitasTentativas.class, () -> limitador.verificar(ip));
        assertNotNull(exception);
    }
}

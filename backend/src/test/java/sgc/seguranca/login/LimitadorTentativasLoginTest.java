package sgc.seguranca.login;

import org.junit.jupiter.api.*;
import org.springframework.core.env.*;
import sgc.comum.erros.*;
import sgc.seguranca.login.LimitadorTentativasLogin.*;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LimitadorTentativasLogin - Testes unitários")
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
        limitador.verificar(ip);

        when(clock.instant()).thenReturn(Instant.now().plusSeconds(660));

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
    void deveEvictOldestIpSeCacheCheioEEntradasRecentes() {
        // Cache minúsculo
        int limiteTeste = 5;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);

        // Enche o cache
        for (int i = 0; i < limiteTeste; i++) {
            limitadorTeste.verificar("Ip" + i);
        }
        assertEquals(limiteTeste, limitadorTeste.getCacheSize());

        // Tenta adicionar mais um, SEM avançar o tempo
        // Agora deve PERMITIR (fazendo eviction da entrada mais antiga)
        assertDoesNotThrow(() -> limitadorTeste.verificar("IpNovo"));

        // O cache deve continuar com o tamanho limite
        assertEquals(limiteTeste, limitadorTeste.getCacheSize());

        // Verifica se o Ip0 (mais antigo) foi removido
        // (Ao verificar IpNovo, Ip0 foi removido. Se verificarmos Ip0 agora, ele deve ser adicionado novamente sem erro)
        assertDoesNotThrow(() -> limitadorTeste.verificar("Ip0"));
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

    @Nested
    @DisplayName("Cobertura extra - comportamento interno")
    class CoberturaTentativasTest {

        private Environment environment;
        private RelogioMutavel relogio;
        private LimitadorTentativasLogin target;

        private static Object acessarCampo(Object alvo) throws Exception {
            Field campo = alvo.getClass().getDeclaredField("tentativasPorIp");
            campo.setAccessible(true);
            return campo.get(alvo);
        }

        private static Object invocarMetodo(
                Object alvo, String nomeMetodo, Class<?>[] tiposParametros, Object[] argumentos) throws Exception {
            var metodo = alvo.getClass().getDeclaredMethod(nomeMetodo, tiposParametros);
            metodo.setAccessible(true);
            return metodo.invoke(alvo, argumentos);
        }

        @BeforeEach
        void setUp() {
            environment = mock(Environment.class);
            relogio = new RelogioMutavel();
            target = new LimitadorTentativasLogin(environment, 2, relogio);
        }

        private void setupHabilitarLimiter() {
            when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(false);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        }

        @Test
        @DisplayName("Deve cobrir a remoção do IP mais antigo quando o cache está cheio")
        void deveCobrirRemocaoIpMaisAntigoQuandoCacheCheio() {
            setupHabilitarLimiter();
            target.verificar("1.1.1.1");
            target.verificar("2.2.2.2");

            assertThat(target.getCacheSize()).isEqualTo(2);

            target.verificar("3.3.3.3");

            assertThat(target.getCacheSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve cobrir o fallback de primeiro IP quando o cache contém apenas filas vazias")
        void deveCobrirFallbackPrimeiroIpQuandoFilasVazias() throws Exception {
            setupHabilitarLimiter();
            target = new LimitadorTentativasLogin(environment, 1, relogio);

            @SuppressWarnings("unchecked")
            Map<String, Deque<LocalDateTime>> cacheInterno = (Map<String, Deque<LocalDateTime>>) acessarCampo(target);
            cacheInterno.put("10.0.0.1", new ConcurrentLinkedDeque<>());

            target.verificar("10.0.0.2");

            assertThat(target.getCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve cobrir o caso excepcional de cache vazio ao tentar remover")
        void deveCobrirErroConfiguracaoCacheVazio() {
            setupHabilitarLimiter();
            target = new LimitadorTentativasLogin(environment, 0, relogio);

            assertThatThrownBy(() -> target.verificar("qualquer-ip"))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("cache vazio");
        }

        @Test
        @DisplayName("Deve limpar tentativas antigas e remover entrada vazia antes de registrar nova tentativa")
        void deveCobrirLimpezaTentativasAntigasComRemocaoDoIp() {
            setupHabilitarLimiter();
            target = new LimitadorTentativasLogin(environment, 10, relogio);
            target.verificar("1.1.1.1");

            relogio.avancarDoisMinutos();

            target.verificar("1.1.1.1");

            assertThat(target.getCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve cobrir o caso em que o IP é branco")
        void deveCobrirIpBranco() {
            setupHabilitarLimiter();
            target.verificar("");
            target.verificar("   ");

            assertThat(target.getCacheSize()).isZero();
        }

        @Test
        @DisplayName("Deve cobrir o caso em que o limiter está desabilitado por perfil")
        void deveCobrirLimiterDesabilitadoPorPerfil() {
            when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(false);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

            target.verificar("1.2.3.4");

            assertThat(target.getCacheSize()).isZero();
        }

        @Test
        @DisplayName("Deve cobrir filtro com fila vazia e fila preenchida ao buscar IP mais antigo")
        void deveCobrirFiltroMistoAoBuscarIpMaisAntigo() throws Exception {
            @SuppressWarnings("unchecked")
            Map<String, Deque<LocalDateTime>> cacheInterno = (Map<String, Deque<LocalDateTime>>) acessarCampo(target);

            Deque<LocalDateTime> filaVazia = new ArrayDeque<>();
            Deque<LocalDateTime> filaPreenchida = new ArrayDeque<>();
            filaPreenchida.add(LocalDateTime.now(relogio));

            cacheInterno.put("10.0.0.1", filaVazia);
            cacheInterno.put("10.0.0.2", filaPreenchida);

            String ipMaisAntigo = (String) invocarMetodo(target, "encontrarIpMaisAntigo", new Class<?>[]{}, new Object[]{});
            assertThat(ipMaisAntigo).isEqualTo("10.0.0.2");
        }

        @Test
        @DisplayName("Deve cobrir fallback para primeira chave quando todas as filas estao vazias")
        void deveCobrirFallbackPrimeiraChaveQuandoFilasVazias() throws Exception {
            @SuppressWarnings("unchecked")
            Map<String, Deque<LocalDateTime>> cacheInterno = (Map<String, Deque<LocalDateTime>>) acessarCampo(target);
            cacheInterno.put("10.0.0.1", new ArrayDeque<>());
            cacheInterno.put("10.0.0.2", new ArrayDeque<>());

            String ipMaisAntigo = (String) invocarMetodo(target, "encontrarIpMaisAntigo", new Class<?>[]{}, new Object[]{});
            assertThat(ipMaisAntigo).isNotBlank();
            assertThat(cacheInterno).containsKey(ipMaisAntigo);
        }

        @Test
        @DisplayName("Deve manter tentativa recente ao limpar tentativas antigas")
        void deveCobrirRamoQuandoTentativaAindaEstaNaJanela() throws Exception {
            @SuppressWarnings("unchecked")
            Map<String, Deque<LocalDateTime>> cacheInterno = (Map<String, Deque<LocalDateTime>>) acessarCampo(target);
            Deque<LocalDateTime> tentativas = new ArrayDeque<>();
            tentativas.add(LocalDateTime.now(relogio));
            cacheInterno.put("10.0.0.9", tentativas);

            invocarMetodo(
                    target,
                    "limparTentativasAntigas",
                    new Class<?>[]{String.class},
                    new Object[]{"10.0.0.9"});

            assertThat(cacheInterno).containsKey("10.0.0.9");
            assertThat(cacheInterno.get("10.0.0.9")).hasSize(1);
        }

        @Test
        @DisplayName("Deve remover IP quando todas as tentativas estao expiradas")
        void deveCobrirRemocaoDoIpQuandoTentativasExpiradas() throws Exception {
            @SuppressWarnings("unchecked")
            Map<String, Deque<LocalDateTime>> cacheInterno = (Map<String, Deque<LocalDateTime>>) acessarCampo(target);
            Deque<LocalDateTime> tentativasExpiradas = new ArrayDeque<>();
            tentativasExpiradas.add(LocalDateTime.now(relogio).minusMinutes(5));
            cacheInterno.put("10.0.0.10", tentativasExpiradas);

            invocarMetodo(
                    target,
                    "limparTentativasAntigas",
                    new Class<?>[]{String.class},
                    new Object[]{"10.0.0.10"});

            assertThat(cacheInterno).doesNotContainKey("10.0.0.10");
        }

        @Test
        @DisplayName("Deve remover IP quando a fila de tentativas ja inicia vazia")
        void deveCobrirRamoComFilaInicialmenteVazia() throws Exception {
            @SuppressWarnings("unchecked")
            Map<String, Deque<LocalDateTime>> cacheInterno = (Map<String, Deque<LocalDateTime>>) acessarCampo(target);
            cacheInterno.put("10.0.0.11", new ArrayDeque<>());

            invocarMetodo(
                    target,
                    "limparTentativasAntigas",
                    new Class<?>[]{String.class},
                    new Object[]{"10.0.0.11"});

            assertThat(cacheInterno).doesNotContainKey("10.0.0.11");
        }

        private static final class RelogioMutavel extends Clock {
            private Instant instanteAtual;

            private RelogioMutavel() {
                this.instanteAtual = Instant.parse("2026-03-26T10:00:00Z");
            }

            private void avancarDoisMinutos() {
                this.instanteAtual = Instant.parse("2026-03-26T10:02:00Z");
            }

            @Override
            public ZoneId getZone() {
                return ZoneId.of("UTC");
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return instanteAtual;
            }
        }
    }
}

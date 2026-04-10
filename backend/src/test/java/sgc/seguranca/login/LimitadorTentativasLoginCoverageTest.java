package sgc.seguranca.login;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.*;
import org.springframework.core.env.*;
import sgc.comum.erros.*;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LimitadorTentativasLogin - Cobertura de Testes")
class LimitadorTentativasLoginCoverageTest {

    @Mock
    private Environment environment;

    private RelogioMutavel relogio;
    private LimitadorTentativasLogin target;

    @BeforeEach
    void setUp() {
        relogio = new RelogioMutavel();
        when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(false);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        target = new LimitadorTentativasLogin(environment, 2, relogio);
    }

    @Test
    @DisplayName("Deve cobrir a remoção do IP mais antigo quando o cache está cheio")
    void deveCobrirRemocaoIpMaisAntigoQuandoCacheCheio() {
        target.verificar("1.1.1.1");
        target.verificar("2.2.2.2");

        assertThat(target.getCacheSize()).isEqualTo(2);

        target.verificar("3.3.3.3");

        assertThat(target.getCacheSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve cobrir o fallback de primeiro IP quando o cache contém apenas filas vazias")
    void deveCobrirFallbackPrimeiroIpQuandoFilasVazias() throws Exception {
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
        target = new LimitadorTentativasLogin(environment, 0, relogio);

        assertThatThrownBy(() -> target.verificar("qualquer-ip"))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("cache vazio");
    }

    @Test
    @DisplayName("Deve limpar tentativas antigas e remover entrada vazia antes de registrar nova tentativa")
    void deveCobrirLimpezaTentativasAntigasComRemocaoDoIp() {
        target = new LimitadorTentativasLogin(environment, 10, relogio);
        target.verificar("1.1.1.1");

        relogio.avancarDoisMinutos();

        target.verificar("1.1.1.1");

        assertThat(target.getCacheSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve cobrir o caso em que o IP é branco")
    void deveCobrirIpBranco() {
        target.verificar("");
        target.verificar("   ");

        assertThat(target.getCacheSize()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve cobrir o caso em que o limiter está desabilitado por perfil")
    void deveCobrirLimiterDesabilitadoPorPerfil() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        target.verificar("1.2.3.4");

        assertThat(target.getCacheSize()).isEqualTo(0);
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

package sgc.seguranca.login;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.springframework.core.env.*;
import sgc.comum.erros.*;
import sgc.seguranca.login.LimitadorTentativasLogin.*;

import java.time.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LimitadorTentativasLogin")
class LimitadorTentativasLoginTest {
    private Environment environment;
    private Clock clock;
    private LimitadorTentativasLogin limitador;
    private Instant instanteBase;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        clock = mock(Clock.class);
        instanteBase = Instant.parse("2026-03-26T10:00:00Z");
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(clock.instant()).thenReturn(instanteBase);
        when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(false);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        limitador = new LimitadorTentativasLogin(environment, clock);
    }

    @Test
    @DisplayName("deve permitir até o limite de tentativas por IP")
    void devePermitirAteLimiteDeTentativasPorIp() {
        String ip = "192.168.1.1";
        IntStream.range(0, 5).forEach(i -> assertThatCode(() -> limitador.verificar(ip)).doesNotThrowAnyException());
    }

    @Test
    @DisplayName("deve bloquear após exceder o limite de tentativas")
    void deveBloquearAposExcederLimiteDeTentativas() {
        String ip = "192.168.1.2";
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip));

        assertThatThrownBy(() -> limitador.verificar(ip))
                .isInstanceOf(ErroMuitasTentativas.class)
                .hasMessageContaining("Muitas tentativas de login");
    }

    @Test
    @DisplayName("não deve bloquear IPs diferentes")
    void naoDeveBloquearIpsDiferentes() {
        String ipBloqueado = "10.0.0.1";
        String ipLivre = "10.0.0.2";

        IntStream.range(0, 5).forEach(i -> limitador.verificar(ipBloqueado));

        assertThatThrownBy(() -> limitador.verificar(ipBloqueado))
                .isInstanceOf(ErroMuitasTentativas.class);
        assertThatCode(() -> limitador.verificar(ipLivre)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "e2e"})
    @DisplayName("não deve bloquear quando profile desabilita o limitador")
    void naoDeveBloquearQuandoPerfilDesabilitaLimitador(String profile) {
        when(environment.getActiveProfiles()).thenReturn(new String[]{profile});
        String ip = "192.168.1.4";

        IntStream.range(0, 10).forEach(i -> assertThatCode(() -> limitador.verificar(ip)).doesNotThrowAnyException());
        assertThat(limitador.getCacheSize()).isZero();
    }

    @Test
    @DisplayName("deve permitir novamente após expiração da janela")
    void devePermitirNovamenteAposExpiracaoDaJanela() {
        String ip = "192.168.1.10";
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip));
        when(clock.instant()).thenReturn(instanteBase.plusSeconds(61));

        IntStream.range(0, 5).forEach(i -> assertThatCode(() -> limitador.verificar(ip)).doesNotThrowAnyException());
        assertThatThrownBy(() -> limitador.verificar(ip)).isInstanceOf(ErroMuitasTentativas.class);
    }

    @Test
    @DisplayName("deve limpar cache quando entradas antigas expiram")
    void deveLimparCacheQuandoEntradasAntigasExpiram() {
        int limiteTeste = 100;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);
        IntStream.range(0, limiteTeste).forEach(i -> limitadorTeste.verificar("10.0.0." + i));
        assertThat(limitadorTeste.getCacheSize()).isEqualTo(limiteTeste);

        when(clock.instant()).thenReturn(instanteBase.plusSeconds(120));
        limitadorTeste.verificar("10.0.1.1");

        assertThat(limitadorTeste.getCacheSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("deve remover IP mais antigo quando cache está cheio com entradas recentes")
    void deveRemoverIpMaisAntigoQuandoCacheCheio() {
        int limiteTeste = 5;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);
        IntStream.range(0, limiteTeste).forEach(i -> limitadorTeste.verificar("Ip" + i));
        assertThat(limitadorTeste.getCacheSize()).isEqualTo(limiteTeste);

        assertThatCode(() -> limitadorTeste.verificar("IpNovo")).doesNotThrowAnyException();
        assertThat(limitadorTeste.getCacheSize()).isEqualTo(limiteTeste);
        assertThatCode(() -> limitadorTeste.verificar("Ip0")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve permitir IP existente mesmo com cache cheio e bloquear apenas no excedente")
    void devePermitirIpExistenteMesmoComCacheCheio() {
        int limiteTeste = 5;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);
        IntStream.range(0, limiteTeste).forEach(i -> limitadorTeste.verificar("Ip" + i));

        assertThatCode(() -> limitadorTeste.verificar("Ip0")).doesNotThrowAnyException();
        IntStream.range(0, 3).forEach(i -> limitadorTeste.verificar("Ip0"));
        assertThatThrownBy(() -> limitadorTeste.verificar("Ip0")).isInstanceOf(ErroMuitasTentativas.class);
    }

    @Test
    @DisplayName("deve desabilitar limitador quando propriedade de ambiente de testes está ativa")
    void deveDesabilitarLimitadorQuandoPropriedadeDeTestesAtiva() {
        when(environment.getProperty("aplicacao.ambiente-testes", Boolean.class, false)).thenReturn(true);
        String ip = "192.168.1.15";

        IntStream.range(0, 10).forEach(i -> assertThatCode(() -> limitador.verificar(ip)).doesNotThrowAnyException());
        assertThat(limitador.getCacheSize()).isZero();
    }

    @Test
    @DisplayName("deve bloquear em ambiente produtivo")
    void deveBloquearEmAmbienteProdutivo() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        String ip = "192.168.1.16";
        IntStream.range(0, 5).forEach(i -> limitador.verificar(ip));

        assertThatThrownBy(() -> limitador.verificar(ip)).isInstanceOf(ErroMuitasTentativas.class);
    }

    @Test
    @DisplayName("deve falhar quando limite de cache é inválido")
    void deveFalharQuandoLimiteDeCacheInvalido() {
        LimitadorTentativasLogin limitadorInvalido = new LimitadorTentativasLogin(environment, 0, clock);

        assertThatThrownBy(() -> limitadorInvalido.verificar("qualquer-ip"))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("cache vazio");
    }

    @Test
    @DisplayName("deve ignorar IP em branco")
    void deveIgnorarIpEmBranco() {
        limitador.verificar("");
        limitador.verificar("   ");

        assertThat(limitador.getCacheSize()).isZero();
    }

    @Test
    @DisplayName("deve cobrir branch com deque vazio na busca do IP mais antigo")
    @SuppressWarnings("unchecked")
    void deveCobrirBranchComDequeVazioNaBuscaDoIpMaisAntigo() {
        int limiteTeste = 1;
        LimitadorTentativasLogin limitadorTeste = new LimitadorTentativasLogin(environment, limiteTeste, clock);

        java.util.Deque<LocalDateTime> dequeVazio = mock(java.util.Deque.class);
        when(dequeVazio.isEmpty()).thenReturn(false, true);
        when(dequeVazio.peekFirst()).thenReturn(null);

        java.util.Deque<LocalDateTime> dequeValido = new java.util.concurrent.ConcurrentLinkedDeque<>();
        dequeValido.add(LocalDateTime.now(clock));

        limitadorTeste.getTentativasPorIp().put("IP_VAZIO", dequeVazio);
        limitadorTeste.getTentativasPorIp().put("IP_VALIDO", dequeValido);

        assertThatCode(() -> limitadorTeste.verificar("IP_NOVO")).doesNotThrowAnyException();
    }
}

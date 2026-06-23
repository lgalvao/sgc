package sgc.comum.util;

import org.aspectj.lang.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Aspecto de Monitoramento")
class MonitoramentoAspectTest {
    private final MonitoramentoAspect aspect = new MonitoramentoAspect(true, 500);
    private final MonitoramentoAspect aspectInativo = new MonitoramentoAspect(false, 500);

    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private Signature signature;

    @Test
    @DisplayName("Deve executar sem logar aviso se for rápido")
    void deveExecutarRapido() throws Throwable {
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("OK");
    }

    @Test
    @DisplayName("Deve executar sem monitoramento quando estiver inativo")
    void deveExecutarSemMonitoramentoQuandoInativo() throws Throwable {
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspectInativo.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("OK");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("Deve logar aviso se for lento")
    @SuppressWarnings("java:S2925")
    void deveLogarAvisoSeLento() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("sgc.Classe");
        when(signature.getName()).thenReturn("metodo");

        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(550); // Simula lentidão > 500ms
            return "OK";
        });

        Object result = aspect.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("OK");
    }

    @Test
    @DisplayName("Deve invocar construtor vazio sem erros")
    void deveInvocarConstrutorVazio() {
        assertThatCode(MonitoramentoAspect::new).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve executar sem logar aviso se for rapido")
    void deveExecutarRapidoESemTrace() throws Throwable {
        MonitoramentoAspect aspecto = new MonitoramentoAspect(true, 500);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("sgc.Classe");
        when(signature.getName()).thenReturn("metodo");
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspecto.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("OK");
    }
}

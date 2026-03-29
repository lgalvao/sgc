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
    private final MonitoramentoAspect aspect = new MonitoramentoAspect();

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
    @DisplayName("Deve logar aviso se for lento")
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
    @DisplayName("Deve executar com trace completo mesmo sem assinatura")
    void deveExecutarComTraceCompletoSemAssinatura() throws Throwable {
        MonitoramentoAspect aspectoComTrace = new MonitoramentoAspect(true, 1_000);
        when(joinPoint.getSignature()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("TRACE");

        Object result = aspectoComTrace.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("TRACE");
    }
}

package sgc.comum.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
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
        // O teste passa se não lançar exceção. As linhas de log serão executadas.
    }
}

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
    private final MonitoramentoAspect aspect = new MonitoramentoAspect(true, false, 500);
    private final MonitoramentoAspect aspectInativo = new MonitoramentoAspect(false, false, 500);

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
    @DisplayName("Deve executar com trace completo mesmo sem assinatura")
    void deveExecutarComTraceCompletoSemAssinatura() throws Throwable {
        MonitoramentoAspect aspectoComTrace = new MonitoramentoAspect(true, true, 1_000);
        when(joinPoint.getSignature()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("TRACE");

        Object result = aspectoComTrace.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("TRACE");
    }

    @Test
    @DisplayName("Deve executar e logar trace detalhado se ativado pela requisicao e trace completo for false")
    void deveLogarTraceAtivadoPelaRequisicao() throws Throwable {
        MonitoramentoAspect aspecto = new MonitoramentoAspect(true, false, 1_000);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("sgc.Classe");
        when(signature.getName()).thenReturn("metodo");
        when(joinPoint.proceed()).thenReturn("TRACE_REQ");

        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(
                        new org.springframework.mock.web.MockHttpServletRequest()
                )
        );
        org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
                .setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_MONITORAMENTO_ATIVO, true, org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST);

        Object result = aspecto.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("TRACE_REQ");
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Deve executar sem logar aviso se for rapido e sem trace completo")
    void deveExecutarRapidoESemTrace() throws Throwable {
        MonitoramentoAspect aspecto = new MonitoramentoAspect(true, false, 500);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("sgc.Classe");
        when(signature.getName()).thenReturn("metodo");
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspecto.monitorarExecucao(joinPoint);

        assertThat(result).isEqualTo("OK");
    }
}

package sgc.comum.util;

import lombok.extern.slf4j.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.util.concurrent.atomic.*;

@Aspect
@Component
@Slf4j
public class MonitoramentoAspect {
    private final boolean monitoramentoJavaLentoAtivo;
    private final boolean monitoramentoJavaCompletoAtivo;
    private final long tempoMinimoJavaMs;
    private final AtomicLong contadorChamadas = new AtomicLong();

    public MonitoramentoAspect() {
        this(false, false, 500);
    }

    public MonitoramentoAspect(boolean monitoramentoJavaLentoAtivo,
                               boolean monitoramentoJavaCompletoAtivo,
                               long tempoMinimoJavaMs) {
        this.monitoramentoJavaLentoAtivo = monitoramentoJavaLentoAtivo;
        this.monitoramentoJavaCompletoAtivo = monitoramentoJavaCompletoAtivo;
        this.tempoMinimoJavaMs = tempoMinimoJavaMs;
    }

    @Autowired
    public MonitoramentoAspect(MonitoramentoProperties props) {
        this.monitoramentoJavaLentoAtivo = props.isMonitoramentoJavaLentoAtivo();
        this.monitoramentoJavaCompletoAtivo = props.isMonitoramentoJavaCompletoAtivo();
        this.tempoMinimoJavaMs = props.getTempoMinimoJavaMs();
    }

    @Around("execution(* sgc..*Service.*(..))" +
            " || execution(* sgc..*Repo.*(..))" +
            " || execution(* sgc..*Facade.*(..))"
    )

    public Object monitorarExecucao(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!monitoramentoJavaLentoAtivo && !monitoramentoJavaCompletoAtivo) return joinPoint.proceed();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            double tempoTotal = stopWatch.getTotalTimeNanos()/1_000_000d;
            String tempoTotalStr = "%.2f".formatted(tempoTotal);

            long numeroChamada = contadorChamadas.incrementAndGet();
            Signature assinatura = joinPoint.getSignature();
            String classe = assinatura != null ? assinatura.getDeclaringTypeName() : joinPoint.getClass().getName();
            String metodo = assinatura != null ? assinatura.getName() : "desconhecido";
            boolean monitoramentoDetalhado = monitoramentoJavaCompletoAtivo || FiltroMonitoramentoHttp.isMonitoramentoAtivoNaRequisicao();
            String http = FiltroMonitoramentoHttp.obterDescricaoHttpAtual();

            if (monitoramentoDetalhado) {
                log.info("TRACE #{} http=\"{}\" {}.{} {}ms",
                        numeroChamada,
                        http,
                        classe,
                        metodo,
                        tempoTotalStr);
            } else if (tempoTotal > tempoMinimoJavaMs) {
                log.warn("TRACE-LENTO http=\"{}\" {}.{} {}ms",
                        http,
                        classe,
                        metodo,
                        tempoTotalStr);
            }
        }
    }
}

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
    private final boolean traceCompleto;
    private final long limiteAlertaMs;
    private final AtomicLong contadorChamadas = new AtomicLong();

    public MonitoramentoAspect() {
        this(false, 500);
    }

    public MonitoramentoAspect(
            @Value("${sgc.monitoramento.trace-completo:false}") boolean traceCompleto,
            @Value("${sgc.monitoramento.limite-alerta-ms:500}") long limiteAlertaMs
    ) {
        this.traceCompleto = traceCompleto;
        this.limiteAlertaMs = limiteAlertaMs;
    }

    @Around("execution(* sgc..*Service.*(..)) || execution(* sgc..*Repo.*(..))")
    public Object monitorarExecucao(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long tempoTotal = stopWatch.getTotalTimeMillis();
            long numeroChamada = contadorChamadas.incrementAndGet();
            Signature assinatura = joinPoint.getSignature();
            String classe = assinatura != null ? assinatura.getDeclaringTypeName() : joinPoint.getClass().getName();
            String metodo = assinatura != null ? assinatura.getName() : "desconhecido";

            if (traceCompleto) {
                log.info("EXECUCAO MONITORADA #{}: {}.{} levou {} ms",
                        numeroChamada,
                        classe,
                        metodo,
                        tempoTotal);
            } else if (tempoTotal > limiteAlertaMs) {
                log.warn("EXECUCAO LENTA: {}.{} levou {} ms",
                        classe,
                        metodo,
                        tempoTotal);
            }
        }
    }
}

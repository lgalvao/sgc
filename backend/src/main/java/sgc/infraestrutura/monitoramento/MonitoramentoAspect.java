package sgc.infraestrutura.monitoramento;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class MonitoramentoAspect {

    private static final Logger log = LoggerFactory.getLogger(MonitoramentoAspect.class);
    private static final long LIMITE_ALERTA_MS = 50;

    @Around("execution(* sgc..*Service.*(..)) || execution(* sgc..*Repo.*(..))")
    public Object monitorarExecucao(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long tempoTotal = stopWatch.getTotalTimeMillis();

            if (tempoTotal > LIMITE_ALERTA_MS) {
                log.warn("EXECUCAO LENTA: {}.{} levou {} ms",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    tempoTotal);
            }
        }
    }
}

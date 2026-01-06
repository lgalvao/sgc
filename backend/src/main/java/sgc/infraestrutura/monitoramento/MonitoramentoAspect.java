package sgc.infraestrutura.monitoramento;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class MonitoramentoAspect {
    private static final long LIMITE_ALERTA_MS = 100;

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

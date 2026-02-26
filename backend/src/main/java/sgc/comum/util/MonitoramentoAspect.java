package sgc.comum.util;

import lombok.extern.slf4j.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

@Aspect
@Component
@Slf4j
public class MonitoramentoAspect {
    private static final long LIMITE_ALERTA_MS = 500;

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

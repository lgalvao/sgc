package sgc.comum.util;

import lombok.extern.slf4j.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

@Aspect
@Component
@Slf4j
public class MonitoramentoAspect {
    private final boolean monitoramentoJavaLentoAtivo;
    private final long tempoMinimoJavaMs;

    public MonitoramentoAspect() {
        this(false, 500);
    }

    public MonitoramentoAspect(boolean monitoramentoJavaLentoAtivo, long tempoMinimoJavaMs) {
        this.monitoramentoJavaLentoAtivo = monitoramentoJavaLentoAtivo;
        this.tempoMinimoJavaMs = tempoMinimoJavaMs;
    }

    @Autowired
    public MonitoramentoAspect(MonitoramentoProperties props) {
        this.monitoramentoJavaLentoAtivo = props.isMonitoramentoJavaLentoAtivo();
        this.tempoMinimoJavaMs = props.getTempoMinimoJavaMs();
    }

    @Around("execution(* sgc..*Service.*(..))" +
            " || execution(* sgc..*Repo.*(..))" +
            " || execution(* sgc..*Facade.*(..))"
    )

    public Object monitorarExecucao(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!monitoramentoJavaLentoAtivo) return joinPoint.proceed();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            double tempoTotal = stopWatch.getTotalTimeNanos() / 1_000_000d;
            Signature assinatura = joinPoint.getSignature();
            String classe = assinatura != null ? assinatura.getDeclaringTypeName() : joinPoint.getClass().getName();
            String metodo = assinatura != null ? assinatura.getName() : "desconhecido";

            if (tempoTotal > tempoMinimoJavaMs) {
                FiltroMonitoramentoHttp.registrarJavaLento(classe, metodo, tempoTotal);
            }
        }
    }
}

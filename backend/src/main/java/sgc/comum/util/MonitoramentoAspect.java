package sgc.comum.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

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
            " || execution(* sgc..*Repo.*(..))"
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

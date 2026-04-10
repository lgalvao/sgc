package sgc.comum.util;

import lombok.extern.slf4j.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.util.concurrent.atomic.*;

@Aspect
@Component
@Slf4j
public class MonitoramentoAspect {
    private final boolean ativo;
    private final boolean traceCompleto;
    private final long limiteAlertaMs;
    private final AtomicLong contadorChamadas = new AtomicLong();

    public MonitoramentoAspect() {
        this(false, false, 500);
    }

    public MonitoramentoAspect(boolean ativo, boolean traceCompleto, long limiteAlertaMs) {
        this.ativo = ativo;
        this.traceCompleto = traceCompleto;
        this.limiteAlertaMs = limiteAlertaMs;
    }

    public MonitoramentoAspect(MonitoramentoProperties monitoramentoProperties) {
        this(
                monitoramentoProperties.isAtivo(),
                monitoramentoProperties.isTraceCompleto(),
                monitoramentoProperties.getLimiteAlertaMs()
        );
    }

    @Around(
            "execution(* sgc..*Service.*(..))" +
            " || execution(* sgc..*Repo.*(..))" +
            " || execution(* sgc..*Facade.*(..))"
    )
    public Object monitorarExecucao(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!ativo) {
            return joinPoint.proceed();
        }

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
            String correlacaoId = FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
            boolean monitoramentoDetalhado = traceCompleto || FiltroMonitoramentoHttp.isMonitoramentoAtivoNaRequisicao();

            if (monitoramentoDetalhado) {
                log.info("TRACE #{} [{}] {}.{} {}ms",
                        numeroChamada,
                        correlacaoId,
                        classe,
                        metodo,
                        tempoTotal);
            } else if (tempoTotal > limiteAlertaMs) {
                log.warn("TRACE-LENTO [{}] {}.{} {}ms",
                        correlacaoId,
                        classe,
                        metodo,
                        tempoTotal);
            }
        }
    }
}

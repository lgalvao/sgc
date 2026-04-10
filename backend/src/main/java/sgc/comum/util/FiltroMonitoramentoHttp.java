package sgc.comum.util;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.*;
import org.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.context.request.*;
import org.springframework.web.filter.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
public class FiltroMonitoramentoHttp extends OncePerRequestFilter {
    public static final String HEADER_CORRELACAO_ID = "X-Correlacao-Id";
    public static final String HEADER_MONITORAMENTO_ATIVO = "X-Monitoramento-Ativo";
    public static final String HEADER_TEMPO_SERVIDOR_MS = "X-Tempo-Servidor-Ms";
    public static final String ATRIBUTO_CORRELACAO_ID = "sgc.monitoramento.correlacaoId";
    public static final String ATRIBUTO_MONITORAMENTO_ATIVO = "sgc.monitoramento.ativo";
    public static final String MDC_CORRELACAO_ID = "correlacaoId";

    private final MonitoramentoProperties monitoramentoProperties;

    public FiltroMonitoramentoHttp(MonitoramentoProperties monitoramentoProperties) {
        this.monitoramentoProperties = monitoramentoProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!monitoramentoProperties.isAtivo()) {
            filterChain.doFilter(request, response);
            return;
        }

        long inicioNs = System.nanoTime();
        String correlacaoId = obterOuGerarCorrelacaoId(request);
        boolean monitoramentoAtivoNaRequisicao = deveMonitorarDetalhado(request);

        request.setAttribute(ATRIBUTO_CORRELACAO_ID, correlacaoId);
        request.setAttribute(ATRIBUTO_MONITORAMENTO_ATIVO, monitoramentoAtivoNaRequisicao);
        response.setHeader(HEADER_CORRELACAO_ID, correlacaoId);

        MDC.put(MDC_CORRELACAO_ID, correlacaoId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNs);

            response.setHeader(HEADER_TEMPO_SERVIDOR_MS, String.valueOf(duracaoMs));
            response.setHeader("Server-Timing", "app;dur=" + duracaoMs);

            if (monitoramentoAtivoNaRequisicao) {
                log.info("HTTP MONITORADO: {} {} -> {} em {} ms",
                        request.getMethod(),
                        obterCaminhoComQueryString(request),
                        response.getStatus(),
                        duracaoMs);
            } else if (duracaoMs > monitoramentoProperties.getLimiteAlertaMs()) {
                log.warn("HTTP LENTO: {} {} -> {} em {} ms",
                        request.getMethod(),
                        obterCaminhoComQueryString(request),
                        response.getStatus(),
                        duracaoMs);
            }

            MDC.remove(MDC_CORRELACAO_ID);
        }
    }

    public static boolean isMonitoramentoAtivoNaRequisicao() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return Boolean.TRUE.equals(
                    servletRequestAttributes.getRequest().getAttribute(ATRIBUTO_MONITORAMENTO_ATIVO)
            );
        }
        return false;
    }

    public static String obterCorrelacaoIdAtual() {
        String correlacaoIdMdc = MDC.get(MDC_CORRELACAO_ID);
        if (StringUtils.hasText(correlacaoIdMdc)) {
            return correlacaoIdMdc;
        }

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            Object correlacaoId = servletRequestAttributes.getRequest().getAttribute(ATRIBUTO_CORRELACAO_ID);
            if (correlacaoId instanceof String valor && StringUtils.hasText(valor)) {
                return valor;
            }
        }

        return UUID.randomUUID().toString();
    }

    private boolean deveMonitorarDetalhado(HttpServletRequest request) {
        if (monitoramentoProperties.isTraceCompleto()) {
            return true;
        }

        if (monitoramentoProperties.isPermitirAtivacaoPorHeader()
                && "true".equalsIgnoreCase(request.getHeader(HEADER_MONITORAMENTO_ATIVO))) {
            return true;
        }

        double taxaAmostragem = monitoramentoProperties.getTaxaAmostragem();
        return taxaAmostragem > 0 && ThreadLocalRandom.current().nextDouble() < taxaAmostragem;
    }

    private String obterOuGerarCorrelacaoId(HttpServletRequest request) {
        String correlacaoIdHeader = request.getHeader(HEADER_CORRELACAO_ID);
        if (StringUtils.hasText(correlacaoIdHeader)) {
            return correlacaoIdHeader;
        }
        return UUID.randomUUID().toString();
    }

    private String obterCaminhoComQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (!StringUtils.hasText(queryString)) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }
}

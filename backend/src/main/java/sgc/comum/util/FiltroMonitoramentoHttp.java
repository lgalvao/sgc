package sgc.comum.util;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.context.request.*;
import org.springframework.web.filter.*;

import java.io.*;
import java.util.*;

@Component
public class FiltroMonitoramentoHttp extends OncePerRequestFilter {
    public static final String HEADER_CORRELACAO_ID = "X-Correlacao-Id";
    public static final String HEADER_TEMPO_SERVIDOR_MS = "X-Tempo-Servidor-Ms";
    public static final String ATRIBUTO_CORRELACAO_ID = "sgc.monitoramento.correlacaoId";
    public static final String ATRIBUTO_HTTP_METODO = "sgc.monitoramento.httpMetodo";
    public static final String ATRIBUTO_HTTP_CAMINHO = "sgc.monitoramento.httpCaminho";
    public static final String ATRIBUTO_JAVA_LENTOS = "sgc.monitoramento.javaLentos";
    public static final String MDC_CORRELACAO_ID = "correlacaoId";
    private static final Logger LOG_MONITORAMENTO = LoggerFactory.getLogger("sgc.monitoramento");

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
        if (!monitoramentoProperties.isMonitoramentoHttpAtivo()) {
            filterChain.doFilter(request, response);
            return;
        }

        long inicioNs = System.nanoTime();
        String correlacaoId = obterOuGerarCorrelacaoId(request);

        request.setAttribute(ATRIBUTO_CORRELACAO_ID, correlacaoId);
        request.setAttribute(ATRIBUTO_HTTP_METODO, request.getMethod());
        request.setAttribute(ATRIBUTO_HTTP_CAMINHO, obterCaminhoComQueryString(request));
        request.setAttribute(ATRIBUTO_JAVA_LENTOS, new ArrayList<String>());
        response.setHeader(HEADER_CORRELACAO_ID, correlacaoId);

        MDC.put(MDC_CORRELACAO_ID, correlacaoId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNs);

            response.setHeader(HEADER_TEMPO_SERVIDOR_MS, String.valueOf(duracaoMs));
            response.setHeader("Server-Timing", "app;dur=" + duracaoMs);

            if (deveLogarHttp(duracaoMs)) {
                LOG_MONITORAMENTO.info(formatarLinhaHttp(request, response, duracaoMs));
            }
            logarJavaLento(request);

            MDC.remove(MDC_CORRELACAO_ID);
        }
    }

    private boolean deveLogarHttp(long duracaoMs) {
        return duracaoMs >= monitoramentoProperties.getTempoHttpLentoMs();
    }

    private String formatarLinhaHttp(HttpServletRequest request,
                                     HttpServletResponse response,
                                     long duracaoMs) {
        String caminho = obterCaminhoComQueryString(request);
        int status = response.getStatus();

        if (status == HttpServletResponse.SC_OK) {
            return String.format("http %s %s %dms", request.getMethod(), caminho, duracaoMs);
        }

        return String.format("http %s %s %d %dms", request.getMethod(), caminho, status, duracaoMs);
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

    public static String obterDescricaoHttpAtual() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object metodo = request.getAttribute(ATRIBUTO_HTTP_METODO);
            Object caminho = request.getAttribute(ATRIBUTO_HTTP_CAMINHO);
            if (metodo instanceof String metodoAtual
                    && StringUtils.hasText(metodoAtual)
                    && caminho instanceof String caminhoAtual
                    && StringUtils.hasText(caminhoAtual)) {
                return metodoAtual + " " + caminhoAtual;
            }
        }
        return "sem-http";
    }

    public static void registrarJavaLento(String classe, String metodo, double duracaoMs) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            Object javaLentos = servletRequestAttributes.getRequest().getAttribute(ATRIBUTO_JAVA_LENTOS);
            if (javaLentos instanceof List<?> lista) {
                @SuppressWarnings("unchecked")
                List<String> entradas = (List<String>) lista;
                entradas.add("java %s.%s %.2fms".formatted(obterNomeSimples(classe), metodo, duracaoMs));
            }
        }
    }

    private void logarJavaLento(HttpServletRequest request) {
        Object javaLentos = request.getAttribute(ATRIBUTO_JAVA_LENTOS);
        if (!(javaLentos instanceof List<?> entradas) || entradas.isEmpty()) {
            return;
        }

        entradas.stream()
                .map(String.class::cast)
                .forEach(LOG_MONITORAMENTO::info);
    }

    private static String obterNomeSimples(String classe) {
        int indice = classe.lastIndexOf('.');
        if (indice < 0 || indice == classe.length() - 1) {
            return classe;
        }
        return classe.substring(indice + 1);
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

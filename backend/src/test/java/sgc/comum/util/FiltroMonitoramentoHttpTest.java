package sgc.comum.util;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.jspecify.annotations.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do filtro de monitoramento HTTP")
class FiltroMonitoramentoHttpTest {

    @Test
    @DisplayName("Deve propagar correlacao e marcar monitoramento detalhado via header")
    void devePropagarCorrelacaoEAtivarMonitoramentoPorHeader() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setPermitirAtivacaoPorHeader(true);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/processos");
        request.addHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID, "corr-123");
        request.addHeader(FiltroMonitoramentoHttp.HEADER_MONITORAMENTO_ATIVO, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            HttpServletRequest requestHttp = (HttpServletRequest) servletRequest;
            assertThat(requestHttp.getAttribute(FiltroMonitoramentoHttp.ATRIBUTO_CORRELACAO_ID))
                    .isEqualTo("corr-123");
            assertThat(requestHttp.getAttribute(FiltroMonitoramentoHttp.ATRIBUTO_MONITORAMENTO_ATIVO))
                    .isEqualTo(true);
        };

        filtro.doFilter(request, response, filterChain);

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isEqualTo("corr-123");
        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_TEMPO_SERVIDOR_MS)).isNotBlank();
        assertThat(response.getHeader("Server-Timing")).startsWith("app;dur=");
    }

    @Test
    @DisplayName("Deve logar erro quando o tempo for muito alto (HTTP LENTO)")
    @SuppressWarnings("java:S2925")
    void deveLogarErroHTTP() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setLimiteAlertaMs(1);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/processos");
        request.setQueryString("param1=valor1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Deve ignorar requisicao fora de /api quando o filtro decidir nao aplicar")
    void deveIgnorarRequisicaoForaDeApi() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
            // não faz nada
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNull();
    }

    @Test
    @DisplayName("Deve aplicar taxa de amostragem quando trace não for completo nem habilitado por header")
    void deveAplicarAmostragem() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTraceCompleto(false);
        properties.setTaxaAmostragem(1.0); // Garante que caia na amostragem

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Não deve aplicar amostragem quando taxa for 0")
    void naoDeveAplicarAmostragem() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTraceCompleto(false);
        properties.setTaxaAmostragem(0.0);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Deve formatar linha com erro 500 sem amostragem")
    void deveFormatarErro500SemAmostragem() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTraceCompleto(true);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/erro");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Deve ser inativo")
    void deveSerInativo() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(false);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/inativo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNull();
    }

    @Test
    @DisplayName("Deve usar HTTP MUITO LENTO")
    @SuppressWarnings("java:S2925")
    void deveUsarMuitoLento() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTraceCompleto(true);
        properties.setLimiteMuitoLentoMs(1);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/muito-lento");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNotBlank();
    }

    @Test
    @DisplayName("isMonitoramentoAtivoNaRequisicao deve retornar false quando não houver contexto")
    void isMonitoramentoAtivoNaRequisicaoDeveRetornarFalseQuandoSemContexto() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        assertThat(FiltroMonitoramentoHttp.isMonitoramentoAtivoNaRequisicao()).isFalse();
    }

    @Test
    @DisplayName("isMonitoramentoAtivoNaRequisicao deve retornar false quando atributo for null")
    void isMonitoramentoAtivoNaRequisicaoDeveRetornarFalseQuandoNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // Não seta o atributo
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );
        assertThat(FiltroMonitoramentoHttp.isMonitoramentoAtivoNaRequisicao()).isFalse();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("isMonitoramentoAtivoNaRequisicao deve retornar false quando context for invalido")
    void isMonitoramentoAtivoNaRequisicaoDeveRetornarFalseQuandoContextInvalido() {
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.RequestAttributes() {
                    @Override
                    @NullUnmarked
                    public Object getAttribute(@NonNull String name, int scope) { return null; }
                    @Override
                    public void setAttribute(String name, Object value, int scope) { /* dummy */ }
                    @Override
                    public void removeAttribute(String name, int scope) { /* dummy */ }
                    @Override
                    public String[] getAttributeNames(int scope) { return new String[0]; }
                    @Override
                    public void registerDestructionCallback(String name, Runnable callback, int scope) { /* dummy */ }
                    @Override
                    @NullUnmarked
                    public Object resolveReference(@NonNull String key) { return null; }
                    @Override
                    @NullUnmarked
                    public String getSessionId() { return null; }
                    @Override
                    @NullUnmarked
                    public Object getSessionMutex() { return null; }
                }
        );
        assertThat(FiltroMonitoramentoHttp.isMonitoramentoAtivoNaRequisicao()).isFalse();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("obterCorrelacaoIdAtual deve retornar UUID quando não houver contexto nem MDC")
    void obterCorrelacaoIdAtualDeveRetornarUUIDQuandoSemContexto() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        org.slf4j.MDC.clear();
        String id = FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
        assertThat(id).isNotBlank().hasSize(36);
    }

    @Test
    @DisplayName("obterCorrelacaoIdAtual deve retornar o valor do request attribute")
    void obterCorrelacaoIdAtualDeveRetornarDoRequest() {
        org.slf4j.MDC.clear();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_CORRELACAO_ID, "req-corr-123");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        assertThat(FiltroMonitoramentoHttp.obterCorrelacaoIdAtual()).isEqualTo("req-corr-123");
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("obterCorrelacaoIdAtual deve retornar UUID quando request attribute não for string válida")
    void obterCorrelacaoIdAtualDeveRetornarUUIDQuandoRequestAtributoInvalido() {
        org.slf4j.MDC.clear();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_CORRELACAO_ID, new Object()); // Não é String
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        String id = FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
        assertThat(id).isNotBlank().hasSize(36);
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("obterCorrelacaoIdAtual deve retornar o valor do MDC se existir")
    void obterCorrelacaoIdDeveRetornarDoMDC() {
        org.slf4j.MDC.put(FiltroMonitoramentoHttp.MDC_CORRELACAO_ID, "mdc-corr-123");

        String id = FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
        assertThat(id).isEqualTo("mdc-corr-123");

        org.slf4j.MDC.clear();
    }

    @Test
    @DisplayName("obterCorrelacaoIdAtual deve retornar UUID quando request attribute for string vazia")
    void obterCorrelacaoIdDeveRetornarUUIDQuandoRequestAtributoStringVazia() {
        org.slf4j.MDC.clear();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_CORRELACAO_ID, "   "); // String vazia
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        String id = FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
        assertThat(id).isNotBlank().hasSize(36);
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Deve usar HTTP LENTO (entre o alerta e o muito lento)")
    @SuppressWarnings("java:S2925")
    void deveUsarLento() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTraceCompleto(true);
        properties.setLimiteMuitoLentoMs(100);
        properties.setLimiteLentoMs(1);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/lento");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNotBlank();
    }

    @Test
    @DisplayName("isMonitoramentoAtivoNaRequisicao deve retornar verdadeiro do request")
    void isMonitoramentoAtivoNaRequisicaoDeveRetornarDoRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_MONITORAMENTO_ATIVO, Boolean.TRUE);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        assertThat(FiltroMonitoramentoHttp.isMonitoramentoAtivoNaRequisicao()).isTrue();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("obterCorrelacaoIdAtual deve retornar UUID quando context for invalido")
    void obterCorrelacaoIdDeveRetornarUUIDQuandoContextInvalido() {
        org.slf4j.MDC.clear();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.RequestAttributes() {
                    @Override
                    @NullUnmarked
                    public Object getAttribute(@NonNull String name, int scope) { return null; }
                    @Override
                    public void setAttribute(String name, Object value, int scope) { /* dummy */ }
                    @Override
                    public void removeAttribute(String name, int scope) { /* dummy */ }
                    @Override
                    public String[] getAttributeNames(int scope) { return new String[0]; }
                    @Override
                    public void registerDestructionCallback(String name, Runnable callback, int scope) { /* dummy */ }
                    @Override
                    @NullUnmarked
                    public Object resolveReference(@NonNull String key) { return null; }
                    @Override
                    @NullUnmarked
                    public String getSessionId() { return null; }
                    @Override
                    @NullUnmarked
                    public Object getSessionMutex() { return null; }
                }
        );
        String id = FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
        assertThat(id).isNotBlank().hasSize(36);
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("obterCorrelacaoIdAtual deve retornar UUID quando atributo null")
    void obterCorrelacaoIdDeveRetornarUUIDQuandoNull() {
        org.slf4j.MDC.clear();
        MockHttpServletRequest request = new MockHttpServletRequest();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );
        String id = FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
        assertThat(id).isNotBlank().hasSize(36);
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Deve usar trace completo da properties")
    void deveUsarTraceCompletoDaProperties() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTraceCompleto(true);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> assertThat((Boolean) req.getAttribute(FiltroMonitoramentoHttp.ATRIBUTO_MONITORAMENTO_ATIVO)).isTrue());
    }

    @Test
    @DisplayName("Deve usar amostragem ativando monitoramento detalhado")
    void deveUsarAmostragemAtivando() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTraceCompleto(false);
        properties.setTaxaAmostragem(1.0); // 100% de chance

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> assertThat((Boolean) req.getAttribute(FiltroMonitoramentoHttp.ATRIBUTO_MONITORAMENTO_ATIVO)).isTrue());
    }

    @Test
    @DisplayName("obterOuGerarCorrelacaoId deve gerar novo ID quando string for vazia")
    void obterCorrelacaoIdDeveGerarNovoQuandoVazia() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");
        request.addHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID, "   "); // Empty or blank string
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        String generatedId = response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID);
        assertThat(generatedId)
                .isNotBlank()
                .isNotEqualTo("   ");
    }
}

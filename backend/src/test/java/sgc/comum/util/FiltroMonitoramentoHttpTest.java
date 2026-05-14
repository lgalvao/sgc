package sgc.comum.util;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.jspecify.annotations.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do filtro de monitoramento HTTP")
class FiltroMonitoramentoHttpTest {

    @Test
    @DisplayName("Deve propagar correlacao")
    void devePropagarCorrelacao() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.SIM);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/processos");
        request.addHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID, "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            HttpServletRequest requestHttp = (HttpServletRequest) servletRequest;
            assertThat(requestHttp.getAttribute(FiltroMonitoramentoHttp.ATRIBUTO_CORRELACAO_ID))
                    .isEqualTo("corr-123");
        };

        filtro.doFilter(request, response, filterChain);

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isEqualTo("corr-123");
        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_TEMPO_SERVIDOR_MS)).isNotBlank();
        assertThat(response.getHeader("Server-Timing")).startsWith("app;dur=");
    }

    @Test
    @DisplayName("Deve expor descricao HTTP atual durante a requisicao")
    void deveExporDescricaoHttpAtualDuranteRequisicao() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.SIM);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/processos");
        request.setQueryString("pagina=1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
            org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                    new org.springframework.web.context.request.ServletRequestAttributes((HttpServletRequest) req)
            );
            assertThat(FiltroMonitoramentoHttp.obterDescricaoHttpAtual())
                    .isEqualTo("GET /api/processos?pagina=1");
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        });
    }

    @Test
    @DisplayName("obterDescricaoHttpAtual deve retornar sem-http quando nao houver contexto")
    void obterDescricaoHttpAtualDeveRetornarSemHttpQuandoSemContexto() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();

        assertThat(FiltroMonitoramentoHttp.obterDescricaoHttpAtual()).isEqualTo("sem-http");
    }

    @Test
    @DisplayName("Deve registrar HTTP quando passar do tempo minimo")
    @SuppressWarnings("java:S2925")
    void deveLogarErroHTTP() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.SIM);
        properties.setTempoHttpLentoMs(1);

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
    @DisplayName("Deve registrar metodo Java com prefixo")
    void deveRegistrarMetodoJavaComPrefixo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        List<String> entradas = new ArrayList<>();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_JAVA_LENTOS, entradas);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        try {
            FiltroMonitoramentoHttp.registrarJavaLento("sgc.processo.service.ProcessoService", "iniciar", 30.41);
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }

        assertThat(entradas).containsExactly("java ProcessoService.iniciar 30.41ms");
    }

    @Test
    @DisplayName("Deve ignorar requisicao fora de /api quando o filtro decidir nao aplicar")
    void deveIgnorarRequisicaoForaDeApi() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.SIM);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
            // não faz nada
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNull();
    }

    @Test
    @DisplayName("Deve ser inativo")
    void deveSerInativo() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.NAO);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/inativo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNull();
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
    @DisplayName("Deve registrar HTTP acima do limite configurado")
    @SuppressWarnings("java:S2925")
    void deveUsarLento() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.SIM);
        properties.setTempoHttpLentoMs(1);

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
    @DisplayName("obterCorrelacaoIdAtual deve retornar UUID quando context for invalido")
    void obterCorrelacaoIdDeveRetornarUUIDQuandoContextInvalido() {
        org.slf4j.MDC.clear();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.RequestAttributes() {
                    @Override
                    @NullUnmarked
                    public Object getAttribute(@NonNull String name, int scope) {
                        return null;
                    }

                    @Override
                    public void setAttribute(String name, Object value, int scope) { /* dummy */ }

                    @Override
                    public void removeAttribute(String name, int scope) { /* dummy */ }

                    @Override
                    public String[] getAttributeNames(int scope) {
                        return new String[0];
                    }

                    @Override
                    public void registerDestructionCallback(String name, Runnable callback, int scope) { /* dummy */ }

                    @Override
                    @NullUnmarked
                    public Object resolveReference(@NonNull String key) {
                        return null;
                    }

                    @Override
                    @NullUnmarked
                    public String getSessionId() {
                        return null;
                    }

                    @Override
                    @NullUnmarked
                    public Object getSessionMutex() {
                        return null;
                    }
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
    @DisplayName("obterOuGerarCorrelacaoId deve gerar novo ID quando string for vazia")
    void obterCorrelacaoIdDeveGerarNovoQuandoVazia() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.SIM);
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

    @Test
    @DisplayName("obterDescricaoHttpAtual deve retornar sem-http quando atributos não estiverem disponíveis")
    void obterDescricaoHttpAtualSemAtributos() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sem-atributos");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        assertThat(FiltroMonitoramentoHttp.obterDescricaoHttpAtual()).isEqualTo("sem-http");
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("registrarJavaLento não deve falhar sem contexto ou com atributo inválido")
    void registrarJavaLentoSemContextoOuAtributoInvalido() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        assertThatCode(() -> FiltroMonitoramentoHttp.registrarJavaLento("Classe", "metodo", 10)).doesNotThrowAnyException();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_JAVA_LENTOS, "invalido");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );
        assertThatCode(() -> FiltroMonitoramentoHttp.registrarJavaLento("Classe", "metodo", 11)).doesNotThrowAnyException();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("registrarJavaLento deve preservar nome simples para classe sem pacote ou com sufixo inválido")
    void registrarJavaLentoComNomeClasseSemPacoteOuInvalido() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        List<String> entradas = new ArrayList<>();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_JAVA_LENTOS, entradas);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        FiltroMonitoramentoHttp.registrarJavaLento("ClasseSimples", "metodoA", 1.5);
        FiltroMonitoramentoHttp.registrarJavaLento("pacote.", "metodoB", 2.5);

        assertThat(entradas).containsExactly(
                "java ClasseSimples.metodoA 1.50ms",
                "java pacote..metodoB 2.50ms"
        );
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("deve preencher tempo mesmo com status HTTP diferente de 200")
    void deveProcessarStatusHttpDiferenteDe200() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setModo(MonitoramentoProperties.Modo.SIM);
        properties.setTempoHttpLentoMs(0);
        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/erro");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> ((HttpServletResponse) res).setStatus(404));

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_TEMPO_SERVIDOR_MS)).isNotBlank();
        assertThat(response.getHeader("Server-Timing")).startsWith("app;dur=");
    }

    @Test
    @DisplayName("obterDescricaoHttpAtual deve retornar sem-http quando método/caminho forem inválidos")
    void obterDescricaoHttpAtualComMetodoOuCaminhoInvalidos() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/filtro");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_METODO, " ");
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_CAMINHO, "/api/filtro");
        assertThat(FiltroMonitoramentoHttp.obterDescricaoHttpAtual()).isEqualTo("sem-http");

        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_METODO, "GET");
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_CAMINHO, " ");
        assertThat(FiltroMonitoramentoHttp.obterDescricaoHttpAtual()).isEqualTo("sem-http");

        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_METODO, "GET");
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_CAMINHO, "/api/filtro");
        assertThat(FiltroMonitoramentoHttp.obterDescricaoHttpAtual()).isEqualTo("GET /api/filtro");

        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_METODO, 123);
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_HTTP_CAMINHO, "/api/filtro");
        assertThat(FiltroMonitoramentoHttp.obterDescricaoHttpAtual()).isEqualTo("sem-http");
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("logarJavaLento deve ignorar atributo não-lista")
    void logarJavaLentoIgnoraAtributoNaoLista() throws Exception {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        Method metodo = FiltroMonitoramentoHttp.class.getDeclaredMethod("logarJavaLento", HttpServletRequest.class);
        metodo.setAccessible(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_JAVA_LENTOS, "nao-lista");

        assertThatCode(() -> metodo.invoke(filtro, request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("logarJavaLento deve processar lista não vazia sem lançar exceção")
    void logarJavaLentoProcessaListaNaoVazia() throws Exception {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        Method metodo = FiltroMonitoramentoHttp.class.getDeclaredMethod("logarJavaLento", HttpServletRequest.class);
        metodo.setAccessible(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(FiltroMonitoramentoHttp.ATRIBUTO_JAVA_LENTOS, new ArrayList<>(List.of("java Classe.metodo 10.00ms")));

        assertThatCode(() -> metodo.invoke(filtro, request)).doesNotThrowAnyException();
    }
}

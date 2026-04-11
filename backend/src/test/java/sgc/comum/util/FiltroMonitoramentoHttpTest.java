package sgc.comum.util;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
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

        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
                HttpServletRequest requestHttp = (HttpServletRequest) servletRequest;
                assertThat(requestHttp.getAttribute(FiltroMonitoramentoHttp.ATRIBUTO_CORRELACAO_ID))
                        .isEqualTo("corr-123");
                assertThat(requestHttp.getAttribute(FiltroMonitoramentoHttp.ATRIBUTO_MONITORAMENTO_ATIVO))
                        .isEqualTo(true);
            }
        };

        filtro.doFilter(request, response, filterChain);

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isEqualTo("corr-123");
        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_TEMPO_SERVIDOR_MS)).isNotBlank();
        assertThat(response.getHeader("Server-Timing")).startsWith("app;dur=");
    }

    @Test
    @DisplayName("Deve ignorar requisicao fora de /api quando o filtro decidir nao aplicar")
    void deveIgnorarRequisicaoForaDeApi() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (_request, _response) -> {
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNull();
    }

    @Test
    @DisplayName("Deve registrar latência no header da resposta")
    void deveRegistrarLatencia() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/lento");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_TEMPO_SERVIDOR_MS)).isNotBlank();
    }

    @Test
    @DisplayName("Deve usar amostragem probabilística")
    void deveUsarAmostragemProbabilistica() throws ServletException, IOException {
        MonitoramentoProperties properties = new MonitoramentoProperties();
        properties.setAtivo(true);
        properties.setTaxaAmostragem(1.0); // 100% de amostragem

        FiltroMonitoramentoHttp filtro = new FiltroMonitoramentoHttp(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertThat(response.getHeader(FiltroMonitoramentoHttp.HEADER_CORRELACAO_ID)).isNotNull();
    }
}

package sgc.comum.web;

import jakarta.servlet.*;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.mock.web.*;
import org.springframework.web.servlet.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FrontendErrorViewResolver - Fallback da SPA")
class FrontendErrorViewResolverTest {
    private final FrontendErrorViewResolver resolver = new FrontendErrorViewResolver();

    @Test
    @DisplayName("Deve encaminhar rota simples do frontend para index")
    void deveEncaminharRotaSimplesDoFrontendParaIndex() {
        MockHttpServletRequest request = criarRequest("/login");

        ModelAndView modelAndView = resolver.resolveErrorView(request, HttpStatus.NOT_FOUND, Map.of());

        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("forward:/index.html");
        assertThat(modelAndView.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Deve encaminhar rota aninhada do frontend para index")
    void deveEncaminharRotaAninhadaDoFrontendParaIndex() {
        MockHttpServletRequest request = criarRequest("/processos/123");

        ModelAndView modelAndView = resolver.resolveErrorView(request, HttpStatus.NOT_FOUND, Map.of());

        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("forward:/index.html");
    }

    @Test
    @DisplayName("Nao deve encaminhar rota da api")
    void naoDeveEncaminharRotaDaApi() {
        MockHttpServletRequest request = criarRequest("/api/usuarios");

        ModelAndView modelAndView = resolver.resolveErrorView(request, HttpStatus.NOT_FOUND, Map.of());

        assertThat(modelAndView).isNull();
    }

    @Test
    @DisplayName("Nao deve encaminhar recurso estatico")
    void naoDeveEncaminharRecursoEstatico() {
        MockHttpServletRequest request = criarRequest("/assets/index.js");

        ModelAndView modelAndView = resolver.resolveErrorView(request, HttpStatus.NOT_FOUND, Map.of());

        assertThat(modelAndView).isNull();
    }

    private MockHttpServletRequest criarRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, uri);
        return request;
    }
}

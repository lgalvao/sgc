package sgc.seguranca.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.springframework.security.web.csrf.*;
import sgc.seguranca.config.ConfigSeguranca.*;

import static org.mockito.Mockito.*;

@DisplayName("FiltroMaterializacaoCsrf")
class FiltroMaterializacaoCsrfTest {

    @Test
    @DisplayName("deve materializar token CSRF quando disponível")
    void deveMaterializarTokenCsrfQuandoDisponivel() throws Exception {
        FiltroMaterializacaoCsrf filtro = new FiltroMaterializacaoCsrf();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        CsrfToken csrfToken = mock(CsrfToken.class);

        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        filtro.doFilterInternal(request, response, filterChain);

        verify(csrfToken).getToken();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("deve ignorar quando token CSRF for nulo")
    void deveIgnorarQuandoTokenCsrfForNulo() throws Exception {
        FiltroMaterializacaoCsrf filtro = new FiltroMaterializacaoCsrf();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}

package sgc.comum.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.boot.webmvc.autoconfigure.error.*;
import org.springframework.web.servlet.*;

import java.util.*;

@Component
public class FrontendErrorViewResolver implements ErrorViewResolver {
    private static final List<String> PREFIXOS_EXCLUIDOS = List.of(
            "/api",
            "/actuator",
            "/assets",
            "/swagger-ui",
            "/v3"
    );

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request,
                                         HttpStatus status,
                                         Map<String, Object> model) {
        if (status != HttpStatus.NOT_FOUND || !"GET".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String uri = obterUriComErro(request);
        if (uri == null || uri.isBlank() || uri.contains(".") || PREFIXOS_EXCLUIDOS.stream().anyMatch(uri::startsWith)) {
            return null;
        }

        return new ModelAndView("forward:/index.html", Collections.emptyMap(), HttpStatus.OK);
    }

    private String obterUriComErro(HttpServletRequest request) {
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (uri instanceof String uriString) {
            return uriString;
        }
        return request.getRequestURI();
    }
}

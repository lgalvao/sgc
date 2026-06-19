package sgc.comum.erros;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.seguranca.SgcPermissionEvaluator;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestSecurityController.class)
@Import({TestSecurityConfig.class, RestExceptionHandler.class})
@DisplayName("Testes do Manipulador de Exceções REST")
class RestExceptionHandlerTest {
    private static final String JSON_VALIDO_TESTE = "{\"dadoSensivel\":\"valor-seguro\"}";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Autowired
    private RestExceptionHandler restExceptionHandler;

    @MockitoBean
    private TestSecurityController controller;

    @Test
    @DisplayName("Deve tratar ErroValidacao (422)")
    void deveTratarErroValidacao() throws Exception {
        Mockito.doThrow(new ErroValidacao("Erro validação negócio"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Erro validação negócio"));
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException (403)")
    void deveTratarAccessDeniedException() throws Exception {
        Mockito.doThrow(new AccessDeniedException("Acesso negado"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    @DisplayName("Deve tratar ErroAutenticacao (401)")
    void deveTratarErroAutenticacao() throws Exception {
        Mockito.doThrow(new ErroAutenticacao("Falha autenticação"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Falha autenticação"));
    }

    @Test
    @DisplayName("Deve sanitizar ErroAutenticacao (401)")
    void deveSanitizarErroAutenticacao() throws Exception {
        Mockito.doThrow(new ErroAutenticacao("<script>alert('x')</script>Falha"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Falha"));
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException remanescente como erro interno (500)")
    void deveTratarIllegalStateExceptionComoErroInterno() throws Exception {
        Mockito.doThrow(new IllegalStateException("Estado inválido"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro inesperado."))
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"));
    }

    @Test
    @DisplayName("Deve tratar ErroInterno (500)")
    void deveTratarErroInterno() throws Exception {
        Mockito.doThrow(new ErroInterno("Falha interna") {
                })
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno do sistema."))
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException remanescente como erro interno (500)")
    void deveTratarIllegalArgumentExceptionComoErroInterno() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Argumento inválido"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro inesperado."))
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"));
    }

    @Test
    @DisplayName("Deve tratar Exception genérica (500)")
    void deveTratarExceptionGenerica() throws Exception {
        Mockito.doThrow(new RuntimeException("Erro inesperado"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro inesperado."));
    }

    @Test
    @DisplayName("Deve tratar ConstraintViolationException (400)")
    void deveTratarConstraintViolationException() throws Exception {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(violation.getMessage()).thenReturn("Violação de constraint");
        Mockito.doReturn(Object.class).when(violation).getRootBeanClass();
        Mockito.when(violation.getPropertyPath()).thenAnswer(i -> Mockito.mock(Path.class));

        Mockito.doThrow(new ConstraintViolationException("Erro constraint", Set.of(violation)))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A requisição contém dados inválidos."));
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException (400)")
    void deveTratarHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON Error", Mockito.mock(HttpInputMessage.class));
        ResponseEntity<Object> response = restExceptionHandler.handleHttpMessageNotReadable(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                Mockito.mock(WebRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(((ErroApi) response.getBody()).getMessage()).isEqualTo("Requisição JSON malformada");
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException (400)")
    void deveTratarMethodArgumentNotValidException() throws Exception {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError error = new FieldError("obj", "field", "defaultMessage");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        MethodParameter methodParameter = Mockito.mock(MethodParameter.class);
        Mockito.when(methodParameter.getExecutable()).thenReturn(Object.class.getMethod("toString"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = restExceptionHandler.handleMethodArgumentNotValid(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                Mockito.mock(WebRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(((ErroApi) response.getBody()).getMessage()).isEqualTo("A requisição contém dados de entrada inválidos.");
    }

    @Test
    @DisplayName("Deve sanitizar mensagens em MethodArgumentNotValidException")
    void deveSanitizarMethodArgumentNotValidException() throws Exception {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError error = new FieldError("obj", "field", "<b>mensagem</b>");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        MethodParameter methodParameter = Mockito.mock(MethodParameter.class);
        Mockito.when(methodParameter.getExecutable()).thenReturn(Object.class.getMethod("toString"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = restExceptionHandler.handleMethodArgumentNotValid(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                Mockito.mock(WebRequest.class));

        assertThat(Objects.requireNonNull(response).getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi -> {
                    assertThat(erroApi.getErros()).hasSize(1);
                    assertThat(Objects.requireNonNull(erroApi.getErros()).getFirst().mensagem()).isEqualTo("mensagem");
                });
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase genérico (400)")
    void deveTratarErroNegocioBase() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro base", "CODE", HttpStatus.BAD_REQUEST) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi -> {
                    assertThat(erroApi.getMessage()).isEqualTo("Erro base");
                    assertThat(erroApi.getCode()).isEqualTo("CODE");
                });
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase com detalhes")
    void deveTratarErroNegocioBaseComDetalhes() {
        Map<String, String> details = Map.of("campo", "erro");
        ErroNegocioBase ex = new ErroNegocioBase("Erro com detalhe", "CODE", HttpStatus.BAD_REQUEST, details) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi -> {
                    assertThat(erroApi.getDetails()).isEqualTo(details);
                    assertThat(erroApi.getMessage()).isEqualTo("Erro com detalhe");
                });
    }

    @Test
    @DisplayName("Deve sanitizar mensagem de ErroNegocioBase")
    void deveSanitizarMensagemDeErroNegocioBase() {
        ErroNegocioBase ex = new ErroNegocioBase("<script>erro</script>", "CODE", HttpStatus.BAD_REQUEST) {
        };

        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);

        assertThat(response.getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi ->
                        assertThat(erroApi.getMessage()).isEmpty());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase com lista de detalhes vazia")
    void deveTratarErroNegocioBaseComDetalhesVazio() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro detalhe vazio", "CODE", HttpStatus.BAD_REQUEST, Collections.emptyMap()) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi -> assertThat(erroApi.getDetails()).isEmpty());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase genérico (500)")
    void deveTratarErroNegocioBase500() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro server", "CODE", HttpStatus.INTERNAL_SERVER_ERROR) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi -> {
                    assertThat(erroApi.getMessage()).isEqualTo("Erro server");
                    assertThat(erroApi.getCode()).isEqualTo("CODE");
                });
    }

    private HttpMessageNotWritableException criarExcecaoEscrita() {
        return new HttpMessageNotWritableException("Erro de escrita");
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotWritableException com resposta padronizada")
    void deveTratarHttpMessageNotWritableException() {
        WebRequest mockRequest = Mockito.mock(WebRequest.class);

        ResponseEntity<Object> response = restExceptionHandler.handleHttpMessageNotWritable(
                criarExcecaoEscrita(), null, HttpStatus.INTERNAL_SERVER_ERROR, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi -> {
                    assertThat(erroApi.getMessage()).isEqualTo("Erro inesperado ao processar a resposta.");
                    assertThat(erroApi.getCode()).isEqualTo("ERRO_SERIALIZACAO");
                });
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotWritableException com causa raiz e contexto ServletWebRequest")
    void deveTratarHttpMessageNotWritableComCausaRaizEContextoServlet() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/teste");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        IllegalArgumentException causaRaiz = new IllegalArgumentException("raiz");
        RuntimeException causaIntermediaria = new RuntimeException("intermediaria", causaRaiz);
        HttpMessageNotWritableException ex = new HttpMessageNotWritableException("erro escrita", causaIntermediaria);

        ResponseEntity<Object> retorno = restExceptionHandler.handleHttpMessageNotWritable(
                ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, webRequest);

        assertThat(retorno.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(retorno.getBody()).isInstanceOf(ErroApi.class);
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotWritableException com causa cíclica sem loop infinito")
    void deveTratarHttpMessageNotWritableComCausaCiclica() {
        class CausaCiclica extends RuntimeException {
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        }
        HttpMessageNotWritableException ex = new HttpMessageNotWritableException("erro", new CausaCiclica());

        ResponseEntity<Object> retorno = restExceptionHandler.handleHttpMessageNotWritable(
                ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, Mockito.mock(WebRequest.class));

        assertThat(retorno.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(retorno.getBody()).isInstanceOf(ErroApi.class);
    }

    @Test
    @DisplayName("Deve tratar Exception genérica sem mensagem com erro padrão")
    void deveTratarExceptionGenericaSemMensagem() {
        Exception ex = new NullPointerException();

        ResponseEntity<ErroApi> response = restExceptionHandler.handleGenericException(ex);
        ErroApi corpo = Objects.requireNonNull(response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(corpo.getMessage()).isEqualTo("Erro inesperado.");
        assertThat(corpo.getCode()).isEqualTo("ERRO_INTERNO");
    }

    @Test
    @DisplayName("Deve encaminhar rota da SPA para index ao tratar NoResourceFoundException")
    void deveEncaminharRotaDaSpaParaIndexAoTratarNoResourceFoundException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/painel");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/painel", "painel");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertThat(resultado).isNull();
        assertThat(response.getForwardedUrl()).isEqualTo("/index.html");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Nao deve encaminhar rota da API ao tratar NoResourceFoundException")
    void naoDeveEncaminharRotaDaApiAoTratarNoResourceFoundException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/inexistente");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/inexistente", "/api/inexistente");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertThat(resultado).isNotNull();
        assertThat(response.getForwardedUrl()).isNull();
    }

    @Test
    @DisplayName("Nao deve encaminhar recurso estatico ao tratar NoResourceFoundException")
    void naoDeveEncaminharRecursoEstaticoAoTratarNoResourceFoundException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/assets/inexistente.js");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/assets/inexistente.js", "assets/inexistente.js");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertThat(resultado).isNotNull();
        assertThat(response.getForwardedUrl()).isNull();
    }

    @Test
    @DisplayName("Nao deve encaminhar rota SPA quando método não for GET")
    void naoDeveEncaminharQuandoMetodoNaoEhGet() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/painel");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.POST, "/painel", "painel");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertThat(resultado).isNotNull();
        assertThat(response.getForwardedUrl()).isNull();
    }

    @Test
    @DisplayName("Nao deve encaminhar rota SPA quando caminho estiver em branco")
    void naoDeveEncaminharQuandoCaminhoEmBranco() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "", "");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Nao deve encaminhar rota SPA quando resposta estiver ausente")
    void naoDeveEncaminharQuandoRespostaAusente() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/painel");
        ServletWebRequest webRequest = new ServletWebRequest(request);
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/painel", "painel");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Nao deve encaminhar rota SPA quando forward para index falhar")
    void naoDeveEncaminharQuandoForwardFalhar() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = Mockito.mock(RequestDispatcher.class);
        Mockito.when(request.getMethod()).thenReturn("GET");
        Mockito.when(request.getRequestURI()).thenReturn("/painel");
        Mockito.when(request.getRequestDispatcher("/index.html")).thenReturn(dispatcher);
        Mockito.doThrow(new ServletException("falha")).when(dispatcher).forward(request, response);

        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/painel", "painel");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Nao deve tentar encaminhar quando WebRequest não é ServletWebRequest")
    void naoDeveEncaminharQuandoWebRequestNaoEhServlet() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/painel", "painel");

        ResponseEntity<Object> resultado = restExceptionHandler.handleNoResourceFoundException(
                ex, new HttpHeaders(), HttpStatus.NOT_FOUND, Mockito.mock(WebRequest.class));

        assertThat(resultado).isNotNull();
    }
}

package sgc.comum.erros;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.security.access.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.*;
import org.springframework.validation.*;
import org.springframework.web.bind.*;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.resource.*;
import sgc.integracao.mocks.*;
import sgc.seguranca.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("Deve tratar IllegalStateException (409)")
    void deveTratarIllegalStateException() throws Exception {
        Mockito.doThrow(new IllegalStateException("Estado inválido"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Operação inválida para o estado atual."));
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
    @DisplayName("Deve tratar IllegalArgumentException (400)")
    void deveTratarIllegalArgumentException() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Argumento inválido"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO_TESTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos fornecidos na requisição."));
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
                .andExpect(jsonPath("$.message").value("Erro inesperado. Consulte o suporte com o código de rastreamento."));
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
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Requisição JSON malformada", ((ErroApi) response.getBody()).getMessage());
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
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("A requisição contém dados de entrada inválidos.", ((ErroApi) response.getBody()).getMessage());
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
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro base", ((ErroApi) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase com detalhes")
    void deveTratarErroNegocioBaseComDetalhes() {
        Map<String, String> details = Map.of("campo", "erro");
        ErroNegocioBase ex = new ErroNegocioBase("Erro com detalhe", "CODE", HttpStatus.BAD_REQUEST, details) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(details, ((ErroApi) response.getBody()).getDetails());
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
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat(response.getBody())
                .isInstanceOfSatisfying(ErroApi.class, erroApi -> assertThat(erroApi.getDetails()).isEmpty());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase genérico (500)")
    void deveTratarErroNegocioBase500() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro server", "CODE", HttpStatus.INTERNAL_SERVER_ERROR) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private HttpMessageNotWritableException criarExcecaoEscrita() {
        return new HttpMessageNotWritableException("Erro de escrita");
    }

    @Test
    @DisplayName("Deve cobrir descreverRequisicao quando não for ServletWebRequest")
    void deveCobrirDescreverRequisicaoNaoServlet() {
        WebRequest mockRequest = Mockito.mock(WebRequest.class);

        ResponseEntity<Object> response = restExceptionHandler.handleHttpMessageNotWritable(
                criarExcecaoEscrita(), null, HttpStatus.INTERNAL_SERVER_ERROR, mockRequest);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Deve cobrir descreverRequisicao quando for ServletWebRequest")
    void deveCobrirDescreverRequisicaoServlet() {
        ServletWebRequest mockRequest = Mockito.mock(ServletWebRequest.class);
        jakarta.servlet.http.HttpServletRequest mockHttpRequest = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);

        Mockito.when(mockRequest.getRequest()).thenReturn(mockHttpRequest);
        Mockito.when(mockHttpRequest.getMethod()).thenReturn("POST");
        Mockito.when(mockHttpRequest.getRequestURI()).thenReturn("/api/teste");

        restExceptionHandler.handleHttpMessageNotWritable(criarExcecaoEscrita(), null, HttpStatus.INTERNAL_SERVER_ERROR, mockRequest);

        Mockito.verify(mockRequest).getRequest();
    }

    @Test
    @DisplayName("Deve cobrir obterCausaRaiz com múltiplas causas")
    void deveCobrirObterCausaRaizMultiplasCausas() {
        Exception causaRaiz = new RuntimeException("Raiz");
        Exception causaMeio = new RuntimeException("Meio", causaRaiz);
        Exception ex = new RuntimeException("Topo", causaMeio);
        WebRequest request = Mockito.mock(WebRequest.class);

        ResponseEntity<Object> response = restExceptionHandler.handleHttpMessageNotWritable(
                new HttpMessageNotWritableException("Erro", ex),
                null, HttpStatus.INTERNAL_SERVER_ERROR, request);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Deve cobrir handleErroNegocio com erro 4xx e detalhes")
    @SuppressWarnings("unchecked")
    void deveCobrirHandleErroNegocio4xxComDetalhes() {
        Map<String, String> details = new HashMap<>();
        details.put("campo", "erro no campo");

        ErroNegocioBase ex = new ErroNegocioBase("Mensagem Erro", "COD_400", HttpStatus.BAD_REQUEST, details) {
        };

        ResponseEntity<ErroApi> response = restExceptionHandler.handleErroNegocio(ex);
        ErroApi corpo = Objects.requireNonNull(response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(corpo.getMessage()).isEqualTo("Mensagem Erro");

        Map<String, Object> responseDetails = (Map<String, Object>) corpo.getDetails();
        assertThat(responseDetails).containsEntry("campo", "erro no campo");
    }

    @Test
    @DisplayName("Deve cobrir handleErroNegocio com erro 5xx")
    void deveCobrirHandleErroNegocio5xx() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro Crítico", "COD_500", HttpStatus.INTERNAL_SERVER_ERROR) {
        };

        ResponseEntity<ErroApi> response = restExceptionHandler.handleErroNegocio(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Deve cobrir handleGenericException com mensagem nula")
    void deveCobrirHandleGenericExceptionMensagemNula() {
        Exception ex = new NullPointerException();

        ResponseEntity<ErroApi> response = restExceptionHandler.handleGenericException(ex);
        ErroApi corpo = Objects.requireNonNull(response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(corpo.getMessage()).isEqualTo("Erro inesperado. Consulte o suporte com o código de rastreamento.");
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
}

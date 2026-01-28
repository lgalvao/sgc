package sgc.comum.erros;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import sgc.integracao.mocks.TestSecurityConfig;

@Tag("unit")
@WebMvcTest(TestSecurityController.class)
@Import({TestSecurityConfig.class, RestExceptionHandler.class})
@DisplayName("Testes do Manipulador de Exceções REST")
class RestExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\": \"valor\"}"))
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\": \"valor\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    @DisplayName("Deve tratar ErroAutenticacao (401)")
    void deveTratarErroAutenticacao() throws Exception {
        Mockito.doThrow(new ErroAutenticacao("Falha autenticação"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\": \"valor\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Falha autenticação"));
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException (409)")
    void deveTratarIllegalStateException() throws Exception {
        Mockito.doThrow(new IllegalStateException("Estado inválido"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\": \"valor\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A operação não pode ser executada no estado atual do recurso."));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException (400)")
    void deveTratarIllegalArgumentException() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Argumento inválido"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\": \"valor\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A requisição contém um argumento inválido ou malformado."));
    }

    @Test
    @DisplayName("Deve tratar Exception genérica (500)")
    void deveTratarExceptionGenerica() throws Exception {
        Mockito.doThrow(new RuntimeException("Erro inesperado"))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\": \"valor\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro inesperado"));
    }

    @Test
    @DisplayName("Deve tratar ConstraintViolationException (400)")
    void deveTratarConstraintViolationException() throws Exception {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(violation.getMessage()).thenReturn("Violação de constraint");
        Mockito.doReturn(Object.class).when(violation).getRootBeanClass();
        Mockito.when(violation.getPropertyPath()).thenAnswer(i -> Mockito.mock(jakarta.validation.Path.class));

        Mockito.doThrow(new ConstraintViolationException("Erro constraint", Set.of(violation)))
                .when(controller).teste(any());

        mockMvc.perform(post("/test/validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\": \"valor\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A requisição contém dados inválidos."));
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException (400)")
    void deveTratarHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON Error", Mockito.mock(org.springframework.http.HttpInputMessage.class));
        ResponseEntity<Object> response = restExceptionHandler.handleHttpMessageNotReadable(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Requisição JSON malformada", ((ErroApi) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException (400)")
    void deveTratarMethodArgumentNotValidException() throws Exception {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError error = new FieldError("obj", "field", "defaultMessage");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        org.springframework.core.MethodParameter methodParameter = Mockito.mock(org.springframework.core.MethodParameter.class);
        Mockito.when(methodParameter.getExecutable()).thenReturn(Object.class.getMethod("toString"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = restExceptionHandler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("A requisição contém dados de entrada inválidos.", ((ErroApi) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase genérico (400)")
    void deveTratarErroNegocioBase() {
        // ErroNegocioBase is abstract or base, let's use a subclass or anonymous
        ErroNegocioBase ex = new ErroNegocioBase("Erro Base", "CODE", HttpStatus.BAD_REQUEST) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erro Base", ((ErroApi) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase genérico (500)")
    void deveTratarErroNegocioBase500() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro Server", "CODE", HttpStatus.INTERNAL_SERVER_ERROR) {
        };
        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar ErroInterno (500)")
    void deveTratarErroInterno() {
        ErroEstadoImpossivel ex = new ErroEstadoImpossivel("Bug crítico");
        ResponseEntity<?> response = restExceptionHandler.handleErroInterno(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertThat(((ErroApi) response.getBody()).getMessage()).contains("Erro interno do sistema");
    }

    @Test
    @DisplayName("Deve tratar casos nulos nos handlers da Spring")
    void deveTratarCasosNulos() {
        ResponseEntity<Object> r1 = restExceptionHandler.handleHttpMessageNotReadable(null, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);
        assertEquals(HttpStatus.BAD_REQUEST, r1.getStatusCode());

        ResponseEntity<Object> r2 = restExceptionHandler.handleMethodArgumentNotValid(null, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);
        assertEquals(HttpStatus.BAD_REQUEST, r2.getStatusCode());
    }

    @Test
    @DisplayName("Deve incluir detalhes no ErroApi")
    void deveIncluirDetalhesNoErroApi() {
        Map<String, String> details = Map.of("key", "value");
        ErroNegocioBase ex = new ErroNegocioBase("Com Detalhes", "DET", HttpStatus.BAD_REQUEST, details) {};

        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);

        ErroApi body = (ErroApi) response.getBody();
        assertThat(body.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase com detalhes vazios")
    void deveTratarErroNegocioBaseComDetalhesVazios() {
        Map<String, String> details = Collections.emptyMap();
        ErroNegocioBase ex = new ErroNegocioBase("Detalhes Vazios", "DET_VAZIO", HttpStatus.BAD_REQUEST, details) {};

        ResponseEntity<?> response = restExceptionHandler.handleErroNegocio(ex);

        ErroApi body = (ErroApi) response.getBody();
        assertThat(body.getDetails()).isNullOrEmpty();
    }
}

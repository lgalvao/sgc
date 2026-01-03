package sgc.comum.erros;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import sgc.integracao.mocks.TestSecurityConfig;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(status().isUnprocessableEntity())
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
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(violation.getMessage()).thenReturn("Violação de constraint");
        Mockito.when(violation.getRootBeanClass()).thenAnswer(i -> Object.class);
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
    void deveTratarMethodArgumentNotValidException() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError error = new FieldError("obj", "field", "defaultMessage");
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        org.springframework.core.MethodParameter methodParameter = Mockito.mock(org.springframework.core.MethodParameter.class);
        Mockito.when(methodParameter.getExecutable()).thenReturn(new Object() { public String toGenericString() { return ""; } }.getClass().getEnclosingMethod());
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = restExceptionHandler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("A requisição contém dados de entrada inválidos.", ((ErroApi) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase genérico (400)")
    void deveTratarErroNegocioBase() {
        // ErroNegocioBase is abstract or base, let's use a subclass or anonymous
        ErroNegocioBase ex = new ErroNegocioBase("Erro Base", "CODE", HttpStatus.BAD_REQUEST) {};
        ResponseEntity<Object> response = restExceptionHandler.handleErroNegocio(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erro Base", ((ErroApi) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase genérico (500)")
    void deveTratarErroNegocioBase500() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro Server", "CODE", HttpStatus.INTERNAL_SERVER_ERROR) {};
        ResponseEntity<Object> response = restExceptionHandler.handleErroNegocio(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}

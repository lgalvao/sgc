package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import org.springframework.web.servlet.mvc.method.annotation.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.seguranca.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventosController.class)
@Import(TestSecurityConfig.class)
class EventosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistroSseEmitter registroSseEmitter;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("deve registrar e retornar emissor SSE para usuário autenticado")
    void deveAssinarSse() throws Exception {
        SseEmitter emitter = new SseEmitter();
        when(registroSseEmitter.registrar()).thenReturn(emitter);

        Usuario usuarioMock = Usuario.builder().tituloEleitoral("12345678901").build();

        mockMvc.perform(get("/api/eventos")
                        .with(user(usuarioMock))
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        verify(registroSseEmitter).registrar();
    }

    @Test
    @DisplayName("deve retornar 401 para acesso não autenticado")
    @WithAnonymousUser
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/eventos")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isUnauthorized());
    }
}

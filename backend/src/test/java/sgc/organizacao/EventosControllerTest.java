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
import sgc.organizacao.service.*;
import sgc.seguranca.config.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventosController.class)
@Import(ConfigSeguranca.class)
class EventosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistroSseEmitter registroSseEmitter;

    @Test
    @DisplayName("deve registrar e retornar emissor SSE")
    @WithMockUser
    void deveAssinarSse() throws Exception {
        SseEmitter emitter = new SseEmitter();
        when(registroSseEmitter.registrar()).thenReturn(emitter);

        mockMvc.perform(get("/api/eventos")
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        verify(registroSseEmitter).registrar();
    }
}

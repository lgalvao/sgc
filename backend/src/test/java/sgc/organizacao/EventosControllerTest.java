package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sgc.comum.cache.RegistroSseEmitter;
import sgc.seguranca.config.ConfigSeguranca;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

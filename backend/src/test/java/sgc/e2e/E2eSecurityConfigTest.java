package sgc.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
class E2eSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("deve exigir autenticação para assinar eventos SSE")
    void deveExigirAutenticacaoParaAssinarEventos() throws Exception {
        mockMvc.perform(get("/api/eventos")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isUnauthorized());
    }
}

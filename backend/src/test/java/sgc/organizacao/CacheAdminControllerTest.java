package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.organizacao.service.*;
import sgc.seguranca.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CacheAdminController.class)
@EnableMethodSecurity
class CacheAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendadorRefreshCache agendadorRefreshCache;

    @MockitoBean
    private RegistroSseEmitter registroSseEmitter;

    @MockitoBean
    private SgcPermissionEvaluator sgcPermissionEvaluator;

    @Test
    @DisplayName("deve evictar tudo se o usuario tem ROLE_ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deveEvictarTudoRoleAdmin() throws Exception {
        mockMvc.perform(post("/api/cache/evict").with(csrf()))
                .andExpect(status().isNoContent());

        verify(agendadorRefreshCache).evictarTodosCaches();
        verify(agendadorRefreshCache).recarregarCaches();
        verify(registroSseEmitter).transmitir("org-cache-refreshed");
    }

    @Test
    @DisplayName("nao deve evictar tudo se o usuario nao tem ROLE_ADMIN")
    @WithMockUser(roles = "USER")
    void naoDeveEvictarTudoSemRoleAdmin() throws Exception {
        mockMvc.perform(post("/api/cache/evict").with(csrf()))
                .andExpect(status().isForbidden());
    }
}

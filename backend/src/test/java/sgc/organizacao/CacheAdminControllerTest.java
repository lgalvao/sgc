package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import sgc.comum.cache.AgendadorRefreshCache;
import sgc.comum.cache.RegistroSseEmitter;
import sgc.seguranca.SgcPermissionEvaluator;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

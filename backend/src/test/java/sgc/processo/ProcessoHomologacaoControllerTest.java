package sgc.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.RestExceptionHandler;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.service.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessoHomologacaoController.class)
@Import({RestExceptionHandler.class, TestSecurityConfig.class})
@ActiveProfiles({"test", "hom"})
@DisplayName("ProcessoHomologacaoController")
class ProcessoHomologacaoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessoExclusaoCompletaService processoExclusaoCompletaService;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("POST /api/processos/{codigo}/excluir-completo - deve permitir ADMIN")
    void devePermitirAdmin() throws Exception {
        Usuario usuario = Usuario.builder()
                .tituloEleitoral("123")
                .perfilAtivo(Perfil.ADMIN)
                .authorities(Set.of(Perfil.ADMIN.toGrantedAuthority()))
                .build();

        mockMvc.perform(post("/api/processos/10/excluir-completo")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(processoExclusaoCompletaService).excluirCompleto(10L);
    }

    @Test
    @DisplayName("POST /api/processos/{codigo}/excluir-completo - deve negar perfil nao administrador")
    void deveNegarPerfilNaoAdministrador() throws Exception {
        Usuario usuario = Usuario.builder()
                .tituloEleitoral("123")
                .perfilAtivo(Perfil.CHEFE)
                .authorities(Set.of(Perfil.CHEFE.toGrantedAuthority()))
                .build();

        mockMvc.perform(post("/api/processos/10/excluir-completo")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(processoExclusaoCompletaService);
    }
}

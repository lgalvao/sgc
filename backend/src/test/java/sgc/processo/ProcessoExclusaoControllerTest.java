package sgc.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.processo.service.ProcessoExclusaoCompletaService;
import sgc.seguranca.SgcPermissionEvaluator;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessoExclusaoController.class)
@Import({RestExceptionHandler.class, TestSecurityConfig.class})
@ActiveProfiles({"test", "hom"})
@DisplayName("ProcessoExclusaoController")
class ProcessoExclusaoControllerTest {
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

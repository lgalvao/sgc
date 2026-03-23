package sgc.processo.painel;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.alerta.model.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PainelController.class)
@Import(TestSecurityConfig.class)
@DisplayName("PainelController - Testes de Controller")
class PainelControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @MockitoBean
    private PainelFacade painelFacade;

    @Test
    @DisplayName("GET /api/painel/processos - Deve listar processos com sucesso usando contexto do Token")
    void listarProcessos_Sucesso() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .perfilAtivo(Perfil.ADMIN)
                .unidadeAtivaCodigo(1L)
                .build();
        usuarioMock.setAuthorities(Set.of(Perfil.ADMIN.toGrantedAuthority()));

        Page<ProcessoResumoDto> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarProcessos(eq(Perfil.ADMIN), eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/processos")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/painel/alertas - Deve listar alertas com sucesso usando contexto do Token")
    void listarAlertas_Sucesso() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .unidadeAtivaCodigo(1L)
                .perfilAtivo(Perfil.ADMIN)
                .authorities(Set.of(Perfil.ADMIN.toGrantedAuthority()))
                .build();

        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarAlertas(eq("123"), eq(1L), eq("ADMIN"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/alertas")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/painel/alertas - Deve ignorar parâmetros de usuário/unidade da URL e usar o Token")
    void listarAlertas_IgnoraParametrosUrl() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .unidadeAtivaCodigo(1L)
                .perfilAtivo(Perfil.ADMIN)
                .authorities(Set.of(Perfil.ADMIN.toGrantedAuthority()))
                .build();

        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        // O Mock espera os dados do TOKEN (123 e 1L), ignorando os lixos passados na URL
        when(painelFacade.listarAlertas(eq("123"), eq(1L), eq("ADMIN"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/alertas")
                        .param("usuarioTitulo", "ATAQUE_IDOR")
                        .param("unidade", "999")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(painelFacade).listarAlertas(eq("123"), eq(1L), eq("ADMIN"), any(Pageable.class));
    }

}

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
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.painel.dto.PainelBootstrapDto;
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
    @MockitoBean
    private UsuarioFacade usuarioFacade;

    @Test
    @DisplayName("GET /api/painel/processos - Deve listar processos com sucesso usando contexto do Token")
    void listarProcessos_Sucesso() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .perfilAtivo(Perfil.ADMIN)
                .unidadeAtivaCodigo(1L)
                .build();
        usuarioMock.setAuthorities(Set.of(Perfil.ADMIN.toGrantedAuthority()));
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        Page<ProcessoResumoDto> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarProcessos(any(ContextoUsuarioAutenticado.class), any(Pageable.class))).thenReturn(page);

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
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarAlertas(any(ContextoUsuarioAutenticado.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/alertas")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/painel/bootstrap - Deve obter dados de bootstrap com sucesso usando contexto do Token")
    void obterBootstrap_Sucesso() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .perfilAtivo(Perfil.ADMIN)
                .unidadeAtivaCodigo(1L)
                .build();
        usuarioMock.setAuthorities(Set.of(Perfil.ADMIN.toGrantedAuthority()));
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        sgc.processo.painel.dto.PainelBootstrapDto dto = sgc.processo.painel.dto.PainelBootstrapDto.builder()
                .processos(Collections.emptyList())
                .alertas(Collections.emptyList())
                .build();
        when(painelFacade.obterBootstrap(any(ContextoUsuarioAutenticado.class))).thenReturn(dto);

        mockMvc.perform(get("/api/painel/bootstrap")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processos").isArray())
                .andExpect(jsonPath("$.alertas").isArray());
    }

    @Test
    @DisplayName("POST /api/painel/alertas/marcar-lidos - Deve marcar alertas como lidos com sucesso")
    void marcarAlertasLidos_Sucesso() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .perfilAtivo(Perfil.ADMIN)
                .unidadeAtivaCodigo(1L)
                .build();
        usuarioMock.setAuthorities(Set.of(Perfil.ADMIN.toGrantedAuthority()));
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        mockMvc.perform(post("/api/painel/alertas/marcar-lidos")
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isNoContent());

        verify(painelFacade).marcarAlertasLidos(any(ContextoUsuarioAutenticado.class), eq(List.of(1L, 2L, 3L)));
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
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        when(painelFacade.listarAlertas(any(ContextoUsuarioAutenticado.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/alertas")
                        .param("usuarioTitulo", "ATAQUE_IDOR")
                        .param("unidade", "999")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(painelFacade).listarAlertas(any(ContextoUsuarioAutenticado.class), any(Pageable.class));
    }

}

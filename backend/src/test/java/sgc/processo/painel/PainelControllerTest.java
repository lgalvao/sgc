package sgc.processo.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.alerta.AlertaDtoMapper;
import sgc.alerta.model.Alerta;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.seguranca.SgcPermissionEvaluator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PainelController.class)
@Import({TestSecurityConfig.class, AlertaDtoMapper.class})
@DisplayName("PainelController - Testes de Controller")
class PainelControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @MockitoBean
    private PainelService painelService;
    @MockitoBean
    private UsuarioAplicacaoService usuarioAplicacaoService;

    @Test
    @DisplayName("GET /api/painel/processos - Deve listar processos com sucesso usando contexto do Token")
    void listarProcessos_Sucesso() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .perfilAtivo(Perfil.ADMIN)
                .unidadeAtivaCodigo(1L)
                .build();
        usuarioMock.setAuthorities(Set.of(Perfil.ADMIN.toGrantedAuthority()));
        when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        Page<ProcessoResumoDto> page = new PageImpl<>(Collections.emptyList());
        when(painelService.listarProcessos(any(ContextoUsuarioAutenticado.class), any(Pageable.class))).thenReturn(page);

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
        when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        when(painelService.listarAlertas(any(ContextoUsuarioAutenticado.class), any(Pageable.class))).thenReturn(page);

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
        when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        sgc.processo.painel.dto.PainelBootstrapDto dto = sgc.processo.painel.dto.PainelBootstrapDto.builder()
                .processos(Collections.emptyList())
                .alertas(Collections.emptyList())
                .build();
        when(painelService.obterBootstrap(any(ContextoUsuarioAutenticado.class))).thenReturn(dto);

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
        when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        mockMvc.perform(post("/api/painel/alertas/marcar-lidos")
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isNoContent());

        verify(painelService).marcarAlertasLidos(any(ContextoUsuarioAutenticado.class), eq(List.of(1L, 2L, 3L)));
    }

    @Test
    @DisplayName("GET /api/painel/alertas - Deve ignorar Configuraçãos de usuário/unidade da URL e usar o Token")
    void listarAlertas_IgnoraParametrosUrl() throws Exception {
        Usuario usuarioMock = Usuario.builder()
                .tituloEleitoral("123")
                .unidadeAtivaCodigo(1L)
                .perfilAtivo(Perfil.ADMIN)
                .authorities(Set.of(Perfil.ADMIN.toGrantedAuthority()))
                .build();
        when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("123", 1L, Perfil.ADMIN));

        Page<Alerta> page = new PageImpl<>(Collections.emptyList());
        when(painelService.listarAlertas(any(ContextoUsuarioAutenticado.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/painel/alertas")
                        .param("usuarioTitulo", "ATAQUE_IDOR")
                        .param("unidade", "999")
                        .with(authentication(new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(painelService).listarAlertas(any(ContextoUsuarioAutenticado.class), any(Pageable.class));
    }

}

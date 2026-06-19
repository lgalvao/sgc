package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.SgcPermissionEvaluator;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertaController.class)
@Import({TestSecurityConfig.class, AlertaDtoMapper.class})
@DisplayName("AlertaController")
class AlertaControllerTest {

    private static final String TITULO_TESTE = "12345678901"; // 11 ou 12 dígitos
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;
    @MockitoBean
    private AlertaAplicacaoService alertaAplicacaoService;
    @MockitoBean
    private UsuarioAplicacaoService usuarioAplicacaoService;

    @Nested
    @DisplayName("Listar alertas")
    class ListarAlertas {
        @Test
        @DisplayName("Deve retornar lista de alertas com sucesso usando o contexto do JWT (unidade e titulo)")
        void listarAlertas_quandoSucesso_deveRetornarListaDeAlertas() throws Exception {
            Usuario usuarioMock = Usuario.builder()
                    .tituloEleitoral(TITULO_TESTE)
                    .unidadeAtivaCodigo(1L)
                    .perfilAtivo(Perfil.GESTOR)
                    .build();
            when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(TITULO_TESTE, 1L, Perfil.GESTOR));

            when(alertaAplicacaoService.alertasPorUsuario(any(ContextoUsuarioAutenticado.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas")
                            .with(user(usuarioMock))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(alertaAplicacaoService).alertasPorUsuario(any(ContextoUsuarioAutenticado.class));
        }

        @Test
        @DisplayName("Deve retornar lista de alertas não lidos com sucesso usando o contexto do JWT")
        void listarNaoLidos_quandoSucesso_deveRetornarListaDeAlertas() throws Exception {
            Usuario usuarioMock = Usuario.builder()
                    .tituloEleitoral(TITULO_TESTE)
                    .unidadeAtivaCodigo(1L)
                    .perfilAtivo(Perfil.GESTOR)
                    .build();
            when(usuarioAplicacaoService.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(TITULO_TESTE, 1L, Perfil.GESTOR));

            when(alertaAplicacaoService.listarNaoLidos(any(ContextoUsuarioAutenticado.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas/nao-lidos")
                            .with(user(usuarioMock))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(alertaAplicacaoService).listarNaoLidos(any(ContextoUsuarioAutenticado.class));
        }
    }

}

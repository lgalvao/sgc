package sgc.alerta;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertaController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AlertaController")
class AlertaControllerTest {

    private static final String TITULO_TESTE = "12345678901"; // 11 ou 12 dígitos
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;
    @MockitoBean
    private AlertaFacade alertaFacade;

    @Nested
    @DisplayName("Marcar como Lidos")
    class MarcarComoLidos {
        @Test
        @DisplayName("Deve retornar OK quando marcar como lidos com sucesso")
        void marcarComoLidos_quandoSucesso_deveRetornarOk() throws Exception {
            Usuario usuarioMock = Usuario.builder()
                    .tituloEleitoral(TITULO_TESTE)
                    .build();

            mockMvc.perform(post("/api/alertas/marcar-como-lidos")
                            .with(user(usuarioMock))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[1, 2, 3]"))
                    .andExpect(status().isOk());

            verify(alertaFacade).marcarComoLidos(eq(TITULO_TESTE), anyList());
        }

        @Test
        @DisplayName("Deve retornar OK quando lista vazia")
        void marcarComoLidos_quandoListaVazia_deveRetornarOk() throws Exception {
            Usuario usuarioMock = Usuario.builder()
                    .tituloEleitoral(TITULO_TESTE)
                    .build();

            mockMvc.perform(post("/api/alertas/marcar-como-lidos")
                            .with(user(usuarioMock))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isOk());

            verify(alertaFacade).marcarComoLidos(eq(TITULO_TESTE), anyList());
        }
    }

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

            when(alertaFacade.alertasPorUsuario(eq(TITULO_TESTE), eq(1L), anyString()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas")
                            .with(user(usuarioMock))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(alertaFacade).alertasPorUsuario(eq(TITULO_TESTE), eq(1L), anyString());
        }

        @Test
        @DisplayName("Deve retornar lista de alertas não lidos com sucesso usando o contexto do JWT")
        void listarNaoLidos_quandoSucesso_deveRetornarListaDeAlertas() throws Exception {
            Usuario usuarioMock = Usuario.builder()
                    .tituloEleitoral(TITULO_TESTE)
                    .unidadeAtivaCodigo(1L)
                    .perfilAtivo(Perfil.GESTOR)
                    .build();

            when(alertaFacade.listarNaoLidos(eq(TITULO_TESTE), eq(1L), anyString()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas/nao-lidos")
                            .with(user(usuarioMock))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(alertaFacade).listarNaoLidos(eq(TITULO_TESTE), eq(1L), anyString());
        }
    }

}


package sgc.alerta;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.integracao.mocks.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
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
            mockMvc.perform(post("/api/alertas/marcar-como-lidos")
                            .with(user(TITULO_TESTE))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[1, 2, 3]"))
                    .andExpect(status().isOk());

            verify(alertaFacade).marcarComoLidos(eq(TITULO_TESTE), anyList());
        }

        @Test
        @DisplayName("Deve retornar OK quando lista vazia")
        void marcarComoLidos_quandoListaVazia_deveRetornarOk() throws Exception {
            mockMvc.perform(post("/api/alertas/marcar-como-lidos")
                            .with(user(TITULO_TESTE))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isOk());

            verify(alertaFacade).marcarComoLidos(eq(TITULO_TESTE), anyList());
        }
    }

    @Nested
    @DisplayName("Listar Alertas")
    class ListarAlertas {
        @Test
        @DisplayName("Deve retornar lista de alertas com sucesso")
        void listarAlertas_quandoSucesso_deveRetornarListaDeAlertas() throws Exception {
            when(alertaFacade.alertasPorUsuario(TITULO_TESTE))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas")
                            .with(user(TITULO_TESTE))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(alertaFacade).alertasPorUsuario(TITULO_TESTE);
        }

        @Test
        @DisplayName("Deve retornar lista de alertas não lidos com sucesso")
        void listarNaoLidos_quandoSucesso_deveRetornarListaDeAlertas() throws Exception {
            when(alertaFacade.listarNaoLidos(TITULO_TESTE))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas/nao-lidos")
                            .with(user(TITULO_TESTE))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(alertaFacade).listarNaoLidos(TITULO_TESTE);
        }
    }

    @Nested
    @DisplayName("Extração de Título de Usuário - Switch Expression")
    class ExtracaoTituloUsuario {
        
        @Test
        @DisplayName("Deve extrair titulo quando principal é String")
        void deveExtrairTituloQuandoPrincipalString() throws Exception {
            // Este caso já é coberto pelos testes acima usando .with(user(TITULO_TESTE))
            // que passa uma String como principal
            
            when(alertaFacade.alertasPorUsuario(TITULO_TESTE))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas")
                            .with(user(TITULO_TESTE)) // Principal como String
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(alertaFacade).alertasPorUsuario(TITULO_TESTE);
        }
        
        @Test
        @DisplayName("Deve extrair titulo quando principal é UserDetails")
        void deveExtrairTituloQuandoPrincipalUserDetails() throws Exception {
            UserDetails userDetails = User.builder()
                    .username("98765432100")
                    .password("password")
                    .authorities("ROLE_USER")
                    .build();
            
            when(alertaFacade.listarNaoLidos("98765432100"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/alertas/nao-lidos")
                            .with(user(userDetails)) // Principal como UserDetails
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(alertaFacade).listarNaoLidos("98765432100");
        }
    }
}


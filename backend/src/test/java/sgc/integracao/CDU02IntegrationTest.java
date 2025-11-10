package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.model.Processo;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-02: Visualizar Painel")
@Import(TestSecurityConfig.class)
public class CDU02IntegrationTest {

    private static final String API_PAINEL_PROCESSOS = "/api/painel/processos";
    private static final String API_PAINEL_ALERTAS = "/api/painel/alertas";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;

    private void criarAlerta(String descricao, Processo processo, Usuario usuario, Unidade unidade) {
        Alerta a = new Alerta();
        a.setDescricao(descricao);
        a.setProcesso(processo);
        a.setUsuarioDestino(usuario);
        a.setUnidadeDestino(unidade);
        a.setDataHora(LocalDateTime.now());
        alertaRepo.save(a);
    }

    private void setupSecurityContext(String tituloEleitoral, Long codigoUnidade, String... perfis) {
        Unidade unidade = unidadeRepo.findById(codigoUnidade).orElseThrow();
        Usuario principal = usuarioRepo.findById(tituloEleitoral)
                .orElseGet(() -> {
                    Usuario newUser = new Usuario(
                            tituloEleitoral,
                            "Usuario de Teste",
                            "teste@sgc.com",
                            "123",
                            unidade,
                            Arrays.stream(perfis).map(Perfil::valueOf).collect(Collectors.toList())
                    );
                    return usuarioRepo.save(newUser);
                });
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Processos")
    @Sql("/datasets/cdu02-processos.sql")
    class VisibilidadeProcessosTestes {

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve ver todos os processos, incluindo os com status 'Criado'")
        void testListarProcessos_Admin_VeTodos() throws Exception {
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(4)));
        }

        @Test
        @DisplayName("GESTOR da unidade raiz deve ver todos os processos da sua unidade e de todas as subordinadas")
        void testListarProcessos_GestorRaiz_VeTodos() throws Exception {
            setupSecurityContext("1", 2L, "GESTOR"); // Unidade Raiz
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "GESTOR")
                            .param("codigoUnidade", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)));
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 1 deve ver processos da sua unidade e da Neta 1")
        void testListarProcessos_ChefeUnidadeFilha1_VeProcessosSubordinados() throws Exception {
            setupSecurityContext("2", 6L, "CHEFE"); // Unidade Filha 1
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "CHEFE")
                            .param("codigoUnidade", "6"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 2 não deve ver processos de outras unidades")
        void testListarProcessos_ChefeUnidadeFilha2_NaoVeProcessosDeOutros() throws Exception {
            setupSecurityContext("3", 7L, "CHEFE"); // Unidade Filha 2
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "CHEFE")
                            .param("codigoUnidade", "7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("Nenhum perfil, exceto ADMIN, deve ver processos com status 'Criado'")
        void testListarProcessos_NaoAdmin_NaoVeProcessosCriados() throws Exception {
            setupSecurityContext("1", 2L, "GESTOR"); // Unidade Raiz
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "GESTOR")
                            .param("codigoUnidade", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado')]", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Alertas")
    class VisibilidadeAlertasTestes {

        @Test
        @DisplayName("Usuário deve ver alertas direcionados a ele")
        void testListarAlertas_UsuarioVeSeusAlertas() throws Exception {
            setupSecurityContext("8", 2L, "GESTOR");
            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", "8"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].descricao").value("Alerta para Gestor"));
        }

        @Test
        @DisplayName("Usuário deve ver alertas direcionados à sua unidade")
        void testListarAlertas_UsuarioVeAlertasDaSuaUnidade() throws Exception {
            setupSecurityContext("333333333333", 6L, "CHEFE");
            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("codigoUnidade", "6"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("Usuário não deve ver alertas de outros usuários ou unidades")
        void testListarAlertas_UsuarioNaoVeAlertasDeOutros() throws Exception {
            setupSecurityContext("3", 7L, "CHEFE");
            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", "3")
                            .param("codigoUnidade", "7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }
}

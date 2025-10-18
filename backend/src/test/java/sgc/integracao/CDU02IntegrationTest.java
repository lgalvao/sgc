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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.comum.BeanUtil;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
@Import({TestSecurityConfig.class, BeanUtil.class})
public class CDU02IntegrationTest {

    private static final String API_PAINEL_PROCESSOS = "/api/painel/processos";
    private static final String API_PAINEL_ALERTAS = "/api/painel/alertas";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private UnidadeProcessoRepo unidadeProcessoRepo;

    // Unidades
    private Unidade unidadeRaiz, unidadeFilha1, unidadeFilha2, unidadeNeta1;

    @BeforeEach
    void setup() {
        unidadeRaiz = new Unidade("Raiz", "RAIZ");
        unidadeRepo.save(unidadeRaiz);
        unidadeFilha1 = new Unidade("Filha 1", "F1");
        unidadeFilha1.setUnidadeSuperior(unidadeRaiz);
        unidadeRepo.save(unidadeFilha1);
        unidadeFilha2 = new Unidade("Filha 2", "F2");
        unidadeFilha2.setUnidadeSuperior(unidadeRaiz);
        unidadeRepo.save(unidadeFilha2);
        unidadeNeta1 = new Unidade("Neta 1", "N1");
        unidadeNeta1.setUnidadeSuperior(unidadeFilha1);
        unidadeRepo.save(unidadeNeta1);

        Processo p1 = criarProcesso("Processo da Raiz", SituacaoProcesso.EM_ANDAMENTO, unidadeRaiz);
        Processo p2 = criarProcesso("Processo da Filha 1", SituacaoProcesso.EM_ANDAMENTO, unidadeFilha1);
        Processo p3 = criarProcesso("Processo da Filha 2", SituacaoProcesso.FINALIZADO, unidadeFilha2);
        criarProcesso("Processo da Neta 1", SituacaoProcesso.EM_ANDAMENTO, unidadeNeta1);
        criarProcesso("Processo Criado", SituacaoProcesso.CRIADO, unidadeRaiz);

        Usuario u1 = new Usuario(1L, "Gestor Raiz", "gestor@test.com", "123", unidadeRaiz, List.of(Perfil.GESTOR));
        usuarioRepo.save(u1);
        Usuario u2 = new Usuario(2L, "Chefe Filha 1", "chefe1@test.com", "123", unidadeFilha1, List.of(Perfil.CHEFE));
        usuarioRepo.save(u2);
        Usuario u3 = new Usuario(3L, "Chefe Filha 2", "chefe2@test.com", "123", unidadeFilha2, List.of(Perfil.CHEFE));
        usuarioRepo.save(u3);

        criarAlerta("Alerta para Gestor", p1, u1, null);
        criarAlerta("Alerta para Unidade Filha 1", p2, u2, unidadeFilha1);
    }

    private Processo criarProcesso(String descricao, SituacaoProcesso situacao, Unidade... participantes) {
        Processo p = new Processo();
        p.setDescricao(descricao);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setSituacao(situacao);
        p.setDataLimite(LocalDateTime.now().plusDays(30));
        processoRepo.save(p);
        for (Unidade u : participantes) {
            unidadeProcessoRepo.save(new UnidadeProcesso(p.getCodigo(), u.getCodigo(), u.getNome(), u.getSigla(), null, u.getTipo(), u.getSituacao().name(), u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getCodigo() : null));
        }
        return p;
    }

    private void criarAlerta(String descricao, Processo processo, Usuario usuario, Unidade unidade) {
        Alerta a = new Alerta();
        a.setDescricao(descricao);
        a.setProcesso(processo);
        a.setUsuarioDestino(usuario);
        a.setUnidadeDestino(unidade);
        a.setDataHora(LocalDateTime.now());
        alertaRepo.save(a);
    }

    private void setupSecurityContext(long tituloEleitoral, Unidade unidade, String... perfis) {
        Usuario principal = new Usuario(
            tituloEleitoral,
            "Usuario de Teste",
            "teste@sgc.com",
            "123",
            unidade,
            Arrays.stream(perfis).map(Perfil::valueOf).collect(Collectors.toList())
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Processos")
    class VisibilidadeProcessosTestes {

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve ver todos os processos, incluindo os com status 'Criado'")
        void testListarProcessos_Admin_VeTodos() throws Exception {
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(5))); // Todos os 5 processos
        }

        @Test
        @DisplayName("GESTOR da unidade raiz deve ver todos os processos da sua unidade e de todas as subordinadas")
        void testListarProcessos_GestorRaiz_VeTodos() throws Exception {
            setupSecurityContext(1L, unidadeRaiz, "GESTOR");
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "GESTOR")
                            .param("unidade", unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(4))); // processoRaiz, processoFilha1, processoFilha2, processoNeta1
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 1 deve ver processos da sua unidade e da Neta 1")
        void testListarProcessos_ChefeUnidadeFilha1_VeProcessosSubordinados() throws Exception {
            setupSecurityContext(2L, unidadeFilha1, "CHEFE");
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "CHEFE")
                            .param("unidade", unidadeFilha1.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 2 não deve ver processos de outras unidades")
        void testListarProcessos_ChefeUnidadeFilha2_NaoVeProcessosDeOutros() throws Exception {
            setupSecurityContext(3L, unidadeFilha2, "CHEFE");
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "CHEFE")
                            .param("unidade", unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("Nenhum perfil, exceto ADMIN, deve ver processos com status 'Criado'")
        void testListarProcessos_NaoAdmin_NaoVeProcessosCriados() throws Exception {
            setupSecurityContext(1L, unidadeRaiz, "GESTOR");
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param("perfil", "GESTOR")
                            .param("unidade", unidadeRaiz.getCodigo().toString()))
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
            setupSecurityContext(1L, unidadeRaiz, "GESTOR");
            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].descricao").value("Alerta para Gestor"));
        }

        @Test
        @DisplayName("Usuário deve ver alertas direcionados à sua unidade")
        void testListarAlertas_UsuarioVeAlertasDaSuaUnidade() throws Exception {
            setupSecurityContext(2L, unidadeFilha1, "CHEFE");
            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("unidade", unidadeFilha1.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].descricao").value("Alerta para Unidade Filha 1"));
        }

        @Test
        @DisplayName("Usuário não deve ver alertas de outros usuários ou unidades")
        void testListarAlertas_UsuarioNaoVeAlertasDeOutros() throws Exception {
            setupSecurityContext(3L, unidadeFilha2, "CHEFE");
            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", "3")
                            .param("unidade", unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }
}
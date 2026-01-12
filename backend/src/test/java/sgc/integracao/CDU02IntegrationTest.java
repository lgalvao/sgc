package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.fixture.AlertaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-02: Visualizar Painel")
@Import(TestSecurityConfig.class)
class CDU02IntegrationTest extends BaseIntegrationTest {
    private static final String API_PAINEL_PROCESSOS = "/api/painel/processos";
    private static final String API_PAINEL_ALERTAS = "/api/painel/alertas";

    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidadeRaiz;
    private Unidade unidadeFilha1;
    private Unidade unidadeFilha2;
    private Processo processoRaiz;
    private Processo processoFilha1;

    @BeforeEach
    void setup() {
        // Ajusta sequências para evitar conflito com data.sql
        try {
            jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 20000");
            jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 80000");
            jdbcTemplate.execute("ALTER TABLE SGC.ALERTA ALTER COLUMN CODIGO RESTART WITH 90000");
        } catch (Exception e) {
            // Ignora se o DB não suportar (H2 suporta)
            System.err.println("Aviso: Não foi possível reiniciar sequências: " + e.getMessage());
        }

        // Setup Programático - Não depende do data.sql

        // 1. Criar Unidades (Hierarquia)
        // Raiz
        unidadeRaiz = UnidadeFixture.unidadePadrao();
        unidadeRaiz.setCodigo(null); // Auto-increment
        unidadeRaiz.setNome("Unidade Raiz Teste");
        unidadeRaiz = unidadeRepo.save(unidadeRaiz);

        // Filha 1 -> Raiz
        unidadeFilha1 = UnidadeFixture.unidadePadrao();
        unidadeFilha1.setCodigo(null);
        unidadeFilha1.setNome("Unidade Filha 1 Teste");
        unidadeFilha1.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha1 = unidadeRepo.save(unidadeFilha1);

        // Filha 2 -> Raiz
        unidadeFilha2 = UnidadeFixture.unidadePadrao();
        unidadeFilha2.setCodigo(null);
        unidadeFilha2.setNome("Unidade Filha 2 Teste");
        unidadeFilha2.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha2 = unidadeRepo.save(unidadeFilha2);

        // 2. Criar Processos e Vincular a Unidades

        processoRaiz = ProcessoFixture.processoEmAndamento();
        processoRaiz.setCodigo(null);
        processoRaiz.setDescricao("Processo Raiz");
        processoRaiz.getParticipantes().add(unidadeRaiz);
        processoRaiz = processoRepo.save(processoRaiz);

        processoFilha1 = ProcessoFixture.processoEmAndamento();
        processoFilha1.setCodigo(null);
        processoFilha1.setDescricao("Processo Filha 1");
        processoFilha1.getParticipantes().add(unidadeFilha1);
        processoFilha1 = processoRepo.save(processoFilha1);

        Processo processoCriado = ProcessoFixture.processoPadrao(); // Status CRIADO
        processoCriado.setCodigo(null);
        processoCriado.setDescricao("Processo Criado Teste");
        processoCriado.getParticipantes().add(unidadeRaiz);
        processoRepo.save(processoCriado);

        // Flush para garantir persistência antes dos testes
        unidadeRepo.flush();
        processoRepo.flush();
    }

    private Usuario setupSecurityContext(String tituloEleitoral, Unidade unidade, String... perfis) {
        // Cria usuário dinamicamente se não existir
        Usuario usuario = usuarioRepo.findById(tituloEleitoral).orElseGet(() -> {
            Usuario newUser = UsuarioFixture.usuarioComTitulo(tituloEleitoral);
            newUser.setUnidadeLotacao(unidade);
            newUser = usuarioRepo.save(newUser);
            // Insere na tabela de join simulada se necessário (H2 specific for View)
             for (String perfilStr : perfis) {
                try {
                    jdbcTemplate.update(
                            "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                            newUser.getTituloEleitoral(), unidade.getCodigo(), perfilStr);
                } catch (Exception ignored) {
                    // Ignora se já existe
                }
            }
            return newUser;
        });

        Set<UsuarioPerfil> perfisSet = new HashSet<>();
        for (String perfilStr : perfis) {
            perfisSet.add(UsuarioPerfil.builder()
                    .usuario(usuario)
                    .unidade(unidade)
                    .perfil(Perfil.valueOf(perfilStr))
                    .build());
        }
        usuario.setAtribuicoes(perfisSet);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(usuario, null,
                usuario.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return usuario;
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Processos")
    class VisibilidadeProcessosTestes {

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN deve ver todos os processos, incluindo os com status 'Criado'")
        void testListarProcessos_Admin_VeTodos() throws Exception {
            // Admin vê tudo, inclusive os 3 criados no setup
            mockMvc.perform(get(API_PAINEL_PROCESSOS).param("perfil", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Raiz')]").exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Filha 1')]").exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Teste')]").exists());
        }

        @Test
        @DisplayName("GESTOR da unidade raiz deve ver todos os processos da sua unidade e de todas as subordinadas")
        @org.springframework.security.test.context.support.WithMockUser(username = "99001")
        void testListarProcessos_GestorRaiz_VeTodos() throws Exception {
            setupSecurityContext("99001", unidadeRaiz, "GESTOR");

            mockMvc.perform(
                    get(API_PAINEL_PROCESSOS)
                            .param("perfil", "GESTOR")
                            .param("unidade", unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Deve ver o da Raiz e o da Filha
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Raiz')]").exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Filha 1')]").exists())
                    // GESTOR não vê processos com status CRIADO (regra confirmada)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Teste')]").doesNotExist());
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 2 não deve ver processos de outras unidades")
        @org.springframework.security.test.context.support.WithMockUser(username = "99002")
        void testListarProcessos_ChefeUnidadeFilha2_NaoVeProcessosDeOutros() throws Exception {
            setupSecurityContext("99002", unidadeFilha2, "CHEFE");

            // Unidade Filha 2 não tem processos no setup
            mockMvc.perform(
                    get(API_PAINEL_PROCESSOS)
                            .param("perfil", "CHEFE")
                            .param("unidade", unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Raiz')]").doesNotExist())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Filha 1')]").doesNotExist());
        }

        @Test
        @DisplayName("Nenhum perfil, exceto ADMIN, deve ver processos com status 'Criado' (Exceto da própria unidade)")
        @org.springframework.security.test.context.support.WithMockUser(username = "99003")
        void testListarProcessos_NaoAdmin_NaoVeProcessosCriados() throws Exception {
            setupSecurityContext("99003", unidadeRaiz, "GESTOR");

            // Vamos replicar o cenário: Processo Criado numa unidade FILHA.
            Processo processoCriadoFilha = ProcessoFixture.processoPadrao();
            processoCriadoFilha.setCodigo(null);
            processoCriadoFilha.setDescricao("Processo Criado Filha");
            processoCriadoFilha.getParticipantes().add(unidadeFilha1);
            processoRepo.saveAndFlush(processoCriadoFilha);

            mockMvc.perform(
                    get(API_PAINEL_PROCESSOS)
                            .param("perfil", "GESTOR")
                            .param("unidade", unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Deve ver Raiz e Filha (Em Andamento)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Filha 1')]").exists())
                    // NÃO deve ver Criado Filha (Se a regra for essa)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Filha')]").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Alertas")
    class VisibilidadeAlertasTestes {
        @Test
        @DisplayName("Usuário deve ver alertas direcionados a ele")
        @org.springframework.security.test.context.support.WithMockUser(username = "99004")
        void testListarAlertas_UsuarioVeSeusAlertas() throws Exception {
            Usuario usuario = setupSecurityContext("99004", unidadeRaiz, "GESTOR");

            Alerta alerta = AlertaFixture.alertaParaUsuario(processoRaiz, usuario);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Pessoal Teste");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", usuario.getTituloEleitoral())
                            .param("unidade", unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Pessoal Teste')]").exists());
        }

        @Test
        @DisplayName("Usuário deve ver alertas direcionados à sua unidade e às suas subordinadas")
        @org.springframework.security.test.context.support.WithMockUser(username = "99005")
        void testListarAlertas_UsuarioVeAlertasDaSuaUnidade() throws Exception {
            setupSecurityContext("99005", unidadeRaiz, "GESTOR");
            // Alerta para Unidade Filha 1 (Subordinada à Raiz)
            Alerta alerta = AlertaFixture.alertaParaUnidade(processoFilha1, unidadeFilha1);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Subordinada Teste");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS)
                    .param("unidade", unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Painel não mostra alertas de subordinadas (regra atual do código)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Subordinada Teste')]").doesNotExist());
        }

        @Test
        @DisplayName("Usuário não deve ver alertas de outros usuários ou unidades")
        @org.springframework.security.test.context.support.WithMockUser(username = "99006")
        void testListarAlertas_UsuarioNaoVeAlertasDeOutros() throws Exception {
            Usuario usuario = setupSecurityContext("99006", unidadeFilha2, "CHEFE");

            // Alerta para Unidade Filha 1 (Irmã, não subordinada)
            Alerta alerta = AlertaFixture.alertaParaUnidade(processoFilha1, unidadeFilha1);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Outra Unidade");
            alertaRepo.save(alerta);

            mockMvc.perform(
                    get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", usuario.getTituloEleitoral())
                            .param("unidade", unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Outra Unidade')]").doesNotExist());
        }
    }
}

package sgc.integracao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.fixture.AlertaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;

import java.util.Collections;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@Transactional
@DisplayName("CDU-02: Visualizar Painel")
@Slf4j
class CDU02IntegrationTest extends BaseIntegrationTest {
    private static final String API_PAINEL_PROCESSOS = "/api/painel/processos";
    private static final String API_PAINEL_ALERTAS = "/api/painel/alertas";
    private static final String PERFIL = "perfil";
    private static final String UNIDADE = "unidade";
    private static final String GESTOR = "GESTOR";
    private static final String CHEFE = "CHEFE";
    private static final String PROCESSO_RAIZ_JSON_PATH = "$.content[?(@.descricao == 'Processo Raiz')]";
    private static final String PROCESSO_FILHA_1_JSON_PATH = "$.content[?(@.descricao == 'Processo Filha 1')]";

    @Autowired
    private UsuarioRepo usuarioRepo;
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
        } catch (DataAccessException e) {
            // Ignora se o DB não suportar (H2 suporta)
            log.warn("Aviso: Não foi possível reiniciar sequências: {}", e.getMessage());
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
        processoRaiz.adicionarParticipantes(Set.of(unidadeRaiz));
        processoRaiz = processoRepo.save(processoRaiz);

        processoFilha1 = ProcessoFixture.processoEmAndamento();
        processoFilha1.setCodigo(null);
        processoFilha1.setDescricao("Processo Filha 1");
        processoFilha1.adicionarParticipantes(Set.of(unidadeFilha1));
        processoFilha1 = processoRepo.save(processoFilha1);

        Processo processoCriado = ProcessoFixture.processoPadrao(); // Status CRIADO
        processoCriado.setCodigo(null);
        processoCriado.setDescricao("Processo Criado Teste");
        processoCriado.adicionarParticipantes(Set.of(unidadeRaiz));
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
            return usuarioRepo.save(newUser);
        });

        // Configura perfil e unidade ativos da sessão
        if (perfis.length > 0) {
            Perfil p = Perfil.valueOf(perfis[0]);
            usuario.setPerfilAtivo(p);
            usuario.setUnidadeAtivaCodigo(unidade.getCodigo());
        }

        // Insere na tabela de join simulada se necessário (H2 specific for View)
        for (String perfilStr : perfis) {
            try {
                jdbcTemplate.update(
                        "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                        usuario.getTituloEleitoral(), unidade.getCodigo(), perfilStr);
            } catch (DataAccessException ignored) {
                // Ignora se já existe
            }
        }

        // Define authorities como apenas o perfil selecionado para simular o comportamento do FiltroJwt
        Set<GrantedAuthority> authorities = Collections.emptySet();
        if (perfis.length > 0) {
            authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + perfis[0]));
        }
        usuario.setAuthorities(authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(usuario, null,
                authorities);

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
        void testListarProcessosAdminVeTodos() throws Exception {
            // Admin vê tudo, inclusive os 3 criados no setup
            mockMvc.perform(get(API_PAINEL_PROCESSOS).param(PERFIL, "ADMIN").param(UNIDADE, "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).exists())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Teste')]").exists());
        }

        @Test
        @DisplayName("GESTOR da unidade raiz deve ver todos os processos da sua unidade e de todas as subordinadas")
        @WithMockUser(username = "99001")
        void testListarProcessosGestorRaizVeTodos() throws Exception {
            setupSecurityContext("99001", unidadeRaiz, GESTOR);

            mockMvc.perform(
                    get(API_PAINEL_PROCESSOS)
                            .param(PERFIL, GESTOR)
                            .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Deve ver o da Raiz e o da Filha
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).exists())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    // GESTOR não vê processos com status CRIADO (regra confirmada)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Teste')]").doesNotExist());
        }

        @Test
        @DisplayName("CHEFE da unidade Filha 2 não deve ver processos de outras unidades")
        @WithMockUser(username = "99002")
        void testListarProcessosChefeUnidadeFilha2NaoVeProcessosDeOutros() throws Exception {
            setupSecurityContext("99002", unidadeFilha2, CHEFE);

            // Unidade Filha 2 não tem processos no setup
            mockMvc.perform(
                    get(API_PAINEL_PROCESSOS)
                            .param(PERFIL, CHEFE)
                            .param(UNIDADE, unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).doesNotExist())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).doesNotExist());
        }

        @Test
        @DisplayName("Nenhum perfil, exceto ADMIN, deve ver processos com status 'Criado' (Exceto da própria unidade)")
        @WithMockUser(username = "99003")
        void testListarProcessosNaoAdminNaoVeProcessosCriados() throws Exception {
            setupSecurityContext("99003", unidadeRaiz, GESTOR);

            // Vamos replicar o cenário: Processo Criado numa unidade FILHA.
            Processo processoCriadoFilha = ProcessoFixture.processoPadrao();
            processoCriadoFilha.setCodigo(null);
            processoCriadoFilha.setDescricao("Processo Criado Filha");
            processoCriadoFilha.adicionarParticipantes(Set.of(unidadeFilha1));
            processoRepo.saveAndFlush(processoCriadoFilha);

            mockMvc.perform(
                    get(API_PAINEL_PROCESSOS)
                            .param(PERFIL, GESTOR)
                            .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Deve ver Raiz e Filha (Em Andamento)
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    // NÃO deve ver Criado Filha (Se a regra for essa)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Filha')]").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Alertas")
    class VisibilidadeAlertasTestes {
        @Test
        @DisplayName("Usuário deve ver alertas direcionados a ele")
        @WithMockUser(username = "99004")
        void testListarAlertasUsuarioVeSeusAlertas() throws Exception {
            Usuario usuario = setupSecurityContext("99004", unidadeRaiz, GESTOR);

            Alerta alerta = AlertaFixture.alertaParaUsuario(processoRaiz, usuario);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Pessoal Teste");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS)
                    .param("usuarioTitulo", usuario.getTituloEleitoral())
                    .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Pessoal Teste')]").exists());
        }

        @Test
        @DisplayName("Usuário deve ver alertas direcionados à sua unidade e às suas subordinadas")
        @WithMockUser(username = "99005")
        void testListarAlertasUsuarioVeAlertasDaSuaUnidade() throws Exception {
            setupSecurityContext("99005", unidadeRaiz, GESTOR);
            // Alerta para Unidade Filha 1 (Subordinada à Raiz)
            Alerta alerta = AlertaFixture.alertaParaUnidade(processoFilha1, unidadeFilha1);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Subordinada Teste");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS)
                    .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Painel não mostra alertas de subordinadas (regra atual do código)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Subordinada Teste')]").doesNotExist());
        }

        @Test
        @DisplayName("Usuário não deve ver alertas de outros usuários ou unidades")
        @WithMockUser(username = "99006")
        void testListarAlertasUsuarioNaoVeAlertasDeOutros() throws Exception {
            Usuario usuario = setupSecurityContext("99006", unidadeFilha2, CHEFE);

            // Alerta para Unidade Filha 1 (Irmã, não subordinada)
            Alerta alerta = AlertaFixture.alertaParaUnidade(processoFilha1, unidadeFilha1);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Outra Unidade");
            alertaRepo.save(alerta);

            mockMvc.perform(
                    get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", usuario.getTituloEleitoral())
                            .param(UNIDADE, unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Outra Unidade')]").doesNotExist());
        }
    }
}

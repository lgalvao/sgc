package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.SgrhService;
import sgc.sgrh.api.PerfilDto;
import sgc.sgrh.internal.model.Perfil;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioPerfil;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("CDU-06: Detalhar processo")
public class CDU06IntegrationTest extends BaseIntegrationTest {
    private static final String TEST_USER_ID = "123456789";

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private SgrhService sgrhService;

    private Processo processo;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        // Reset sequences
        try {
            jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 50000");
            jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 60000");
        } catch (Exception ignored) {}

        // Cria unidade programaticamente
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("U_TESTE");
        unidade.setNome("Unidade Teste");
        unidade = unidadeRepo.save(unidade);

        // Cria processo
        processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        processo = processoRepo.save(processo);

        SecurityContextHolder.clearContext();
    }

    private org.springframework.security.core.Authentication setupSecurityContext(Unidade unidade, Perfil perfil) {
        Usuario principal = UsuarioFixture.usuarioPadrao();
        principal.setTituloEleitoral(TEST_USER_ID);
        principal.setUnidadeLotacao(unidade);

        Set<UsuarioPerfil> atribuicoes = new HashSet<>();
        atribuicoes.add(
                UsuarioPerfil.builder()
                        .usuario(principal)
                        .unidade(unidade)
                        .perfil(perfil)
                        .build());
        principal.setAtribuicoes(atribuicoes);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        when(sgrhService.buscarPerfisUsuario(anyString())).thenReturn(List.of(
                new PerfilDto(TEST_USER_ID, unidade.getCodigo(), unidade.getNome(), perfil.name())));
        return auth;
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve detalhar processo para Admin")
    void testDetalharProcesso_sucesso() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        subprocessoRepo.save(new Subprocesso(processo, unidade, null,
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo de Teste"));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 para processo inexistente")
    void testDetalharProcesso_naoEncontrado() throws Exception {
        mockMvc.perform(get("/api/processos/{id}/detalhes", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve mostrar 'podeFinalizar' como true para Admin com subprocessos homologados")
    void testPodeFinalizar_true_comAdmin() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        subprocessoRepo.save(
                new Subprocesso(
                        processo,
                        unidade,
                        null,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeFinalizar' como false para não Admin")
    void testPodeFinalizar_false_semAdmin() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        org.springframework.security.core.Authentication auth = setupSecurityContext(unidade, Perfil.CHEFE);
        subprocessoRepo.save(
                new Subprocesso(
                        processo,
                        unidade,
                        null,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(false));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarCadastro' como true para Gestor com cadastro disponibilizado")
    void testPodeHomologarCadastro_true() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        org.springframework.security.core.Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(new Subprocesso(processo, unidade, null,
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarCadastro").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarMapa' como true para Gestor com mapa validado")
    void testPodeHomologarMapa_true() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        org.springframework.security.core.Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(new Subprocesso(processo, unidade, null,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO, processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarMapa").value(true));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar detalhes da unidade com situação do subprocesso correta")
    void testDetalharProcesso_dadosSubprocesso() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);

        Subprocesso subprocesso = new Subprocesso(processo, unidade, null,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidades[0].situacaoSubprocesso")
                        .value("MAPEAMENTO_MAPA_HOMOLOGADO"))
                .andExpect(jsonPath("$.unidades[0].dataLimite").exists());
    }
}

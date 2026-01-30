package sgc.integracao;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

import sgc.Sgc;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import org.springframework.security.core.Authentication;
import sgc.organizacao.model.UsuarioRepo;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("CDU-06: Detalhar processo")
class CDU06IntegrationTest extends BaseIntegrationTest {
    private static final String TEST_USER_ID = "123456789";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private UsuarioFacade usuarioService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private sgc.organizacao.model.UsuarioPerfilRepo usuarioPerfilRepo;

    @PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private Processo processo;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        // Reset sequences
        jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 50000");
        jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 60000");

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

    private Authentication setupSecurityContext(Unidade unidade, Perfil perfil) {
        Usuario principal = UsuarioFixture.usuarioPadrao();
        principal.setTituloEleitoral(TEST_USER_ID);
        principal.setUnidadeLotacao(unidade);
        usuarioRepo.save(principal);
        principal.setAuthorities(Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + perfil.name())));

        // Insert into VW_USUARIO_PERFIL_UNIDADE to support ProcessoDetalheBuilder query
        try {
            var up = sgc.organizacao.model.UsuarioPerfil.builder()
                    .usuarioTitulo(TEST_USER_ID)
                    .unidadeCodigo(unidade.getCodigo())
                    .perfil(perfil)
                    .build();
            usuarioPerfilRepo.save(up);
        } catch (Exception e) {
             e.printStackTrace();
        }
        
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        when(usuarioService.buscarPerfisUsuario(anyString())).thenReturn(List.of(
                new PerfilDto(TEST_USER_ID, unidade.getCodigo(), unidade.getNome(), perfil.name(), perfil.name())));
        return auth;
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve detalhar processo para Admin")
    void testDetalharProcesso_sucesso() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());

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
        subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeFinalizar' como false para não Admin")
    void testPodeFinalizar_false_semAdmin() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        Authentication auth = setupSecurityContext(unidade, Perfil.CHEFE);
        subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(false));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarCadastro' como true para Gestor com cadastro disponibilizado")
    void testPodeHomologarCadastro_true() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarCadastro").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarMapa' como true para Gestor com mapa validado")
    void testPodeHomologarMapa_true() throws Exception {
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());

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

        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidades[0].situacaoSubprocesso")
                        .value("MAPEAMENTO_MAPA_HOMOLOGADO"))
                .andExpect(jsonPath("$.unidades[0].dataLimite").exists());
    }
}

package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-06: Detalhar processo")
class CDU06IntegrationTest extends BaseIntegrationTest {
    private static final String TEST_USER_ID = "123456789";

    @MockitoBean
    private UsuarioFacade usuarioService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @PersistenceContext
    private EntityManager entityManager;

    private Processo processo;
    private Unidade unidade;

    @BeforeEach
    void setUp() {

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
        principal.setPerfilAtivo(perfil);
        principal.setUnidadeAtivaCodigo(unidade.getCodigo());
        usuarioRepo.save(principal);
        principal.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));

        // Insert into VW_USUARIO_PERFIL_UNIDADE to support ProcessoDetalheBuilder query
        try {
            var up = UsuarioPerfil.builder()
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
        processo.adicionarParticipantes(Set.of(unidade));
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
        processo.adicionarParticipantes(Set.of(unidade));
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
        processo.adicionarParticipantes(Set.of(unidade));
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
        processo.adicionarParticipantes(Set.of(unidade));
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
        processo.adicionarParticipantes(Set.of(unidade));
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
        processo.adicionarParticipantes(Set.of(unidade));
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

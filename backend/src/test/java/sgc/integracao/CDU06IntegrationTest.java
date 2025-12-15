package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioPerfil;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

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

    @MockitoBean
    private SgrhService sgrhService;

    private Processo processo;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        processo = processoRepo.save(processo);
        SecurityContextHolder.clearContext();
    }

    private org.springframework.security.core.Authentication setupSecurityContext(Unidade unidade, Perfil perfil) {
        Usuario principal =
                new Usuario(TEST_USER_ID, "Usuario Teste", "teste@teste.com", "123", unidade);

        Set<UsuarioPerfil> atribuicoes = new HashSet<>();
        atribuicoes.add(
                UsuarioPerfil.builder()
                        .usuario(principal)
                        .unidade(unidade)
                        .perfil(perfil)
                        .build());
        principal.setAtribuicoes(atribuicoes);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        when(sgrhService.buscarPerfisUsuario(anyString()))
                .thenReturn(
                        List.of(
                                new PerfilDto(
                                        TEST_USER_ID,
                                        unidade.getCodigo(),
                                        unidade.getNome(),
                                        perfil.name())));
        return auth;
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve detalhar processo para Admin")
    void testDetalharProcesso_sucesso() throws Exception {
        Unidade unidade = unidadeRepo.findById(100L).orElseThrow();
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        subprocessoRepo.save(
                new Subprocesso(
                        processo,
                        unidade,
                        null,
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                        processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo de Teste"));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 para processo inexistente")
    void testDetalharProcesso_naoEncontrado() throws Exception {
        mockMvc.perform(get("/api/processos/{id}/detalhes", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve mostrar 'podeFinalizar' como true para Admin com subprocessos homologados")
    void testPodeFinalizar_true_comAdmin() throws Exception {
        Unidade unidade = unidadeRepo.findById(101L).orElseThrow();
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
        Unidade unidade = unidadeRepo.findById(102L).orElseThrow();
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
    @DisplayName(
            "Deve mostrar 'podeHomologarCadastro' como true para Gestor com cadastro"
                    + " disponibilizado")
    void testPodeHomologarCadastro_true() throws Exception {
        Unidade unidade = unidadeRepo.findById(8L).orElseThrow();
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        org.springframework.security.core.Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(
                new Subprocesso(
                        processo,
                        unidade,
                        null,
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                        processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarCadastro").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarMapa' como true para Gestor com mapa validado")
    void testPodeHomologarMapa_true() throws Exception {
        Unidade unidade = unidadeRepo.findById(9L).orElseThrow();
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);
        org.springframework.security.core.Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(
                new Subprocesso(
                        processo,
                        unidade,
                        null,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarMapa").value(true));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar detalhes da unidade com situação do subprocesso correta")
    void testDetalharProcesso_dadosSubprocesso() throws Exception {
        Unidade unidade = unidadeRepo.findById(100L).orElseThrow();
        processo.setParticipantes(new HashSet<>(Set.of(unidade)));
        processoRepo.save(processo);

        Subprocesso subprocesso =
                new Subprocesso(
                        processo,
                        unidade,
                        null,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.unidades[0].situacaoSubprocesso")
                                .value("MAPEAMENTO_MAPA_HOMOLOGADO"))
                .andExpect(jsonPath("$.unidades[0].dataLimite").exists());
    }
}

package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.Perfil;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.dto.PerfilDto;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("CDU-06: Detalhar processo")
public class CDU06IntegrationTest {

    private static final long TEST_USER_ID = 123456789L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeProcessoRepo unidadeProcessoRepo;

    @MockBean
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

    private void setupSecurityContext(Unidade unidade, Perfil perfil) {
        Usuario principal = new Usuario(
            TEST_USER_ID, "Usuario Teste", "teste@teste.com", "123",
            unidade,
            Collections.singletonList(perfil)
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Mock crucial para a verificação de segurança
        when(sgrhService.buscarPerfisUsuario(anyString()))
            .thenReturn(List.of(new PerfilDto(String.valueOf(TEST_USER_ID), unidade.getCodigo(), unidade.getNome(), perfil.name())));
    }

    private UnidadeProcesso createUnidadeProcesso(Unidade unidade, Processo processo) {
        UnidadeProcesso up = new UnidadeProcesso();
        up.setProcessoCodigo(processo.getCodigo());
        up.setUnidadeCodigo(unidade.getCodigo());
        up.setNome(unidade.getNome());
        up.setSigla(unidade.getSigla());
        return up;
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve detalhar processo com sucesso para Admin")
    void testDetalharProcesso_sucesso() throws Exception {
        Unidade unidade = unidadeRepo.save(new Unidade("Unidade Teste", "UT"));
        unidadeProcessoRepo.save(createUnidadeProcesso(unidade, processo));
        subprocessoRepo.save(new Subprocesso(processo, unidade, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processo.getDataLimite()));

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
        Unidade unidade = unidadeRepo.save(new Unidade("Unidade Admin", "UA"));
        unidadeProcessoRepo.save(createUnidadeProcesso(unidade, processo));
        subprocessoRepo.save(new Subprocesso(processo, unidade, null, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.podeFinalizar").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeFinalizar' como false para não Admin")
    void testPodeFinalizar_false_semAdmin() throws Exception {
        Unidade unidade = unidadeRepo.save(new Unidade("Unidade Chefe", "UC"));
        unidadeProcessoRepo.save(createUnidadeProcesso(unidade, processo));
        setupSecurityContext(unidade, Perfil.CHEFE);
        subprocessoRepo.save(new Subprocesso(processo, unidade, null, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.podeFinalizar").value(false));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarCadastro' como true para Gestor com cadastro disponibilizado")
    void testPodeHomologarCadastro_true() throws Exception {
        Unidade unidade = unidadeRepo.save(new Unidade("Unidade Gestor", "UG"));
        unidadeProcessoRepo.save(createUnidadeProcesso(unidade, processo));
        setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(new Subprocesso(processo, unidade, null, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.podeHomologarCadastro").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarMapa' como true para Gestor com mapa validado")
    void testPodeHomologarMapa_true() throws Exception {
        Unidade unidade = unidadeRepo.save(new Unidade("Unidade Gestor", "UG"));
        unidadeProcessoRepo.save(createUnidadeProcesso(unidade, processo));
        setupSecurityContext(unidade, Perfil.GESTOR);
        subprocessoRepo.save(new Subprocesso(processo, unidade, null, SituacaoSubprocesso.MAPA_VALIDADO, processo.getDataLimite()));

        mockMvc.perform(get("/api/processos/{id}/detalhes", processo.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.podeHomologarMapa").value(true));
    }
}

package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.dto.ProcessoDto;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeMapa;
import sgc.unidade.model.UnidadeMapaRepo;
import sgc.unidade.model.UnidadeRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integração: Contexto de Edição de Mapa (BFF)")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({
        TestSecurityConfig.class,
        sgc.integracao.mocks.TestThymeleafConfig.class,
        sgc.integracao.mocks.TestEventConfig.class
})
@org.springframework.transaction.annotation.Transactional
class SubprocessoContextoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired
    private jakarta.persistence.EntityManager entityManager;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockitoBean
    private SgrhService sgrhService;

    private Unidade unidade;
    private Usuario chefe;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        String adminTitulo = "111111111111";
        String chefeTitulo = "333333333333";
        Long unidadeCodigo = 102L;

        // Update titular via JDBC to avoid Immutable entity issues
        jdbcTemplate.update("UPDATE sgc.vw_unidade SET titulo_titular = ? WHERE codigo = ?",
                chefeTitulo, unidadeCodigo);

        entityManager.clear();

        unidade = unidadeRepo.findById(unidadeCodigo).orElseThrow();
        admin = usuarioRepo.findById(adminTitulo).orElseThrow();
        chefe = usuarioRepo.findById(chefeTitulo).orElseThrow();

        // Initialize lazy collections
        admin.getAtribuicoesTemporarias().size();
        chefe.getAtribuicoesTemporarias().size();

        // Mocks SgrhService
        when(sgrhService.buscarPerfisUsuario(admin.getTituloEleitoral().toString()))
                .thenReturn(List.of(new PerfilDto(admin.getTituloEleitoral().toString(), 100L, "SEDOC", Perfil.ADMIN.name())));
        when(sgrhService.buscarPerfisUsuario(chefe.getTituloEleitoral().toString()))
                .thenReturn(List.of(new PerfilDto(chefe.getTituloEleitoral().toString(), 102L, "SA", Perfil.CHEFE.name())));

        when(sgrhService.buscarUsuarioPorLogin(admin.getTituloEleitoral().toString())).thenReturn(admin);
        when(sgrhService.buscarUsuarioPorLogin(chefe.getTituloEleitoral().toString())).thenReturn(chefe);

        when(sgrhService.buscarUnidadePorSigla("SA")).thenReturn(java.util.Optional.of(sgc.sgrh.dto.UnidadeDto.builder().codigo(102L).sigla("SA").nome("Secretaria Adjunta").build()));

        // Setup attributes
        chefe.setAtribuicoes(new java.util.HashSet<>(java.util.List.of(
            sgc.sgrh.model.UsuarioPerfil.builder().usuario(chefe).perfil(Perfil.CHEFE).unidade(unidade).build()
        )));
        admin.setAtribuicoes(new java.util.HashSet<>(java.util.List.of(
            sgc.sgrh.model.UsuarioPerfil.builder().usuario(admin).perfil(Perfil.ADMIN).unidade(unidadeRepo.findById(100L).orElseThrow()).build()
        )));

        // Ensure UnidadeMapa exists
        Mapa mapa201 = mapaRepo.findById(201L).orElseThrow();
        if (unidadeMapaRepo.findById(unidade.getCodigo()).isEmpty()) {
            UnidadeMapa unidadeMapa = new UnidadeMapa(unidade.getCodigo(), mapa201);
            unidadeMapaRepo.save(unidadeMapa);
        }
    }

    @Test
    @DisplayName("Deve retornar contexto de edição completo para o Chefe")
    void deveRetornarContextoCompleto() throws Exception {
        // Criar processo e subprocesso
        Long subprocessoId = criarEComecarProcessoDeRevisao();

        mockMvc.perform(
                        get("/api/subprocessos/{id}/contexto-edicao", subprocessoId)
                                .with(user(chefe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.sigla", is(unidade.getSigla())))
                .andExpect(jsonPath("$.subprocesso.situacao", is("REVISAO_CADASTRO_EM_ANDAMENTO")))
                .andExpect(jsonPath("$.mapa").exists())
                .andExpect(jsonPath("$.atividadesDisponiveis", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("Deve retornar 404 para subprocesso inexistente")
    void deveRetornar404() throws Exception {
        mockMvc.perform(
                get("/api/subprocessos/{id}/contexto-edicao", 99999L)
                        .with(user(chefe)))
        .andExpect(status().isNotFound());
    }

    private Long criarEComecarProcessoDeRevisao() throws Exception {
        Map<String, Object> criarReqMap = Map.of(
                "descricao", "Processo Revisão",
                "tipo", "REVISAO",
                "dataLimiteEtapa1", LocalDateTime.now().plusDays(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                "unidades", List.of(unidade.getCodigo())
        );
        String reqJson = objectMapper.writeValueAsString(criarReqMap);

        String resJson = mockMvc.perform(post("/api/processos")
                        .with(csrf())
                        .with(user(admin))
                        .contentType("application/json")
                        .content(reqJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ProcessoDto processoDto = objectMapper.readValue(resJson, ProcessoDto.class);

        Map<String, Object> iniciarReqMap = Map.of("tipo", "REVISAO", "unidades", List.of(unidade.getCodigo()));
        String iniciarReqJson = objectMapper.writeValueAsString(iniciarReqMap);

        mockMvc.perform(post("/api/processos/{codigo}/iniciar", processoDto.getCodigo())
                        .with(csrf())
                        .with(user(admin))
                        .contentType("application/json")
                        .content(iniciarReqJson))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Re-fetch users again after clear to avoid LazyInitializationException in this method if accessed
        // But we return simple Long.

        Subprocesso sp = subprocessoRepo.findByProcessoCodigo(processoDto.getCodigo()).get(0);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(sp);
        return sp.getCodigo();
    }
}

package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@DisplayName("CDU-15: Manter Mapa de Competências")
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class CDU15IntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSO_MAPA = "/api/subprocessos/{codigo}/mapa";
    private static final String API_SUBPROCESSO_MAPA_ATUALIZAR = "/api/subprocessos/{codigo}/mapa/atualizar";

    private Subprocesso subprocesso;
    private Atividade atividade1;
    private Atividade atividade2;

    @BeforeEach
    void setUp() {
        // Criar Unidade via Fixture
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Unidade CDU-15");
        unidade.setSigla("U15");
        unidade = unidadeRepo.save(unidade);

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo Teste CDU-15");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        // Criar Subprocesso
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso = subprocessoRepo.save(subprocesso);

        // Criar Mapa
        Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        // Atualizar referência no subprocesso
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        atividade1 = Atividade.builder().mapa(mapa).descricao("Atividade 1").build();
        atividade1 = atividadeRepo.save(atividade1);

        atividade2 = Atividade.builder().mapa(mapa).descricao("Atividade 2").build();
        atividade2 = atividadeRepo.save(atividade2);
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve criar competências em um mapa vazio e mudar situação do subprocesso")
    void deveCriarCompetenciasEmMapaVazio() throws Exception {
        // Given
        var request = new SalvarMapaRequest(
                "Observações iniciais",
                List.of(
                        new CompetenciaMapaDto(
                                null,
                                "Nova Competência 1",
                                List.of(atividade1.getCodigo())),
                        new CompetenciaMapaDto(
                                null,
                                "Nova Competência 2",
                                List.of(atividade2.getCodigo()))));

        // When & Then
        mockMvc.perform(
                        post(
                                API_SUBPROCESSO_MAPA_ATUALIZAR,
                                subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(2))
                .andExpect(jsonPath("$.competencias[0].descricao").value("Nova Competência 1"))
                .andExpect(jsonPath("$.competencias[1].descricao").value("Nova Competência 2"));

        Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve editar, adicionar e remover competências de um mapa existente")
    void deveEditarAdicionarRemoverCompetencias() throws Exception {
        // First, create an initial map
        var initialRequest = new SalvarMapaRequest(
                "Observações",
                List.of(
                        new CompetenciaMapaDto(
                                null,
                                "Competência Original",
                                List.of(atividade1.getCodigo()))));
        String responseBody = mockMvc.perform(
                        post(
                                API_SUBPROCESSO_MAPA_ATUALIZAR,
                                subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long competenciaOriginalId = objectMapper.readTree(responseBody).at("/competencias/0/codigo").asLong();

        // Now, edit the map
        var updateRequest = new SalvarMapaRequest(
                "Observações atualizadas",
                List.of(
                        new CompetenciaMapaDto(
                                competenciaOriginalId,
                                "Competência Editada",
                                List.of(atividade1.getCodigo(),
                                        atividade2.getCodigo())),
                        new CompetenciaMapaDto(
                                null,
                                "Competência Adicional",
                                List.of(atividade2.getCodigo()))));

        mockMvc.perform(
                        post(
                                API_SUBPROCESSO_MAPA_ATUALIZAR,
                                subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(2))
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " Editada')].atividadesCodigos.length()")
                                .value(2))
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " Adicional')].atividadesCodigos.length()")
                                .value(1));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 400 se tentar salvar mapa para subprocesso em situação inválida")
    void deveRetornarErroParaSituacaoInvalida() throws Exception {
        // Given
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(subprocesso);

        var request = new SalvarMapaRequest(
                "Obs",
                List.of(
                        new CompetenciaMapaDto(
                                null, "Competência", List.of(atividade1.getCodigo()))));

        // When & Then
        mockMvc.perform(
                        post(
                                API_SUBPROCESSO_MAPA_ATUALIZAR,
                                subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve obter o mapa completo do subprocesso")
    void deveObterMapaCompleto() throws Exception {
        // Given
        var request = new SalvarMapaRequest(
                "Observações",
                List.of(
                        new CompetenciaMapaDto(
                                null,
                                "Competência para GET",
                                List.of(atividade1.getCodigo()))));
        mockMvc.perform(
                post(API_SUBPROCESSO_MAPA_ATUALIZAR, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get(API_SUBPROCESSO_MAPA, subprocesso.getCodigo()))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocessoCodigo").value(subprocesso.getCodigo()))
                .andExpect(jsonPath("$.competencias.length()").value(1))
                .andExpect(jsonPath("$.competencias[0].descricao").value("Competência para GET"));
    }

    @Nested
    @DisplayName("Testes de CRUD de Competência Individual")
    class CrudCompetenciaTests {

        @Test
        @WithMockAdmin
        @DisplayName("Deve adicionar uma nova competência a um mapa")
        void deveAdicionarCompetencia() throws Exception {
            var request = new CompetenciaRequest(
                    "Nova Competência", List.of(atividade1.getCodigo()));

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{codSubprocesso}/competencias",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.competencias.length()").value(1))
                    .andExpect(jsonPath("$.competencias[0].descricao").value("Nova Competência"));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve atualizar uma competência existente")
        void deveAtualizarCompetencia() throws Exception {
            // Adicionar primeiro
            var addRequest = new CompetenciaRequest(
                    "Competência Original", List.of(atividade1.getCodigo()));
            var result = mockMvc.perform(
                            post(
                                    "/api/subprocessos/{codSubprocesso}/competencias",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(addRequest)))
                    .andReturn();
            var codCompetencia = objectMapper
                    .readTree(result.getResponse().getContentAsString())
                    .at("/competencias/0/codigo")
                    .asLong();

            // Atualizar
            var updateRequest = new CompetenciaRequest(
                    "Competência Atualizada",
                    List.of(atividade1.getCodigo(), atividade2.getCodigo()));
            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{codSubprocesso}/competencias/{codCompetencia}/atualizar",
                                    subprocesso.getCodigo(),
                                    codCompetencia)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.competencias[0].descricao")
                                    .value("Competência Atualizada"))
                    .andExpect(jsonPath("$.competencias[0].atividadesCodigos.length()").value(2));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve remover uma competência existente")
        void deveRemoverCompetencia() throws Exception {
            // Adicionar primeiro
            var addRequest = new CompetenciaRequest(
                    "Competência a ser removida", List.of(atividade1.getCodigo()));
            var result = mockMvc.perform(
                            post(
                                    "/api/subprocessos/{codSubprocesso}/competencias",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(addRequest)))
                    .andReturn();
            var codCompetencia = objectMapper
                    .readTree(result.getResponse().getContentAsString())
                    .at("/competencias/0/codigo")
                    .asLong();

            // Remover
            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{codSubprocesso}/competencias/{codCompetencia}/remover",
                                    subprocesso.getCodigo(),
                                    codCompetencia)
                                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.competencias.length()").value(0));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar 409 se tentar editar mapa para subprocesso em situação inválida")
        void deveRetornarErroParaSituacaoInvalidaCrud() throws Exception {
            subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);

            var request = new CompetenciaRequest(
                    "Nova Competência", List.of(atividade1.getCodigo()));

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{codSubprocesso}/competencias",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent());
        }
    }
}

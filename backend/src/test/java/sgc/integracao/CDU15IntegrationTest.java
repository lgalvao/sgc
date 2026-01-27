package sgc.integracao;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import sgc.Sgc;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

@org.springframework.boot.test.context.SpringBootTest(classes = Sgc.class)
@DisplayName("CDU-15: Manter Mapa de Competências")
@Import({ TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class })
@ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
@Tag("integration")
class CDU15IntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSO_MAPA = "/api/subprocessos/{codigo}/mapa";

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

        // Criar Mapa
        Mapa mapa = MapaFixture.mapaPadrao(null);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        // Criar Subprocesso
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);

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
                        "/api/subprocessos/{codSubprocesso}/mapa/atualizar",
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
                        "/api/subprocessos/{codSubprocesso}/mapa/atualizar",
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
                        "/api/subprocessos/{codSubprocesso}/mapa/atualizar",
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
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(subprocesso);

        var request = new SalvarMapaRequest(
                "Obs",
                List.of(
                        new CompetenciaMapaDto(
                                null, "Competência", List.of(atividade1.getCodigo()))));

        // When & Then
        mockMvc.perform(
                post(
                        "/api/subprocessos/{codSubprocesso}/mapa/atualizar",
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
                post("/api/subprocessos/{codSubprocesso}/mapa/atualizar", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        // When & Then
        mockMvc.perform(get(API_SUBPROCESSO_MAPA, subprocesso.getCodigo()))
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
            var request = new sgc.subprocesso.dto.CompetenciaRequest(
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
            var addRequest = new sgc.subprocesso.dto.CompetenciaRequest(
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
            var updateRequest = new sgc.subprocesso.dto.CompetenciaRequest(
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
            var addRequest = new sgc.subprocesso.dto.CompetenciaRequest(
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
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);

            var request = new sgc.subprocesso.dto.CompetenciaRequest(
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

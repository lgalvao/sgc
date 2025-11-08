package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import sgc.atividade.model.Atividade;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@DisplayName("CDU-15: Manter Mapa de Competências")
@Import(TestThymeleafConfig.class)
class CDU15IntegrationTest extends BaseIntegrationTest {

    private static final String API_SUBPROCESSO_MAPA = "/api/subprocessos/{codigo}/mapa";

    private Subprocesso subprocesso;
    private Atividade atividade1;
    private Atividade atividade2;

    @BeforeEach
    void setUp() {
        Unidade unidade = unidadeRepo.findById(10L).orElseThrow(); // Use existing SESEL

        Processo processo = new Processo();
        processo.setDescricao("Processo Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataCriacao(java.time.LocalDateTime.now());
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        Mapa mapa = mapaRepo.save(new Mapa());

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(processo);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(sp);

        atividade1 = atividadeRepo.save(new Atividade(mapa, "Atividade 1"));
        atividade2 = atividadeRepo.save(new Atividade(mapa, "Atividade 2"));
    }

    @Test
    @WithMockGestor
    @DisplayName("Deve criar competências em um mapa vazio e mudar situação do subprocesso")
    void deveCriarCompetenciasEmMapaVazio() throws Exception {
        // Given
        var request = new SalvarMapaRequest(
            "Observações iniciais",
            List.of(
                new CompetenciaMapaDto(null, "Nova Competência 1", List.of(atividade1.getCodigo())),
                new CompetenciaMapaDto(null, "Nova Competência 2", List.of(atividade2.getCodigo()))
            )
        );

        // When & Then
        mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/mapa/atualizar", subprocesso.getCodigo())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competencias.length()").value(2))
            .andExpect(jsonPath("$.competencias[0].descricao").value("Nova Competência 1"))
            .andExpect(jsonPath("$.competencias[1].descricao").value("Nova Competência 2"));

        Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_CRIADO);
    }

    @Test
    @WithMockGestor
    @DisplayName("Deve editar, adicionar e remover competências de um mapa existente")
    void deveEditarAdicionarRemoverCompetencias() throws Exception {
        // First, create an initial map
        var initialRequest = new SalvarMapaRequest(
            "Observações",
            List.of(new CompetenciaMapaDto(null, "Competência Original", List.of(atividade1.getCodigo())))
        );
        String responseBody = mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/mapa/atualizar", subprocesso.getCodigo())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long competenciaOriginalId = objectMapper.readTree(responseBody).at("/competencias/0/codigo").asLong();

        // Now, edit the map
        var updateRequest = new SalvarMapaRequest(
            "Observações atualizadas",
            List.of(
                new CompetenciaMapaDto(competenciaOriginalId, "Competência Editada", List.of(atividade1.getCodigo(), atividade2.getCodigo())),
                new CompetenciaMapaDto(null, "Competência Adicional", List.of(atividade2.getCodigo()))
            )
        );

        mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/mapa/atualizar", subprocesso.getCodigo())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competencias.length()").value(2))
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência Editada')].atividadesCodigos.length()").value(2))
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência Adicional')].atividadesCodigos.length()").value(1));
    }

    @Test
    @WithMockGestor
    @DisplayName("Deve retornar 400 se tentar salvar mapa para subprocesso em situação inválida")
    void deveRetornarErroParaSituacaoInvalida() throws Exception {
        // Given
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(subprocesso);

        var request = new SalvarMapaRequest("Obs", List.of());

        // When & Then
        mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/mapa/atualizar", subprocesso.getCodigo())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockGestor
    @DisplayName("Deve obter o mapa completo do subprocesso")
    void deveObterMapaCompleto() throws Exception {
        // Given
        var request = new SalvarMapaRequest(
            "Observações",
            List.of(new CompetenciaMapaDto(null, "Competência para GET", List.of(atividade1.getCodigo())))
        );
        mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/mapa/atualizar", subprocesso.getCodigo())
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
        @WithMockGestor
        @DisplayName("Deve adicionar uma nova competência a um mapa")
        void deveAdicionarCompetencia() throws Exception {
            var request = new sgc.subprocesso.dto.CompetenciaReq("Nova Competência", List.of(atividade1.getCodigo()));

            mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(1))
                .andExpect(jsonPath("$.competencias[0].descricao").value("Nova Competência"));
        }

        @Test
        @WithMockGestor
        @DisplayName("Deve atualizar uma competência existente")
        void deveAtualizarCompetencia() throws Exception {
            // Adicionar primeiro
            var addRequest = new sgc.subprocesso.dto.CompetenciaReq("Competência Original", List.of(atividade1.getCodigo()));
            var result = mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn();
            var codCompetencia = objectMapper.readTree(result.getResponse().getContentAsString()).at("/competencias/0/codigo").asLong();

            // Atualizar
            var updateRequest = new sgc.subprocesso.dto.CompetenciaReq("Competência Atualizada", List.of(atividade1.getCodigo(), atividade2.getCodigo()));
            mockMvc.perform(put("/api/subprocessos/{codSubprocesso}/competencias/{codCompetencia}", subprocesso.getCodigo(), codCompetencia)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias[0].descricao").value("Competência Atualizada"))
                .andExpect(jsonPath("$.competencias[0].atividadesCodigos.length()").value(2));
        }

        @Test
        @WithMockGestor
        @DisplayName("Deve remover uma competência existente")
        void deveRemoverCompetencia() throws Exception {
            // Adicionar primeiro
            var addRequest = new sgc.subprocesso.dto.CompetenciaReq("Competência a ser removida", List.of(atividade1.getCodigo()));
            var result = mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn();
            var codCompetencia = objectMapper.readTree(result.getResponse().getContentAsString()).at("/competencias/0/codigo").asLong();

            // Remover
            mockMvc.perform(delete("/api/subprocessos/{codSubprocesso}/competencias/{codCompetencia}", subprocesso.getCodigo(), codCompetencia)
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(0));
        }

        @Test
        @WithMockGestor
        @DisplayName("Deve retornar 409 se tentar editar mapa para subprocesso em situação inválida")
        void deveRetornarErroParaSituacaoInvalidaCrud() throws Exception {
            subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);

            var request = new sgc.subprocesso.dto.CompetenciaReq("Nova Competência", List.of(atividade1.getCodigo()));

            mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
        }
    }
}
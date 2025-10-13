package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.processo.SituacaoProcesso;
import sgc.integracao.mocks.WithMockGestor;
import sgc.processo.modelo.TipoProcesso;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-15: Manter Mapa de Competências")
class CDU15IntegrationTest {

    private static final String API_SUBPROCESSO_MAPA = "/api/subprocessos/{id}/mapa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    private Subprocesso subprocesso;
    private Atividade atividade1;
    private Atividade atividade2;

    @BeforeEach
    void setUp() {
        Unidade unidade = unidadeRepo.save(new Unidade("Teste", "TST"));

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
        mockMvc.perform(put(API_SUBPROCESSO_MAPA, subprocesso.getCodigo())
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
        String responseBody = mockMvc.perform(put(API_SUBPROCESSO_MAPA, subprocesso.getCodigo())
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

        mockMvc.perform(put(API_SUBPROCESSO_MAPA, subprocesso.getCodigo())
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
        mockMvc.perform(put(API_SUBPROCESSO_MAPA, subprocesso.getCodigo())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
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
        mockMvc.perform(put(API_SUBPROCESSO_MAPA, subprocesso.getCodigo())
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
}
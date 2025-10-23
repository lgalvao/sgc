package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-15: Manter Mapa de Competências (CRUD Competencia)")
class CDU15CrudCompetenciaIntegrationTest {

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

    @Nested
    @DisplayName("Testes de Adição de Competência")
    class AdicionarCompetenciaTest {

        @Test
        @WithMockGestor
        @DisplayName("Deve adicionar uma nova competência a um mapa")
        void deveAdicionarCompetencia() throws Exception {
            var request = new CompetenciaReq("Nova Competência", List.of(atividade1.getCodigo()));

            mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(1))
                .andExpect(jsonPath("$.competencias[0].descricao").value("Nova Competência"));
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Competência")
    class AtualizarCompetenciaTest {
        @Test
        @WithMockGestor
        @DisplayName("Deve atualizar uma competência existente")
        void deveAtualizarCompetencia() throws Exception {
            // Adicionar primeiro
            var addRequest = new CompetenciaReq("Competência Original", List.of(atividade1.getCodigo()));
            var result = mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn();
            var competenciaId = objectMapper.readTree(result.getResponse().getContentAsString()).at("/competencias/0/codigo").asLong();

            // Atualizar
            var updateRequest = new CompetenciaReq("Competência Atualizada", List.of(atividade1.getCodigo(), atividade2.getCodigo()));
            mockMvc.perform(put("/api/subprocessos/{codSubprocesso}/competencias/{competenciaId}", subprocesso.getCodigo(), competenciaId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias[0].descricao").value("Competência Atualizada"))
                .andExpect(jsonPath("$.competencias[0].atividadesCodigos.length()").value(2));
        }
    }

    @Nested
    @DisplayName("Testes de Remoção de Competência")
    class RemoverCompetenciaTest {
        @Test
        @WithMockGestor
        @DisplayName("Deve remover uma competência existente")
        void deveRemoverCompetencia() throws Exception {
            // Adicionar primeiro
            var addRequest = new CompetenciaReq("Competência a ser removida", List.of(atividade1.getCodigo()));
            var result = mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn();
            var competenciaId = objectMapper.readTree(result.getResponse().getContentAsString()).at("/competencias/0/codigo").asLong();

            // Remover
            mockMvc.perform(delete("/api/subprocessos/{codSubprocesso}/competencias/{competenciaId}", subprocesso.getCodigo(), competenciaId)
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(0));
        }
    }

    @Test
    @WithMockGestor
    @DisplayName("Deve retornar 409 se tentar editar mapa para subprocesso em situação inválida")
    void deveRetornarErroParaSituacaoInvalida() throws Exception {
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(subprocesso);

        var request = new CompetenciaReq("Nova Competência", List.of(atividade1.getCodigo()));

        mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencias", subprocesso.getCodigo())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }
}

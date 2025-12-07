package sgc.integracao;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

/**
 * Testes de integração focados no endpoint /api/subprocessos/buscar. Este endpoint é crítico para a
 * navegação do frontend para a tela de subprocesso.
 */
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Endpoint: /api/subprocessos/buscar")
@Import(sgc.integracao.mocks.TestSecurityConfig.class)
class SubprocessoBuscarIntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSOS_BUSCAR = "/api/subprocessos/buscar";

    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;

    private Unidade unidade;
    private Processo processoEmAndamento;
    private Processo processoFinalizado;
    private Subprocesso subprocessoEmAndamento;
    private Subprocesso subprocessoFinalizado;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(11L).orElseThrow(); // SENIC

        // Processo em andamento
        processoEmAndamento =
                new Processo(
                        "Processo em Andamento",
                        TipoProcesso.MAPEAMENTO,
                        SituacaoProcesso.EM_ANDAMENTO,
                        LocalDateTime.now().plusDays(30));
        processoEmAndamento.setParticipantes(Set.of(unidade));
        processoRepo.save(processoEmAndamento);

        var mapaEmAndamento = mapaRepo.save(new Mapa());
        subprocessoEmAndamento =
                new Subprocesso(
                        processoEmAndamento,
                        unidade,
                        mapaEmAndamento,
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                        processoEmAndamento.getDataLimite());
        subprocessoRepo.save(subprocessoEmAndamento);

        // Processo finalizado
        processoFinalizado =
                new Processo(
                        "Processo Finalizado",
                        TipoProcesso.MAPEAMENTO,
                        SituacaoProcesso.FINALIZADO,
                        LocalDateTime.now().plusDays(30));
        processoFinalizado.setParticipantes(Set.of(unidade));
        processoFinalizado.setDataFinalizacao(LocalDateTime.now());
        processoRepo.save(processoFinalizado);

        var mapaFinalizado = mapaRepo.save(new Mapa());
        subprocessoFinalizado =
                new Subprocesso(
                        processoFinalizado,
                        unidade,
                        mapaFinalizado,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        processoFinalizado.getDataLimite());
        subprocessoRepo.save(subprocessoFinalizado);
    }

    @Nested
    @DisplayName("Buscar por processo e unidade")
    class BuscarPorProcessoEUnidade {

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar subprocesso para processo em andamento")
        void deveRetornarSubprocesso_QuandoProcessoEmAndamento() throws Exception {
            mockMvc.perform(
                            get(API_SUBPROCESSOS_BUSCAR)
                                    .param(
                                            "codProcesso",
                                            processoEmAndamento.getCodigo().toString())
                                    .param("siglaUnidade", unidade.getSigla()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.codigo", is(subprocessoEmAndamento.getCodigo().intValue())));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar subprocesso para processo FINALIZADO")
        void deveRetornarSubprocesso_QuandoProcessoFinalizado() throws Exception {
            mockMvc.perform(
                            get(API_SUBPROCESSOS_BUSCAR)
                                    .param("codProcesso", processoFinalizado.getCodigo().toString())
                                    .param("siglaUnidade", unidade.getSigla()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.codigo", is(subprocessoFinalizado.getCodigo().intValue())));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar 404 quando sigla de unidade não existe")
        void deveRetornar404_QuandoSiglaUnidadeNaoExiste() throws Exception {
            mockMvc.perform(
                            get(API_SUBPROCESSOS_BUSCAR)
                                    .param(
                                            "codProcesso",
                                            processoEmAndamento.getCodigo().toString())
                                    .param("siglaUnidade", "UNIDADE_INEXISTENTE"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar 404 quando processo não existe")
        void deveRetornar404_QuandoProcessoNaoExiste() throws Exception {
            mockMvc.perform(
                            get(API_SUBPROCESSOS_BUSCAR)
                                    .param("codProcesso", "999999")
                                    .param("siglaUnidade", unidade.getSigla()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar 404 quando unidade existe mas não participa do processo")
        void deveRetornar404_QuandoUnidadeNaoParticipaDoProcesso() throws Exception {
            // Busca uma unidade que existe mas não participa do processo
            Unidade outraUnidade = unidadeRepo.findById(1L).orElseThrow(); // SEDOC

            mockMvc.perform(
                            get(API_SUBPROCESSOS_BUSCAR)
                                    .param(
                                            "codProcesso",
                                            processoEmAndamento.getCodigo().toString())
                                    .param("siglaUnidade", outraUnidade.getSigla()))
                    .andExpect(status().isNotFound());
        }
    }
}

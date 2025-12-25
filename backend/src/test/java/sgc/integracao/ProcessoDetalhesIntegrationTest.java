package sgc.integracao;

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
import sgc.mapa.api.model.Mapa;
import sgc.mapa.api.model.MapaRepo;
import sgc.processo.api.model.Processo;
import sgc.processo.api.model.ProcessoRepo;
import sgc.processo.api.model.SituacaoProcesso;
import sgc.processo.api.model.TipoProcesso;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.api.model.Unidade;
import sgc.unidade.api.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração focados no endpoint /api/processos/{id}. Este endpoint é
 * chamado pelo
 * ProcessoView para mostrar a lista de unidades participantes.
 */
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Endpoint: /api/processos/{id}")
@Import(sgc.integracao.mocks.TestSecurityConfig.class)
class ProcessoDetalhesIntegrationTest extends BaseIntegrationTest {
        private static final String API_PROCESSO_DETALHES = "/api/processos/{codProcesso}";

        @Autowired
        private ProcessoRepo processoRepo;

        @Autowired
        private UnidadeRepo unidadeRepo;

        @Autowired
        private SubprocessoRepo subprocessoRepo;

        @Autowired
        private MapaRepo mapaRepo;

        private Unidade unidade;
        private Processo processoEmAndamento;
        private Processo processoFinalizado;

        @BeforeEach
        void setUp() {
                unidade = unidadeRepo.findById(11L).orElseThrow(); // SENIC

                // Processo em andamento
                processoEmAndamento = new Processo(
                                "Processo em Andamento",
                                TipoProcesso.MAPEAMENTO,
                                SituacaoProcesso.EM_ANDAMENTO,
                                LocalDateTime.now().plusDays(30));
                processoEmAndamento.setParticipantes(Set.of(unidade));
                processoEmAndamento = processoRepo.saveAndFlush(processoEmAndamento);

                var mapaEmAndamento = mapaRepo.save(new Mapa());
                var subprocessoEmAndamento = new Subprocesso(
                                processoEmAndamento,
                                unidade,
                                mapaEmAndamento,
                                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                                processoEmAndamento.getDataLimite());
                subprocessoRepo.save(subprocessoEmAndamento);

                // Processo finalizado
                processoFinalizado = new Processo(
                                "Processo Finalizado",
                                TipoProcesso.MAPEAMENTO,
                                SituacaoProcesso.FINALIZADO,
                                LocalDateTime.now().plusDays(30));
                processoFinalizado.setParticipantes(Set.of(unidade));
                processoFinalizado.setDataFinalizacao(LocalDateTime.now());
                processoFinalizado = processoRepo.saveAndFlush(processoFinalizado);

                var mapaFinalizado = mapaRepo.save(new Mapa());
                var subprocessoFinalizado = new Subprocesso(
                                processoFinalizado,
                                unidade,
                                mapaFinalizado,
                                SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                                processoFinalizado.getDataLimite());
                subprocessoRepo.save(subprocessoFinalizado);
        }

        @Nested
        @DisplayName("Detalhes do processo com unidades participantes")
        class DetalhesProcesso {
                @Test
                @WithMockAdmin
                @DisplayName("Deve retornar detalhes com unidades para processo em andamento")
                void deveRetornarDetalhesComUnidades_QuandoProcessoEmAndamento() throws Exception {
                        mockMvc.perform(get(API_PROCESSO_DETALHES, processoEmAndamento.getCodigo()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.codigo", is(processoEmAndamento.getCodigo().intValue())))
                                        .andExpect(jsonPath("$.situacao", is("EM_ANDAMENTO")));
                }

                @Test
                @WithMockAdmin
                @DisplayName("Deve retornar detalhes com unidades para processo FINALIZADO")
                void deveRetornarDetalhesComUnidades_QuandoProcessoFinalizado() throws Exception {
                        mockMvc.perform(get(API_PROCESSO_DETALHES, processoFinalizado.getCodigo()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.codigo", is(processoFinalizado.getCodigo().intValue())))
                                        .andExpect(jsonPath("$.situacao", is("FINALIZADO")));
                }

                @Test
                @WithMockAdmin
                @DisplayName("Deve retornar 404 quando processo não existe")
                void deveRetornar404_QuandoProcessoNaoExiste() throws Exception {
                        mockMvc.perform(get(API_PROCESSO_DETALHES, 999999L)).andExpect(status().isNotFound());
                }
        }
}

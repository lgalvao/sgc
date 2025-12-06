package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração que replica o fluxo completo do sistema:
 * 1. Admin cria processo de mapeamento
 * 2. Admin inicia processo
 * 3. Transições de estado até MAPA_HOMOLOGADO
 * 4. Admin finaliza processo
 * 5. Tenta buscar subprocesso por processo e sigla da unidade
 * 
 * Este teste replica o que o E2E CDU-11 faz para identificar
 * por que "Unidade não encontrada" ocorre após finalização.
 */
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Fluxo Completo: Processo de Mapeamento até Finalização")
@Import(sgc.integracao.mocks.TestSecurityConfig.class)
class FluxoCompletoProcessoIntegrationTest extends BaseIntegrationTest {
        private static final String API_SUBPROCESSOS_BUSCAR = "/api/subprocessos/buscar";

        @Autowired
        private ProcessoService processoService;
        @Autowired
        private SubprocessoRepo subprocessoRepo;
        @Autowired
        private SubprocessoDtoService subprocessoDtoService;
        @Autowired
        private UnidadeRepo unidadeRepo;

        private Unidade unidadeSENIC;
        private Long codProcesso;
        private Long codSubprocesso;

        @BeforeEach
        void setUp() {
                // Buscar a unidade SENIC (código 11 no data.sql do backend)
                unidadeSENIC = unidadeRepo.findBySigla("SENIC")
                                .orElseThrow(() -> new RuntimeException("Unidade SENIC não encontrada"));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve poder buscar subprocesso após fluxo completo de finalização")
        void devePoderBuscarSubprocesso_AposFluxoCompleto() throws Exception {
                // ============================================================
                // PASSO 1: Admin cria processo de mapeamento
                // ============================================================
                CriarProcessoReq criarReq = CriarProcessoReq.builder()
                                .descricao("Processo de Mapeamento para Teste")
                                .tipo(TipoProcesso.MAPEAMENTO)
                                .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                                .unidades(List.of(unidadeSENIC.getCodigo()))
                                .build();

                var processoCriado = processoService.criar(criarReq);
                codProcesso = processoCriado.getCodigo();
                System.out.println("Processo criado com código: " + codProcesso);

                // ============================================================
                // PASSO 2: Admin inicia processo
                // ============================================================
                var errosIniciacao = processoService.iniciarProcessoMapeamento(
                                codProcesso,
                                List.of(unidadeSENIC.getCodigo()));
                assertThat(errosIniciacao).isEmpty();
                System.out.println("Processo iniciado");

                // Buscar o subprocesso criado
                var subprocessos = processoService.listarTodosSubprocessos(codProcesso);
                assertThat(subprocessos).hasSize(1);
                codSubprocesso = subprocessos.get(0).getCodigo();
                System.out.println("Subprocesso criado com código: " + codSubprocesso);

                // ============================================================
                // PASSO 3-7: Simular transições de estado diretamente no repositório
                // (Simplificação para focar no problema principal)
                // ============================================================
                var subprocesso = subprocessoRepo.findById(codSubprocesso)
                                .orElseThrow(() -> new RuntimeException("Subprocesso não encontrado"));

                // Alterar situação para MAPA_HOMOLOGADO (pré-requisito para finalizar)
                subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
                subprocessoRepo.saveAndFlush(subprocesso);
                System.out.println("Subprocesso atualizado para situação: " + subprocesso.getSituacao());

                // Verificar situação antes de finalizar via service
                var subprocessoAntesFinalizar = subprocessoDtoService.obterPorProcessoEUnidade(
                                codProcesso, unidadeSENIC.getCodigo());
                System.out.println("Situação antes de finalizar: " + subprocessoAntesFinalizar.getSituacao());
                assertThat(subprocessoAntesFinalizar.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);

                // ============================================================
                // PASSO 8: Finalizar processo (Admin)
                // ============================================================
                processoService.finalizar(codProcesso);
                System.out.println("Processo finalizado");

                // ============================================================
                // PASSO 9: Buscar subprocesso via API (como o frontend faz)
                // ============================================================
                System.out.println("Buscando subprocesso via API...");
                System.out.println("codProcesso: " + codProcesso);
                System.out.println("siglaUnidade: " + unidadeSENIC.getSigla());

                // Esta é a chamada que o frontend faz ao navegar para SubprocessoView
                var result = mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                                .param("codProcesso", codProcesso.toString())
                                .param("siglaUnidade", unidadeSENIC.getSigla()))
                                .andExpect(status().isOk())
                                .andReturn();

                System.out.println("Resposta API: " + result.getResponse().getContentAsString());

                mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                                .param("codProcesso", codProcesso.toString())
                                .param("siglaUnidade", unidadeSENIC.getSigla()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.codigo", is(codSubprocesso.intValue())));

                System.out.println("✅ Subprocesso encontrado via API após finalização!");
        }
}

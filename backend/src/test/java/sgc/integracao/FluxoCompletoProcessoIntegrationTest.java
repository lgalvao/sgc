package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração que replica o fluxo completo do sistema para um processo
 * de mapeamento:
 * 1. Admin cria processo de mapeamento
 * 2. Admin inicia processo
 * 3. Transições de estado até MAPA_HOMOLOGADO
 * 4. Admin finaliza processo
 * 5. Tenta buscar subprocesso por processo e sigla da unidade
 *
 */
@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Fluxo Completo: Processo de Mapeamento até Finalização")
@Import(sgc.integracao.mocks.TestSecurityConfig.class)
class FluxoCompletoProcessoIntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSOS_BUSCAR = "/api/subprocessos/buscar";

    @Autowired
    private ProcessoFacade processoFacade;

    @Autowired
    private SubprocessoFacade subprocessoFacade;

    private Unidade unidadeSENIC;

    @BeforeEach
    void setUp() {
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
        CriarProcessoRequest criarReq = CriarProcessoRequest.builder()
                .descricao("Processo de Mapeamento para Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                .unidades(List.of(unidadeSENIC.getCodigo()))
                .build();

        var processoCriado = processoFacade.criar(criarReq);
        Long codProcesso = processoCriado.getCodigo();

        // ============================================================
        // PASSO 2: Admin inicia processo
        // ============================================================
        var errosIniciacao = processoFacade.iniciarProcessoMapeamento(
                codProcesso, List.of(unidadeSENIC.getCodigo()));
        assertThat(errosIniciacao).isEmpty();

        // Buscar o subprocesso criado
        var subprocessos = processoFacade.listarTodosSubprocessos(codProcesso);
        assertThat(subprocessos).hasSize(1);
        Long codSubprocesso = subprocessos.getFirst().getCodigo();

        // ============================================================
        // PASSO 3-7: Simular transições de estado diretamente no repositório
        // (Simplificação para focar no problema principal)
        // ============================================================
        var subprocesso = subprocessoRepo
                .findById(codSubprocesso)
                .orElseThrow(() -> new RuntimeException("Subprocesso não encontrado"));

        // Alterar situação para MAPA_HOMOLOGADO (pré-requisito para finalizar)
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoRepo.saveAndFlush(subprocesso);

        // Verificar situação antes de finalizar via service
        var subprocessoAntesFinalizar = subprocessoFacade.obterPorProcessoEUnidade(
                codProcesso, unidadeSENIC.getCodigo());

        assertThat(subprocessoAntesFinalizar.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        // ============================================================
        // PASSO 8: Finalizar processo (Admin)
        // ============================================================
        processoFacade.finalizar(codProcesso);

        // ============================================================
        // PASSO 9: Buscar subprocesso via API (como o frontend faz)
        // ============================================================
        // Esta é a chamada que o frontend faz ao navegar para SubprocessoView
        mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                        .param("codProcesso", codProcesso.toString())
                        .param("siglaUnidade", unidadeSENIC.getSigla()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(codSubprocesso.intValue())));
    }
}

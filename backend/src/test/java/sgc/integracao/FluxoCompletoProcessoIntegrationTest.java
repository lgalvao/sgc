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
import sgc.atividade.service.AtividadeService;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoService;
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
 * 3. Admin homologa cadastro (aceita)
 * 4. Admin homologa mapa
 * 5. Admin finaliza processo
 * 6. Tenta buscar subprocesso por processo e sigla da unidade
 * 
 * Este teste replica exatamente o que o E2E CDU-11 faz para identificar
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
    private SubprocessoService subprocessoService;
    @Autowired
    private SubprocessoDtoService subprocessoDtoService;
    @Autowired
    private AtividadeService atividadeService;
    @Autowired
    private UnidadeRepo unidadeRepo;

    private Unidade unidadeSECAO221;
    private Long codProcesso;
    private Long codSubprocesso;

    @BeforeEach
    void setUp() {
        // Buscar a unidade SECAO_221 (código 18 no seed.sql)
        unidadeSECAO221 = unidadeRepo.findBySigla("SECAO_221")
                .orElseThrow(() -> new RuntimeException("Unidade SECAO_221 não encontrada"));
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
                .unidades(List.of(unidadeSECAO221.getCodigo()))
                .build();

        var processoCriado = processoService.criar(criarReq);
        codProcesso = processoCriado.getCodigo();
        System.out.println("Processo criado com código: " + codProcesso);

        // ============================================================
        // PASSO 2: Admin inicia processo
        // ============================================================
        var errosIniciacao = processoService.iniciarProcessoMapeamento(
                codProcesso,
                List.of(unidadeSECAO221.getCodigo()));
        assertThat(errosIniciacao).isEmpty();
        System.out.println("Processo iniciado");

        // Buscar o subprocesso criado
        var subprocessos = processoService.listarTodosSubprocessos(codProcesso);
        assertThat(subprocessos).hasSize(1);
        codSubprocesso = subprocessos.get(0).getCodigo();
        System.out.println("Subprocesso criado com código: " + codSubprocesso);

        // ============================================================
        // PASSO 3: Disponibilizar cadastro (simular Chefe)
        // ============================================================
        subprocessoService.disponibilizar(codSubprocesso);
        System.out.println("Cadastro disponibilizado");

        // ============================================================
        // PASSO 4: Aceitar cadastro (Admin)
        // ============================================================
        subprocessoService.aceitarCadastro(codSubprocesso);
        System.out.println("Cadastro aceito");

        // ============================================================
        // PASSO 5: Disponibilizar mapa (o teste pode precisar de competências)
        // ============================================================
        subprocessoService.disponibilizarMapa(codSubprocesso);
        System.out.println("Mapa disponibilizado");

        // ============================================================
        // PASSO 6: Aceitar mapa (Gestor/Admin)
        // ============================================================
        subprocessoService.aceitarMapa(codSubprocesso);
        System.out.println("Mapa aceito");

        // ============================================================
        // PASSO 7: Homologar mapa (Chefe)
        // ============================================================
        subprocessoService.homologarMapa(codSubprocesso);
        System.out.println("Mapa homologado");

        // Verificar situação antes de finalizar
        var subprocessoAntesFinalizar = subprocessoDtoService.obterPorProcessoEUnidade(
                codProcesso, unidadeSECAO221.getCodigo());
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
        System.out.println("siglaUnidade: " + unidadeSECAO221.getSigla());

        // Esta é a chamada que o frontend faz ao navegar para SubprocessoView
        mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                .param("codProcesso", codProcesso.toString())
                .param("siglaUnidade", unidadeSECAO221.getSigla()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(codSubprocesso.intValue())));

        System.out.println("✅ Subprocesso encontrado via API após finalização!");
    }
}

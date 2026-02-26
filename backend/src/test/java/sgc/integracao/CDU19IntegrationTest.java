package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-19: Validar Mapa de Competências")
class CDU19IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Use existing units from data.sql to match @WithMockChefe user
        // Unit 6 (COSIS - INTERMEDIARIA) is the parent
        // Unit 9 (SEDIA - OPERACIONAL) is subordinate to 6
        // User '333333333333' has CHEFE profile for unit 9
        unidadeSuperior = unidadeRepo.findById(6L)
                .orElseThrow(() -> new RuntimeException("Unit 6 not found in data.sql"));

        unidade = unidadeRepo.findById(9L)
                .orElseThrow(() -> new RuntimeException("Unit 9 not found in data.sql"));

        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo para CDU-19");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        Mapa mapa = MapaFixture.mapaPadrao(null);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        subprocesso = subprocessoRepo.save(subprocesso);
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Apresentar Sugestões'")
    class ApresentarSugestoesTest {
        @Test
        @DisplayName(
                "Deve apresentar sugestões, alterar status, mas não criar movimentação ou alerta")
        @WithMockChefe("333333333333")
            // CHEFE of unit 9
        void testApresentarSugestoes_Sucesso() throws Exception {
            String sugestoes = "Minha sugestão de teste";

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/apresentar-sugestoes",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType("application/json")
                                    .content("{\"texto\": \"" + sugestoes + "\"}"))
                    .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
            assertThat(subprocessoAtualizado.getMapa().getSugestoes()).isEqualTo(sugestoes);

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao())
                    .isEqualTo("Sugestões apresentadas para o mapa de competências");

            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla())
                    .isEqualTo(unidade.getSigla());

            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());
            List<Alerta> alertas =
                    alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());

            assertThat(alertas).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Validar Mapa'")
    class ValidarMapaTest {
        @Test
        @DisplayName("Deve validar o mapa, alterar status, registrar movimentação e criar alerta")
        @WithMockChefe("333333333333")
            // CHEFE of unit 9
        void testValidarMapa_Sucesso() throws Exception {
            mockMvc.perform(
                            post("/api/subprocessos/{id}/validar-mapa", subprocesso.getCodigo())
                                    .with(csrf()))
                    .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao())
                    .isEqualTo("Validação do mapa de competências");
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla())
                    .isEqualTo(unidade.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());

            List<Alerta> alertas =
                    alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getDescricao())
                    .contains("Validação do mapa de competências da unidade " + unidade.getSigla() + " aguardando análise");
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());
        }
    }
}

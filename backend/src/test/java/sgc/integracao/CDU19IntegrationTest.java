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
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-19: Validar Mapa de Competências")
class CDU19IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private MapaRepo mapaRepo;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Usar dados existentes (SEDIA e COSIS)
        unidadeSuperior = unidadeRepo.findById(6L).orElseThrow(); // COSIS
        unidade = unidadeRepo.findById(9L).orElseThrow(); // SEDIA

        // Criar dados para o teste dinamicamente
        Processo processo = new Processo();
        processo.setDescricao("Processo para CDU-19");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(30));
        processoRepo.save(processo);

        Mapa mapa = new Mapa();
        mapaRepo.save(mapa);

        subprocesso =
                new Subprocesso(
                        processo,
                        unidade,
                        mapa,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO,
                        processo.getDataLimite());
        subprocessoRepo.save(subprocesso);
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Apresentar Sugestões'")
    class ApresentarSugestoesTest {
        @Test
        @DisplayName(
                "Deve apresentar sugestões, alterar status, mas não criar movimentação ou alerta")
        @WithMockChefe
        void testApresentarSugestoes_Sucesso() throws Exception {
            String sugestoes = "Minha sugestão de teste";

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/apresentar-sugestoes",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType("application/json")
                                    .content("{\"sugestoes\": \"" + sugestoes + "\"}"))
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
        @WithMockChefe
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
                    .contains("Validação do mapa de competências da SEDIA aguardando análise");
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());
        }
    }
}

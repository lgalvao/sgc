package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@Disabled("Este teste foi desabilitado porque depende de dados do data.sql, que foi removido.")
@Transactional
@DisplayName("CDU-19: Validar Mapa de Competências")
class CDU19IntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Use dados pré-carregados do data.sql (CDU-19 test data)
        unidadeSuperior = unidadeRepo.findById(6L).orElseThrow(); // COSIS
        unidade = unidadeRepo.findById(9L).orElseThrow(); // SEDIA
        subprocesso = subprocessoRepo.findById(1900L).orElseThrow();

        // Limpa movimentações e alertas relacionados ao subprocesso de teste
        movimentacaoRepo.deleteAll(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1900L));
        alertaRepo.deleteAll(alertaRepo.findByProcessoCodigo(1900L));

        // Garante que o subprocesso está no estado correto
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        subprocesso.getMapa().setSugestoes(null);
        subprocesso.getMapa().setSugestoesApresentadas(false);
        subprocessoRepo.save(subprocesso);
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Apresentar Sugestões'")
    class ApresentarSugestoesTest {

        @Test
        @DisplayName("Deve apresentar sugestões, alterar status, mas não criar movimentação ou alerta")
        @WithMockChefe
        void testApresentarSugestoes_Sucesso() throws Exception {
            String sugestoes = "Minha sugestão de teste";

            mockMvc.perform(post("/api/subprocessos/{id}/apresentar-sugestoes", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"sugestoes\": \"" + sugestoes + "\"}"))
                .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_COM_SUGESTOES);
            assertThat(subprocessoAtualizado.getMapa().getSugestoes()).isEqualTo(sugestoes);

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao()).isEqualTo("Sugestões apresentadas para o mapa de competências");
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeSuperior.getSigla());
            List<Alerta> alertas = alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
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
            mockMvc.perform(post("/api/subprocessos/{id}/validar-mapa", subprocesso.getCodigo())
                    .with(csrf()))
                .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_VALIDADO);

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao()).isEqualTo("Validação do mapa de competências");
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeSuperior.getSigla());

            List<Alerta> alertas = alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getDescricao()).contains("Validação do mapa de competências da SEDIA aguardando análise");
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeSuperior.getSigla());
        }
    }
}

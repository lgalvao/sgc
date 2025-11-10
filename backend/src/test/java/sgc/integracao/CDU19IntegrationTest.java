package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
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
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.UnidadeMapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
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
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-19: Validar Mapa de Competências")
class CDU19IntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        movimentacaoRepo.deleteAll();
        alertaRepo.deleteAll();
        competenciaRepo.deleteAll();
        atividadeRepo.deleteAll();
        conhecimentoRepo.deleteAll();
        unidadeMapaRepo.deleteAll();
        subprocessoRepo.deleteAll();
        mapaRepo.deleteAll();
        processoRepo.deleteAll();

        unidadeSuperior = unidadeRepo.findById(6L).orElseThrow();
        unidade = unidadeRepo.findById(9L).orElseThrow();

        Usuario chefe = usuarioRepo.findById("333333333333").orElseThrow();

        Processo processo = processoRepo.save(new Processo("Processo de Teste", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now()));
        Mapa mapa = mapaRepo.save(new Mapa());
        subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.MAPA_DISPONIBILIZADO, LocalDateTime.now());
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
            List<Alerta> alertas = alertaRepo.findAll();
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

            List<Alerta> alertas = alertaRepo.findAll();
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getDescricao()).contains("Validação do mapa de competências da SEDIA aguardando análise");
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeSuperior.getSigla());
        }
    }
}

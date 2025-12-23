package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.atividade.internal.model.Atividade;
import sgc.atividade.internal.model.AtividadeRepo;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.internal.model.Competencia;
import sgc.mapa.internal.model.CompetenciaRepo;
import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.model.MapaRepo;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.subprocesso.api.AtividadeAjusteDto;
import sgc.subprocesso.api.CompetenciaAjusteDto;
import sgc.subprocesso.api.SalvarAjustesReq;
import sgc.subprocesso.api.SubmeterMapaAjustadoReq;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Sgc.class, TestSecurityConfig.class, TestThymeleafConfig.class})
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-16: Ajustar mapa de competências")
@WithMockAdmin
public class CDU16IntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSO_MAPA_AJUSTE =
            "/api/subprocessos/{codSubprocesso}/mapa-ajuste/atualizar";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    private Subprocesso subprocesso;
    private Atividade atividade1;

    @BeforeEach
    void setUp() {
        // Criar Unidade via Fixture
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Unidade CDU-16");
        unidade.setSigla("U16");
        unidade = unidadeRepo.save(unidade);

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo de Revisão");
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        // Criar Mapa via Fixture
        Mapa mapa = MapaFixture.mapaPadrao(null);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        // Criar Subprocesso via Fixture
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocesso = subprocessoRepo.save(subprocesso);

        var c1 = competenciaRepo.save(new Competencia("Competência 1", mapa));
        atividade1 = new Atividade(mapa, "Atividade 1");
        var atividade2 = new Atividade(mapa, "Atividade 2");
        atividade1.getCompetencias().add(c1);
        atividade2.getCompetencias().add(c1);
        c1.getAtividades().add(atividade1);
        c1.getAtividades().add(atividade2);
        atividadeRepo.saveAll(List.of(atividade1, atividade2));
        competenciaRepo.save(c1);
    }

    @Test
    @DisplayName("Deve submeter o mapa ajustado e alterar a situação do subprocesso")
    void deveSubmeterMapaAjustadoComSucesso() throws Exception {
        var request =
                new SubmeterMapaAjustadoReq(
                        "Ajustes realizados conforme solicitado.",
                        LocalDateTime.now().plusDays(10));

        mockMvc.perform(
                        post(
                                "/api/subprocessos/{id}/submeter-mapa-ajustado",
                                subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Subprocesso subprocessoAtualizado =
                subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao())
                .isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
    }

    @Nested
    @DisplayName("Testes de ajuste do mapa")
    class AjusteDoMapa {

        @Test
        @DisplayName("Deve salvar ajustes no mapa e alterar a situação do subprocesso")
        void deveSalvarAjustesComSucesso() throws Exception {
            Competencia c1 = competenciaRepo.findAll().getFirst();

            var request =
                    new SalvarAjustesReq(
                            List.of(
                                    CompetenciaAjusteDto.builder()
                                            .codCompetencia(c1.getCodigo())
                                            .nome("Competência Ajustada")
                                            .atividades(
                                                    List.of(
                                                            AtividadeAjusteDto.builder()
                                                                    .codAtividade(
                                                                            atividade1.getCodigo())
                                                                    .nome("Atividade 1 Ajustada")
                                                                    .conhecimentos(List.of())
                                                                    .build()))
                                            .build()));

            mockMvc.perform(
                            post(API_SUBPROCESSO_MAPA_AJUSTE, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            Atividade atividadeAtualizada =
                    atividadeRepo.findById(atividade1.getCodigo()).orElseThrow();
            assertThat(atividadeAtualizada.getDescricao()).isEqualTo("Atividade 1 Ajustada");
        }

        @Test
        @DisplayName("Deve retornar 409 se tentar ajustar mapa em situação inválida")
        void deveRetornarErroParaSituacaoInvalida() throws Exception {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            subprocessoRepo.save(subprocesso);

            var request = new SalvarAjustesReq(List.of());

            mockMvc.perform(
                            post(API_SUBPROCESSO_MAPA_AJUSTE, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }
}

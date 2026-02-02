package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = {Sgc.class, TestSecurityConfig.class, TestThymeleafConfig.class})
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-16: Ajustar mapa de competências")
@WithMockAdmin
class CDU16IntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSO_MAPA_AJUSTE =
            "/api/subprocessos/{codSubprocesso}/mapa-ajuste/atualizar";

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

        // Criar Subprocesso via Fixture (Primeiro o subprocesso, pois Mapa depende dele)
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(null); // Importante: limpar mapa da fixture para evitar dependência circular errada
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocesso = subprocessoRepo.save(subprocesso);

        // Criar Mapa via Fixture (Ligado ao Subprocesso)
        Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        // Atualizar referência no subprocesso (para consistência do objeto em memória)
        subprocesso.setMapa(mapa);

        // Criar Competências e Atividades
        var c1 = competenciaRepo.save(Competencia.builder().descricao("Competência 1").mapa(mapa).build());

        // As atividades devem ser salvas antes de serem associadas à competência
        atividade1 = Atividade.builder().mapa(mapa).descricao("Atividade 1").build();
        var atividade2 = Atividade.builder().mapa(mapa).descricao("Atividade 2").build();

        List<Atividade> atividadesSalvas = atividadeRepo.saveAll(List.of(atividade1, atividade2));
        atividade1 = atividadesSalvas.get(0);
        var atividade2Salva = atividadesSalvas.get(1);

        // Associar atividades à competência
        atividade1.getCompetencias().add(c1);
        atividade2Salva.getCompetencias().add(c1);
        c1.getAtividades().add(atividade1);
        c1.getAtividades().add(atividade2Salva);

        // Salvar associações
        atividadeRepo.saveAll(List.of(atividade1, atividade2Salva));
        competenciaRepo.save(c1);
    }

    @Test
    @DisplayName("Deve submeter o mapa ajustado e alterar a situação do subprocesso")
    void deveSubmeterMapaAjustadoComSucesso() throws Exception {
        var request =
                new SubmeterMapaAjustadoRequest(
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
            Atividade a1 = atividadeRepo.findAll().stream().filter(a -> a.getDescricao().equals("Atividade 1")).findFirst().orElseThrow();

            var request =
                    new SalvarAjustesRequest(
                            List.of(
                                    CompetenciaAjusteDto.builder()
                                            .codCompetencia(c1.getCodigo())
                                            .nome("Competência Ajustada")
                                            .atividades(
                                                    List.of(
                                                            AtividadeAjusteDto.builder()
                                                                    .codAtividade(
                                                                            a1.getCodigo())
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

            var request = new SalvarAjustesRequest(
                    List.of(
                            CompetenciaAjusteDto.builder()
                                    .codCompetencia(1L)
                                    .nome("Competência Dummy")
                                    .atividades(List.of())
                                    .build()));

            mockMvc.perform(
                            post(API_SUBPROCESSO_MAPA_AJUSTE, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent());
        }
    }
}

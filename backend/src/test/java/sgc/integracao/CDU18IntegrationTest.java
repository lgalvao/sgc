package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.*;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-18: Visualizar Mapa de Competências")
class CDU18IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        // Criar Unidade via Fixture
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Unidade CDU-18");
        unidade.setSigla("U18");
        unidade = unidadeRepo.save(unidade);

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Processo de Teste");
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
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);

        Atividade atividade1 = atividadeRepo.save(Atividade.builder().mapa(mapa).descricao("Atividade 1").build());
        Atividade atividade2 = atividadeRepo.save(Atividade.builder().mapa(mapa).descricao("Atividade 2").build());

        Conhecimento conhecimento1 =
                conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 1.1").atividade(atividade1).build());
        Conhecimento conhecimento2 =
                conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 1.2").atividade(atividade1).build());
        Conhecimento conhecimento3 =
                conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 2.1").atividade(atividade2).build());

        atividade1.getConhecimentos().addAll(List.of(conhecimento1, conhecimento2));
        atividade2.getConhecimentos().add(conhecimento3);

        Competencia competencia1 = Competencia.builder().descricao("Competência 1").mapa(mapa).build();
        competenciaRepo.save(competencia1);

        Competencia competencia2 = Competencia.builder().descricao("Competência 2").mapa(mapa).build();
        competenciaRepo.save(competencia2);

        // Configurar relacionamento bidirecional no lado owning (Atividade)
        atividade1.getCompetencias().add(competencia1);
        atividade2.getCompetencias().add(competencia2);
        atividadeRepo.saveAll(List.of(atividade1, atividade2));
    }

    @Test
    @DisplayName("Deve retornar o mapa de competências formatado para visualização")
    @WithMockAdmin
    void deveRetornarMapaParaVisualizacao() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{id}/mapa-visualizacao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.sigla").value(unidade.getSigla()))
                .andExpect(jsonPath("$.unidade.nome").value(unidade.getNome()))
                .andExpect(jsonPath("$.competencias").isArray())
                .andExpect(jsonPath("$.competencias.length()").value(2))
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " 1')].atividades.length()")
                                .value(1))
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " 1')].atividades[?(@.descricao == 'Atividade"
                                        + " 1')].conhecimentos.length()")
                                .value(2))
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " 1')].atividades[?(@.descricao == 'Atividade"
                                        + " 1')].conhecimentos[?(@.descricao == 'Conhecimento"
                                        + " 1.1')]")
                                .exists())
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " 1')].atividades[?(@.descricao == 'Atividade"
                                        + " 1')].conhecimentos[?(@.descricao == 'Conhecimento"
                                        + " 1.2')]")
                                .exists())
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " 2')].atividades.length()")
                                .value(1))
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " 2')].atividades[?(@.descricao == 'Atividade"
                                        + " 2')].conhecimentos.length()")
                                .value(1))
                .andExpect(
                        jsonPath(
                                "$.competencias[?(@.descricao == 'Competência"
                                        + " 2')].atividades[?(@.descricao == 'Atividade"
                                        + " 2')].conhecimentos[?(@.descricao == 'Conhecimento"
                                        + " 2.1')]")
                                .exists());
    }

    @Test
    @DisplayName("Deve retornar 404 se o subprocesso não existir")
    @WithMockAdmin
    void deveRetornar404ParaSubprocessoInexistente() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{id}/mapa-visualizacao", 9999L))
                .andExpect(status().isNotFound());
    }
}

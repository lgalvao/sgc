package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
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

        Atividade atividade1 = Atividade.builder().mapa(mapa).descricao("Atividade 1").build();
        atividade1 = atividadeRepo.save(atividade1);
        mapa.getAtividades().add(atividade1);

        Atividade atividade2 = Atividade.builder().mapa(mapa).descricao("Atividade 2").build();
        atividade2 = atividadeRepo.save(atividade2);
        mapa.getAtividades().add(atividade2);

        Conhecimento conhecimento1 =
                conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 1.1").atividade(atividade1).build());
        Conhecimento conhecimento2 =
                conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 1.2").atividade(atividade1).build());
        Conhecimento conhecimento3 =
                conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 2.1").atividade(atividade2).build());

        atividade1.getConhecimentos().addAll(List.of(conhecimento1, conhecimento2));
        atividade2.getConhecimentos().add(conhecimento3);

        Competencia competencia1 = Competencia.builder().descricao("Competência 1").mapa(mapa).build();
        competencia1 = competenciaRepo.save(competencia1);
        mapa.getCompetencias().add(competencia1);

        Competencia competencia2 = Competencia.builder().descricao("Competência 2").mapa(mapa).build();
        competencia2 = competenciaRepo.save(competencia2);
        mapa.getCompetencias().add(competencia2);

        // Configurar relacionamento bidirecional no lado owning (Atividade)
        atividade1.getCompetencias().add(competencia1);
        atividade2.getCompetencias().add(competencia2);
        
        // Configurar o lado inverso para garantir consistência no teste transacional
        competencia1.getAtividades().add(atividade1);
        competencia2.getAtividades().add(atividade2);
        
        atividadeRepo.saveAll(List.of(atividade1, atividade2));
        competenciaRepo.saveAll(List.of(competencia1, competencia2));
        mapaRepo.save(mapa);
    }

    @Test
    @DisplayName("Deve retornar o mapa de competências formatado para visualização")
    @WithMockAdmin
    void deveRetornarMapaParaVisualizacao() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{id}/mapa-visualizacao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.sigla").value(unidade.getSigla()))
                .andExpect(jsonPath("$.unidade.nome").value(unidade.getNome()))
                .andExpect(jsonPath("$.competencias[0].descricao").value("Competência 1"))
                .andExpect(jsonPath("$.competencias[0].atividades.length()").value(1))
                .andExpect(jsonPath("$.competencias[0].atividades[0].descricao").value("Atividade 1"))
                .andExpect(jsonPath("$.competencias[0].atividades[0].conhecimentos.length()").value(2))
                .andExpect(jsonPath("$.competencias[0].atividades[0].conhecimentos[0].descricao").exists())
                .andExpect(jsonPath("$.competencias[0].atividades[0].conhecimentos[1].descricao").exists())
                .andExpect(jsonPath("$.competencias[1].descricao").value("Competência 2"))
                .andExpect(jsonPath("$.competencias[1].atividades.length()").value(1))
                .andExpect(jsonPath("$.competencias[1].atividades[0].descricao").value("Atividade 2"))
                .andExpect(jsonPath("$.competencias[1].atividades[0].conhecimentos.length()").value(1))
                .andExpect(jsonPath("$.competencias[1].atividades[0].conhecimentos[0].descricao").value("Conhecimento 2.1"));
    }

}

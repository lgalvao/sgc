package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-18: Visualizar Mapa de Competências")
class CDU18IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(11L).orElseThrow();
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusMonths(1));
        processo.setDescricao("Processo de Teste");
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        Mapa mapa = mapaRepo.save(new Mapa());
        subprocesso = new Subprocesso(
            processo,
            unidade,
            mapa,
            SituacaoSubprocesso.CADASTRO_HOMOLOGADO,
            LocalDateTime.now().plusMonths(1)
        );
        subprocesso = subprocessoRepo.save(subprocesso);


        Atividade atividade1 = atividadeRepo.save(new Atividade(mapa, "Atividade 1"));
        Atividade atividade2 = atividadeRepo.save(new Atividade(mapa, "Atividade 2"));

        Conhecimento conhecimento1 = conhecimentoRepo.save(new Conhecimento("Conhecimento 1.1", atividade1));
        Conhecimento conhecimento2 = conhecimentoRepo.save(new Conhecimento("Conhecimento 1.2", atividade1));
        Conhecimento conhecimento3 = conhecimentoRepo.save(new Conhecimento("Conhecimento 2.1", atividade2));

        atividade1.getConhecimentos().addAll(List.of(conhecimento1, conhecimento2));
        atividade2.getConhecimentos().add(conhecimento3);
        atividadeRepo.saveAll(List.of(atividade1, atividade2));

        Competencia competencia1 = new Competencia("Competência 1", mapa);
        competencia1.setAtividades(Set.of(atividade1));
        competenciaRepo.save(competencia1);

        Competencia competencia2 = new Competencia("Competência 2", mapa);
        competencia2.setAtividades(Set.of(atividade2));
        competenciaRepo.save(competencia2);
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
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência 1')].atividades.length()").value(1))
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência 1')].atividades[?(@.descricao == 'Atividade 1')].conhecimentos.length()").value(2))
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência 1')].atividades[?(@.descricao == 'Atividade 1')].conhecimentos[?(@.descricao == 'Conhecimento 1.1')]").exists())
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência 1')].atividades[?(@.descricao == 'Atividade 1')].conhecimentos[?(@.descricao == 'Conhecimento 1.2')]").exists())
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência 2')].atividades.length()").value(1))
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência 2')].atividades[?(@.descricao == 'Atividade 2')].conhecimentos.length()").value(1))
            .andExpect(jsonPath("$.competencias[?(@.descricao == 'Competência 2')].atividades[?(@.descricao == 'Atividade 2')].conhecimentos[?(@.descricao == 'Conhecimento 2.1')]").exists());
    }

    @Test
    @DisplayName("Deve retornar 404 se o subprocesso não existir")
    @WithMockAdmin
    void deveRetornar404ParaSubprocessoInexistente() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{id}/mapa-visualizacao", 9999L))
            .andExpect(status().isNotFound());
    }
}

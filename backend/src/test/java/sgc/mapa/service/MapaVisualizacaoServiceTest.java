package sgc.mapa.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do Serviço de Visualização de Mapa")
class MapaVisualizacaoServiceTest {
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @InjectMocks
    private MapaVisualizacaoService service;

    @Test
    @DisplayName("Deve obter mapa para visualização")
    void deveObterMapaParaVisualizacao() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sub.setMapa(mapa);

        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        sub.setUnidade(unidade);

        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L);
        ativ1.setDescricao("A1");

        Atividade ativ2 = new Atividade(); // Sem competencia
        ativ2.setCodigo(2L);
        ativ2.setDescricao("A2");

        List<Object[]> projectionResult = new ArrayList<>();
        projectionResult.add(new Object[]{50L, "C1", 1L});

        when(atividadeRepo.findWithConhecimentosByMapaCodigo(10L)).thenReturn(List.of(ativ1, ativ2));
        when(competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(10L)).thenReturn(projectionResult);

        MapaVisualizacaoDto dto = service.obterMapaParaVisualizacao(sub);

        assertThat(dto).isNotNull();
        assertThat(dto.competencias()).hasSize(1);
        assertThat(dto.atividadesSemCompetencia()).hasSize(1);
        assertThat(dto.atividadesSemCompetencia().getFirst().codigo()).isEqualTo(2L);
    }
    @Test
    @DisplayName("Deve ignorar atividade não encontrada nos tuples de competência")
    void deveIgnorarAtividadeNaoEncontrada() {
        Subprocesso sub = new Subprocesso();
        sub.setMapa(new Mapa());
        sub.getMapa().setCodigo(10L);
        sub.setUnidade(new Unidade());

        List<Object[]> projectionResult = new ArrayList<>();
        projectionResult.add(new Object[]{50L, "C1", 999L}); // 999L não existe nas atividades

        when(atividadeRepo.findWithConhecimentosByMapaCodigo(10L)).thenReturn(List.of());
        when(competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(10L)).thenReturn(projectionResult);

        MapaVisualizacaoDto dto = service.obterMapaParaVisualizacao(sub);

        assertThat(dto.competencias().getFirst().getAtividades()).isEmpty();
    }
}

package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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

        // Mocking the new projection method
        List<Object[]> projectionResult = new java.util.ArrayList<>();
        projectionResult.add(new Object[]{50L, "C1", 1L});

        when(atividadeRepo.findByMapaCodigoWithConhecimentos(10L)).thenReturn(List.of(ativ1, ativ2));
        when(competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(10L)).thenReturn(projectionResult);

        MapaVisualizacaoDto dto = service.obterMapaParaVisualizacao(sub);

        assertThat(dto).isNotNull();
        assertThat(dto.getCompetencias()).hasSize(1);
        assertThat(dto.getAtividadesSemCompetencia()).hasSize(1);
        assertThat(dto.getAtividadesSemCompetencia().getFirst().getCodigo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve lançar erro se subprocesso não tem mapa")
    void deveLancarErroSeSemMapa() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        // Mapa null

        assertThatThrownBy(() -> service.obterMapaParaVisualizacao(sub))
                .isInstanceOf(sgc.comum.erros.ErroEstadoImpossivel.class);
    }
}

package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class MapaVisualizacaoServiceCoverageTest {

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @InjectMocks
    private MapaVisualizacaoService service;

    @Test
    @DisplayName("Deve retornar resposta vazia se mapa não encontrado")
    void deveRetornarVazioSeMapaNaoEncontrado() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        Unidade u = new Unidade();
        sub.setUnidade(u);
        // sub.getMapa() is null by default

        when(mapaRepo.findFullBySubprocessoCodigo(1L)).thenReturn(Optional.empty());

        MapaVisualizacaoResponse res = service.obterMapaParaVisualizacao(sub);

        assertThat(res).isNotNull();
        assertThat(res.unidade()).isEqualTo(u);
        assertThat(res.competencias()).isEmpty();
        assertThat(res.atividadesSemCompetencia()).isEmpty();
    }
}

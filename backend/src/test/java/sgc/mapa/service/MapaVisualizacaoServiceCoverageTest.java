package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapaVisualizacaoServiceCoverageTest {
    @Mock
    private MapaRepo mapaRepo;

    @InjectMocks
    private MapaVisualizacaoService service;

    @Test
    @DisplayName("Deve retornar resposta vazia se mapa não encontrado")
    void deveRetornarVazioSeMapaNaoEncontrado() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);

        Unidade u = new Unidade();
        sub.setUnidade(u);
        when(mapaRepo.findFullBySubprocessoCodigo(1L)).thenReturn(Optional.empty());

        MapaVisualizacaoResponse res = service.obterMapaParaVisualizacao(sub);
        assertThat(res).isNotNull();
        assertThat(res.unidade()).isEqualTo(u);
        assertThat(res.competencias()).isEmpty();
        assertThat(res.atividadesSemCompetencia()).isEmpty();
    }
}

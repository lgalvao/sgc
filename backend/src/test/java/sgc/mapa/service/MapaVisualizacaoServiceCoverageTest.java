package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

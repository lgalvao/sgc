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
@DisplayName("Testes do Serviço de Visualização de Mapa")
class MapaVisualizacaoServiceTest {
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private MapaRepo mapaRepo;
    @InjectMocks
    private MapaVisualizacaoService service;

    @Test
    @DisplayName("Deve obter mapa para visualização")
    void deveObterMapaParaVisualizacao() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        sub.setUnidade(unidade);

        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L);
        ativ1.setDescricao("A1");

        Atividade ativ2 = new Atividade(); // Sem competencia
        ativ2.setCodigo(2L);
        ativ2.setDescricao("A2");

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        mapa.setAtividades(Set.of(ativ1, ativ2));
        sub.setMapa(mapa);

        Competencia comp1 = new Competencia();
        comp1.setCodigo(50L);
        comp1.setDescricao("C1");
        comp1.setAtividades(Set.of(ativ1));

        when(mapaRepo.findFullBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapa_Codigo(10L)).thenReturn(List.of(comp1));

        MapaVisualizacaoResponse response = service.obterMapaParaVisualizacao(sub);

        assertThat(response).isNotNull();
        assertThat(response.competencias()).hasSize(1);
        assertThat(response.atividadesSemCompetencia()).hasSize(1);
        assertThat(response.atividadesSemCompetencia().getFirst().getCodigo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve mapear conhecimentos das atividades")
    void deveMapearConhecimentos() {
        Subprocesso sub = new Subprocesso();
        sub.setCodigo(1L);
        sub.setUnidade(new Unidade());

        Atividade ativ = new Atividade();
        ativ.setCodigo(1L);
        ativ.setDescricao("A1");
        
        Conhecimento k = Conhecimento.builder()
                .codigo(100L)
                .descricao("K1")
                .build();
        ativ.setConhecimentos(Set.of(k));

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        mapa.setAtividades(Set.of(ativ));
        sub.setMapa(mapa);

        when(mapaRepo.findFullBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapa_Codigo(10L)).thenReturn(List.of());

        MapaVisualizacaoResponse response = service.obterMapaParaVisualizacao(sub);

        assertThat(response.atividadesSemCompetencia().getFirst().getConhecimentos()).hasSize(1);
        assertThat(response.atividadesSemCompetencia().getFirst().getConhecimentos().stream().findFirst().get().getDescricao()).isEqualTo("K1");
    }
}

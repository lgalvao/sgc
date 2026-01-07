package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.*;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Visualização de Mapa")
class MapaVisualizacaoServiceTest {

    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private AtividadeRepo atividadeRepo;

    @InjectMocks
    private MapaVisualizacaoService service;

    @Test
    @DisplayName("Deve obter mapa para visualização")
    void deveObterMapaParaVisualizacao() {
        Long subId = 1L;
        Subprocesso sub = new Subprocesso();
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

        Competencia comp = new Competencia();
        comp.setCodigo(50L);
        comp.setDescricao("C1");
        comp.setAtividades(Set.of(ativ1));

        when(subprocessoService.buscarSubprocesso(subId)).thenReturn(sub);
        when(atividadeRepo.findByMapaCodigoWithConhecimentos(10L)).thenReturn(List.of(ativ1, ativ2));
        when(competenciaRepo.findByMapaCodigo(10L)).thenReturn(List.of(comp));

        MapaVisualizacaoDto dto = service.obterMapaParaVisualizacao(subId);

        assertThat(dto).isNotNull();
        assertThat(dto.getCompetencias()).hasSize(1);
        assertThat(dto.getAtividadesSemCompetencia()).hasSize(1);
        assertThat(dto.getAtividadesSemCompetencia().getFirst().getCodigo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve lançar erro se subprocesso não tem mapa")
    void deveLancarErroSeSemMapa() {
        Long subId = 1L;
        Subprocesso sub = new Subprocesso();
        // Mapa null

        when(subprocessoService.buscarSubprocesso(subId)).thenReturn(sub);

        assertThatThrownBy(() -> service.obterMapaParaVisualizacao(subId))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}

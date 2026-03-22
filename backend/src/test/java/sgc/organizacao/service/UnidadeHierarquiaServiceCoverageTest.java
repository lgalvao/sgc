package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.model.UnidadeRepo;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeHierarquiaService - Cobertura adicional")
class UnidadeHierarquiaServiceCoverageTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private UnidadeHierarquiaService unidadeHierarquiaService;

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve usar mapas vigentes se requerido e testar bloqueadas")
    void deveBuscarArvoreComElegibilidadeRequerMapa() {
        Unidade oper = new Unidade(); oper.setCodigo(1L); oper.setTipo(TipoUnidade.OPERACIONAL);
        Unidade inter = new Unidade(); inter.setCodigo(2L); inter.setTipo(TipoUnidade.INTERMEDIARIA);
        Unidade semMapa = new Unidade(); semMapa.setCodigo(3L); semMapa.setTipo(TipoUnidade.OPERACIONAL);
        Unidade bloqueada = new Unidade(); bloqueada.setCodigo(4L); bloqueada.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(oper, inter, semMapa, bloqueada));
        when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L, 2L, 4L));

        Set<Long> bloqueadas = new HashSet<>();
        bloqueadas.add(4L);

        List<UnidadeDto> res = unidadeHierarquiaService.buscarArvoreComElegibilidade(true, bloqueadas);

        assertThat(res).hasSize(4); // todas as raizes (todas sem superior no mock)
        UnidadeDto operDto = res.stream().filter(u -> u.getCodigo().equals(1L)).findFirst().get();
        assertThat(operDto.isElegivel()).isTrue();

        UnidadeDto interDto = res.stream().filter(u -> u.getCodigo().equals(2L)).findFirst().get();
        assertThat(interDto.isElegivel()).isFalse(); // INTERMEDIARIA nunca é elegível

        UnidadeDto semMapaDto = res.stream().filter(u -> u.getCodigo().equals(3L)).findFirst().get();
        assertThat(semMapaDto.isElegivel()).isFalse(); // Sem mapa vigente

        UnidadeDto bloqueadaDto = res.stream().filter(u -> u.getCodigo().equals(4L)).findFirst().get();
        assertThat(bloqueadaDto.isElegivel()).isFalse(); // Esta na lista de bloqueadas
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve aceitar sem mapa se nao requerido")
    void deveBuscarArvoreComElegibilidadeNaoRequerMapa() {
        Unidade semMapa = new Unidade(); semMapa.setCodigo(3L); semMapa.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(semMapa));

        Set<Long> bloqueadas = new HashSet<>();

        List<UnidadeDto> res = unidadeHierarquiaService.buscarArvoreComElegibilidade(false, bloqueadas);

        UnidadeDto semMapaDto = res.stream().filter(u -> u.getCodigo().equals(3L)).findFirst().get();
        assertThat(semMapaDto.isElegivel()).isTrue(); // Como nao requer mapa, deve ser elegivel
    }
}

package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeService")
class UnidadeServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private UnidadeService service;

    @Test
    @DisplayName("buscarPorId - Sucesso")
    void buscarPorId() {
        Unidade u = new Unidade();
        when(repo.buscar(eq(Unidade.class), any())).thenReturn(u);

        Unidade result = service.buscarPorId(1L);

        assertThat(result).isSameAs(u);
    }

    @Test
    @DisplayName("buscarPorSigla - Sucesso")
    void buscarPorSigla() {
        Unidade u = new Unidade();
        when(repo.buscarPorSigla(Unidade.class, "U1")).thenReturn(u);

        Unidade result = service.buscarPorSigla("U1");

        assertThat(result).isSameAs(u);
    }

    @Test
    @DisplayName("buscarEntidadesPorIds - Sucesso")
    void buscarEntidadesPorIds() {
        List<Unidade> lista = List.of(new Unidade());
        when(unidadeRepo.findAllById(any())).thenReturn(lista);

        List<Unidade> result = service.buscarEntidadesPorIds(List.of(1L));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("buscarTodasEntidadesComHierarquia - Sucesso")
    void buscarTodasEntidadesComHierarquia() {
        List<Unidade> lista = List.of(new Unidade());
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(lista);

        List<Unidade> result = service.buscarTodasEntidadesComHierarquia();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("buscarSiglasPorIds - Sucesso")
    void buscarSiglasPorIds() {
        List<String> lista = List.of("U1");
        when(unidadeRepo.findSiglasByCodigos(any())).thenReturn(lista);

        List<String> result = service.buscarSiglasPorIds(List.of(1L));

        assertThat(result).containsExactly("U1");
    }

    @Test
    @DisplayName("verificarMapaVigente - Sucesso")
    void verificarMapaVigente() {
        when(unidadeMapaRepo.existsById(1L)).thenReturn(true);
        assertThat(service.verificarMapaVigente(1L)).isTrue();
    }

    @Test
    @DisplayName("buscarTodosCodigosUnidadesComMapa - Sucesso")
    void buscarTodosCodigosUnidadesComMapa() {
        when(unidadeMapaRepo.findAllUnidadeCodigos()).thenReturn(List.of(1L, 2L));
        assertThat(service.buscarTodosCodigosUnidadesComMapa()).hasSize(2);
    }

    @Test
    @DisplayName("definirMapaVigente - Criar novo")
    void definirMapaVigente_Novo() {
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty());
        Mapa mapa = new Mapa();

        service.definirMapaVigente(1L, mapa);

        verify(unidadeMapaRepo).save(argThat(um -> um.getUnidadeCodigo().equals(1L) && um.getMapaVigente().equals(mapa)));
    }

    @Test
    @DisplayName("definirMapaVigente - Atualizar existente")
    void definirMapaVigente_Existente() {
        UnidadeMapa existente = new UnidadeMapa();
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(existente));
        Mapa mapa = new Mapa();

        service.definirMapaVigente(1L, mapa);

        assertThat(existente.getMapaVigente()).isSameAs(mapa);
        verify(unidadeMapaRepo).save(existente);
    }
}

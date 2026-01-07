package sgc.subprocesso.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoMapper")
class SubprocessoMapperTest {

    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private MapaRepo mapaRepo;

    @InjectMocks
    private SubprocessoMapperImpl mapper;

    @Test
    @DisplayName("Deve mapear Processo por ID com sucesso")
    void deveMapearProcesso() {
        Processo p = new Processo();
        p.setCodigo(1L);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        Processo res = mapper.mapProcesso(1L);
        assertThat(res).isEqualTo(p);
    }

    @Test
    @DisplayName("Deve lançar erro se Processo não encontrado")
    void deveLancarErroProcessoNaoEncontrado() {
        when(processoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapper.mapProcesso(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Processo não encontrado");
    }

    @Test
    @DisplayName("Deve retornar null se ID Processo for null")
    void deveRetornarNullProcessoIdNull() {
        assertThat(mapper.mapProcesso(null)).isNull();
    }

    @Test
    @DisplayName("Deve mapear Unidade por ID com sucesso")
    void deveMapearUnidade() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(u));

        Unidade res = mapper.mapUnidade(1L);
        assertThat(res).isEqualTo(u);
    }

    @Test
    @DisplayName("Deve lançar erro se Unidade não encontrada")
    void deveLancarErroUnidadeNaoEncontrada() {
        when(unidadeRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapper.mapUnidade(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unidade não encontrada");
    }

    @Test
    @DisplayName("Deve retornar null se ID Unidade for null")
    void deveRetornarNullUnidadeIdNull() {
        assertThat(mapper.mapUnidade(null)).isNull();
    }

    @Test
    @DisplayName("Deve mapear Mapa por ID com sucesso")
    void deveMapearMapa() {
        Mapa m = new Mapa();
        m.setCodigo(1L);
        when(mapaRepo.findById(1L)).thenReturn(Optional.of(m));

        Mapa res = mapper.mapMapa(1L);
        assertThat(res).isEqualTo(m);
    }

    @Test
    @DisplayName("Deve lançar erro se Mapa não encontrado")
    void deveLancarErroMapaNaoEncontrado() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapper.mapMapa(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mapa não encontrado");
    }

    @Test
    @DisplayName("Deve retornar null se ID Mapa for null")
    void deveRetornarNullMapaIdNull() {
        assertThat(mapper.mapMapa(null)).isNull();
    }
}

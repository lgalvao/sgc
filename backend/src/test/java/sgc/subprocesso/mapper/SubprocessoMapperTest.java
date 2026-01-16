package sgc.subprocesso.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.comum.repo.RepositorioComum;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoMapper")
class SubprocessoMapperTest {

    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private MapaRepo mapaRepo;
    @Mock private RepositorioComum repo;

    private SubprocessoMapper mapper;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        mapper = org.mapstruct.factory.Mappers.getMapper(SubprocessoMapper.class);
        mapper.processoRepo = processoRepo;
        mapper.unidadeRepo = unidadeRepo;
        mapper.mapaRepo = mapaRepo;
        mapper.repo = repo;
    }

    @Test
    @DisplayName("Deve mapear Processo por ID com sucesso")
    void deveMapearProcesso() {
        Processo p = new Processo();
        p.setCodigo(1L);
        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        Processo res = mapper.mapProcesso(1L);
        assertThat(res).isEqualTo(p);
    }

    @Test
    @DisplayName("Deve lançar erro se Processo não encontrado")
    void deveLancarErroProcessoNaoEncontrado() {
        when(repo.buscar(Processo.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 1L));
        assertThatThrownBy(() -> mapper.mapProcesso(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
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
        when(repo.buscar(Unidade.class, 1L)).thenReturn(u);

        Unidade res = mapper.mapUnidade(1L);
        assertThat(res).isEqualTo(u);
    }

    @Test
    @DisplayName("Deve lançar erro se Unidade não encontrada")
    void deveLancarErroUnidadeNaoEncontrada() {
        when(repo.buscar(Unidade.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 1L));
        assertThatThrownBy(() -> mapper.mapUnidade(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
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
        when(repo.buscar(Mapa.class, 1L)).thenReturn(m);

        Mapa res = mapper.mapMapa(1L);
        assertThat(res).isEqualTo(m);
    }

    @Test
    @DisplayName("Deve lançar erro se Mapa não encontrado")
    void deveLancarErroMapaNaoEncontrado() {
        when(repo.buscar(Mapa.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 1L));
        assertThatThrownBy(() -> mapper.mapMapa(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve retornar null se ID Mapa for null")
    void deveRetornarNullMapaIdNull() {
        assertThat(mapper.mapMapa(null)).isNull();
    }
}

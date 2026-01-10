package sgc.subprocesso.service.decomposed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SubprocessoCrudService")
class SubprocessoCrudServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private MapaFacade mapaFacade;

    @InjectMocks
    private SubprocessoCrudService service;

    @Test
    @DisplayName("Deve criar subprocesso com sucesso")
    void deveCriar() {
        SubprocessoDto dto = SubprocessoDto.builder().build();
        Subprocesso entity = new Subprocesso();
        when(subprocessoMapper.toEntity(dto)).thenReturn(entity);
        when(repositorioSubprocesso.save(any())).thenReturn(entity);
        when(mapaFacade.salvar(any())).thenReturn(new Mapa());
        when(subprocessoMapper.toDTO(any())).thenReturn(dto);

        assertThat(service.criar(dto)).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar subprocesso por código")
    void deveBuscarPorCodigo() {
        Subprocesso sp = new Subprocesso();
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
        assertThat(service.buscarSubprocesso(1L)).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção se não encontrar")
    void deveLancarExcecaoSeNaoEncontrar() {
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarSubprocesso(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve buscar subprocesso com mapa com sucesso")
    void deveBuscarSubprocessoComMapa() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        Subprocesso resultado = service.buscarSubprocessoComMapa(1L);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getMapa()).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar subprocesso com mapa se mapa for nulo")
    void deveLancarExcecaoSeMapaNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(null);
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.buscarSubprocessoComMapa(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Subprocesso não possui mapa associado");
    }

    @Test
    @DisplayName("Deve listar entidades por processo")
    void deveListarEntidadesPorProcesso() {
        when(repositorioSubprocesso.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(new Subprocesso()));

        List<Subprocesso> lista = service.listarEntidadesPorProcesso(1L);
        assertThat(lista).isNotEmpty();
    }

    @Test
    @DisplayName("Deve obter status do subprocesso")
    void deveObterStatus() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        SubprocessoSituacaoDto status = service.obterStatus(1L);
        assertThat(status.getCodigo()).isEqualTo(1L);
        assertThat(status.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        assertThat(status.getSituacaoLabel()).isEqualTo("NAO_INICIADO");
    }

    @Test
    @DisplayName("Deve obter status com label nulo se situação for nula")
    void deveObterStatusComLabelNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(null);
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        SubprocessoSituacaoDto status = service.obterStatus(1L);
        assertThat(status.getSituacaoLabel()).isNull();
    }

    @Test
    @DisplayName("Deve obter entidade por código do mapa")
    void deveObterEntidadePorCodigoMapa() {
        Subprocesso sp = new Subprocesso();
        when(repositorioSubprocesso.findByMapaCodigo(10L)).thenReturn(Optional.of(sp));

        assertThat(service.obterEntidadePorCodigoMapa(10L)).isEqualTo(sp);
    }

    @Test
    @DisplayName("Deve lançar exceção ao não encontrar subprocesso por código do mapa")
    void deveLancarExcecaoAoNaoEncontrarPorCodigoMapa() {
        when(repositorioSubprocesso.findByMapaCodigo(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterEntidadePorCodigoMapa(10L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve atualizar subprocesso removendo mapa")
    void deveAtualizarRemovendoMapa() {
        Subprocesso sp = new Subprocesso();
        SubprocessoDto dto = SubprocessoDto.builder().build(); // codMapa null

        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
        when(repositorioSubprocesso.save(sp)).thenReturn(sp);
        when(subprocessoMapper.toDTO(sp)).thenReturn(dto);

        SubprocessoDto resultado = service.atualizar(1L, dto);
        assertThat(resultado).isNotNull();
        assertThat(sp.getMapa()).isNull();
    }

    @Test
    @DisplayName("Deve atualizar subprocesso definindo mapa")
    void deveAtualizarDefinindoMapa() {
        Subprocesso sp = new Subprocesso();
        SubprocessoDto dto = SubprocessoDto.builder().codMapa(5L).build();

        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
        when(repositorioSubprocesso.save(sp)).thenReturn(sp);
        when(subprocessoMapper.toDTO(sp)).thenReturn(dto);

        SubprocessoDto resultado = service.atualizar(1L, dto);
        assertThat(resultado).isNotNull();
        assertThat(sp.getMapa().getCodigo()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar subprocesso inexistente")
    void deveLancarExcecaoAoAtualizarInexistente() {
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());
        SubprocessoDto dto = SubprocessoDto.builder().build();

        assertThatThrownBy(() -> service.atualizar(1L, dto))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve excluir subprocesso existente")
    void deveExcluirSubprocesso() {
        when(repositorioSubprocesso.existsById(1L)).thenReturn(true);
        service.excluir(1L);
        verify(repositorioSubprocesso).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir subprocesso inexistente")
    void deveLancarExcecaoAoExcluirInexistente() {
        when(repositorioSubprocesso.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve listar todos os subprocessos")
    void deveListarTodos() {
        when(repositorioSubprocesso.findAllComFetch()).thenReturn(List.of(new Subprocesso()));
        when(subprocessoMapper.toDTO(any())).thenReturn(SubprocessoDto.builder().build());

        assertThat(service.listar()).hasSize(1);
    }

    @Test
    @DisplayName("Deve obter subprocesso por processo e unidade")
    void deveObterPorProcessoEUnidade() {
        Subprocesso sp = new Subprocesso();
        when(repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(1L, 2L)).thenReturn(Optional.of(sp));
        when(subprocessoMapper.toDTO(sp)).thenReturn(SubprocessoDto.builder().build());

        assertThat(service.obterPorProcessoEUnidade(1L, 2L)).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção ao não encontrar subprocesso por processo e unidade")
    void deveLancarExcecaoAoNaoEncontrarPorProcessoEUnidade() {
        when(repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterPorProcessoEUnidade(1L, 2L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve verificar acesso da unidade ao processo")
    void deveVerificarAcesso() {
        when(repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(1L, List.of(2L)))
                .thenReturn(true);

        assertThat(service.verificarAcessoUnidadeAoProcesso(1L, List.of(2L))).isTrue();
    }
}

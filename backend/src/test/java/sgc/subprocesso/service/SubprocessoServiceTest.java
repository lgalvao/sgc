package sgc.subprocesso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.MapaFixture;

@ExtendWith(MockitoExtension.class)
class SubprocessoServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoRepo repositorioConhecimento;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private MapaRepo mapaRepo;

    @InjectMocks
    private SubprocessoService service;

    @Test
    @DisplayName("obterStatus sucesso")
    void obterSituacao() {
        Subprocesso sp = SubprocessoFixture.subprocessoPadrao(null, null);
        sp.setCodigo(1L);
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        assertThat(service.obterSituacao(1L)).isNotNull();
    }

    @Test
    @DisplayName("obterStatus falha")
    void obterSituacaoFalha() {
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obterSituacao(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("obterEntidadePorCodigoMapa sucesso")
    void obterEntidadePorCodigoMapa() {
        when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(SubprocessoFixture.subprocessoPadrao(null, null)));
        assertThat(service.obterEntidadePorCodigoMapa(100L)).isNotNull();
    }

    @Test
    @DisplayName("criar sucesso")
    void criar() {
        SubprocessoDto dto = SubprocessoDto.builder().build();
        Subprocesso entity = SubprocessoFixture.subprocessoPadrao(null, null);
        
        when(subprocessoMapper.toEntity(dto)).thenReturn(entity);
        when(repositorioSubprocesso.save(any())).thenReturn(entity);
        when(mapaRepo.save(any())).thenReturn(MapaFixture.mapaPadrao(null));
        when(subprocessoMapper.toDTO(any())).thenReturn(dto);

        assertThat(service.criar(dto)).isNotNull();
        verify(repositorioSubprocesso, times(2)).save(any());
    }

    @Test
    @DisplayName("atualizar sucesso")
    void atualizar() {
        SubprocessoDto dto = SubprocessoDto.builder().codMapa(100L).build();
        Subprocesso entity = SubprocessoFixture.subprocessoPadrao(null, null);

        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(entity));
        when(repositorioSubprocesso.save(any())).thenReturn(entity);
        when(subprocessoMapper.toDTO(any())).thenReturn(dto);

        assertThat(service.atualizar(1L, dto)).isNotNull();
    }

    @Test
    @DisplayName("excluir sucesso")
    void excluir() {
        when(repositorioSubprocesso.existsById(1L)).thenReturn(true);
        service.excluir(1L);
        verify(repositorioSubprocesso).deleteById(1L);
    }

    @Test
    @DisplayName("excluir falha")
    void excluirFalha() {
        when(repositorioSubprocesso.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}

package sgc.subprocesso.service.decomposed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.service.MapaService;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SubprocessoCrudService")
class SubprocessoCrudServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private MapaService mapaService;

    @InjectMocks
    private SubprocessoCrudService service;

    @Test
    @DisplayName("Deve criar subprocesso com sucesso")
    void deveCriar() {
        SubprocessoDto dto = SubprocessoDto.builder().build();
        Subprocesso entity = new Subprocesso();
        when(subprocessoMapper.toEntity(dto)).thenReturn(entity);
        when(repositorioSubprocesso.save(any())).thenReturn(entity);
        when(mapaService.salvar(any())).thenReturn(new sgc.mapa.model.Mapa());
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
}

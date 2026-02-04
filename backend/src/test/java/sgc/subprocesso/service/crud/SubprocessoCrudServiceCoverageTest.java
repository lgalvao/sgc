package sgc.subprocesso.service.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.comum.repo.ComumRepo;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.factory.SubprocessoFactory;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoCrudServiceCoverageTest {

    @InjectMocks
    private SubprocessoCrudService crudService;

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private ComumRepo repositorioComum;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private SubprocessoFactory subprocessoFactory;

    @Test
    @DisplayName("criar deve lançar ErroEstadoImpossivel se DTO for nulo")
    void criar_DeveLancarErro() {
        CriarSubprocessoRequest req = CriarSubprocessoRequest.builder()
                .codProcesso(1L)
                .codUnidade(10L)
                .dataLimiteEtapa1(LocalDateTime.now())
                .build();
        
        when(subprocessoFactory.criar(req)).thenReturn(new Subprocesso());
        when(subprocessoMapper.toDto(any())).thenReturn(null);

        assertThrows(ErroEstadoImpossivel.class, () -> crudService.criar(req));
    }

    @Test
    @DisplayName("atualizar deve lançar ErroEstadoImpossivel se DTO for nulo")
    void atualizar_DeveLancarErro() {
        Long codigo = 1L;
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder()
                .codUnidade(10L)
                .build();

        Subprocesso subprocesso = new Subprocesso();
        when(repositorioComum.buscar(Subprocesso.class, codigo)).thenReturn(subprocesso);
        when(subprocessoRepo.save(subprocesso)).thenReturn(subprocesso);
        when(subprocessoMapper.toDto(any())).thenReturn(null);

        assertThrows(ErroEstadoImpossivel.class, () -> crudService.atualizar(codigo, req));
    }

    @Test
    @DisplayName("obterPorProcessoEUnidade deve lançar ErroEstadoImpossivel se DTO for nulo")
    void obterPorProcessoEUnidade_DeveLancarErro() {
        Long codProcesso = 1L;
        Long codUnidade = 2L;

        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade))
                .thenReturn(Optional.of(new Subprocesso()));
        when(subprocessoMapper.toDto(any())).thenReturn(null);

        assertThrows(ErroEstadoImpossivel.class, () -> crudService.obterPorProcessoEUnidade(codProcesso, codUnidade));
    }
}

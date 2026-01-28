package sgc.processo.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import sgc.mapa.model.Mapa;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para ProcessoFinalizador")
class ProcessoFinalizadorCoverageTest {

    @InjectMocks
    private ProcessoFinalizador finalizador;

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ApplicationEventPublisher publicadorEventos;

    @Test
    @DisplayName("Deve lançar erro se subprocesso sem unidade")
    void deveLancarErroSubprocessoSemUnidade() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        when(processoRepo.findById(codigo)).thenReturn(Optional.of(p));

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        s.setUnidade(null); // Explicitamente null
        s.setMapa(new Mapa());

        when(subprocessoFacade.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        assertThatThrownBy(() -> finalizador.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Subprocesso 10 sem unidade associada");
    }

    @Test
    @DisplayName("Deve lançar erro se subprocesso sem mapa")
    void deveLancarErroSubprocessoSemMapa() {
        Long codigo = 1L;
        Processo p = new Processo();
        p.setCodigo(codigo);
        when(processoRepo.findById(codigo)).thenReturn(Optional.of(p));

        Subprocesso s = new Subprocesso();
        s.setCodigo(10L);
        s.setUnidade(new Unidade());
        s.setMapa(null); // Explicitamente null

        when(subprocessoFacade.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(s));

        assertThatThrownBy(() -> finalizador.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Subprocesso 10 sem mapa associado");
    }
}

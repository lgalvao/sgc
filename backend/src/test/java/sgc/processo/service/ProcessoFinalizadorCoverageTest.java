package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.UnidadeFacade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFinalizador - Cobertura Adicional")
class ProcessoFinalizadorCoverageTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private ConsultasSubprocessoService queryService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ApplicationEventPublisher publicadorEventos;

    @InjectMocks
    private ProcessoFinalizador finalizador;

    @Test
    @DisplayName("finalizar deve ignorar mapas se tipo for DIAGNOSTICO")
    void deveIgnorarMapasSeDiagnostico() {
        // Arrange
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setTipo(TipoProcesso.DIAGNOSTICO);

        when(repo.buscar(Processo.class, codigo)).thenReturn(processo);

        // Act
        finalizador.finalizar(codigo);

        // Assert
        verify(queryService, never()).listarEntidadesPorProcesso(any());
        verify(processoRepo).save(processo);
    }

    @Test
    @DisplayName("finalizar deve falhar se subprocesso não tiver unidade")
    void deveFalharSeSubprocessoSemUnidade() {
        // Arrange
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(null); // Sem unidade

        when(repo.buscar(Processo.class, codigo)).thenReturn(processo);
        when(queryService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(subprocesso));

        // Act & Assert (cobertura da linha 92)
        assertThatThrownBy(() -> finalizador.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem unidade associada");
    }
}

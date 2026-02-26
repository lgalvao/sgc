package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.organizacao.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFinalizador - Cobertura Adicional")
class ProcessoFinalizadorCoverageTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private ConsultasSubprocessoService queryService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ProcessoNotificacaoService notificacaoService;

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
        verify(notificacaoService).emailFinalizacaoProcesso(codigo);
    }
}

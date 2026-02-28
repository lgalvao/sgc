package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
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
    private ConsultasSubprocessoService queryService;
    @Mock
    private ProcessoNotificacaoService notificacaoService;
    @Mock
    private ProcessoValidador processoValidador;

    @InjectMocks
    private ProcessoFinalizador finalizador;

    @Test
    @DisplayName("finalizar deve ignorar mapas se tipo for DIAGNOSTICO")
    void deveIgnorarMapasSeDiagnostico() {

        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setTipo(TipoProcesso.DIAGNOSTICO);

        when(repo.buscar(Processo.class, codigo)).thenReturn(processo);


        finalizador.finalizar(codigo);


        verify(queryService, never()).listarEntidadesPorProcesso(any());
        verify(processoRepo).save(processo);
        verify(notificacaoService).emailFinalizacaoProcesso(codigo);
    }
}

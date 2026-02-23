package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.ComumRepo;
import sgc.organizacao.OrganizacaoFacade;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

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

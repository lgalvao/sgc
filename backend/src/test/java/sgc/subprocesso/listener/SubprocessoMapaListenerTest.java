package sgc.subprocesso.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.subprocesso.service.SubprocessoFacade;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubprocessoMapaListenerTest {

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @InjectMocks
    private SubprocessoMapaListener listener;

    @Test
    @DisplayName("Deve atualizar situação para em andamento ao receber evento de mapa alterado")
    void deveAtualizarSituacao() {
        Long mapaCodigo = 123L;
        EventoMapaAlterado evento = new EventoMapaAlterado(mapaCodigo);

        listener.handleMapaAlterado(evento);

        verify(subprocessoFacade).atualizarSituacaoParaEmAndamento(mapaCodigo);
    }
}

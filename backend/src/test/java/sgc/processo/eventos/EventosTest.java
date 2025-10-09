package sgc.processo.eventos;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventosTest {
    @Test
    void processoCriadoEvento_Getters() {
        ProcessoCriadoEvento evento = new ProcessoCriadoEvento(this, 1L);

        assertEquals(1L, evento.getIdProcesso());
        assertEquals(this, evento.getSource());
    }

    @Test
    void processoFinalizadoEvento_Getters() {
        ProcessoFinalizadoEvento evento = new ProcessoFinalizadoEvento(this, 1L);

        assertEquals(1L, evento.getIdProcesso());
        assertEquals(this, evento.getSource());
    }

    @Test
    void processoIniciadoEvento_Getters() {
        ProcessoIniciadoEvento evento = new ProcessoIniciadoEvento(
                1L, "MAPEAMENTO", LocalDateTime.now(), List.of(1L, 2L)
        );

        assertEquals(1L, evento.idProcesso());
        assertEquals("MAPEAMENTO", evento.tipo());
        assertNotNull(evento.dataHoraInicio());
        assertEquals(List.of(1L, 2L), evento.idsUnidades());
    }

    @Test
    void subprocessoDisponibilizadoEvento_Getters() {
        SubprocessoDisponibilizadoEvento evento = new SubprocessoDisponibilizadoEvento(1L);

        assertEquals(1L, evento.idSubprocesso());
    }
}
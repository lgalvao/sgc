package sgc.processo.eventos;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventosTest {
    @Test
    void processoCriadoEvento_Getters() {
        EventoProcessoCriado evento = new EventoProcessoCriado(this, 1L);

        assertEquals(1L, evento.getCodProcesso());
        assertEquals(this, evento.getSource());
    }

    @Test
    void processoFinalizadoEvento_Getters() {
        EventoProcessoFinalizado evento = new EventoProcessoFinalizado(this, 1L);

        assertEquals(1L, evento.getCodProcesso());
        assertEquals(this, evento.getSource());
    }

    @Test
    void processoIniciadoEvento_Getters() {
        EventoProcessoIniciado evento = new EventoProcessoIniciado(
                1L, "MAPEAMENTO", LocalDateTime.now(), List.of(1L, 2L)
        );

        assertEquals(1L, evento.codProcesso());
        assertEquals("MAPEAMENTO", evento.tipo());
        assertNotNull(evento.dataHoraInicio());
        assertEquals(List.of(1L, 2L), evento.codUnidades());
    }

    @Test
    void subprocessoDisponibilizadoEvento_Getters() {
        EventoSubprocessoDisponibilizado evento = new EventoSubprocessoDisponibilizado(1L);

        assertEquals(1L, evento.codSubprocesso());
    }
}
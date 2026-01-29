package sgc.processo.eventos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@DisplayName("Eventos de Processo")
class EventosTest {
    @Test
    @DisplayName("Deve retornar valores corretos para EventoProcessoFinalizado")
    void deveRetornarValoresCorretosParaEventoProcessoFinalizado() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        EventoProcessoFinalizado evento = new EventoProcessoFinalizado(1L, now);

        // Act & Assert
        assertEquals(1L, evento.getCodProcesso());
        assertEquals(now, evento.getDataHoraFinalizacao());
    }

    @Test
    @DisplayName("Deve retornar valores corretos para EventoProcessoIniciado")
    void deveRetornarValoresCorretosParaEventoProcessoIniciado() {
        // Arrange
        EventoProcessoIniciado evento =
                new EventoProcessoIniciado(1L, "MAPEAMENTO", LocalDateTime.now(), List.of(1L, 2L));

        // Act & Assert
        assertEquals(1L, evento.getCodProcesso());
        assertEquals("MAPEAMENTO", evento.getTipo());
        assertNotNull(evento.getDataHoraInicio());
        assertEquals(List.of(1L, 2L), evento.getCodUnidades());
    }

}

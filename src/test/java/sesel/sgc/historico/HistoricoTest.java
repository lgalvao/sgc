package sesel.sgc.historico;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoricoTest {
    @Test
    void testGettersAndSetters() {
        Historico historico = new Historico();
        List<EventoHistorico> eventos = new ArrayList<>();
        EventoHistorico evento = new EventoHistorico();
        eventos.add(evento);
        historico.setEventos(eventos);
        UUID id = UUID.randomUUID();
        historico.setId(id);

        assertEquals(eventos, historico.getEventos());
        assertEquals(id, historico.getId());
    }

    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(Historico.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    @Test
    void construtorEGetters() {
        Historico historico = new Historico();
        assertNotNull(historico);
    }
}
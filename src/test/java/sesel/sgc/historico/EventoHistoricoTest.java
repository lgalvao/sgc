package sesel.sgc.historico;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventoHistoricoTest {
    @Test
    void testGettersAndSetters() {
        EventoHistorico evento = new EventoHistorico();
        EventoHistoricoTipo tipo = EventoHistoricoTipo.CRIACAO_PROCESSO;
        LocalDateTime dataHora = LocalDateTime.now();
        Unidade unidade = new Unidade();
        Historico historico = new Historico();
        String detalhes = "Detalhes do evento";
        UUID id = UUID.randomUUID();

        evento.setTipo(tipo);
        evento.setDataHora(dataHora);
        evento.setUnidade(unidade);
        evento.setHistorico(historico);
        evento.setDetalhes(detalhes);
        evento.setId(id);

        assertEquals(tipo, evento.getTipo());
        assertEquals(dataHora, evento.getDataHora());
        assertEquals(unidade, evento.getUnidade());
        assertEquals(historico, evento.getHistorico());
        assertEquals(detalhes, evento.getDetalhes());
        assertEquals(id, evento.getId());
    }

    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(EventoHistorico.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    @Test
    void construtorEGetters() {
        EventoHistorico evento = new EventoHistorico();
        assertNotNull(evento);
    }
}
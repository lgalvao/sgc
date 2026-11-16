package sesel.sgc.mapa.conhecimentos;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConhecimentoTest {
    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(Conhecimento.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    @Test
    void testSetAndGetDescricaoTipo() {
        Conhecimento c = new Conhecimento();
        c.setDescricao("Java");
        c.setTipo(ConhecimentoTipo.TECNICO);
        assertEquals("Java", c.getDescricao());
        assertEquals(ConhecimentoTipo.TECNICO, c.getTipo());
    }
}
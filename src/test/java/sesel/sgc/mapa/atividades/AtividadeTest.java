package sesel.sgc.mapa.atividades;

import org.junit.jupiter.api.Test;
import sesel.sgc.mapa.conhecimentos.Conhecimento;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtividadeTest {
    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        Atividade a1 = new Atividade();
        a1.setId(id);

        Atividade a2 = new Atividade();
        a2.setId(id);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testConhecimentosManipulation() {
        Atividade atividade = new Atividade();
        Conhecimento c1 = new Conhecimento();
        Conhecimento c2 = new Conhecimento();

        List<Conhecimento> conhecimentos = new java.util.ArrayList<>();
        conhecimentos.add(c1);
        conhecimentos.add(c2);

        atividade.setConhecimentos(conhecimentos);

        assertEquals(2, atividade.getConhecimentos().size());
        atividade.getConhecimentos().remove(c1);
        assertEquals(1, atividade.getConhecimentos().size());
    }
}
package sesel.sgc.mapa;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import sesel.sgc.unidade.Unidade;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapaUnidadeTest {
    @Test
    void testGettersAndSetters() {
        MapaUnidade mapa = new MapaUnidade();
        Unidade unidade = new Unidade();
        List<Competencia> competencias = new ArrayList<>();
        Competencia comp = new Competencia();
        competencias.add(comp);
        mapa.setUnidade(unidade);
        mapa.setCompetencias(competencias);
        UUID id = UUID.randomUUID();
        mapa.setId(id);

        assertEquals(unidade, mapa.getUnidade());
        assertEquals(competencias, mapa.getCompetencias());
        assertEquals(id, mapa.getId());
    }

    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(MapaUnidade.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }
}
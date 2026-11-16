package sesel.sgc.mapa;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import sesel.sgc.mapa.atividades.Atividade;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompetenciaTest {
    @Test
    void testGettersAndSetters() {
        Competencia competencia = new Competencia();
        String descricao = "Descrição";
        String detalhamento = "Detalhamento";
        MapaUnidade mapaUnidade = new MapaUnidade();
        List<Atividade> atividades = new ArrayList<>();
        Atividade atividade = new Atividade();
        atividades.add(atividade);
        UUID id = UUID.randomUUID();

        competencia.setDescricao(descricao);
        competencia.setDetalhamento(detalhamento);
        competencia.setMapaUnidade(mapaUnidade);
        competencia.setAtividades(atividades);
        competencia.setId(id);

        assertEquals(descricao, competencia.getDescricao());
        assertEquals(detalhamento, competencia.getDetalhamento());
        assertEquals(mapaUnidade, competencia.getMapaUnidade());
        assertEquals(atividades, competencia.getAtividades());
        assertEquals(id, competencia.getId());
    }

    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(Competencia.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }
}
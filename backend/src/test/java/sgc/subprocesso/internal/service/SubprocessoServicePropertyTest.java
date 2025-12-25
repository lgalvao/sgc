package sgc.subprocesso.internal.service;

import net.jqwik.api.*;
import org.mockito.Mockito;
import sgc.atividade.api.model.Atividade;
import sgc.atividade.api.model.AtividadeRepo;
import sgc.atividade.api.model.ConhecimentoRepo;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.api.model.Competencia;
import sgc.mapa.api.model.CompetenciaRepo;
import sgc.mapa.api.model.Mapa;
import sgc.mapa.api.model.MapaRepo;
import sgc.subprocesso.internal.mappers.SubprocessoMapper;
import sgc.subprocesso.internal.model.SubprocessoRepo;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("pbt")
class SubprocessoServicePropertyTest {

    @Provide
    Arbitrary<GraphScenario> connectedGraphs() {
        return graphs(true);
    }

    @Provide
    Arbitrary<GraphScenario> disconnectedGraphs() {
        return graphs(false);
    }

    private Arbitrary<GraphScenario> graphs(boolean connected) {
        return Arbitraries.randomValue(random -> {
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);

            int numComps = random.nextInt(1, 5);
            int numAtivs = random.nextInt(1, 5);

            List<Competencia> comps = new ArrayList<>();
            for (int i = 0; i < numComps; i++) {
                comps.add(new Competencia((long)i + 1000, "C"+i, mapa));
            }

            List<Atividade> ativs = new ArrayList<>();
            for (int i = 0; i < numAtivs; i++) {
                ativs.add(new Atividade(mapa, "A"+i));
                ativs.get(i).setCodigo((long)i + 2000);
            }

            if (connected) {
                // Ensure every node has at least one edge
                for (Competencia c : comps) {
                    Atividade a = ativs.get(random.nextInt(ativs.size()));
                    link(c, a);
                }
                for (Atividade a : ativs) {
                    if (a.getCompetencias().isEmpty()) {
                         Competencia c = comps.get(random.nextInt(comps.size()));
                         link(c, a);
                    }
                }
            } else {
                // Ensure at least one is disconnected
                // Clear all links first (fresh start)
                comps.forEach(c -> c.getAtividades().clear());
                ativs.forEach(a -> a.getCompetencias().clear());

                // Randomly link some
                int maxLinks = numComps * numAtivs / 2;
                int randomLinks = (maxLinks > 0) ? random.nextInt(0, maxLinks) : 0;
                for(int i=0; i<randomLinks; i++) {
                    link(comps.get(random.nextInt(comps.size())), ativs.get(random.nextInt(ativs.size())));
                }

                // Force at least one disconnection
                if (random.nextBoolean()) {
                    // Isolate a competency
                    Competencia isolated = comps.get(0);
                    isolated.getAtividades().clear();
                    for (Atividade a : ativs) {
                        a.getCompetencias().remove(isolated);
                    }
                } else {
                    // Isolate an activity
                    Atividade isolated = ativs.get(0);
                    isolated.getCompetencias().clear();
                    for (Competencia c : comps) {
                        c.getAtividades().remove(isolated);
                    }
                }
            }

            return new GraphScenario(mapa, comps, ativs);
        });
    }

    private void link(Competencia c, Atividade a) {
        c.getAtividades().add(a);
        a.getCompetencias().add(c);
    }

    @Property
    void validarAssociacoes_Connected(@ForAll("connectedGraphs") GraphScenario scenario) {
        SubprocessoService service = createService(scenario);
        service.validarAssociacoesMapa(1L); // Should not throw
    }

    @Property
    void validarAssociacoes_Disconnected(@ForAll("disconnectedGraphs") GraphScenario scenario) {
        SubprocessoService service = createService(scenario);
        assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
            .isInstanceOf(ErroValidacao.class);
    }

    private SubprocessoService createService(GraphScenario scenario) {
        SubprocessoRepo subRepo = mock(SubprocessoRepo.class);
        AtividadeRepo ativRepo = mock(AtividadeRepo.class);
        ConhecimentoRepo conRepo = mock(ConhecimentoRepo.class);
        CompetenciaRepo compRepo = mock(CompetenciaRepo.class);
        SubprocessoMapper mapper = mock(SubprocessoMapper.class);
        MapaRepo mapaRepo = mock(MapaRepo.class);

        when(compRepo.findByMapaCodigo(1L)).thenReturn(scenario.competencias);
        when(ativRepo.findByMapaCodigo(1L)).thenReturn(scenario.atividades);

        return new SubprocessoService(subRepo, ativRepo, conRepo, compRepo, mapper, mapaRepo);
    }

    record GraphScenario(Mapa mapa, List<Competencia> competencias, List<Atividade> atividades) {}
}

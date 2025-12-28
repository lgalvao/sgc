package sgc.mapa.mapper;

import net.jqwik.api.*;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MapaCompletoMapperPropertyTest {

    private final MapaCompletoMapper mapper = new MapaCompletoMapperImpl();

    @Property
    void toDtoPreservesData(@ForAll("mapas") Mapa mapa,
                            @ForAll("codSubprocessos") Long codSubprocesso,
                            @ForAll("competencias") List<Competencia> competencias) {

        MapaCompletoDto dto = mapper.toDto(mapa, codSubprocesso, competencias);

        assertThat(dto.getCodigo()).isEqualTo(mapa.getCodigo());
        assertThat(dto.getSubprocessoCodigo()).isEqualTo(codSubprocesso);
        assertThat(dto.getObservacoes()).isEqualTo(mapa.getObservacoesDisponibilizacao());

        if (competencias == null) {
            assertThat(dto.getCompetencias()).isNull();
        } else {
            assertThat(dto.getCompetencias()).hasSize(competencias.size());
            for (int i = 0; i < competencias.size(); i++) {
                var compDto = dto.getCompetencias().get(i);
                var compEntity = competencias.get(i);

                assertThat(compDto.getCodigo()).isEqualTo(compEntity.getCodigo());
                assertThat(compDto.getDescricao()).isEqualTo(compEntity.getDescricao());

                if (compEntity.getAtividades() != null) {
                    var expectedIds = compEntity.getAtividades().stream()
                            .map(Atividade::getCodigo)
                            .collect(Collectors.toList());
                    assertThat(compDto.getAtividadesCodigos()).containsExactlyInAnyOrderElementsOf(expectedIds);
                }
            }
        }
    }

    @Provide
    Arbitrary<Mapa> mapas() {
        return Arbitraries.longs().greaterOrEqual(1L).flatMap(codigo ->
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(0).ofMaxLength(1000).map(obs -> {
                    Mapa mapa = new Mapa();
                    mapa.setCodigo(codigo);
                    mapa.setObservacoesDisponibilizacao(obs);
                    return mapa;
                })
        );
    }

    @Provide
    Arbitrary<Long> codSubprocessos() {
        return Arbitraries.longs().greaterOrEqual(1L);
    }

    @Provide
    Arbitrary<List<Competencia>> competencias() {
        return Arbitraries.longs().greaterOrEqual(1L).list().ofMinSize(0).ofMaxSize(10).flatMap(codigos -> {
            List<Competencia> comps = new java.util.ArrayList<>();
            for (Long codigo : codigos) {
                Competencia c = new Competencia();
                c.setCodigo(codigo);
                c.setDescricao("Descricao " + codigo);

                Atividade a1 = new Atividade();
                a1.setCodigo(codigo + 100);
                Atividade a2 = new Atividade();
                a2.setCodigo(codigo + 101);

                c.setAtividades(Set.of(a1, a2));
                comps.add(c);
            }
            return Arbitraries.just(comps);
        });
    }
}

package sgc.subprocesso.internal.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.analise.internal.model.Analise;
import sgc.mapa.api.model.Competencia;
import sgc.mapa.api.model.Mapa;
import sgc.subprocesso.api.MapaAjusteDto;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.unidade.api.model.Unidade;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MapaAjusteMapperTest {

    private final MapaAjusteMapper mapper = Mappers.getMapper(MapaAjusteMapper.class);

    @Test
    void toDto() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        sp.setMapa(mapa);

        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste");
        sp.setUnidade(unidade);

        Analise analise = new Analise();
        analise.setObservacoes("Motivo da devolução");

        Competencia competencia = new Competencia();
        competencia.setCodigo(10L);
        competencia.setDescricao("Comp A");
        // Initialize activities set to avoid NullPointerException if mapper accesses it
        competencia.setAtividades(Collections.emptySet());

        MapaAjusteDto dto = mapper.toDto(sp, analise, Collections.singletonList(competencia), Collections.emptyList(), Collections.emptyList());

        assertThat(dto).isNotNull();
        assertThat(dto.getCodMapa()).isEqualTo(1L);
        assertThat(dto.getUnidadeNome()).isEqualTo("Unidade Teste");
        assertThat(dto.getJustificativaDevolucao()).isEqualTo("Motivo da devolução");
        assertThat(dto.getCompetencias()).hasSize(1);
        assertThat(dto.getCompetencias().get(0).getNome()).isEqualTo("Comp A");
    }
}

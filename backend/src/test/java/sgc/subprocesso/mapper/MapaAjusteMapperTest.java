package sgc.subprocesso.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.model.Analise;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do MapaAjusteMapper")
class MapaAjusteMapperTest {
    private final MapaAjusteMapper mapper = Mappers.getMapper(MapaAjusteMapper.class);

    @Test
    @DisplayName("Deve mapear corretamente para DTO")
    void deveMapearParaDto() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        sp.setMapa(mapa);
        Unidade u = new Unidade();
        u.setNome("Unidade 1");
        sp.setUnidade(u);

        Analise analise = new Analise();
        analise.setObservacoes("Justificativa");

        Competencia comp = new Competencia();
        comp.setCodigo(10L);
        comp.setDescricao("Comp 1");

        Atividade ativ = new Atividade();
        ativ.setCodigo(20L);
        ativ.setDescricao("Ativ 1");

        Conhecimento con = new Conhecimento();
        con.setCodigo(30L);
        con.setDescricao("Con 1");
        con.setAtividade(ativ);

        // Linkar
        comp.setAtividades(new HashSet<>(List.of(ativ)));

        Map<Long, Set<Long>> associacoes = new HashMap<>();
        associacoes.put(comp.getCodigo(), Set.of(ativ.getCodigo()));

        MapaAjusteDto dto = mapper.toDto(sp, analise, List.of(comp), List.of(ativ), List.of(con), associacoes);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodMapa()).isEqualTo(1L);
        assertThat(dto.getUnidadeNome()).isEqualTo("Unidade 1");
        assertThat(dto.getJustificativaDevolucao()).isEqualTo("Justificativa");

        assertThat(dto.getCompetencias()).hasSize(1);
        assertThat(dto.getCompetencias().getFirst().getAtividades()).hasSize(1);
        assertThat(dto.getCompetencias().getFirst().getAtividades().getFirst().conhecimentos()).hasSize(1);
        assertThat(dto.getCompetencias().getFirst().getAtividades().getFirst().conhecimentos().getFirst().incluido()).isTrue();
    }
}

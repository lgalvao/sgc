package sgc.subprocesso.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.analise.model.Analise;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MapaAjusteMapper Coverage Tests")
class MapaAjusteMapperCoverageTest {

    private final MapaAjusteMapper mapper = new MapaAjusteMapperImpl();

    @Test
    @DisplayName("Deve retornar null quando todos os parâmetros são nulos")
    void deveRetornarNullQuandoTodosNulos() {
        assertThat(mapper.toDto(null, null, null, null, null, null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir branches de Subprocesso nulo ou com campos nulos")
    void deveCobrirBranchesSubprocesso() {
        Subprocesso sp = new Subprocesso();
        MapaAjusteDto dto = mapper.toDto(sp, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new HashMap<>());

        assertThat(dto).isNotNull();
        assertThat(dto.getCodMapa()).isNull();
        assertThat(dto.getUnidadeNome()).isNull();

        sp.setMapa(Mapa.builder().codigo(1L).build());
        sp.setUnidade(Unidade.builder().nome("Unidade 1").build());
        dto = mapper.toDto(sp, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new HashMap<>());
        assertThat(dto.getCodMapa()).isEqualTo(1L);
        assertThat(dto.getUnidadeNome()).isEqualTo("Unidade 1");
    }

    @Test
    @DisplayName("Deve cobrir lógica complexa de mapCompetencias")
    void deveCobrirMapCompetencias() {
        Competencia comp = Competencia.builder().codigo(10L).descricao("Comp 1").build();
        Atividade ativ = Atividade.builder().codigo(20L).descricao("Ativ 1").build();

        Conhecimento con = new Conhecimento();
        con.setCodigo(30L);
        con.setDescricao("Con 1");
        con.setAtividade(ativ);

        Map<Long, java.util.Set<Long>> associacoes = new HashMap<>();
        associacoes.put(10L, java.util.Set.of(20L));

        MapaAjusteDto dto = mapper.toDto(null, null, List.of(comp), List.of(ativ), List.of(con), associacoes);

        assertThat(dto.getCompetencias()).hasSize(1);
        assertThat(dto.getCompetencias().get(0).getAtividades()).hasSize(1);
        assertThat(dto.getCompetencias().get(0).getAtividades().get(0).conhecimentos()).hasSize(1);
        assertThat(dto.getCompetencias().get(0).getAtividades().get(0).conhecimentos().get(0).incluido()).isTrue();
    }
}

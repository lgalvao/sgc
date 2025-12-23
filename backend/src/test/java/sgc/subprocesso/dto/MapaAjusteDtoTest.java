package sgc.subprocesso.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sgc.analise.internal.model.Analise;
import sgc.atividade.internal.model.Atividade;
import sgc.atividade.internal.model.Conhecimento;
import sgc.mapa.internal.model.Competencia;
import sgc.mapa.internal.model.Mapa;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.internal.model.Unidade;

class MapaAjusteDtoTest {

    @Test
    @DisplayName("Deve criar MapaAjusteDto corretamente via método estático 'of'")
    void deveCriarViaOf() {
        Subprocesso subprocesso = mock(Subprocesso.class);
        Analise analise = mock(Analise.class);
        Mapa mapa = mock(Mapa.class);
        Unidade unidade = mock(Unidade.class);

        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(100L);
        when(subprocesso.getUnidade()).thenReturn(unidade);
        when(unidade.getNome()).thenReturn("Unidade Teste");
        when(analise.getObservacoes()).thenReturn("Justificativa");

        Competencia comp = mock(Competencia.class);
        when(comp.getCodigo()).thenReturn(1L);
        when(comp.getDescricao()).thenReturn("Comp 1");

        Atividade ativ = mock(Atividade.class);
        when(ativ.getCodigo()).thenReturn(2L);
        when(ativ.getDescricao()).thenReturn("Ativ 1");

        Conhecimento con = mock(Conhecimento.class);
        when(con.getCodigo()).thenReturn(3L);
        when(con.getDescricao()).thenReturn("Con 1");
        when(con.getAtividade()).thenReturn(ativ);

        // Configurar relação Competencia -> Atividades para 'isLinked'
        Set<Atividade> atividadesSet = new HashSet<>();
        atividadesSet.add(ativ);
        when(comp.getAtividades()).thenReturn(atividadesSet);

        List<Competencia> competencias = List.of(comp);
        List<Atividade> atividades = List.of(ativ);
        List<Conhecimento> conhecimentos = List.of(con);

        MapaAjusteDto dto = MapaAjusteDto.of(subprocesso, analise, competencias, atividades, conhecimentos);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodMapa()).isEqualTo(100L);
        assertThat(dto.getUnidadeNome()).isEqualTo("Unidade Teste");
        assertThat(dto.getJustificativaDevolucao()).isEqualTo("Justificativa");
        assertThat(dto.getCompetencias()).hasSize(1);

        CompetenciaAjusteDto compDto = dto.getCompetencias().get(0);
        assertThat(compDto.getCodCompetencia()).isEqualTo(1L);
        assertThat(compDto.getNome()).isEqualTo("Comp 1");
        assertThat(compDto.getAtividades()).hasSize(1);

        AtividadeAjusteDto ativDto = compDto.getAtividades().get(0);
        assertThat(ativDto.getCodAtividade()).isEqualTo(2L);
        assertThat(ativDto.getNome()).isEqualTo("Ativ 1");
        assertThat(ativDto.getConhecimentos()).hasSize(1);

        ConhecimentoAjusteDto conDto = ativDto.getConhecimentos().get(0);
        assertThat(conDto.getConhecimentoCodigo()).isEqualTo(3L);
        assertThat(conDto.getNome()).isEqualTo("Con 1");
        assertThat(conDto.isIncluido()).isTrue();
    }

    @Test
    @DisplayName("Deve lidar com listas vazias e nulos")
    void deveLidarComVazios() {
        Subprocesso subprocesso = mock(Subprocesso.class);
        Mapa mapa = mock(Mapa.class);
        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(100L);
        when(subprocesso.getUnidade()).thenReturn(null);

        MapaAjusteDto dto = MapaAjusteDto.of(
                subprocesso,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertThat(dto).isNotNull();
        assertThat(dto.getUnidadeNome()).isEmpty();
        assertThat(dto.getJustificativaDevolucao()).isNull();
        assertThat(dto.getCompetencias()).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar builder")
    void deveVerificarBuilder() {
        MapaAjusteDto dto = MapaAjusteDto.builder()
                .codMapa(1L)
                .unidadeNome("Nome")
                .competencias(new ArrayList<>())
                .justificativaDevolucao("Just")
                .build();

        assertThat(dto.getCodMapa()).isEqualTo(1L);
        assertThat(dto.getUnidadeNome()).isEqualTo("Nome");
        assertThat(dto.getCompetencias()).isEmpty();
        assertThat(dto.getJustificativaDevolucao()).isEqualTo("Just");
    }
}

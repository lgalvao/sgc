package sgc.subprocesso.dto;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import sgc.analise.model.Analise;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

@Tag("unit")
@DisplayName("Testes de MapaAjusteDto")
class MapaAjusteDtoTest {
    private Subprocesso subprocesso;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste");

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("Deve validar funcionamento do Builder")
    void deveValidarBuilder() {
        MapaAjusteDto dto = MapaAjusteDto.builder()
                .codMapa(1L)
                .unidadeNome("Unidade")
                .justificativaDevolucao("Obs")
                .competencias(List.of())
                .build();

        assertThat(dto.getCodMapa()).isEqualTo(1L);
        assertThat(dto.getUnidadeNome()).isEqualTo("Unidade");
        assertThat(dto.getJustificativaDevolucao()).isEqualTo("Obs");
        assertThat(dto.getCompetencias()).isEmpty();
    }

    @Nested
    @DisplayName("Método de criação estático of")
    @SuppressWarnings("unused")
    class OfMethodTests {
       @Test
        @DisplayName("Deve mapear corretamente os dados básicos")
        void deveMapearDadosBasicos() {
            Analise analise = new Analise();
            analise.setObservacoes("Justificativa Teste");

            MapaAjusteDto dto = MapaAjusteDto.of(
                    subprocesso, analise, List.of(), List.of(), List.of());

            assertThat(dto.getCodMapa()).isEqualTo(100L);
            assertThat(dto.getUnidadeNome()).isEqualTo("Unidade Teste");
            assertThat(dto.getJustificativaDevolucao()).isEqualTo("Justificativa Teste");
        }

        @Test
        @DisplayName("Deve mapear hierarquia de competências e atividades")
        void deveMapearHierarquia() {
            // Setup
            Atividade ativ = new Atividade();
            ativ.setCodigo(2L);
            ativ.setDescricao("Ativ 1");

            Conhecimento con = new Conhecimento();
            con.setCodigo(3L);
            con.setDescricao("Con 1");
            con.setAtividade(ativ);

            Competencia comp = new Competencia();
            comp.setCodigo(1L);
            comp.setDescricao("Comp 1");
            comp.setAtividades(Set.of(ativ));

            // Execute
            Analise analise = new Analise();
            analise.setObservacoes("");
            MapaAjusteDto dto = MapaAjusteDto.of(
                    subprocesso, analise, List.of(comp), List.of(ativ), List.of(con));

            // Assert
            assertThat(dto.getCompetencias()).hasSize(1);
            CompetenciaAjusteDto compDto = dto.getCompetencias().getFirst();
            assertThat(compDto.getCodCompetencia()).isEqualTo(1L);

            assertThat(compDto.getAtividades()).hasSize(1);
            AtividadeAjusteDto ativDto = compDto.getAtividades().getFirst();
            assertThat(ativDto.codAtividade()).isEqualTo(2L);

            assertThat(ativDto.conhecimentos()).hasSize(1);
            ConhecimentoAjusteDto conDto = ativDto.conhecimentos().getFirst();
            assertThat(conDto.conhecimentoCodigo()).isEqualTo(3L);
            assertThat(conDto.incluido()).isTrue();
        }
    }
}

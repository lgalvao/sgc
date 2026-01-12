package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;
import sgc.analise.model.Analise;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de MapaAjusteDto")
class MapaAjusteDtoTest {

    private Subprocesso subprocesso;
    private Mapa mapa;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = new Unidade();
        unidade.setNome("Unidade Teste");

        mapa = new Mapa();
        mapa.setCodigo(100L);

        subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
    }

    @Nested
    @DisplayName("Método de criação estático of")
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
            MapaAjusteDto dto = MapaAjusteDto.of(
                    subprocesso, null, List.of(comp), List.of(ativ), List.of(con));

            // Assert
            assertThat(dto.getCompetencias()).hasSize(1);
            CompetenciaAjusteDto compDto = dto.getCompetencias().get(0);
            assertThat(compDto.getCodCompetencia()).isEqualTo(1L);
            
            assertThat(compDto.getAtividades()).hasSize(1);
            AtividadeAjusteDto ativDto = compDto.getAtividades().get(0);
            assertThat(ativDto.getCodAtividade()).isEqualTo(2L);
            
            assertThat(ativDto.getConhecimentos()).hasSize(1);
            ConhecimentoAjusteDto conDto = ativDto.getConhecimentos().get(0);
            assertThat(conDto.getConhecimentoCodigo()).isEqualTo(3L);
            assertThat(conDto.isIncluido()).isTrue();
        }

        @Test
        @DisplayName("Deve lidar com valores nulos de forma segura")
        void deveLidarComNulos() {
            subprocesso.setUnidade(null);
            
            MapaAjusteDto dto = MapaAjusteDto.of(
                    subprocesso, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

            assertThat(dto.getUnidadeNome()).isEmpty();
            assertThat(dto.getJustificativaDevolucao()).isNull();
            assertThat(dto.getCompetencias()).isEmpty();
        }
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
}

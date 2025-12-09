package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.TipoImpactoAtividade;
import sgc.mapa.service.ImpactoCompetenciaService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoCompetenciaServiceTest {
    @InjectMocks private ImpactoCompetenciaService service;

    @Mock private CompetenciaRepo competenciaRepo;

    @Mock private AtividadeRepo atividadeRepo;

    private Mapa mapa;
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        mapa = new Mapa();
        mapa.setCodigo(1L);

        competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao("Comp 1");
        competencia.setMapa(mapa);
    }

    @Nested
    @DisplayName("Testes para identificarCompetenciasImpactadas")
    class IdentificarCompetenciasImpactadasTests {
        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades removidas ou alteradas")
        void identificarCompetenciasImpactadas_SemAtividades_RetornaVazio() {
            List<CompetenciaImpactadaDto> result =
                    service.identificarCompetenciasImpactadas(
                            mapa, Collections.emptyList(), Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve identificar competências impactadas por atividades removidas")
        void identificarCompetenciasImpactadas_AtividadesRemovidas_RetornaImpactos() {
            AtividadeImpactadaDto atividadeRemovidaDto =
                    new AtividadeImpactadaDto(
                            1L,
                            "Atividade Removida",
                            TipoImpactoAtividade.REMOVIDA,
                            null,
                            Collections.emptyList());
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);

            competencia.setAtividades(Set.of(atividade));
            atividade.setCompetencias(Set.of(competencia));

            when(competenciaRepo.findByMapaCodigo(mapa.getCodigo()))
                    .thenReturn(Collections.singletonList(competencia));
            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));

            List<CompetenciaImpactadaDto> result =
                    service.identificarCompetenciasImpactadas(
                            mapa,
                            Collections.singletonList(atividadeRemovidaDto),
                            Collections.emptyList());

            assertThat(result).isNotEmpty();
            assertThat(result.getFirst().getDescricao()).isEqualTo("Comp 1");
            assertThat(result.getFirst().getTipoImpacto().name()).isEqualTo("ATIVIDADE_REMOVIDA");
        }

        @Test
        @DisplayName("Deve identificar competências impactadas por atividades alteradas")
        void identificarCompetenciasImpactadas_AtividadesAlteradas() {
            AtividadeImpactadaDto atividadeDto =
                    new AtividadeImpactadaDto(
                            1L,
                            "Atividade Nova",
                            TipoImpactoAtividade.ALTERADA,
                            "Atividade Antiga",
                            Collections.emptyList());
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);

            competencia.setAtividades(Set.of(atividade));
            atividade.setCompetencias(Set.of(competencia));

            when(competenciaRepo.findByMapaCodigo(mapa.getCodigo()))
                    .thenReturn(Collections.singletonList(competencia));
            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));

            List<CompetenciaImpactadaDto> result =
                    service.identificarCompetenciasImpactadas(
                            mapa, Collections.emptyList(), Collections.singletonList(atividadeDto));

            assertThat(result).isNotEmpty();
            assertThat(result.getFirst().getTipoImpacto().name()).isEqualTo("ATIVIDADE_ALTERADA");
        }

        @Test
        @DisplayName("Deve identificar impacto genérico quando houver remoção e alteração")
        void identificarCompetenciasImpactadas_Misto() {
            AtividadeImpactadaDto remDto =
                    new AtividadeImpactadaDto(
                            1L,
                            "Ativ Rem",
                            TipoImpactoAtividade.REMOVIDA,
                            null,
                            Collections.emptyList());
            AtividadeImpactadaDto altDto =
                    new AtividadeImpactadaDto(
                            2L,
                            "Ativ Alt",
                            TipoImpactoAtividade.ALTERADA,
                            "Old",
                            Collections.emptyList());

            Atividade ativ1 = new Atividade();
            ativ1.setCodigo(1L);
            Atividade ativ2 = new Atividade();
            ativ2.setCodigo(2L);

            competencia.setAtividades(Set.of(ativ1, ativ2));
            ativ1.setCompetencias(Set.of(competencia));
            ativ2.setCompetencias(Set.of(competencia));

            when(competenciaRepo.findByMapaCodigo(mapa.getCodigo()))
                    .thenReturn(Collections.singletonList(competencia));
            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(ativ1));
            when(atividadeRepo.findById(2L)).thenReturn(Optional.of(ativ2));

            List<CompetenciaImpactadaDto> result =
                    service.identificarCompetenciasImpactadas(
                            mapa,
                            Collections.singletonList(remDto),
                            Collections.singletonList(altDto));

            assertThat(result).isNotEmpty();
            assertThat(result.getFirst().getTipoImpacto().name()).isEqualTo("IMPACTO_GENERICO");
        }
    }

    @Test
    @DisplayName("obterCompetenciasDaAtividade retorna lista de descricoes")
    void obterCompetenciasDaAtividade() {
        Atividade ativ = new Atividade();
        ativ.setCodigo(1L);
        ativ.setCompetencias(Set.of(competencia));

        when(atividadeRepo.findById(1L)).thenReturn(Optional.of(ativ));

        List<String> res = service.obterCompetenciasDaAtividade(1L, mapa);

        assertThat(res).hasSize(1);
        assertThat(res.getFirst()).isEqualTo("Comp 1");
    }

    @Test
    @DisplayName("obterCompetenciasDaAtividade retorna vazio se atividade nao existe")
    void obterCompetenciasDaAtividadeNaoEncontrada() {
        when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());

        List<String> res = service.obterCompetenciasDaAtividade(1L, mapa);

        assertThat(res).isEmpty();
    }
}

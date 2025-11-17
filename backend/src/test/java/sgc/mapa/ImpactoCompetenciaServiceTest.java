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
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.TipoImpactoAtividade;
import sgc.mapa.service.ImpactoCompetenciaService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoCompetenciaServiceTest {

    @InjectMocks
    private ImpactoCompetenciaService service;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private AtividadeRepo atividadeRepo;

    private Mapa mapa;
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        mapa = new Mapa();
        mapa.setCodigo(1L);

        competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setMapa(mapa);
    }

    @Nested
    @DisplayName("Testes para identificarCompetenciasImpactadas")
    class IdentificarCompetenciasImpactadasTests {
        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades removidas ou alteradas")
        void identificarCompetenciasImpactadas_SemAtividades_RetornaVazio() {
            List<CompetenciaImpactadaDto> result = service.identificarCompetenciasImpactadas(mapa, Collections.emptyList(), Collections.emptyList());
            assert (result.isEmpty());
        }

        @Test
        @DisplayName("Deve identificar competências impactadas por atividades removidas")
        void identificarCompetenciasImpactadas_AtividadesRemovidas_RetornaImpactos() {
            AtividadeImpactadaDto atividadeRemovidaDto = new AtividadeImpactadaDto(1L, "Atividade Removida", TipoImpactoAtividade.REMOVIDA, null, Collections.emptyList());
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);

            // Establish the bidirectional relationship for the mock
            competencia.setAtividades(Set.of(atividade));
            atividade.setCompetencias(Set.of(competencia));

            // Mock the repository calls
            when(competenciaRepo.findByMapaCodigo(mapa.getCodigo())).thenReturn(Collections.singletonList(competencia));
            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));

            List<CompetenciaImpactadaDto> result = service.identificarCompetenciasImpactadas(mapa, Collections.singletonList(atividadeRemovidaDto), Collections.emptyList());

            assertFalse(result.isEmpty());
        }
    }
}

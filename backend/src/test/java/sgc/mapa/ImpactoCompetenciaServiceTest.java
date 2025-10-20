package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.TipoImpactoAtividade;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoCompetenciaServiceTest {

    @InjectMocks
    private ImpactoCompetenciaService service;

    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    private Mapa mapa;
    private Competencia competencia;
    private CompetenciaAtividade competenciaAtividade;

    @BeforeEach
    void setUp() {
        mapa = new Mapa();
        mapa.setCodigo(1L);

        competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setMapa(mapa);

        competenciaAtividade = new CompetenciaAtividade();
        competenciaAtividade.setId(new CompetenciaAtividade.Id(1L, 1L));
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
            AtividadeImpactadaDto atividadeRemovida = new AtividadeImpactadaDto(1L, "Atividade Removida", TipoImpactoAtividade.REMOVIDA, null, Collections.emptyList());
            when(competenciaAtividadeRepo.findByAtividadeCodigo(1L)).thenReturn(Collections.singletonList(competenciaAtividade));
            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(competencia));

            List<CompetenciaImpactadaDto> result = service.identificarCompetenciasImpactadas(mapa, Collections.singletonList(atividadeRemovida), Collections.emptyList());

            assertFalse(result.isEmpty());
        }
    }
}

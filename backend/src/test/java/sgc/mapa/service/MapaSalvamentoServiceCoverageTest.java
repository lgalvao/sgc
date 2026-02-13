package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("MapaSalvamentoService - Cobertura Adicional")
class MapaSalvamentoServiceCoverageTest {

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private MapaCompletoMapper mapaCompletoMapper;

    @InjectMocks
    private MapaSalvamentoService service;

    @Test
    @DisplayName("salvarMapaCompleto deve buscar competência no repo se não estiver no mapa atual")
    void deveBuscarCompetenciaNoRepoSeNaoEstiverNoMapa() {
        // Arrange
        Long codMapa = 1L;
        Long codComp = 100L;
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(new ArrayList<>()); // Mapa vazio
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(new ArrayList<>());

        CompetenciaMapaDto compDto = new CompetenciaMapaDto(codComp, "Desc", List.of());
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", List.of(compDto));

        Competencia competenciaExistente = new Competencia();
        competenciaExistente.setCodigo(codComp);
        when(repo.buscar(Competencia.class, codComp)).thenReturn(competenciaExistente);
        when(competenciaRepo.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.salvarMapaCompleto(codMapa, request);

        // Assert
        verify(repo).buscar(Competencia.class, codComp); // Cobertura da linha 128
    }
}

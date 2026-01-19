package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapaSalvamentoServiceCoverageTest {

    @Mock private MapaRepo mapaRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private RepositorioComum repo;
    @Mock private MapaCompletoMapper mapaCompletoMapper;

    @InjectMocks
    private MapaSalvamentoService service;

    @Test
    @DisplayName("salvarMapaCompleto - Sem Atividades (Branch 154)")
    void salvarMapaCompleto_SemAtividades() {
        Long codMapa = 1L;
        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .observacoes("Obs")
                .competencias(Collections.emptyList())
                .build();

        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);

        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList()); // Empty activities

        when(competenciaRepo.saveAll(any())).thenReturn(Collections.emptyList());

        service.salvarMapaCompleto(codMapa, request);

        // Verify saveAll called with empty list of activities
        verify(atividadeRepo).saveAll(Collections.emptyList());
    }

    @Test
    @DisplayName("salvarMapaCompleto - Competencia Sem Atividade (Lines 220-221)")
    void salvarMapaCompleto_CompetenciaSemAtividade() {
        Long codMapa = 1L;

        CompetenciaMapaDto compDto = CompetenciaMapaDto.builder()
                .descricao("Comp 1")
                .atividadesCodigos(Collections.emptyList()) // No activities linked
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .observacoes("Obs")
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);

        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());

        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        atividade.setMapa(mapa);
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(atividade));

        Competencia competenciaSalva = new Competencia();
        competenciaSalva.setCodigo(10L);
        competenciaSalva.setDescricao("Comp 1");
        competenciaSalva.setAtividades(new java.util.HashSet<>());
        when(competenciaRepo.saveAll(any())).thenReturn(List.of(competenciaSalva));

        service.salvarMapaCompleto(codMapa, request);

        // This should trigger the warning log "Competência {} sem atividades vinculadas"
        verify(atividadeRepo).saveAll(any());
    }
}

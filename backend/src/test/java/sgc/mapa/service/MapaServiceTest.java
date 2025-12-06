package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapaServiceTest {

    @Mock private MapaRepo mapaRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private MapaIntegridadeService mapaIntegridadeService;
    @Mock private MapaVinculoService mapaVinculoService;

    @InjectMocks private MapaService service;

    @Test
    @DisplayName("obterMapaCompleto sucesso")
    void obterMapaCompleto() {
        Long id = 1L;
        when(mapaRepo.findById(id)).thenReturn(Optional.of(new Mapa()));
        when(competenciaRepo.findByMapaCodigo(id)).thenReturn(List.of());

        var res = service.obterMapaCompleto(id, 10L);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("salvarMapaCompleto sucesso")
    void salvarMapaCompleto() {
        Long id = 1L;
        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setCompetencias(List.of());
        req.setObservacoes("obs");

        when(mapaRepo.findById(id)).thenReturn(Optional.of(new Mapa()));
        when(competenciaRepo.findByMapaCodigo(id)).thenReturn(List.of());
        when(mapaRepo.save(any())).thenReturn(new Mapa());

        service.salvarMapaCompleto(id, req, "user");

        verify(mapaRepo).save(any());
    }
}

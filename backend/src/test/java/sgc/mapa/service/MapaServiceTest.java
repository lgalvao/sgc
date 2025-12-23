package sgc.mapa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.atividade.internal.model.AtividadeRepo;
import sgc.mapa.api.MapaCompletoDto;
import sgc.mapa.api.SalvarMapaRequest;
import sgc.mapa.internal.mapper.MapaCompletoMapper;
import sgc.mapa.internal.model.CompetenciaRepo;
import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.model.MapaRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para MapaService")
class MapaServiceTest {

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private MapaCompletoMapper mapaCompletoMapper;

    @InjectMocks
    private MapaService service;

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {
        @Test
        @DisplayName("Deve obter mapa completo com sucesso")
        void deveObterMapaCompletoComSucesso() {
            Long id = 1L;
            when(mapaRepo.findById(id)).thenReturn(Optional.of(new Mapa()));
            when(competenciaRepo.findByMapaCodigo(id)).thenReturn(List.of());
            when(mapaCompletoMapper.toDto(any(), any(), any())).thenReturn(MapaCompletoDto.builder().build());

            MapaCompletoDto res = service.obterMapaCompleto(id, 10L);

            assertThat(res).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cenários de Escrita")
    class EscritaTests {
        @Test
        @DisplayName("Deve salvar mapa completo com sucesso")
        void deveSalvarMapaCompletoComSucesso() {
            Long id = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of());
            req.setObservacoes("obs");

            when(mapaRepo.findById(id)).thenReturn(Optional.of(new Mapa()));
            when(competenciaRepo.findByMapaCodigo(id)).thenReturn(List.of());
            when(atividadeRepo.findByMapaCodigo(id)).thenReturn(List.of());
            when(mapaRepo.save(any())).thenReturn(new Mapa());
            when(mapaCompletoMapper.toDto(any(), any(), any())).thenReturn(MapaCompletoDto.builder().build());

            service.salvarMapaCompleto(id, req, "user");

            verify(mapaRepo).save(any());
        }
    }
}

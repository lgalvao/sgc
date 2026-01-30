package sgc.mapa.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import java.util.concurrent.atomic.AtomicLong;
import sgc.mapa.mapper.MapaCompletoMapper;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
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
    @DisplayName("Deve executar validação de integridade gerando warnings")
    void deveGerarWarningsDeIntegridade() {
        // Cobre linhas de log.warn em validarIntegridadeMapa (220, 221 aprox)
        Long codMapa = 1L;

        // Mapa e atividade existente
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);

        Atividade atividade = new Atividade();
        atividade.setCodigo(10L);
        atividade.setMapa(mapa);
        atividade.setCompetencias(new HashSet<>()); // Sem competências iniciais

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(atividade));
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(new ArrayList<>()); // Sem competências prévias

        // Request adiciona nova competência, mas SEM associar à atividade
        CompetenciaMapaDto novaComp = CompetenciaMapaDto.builder()
                .descricao("Nova Competencia")
                .atividadesCodigos(new ArrayList<>()) // Lista vazia de associações
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(novaComp))
                .observacoes("Obs")
                .build();

        // Mocks de salvamento
        var idCounter = new AtomicLong(100L);
        when(competenciaRepo.saveAll(any())).thenAnswer(inv -> {
            List<Competencia> list = inv.getArgument(0);
            // Simula salvamento retornando as mesmas instancias com IDs sequenciais
            list.forEach(c -> c.setCodigo(idCounter.incrementAndGet()));
            return list;
        });

        // Executa
        service.salvarMapaCompleto(codMapa, request);

        verify(mapaRepo).save(mapa);
    }
}

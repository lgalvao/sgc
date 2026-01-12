package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeDeveriaExistir;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("MapaSalvamentoService - Gaps de Cobertura")
class MapaSalvamentoServiceTest {

    @Mock private MapaRepo mapaRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private MapaCompletoMapper mapaCompletoMapper;

    @InjectMocks
    private MapaSalvamentoService mapaSalvamentoService;

    @Test
    @DisplayName("Linhas 135-136: Deve falhar ao salvar competência inexistente que deveria existir")
    void deveFalharCompetenciaInexistente() {
        Long codMapa = 1L;
        SalvarMapaRequest request = new SalvarMapaRequest();
        CompetenciaMapaDto compDto = new CompetenciaMapaDto();
        compDto.setCodigo(999L); // Código que não existe no mapa
        compDto.setDescricao("Desc");
        request.setCompetencias(List.of(compDto));

        Mapa mapa = new Mapa();
        when(mapaRepo.findById(codMapa)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());

        assertThrows(ErroEntidadeDeveriaExistir.class, () -> 
            mapaSalvamentoService.salvarMapaCompleto(codMapa, request));
    }

    @Test
    @DisplayName("Linha 188: Deve falhar ao associar atividade que não pertence ao mapa")
    void deveFalharAtividadeNaoPertenceAoMapa() {
        Long codMapa = 1L;
        SalvarMapaRequest request = new SalvarMapaRequest();
        CompetenciaMapaDto compDto = new CompetenciaMapaDto();
        compDto.setDescricao("Desc");
        compDto.setAtividadesCodigos(new java.util.ArrayList<>(Set.of(2L))); // Atividade ID 2
        request.setCompetencias(List.of(compDto));

        Mapa mapa = new Mapa();
        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L); // O mapa só tem atividade ID 1
        ativ1.setMapa(mapa);

        when(mapaRepo.findById(codMapa)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(ativ1));
        when(competenciaRepo.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        assertThrows(ErroValidacao.class, () -> 
            mapaSalvamentoService.salvarMapaCompleto(codMapa, request));
    }

    @Test
    @DisplayName("Linhas 102-106, 216-217: Deve remover competência obsoleta e logar warn para gap")
    void deveRemoverObsoleta() {
        Long codMapa = 1L;
        SalvarMapaRequest request = new SalvarMapaRequest();
        request.setCompetencias(Collections.emptyList()); // Nenhuma competência na requisição

        Mapa mapa = new Mapa();
        Competencia compExistente = new Competencia("Existente", mapa);
        compExistente.setCodigo(100L);
        
        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L);
        ativ1.setMapa(mapa);
        ativ1.setCompetencias(new HashSet<>(List.of(compExistente)));

        when(mapaRepo.findById(codMapa)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(compExistente));
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(ativ1));
        when(competenciaRepo.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(mapaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapaCompletoMapper.toDto(any(), any(), any())).thenReturn(sgc.mapa.dto.MapaCompletoDto.builder().build());

        // Deve executar sem erros, cobrindo a remoção e os warns de integridade
        var result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }
}

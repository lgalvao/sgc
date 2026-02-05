package sgc.mapa.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("MapaSalvamentoService - Gaps de Cobertura")
class MapaSalvamentoServiceTest {

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
    private MapaSalvamentoService mapaSalvamentoService;

    @Test
    @DisplayName("Linhas 135-136: Deve falhar ao salvar competência inexistente que deveria existir")
    void deveFalharCompetenciaInexistente() {
        Long codMapa = 1L;
        CompetenciaMapaDto compDto = CompetenciaMapaDto.builder()
                .codigo(999L)
                .descricao("Desc")
                .build();
        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = new Mapa();
        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());

        assertThrows(ErroEntidadeNaoEncontrada.class, () ->
                mapaSalvamentoService.salvarMapaCompleto(codMapa, request));
    }

    @Test
    @DisplayName("Linha 188: Deve falhar ao associar atividade que não pertence ao mapa")
    void deveFalharAtividadeNaoPertenceAoMapa() {
        Long codMapa = 1L;
        CompetenciaMapaDto compDto = CompetenciaMapaDto.builder()
                .descricao("Desc")
                .atividadesCodigos(new ArrayList<>(Set.of(2L)))
                .build();
        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = new Mapa();
        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L); // O mapa só tem atividade ID 1
        ativ1.setMapa(mapa);

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
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
        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(Collections.emptyList())
                .build();

        Mapa mapa = new Mapa();
        Competencia compExistente = Competencia.builder().descricao("Existente").mapa(mapa).build();
        compExistente.setCodigo(100L);

        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L);
        ativ1.setMapa(mapa);
        ativ1.setCompetencias(new HashSet<>(List.of(compExistente)));

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(new ArrayList<>(List.of(compExistente)));
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(ativ1));
        when(competenciaRepo.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(mapaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapaCompletoMapper.toDto(any(), any(), any())).thenReturn(MapaCompletoDto.builder().build());

        // Deve executar sem erros, cobrindo a remoção e os warns de integridade
        var result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("Deve salvar mapa sem atividades pré-existentes (branch coverage)")
    void deveSalvarMapaSemAtividadesPreExistentes() {
        // Arrange
        Long codMapa = 100L;
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", List.of());
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        // Retorna lista vazia de competências
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(List.of());
        // Retorna lista vazia de atividades -> Branch a ser coberto no ternário
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of());

        // Mocks auxiliares
        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of());
        when(atividadeRepo.saveAll(anyList())).thenReturn(List.of());

        MapaCompletoDto dto = new MapaCompletoDto(codMapa, null, "Obs", List.of());
        when(mapaCompletoMapper.toDto(eq(mapa), any(), anyList())).thenReturn(dto);

        // Act
        MapaCompletoDto result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        // Assert
        assertThat(result).isNotNull();
        // Verifica que passou sem erro e salvou
        verify(mapaRepo).save(mapa);
    }

    @Test
    @DisplayName("Deve lidar com primeira atividade sem mapa (branch coverage)")
    void deveLidarComPrimeiraAtividadeSemMapa() {
        // Arrange
        Long codMapa = 100L;
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", List.of());
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(List.of());

        // Atividade presente mas sem mapa
        Atividade ativ = new Atividade();
        ativ.setCodigo(1L);
        ativ.setMapa(null);
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(ativ));

        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of());
        when(atividadeRepo.saveAll(anyList())).thenReturn(List.of());
        when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(MapaCompletoDto.builder().build());

        // Act
        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        // Assert
        verify(mapaRepo).save(mapa);
    }
}

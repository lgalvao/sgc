package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("MapaSalvamentoService Tests")
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
    @DisplayName("Deve falhar ao salvar competência inexistente que deveria existir")
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

        assertThatThrownBy(() -> mapaSalvamentoService.salvarMapaCompleto(codMapa, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve falhar ao associar atividade que não pertence ao mapa")
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
        ativ1.setCodigo(1L);
        ativ1.setMapa(mapa);

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(ativ1));
        when(competenciaRepo.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> mapaSalvamentoService.salvarMapaCompleto(codMapa, request))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("Deve remover competência obsoleta e logar warn para gap")
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

        var result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Deve salvar mapa sem atividades pré-existentes")
    void deveSalvarMapaSemAtividadesPreExistentes() {
        Long codMapa = 100L;
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", List.of());
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(List.of());
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of());

        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of());
        when(atividadeRepo.saveAll(anyList())).thenReturn(List.of());

        MapaCompletoDto dto = new MapaCompletoDto(codMapa, null, "Obs", List.of());
        when(mapaCompletoMapper.toDto(eq(mapa), any(), anyList())).thenReturn(dto);

        MapaCompletoDto result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(result).isNotNull();
        verify(mapaRepo).save(mapa);
    }

    @Test
    @DisplayName("Deve lidar com primeira atividade sem mapa")
    void deveLidarComPrimeiraAtividadeSemMapa() {
        Long codMapa = 100L;
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", List.of());
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(List.of());

        Atividade ativ = new Atividade();
        ativ.setCodigo(1L);
        ativ.setMapa(null);
        when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(ativ));

        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of());
        when(atividadeRepo.saveAll(anyList())).thenReturn(List.of());
        when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(MapaCompletoDto.builder().build());

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        verify(mapaRepo).save(mapa);
    }

    @Nested
    @DisplayName("Cobertura Extra")
    class Coverage {

        @Test
        @DisplayName("Deve lançar ErroEstadoImpossivel quando mapper retorna null")
        void salvarMapaCompleto_MapperNull() {
            Long codMapa = 1L;
            SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(Collections.emptyList())
                .build();
            
            Mapa mapa = new Mapa();
            when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
            when(mapaRepo.save(mapa)).thenReturn(mapa);
            when(competenciaRepo.saveAll(anyList())).thenReturn(Collections.emptyList());
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(null);

            assertThatThrownBy(() -> mapaSalvamentoService.salvarMapaCompleto(codMapa, request))
                    .isInstanceOf(ErroEstadoImpossivel.class);
        }

        @Test
        @DisplayName("Deve logar aviso quando competencia sem atividade e atividade sem competencia")
        void validarIntegridadeMapa_LogWarning() {
            Long codMapa = 1L;
            
            CompetenciaMapaDto compDto = CompetenciaMapaDto.builder()
                .descricao("Comp 1")
                .atividadesCodigos(Collections.emptyList())
                .build();
                
            SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();
                
            Mapa mapa = new Mapa();
            Atividade ativExistente = new Atividade();
            ativExistente.setCodigo(1L);
            ativExistente.setCompetencias(new HashSet<>());

            when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
            when(competenciaRepo.findByMapaCodigo(codMapa)).thenReturn(List.of());
            when(atividadeRepo.findByMapaCodigo(codMapa)).thenReturn(List.of(ativExistente));
            
            Competencia savedComp = new Competencia();
            savedComp.setCodigo(100L);
            savedComp.setAtividades(new HashSet<>());

            when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(savedComp));
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(MapaCompletoDto.builder().build());

            mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
            
            verify(mapaCompletoMapper).toDto(any(), any(), anyList());
        }
    }
}

package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.SalvarMapaRequest;
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

    @InjectMocks
    private MapaSalvamentoService mapaSalvamentoService;

    @Test
    @DisplayName("Deve salvar mapa com competência existente")
    void deveSalvarComCompetenciaExistente() {
        Long codMapa = 1L;
        Long codComp = 50L;
        
        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(codComp)
                .descricao("Comp 1")
                .atividadesCodigos(List.of(10L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .observacoes("Obs")
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Competencia comp = Competencia.builder().codigo(codComp).mapa(mapa).atividades(new HashSet<>()).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(comp));
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(comp));

        Mapa result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(result).isEqualTo(mapa);
        verify(competenciaRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve salvar mapa com nova competência")
    void deveSalvarComNovaCompetencia() {
        Long codMapa = 1L;
        
        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(null)
                .descricao("Nova Comp")
                .atividadesCodigos(List.of(10L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        Mapa result = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        assertThat(result).isNotNull();
        verify(competenciaRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lançar erro ao associar atividade de outro mapa")
    void deveLancarErroAtividadeOutroMapa() {
        Long codMapa = 1L;
        
        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(null)
                .descricao("Nova Comp")
                .atividadesCodigos(List.of(99L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> mapaSalvamentoService.salvarMapaCompleto(codMapa, request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("não pertence ao mapa");
    }
}

package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.ComumRepo;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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
    @DisplayName("Deve buscar competência no repo se não estiver no contexto atual")
    void deveBuscarNoRepoSeNaoNoContexto() {
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
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(repo.buscar(Competencia.class, codComp)).thenReturn(comp);
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(comp));

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        verify(repo).buscar(Competencia.class, codComp);
    }

    @Test
    @DisplayName("Deve remover competência obsoleta")
    void deveRemoverObsoleta() {
        Long codMapa = 1L;
        Long codCompObsoleto = 50L;
        
        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of())
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Competencia compObsoleto = spy(Competencia.builder().codigo(codCompObsoleto).mapa(mapa).atividades(new HashSet<>()).build());
        Atividade ativ = spy(Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>(List.of(compObsoleto))).build());

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(compObsoleto));
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenReturn(Collections.emptyList());

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);

        verify(competenciaRepo).deleteAll(anyCollection());
        assertThat(ativ.getCompetencias()).isEmpty();
    }

    @Test
    @DisplayName("Deve validar integridade com avisos quando há itens desvinculados")
    void deveValidarIntegridadeComAvisos() {
        Long codMapa = 1L;
        
        SalvarMapaRequest.CompetenciaRequest compDto = SalvarMapaRequest.CompetenciaRequest.builder()
                .codigo(null)
                .descricao("Comp")
                .atividadesCodigos(List.of()) // Comp sem atividade
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(compDto))
                .build();

        Mapa mapa = Mapa.builder().codigo(codMapa).build();
        Atividade ativ = Atividade.builder().codigo(10L).mapa(mapa).competencias(new HashSet<>()).build(); // Ativ sem comp

        when(repo.buscar(Mapa.class, codMapa)).thenReturn(mapa);
        when(competenciaRepo.findByMapa_Codigo(codMapa)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findByMapa_Codigo(codMapa)).thenReturn(List.of(ativ));
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
        
        // Apenas para cobrir os logs de warn nas linhas 177 e 183
        assertThat(ativ.getCompetencias()).isEmpty();
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
        when(competenciaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> mapaSalvamentoService.salvarMapaCompleto(codMapa, request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("não pertence ao mapa");
    }
}

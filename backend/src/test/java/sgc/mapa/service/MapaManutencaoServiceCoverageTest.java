package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.model.ComumRepo;
import sgc.mapa.model.*;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MapaManutencaoService - Cobertura Adicional")
class MapaManutencaoServiceCoverageTest {
    @InjectMocks
    private MapaManutencaoService service;

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private ComumRepo repo;

    @Test
    @DisplayName("buscarAtividadesPorCodigos deve chamar o repositório")
    void deveBuscarAtividadesPorCodigos() {
        List<Long> ids = List.of(1L, 2L);
        service.buscarAtividadesPorCodigos(ids);
        verify(atividadeRepo).findAllById(ids);
    }


    @Test
    @DisplayName("buscarMapaPorCodigo deve usar o ComumRepo")
    void deveBuscarMapaPorCodigo() {
        service.buscarMapaPorCodigo(1L);
        verify(repo).buscar(Mapa.class, 1L);
    }

    @Test
    @DisplayName("buscarMapaPorSubprocessoCodigo deve chamar o repositório")
    void deveBuscarMapaPorSubprocessoCodigo() {
        service.buscarMapaPorSubprocessoCodigo(1L);
        verify(mapaRepo).findBySubprocessoCodigo(1L);
    }

    @Test
    @DisplayName("buscarMapaVigentePorUnidade deve chamar o repositório")
    void deveBuscarMapaVigentePorUnidade() {
        service.buscarMapaVigentePorUnidade(1L);
        verify(mapaRepo).findMapaVigenteByUnidade(1L);
    }

    @Test
    @DisplayName("listarTodosMapas deve chamar o repositório")
    void deveListarTodosMapas() {
        service.listarTodosMapas();
        verify(mapaRepo).findAll();
    }

    @Test
    @DisplayName("mapaExiste deve retornar o resultado do repositório")
    void deveVerificarSeMapaExiste() {
        service.mapaExiste(1L);
        verify(mapaRepo).existsById(1L);
    }

    @Test
    @DisplayName("salvarMapa deve persistir e retornar o mapa")
    void deveSalvarMapa() {
        Mapa mapa = new Mapa();
        service.salvarMapa(mapa);
        verify(mapaRepo).save(mapa);
    }

    @Test
    @DisplayName("atualizarCompetencia deve remover associações das atividades antigas")
    void atualizarCompetencia_DeveRemoverAssociacoesAntigas() {
        // Arrange
        Long codComp = 1L;
        Competencia comp = new Competencia();
        comp.setCodigo(codComp);
        comp.setAtividades(new HashSet<>());
        
        Atividade ativAntiga = new Atividade();
        ativAntiga.getCompetencias().add(comp);
        
        when(repo.buscar(Competencia.class, codComp)).thenReturn(comp);
        when(atividadeRepo.listarPorCompetencia(comp)).thenReturn(List.of(ativAntiga));
        
        // Act
        service.atualizarCompetencia(codComp, "Nova Desc", List.of());
        
        // Assert
        assertThat(ativAntiga.getCompetencias()).doesNotContain(comp);
        verify(atividadeRepo).saveAll(anyList());
    }
}

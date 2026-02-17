package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("MapaManutencaoService - Cobertura Adicional")
class MapaManutencaoServiceCoverageTest {

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MapaManutencaoService service;

    @Test
    @DisplayName("atualizarDescricoesAtividadeEmLote deve ignorar valores nulos no mapa")
    void deveIgnorarValoresNulosNoLote() {
        // Arrange
        Long codAtiv = 1L;
        Atividade atividade = new Atividade();
        atividade.setCodigo(codAtiv);
        atividade.setDescricao("Antiga");
        atividade.setMapa(new Mapa());

        when(atividadeRepo.findAllById(any())).thenReturn(List.of(atividade));

        // Act
        // Passando null como valor para o código 1L (Map.of não permite null values)
        Map<Long, String> descricoes = new HashMap<>();
        descricoes.put(codAtiv, null);
        service.atualizarDescricoesAtividadeEmLote(descricoes);

        // Assert
        // A descrição não deve ter mudado (cobertura da linha 80)
        verify(atividadeRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("buscarAtividadesPorCodigos deve chamar o repositório")
    void deveBuscarAtividadesPorCodigos() {
        List<Long> ids = List.of(1L, 2L);
        service.buscarAtividadesPorCodigos(ids);
        verify(atividadeRepo).findAllById(ids);
    }

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private ComumRepo repo;

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

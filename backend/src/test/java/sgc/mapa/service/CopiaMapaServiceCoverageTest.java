package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para CopiaMapaService")
class CopiaMapaServiceCoverageTest {

    @InjectMocks
    private CopiaMapaService copiaMapaService;

    @Mock private MapaRepo repositorioMapa;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private CompetenciaRepo competenciaRepo;

    @Test
    @DisplayName("Deve lidar com mapa de origem sem atividades ou competências")
    void deveLidarComMapaVazio() {
        Long codOrigem = 1L;
        Mapa fonte = new Mapa();
        fonte.setCodigo(codOrigem);
        
        when(repositorioMapa.findById(codOrigem)).thenReturn(Optional.of(fonte));
        when(repositorioMapa.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));
        
        // Cobre line 84 (atividadesFonte.isEmpty())
        when(atividadeRepo.findByMapaCodigoWithConhecimentos(codOrigem)).thenReturn(List.of());
        // Cobre line 129 (competenciasFonte.isEmpty())
        when(competenciaRepo.findByMapaCodigo(codOrigem)).thenReturn(List.of());

        Mapa novo = copiaMapaService.copiarMapaParaUnidade(codOrigem);
        
        assertThat(novo).isNotNull();
        verify(atividadeRepo, never()).saveAll(anyList());
        verify(competenciaRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve importar atividades apenas se não vazias")
    void deveImportarAtividadesApenasSeNaoVazias() {
        Long origem = 1L;
        Long destino = 2L;
        
        when(atividadeRepo.findByMapaCodigoWithConhecimentos(origem)).thenReturn(List.of());
        when(atividadeRepo.findByMapaCodigo(destino)).thenReturn(List.of());
        when(repositorioMapa.findById(destino)).thenReturn(Optional.of(new Mapa()));

        copiaMapaService.importarAtividadesDeOutroMapa(origem, destino);
        
        verify(atividadeRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve ignorar atividade associada a competência se não estiver no mapa de atividades")
    void deveIgnorarAtividadeNaoEncontradaNaCopia() {
        Long codOrigem = 1L;
        Mapa fonte = new Mapa();
        fonte.setCodigo(codOrigem);
        
        when(repositorioMapa.findById(codOrigem)).thenReturn(Optional.of(fonte));
        when(repositorioMapa.save(any(Mapa.class))).thenAnswer(i -> i.getArgument(0));
        
        Atividade a1 = new Atividade();
        a1.setCodigo(10L);
        a1.setDescricao("A1");
        
        when(atividadeRepo.findByMapaCodigoWithConhecimentos(codOrigem)).thenReturn(List.of(a1));
        
        Competencia c1 = new Competencia();
        c1.setDescricao("C1");
        Atividade a2 = new Atividade(); // Atividade que NAO esta na lista original
        a2.setCodigo(99L);
        c1.setAtividades(Set.of(a2));
        
        when(competenciaRepo.findByMapaCodigo(codOrigem)).thenReturn(List.of(c1));

        copiaMapaService.copiarMapaParaUnidade(codOrigem);
        
        // Verifica que salvou a competencia mas sem atividades (pois a2 nao foi copiada)
        verify(competenciaRepo).saveAll(argThat(list -> {
            Competencia comp = list.iterator().next();
            return comp.getAtividades().isEmpty();
        }));
    }
}

package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.ComumRepo;
import sgc.mapa.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("CopiaMapaService - Cobertura Adicional")
class CopiaMapaServiceCoverageTest {

    @Mock
    private ComumRepo repo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;

    @InjectMocks
    private CopiaMapaService service;

    @Test
    @DisplayName("copiarMapaParaUnidade deve lidar com atividade não encontrada no mapa de destino")
    void deveLidarComAtividadeNaoEncontrada() {
        // Arrange
        Long codMapaOrigem = 1L;
        Mapa fonte = new Mapa();
        fonte.setCodigo(codMapaOrigem);
        
        when(repo.buscar(Mapa.class, codMapaOrigem)).thenReturn(fonte);
        when(mapaRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        Atividade ativFonte = new Atividade();
        ativFonte.setCodigo(100L);
        ativFonte.setDescricao("Atividade Teste");
        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(codMapaOrigem)).thenReturn(List.of(ativFonte));
        
        Competencia compFonte = new Competencia();
        compFonte.setCodigo(200L);
        compFonte.setDescricao("Competencia Teste");
        
        // Associada a uma atividade que NÃO existe no mapa de origem (ou ID errado no mock)
        Atividade ativFantasma = new Atividade();
        ativFantasma.setCodigo(999L);
        compFonte.setAtividades(Set.of(ativFantasma));
        
        when(competenciaRepo.findByMapa_Codigo(codMapaOrigem)).thenReturn(List.of(compFonte));

        // Act
        service.copiarMapaParaUnidade(codMapaOrigem);

        // Assert
        verify(competenciaRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("importarAtividadesDeOutroMapa deve importar atividades que não existem no destino")
    void deveImportarAtividadesNaoExistentes() {
        // Arrange
        Long mapaOrigemId = 1L;
        Long mapaDestinoId = 2L;
        
        Atividade ativOrigem = Atividade.builder()
                .descricao("Nova Atividade")
                .conhecimentos(new HashSet<>())
                .build();
        
        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaOrigemId)).thenReturn(List.of(ativOrigem));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of()); // Destino vazio
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());
        
        // Act
        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId);
        
        // Assert
        verify(atividadeRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("importarAtividadesDeOutroMapa não deve importar atividades com descrição já existente")
    void naoDeveImportarAtividadesExistentes() {
        // Arrange
        Long mapaOrigemId = 1L;
        Long mapaDestinoId = 2L;
        
        Atividade ativOrigem = Atividade.builder().descricao("Existente").build();
        Atividade ativDestino = Atividade.builder().descricao("Existente").build();
        
        when(atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaOrigemId)).thenReturn(List.of(ativOrigem));
        when(atividadeRepo.findByMapa_Codigo(mapaDestinoId)).thenReturn(List.of(ativDestino));
        when(repo.buscar(Mapa.class, mapaDestinoId)).thenReturn(new Mapa());
        
        // Act
        service.importarAtividadesDeOutroMapa(mapaOrigemId, mapaDestinoId);
        
        // Assert
        verify(atividadeRepo, never()).saveAll(anyList());
    }
}

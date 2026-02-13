package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        when(atividadeRepo.findWithConhecimentosByMapaCodigo(codMapaOrigem)).thenReturn(List.of(ativFonte));
        
        Competencia compFonte = new Competencia();
        compFonte.setCodigo(200L);
        compFonte.setDescricao("Competencia Teste");
        
        // Associada a uma atividade que NÃO existe no mapa de origem (ou ID errado no mock)
        Atividade ativFantasma = new Atividade();
        ativFantasma.setCodigo(999L);
        compFonte.setAtividades(Set.of(ativFantasma));
        
        when(competenciaRepo.findByMapaCodigo(codMapaOrigem)).thenReturn(List.of(compFonte));

        // Act
        service.copiarMapaParaUnidade(codMapaOrigem);

        // Assert
        // O branch if (novaAtividade != null) na linha 126 será false para a atividade 999
        verify(competenciaRepo).saveAll(anyList());
    }
}

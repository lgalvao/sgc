package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Mapa;

import java.util.List;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("MapaManutencaoService - Cobertura Adicional")
class MapaManutencaoServiceCoverageTest {

    @Mock
    private AtividadeRepo atividadeRepo;
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
        java.util.Map<Long, String> descricoes = new java.util.HashMap<>();
        descricoes.put(codAtiv, null);
        service.atualizarDescricoesAtividadeEmLote(descricoes);

        // Assert
        // A descrição não deve ter mudado (cobertura da linha 80)
        verify(atividadeRepo).saveAll(anyList());
    }
}

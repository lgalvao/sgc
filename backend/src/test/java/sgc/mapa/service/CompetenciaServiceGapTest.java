package sgc.mapa.service;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Gaps de Cobertura no CompetenciaService")
class CompetenciaServiceGapTest {
    @InjectMocks
    private CompetenciaService service;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private sgc.mapa.model.AtividadeRepo atividadeRepo;

    @Test
    @DisplayName("Deve salvar competência")
    void deveSalvar() {
        Competencia c = new Competencia();
        service.salvar(c);
        verify(competenciaRepo).save(c);
    }

    @Test
    @DisplayName("Deve retornar imediatamente se não houver atividades para associar")
    void deveRetornarSeListaAtividadesVazia() {
        service.criarCompetenciaComAtividades(new Mapa(), "desc", List.of());
        verify(competenciaRepo).save(any(Competencia.class));
    }
}

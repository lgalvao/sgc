package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ImpactoCompetenciaService")
class ImpactoCompetenciaServiceTest {

    @InjectMocks
    private ImpactoCompetenciaService impactoCompetenciaService;

    @Nested
    @DisplayName("Testes para identificação de competências impactadas")
    class CompetenciasImpactadasTestes {
        // TODO: Adicionar testes
    }
}
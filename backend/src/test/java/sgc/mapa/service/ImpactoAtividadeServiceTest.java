package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.AtividadeRepo;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ImpactoAtividadeService")
class ImpactoAtividadeServiceTest {
    @InjectMocks
    private ImpactoAtividadeService impactoAtividadeService;

    @Mock
    private AtividadeRepo atividadeRepo;
}
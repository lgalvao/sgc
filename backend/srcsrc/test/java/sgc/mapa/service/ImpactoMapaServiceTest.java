package sgc.mapa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.SgcTest;

import static org.junit.jupiter.api.Assertions.*;

@SgcTest
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ImpactoMapaService")
class ImpactoMapaServiceTest {

    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private ImpactoAtividadeService impactoAtividadeService;

    @Mock
    private ImpactoCompetenciaService impactoCompetenciaService;

    @Nested
    @DisplayName("Testes de verificação de acesso")
    class AcessoTestes {
        // TODO: Adicionar testes para verificar o acesso de cada perfil em cada situação
    }

    @Nested
    @DisplayName("Testes de detecção de impactos")
    class ImpactoTestes {
        // TODO: Adicionar testes para verificar a detecção de impactos
    }
}
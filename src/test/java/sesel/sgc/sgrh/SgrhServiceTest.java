package sesel.sgc.sgrh;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sesel.sgc.unidade.UnidadeRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class SgrhServiceTest {
    @MockitoBean
    private UnidadeRepository unidadeRepository;

    @MockitoSpyBean
    private SgrhService sgrhService;

    @Test
    void deveSincronizarDadosSemExcecao() {
        assertDoesNotThrow(() -> sgrhService.sincronizarDados());
    }
}
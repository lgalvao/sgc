package sgc.analise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliseServiceTest {

    @Mock
    private AnaliseRepo analiseRepo;

    @InjectMocks
    private AnaliseService analiseService;

    @Test
    @DisplayName("Deve listar análises por subprocesso ordenadas")
    void deveListarPorSubprocesso() {
        Long codSubprocesso = 1L;
        List<Analise> analises = Arrays.asList(new Analise(), new Analise());
        when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso)).thenReturn(analises);

        List<Analise> resultado = analiseService.listarPorSubprocesso(codSubprocesso);

        assertEquals(2, resultado.size());
        verify(analiseRepo).findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso);
    }

    @Test
    @DisplayName("Deve salvar análise")
    void deveSalvarAnalise() {
        Analise analise = new Analise();
        when(analiseRepo.save(analise)).thenReturn(analise);

        Analise resultado = analiseService.salvar(analise);

        assertNotNull(resultado);
        verify(analiseRepo).save(analise);
    }

    @Test
    @DisplayName("Deve remover análises por subprocesso quando existirem")
    void deveRemoverPorSubprocessoQuandoExistirem() {
        Long codSubprocesso = 1L;
        List<Analise> analises = Arrays.asList(new Analise(), new Analise());
        when(analiseRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(analises);

        analiseService.removerPorSubprocesso(codSubprocesso);

        verify(analiseRepo).findBySubprocessoCodigo(codSubprocesso);
        verify(analiseRepo).deleteAll(analises);
    }

    @Test
    @DisplayName("Não deve tentar remover análises se lista for vazia")
    void naoDeveRemoverQuandoNaoExistirem() {
        Long codSubprocesso = 1L;
        when(analiseRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Collections.emptyList());

        analiseService.removerPorSubprocesso(codSubprocesso);

        verify(analiseRepo).findBySubprocessoCodigo(codSubprocesso);
        verify(analiseRepo, never()).deleteAll(any());
    }
}

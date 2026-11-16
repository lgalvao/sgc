package sesel.sgc.unidade;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UnidadeServiceTest {
    @MockitoBean
    private UnidadeRepository unidadeRepository;

    @MockitoSpyBean
    private UnidadeService unidadeService;

    @Test
    void deveListarTodasUnidades() {
        Unidade unidade = new Unidade();
        when(unidadeRepository.findAll()).thenReturn(List.of(unidade));

        List<Unidade> resultado = unidadeService.listarTodas();
        assertEquals(1, resultado.size());
        verify(unidadeRepository).findAll();
    }

    @Test
    void deveBuscarUnidadePorId() {
        UUID id = UUID.randomUUID();
        Unidade unidade = new Unidade();
        when(unidadeRepository.findById(id)).thenReturn(Optional.of(unidade));

        Unidade resultado = unidadeService.buscarPorId(id);
        assertNotNull(resultado);
        verify(unidadeRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(unidadeRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> unidadeService.buscarPorId(id));
        verify(unidadeRepository).findById(id);
    }
}
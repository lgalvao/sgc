package sesel.sgc.servidor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServidorServiceTest {
    @Mock
    private ServidorRepository servidorRepository;

    @InjectMocks
    private ServidorService servidorService;

    @Test
    void deveListarServidoresPorUnidade() {
        UUID unidadeId = UUID.randomUUID();
        Servidor servidor = new Servidor();

        when(servidorRepository.findByUnidadeId(unidadeId)).thenReturn(List.of(servidor));

        List<Servidor> resultado = servidorService.listarPorUnidade(unidadeId);
        assertEquals(1, resultado.size());
        assertEquals(servidor, resultado.getFirst());
    }
}
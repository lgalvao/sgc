package sesel.sgc.servidor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServidorControllerTest {
    @Mock
    private ServidorService servidorService;

    @Mock
    private Model model;

    @InjectMocks
    private ServidorController servidorController;

    @Test
    void deveListarServidoresPorUnidadeEAdicionarNoModelo() {
        UUID unidadeId = UUID.randomUUID();
        List<Servidor> servidores = List.of(new Servidor());

        when(servidorService.listarPorUnidade(unidadeId)).thenReturn(servidores);
        String view = servidorController.listarPorUnidade(unidadeId, model);

        verify(model).addAttribute("servidores", servidores);

        assertEquals("servidor/servidor-lista", view);
    }
}
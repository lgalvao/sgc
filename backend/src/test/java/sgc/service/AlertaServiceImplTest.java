package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaServiceImpl;
import sgc.alerta.modelo.AlertaUsuario;
import sgc.alerta.modelo.AlertaUsuarioRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceImplTest {

    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;
    @Mock
    private sgc.alerta.modelo.AlertaRepo alertaRepo;
    @Mock
    private sgc.unidade.modelo.UnidadeRepo unidadeRepo;
    @Mock
    private sgc.sgrh.SgrhService sgrhService;

    @InjectMocks
    private AlertaServiceImpl alertaService;

    @Test
    void marcarComoLido_deveDefinirDataDeLeitura_quandoAlertaExiste() {
        // Arrange
        String usuarioTitulo = "testUser";
        Long alertaId = 1L;
        AlertaUsuario.Chave chave = new AlertaUsuario.Chave(alertaId, usuarioTitulo);
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        alertaUsuario.setId(chave);
        alertaUsuario.setDataHoraLeitura(null);

        when(alertaUsuarioRepo.findById(chave)).thenReturn(Optional.of(alertaUsuario));

        // Act
        alertaService.marcarComoLido(usuarioTitulo, alertaId);

        // Assert
        ArgumentCaptor<AlertaUsuario> captor = ArgumentCaptor.forClass(AlertaUsuario.class);
        verify(alertaUsuarioRepo).save(captor.capture());

        AlertaUsuario salvo = captor.getValue();
        assertNotNull(salvo.getDataHoraLeitura());
    }

    @Test
    void marcarComoLido_naoDeveFazerNada_quandoAlertaJaLido() {
        // Arrange
        String usuarioTitulo = "testUser";
        Long alertaId = 1L;
        AlertaUsuario.Chave chave = new AlertaUsuario.Chave(alertaId, usuarioTitulo);
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        alertaUsuario.setId(chave);
        alertaUsuario.setDataHoraLeitura(LocalDateTime.now().minusDays(1));

        when(alertaUsuarioRepo.findById(chave)).thenReturn(Optional.of(alertaUsuario));

        // Act
        alertaService.marcarComoLido(usuarioTitulo, alertaId);

        // Assert
        verify(alertaUsuarioRepo, never()).save(any(AlertaUsuario.class));
    }

    @Test
    void marcarComoLido_deveLancarExcecao_quandoAlertaNaoEncontrado() {
        // Arrange
        String usuarioTitulo = "testUser";
        Long alertaId = 1L;
        AlertaUsuario.Chave chave = new AlertaUsuario.Chave(alertaId, usuarioTitulo);

        when(alertaUsuarioRepo.findById(chave)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> {
            alertaService.marcarComoLido(usuarioTitulo, alertaId);
        });
    }
}
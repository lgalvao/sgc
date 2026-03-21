package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.model.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaService Test")
class AlertaServiceTest {

    @Mock
    private AlertaRepo alertaRepo;

    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    @DisplayName("Deve buscar alertas para Servidor (apenas individuais)")
    void deveListarParaServidor() {
        String usuarioTitulo = "123";
        List<Alerta> alertas = Collections.singletonList(new Alerta());
        when(alertaRepo.buscarAlertasExclusivosDoUsuario(usuarioTitulo)).thenReturn(alertas);

        List<Alerta> resultado = alertaService.listarParaServidor(usuarioTitulo);

        assertThat(resultado).hasSize(1);
        verify(alertaRepo).buscarAlertasExclusivosDoUsuario(usuarioTitulo);
        verify(alertaRepo, never()).buscarAlertasDaUnidadeEIndividuais(anyLong(), anyString());
    }

    @Test
    @DisplayName("Deve buscar alertas para Gestão (unidade + individuais)")
    void deveListarParaGestao() {
        Long codigoUnidade = 1L;
        String usuarioTitulo = "123";
        List<Alerta> alertas = Collections.singletonList(new Alerta());
        when(alertaRepo.buscarAlertasDaUnidadeEIndividuais(codigoUnidade, usuarioTitulo)).thenReturn(alertas);

        List<Alerta> resultado = alertaService.listarParaGestao(codigoUnidade, usuarioTitulo);

        assertThat(resultado).hasSize(1);
        verify(alertaRepo).buscarAlertasDaUnidadeEIndividuais(codigoUnidade, usuarioTitulo);
        verify(alertaRepo, never()).buscarAlertasExclusivosDoUsuario(anyString());
    }

    @Test
    @DisplayName("Deve buscar alerta por código")
    void devePorCodigo() {
        when(alertaRepo.findById(1L)).thenReturn(Optional.of(new Alerta()));
        assertThat(alertaService.porCodigo(1L)).isPresent();
    }

    @Test
    @DisplayName("Deve obter data/hora leitura")
    void deveObterDataHoraLeitura() {
        AlertaUsuario au = new AlertaUsuario();
        LocalDateTime agora = LocalDateTime.now();
        au.setDataHoraLeitura(agora);

        when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(au));

        Optional<LocalDateTime> data = alertaService.dataHoraLeituraAlertaUsuario(1L, "123");

        assertThat(data).isPresent();
        assertThat(data).contains(agora);
    }
}

package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import sgc.alerta.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaService Test")
@SuppressWarnings("NullAway.Init")
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

    @Test
    @DisplayName("Deve buscar vínculos de leitura de alertas do usuário")
    void deveBuscarAlertasUsuarios() {
        List<Long> codigosAlertas = List.of(10L, 20L);
        List<AlertaUsuario> alertasUsuarios = List.of(new AlertaUsuario());
        when(alertaUsuarioRepo.findByUsuarioAndAlertas("123", codigosAlertas)).thenReturn(alertasUsuarios);

        List<AlertaUsuario> resultado = alertaService.alertasUsuarios("123", codigosAlertas);

        assertThat(resultado).hasSize(1);
        verify(alertaUsuarioRepo).findByUsuarioAndAlertas("123", codigosAlertas);
    }

    @Test
    @DisplayName("Deve listar alertas paginados para servidor")
    void deveListarParaServidorPaginado() {
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Alerta> pagina = new PageImpl<>(List.of(new Alerta()));
        when(alertaRepo.buscarAlertasExclusivosDoUsuario("123", paginacao)).thenReturn(pagina);

        Page<Alerta> resultado = alertaService.listarParaServidorPaginado("123", paginacao);

        assertThat(resultado.getContent()).hasSize(1);
        verify(alertaRepo).buscarAlertasExclusivosDoUsuario("123", paginacao);
    }

    @Test
    @DisplayName("Deve listar alertas paginados para gestão")
    void deveListarParaGestaoPaginado() {
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Alerta> pagina = new PageImpl<>(List.of(new Alerta()));
        when(alertaRepo.buscarAlertasDaUnidadeEIndividuais(1L, "123", paginacao)).thenReturn(pagina);

        Page<Alerta> resultado = alertaService.listarParaGestaoPaginado(1L, "123", paginacao);

        assertThat(resultado.getContent()).hasSize(1);
        verify(alertaRepo).buscarAlertasDaUnidadeEIndividuais(1L, "123", paginacao);
    }
}

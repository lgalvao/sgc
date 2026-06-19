package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        verify(alertaRepo, never()).buscarAlertasDaGestao(anyLong(), anyString());
    }

    @Test
    @DisplayName("Deve buscar alertas para Gestão (pessoais e coletivos da unidade)")
    void deveListarParaGestao() {
        Long codigoUnidade = 1L;
        String usuarioTitulo = "123";
        List<Alerta> alertas = Collections.singletonList(new Alerta());
        when(alertaRepo.buscarAlertasDaGestao(codigoUnidade, usuarioTitulo)).thenReturn(alertas);

        List<Alerta> resultado = alertaService.listarParaGestao(codigoUnidade, usuarioTitulo);

        assertThat(resultado).hasSize(1);
        verify(alertaRepo).buscarAlertasDaGestao(codigoUnidade, usuarioTitulo);
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

        assertThat(data)
                .isPresent()
                .contains(agora);
    }

    @Test
    @DisplayName("Deve buscar vínculos de leitura de alertas do usuário")
    void deveBuscarAlertasUsuarios() {
        List<Long> codigosAlertas = List.of(10L, 20L);
        List<AlertaUsuario> alertasUsuarios = List.of(new AlertaUsuario());
        when(alertaUsuarioRepo.listarPorUsuarioEAlertas("123", codigosAlertas)).thenReturn(alertasUsuarios);

        List<AlertaUsuario> resultado = alertaService.alertasUsuarios("123", codigosAlertas);

        assertThat(resultado).hasSize(1);
        verify(alertaUsuarioRepo).listarPorUsuarioEAlertas("123", codigosAlertas);
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
        String usuarioTitulo = "123";
        Page<Alerta> pagina = new PageImpl<>(List.of(new Alerta()));
        when(alertaRepo.buscarAlertasDaGestao(1L, usuarioTitulo, paginacao)).thenReturn(pagina);

        Page<Alerta> resultado = alertaService.listarParaGestaoPaginado(1L, usuarioTitulo, paginacao);

        assertThat(resultado.getContent()).hasSize(1);
        verify(alertaRepo).buscarAlertasDaGestao(1L, usuarioTitulo, paginacao);
    }

    @Test
    @DisplayName("Deve salvar um alerta")
    void deveSalvarAlerta() {
        Alerta alerta = new Alerta();
        when(alertaRepo.save(alerta)).thenReturn(alerta);

        Alerta resultado = alertaService.salvar(alerta);

        assertThat(resultado).isNotNull();
        verify(alertaRepo).save(alerta);
    }

    @Test
    @DisplayName("Deve salvar lista de alertas")
    void deveSalvarTodos() {
        List<Alerta> alertas = List.of(new Alerta(), new Alerta());
        when(alertaRepo.saveAll(alertas)).thenReturn(alertas);

        List<Alerta> resultado = alertaService.salvarTodos(alertas);

        assertThat(resultado).hasSize(2);
        verify(alertaRepo).saveAll(alertas);
    }

    @Test
    @DisplayName("Deve buscar alertaUsuario")
    void deveBuscarAlertaUsuario() {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                .alertaCodigo(1L)
                .usuarioTitulo("123")
                .build();
        AlertaUsuario au = new AlertaUsuario();
        when(alertaUsuarioRepo.findById(chave)).thenReturn(Optional.of(au));

        Optional<AlertaUsuario> resultado = alertaService.alertaUsuario(chave);

        assertThat(resultado).isPresent();
        verify(alertaUsuarioRepo).findById(chave);
    }

    @Test
    @DisplayName("Deve listar alertas por codigos")
    void deveListarPorCodigos() {
        List<Long> codigos = List.of(1L, 2L);
        List<Alerta> alertas = List.of(new Alerta(), new Alerta());
        when(alertaRepo.findAllById(codigos)).thenReturn(alertas);

        List<Alerta> resultado = alertaService.listarPorCodigos(codigos);

        assertThat(resultado).hasSize(2);
        verify(alertaRepo).findAllById(codigos);
    }

    @Test
    @DisplayName("Deve salvar alertaUsuario")
    void deveSalvarAlertaUsuario() {
        AlertaUsuario au = new AlertaUsuario();

        alertaService.salvarAlertaUsuario(au);

        verify(alertaUsuarioRepo).save(au);
    }

    @Test
    @DisplayName("Deve salvar lista de alertasUsuarios")
    void deveSalvarAlertasUsuarios() {
        List<AlertaUsuario> aus = List.of(new AlertaUsuario(), new AlertaUsuario());
        when(alertaUsuarioRepo.saveAll(aus)).thenReturn(aus);

        List<AlertaUsuario> resultado = alertaService.salvarAlertasUsuarios(aus);

        assertThat(resultado).hasSize(2);
        verify(alertaUsuarioRepo).saveAll(aus);
    }
}

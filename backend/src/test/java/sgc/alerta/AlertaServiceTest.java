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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepo alertaRepo;

    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    @DisplayName("Deve salvar alerta com sucesso")
    void deveSalvarAlerta() {
        Alerta alerta = new Alerta();
        when(alertaRepo.save(alerta)).thenReturn(alerta);

        Alerta resultado = alertaService.salvar(alerta);

        assertNotNull(resultado);
        verify(alertaRepo).save(alerta);
    }

    @Test
    @DisplayName("Deve buscar alertas por unidade de destino")
    void deveBuscarPorUnidadeDestino() {
        Long codigoUnidade = 1L;
        List<Alerta> alertas = Collections.singletonList(new Alerta());
        when(alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade)).thenReturn(alertas);

        List<Alerta> resultado = alertaService.buscarPorUnidadeDestino(codigoUnidade);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(alertaRepo).findByUnidadeDestino_Codigo(codigoUnidade);
    }

    @Test
    @DisplayName("Deve buscar alertas por unidade de destino com paginação")
    void deveBuscarPorUnidadeDestinoComPaginacao() {
        Long codigoUnidade = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Alerta> page = new PageImpl<>(Collections.singletonList(new Alerta()));
        when(alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade, pageable)).thenReturn(page);

        Page<Alerta> resultado = alertaService.buscarPorUnidadeDestino(codigoUnidade, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(alertaRepo).findByUnidadeDestino_Codigo(codigoUnidade, pageable);
    }

    @Test
    @DisplayName("Deve buscar alerta por código")
    void deveBuscarPorCodigo() {
        Long codigo = 1L;
        Alerta alerta = new Alerta();
        when(alertaRepo.findById(codigo)).thenReturn(Optional.of(alerta));

        Optional<Alerta> resultado = alertaService.buscarPorCodigo(codigo);

        assertTrue(resultado.isPresent());
        verify(alertaRepo).findById(codigo);
    }

    @Test
    @DisplayName("Deve buscar AlertaUsuario por chave")
    void deveBuscarAlertaUsuario() {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder().alertaCodigo(1L).usuarioTitulo("user").build();
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        when(alertaUsuarioRepo.findById(chave)).thenReturn(Optional.of(alertaUsuario));

        Optional<AlertaUsuario> resultado = alertaService.buscarAlertaUsuario(chave);

        assertTrue(resultado.isPresent());
        verify(alertaUsuarioRepo).findById(chave);
    }

    @Test
    @DisplayName("Deve salvar AlertaUsuario")
    void deveSalvarAlertaUsuario() {
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        when(alertaUsuarioRepo.save(alertaUsuario)).thenReturn(alertaUsuario);

        AlertaUsuario resultado = alertaService.salvarAlertaUsuario(alertaUsuario);

        assertNotNull(resultado);
        verify(alertaUsuarioRepo).save(alertaUsuario);
    }

    @Test
    @DisplayName("Deve buscar AlertaUsuario por usuário e lista de alertas")
    void deveBuscarPorUsuarioEAlertas() {
        String usuario = "user";
        List<Long> codigos = List.of(1L, 2L);
        List<AlertaUsuario> lista = Collections.singletonList(new AlertaUsuario());
        when(alertaUsuarioRepo.findByUsuarioAndAlertas(usuario, codigos)).thenReturn(lista);

        List<AlertaUsuario> resultado = alertaService.buscarPorUsuarioEAlertas(usuario, codigos);

        assertFalse(resultado.isEmpty());
        verify(alertaUsuarioRepo).findByUsuarioAndAlertas(usuario, codigos);
    }

    @Test
    @DisplayName("Deve obter data hora de leitura")
    void deveObterDataHoraLeitura() {
        Long codigoAlerta = 1L;
        String usuario = "user";
        LocalDateTime now = LocalDateTime.now();
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        alertaUsuario.setDataHoraLeitura(now);
        
        when(alertaUsuarioRepo.findById(any(AlertaUsuario.Chave.class))).thenReturn(Optional.of(alertaUsuario));

        Optional<LocalDateTime> resultado = alertaService.obterDataHoraLeitura(codigoAlerta, usuario);

        assertTrue(resultado.isPresent());
        assertEquals(now, resultado.get());
    }
}

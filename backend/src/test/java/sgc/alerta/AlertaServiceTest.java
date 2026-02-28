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

        assertThat(resultado).isNotNull();
        verify(alertaRepo).save(alerta);
    }

    @Test
    @DisplayName("Deve buscar alertas por unidade de destino")
    void devePorUnidadeDestinoPaginado() {
        Long codigoUnidade = 1L;
        List<Alerta> alertas = Collections.singletonList(new Alerta());
        when(alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade)).thenReturn(alertas);

        List<Alerta> resultado = alertaService.porUnidadeDestino(codigoUnidade);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        verify(alertaRepo).findByUnidadeDestino_Codigo(codigoUnidade);
    }

    @Test
    @DisplayName("Deve buscar alertas por unidade de destino com paginação")
    void devePorUnidadeDestinoPaginadoComPaginacao() {
        Long codigoUnidade = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Alerta> page = new PageImpl<>(Collections.singletonList(new Alerta()));
        when(alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade, pageable)).thenReturn(page);

        Page<Alerta> resultado = alertaService.porUnidadeDestinoPaginado(codigoUnidade, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        verify(alertaRepo).findByUnidadeDestino_Codigo(codigoUnidade, pageable);
    }

    @Test
    @DisplayName("Deve buscar alerta por código")
    void devePorCodigo() {
        Long codigo = 1L;
        Alerta alerta = new Alerta();
        when(alertaRepo.findById(codigo)).thenReturn(Optional.of(alerta));

        Optional<Alerta> resultado = alertaService.porCodigo(codigo);

        assertThat(resultado).isPresent();
        verify(alertaRepo).findById(codigo);
    }

    @Test
    @DisplayName("Deve buscar AlertaUsuario por chave")
    void deveAlertaUsuario() {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder().alertaCodigo(1L).usuarioTitulo("user").build();
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        when(alertaUsuarioRepo.findById(chave)).thenReturn(Optional.of(alertaUsuario));

        Optional<AlertaUsuario> resultado = alertaService.alertaUsuario(chave);

        assertThat(resultado).isPresent();
        verify(alertaUsuarioRepo).findById(chave);
    }

    @Test
    @DisplayName("Deve salvar AlertaUsuario")
    void deveSalvarAlertaUsuario() {
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        when(alertaUsuarioRepo.save(alertaUsuario)).thenReturn(alertaUsuario);

        AlertaUsuario resultado = alertaService.salvarAlertaUsuario(alertaUsuario);

        assertThat(resultado).isNotNull();
        verify(alertaUsuarioRepo).save(alertaUsuario);
    }

    @Test
    @DisplayName("Deve buscar AlertaUsuario por usuário e lista de alertas")
    void deveAlertasUsuarios() {
        String usuario = "user";
        List<Long> codigos = List.of(1L, 2L);
        List<AlertaUsuario> lista = Collections.singletonList(new AlertaUsuario());
        when(alertaUsuarioRepo.findByUsuarioAndAlertas(usuario, codigos)).thenReturn(lista);

        List<AlertaUsuario> resultado = alertaService.alertasUsuarios(usuario, codigos);

        assertThat(resultado).isNotEmpty();
        verify(alertaUsuarioRepo).findByUsuarioAndAlertas(usuario, codigos);
    }

    @Test
    @DisplayName("Deve obter data hora de leitura")
    void deveDataHoraLeituraAlertaUsuario() {
        Long codigoAlerta = 1L;
        String usuario = "user";
        LocalDateTime now = LocalDateTime.now();
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        alertaUsuario.setDataHoraLeitura(now);

        when(alertaUsuarioRepo.findById(any(AlertaUsuario.Chave.class))).thenReturn(Optional.of(alertaUsuario));

        Optional<LocalDateTime> resultado = alertaService.dataHoraLeituraAlertaUsuario(codigoAlerta, usuario);

        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isEqualTo(now);
    }
}
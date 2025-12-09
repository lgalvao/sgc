package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.dto.AlertaMapper;
import sgc.alerta.erros.ErroAlerta;
import sgc.alerta.model.*;
import sgc.processo.model.Processo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.service.SgrhService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock private AlertaRepo alertaRepo;
    @Mock private AlertaUsuarioRepo alertaUsuarioRepo;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private SgrhService sgrhService;
    @Mock private UsuarioRepo usuarioRepo;
    @Mock private AlertaMapper alertaMapper;

    @InjectMocks private AlertaService service;

    @Test
    @DisplayName("criarAlerta sucesso")
    void criarAlerta() {
        Processo p = new Processo();
        Long unidadeId = 1L;

        Unidade u = new Unidade();
        u.setCodigo(unidadeId);

        ResponsavelDto resp = new ResponsavelDto();
        resp.setTitularTitulo("123");
        resp.setSubstitutoTitulo("456");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(u));
        when(alertaRepo.save(any())).thenReturn(new Alerta().setCodigo(100L));
        when(sgrhService.buscarResponsavelUnidade(unidadeId)).thenReturn(Optional.of(resp));
        when(usuarioRepo.findById("123")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.findById("456")).thenReturn(Optional.of(new Usuario()));

        service.criarAlerta(
                p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc", LocalDateTime.now());

        verify(alertaUsuarioRepo, times(2)).save(any());
    }

    @Test
    @DisplayName("criarAlerta cria usuario se nao existir")
    void criarAlertaCriaUsuario() {
        Processo p = new Processo();
        Long unidadeId = 1L;

        Unidade u = new Unidade();
        u.setCodigo(unidadeId);

        ResponsavelDto resp = new ResponsavelDto();
        resp.setTitularTitulo("123");

        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(u));
        when(alertaRepo.save(any())).thenReturn(new Alerta().setCodigo(100L));
        when(sgrhService.buscarResponsavelUnidade(unidadeId)).thenReturn(Optional.of(resp));

        when(usuarioRepo.findById("123")).thenReturn(Optional.empty());

        sgc.sgrh.dto.UsuarioDto userDto = new sgc.sgrh.dto.UsuarioDto();
        userDto.setTitulo("123");
        userDto.setNome("Nome");
        userDto.setEmail("email");

        when(sgrhService.buscarUsuarioPorTitulo("123")).thenReturn(Optional.of(userDto));

        service.criarAlerta(
                p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc", LocalDateTime.now());

        verify(alertaUsuarioRepo).save(any());
    }

    @Test
    @DisplayName("criarAlerta ignora erro ao buscar responsavel")
    void criarAlertaIgnoraErro() {
        Processo p = new Processo();
        Long unidadeId = 1L;
        Unidade u = new Unidade();
        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(u));
        when(alertaRepo.save(any())).thenReturn(new Alerta());
        when(sgrhService.buscarResponsavelUnidade(unidadeId))
                .thenThrow(new RuntimeException("Erro"));

        service.criarAlerta(p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc", null);

        verify(alertaUsuarioRepo, never()).save(any());
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado operacional")
    void criarAlertasProcessoIniciadoOperacional() {
        Processo p = new Processo();
        p.setDescricao("Proc");
        Long unidadeId = 1L;

        UnidadeDto uDto = new UnidadeDto();
        uDto.setTipo(TipoUnidade.OPERACIONAL.name());

        when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

        verify(alertaRepo).save(argThat(a -> a.getDescricao().contains("Preencha as atividades")));
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado intermediaria")
    void criarAlertasProcessoIniciadoIntermediaria() {
        Processo p = new Processo();
        p.setDescricao("Proc");
        Long unidadeId = 1L;

        UnidadeDto uDto = new UnidadeDto();
        uDto.setTipo(TipoUnidade.INTERMEDIARIA.name());

        when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

        verify(alertaRepo)
                .save(argThat(a -> a.getDescricao().contains("Aguarde a disponibilização")));
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado interoperacional")
    void criarAlertasProcessoIniciadoInteroperacional() {
        Processo p = new Processo();
        p.setDescricao("Proc");
        Long unidadeId = 1L;

        UnidadeDto uDto = new UnidadeDto();
        uDto.setTipo(TipoUnidade.INTEROPERACIONAL.name());

        when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

        // Should save 2 alerts
        verify(alertaRepo, times(2)).save(any());
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado falha lança ErroAlerta")
    void criarAlertasProcessoIniciadoFalha() {
        Processo p = new Processo();
        Long unidadeId = 1L;

        when(sgrhService.buscarUnidadePorCodigo(unidadeId))
                .thenThrow(new RuntimeException("Erro SGRH"));

        assertThatThrownBy(
                        () ->
                                service.criarAlertasProcessoIniciado(
                                        p, List.of(unidadeId), List.of()))
                .isInstanceOf(ErroAlerta.class);
    }

    @Test
    @DisplayName("criarAlertaUsuario captura excecao e loga")
    void criarAlertaUsuarioExcecao() {
        Processo p = new Processo();
        Long unidadeId = 1L;
        Unidade u = new Unidade();
        u.setCodigo(unidadeId);
        ResponsavelDto resp = new ResponsavelDto();
        resp.setTitularTitulo("123");

        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(u));
        when(alertaRepo.save(any())).thenReturn(new Alerta());
        when(sgrhService.buscarResponsavelUnidade(unidadeId)).thenReturn(Optional.of(resp));
        when(usuarioRepo.findById("123")).thenReturn(Optional.of(new Usuario()));

        doThrow(new RuntimeException("DB Error")).when(alertaUsuarioRepo).save(any());

        // Should not throw exception
        service.criarAlerta(p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc", null);

        verify(alertaUsuarioRepo).save(any());
    }

    @Test
    @DisplayName("criarAlertaCadastroDisponibilizado sucesso")
    void criarAlertaCadastroDisponibilizado() {
        Processo p = new Processo();
        p.setDescricao("P");
        Long origem = 1L;
        Long destino = 2L;
        Unidade uOrigem = new Unidade();
        uOrigem.setSigla("UO");

        when(unidadeRepo.findById(origem)).thenReturn(Optional.of(uOrigem));
        when(unidadeRepo.findById(destino)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertaCadastroDisponibilizado(p, origem, destino);

        verify(alertaRepo).save(any());
    }

    @Test
    @DisplayName("criarAlertaCadastroDevolvido sucesso")
    void criarAlertaCadastroDevolvido() {
        Processo p = new Processo();
        p.setDescricao("P");
        Long destino = 1L;

        when(unidadeRepo.findById(destino)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertaCadastroDevolvido(p, destino, "motivo");

        verify(alertaRepo).save(any());
    }

    @Test
    @DisplayName("marcarComoLido sucesso")
    void marcarComoLido() {
        String user = "123";
        Long alertaId = 10L;
        AlertaUsuario au = new AlertaUsuario();
        au.setDataHoraLeitura(null);

        when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(au));

        service.marcarComoLido(user, alertaId);

        assertThat(au.getDataHoraLeitura()).isNotNull();
        verify(alertaUsuarioRepo).save(au);
    }

    @Test
    @DisplayName("listarAlertasPorUsuario sucesso")
    void listarAlertasPorUsuario() {
        String user = "123";
        Alerta alerta = new Alerta();
        AlertaUsuario au = new AlertaUsuario();
        au.setAlerta(alerta);

        when(alertaUsuarioRepo.findById_UsuarioTituloEleitoral(user)).thenReturn(List.of(au));
        when(alertaMapper.toDto(alerta)).thenReturn(AlertaDto.builder().build());

        var res = service.listarAlertasPorUsuario(user);

        assertThat(res).hasSize(1);
    }
}

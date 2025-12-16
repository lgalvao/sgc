package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.dto.AlertaMapper;
import sgc.alerta.model.*;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.Processo;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.SgrhService;
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

    @Mock
    private AlertaRepo alertaRepo;
    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private SgrhService sgrhService;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private AlertaMapper alertaMapper;

    @InjectMocks
    private AlertaService service;

    @Test
    @DisplayName("criarAlerta sucesso")
    void criarAlerta() {
        Processo p = new Processo();
        Long unidadeId = 1L;
        Unidade u = new Unidade();
        u.setCodigo(unidadeId);

        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(u));
        when(alertaRepo.save(any())).thenReturn(new Alerta().setCodigo(100L));

        Alerta resultado = service.criarAlerta(
                p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc");

        assertThat(resultado).isNotNull();
        verify(alertaRepo).save(any());
    }

    @Test
    @DisplayName("criarAlerta lança erro se unidade não existe")
    void criarAlertaUnidadeNaoEncontrada() {
        Processo p = new Processo();
        Long unidadeId = 999L;

        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.criarAlerta(p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado operacional - texto fixo")
    void criarAlertasProcessoIniciadoOperacional() {
        Processo p = new Processo();
        p.setDescricao("Proc");
        Long unidadeId = 1L;

        UnidadeDto uDto = UnidadeDto.builder().tipo(TipoUnidade.OPERACIONAL.name()).build();

        when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

        // Texto fixo conforme CDU-04/05
        verify(alertaRepo).save(argThat(a -> "Início do processo".equals(a.getDescricao())));
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado intermediaria - texto fixo")
    void criarAlertasProcessoIniciadoIntermediaria() {
        Processo p = new Processo();
        p.setDescricao("Proc");
        Long unidadeId = 1L;

        UnidadeDto uDto = UnidadeDto.builder().tipo(TipoUnidade.INTERMEDIARIA.name()).build();

        when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

        verify(alertaRepo).save(argThat(a ->
                "Início do processo em unidade(s) subordinada(s)".equals(a.getDescricao())));
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado interoperacional cria 2 alertas")
    void criarAlertasProcessoIniciadoInteroperacional() {
        Processo p = new Processo();
        p.setDescricao("Proc");
        Long unidadeId = 1L;

        UnidadeDto uDto = UnidadeDto.builder().tipo(TipoUnidade.INTEROPERACIONAL.name()).build();

        when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        List<Alerta> resultado = service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

        // Interoperacional recebe 2 alertas
        assertThat(resultado).hasSize(2);
        verify(alertaRepo, times(2)).save(any());
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
    @DisplayName("listarAlertasPorUsuario com lazy creation de AlertaUsuario")
    void listarAlertasPorUsuarioComLazyCreation() {
        String usuarioTitulo = "123";
        Long codUnidade = 1L;

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioTitulo);
        usuario.setUnidadeLotacao(unidade);

        Alerta alerta = new Alerta();
        alerta.setCodigo(100L);

        AlertaUsuario alertaUsuarioCriado = new AlertaUsuario();
        alertaUsuarioCriado.setAlerta(alerta);
        alertaUsuarioCriado.setDataHoraLeitura(null);

        when(usuarioRepo.findById(usuarioTitulo)).thenReturn(Optional.of(usuario));
        when(alertaRepo.findByUnidadeDestino_Codigo(codUnidade)).thenReturn(List.of(alerta));
        when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.empty()); // Não existe ainda
        when(alertaUsuarioRepo.save(any())).thenReturn(alertaUsuarioCriado);
        when(alertaMapper.toDto(alerta)).thenReturn(AlertaDto.builder().codigo(100L).build());

        List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

        assertThat(resultado).hasSize(1);
        // Verifica que AlertaUsuario foi criado (lazy creation)
        verify(alertaUsuarioRepo).save(any());
    }

    @Test
    @DisplayName("listarAlertasPorUsuario retorna existente sem criar novo")
    void listarAlertasPorUsuarioSemCriarNovo() {
        String usuarioTitulo = "123";
        Long codUnidade = 1L;

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioTitulo);
        usuario.setUnidadeLotacao(unidade);

        Alerta alerta = new Alerta();
        alerta.setCodigo(100L);

        AlertaUsuario alertaUsuarioExistente = new AlertaUsuario();
        alertaUsuarioExistente.setAlerta(alerta);
        alertaUsuarioExistente.setDataHoraLeitura(LocalDateTime.now());

        when(usuarioRepo.findById(usuarioTitulo)).thenReturn(Optional.of(usuario));
        when(alertaRepo.findByUnidadeDestino_Codigo(codUnidade)).thenReturn(List.of(alerta));
        when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(alertaUsuarioExistente));
        when(alertaMapper.toDto(alerta)).thenReturn(AlertaDto.builder().codigo(100L).build());

        List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

        assertThat(resultado).hasSize(1);
        // Não deve criar novo AlertaUsuario
        verify(alertaUsuarioRepo, never()).save(any());
    }
}

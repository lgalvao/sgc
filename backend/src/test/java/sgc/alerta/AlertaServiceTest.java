package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.api.AlertaDto;
import sgc.alerta.internal.AlertaMapper;
import sgc.alerta.internal.model.*;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.api.model.Processo;
import sgc.sgrh.api.UnidadeDto;
import sgc.sgrh.api.model.Usuario;
import sgc.sgrh.api.model.UsuarioRepo;
import sgc.sgrh.SgrhService;
import sgc.unidade.api.model.TipoUnidade;
import sgc.unidade.api.model.Unidade;
import sgc.unidade.api.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: AlertaService")
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

    @Nested
    @DisplayName("Método: criarAlerta")
    class CriarAlerta {
        @Test
        @DisplayName("Deve criar alerta com sucesso")
        void deveCriarAlertaComSucesso() {
            // Given
            Processo p = new Processo();
            p.setCodigo(1L);
            Long unidadeId = 1L;
            Unidade u = new Unidade();
            u.setCodigo(unidadeId);

            when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(u));
            when(alertaRepo.save(any())).thenReturn(new Alerta().setCodigo(100L));

            // When
            Alerta resultado = service.criarAlerta(
                    p.getCodigo(), TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc");

            // Then
            assertThat(resultado).isNotNull();
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro se unidade não existe")
        void deveLancarErroSeUnidadeNaoExiste() {
            // Given
            Processo p = new Processo();
            p.setCodigo(1L);
            Long unidadeId = 999L;

            when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() ->
                    service.criarAlerta(p.getCodigo(), TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Método: criarAlertasProcessoIniciado")
    class CriarAlertasProcessoIniciado {
        @Test
        @DisplayName("Deve criar alerta operacional com texto fixo")
        void deveCriarAlertaOperacional() {
            // Given
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setDescricao("Proc");
            Long unidadeId = 1L;

            UnidadeDto uDto = UnidadeDto.builder().tipo(TipoUnidade.OPERACIONAL.name()).build();

            when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
            when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertasProcessoIniciado(p.getCodigo(), List.of(unidadeId), List.of());

            // Then
            verify(alertaRepo).save(argThat(a -> "Início do processo".equals(a.getDescricao())));
        }

        @Test
        @DisplayName("Deve criar alerta intermediária com texto fixo")
        void deveCriarAlertaIntermediaria() {
            // Given
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setDescricao("Proc");
            Long unidadeId = 1L;

            UnidadeDto uDto = UnidadeDto.builder().tipo(TipoUnidade.INTERMEDIARIA.name()).build();

            when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
            when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertasProcessoIniciado(p.getCodigo(), List.of(unidadeId), List.of());

            // Then
            verify(alertaRepo).save(argThat(a ->
                    "Início do processo em unidade(s) subordinada(s)".equals(a.getDescricao())));
        }

        @Test
        @DisplayName("Deve criar 2 alertas para interoperacional")
        void deveCriarDoisAlertasInteroperacional() {
            // Given
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setDescricao("Proc");
            Long unidadeId = 1L;

            UnidadeDto uDto = UnidadeDto.builder().tipo(TipoUnidade.INTEROPERACIONAL.name()).build();

            when(sgrhService.buscarUnidadePorCodigo(unidadeId)).thenReturn(Optional.of(uDto));
            when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(new Unidade()));
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            List<Alerta> resultado = service.criarAlertasProcessoIniciado(p.getCodigo(), List.of(unidadeId), List.of());

            // Then
            assertThat(resultado).hasSize(2);
            verify(alertaRepo, times(2)).save(any());
        }
    }

    @Nested
    @DisplayName("Métodos: criação de alertas específicos")
    class AlertasEspecificos {
        @Test
        @DisplayName("Deve criar alerta de cadastro disponibilizado com sucesso")
        void deveCriarAlertaCadastroDisponibilizado() {
            // Given
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setDescricao("P");
            Long origem = 1L;
            Long destino = 2L;
            Unidade uOrigem = new Unidade();
            uOrigem.setSigla("UO");

            when(unidadeRepo.findById(origem)).thenReturn(Optional.of(uOrigem));
            when(unidadeRepo.findById(destino)).thenReturn(Optional.of(new Unidade()));
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertaCadastroDisponibilizado(p.getCodigo(), p.getDescricao(), origem, destino);

            // Then
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve criar alerta de cadastro devolvido com sucesso")
        void deveCriarAlertaCadastroDevolvido() {
            // Given
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setDescricao("P");
            Long destino = 1L;

            when(unidadeRepo.findById(destino)).thenReturn(Optional.of(new Unidade()));
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertaCadastroDevolvido(p.getCodigo(), p.getDescricao(), destino, "motivo");

            // Then
            verify(alertaRepo).save(any());
        }
    }

    @Nested
    @DisplayName("Método: marcarComoLido")
    class MarcarComoLido {
        @Test
        @DisplayName("Deve marcar como lido com sucesso")
        void deveMarcarComoLido() {
            // Given
            String user = "123";
            Long alertaId = 10L;
            AlertaUsuario au = new AlertaUsuario();
            au.setDataHoraLeitura(null);

            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(au));

            // When
            service.marcarComoLido(user, alertaId);

            // Then
            assertThat(au.getDataHoraLeitura()).isNotNull();
            verify(alertaUsuarioRepo).save(au);
        }
    }

    @Nested
    @DisplayName("Método: listarAlertasPorUsuario")
    class ListarAlertasPorUsuario {
        @Test
        @DisplayName("Deve listar e criar AlertaUsuario (lazy creation)")
        void deveListarECriarAlertaUsuario() {
            // Given
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

            // When
            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            // Then
            assertThat(resultado).hasSize(1);
            verify(alertaUsuarioRepo).save(any());
        }

        @Test
        @DisplayName("Deve listar sem criar novo se já existente")
        void deveListarSemCriarNovo() {
            // Given
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

            // When
            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            // Then
            assertThat(resultado).hasSize(1);
            verify(alertaUsuarioRepo, never()).save(any());
        }
    }
}

package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import sgc.usuario.UsuarioService;
import sgc.usuario.model.Usuario;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.service.UnidadeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: AlertaService")
class AlertaServiceTest {

    @Mock
    private AlertaRepo alertaRepo;
    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioService usuarioService;
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
            Long unidadeId = 1L;
            Unidade u = new Unidade();
            u.setCodigo(unidadeId);

            when(unidadeService.buscarEntidadePorId(unidadeId)).thenReturn(u);
            when(alertaRepo.save(any())).thenReturn(new Alerta().setCodigo(100L));

            // When
            Alerta resultado = service.criarAlerta(
                    p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc");

            // Then
            assertThat(resultado).isNotNull();
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro se unidade não existe")
        void deveLancarErroSeUnidadeNaoExiste() {
            // Given
            Processo p = new Processo();
            Long unidadeId = 999L;

            when(unidadeService.buscarEntidadePorId(unidadeId)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade não encontrada"));

            // When / Then
            assertThatThrownBy(() ->
                    service.criarAlerta(p, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeId, "desc"))
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
            p.setDescricao("Proc");
            Long unidadeId = 1L;
            Unidade u = new Unidade();
            u.setCodigo(unidadeId);
            u.setTipo(TipoUnidade.OPERACIONAL);

            when(unidadeService.buscarEntidadesPorIds(any())).thenReturn(List.of(u));
            // findById ainda é usado internamente pelo criarAlerta (chamado pelo criarAlertasProcessoIniciado)
            when(unidadeService.buscarEntidadePorId(unidadeId)).thenReturn(u);
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

            // Then
            verify(alertaRepo).save(argThat(a -> "Início do processo".equals(a.getDescricao())));
        }

        @Test
        @DisplayName("Deve criar alerta para unidade participante e seus ancestrais")
        void deveCriarAlertaParaAncestrais() {
            // Given
            Processo p = new Processo();
            
            Unidade root = Unidade.builder().nome("Root").tipo(TipoUnidade.INTEROPERACIONAL).build();
            root.setCodigo(1L);
            
            Unidade filho = Unidade.builder().nome("Filho").tipo(TipoUnidade.OPERACIONAL).build();
            filho.setCodigo(2L);
            filho.setUnidadeSuperior(root);

            when(unidadeService.buscarEntidadesPorIds(any())).thenReturn(List.of(filho));
            // Mocks para o criarAlerta interno
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(root);
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(filho);
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertasProcessoIniciado(p, List.of(2L), List.of());

            // Then
            // 1 alerta operacional para o filho
            verify(alertaRepo).save(argThat(a -> "Início do processo".equals(a.getDescricao()) 
                    && a.getUnidadeDestino().getCodigo().equals(2L)));
            
            // 1 alerta intermediário para o pai (root)
            verify(alertaRepo).save(argThat(a -> "Início do processo em unidade(s) subordinada(s)".equals(a.getDescricao()) 
                    && a.getUnidadeDestino().getCodigo().equals(1L)));
        }

        @Test
        @DisplayName("Deve criar 2 alertas para interoperacional participante")
        void deveCriarDoisAlertasInteroperacional() {
            // Given
            Processo p = new Processo();
            Long unidadeId = 1L;
            Unidade u = Unidade.builder().tipo(TipoUnidade.INTEROPERACIONAL).build();
            u.setCodigo(unidadeId);

            when(unidadeService.buscarEntidadesPorIds(any())).thenReturn(List.of(u));
            when(unidadeService.buscarEntidadePorId(unidadeId)).thenReturn(u);
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            List<Alerta> resultado = service.criarAlertasProcessoIniciado(p, List.of(unidadeId), List.of());

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
            p.setDescricao("P");
            Long origem = 1L;
            Long destino = 2L;
            Unidade uOrigem = new Unidade();
            uOrigem.setSigla("UO");

            when(unidadeService.buscarEntidadePorId(origem)).thenReturn(uOrigem);
            when(unidadeService.buscarEntidadePorId(destino)).thenReturn(new Unidade());
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertaCadastroDisponibilizado(p, origem, destino);

            // Then
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve criar alerta de cadastro devolvido com sucesso")
        void deveCriarAlertaCadastroDevolvido() {
            // Given
            Processo p = new Processo();
            p.setDescricao("P");
            Long destino = 1L;

            when(unidadeService.buscarEntidadePorId(destino)).thenReturn(new Unidade());
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertaCadastroDevolvido(p, destino, "motivo");

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

            when(usuarioService.buscarEntidadePorId(usuarioTitulo)).thenReturn(usuario);
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

            when(usuarioService.buscarEntidadePorId(usuarioTitulo)).thenReturn(usuario);
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

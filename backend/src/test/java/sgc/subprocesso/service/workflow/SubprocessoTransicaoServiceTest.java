package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.comum.erros.ErroAcessoNegado;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Assertions;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes Unitários para SubprocessoTransicaoService")
class SubprocessoTransicaoServiceTest {

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UsuarioFacade usuarioFacade;

    @InjectMocks
    private SubprocessoTransicaoService service;

    @Test
    @DisplayName("Deve registrar transição com observações e publicar evento")
    void deveRegistrarComObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade origem = mock(Unidade.class);
        Unidade destino = mock(Unidade.class);
        Usuario usuario = new Usuario();

        // Act
        service.registrar(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(origem)
                .destino(destino)
                .usuario(usuario)
                .observacoes("OBS")
                .build());

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(eventPublisher).publishEvent(any(EventoTransicaoSubprocesso.class));
    }

    @Test
    @DisplayName("Deve registrar transição sem observações")
    void deveRegistrarSemObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade origem = mock(Unidade.class);
        Unidade destino = mock(Unidade.class);
        Usuario usuario = new Usuario();

        // Act
        service.registrar(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DEVOLVIDO)
                .origem(origem)
                .destino(destino)
                .usuario(usuario)
                .build());

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(eventPublisher).publishEvent(any(EventoTransicaoSubprocesso.class));
    }

    @Test
    @DisplayName("Deve buscar usuário da facade quando não fornecido no comando")
    void deveBuscarUsuarioDaFacadeQuandoNaoFornecido() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Usuario usuarioAutenticado = new Usuario();
        when(usuarioFacade.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);

        // Act
        service.registrar(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .build());

        // Assert
        verify(usuarioFacade).obterUsuarioAutenticado();
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Deve lançar erro quando usuário não for fornecido e não houver usuário autenticado")
    void deveLancarErroQuandoUsuarioNaoAutenticado() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        when(usuarioFacade.obterUsuarioAutenticado()).thenThrow(new ErroAcessoNegado("Erro"));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .build();

        // Act & Assert
        Assertions.assertThrows(ErroAcessoNegado.class, () -> {
            service.registrar(cmd);
        });
    }

    @Test
    @DisplayName("Deve lidar com unidades nulas")
    void deveLidarComUnidadesNulas() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Usuario usuario = new Usuario();

        // Act
        service.registrar(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .usuario(usuario)
                .build());

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(eventPublisher).publishEvent(any(EventoTransicaoSubprocesso.class));
    }
}

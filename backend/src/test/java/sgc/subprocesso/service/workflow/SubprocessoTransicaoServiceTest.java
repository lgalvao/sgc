package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoTransicao;
import sgc.subprocesso.service.SubprocessoEmailService;
import sgc.subprocesso.service.SubprocessoTransicaoService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para SubprocessoTransicaoService")
class SubprocessoTransicaoServiceTest {

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private AlertaFacade alertaService;

    @Mock
    private SubprocessoEmailService emailService;

    @Mock
    private UsuarioFacade usuarioFacade;

    @InjectMocks
    private SubprocessoTransicaoService service;

    @Test
    @DisplayName("Deve registrar transição com observações e notificar via alerta/email")
    void deveRegistrarComObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getSigla()).thenReturn("U1");
        when(subprocesso.getUnidade()).thenReturn(unidade);
        when(subprocesso.getProcesso()).thenReturn(mock(Processo.class));

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
        verify(alertaService).criarAlertaTransicao(any(), anyString(), any(), any());
        verify(emailService).enviarEmailTransicaoDireta(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve registrar transição sem observações")
    void deveRegistrarSemObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getSigla()).thenReturn("U1");
        when(subprocesso.getUnidade()).thenReturn(unidade);
        when(subprocesso.getProcesso()).thenReturn(mock(Processo.class));

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
        verify(alertaService).criarAlertaTransicao(any(), anyString(), any(), any());
    }

    @Test
    @DisplayName("Deve buscar usuário da facade quando não fornecido no comando")
    void deveBuscarUsuarioDaFacadeQuandoNaoFornecido() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getSigla()).thenReturn("U1");
        when(subprocesso.getUnidade()).thenReturn(unidade);
        when(subprocesso.getProcesso()).thenReturn(mock(Processo.class));

        Usuario usuarioAutenticado = new Usuario();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuarioAutenticado);

        // Act
        service.registrar(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .build());

        // Assert
        verify(usuarioFacade).usuarioAutenticado();
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Deve lançar erro quando usuário não for fornecido e não houver usuário autenticado")
    void deveLancarErroQuandoUsuarioNaoAutenticado() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        when(usuarioFacade.usuarioAutenticado()).thenThrow(new ErroAcessoNegado("Erro"));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .build();

        // Act & Assert
        Assertions.assertThrows(ErroAcessoNegado.class, () -> service.registrar(cmd));
    }

    @Test
    @DisplayName("Deve lidar com unidades nulas")
    void deveLidarComUnidadesNulas() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getSigla()).thenReturn("U1");
        when(subprocesso.getUnidade()).thenReturn(unidade);
        when(subprocesso.getProcesso()).thenReturn(mock(Processo.class));

        Usuario usuario = new Usuario();

        // Act
        service.registrar(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .usuario(usuario)
                .build());

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Não deve enviar alerta/email quando tipo não exige")
    void naoDeveNotificarQuandoTipoNaoExige() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Usuario usuario = new Usuario();

        // CADASTRO_HOMOLOGADO: geraAlerta=false, enviaEmail=false
        service.registrar(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .usuario(usuario)
                .build());

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verifyNoInteractions(alertaService);
        verifyNoInteractions(emailService);
    }


}

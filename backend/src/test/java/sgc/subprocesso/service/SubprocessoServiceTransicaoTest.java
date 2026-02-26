package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.thymeleaf.*;
import org.thymeleaf.context.*;
import sgc.alerta.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Transição")
class SubprocessoServiceTransicaoTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private MovimentacaoRepo movimentacaoRepo;
    @Mock private ComumRepo repo;
    @Mock private AnaliseRepo analiseRepo;
    @Mock private AlertaFacade alertaService;
    @Mock private OrganizacaoFacade organizacaoFacade;
    @Mock private UsuarioFacade usuarioFacade;
    @Mock private ImpactoMapaService impactoMapaService;
    @Mock private CopiaMapaService copiaMapaService;
    @Mock private EmailService emailService;
    @Mock private TemplateEngine templateEngine;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private MapaSalvamentoService mapaSalvamentoService;
    @Mock private MapaAjusteMapper mapaAjusteMapper;
    @Mock private SgcPermissionEvaluator permissionEvaluator;

    @InjectMocks
    private SubprocessoService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
        service.setCopiaMapaService(copiaMapaService);
    }

    @Test
    @DisplayName("Deve registrar transição com observações e notificar via alerta/email")
    void deveRegistrarComObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getSigla()).thenReturn("U1");
        when(subprocesso.getUnidade()).thenReturn(unidade);

        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(subprocesso.getProcesso()).thenReturn(processo);

        Unidade origem = mock(Unidade.class);
        when(origem.getSigla()).thenReturn("O");
        Unidade destino = mock(Unidade.class);
        when(destino.getSigla()).thenReturn("D");
        Usuario usuario = new Usuario();

        // Ensure template engine is mocked to avoid NPE if email logic is triggered
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        // Act
        service.registrarTransicao(RegistrarTransicaoCommand.builder()
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
        // Email verification happens via interactions with EmailService, not by verifying call to private method
        verify(emailService, atLeastOnce()).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve registrar transição sem observações")
    void deveRegistrarSemObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade unidade = mock(Unidade.class);
        when(unidade.getSigla()).thenReturn("U1");
        when(subprocesso.getUnidade()).thenReturn(unidade);

        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(subprocesso.getProcesso()).thenReturn(processo);

        Unidade origem = mock(Unidade.class);
        Unidade destino = mock(Unidade.class);
        Usuario usuario = new Usuario();

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        // Act
        service.registrarTransicao(RegistrarTransicaoCommand.builder()
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

        Processo processo = mock(Processo.class);
        when(subprocesso.getProcesso()).thenReturn(processo);

        Usuario usuarioAutenticado = new Usuario();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuarioAutenticado);

        // Act
        service.registrarTransicao(RegistrarTransicaoCommand.builder()
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
        Assertions.assertThrows(ErroAcessoNegado.class, () -> service.registrarTransicao(cmd));
    }

    @Test
    @DisplayName("Não deve enviar alerta/email quando tipo não exige")
    void naoDeveNotificarQuandoTipoNaoExige() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Usuario usuario = new Usuario();

        // CADASTRO_HOMOLOGADO: geraAlerta=false, enviaEmail=false
        service.registrarTransicao(RegistrarTransicaoCommand.builder()
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

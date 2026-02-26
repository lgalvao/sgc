package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.thymeleaf.*;
import org.thymeleaf.context.*;
import sgc.alerta.*;
import sgc.comum.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Email Notifications")
class SubprocessoServiceEmailTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private OrganizacaoFacade organizacaoFacade;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private EmailService emailService;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private ComumRepo repo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;
    @Mock
    private CopiaMapaService copiaMapaService;

    @InjectMocks
    private SubprocessoService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
    }

    private Unidade criarUnidade(String sigla) {
        return Unidade.builder()
                .codigo(new Random().nextLong(1000) + 1)
                .sigla(sigla)
                .nome("Unidade " + sigla)
                .build();
    }

    @Test
    @DisplayName("Envia email para unidade destino")
    void enviaEmailDestino() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa1(LocalDateTime.now());

        Unidade dest = criarUnidade("DEST");
        Unidade orig = criarUnidade("ORIG");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DEVOLVIDO)
                .origem(orig)
                .destino(dest)
                .observacoes("Motivo")
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Notifica hierarquia")
    void notificaHierarquia() {
        Subprocesso sp = criarSubprocesso();

        Unidade destino = criarUnidade("DEST");
        Unidade origem = criarUnidade("ORIG");
        Unidade superior = criarUnidade("SUP");
        origem.setUnidadeSuperior(superior);

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");
        when(organizacaoFacade.buscarResponsavelUnidade(anyLong())).thenReturn(mock(UnidadeResponsavelDto.class));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(origem)
                .destino(destino)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        // Verifica envio para superior
        verify(emailService).enviarEmailHtml(eq("sup@tre-pe.jus.br"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Lida com exceção ao enviar email")
    void lidaComExcecao() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = criarUnidade("DEST");
        Unidade orig = criarUnidade("ORIG");

        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(orig)
                .destino(dest)
                .usuario(new Usuario())
                .build();

        assertThatCode(() -> service.registrarTransicao(cmd)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não envia email se tipo de transição não exige")
    void naoEnviaSeTipoNaoExige() {
        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(new Subprocesso())
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .origem(criarUnidade("O"))
                .destino(criarUnidade("D"))
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Não notifica hierarquia se origem nula")
    void naoNotificaHierarquiaSemOrigem() {
        Subprocesso sp = criarSubprocesso();
        Unidade destino = criarUnidade("DEST");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(null)
                .destino(destino)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);
        // Com a nova validação, se a origem for nula, o processo de notificação nem inicia (não processa template nem envia e-mail)
        verifyNoInteractions(templateEngine);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Trata exceção ao notificar hierarquia")
    void trataExcecaoHierarquia() {
        Subprocesso sp = criarSubprocesso();
        Unidade origem = criarUnidade("ORIG");
        Unidade superior = criarUnidade("SUP");
        origem.setUnidadeSuperior(superior);
        Unidade destino = criarUnidade("DEST");

        when(organizacaoFacade.buscarResponsavelUnidade(anyLong())).thenReturn(mock(UnidadeResponsavelDto.class));
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");
        
        // Simula erro no envio para a unidade superior, mas o fluxo deve continuar para a destino
        doThrow(new RuntimeException("Fail")).when(emailService).enviarEmailHtml(eq("sup@tre-pe.jus.br"), anyString(), any());

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(origem)
                .destino(destino)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        // Verifica que o envio para a unidade de destino ocorreu
        verify(emailService, atLeastOnce()).enviarEmailHtml(eq("dest@tre-pe.jus.br"), any(), any());
        // Verifica que o envio para o superior foi tentado
        verify(emailService, atLeastOnce()).enviarEmailHtml(eq("sup@tre-pe.jus.br"), any(), any());
    }

    @Test
    @DisplayName("Deve incluir DataLimiteEtapa2 nas variáveis")
    void deveIncluirDataLimiteEtapa2NasVariaveis() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(criarUnidade("O"))
                .destino(criarUnidade("D"))
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(templateEngine).process(eq("cadastro-disponibilizado"), argThat(ctx ->
                ctx instanceof Context && ctx.getVariable("dataLimiteEtapa2") != null
        ));
    }

    @Test
    @DisplayName("Deve incluir Observações nas variáveis")
    void deveIncluirObservacoesNasVariaveis() {
        Subprocesso sp = criarSubprocesso();
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(criarUnidade("O"))
                .destino(criarUnidade("D"))
                .observacoes("Minha Observação")
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(templateEngine).process(eq("cadastro-disponibilizado"), argThat(ctx ->
                ctx instanceof Context && "Minha Observação".equals(ctx.getVariable("observacoes"))
        ));
    }

    @Test
    @DisplayName("Deve lidar com tipo processo iniciado (switch default)")
    void deveLidarComTipoProcessoIniciado() {
        Subprocesso sp = criarSubprocesso();
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.PROCESSO_INICIADO)
                .origem(criarUnidade("O"))
                .destino(criarUnidade("D"))
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(emailService).enviarEmailHtml(any(),
                argThat(s -> s.contains("SGC: Processo iniciado")),
                any());
    }

    @Test
    @DisplayName("Deve enviar email para substituto se houver")
    void deveEnviarEmailParaSubstitutoSeHouver() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = criarUnidade("DEST");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(dest.getCodigo())
                .substitutoTitulo("123456")
                .build();
        when(organizacaoFacade.buscarResponsavelUnidade(dest.getCodigo())).thenReturn(resp);

        Usuario substituto = new Usuario();
        substituto.setEmail("sub@teste.com");
        when(usuarioFacade.buscarUsuarioPorTitulo("123456")).thenReturn(Optional.of(substituto));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(criarUnidade("O"))
                .destino(dest)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(emailService).enviarEmailHtml(eq("sub@teste.com"), anyString(), eq("html"));
    }

    private Subprocesso criarSubprocesso() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(criarUnidade("U1"));
        return sp;
    }
}

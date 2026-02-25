package sgc.subprocesso.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.comum.model.ComumRepo;
import sgc.subprocesso.dto.MapaAjusteMapper;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaSalvamentoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Email Notifications")
class SubprocessoServiceEmailTest {

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
    }

    @Test
    @DisplayName("Envia email para unidade destino")
    void enviaEmailDestino() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa1(LocalDateTime.now());

        Unidade dest = new Unidade();
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DEVOLVIDO)
                .origem(new Unidade())
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

        Unidade destino = new Unidade();
        destino.setCodigo(1L);
        destino.setSigla("DEST");

        Unidade origem = new Unidade();
        origem.setSigla("ORIG");
        Unidade superior = new Unidade();
        superior.setCodigo(2L); // ID diferente de DEST
        superior.setSigla("SUP");
        origem.setUnidadeSuperior(superior);

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        // Mock responsavel para evitar NPE
        when(organizacaoFacade.buscarResponsavelUnidade(1L)).thenReturn(mock(UnidadeResponsavelDto.class));

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

        Unidade dest = new Unidade();
        dest.setSigla("DEST");
        
        // Ensure template engine is called, so exception is thrown in the try block
        lenient().when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(new Unidade())
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
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO) // Does not send email? Checking TipoTransicao...
                // Wait, CADASTRO_HOMOLOGADO might not send email.
                // Assuming original test `naoEnviaSeTipoNaoExige` used this type.
                .origem(new Unidade())
                .destino(new Unidade())
                .usuario(new Usuario())
                .build();

        // But cmd.sp() cannot be null as SubprocessoService uses it.
        // And Movimentacao requires user.

        service.registrarTransicao(cmd);

        verifyNoInteractions(emailService);
        verifyNoInteractions(templateEngine);
    }

    @Test
    @DisplayName("Não notifica hierarquia se origem nula")
    void naoNotificaHierarquiaSemOrigem() {
        Subprocesso sp = criarSubprocesso();
        Unidade destino = new Unidade();
        destino.setSigla("DEST");

        lenient().when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(null) // Null origin
                .destino(destino)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        // Logs error because variable creation fails with null origin, so no email sent
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Trata exceção ao notificar hierarquia")
    void trataExcecaoHierarquia() {
        Subprocesso sp = criarSubprocesso();
        sp.getUnidade().setSigla("U1");

        Unidade origem = new Unidade();
        origem.setSigla("ORIG");
        Unidade superior = new Unidade();
        superior.setCodigo(2L); // ID diferente de DEST
        superior.setSigla("SUP");
        origem.setUnidadeSuperior(superior);

        Unidade destino = new Unidade();
        destino.setCodigo(1L);
        destino.setSigla("DEST");

        // Mock responsavel para evitar NPE
        lenient().when(organizacaoFacade.buscarResponsavelUnidade(1L)).thenReturn(mock(UnidadeResponsavelDto.class));

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        doNothing().when(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), anyString(), any());
        doThrow(new RuntimeException("Fail")).when(emailService).enviarEmailHtml(eq("sup@tre-pe.jus.br"), anyString(), any());

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(origem)
                .destino(destino)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(emailService).enviarEmailHtml(eq("sup@tre-pe.jus.br"), anyString(), any());
        verify(emailService).enviarEmailHtml(eq("dest@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Cria variáveis sem datas nem observações")
    void deveCriarVariaveisSemDatasNemObservacoes() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa1(null);
        sp.setDataLimiteEtapa2(null);

        Unidade orig = new Unidade(); orig.setSigla("O");
        Unidade dest = new Unidade(); dest.setSigla("D");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(orig)
                .destino(dest)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);
        verify(emailService).enviarEmailHtml(eq("d@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Deve incluir DataLimiteEtapa2 nas variáveis")
    void deveIncluirDataLimiteEtapa2NasVariaveis() {
        Subprocesso sp = criarSubprocesso();
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        Unidade dest = new Unidade(); dest.setSigla("D");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(new Unidade())
                .destino(dest)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(templateEngine).process(eq("cadastro-disponibilizado"), argThat(ctx -> 
            ctx instanceof Context && ((Context)ctx).getVariable("dataLimiteEtapa2") != null
        ));
    }

    @Test
    @DisplayName("Deve incluir Observações nas variáveis")
    void deveIncluirObservacoesNasVariaveis() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        Unidade dest = new Unidade(); dest.setSigla("D");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(new Unidade())
                .destino(dest)
                .observacoes("Minha Observação")
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(templateEngine).process(eq("cadastro-disponibilizado"), argThat(ctx ->
                ctx instanceof Context && "Minha Observação".equals(((Context)ctx).getVariable("observacoes"))
        ));
    }

    @Test
    @DisplayName("Deve lidar com tipo processo iniciado (switch default)")
    void deveLidarComTipoProcessoIniciado() {
        Subprocesso sp = criarSubprocesso();

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        Unidade dest = new Unidade(); dest.setSigla("D");

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.PROCESSO_INICIADO)
                .origem(new Unidade())
                .destino(dest)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        // Verifica que enviou email com assunto formatado
        verify(emailService).enviarEmailHtml(any(),
                argThat(s -> s != null && s.contains("SGC: Processo iniciado")),
                any());
    }

    @Test
    @DisplayName("Deve enviar email para substituto se houver")
    void deveEnviarEmailParaSubstitutoSeHouver() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = new Unidade();
        dest.setCodigo(1L);
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .substitutoTitulo("123456")
                .build();
        when(organizacaoFacade.buscarResponsavelUnidade(1L)).thenReturn(resp);

        Usuario substituto = new Usuario();
        substituto.setEmail("sub@teste.com");
        when(usuarioFacade.buscarUsuarioPorTitulo("123456")).thenReturn(Optional.of(substituto));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(new Unidade())
                .destino(dest)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        verify(emailService).enviarEmailHtml(eq("sub@teste.com"), anyString(), eq("html"));
    }

    @Test
    @DisplayName("Não deve enviar email para substituto se email vazio")
    void naoDeveEnviarEmailParaSubstitutoSeEmailVazio() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = new Unidade();
        dest.setCodigo(1L);
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .substitutoTitulo("123456")
                .build();
        when(organizacaoFacade.buscarResponsavelUnidade(1L)).thenReturn(resp);

        Usuario substituto = new Usuario();
        substituto.setEmail(""); // Email vazio
        when(usuarioFacade.buscarUsuarioPorTitulo("123456")).thenReturn(Optional.of(substituto));

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(new Unidade())
                .destino(dest)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        // Verifica envio para a unidade (padrão), mas NÃO para o substituto
        verify(emailService, times(1)).enviarEmailHtml(anyString(), anyString(), anyString());
        verify(emailService, never()).enviarEmailHtml(eq(""), anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve enviar email pessoal se responsavel for null")
    void naoDeveEnviarEmailPessoalSeResponsavelNull() {
        Subprocesso sp = criarSubprocesso();
        Unidade dest = new Unidade();
        dest.setCodigo(1L);
        dest.setSigla("DEST");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");
        when(organizacaoFacade.buscarResponsavelUnidade(1L)).thenReturn(null);

        RegistrarTransicaoCommand cmd = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_DISPONIBILIZADO)
                .origem(new Unidade())
                .destino(dest)
                .usuario(new Usuario())
                .build();

        service.registrarTransicao(cmd);

        // Apenas o email da unidade deve ser enviado
        verify(emailService, times(1)).enviarEmailHtml(anyString(), anyString(), anyString());
    }

    private Subprocesso criarSubprocesso() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setNome("Unidade 1");
        return sp;
    }
}

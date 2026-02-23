package sgc.processo.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.notificacao.EmailModelosService;
import sgc.notificacao.EmailService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;


import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do ProcessoNotificacaoService")
class ProcessoNotificacaoServiceTest {

    @InjectMocks
    private ProcessoNotificacaoService service;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private UnidadeFacade unidadeService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailModelosService emailModelosService;

    @Mock
    private AlertaFacade servicoAlertas;



    private Processo criarProcesso(Long codigo) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo " + codigo);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(new ArrayList<>());
        return p;
    }

    private Unidade criarUnidade(Long codigo, TipoUnidade tipo) {
        return Unidade.builder()
                .codigo(codigo)
                .tipo(tipo)
                .sigla("U" + codigo)
                .situacao(SituacaoUnidade.ATIVA)
                .build();
    }

    @Test
    @DisplayName("Deve disparar e-mails ao iniciar processo")
    void deveDispararEmailsAoIniciar() {
        when(emailModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(1L);
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));

        Unidade unidade = criarUnidade(10L, TipoUnidade.OPERACIONAL);
        unidade.setSigla("U1");
        unidade.setNome("Unidade 1");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        UnidadeResponsavelDto responsavel = UnidadeResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("111")
                .substitutoTitulo("222")
                .build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));

        Usuario titular = Usuario.builder().tituloEleitoral("111").email("t@t.com").build();
        Usuario substituto = Usuario.builder().tituloEleitoral("222").email("s@s.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("111", titular, "222", substituto));

        service.emailInicioProcesso(1L, List.of());

        verify(emailService).enviarEmailHtml(eq("u1@tre-pe.jus.br"), anyString(), any());
        verify(emailService).enviarEmailHtml(eq("s@s.com"), anyString(), any());
    }

    @Test
    @DisplayName("Deve ignorar processamento se não houver subprocessos ao iniciar")
    void deveIgnorarProcessamentoSeNaoHouverSubprocessosAoIniciar() {
        Processo processo = criarProcesso(1L);
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of());

        service.emailInicioProcesso(1L, List.of());

        verify(servicoAlertas, never()).criarAlertasProcessoIniciado(any(), any());
    }

    @Test
    @DisplayName("Deve cobrir exceções de tipo de unidade no início")
    void deveCobrirExcecaoTipoUnidadeInvalido() {
        Processo processo = criarProcesso(1L);
        Subprocesso s = new Subprocesso();
        Unidade u = criarUnidade(1L, TipoUnidade.SEM_EQUIPE);
        s.setUnidade(u);

        // Testa exceção ao criar corpo para tipo não suportado
        assertThatThrownBy(() -> service.criarCorpoEmailPorTipo(TipoUnidade.SEM_EQUIPE, processo, s))
                .isInstanceOf(IllegalArgumentException.class);

        // Testa que RAIZ não joga exceção
        assertThatCode(() -> service.criarCorpoEmailPorTipo(TipoUnidade.RAIZ, processo, s))
                .doesNotThrowAnyException();

        // Testa catch no fluxo principal para tipo não suportado (ex: SEM_EQUIPE)
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularTitulo("T").build()));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", Usuario.builder().build()));

        service.emailInicioProcesso(1L, List.of());
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir branches de substitutos")
    void deveCobrirBranchesSubstitutos() {
        when(emailModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(1L);
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));

        Unidade u = criarUnidade(10L, TipoUnidade.OPERACIONAL);
        Subprocesso s = new Subprocesso();
        s.setUnidade(u);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("T").substitutoTitulo("S").build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, r));

        Usuario titular = Usuario.builder().tituloEleitoral("T").email("t@mail.com").build();

        // 1. Substituto null no mapa de usuários
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", titular));
        service.emailInicioProcesso(1L, List.of());

        // 2. Substituto com email null ou blank
        Usuario subEmailNull = Usuario.builder().tituloEleitoral("S").email(null).build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", titular, "S", subEmailNull));
        service.emailInicioProcesso(1L, List.of());

        Usuario subEmailBlank = Usuario.builder().tituloEleitoral("S").email("  ").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", titular, "S", subEmailBlank));
        service.emailInicioProcesso(1L, List.of());

        verify(emailService, times(3)).enviarEmailHtml(contains("@tre-pe.jus.br"), anyString(), anyString());
        verify(emailService, never()).enviarEmailHtml(eq("t@mail.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve cobrir branches na finalização de processo")
    void deveCobrirBranchesFinalizacao() {
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(2L);
        when(processoRepo.findByIdComParticipantes(2L)).thenReturn(Optional.of(processo));

        Unidade operacional = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        operacional.setSigla("OP");
        Unidade intermediaria = criarUnidade(2L, TipoUnidade.INTERMEDIARIA);
        intermediaria.setSigla("INT");

        processo.adicionarParticipantes(Set.of(operacional, intermediaria));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", Usuario.builder().email("op@mail.com").build(),
                "T2", Usuario.builder().email("int@mail.com").build()
        ));

        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(operacional, intermediaria));
        
        // 1. Intermediária sem subordinadas (log.warn)
        service.emailFinalizacaoProcesso(2L);
        // Operacional recebe (unit email), Intermediaria não (não tem subordinadas válidas no teste)
        verify(emailService, never()).enviarEmailHtml(eq("op@mail.com"), anyString(), any());
        verify(emailService, never()).enviarEmailHtml(eq("int@mail.com"), anyString(), any());
        verify(emailService).enviarEmailHtml(eq("op@tre-pe.jus.br"), anyString(), any());

        // 2. Intermediária com subordinadas (sucesso)
        Unidade sub = criarUnidade(21L, TipoUnidade.OPERACIONAL);
        sub.setUnidadeSuperior(intermediaria);
        sub.setSigla("SUB");
        processo.adicionarParticipantes(Set.of(operacional, intermediaria, sub));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build(),
                21L, UnidadeResponsavelDto.builder().unidadeCodigo(21L).titularTitulo("TS").build()
        ));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", Usuario.builder().email("op@mail.com").build(),
                "T2", Usuario.builder().email("int@mail.com").build(),
                "TS", Usuario.builder().email("sub@mail.com").build()
        ));

        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(operacional, intermediaria, sub));

        service.emailFinalizacaoProcesso(2L);

        // Unit emails: op@ is sent in BOTH calls (part 1 and 2), int@ only in part 2
        verify(emailService, times(2)).enviarEmailHtml(eq("op@tre-pe.jus.br"), anyString(), any());
        verify(emailService, times(1)).enviarEmailHtml(eq("int@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir exceção para unidade SEM_EQUIPE no envio de email")
    void deveCobrirExcecaoSemEquipe() {
        Processo processo = criarProcesso(1L);
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));

        Subprocesso s = new Subprocesso();
        Unidade u = criarUnidade(1L, TipoUnidade.SEM_EQUIPE);
        s.setUnidade(u);

        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        service.emailInicioProcesso(1L, List.of());

        // Deve logar erro e não enviar
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir else implícito na finalização (tipo desconhecido)")
    void deveCobrirElseImplicitoFinalizacao() {
        Processo processo = criarProcesso(2L);
        when(processoRepo.findByIdComParticipantes(2L)).thenReturn(Optional.of(processo));

        // SEM_EQUIPE cai no else implícito de enviarNotificacaoFinalizacao
        Unidade semEquipe = criarUnidade(1L, TipoUnidade.SEM_EQUIPE);

        processo.adicionarParticipantes(Set.of(semEquipe));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", Usuario.builder().email("op@mail.com").build()
        ));
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(semEquipe));
        service.emailFinalizacaoProcesso(2L);
        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve ignorar se participantes for vazio ao finalizar")
    void deveLogarWarningSeParticipantesVazio() {
        Processo p = criarProcesso(1L);
        p.setParticipantes(new ArrayList<>());
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(p));

        service.emailFinalizacaoProcesso(1L);

        verify(unidadeService, never()).buscarResponsaveisUnidades(anyList());
    }

    @Test
    @DisplayName("Deve capturar exceção no catch interno do loop de e-mails")
    void deveCapturarExcecaoNoCatchInterno() {
        when(emailModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(1L);
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));

        Unidade u = criarUnidade(10L, TipoUnidade.OPERACIONAL);
        Subprocesso s = new Subprocesso();
        s.setCodigo(100L);
        s.setUnidade(u);
        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder().unidadeCodigo(10L).titularTitulo("T").build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, r));

        // Quando buscar usuário por titulo retornar null, o try interno vai estourar NPE
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Collections.emptyMap());

        service.emailInicioProcesso(1L, List.of());

        verify(emailService).enviarEmailHtml(eq("u10@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção para SEM_EQUIPE")
    void deveLancarExcecaoParaSemEquipe() {
        Processo processo = criarProcesso(1L);
        Subprocesso s = new Subprocesso();
        Unidade u = criarUnidade(1L, TipoUnidade.SEM_EQUIPE);
        s.setUnidade(u);

        assertThatThrownBy(() -> service.criarCorpoEmailPorTipo(TipoUnidade.SEM_EQUIPE, processo, s))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve ignorar e-mail se titular inválido ao iniciar")
    void deveIgnorarEmailSeTitularInvalidoAoIniciar() {
        when(emailModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(1L);
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));

        // Unidade 1: Titular null no mapa
        Unidade u1 = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        Subprocesso s1 = new Subprocesso();
        s1.setUnidade(u1);

        // Unidade 2: Titular com email null
        Unidade u2 = criarUnidade(2L, TipoUnidade.OPERACIONAL);
        Subprocesso s2 = new Subprocesso();
        s2.setUnidade(u2);

        // Unidade 3: Titular com email blank
        Unidade u3 = criarUnidade(3L, TipoUnidade.OPERACIONAL);
        Subprocesso s3 = new Subprocesso();
        s3.setUnidade(u3);

        when(subprocessoFacade.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s1, s2, s3));

        UnidadeResponsavelDto r1 = UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        UnidadeResponsavelDto r2 = UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build();
        UnidadeResponsavelDto r3 = UnidadeResponsavelDto.builder().unidadeCodigo(3L).titularTitulo("T3").build();

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1, 2L, r2, 3L, r3));

        Usuario user2 = Usuario.builder().tituloEleitoral("T2").email(null).build();
        Usuario user3 = Usuario.builder().tituloEleitoral("T3").email(" ").build();

        // T1 ausente no map
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T2", user2, "T3", user3));

        service.emailInicioProcesso(1L, List.of());

        // Deve enviar para os e-mails das 3 unidades
        verify(emailService).enviarEmailHtml(eq("u1@tre-pe.jus.br"), anyString(), any());
        verify(emailService).enviarEmailHtml(eq("u2@tre-pe.jus.br"), anyString(), any());
        verify(emailService).enviarEmailHtml(eq("u3@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir tipo INTEROPERACIONAL na finalização")
    void deveCobrirTipoInteroperacionalNaFinalizacao() {
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(5L);
        when(processoRepo.findByIdComParticipantes(5L)).thenReturn(Optional.of(processo));

        Unidade interoperacional = criarUnidade(1L, TipoUnidade.INTEROPERACIONAL);
        interoperacional.setSigla("INTER");
        processo.adicionarParticipantes(Set.of(interoperacional));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", Usuario.builder().email("inter@mail.com").build()
        ));
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(interoperacional));

        service.emailFinalizacaoProcesso(5L);

        verify(emailService).enviarEmailHtml(eq("inter@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir exceção ao enviar email para unidade intermediária")
    void deveCobrirExcecaoAoEnviarEmailUnidadeIntermediaria() {
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("corpo");
        when(emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(6L);
        when(processoRepo.findByIdComParticipantes(6L)).thenReturn(Optional.of(processo));

        Unidade intermediaria = criarUnidade(1L, TipoUnidade.INTERMEDIARIA);
        intermediaria.setSigla("INT");
        Unidade sub = criarUnidade(2L, TipoUnidade.OPERACIONAL);
        sub.setUnidadeSuperior(intermediaria);
        sub.setSigla("SUB");

        processo.adicionarParticipantes(Set.of(intermediaria, sub));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", Usuario.builder().email("int@mail.com").build(),
                "T2", Usuario.builder().email("sub@mail.com").build()
        ));

        // Simular exceção ao criar email para unidade intermediária
        when(emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any()))
                .thenThrow(new RuntimeException("Erro ao criar template"));
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(intermediaria, sub));

        service.emailFinalizacaoProcesso(6L);

        // Deve ter tentado enviar para operacional (sucesso) mas não para intermediária (exceção)
        verify(emailService).enviarEmailHtml(eq("sub@tre-pe.jus.br"), anyString(), any());
        verify(emailService, never()).enviarEmailHtml(eq("int@tre-pe.jus.br"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cobrir substituto com email null")
    void deveCobrirSubstitutoComEmailNull() {
        when(emailModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(7L);
        when(processoRepo.findByIdComParticipantes(7L)).thenReturn(Optional.of(processo));

        Unidade u = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        u.setSigla("U1");
        u.setNome("Unidade 1");
        Subprocesso s = new Subprocesso();
        s.setUnidade(u);

        when(subprocessoFacade.listarEntidadesPorProcesso(7L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .titularTitulo("T1")
                .substitutoTitulo("S1")
                .build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r));

        Usuario titular = Usuario.builder().tituloEleitoral("T1").email("t@mail.com").build();
        // Substituto com email null (NPE potencial)
        Usuario substituto = Usuario.builder().tituloEleitoral("S1").email(null).build();

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", titular, "S1", substituto));

        service.emailInicioProcesso(7L, List.of());

        verify(emailService).enviarEmailHtml(eq("u1@tre-pe.jus.br"), anyString(), any());
        verify(emailService, never()).enviarEmailHtml(eq(null), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve cobrir responsáveis sem titulares na finalização")
    void deveCobrirResponsaveisSemTitularesNaFinalizacao() {
        Processo processo = criarProcesso(8L);
        when(processoRepo.findByIdComParticipantes(8L)).thenReturn(Optional.of(processo));

        Unidade u1 = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        Unidade u2 = criarUnidade(2L, TipoUnidade.OPERACIONAL);

        processo.adicionarParticipantes(Set.of(u1, u2));

        // Responsáveis com titularTitulo null - stream resultará vazio
        UnidadeResponsavelDto r1 = UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo(null).build();
        UnidadeResponsavelDto r2 = UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo(null).build();

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1, 2L, r2));

        // Stream vazio leva a buscarUsuariosPorTitulos com lista vazia
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Collections.emptyMap());

        service.emailFinalizacaoProcesso(8L);

        verify(emailService, never()).enviarEmailHtml(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cobrir catch de exceção durante envio de email")
    void deveCobrirCatchExcecaoDuranteEnvioEmail() {
        when(emailModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(9L);
        when(processoRepo.findByIdComParticipantes(9L)).thenReturn(Optional.of(processo));

        Unidade u = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        u.setSigla("OP");
        u.setNome("Unidade Op");
        Subprocesso s = new Subprocesso();
        s.setUnidade(u);

        when(subprocessoFacade.listarEntidadesPorProcesso(9L)).thenReturn(List.of(s));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .titularTitulo("T1")
                .build();
        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r));

        Usuario titular = Usuario.builder().tituloEleitoral("T1").email("t@mail.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", titular));

        // Força erro no envio
        doThrow(new RuntimeException("Fail")).when(emailService).enviarEmailHtml(eq("op@tre-pe.jus.br"), anyString(), anyString());

        service.emailInicioProcesso(9L, List.of());

        // Verifica que o catch capturou a exceção e logou, mas tentou enviar
        verify(emailService).enviarEmailHtml(eq("op@tre-pe.jus.br"), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve cobrir intermediária sem subordinadas diretas filtradas")
    void deveCobrirIntermediariaSemSubordinadasFiltradas() {
        Processo processo = criarProcesso(10L);
        when(processoRepo.findByIdComParticipantes(10L)).thenReturn(Optional.of(processo));

        Unidade intermediaria = criarUnidade(1L, TipoUnidade.INTERMEDIARIA);
        intermediaria.setSigla("INT");

        // Unidade 'outra' sem superior (u.getUnidadeSuperior() == null)
        Unidade outra = criarUnidade(2L, TipoUnidade.OPERACIONAL);
        outra.setSigla("OUTRA");

        // Unidade 'comOutroSuperior' com superior diferente (u.getUnidadeSuperior() != null && !equals)
        Unidade outraIntermediaria = criarUnidade(3L, TipoUnidade.INTERMEDIARIA);
        Unidade comOutroSuperior = criarUnidade(4L, TipoUnidade.OPERACIONAL);
        comOutroSuperior.setUnidadeSuperior(outraIntermediaria);
        comOutroSuperior.setSigla("OUTRO_SUP");

        processo.adicionarParticipantes(Set.of(intermediaria, outra, comOutroSuperior));

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
                1L, UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build(),
                2L, UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build(),
                4L, UnidadeResponsavelDto.builder().unidadeCodigo(4L).titularTitulo("T4").build()
        ));

        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "T1", Usuario.builder().email("int@mail.com").build(),
                "T2", Usuario.builder().email("out@mail.com").build(),
                "T4", Usuario.builder().email("outro_sup@mail.com").build()
        ));
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(intermediaria, outra, comOutroSuperior));

        service.emailFinalizacaoProcesso(10L);

        // Deve enviar para operacionais
        verify(emailService).enviarEmailHtml(eq("outra@tre-pe.jus.br"), anyString(), any());
        verify(emailService).enviarEmailHtml(eq("outro_sup@tre-pe.jus.br"), anyString(), any());

        // Não deve enviar para intermediária pois não há subordinada que tenha ela como superior imediata
        verify(emailService, never()).enviarEmailHtml(eq("int@tre-pe.jus.br"), anyString(), any());
    }
    @Nested
    @DisplayName("Cobertura Extra Isolada")
    class CoberturaExtra {
        
        @Test
        @DisplayName("aoIniciarProcesso com unidade INTERMEDIARIA envia email apenas para titular se substituto ausente")
        void aoIniciarProcesso_Intermediaria() {
            when(emailModelosService.criarEmailProcessoIniciado(any(), any(), any(), any())).thenReturn("corpo");
            Long codProcesso = 1L;

            Processo processo = new Processo();
            processo.setDescricao("Processo Teste");
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            Unidade unidade = new Unidade();
            unidade.setCodigo(11L);
            unidade.setTipo(TipoUnidade.INTERMEDIARIA);
            unidade.setSituacao(SituacaoUnidade.ATIVA);
            unidade.setNome("Unidade Inter");
            unidade.setSigla("UI");

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setUnidade(unidade);
            subprocesso.setCodigo(200L);

            UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(11L)
                    .titularTitulo("TITULAR")
                    .substitutoTitulo("SUBSTITUTO") 
                    .build();
            
            Usuario usuarioTitular = Usuario.builder()
                    .tituloEleitoral("TITULAR")
                    .nome("nome")
                    .email("email@test.com")
                    .build();

            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
            when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(11L, resp));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("TITULAR", usuarioTitular));
            
            service.emailInicioProcesso(codProcesso, List.of());

            verify(emailService).enviarEmailHtml(eq("ui@tre-pe.jus.br"), anyString(), anyString());
        }

        @Test
        @DisplayName("aoIniciarProcesso deve capturar exceção ao enviar e-mail para substituto")
        void aoIniciarProcesso_ErroEmailSubstituto() {
            Long codProcesso = 1L;

            Processo processo = new Processo();
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            Unidade unidade = new Unidade();
            unidade.setCodigo(11L);
            unidade.setTipo(TipoUnidade.OPERACIONAL);
            unidade.setSituacao(SituacaoUnidade.ATIVA);
            unidade.setNome("Unidade");

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setUnidade(unidade);

            UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(11L)
                    .titularTitulo("TIT")
                    .substitutoTitulo("SUB") 
                    .build();
            
            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
            when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(11L, resp));
            
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(null);
            
            Assertions.assertThatCode(() -> service.emailInicioProcesso(codProcesso, List.of())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("enviarEmailParaSubstituto deve capturar exceção")
        void enviarEmailParaSubstituto_CatchException() {
            Assertions.assertThatCode(() -> 
                service.enviarEmailParaSubstituto("TIT", null, "assunto", "corpo", "unidade")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("aoIniciarProcesso quando responsavel da unidade nao encontrado deve retornar early")
        void aoIniciarProcesso_ResponsavelNaoEncontrado() {
            Long codProcesso = 1L;

            Processo processo = new Processo();
            processo.setDescricao("Processo");
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            Subprocesso subprocesso = new Subprocesso();
            Unidade unidade = criarUnidade(99L, TipoUnidade.OPERACIONAL);
            unidade.setSituacao(SituacaoUnidade.ATIVA);
            unidade.setSigla("U99");
            subprocesso.setUnidade(unidade);
            subprocesso.setCodigo(201L);

            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(subprocessoFacade.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));

            when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of());
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of());

            service.emailInicioProcesso(codProcesso, List.of());

            // Unidade criada acima com id 99 e sigla U99
            verify(emailService).enviarEmailHtml(eq("u99@tre-pe.jus.br"), anyString(), any());
        }

        @Test
        @DisplayName("aoFinalizarProcesso deve tratar unidades sem superiores ou superiores diferentes")
        void aoFinalizarProcesso_FiltragemSubordinadas() {
            Long codProcesso = 1L;

            Processo processo = new Processo();
            processo.setCodigo(codProcesso);
            processo.setDescricao("Processo");

            Unidade uInter = Unidade.builder().codigo(1L).tipo(TipoUnidade.INTERMEDIARIA)
                    .situacao(SituacaoUnidade.ATIVA).build();
            uInter.setSigla("UI");

            Unidade uSuperiorDiferente = Unidade.builder().codigo(9L).tipo(TipoUnidade.OPERACIONAL)
                    .situacao(SituacaoUnidade.ATIVA).build();

            Unidade uSubordinadaInvalida = Unidade.builder().codigo(2L).tipo(TipoUnidade.OPERACIONAL)
                    .situacao(SituacaoUnidade.ATIVA).build();
            uSubordinadaInvalida.setUnidadeSuperior(uSuperiorDiferente);

            processo.adicionarParticipantes(Set.of(uInter, uSubordinadaInvalida));

            UnidadeResponsavelDto rInter = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(1L).titularTitulo("T1").build();
            Usuario t1 = Usuario.builder().tituloEleitoral("T1").email("t1@test.com").build();

            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, rInter));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", t1));
            when(unidadeService.porCodigos(anyList())).thenReturn(List.of(uInter, uSubordinadaInvalida));

            service.emailFinalizacaoProcesso(codProcesso);

            verify(emailService, never()).enviarEmailHtml(eq("ui@tre-pe.jus.br"), anyString(), anyString());
        }

        @Test
        @DisplayName("criarCorpoEmailPorTipo deve disparar exceção para tipo SEM_EQUIPE (isolado)")
        void criarCorpoEmailPorTipo_Erro() {
            Processo p = new Processo();
            Subprocesso s = new Subprocesso();
            Assertions.assertThatThrownBy(() -> 
                service.criarCorpoEmailPorTipo(TipoUnidade.SEM_EQUIPE, p, s)
            ).isInstanceOf(IllegalArgumentException.class);
        }

    @Test
    @DisplayName("Deve notificar substituto na finalização do processo")
    void deveNotificarSubstitutoNaFinalizacao() {
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("corpo");
        Processo processo = criarProcesso(11L);
        when(processoRepo.findByIdComParticipantes(11L)).thenReturn(Optional.of(processo));

        Unidade u = criarUnidade(1L, TipoUnidade.OPERACIONAL);
        u.setSigla("OP");
        processo.adicionarParticipantes(Set.of(u));

        UnidadeResponsavelDto r = UnidadeResponsavelDto.builder()
                .unidadeCodigo(1L)
                .titularTitulo("T1")
                .substitutoTitulo("S1")
                .build();

        when(unidadeService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r));

        Usuario substituto = Usuario.builder().tituloEleitoral("S1").email("sub@mail.com").build();
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", Usuario.builder().build(), "S1", substituto));
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(u));

        service.emailFinalizacaoProcesso(11L);

        verify(emailService).enviarEmailHtml(eq("sub@mail.com"), anyString(), anyString());
    }
    }
}

package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.comum.erros.*;
import sgc.fixture.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Serviço de Notificação")
class ProcessoNotificacaoServiceTest {

    @Mock
    private AlertaFacade alertaService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailModelosService emailModelosService;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private ResponsavelUnidadeService responsavelService;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private SubprocessoService subprocessoService;

    @Mock
    private UsuarioFacade usuarioService;

    @InjectMocks
    private ProcessoNotificacaoService service;

    private Processo criarProcesso(Long id) {
        Processo p = new Processo();
        p.setCodigo(id);
        p.setDescricao("Processo de Teste");
        p.setTipo(TipoProcesso.MAPEAMENTO);
        return p;
    }

    @BeforeEach
    void setUp() {
    }

    private void mockUnidadeServiceBasico() {
        when(unidadeService.buscarPorId(anyLong())).thenAnswer(invocation -> {
            Long cod = invocation.getArgument(0);
            Unidade u = new Unidade();
            u.setCodigo(cod);
            u.setSigla("U" + cod);
            u.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
            return u;
        });
    }

    @Test
    @DisplayName("Deve enviar e-mail de início de processo para responsáveis e substitutos")
    void deveEnviarEmailInicioProcesso() {
        mockUnidadeServiceBasico();
        Processo processo = criarProcesso(1L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10"); // Ajustado para bater com o mock global do setUp
        unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(subprocesso));

        UnidadeResponsavelDto responsavel = UnidadeResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularTitulo("TITULAR")
                .titularNome("Titular")
                .substitutoTitulo("SUBSTITUTO")
                .substitutoNome("Substituto")
                .build();

        Usuario usuarioTitular = new Usuario();
        usuarioTitular.setEmail("titular@teste.com");
        Usuario usuarioSubstituto = new Usuario();
        usuarioSubstituto.setEmail("substituto@teste.com");

        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, responsavel));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
                "TITULAR", usuarioTitular,
                "SUBSTITUTO", usuarioSubstituto
        ));

        when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn("Conteúdo HTML");

        service.emailInicioProcesso(1L);

        verify(emailService, times(1)).enviarEmailHtml(eq("u10@tre-pe.jus.br"), anyString(), anyString());
        verify(emailService, times(1)).enviarEmailHtml(eq("substituto@teste.com"), anyString(), anyString());
        verify(alertaService, times(1)).criarAlertasProcessoIniciado(any(), anyList());
    }

    @Test
    @DisplayName("Deve ignorar processamento se não houver subprocessos al iniciar")
    void deveIgnorarProcessamentoSeNaoHouverSubprocessosAoIniciar() {
        Processo processo = criarProcesso(1L);
        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of());

        service.emailInicioProcesso(1L);

        verifyNoInteractions(emailService);
        verifyNoInteractions(alertaService);
    }

    @Test
    @DisplayName("Deve tratar exceção ao enviar e-mail e continuar processamento")
    void deveTratarExcecaoAoEnviarEmail() {
        mockUnidadeServiceBasico();
        Processo processo = criarProcesso(1L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        unidade.setSigla("U1");
        Subprocesso s = new Subprocesso();
        s.setUnidade(unidade);

        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
        when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, UnidadeResponsavelDto.builder().titularTitulo("T").build()));
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T", Usuario.builder().build()));
        when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn("HTML");

        doThrow(new RuntimeException("Erro SMTP")).when(emailService).enviarEmailHtml(any(), any(), any());

        service.emailInicioProcesso(1L);

        verify(emailService, atLeastOnce()).enviarEmailHtml(any(), any(), any());
        verify(alertaService).criarAlertasProcessoIniciado(any(), anyList());
    }

    @Test
    @DisplayName("Deve criar alertas mesmo se não enviar e-mails")
    void deveCriarAlertasMesmoSemEmails() {
        mockUnidadeServiceBasico();
        Processo processo = criarProcesso(1L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        Subprocesso s = new Subprocesso();
        s.setUnidade(unidade);

        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
        when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Collections.emptyMap());
        when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn("HTML");

        service.emailInicioProcesso(1L);

        verify(alertaService).criarAlertasProcessoIniciado(eq(processo), anyList());
    }

    @Test
    @DisplayName("Deve processar múltiplas unidades")
    void deveProcessarMultiplasUnidades() {
        mockUnidadeServiceBasico();
        Processo processo = criarProcesso(1L);
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        u1.setSigla("U1");
        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        u2.setSigla("U2");
        Unidade u3 = new Unidade();
        u3.setCodigo(3L);
        u3.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        u3.setSigla("U3");

        Subprocesso s1 = new Subprocesso();
        s1.setUnidade(u1);
        Subprocesso s2 = new Subprocesso();
        s2.setUnidade(u2);
        Subprocesso s3 = new Subprocesso();
        s3.setUnidade(u3);

        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s1, s2, s3));

        UnidadeResponsavelDto r1 = UnidadeResponsavelDto.builder().unidadeCodigo(1L).titularTitulo("T1").build();
        UnidadeResponsavelDto r2 = UnidadeResponsavelDto.builder().unidadeCodigo(2L).titularTitulo("T2").build();

        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(1L, r1, 2L, r2));
        when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn("HTML");

        Usuario user1 = new Usuario();
        user1.setEmail("u1@t.com");
        Usuario user2 = new Usuario();
        user2.setEmail("u2@t.com");
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("T1", user1, "T2", user2));

        service.emailInicioProcesso(1L);

        // Siglas nos e-mails seguem o padrão do mock U+cod (u1, u2, u3)
        verify(emailService, times(1)).enviarEmailHtml(eq("u1@tre-pe.jus.br"), anyString(), anyString());
        verify(emailService, times(1)).enviarEmailHtml(eq("u2@tre-pe.jus.br"), anyString(), anyString());
        verify(emailService, times(1)).enviarEmailHtml(eq("u3@tre-pe.jus.br"), anyString(), anyString());

        verify(alertaService, times(1)).criarAlertasProcessoIniciado(any(), anyList());
    }

    @Nested
    @DisplayName("Cobertura Extra")
    class CoberturaExtra {

        @Test
        @DisplayName("Deve lidar com map de responsáveis nulo ou incompleto")
        void deveLidarComMapResponsaveisIncompleto() {
            mockUnidadeServiceBasico();
            Processo p = criarProcesso(1L);
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
            u.setSigla("U1");
            Subprocesso s = new Subprocesso();
            s.setUnidade(u);

            when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(p));
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(s));
            when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Collections.emptyMap());
            when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                    .thenReturn("HTML");

            service.emailInicioProcesso(1L);

            verify(usuarioService).buscarUsuariosPorTitulos(anyList());
            verify(alertaService).criarAlertasProcessoIniciado(any(), anyList());
        }

        @Test
        @DisplayName("Deve enviar email para unidade específica (método auxiliar)")
        void deveEnviarEmailParaUnidade() {
            mockUnidadeServiceBasico();
            Long codProcesso = 11L;
            Processo processo = criarProcesso(codProcesso);
            Unidade unidade = new Unidade();
            unidade.setCodigo(11L);
            unidade.setSigla("U11");
            unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setUnidade(unidade);

            UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(11L)
                    .titularTitulo("TITULAR")
                    .build();

            Usuario usuarioTitular = new Usuario();
            usuarioTitular.setEmail("titular@mail.com");

            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
            when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(11L, resp));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("TITULAR", usuarioTitular));
            when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                    .thenReturn("HTML");

            service.emailInicioProcesso(codProcesso);

            verify(emailService).enviarEmailHtml(eq("u11@tre-pe.jus.br"), any(), any());
        }

        @Test
        @DisplayName("Deve enviar email para substituto se houver")
        void deveEnviarEmailSubstituto() {
            mockUnidadeServiceBasico();
            Long codProcesso = 12L;
            Processo processo = criarProcesso(codProcesso);
            Unidade unidade = new Unidade();
            unidade.setCodigo(12L);
            unidade.setSigla("U12");
            unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setUnidade(unidade);

            UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(12L)
                    .titularTitulo("TITULAR")
                    .substitutoTitulo("SUBSTITUTO")
                    .build();

            Usuario usuarioSub = new Usuario();
            usuarioSub.setEmail("sub@mail.com");

            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
            when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(12L, resp));
            when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("SUBSTITUTO", usuarioSub));
            when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                    .thenReturn("HTML");

            service.emailInicioProcesso(codProcesso);

            verify(emailService).enviarEmailHtml(eq("sub@mail.com"), any(), any());
        }
    }

    @Nested
    @DisplayName("Envio de Lembrete")
    class EnvioLembrete {

        @Test
        @DisplayName("Deve enviar lembrete com sucesso formatando data")
        void deveEnviarLembrete() {
            Long codProcesso = 1L;
            Long codUnidade = 10L;
            LocalDateTime dataLimite = LocalDateTime.of(2026, 3, 15, 23, 59);

            Processo processo = new Processo();
            processo.setCodigo(codProcesso);
            processo.setDescricao("Processo com prazo");
            processo.setDataLimite(dataLimite);
            Unidade unidade = UnidadeFixture.unidadeComId(codUnidade);
            unidade.setSigla("U1");
            unidade.setTituloTitular("T1");
            processo.adicionarParticipantes(Set.of(unidade));

            Subprocesso subprocesso = Subprocesso.builder().codigo(99L).build();
            Usuario titular = new Usuario();
            titular.setEmail("titular@teste.com");

            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarPorId(codUnidade)).thenReturn(unidade);
            when(subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade)).thenReturn(subprocesso);
            when(usuarioService.buscarPorLogin("T1")).thenReturn(titular);
            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("HTML");

            service.enviarLembrete(codProcesso, codUnidade);

            verify(alertaService).criarAlertaAdmin(eq(processo), eq(unidade), contains("15/03/2026"));
            verify(subprocessoService).registrarMovimentacaoLembrete(99L);
            verify(emailService).enviarEmailHtml(eq("titular@teste.com"), contains("SGC: Lembrete de prazo"), anyString());
        }

        @Test
        @DisplayName("Deve lançar ErroValidacao quando unidade não participa")
        void deveLancarErroQuandoUnidadeNaoParticipa() {
            Long codProcesso = 1L;
            Long codUnidade = 99L;

            Processo processo = new Processo();
            processo.setCodigo(codProcesso);
            processo.setDescricao("Processo");
            Unidade outra = UnidadeFixture.unidadeComId(2L);
            processo.adicionarParticipantes(Set.of(outra));

            when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarPorId(codUnidade)).thenReturn(UnidadeFixture.unidadeComId(codUnidade));

            assertThatThrownBy(() -> service.enviarLembrete(codProcesso, codUnidade))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("não participa");
        }
    }
}
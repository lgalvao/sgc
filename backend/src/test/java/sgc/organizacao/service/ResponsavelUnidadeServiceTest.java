package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.comum.config.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResponsavelUnidadeService")
class ResponsavelUnidadeServiceTest {
    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Mock
    private CacheViewsOrganizacaoService cacheViewsOrganizacaoService;

    @Mock
    private CacheOrganizacaoService cacheOrganizacaoService;

    @Mock
    private AlertaAplicacaoService alertaAplicacaoService;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private EmailModelosService emailModelosService;

    @Mock
    private ConfigAplicacao configAplicacao;

    @Mock
    private ComumRepo repo;

    @Spy
    private sgc.organizacao.OrganizacaoDtoMapper organizacaoDtoMapper = new sgc.organizacao.OrganizacaoDtoMapper();

    @InjectMocks
    private ResponsavelUnidadeService service;

    @Test
    @DisplayName("todasPossuemResponsavelEfetivo deve retornar false para responsável com título em branco")
    void todasPossuemResponsavelEfetivoDeveRetornarFalseComTituloEmBranco() {
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades())
                .thenReturn(List.of(new ResponsabilidadeLeitura(10L, " ")));

        boolean resultado = service.todasPossuemResponsavelEfetivo(List.of(10L));

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("todasPossuemResponsavelEfetivo deve retornar false quando faltar unidade na resposta")
    void todasPossuemResponsavelEfetivoDeveRetornarFalseQuandoFaltarUnidade() {
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades())
                .thenReturn(List.of(new ResponsabilidadeLeitura(10L, "RESP")));

        boolean resultado = service.todasPossuemResponsavelEfetivo(List.of(10L, 11L));

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("todasPossuemResponsavelEfetivo deve retornar true quando lista estiver vazia")
    void todasPossuemResponsavelEfetivoDeveRetornarTrueQuandoListaVazia() {
        boolean resultado = service.todasPossuemResponsavelEfetivo(List.of());
        assertThat(resultado).isTrue();
    }

    @Nested
    @DisplayName("Buscar atribuições")
    class BuscarAtribuicoesTests {

        @Test
        @DisplayName("Deve buscar todas as atribuições temporárias")
        void deveBuscarTodasAtribuicoes() {

            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("UNIT");

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123456789012");
            usuario.setNome("Usuario Teste");

            AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
            atribuicao.setCodigo(1L);
            atribuicao.setUnidade(unidade);
            atribuicao.setUsuarioTitulo("123456789012");
            atribuicao.setDataInicio(LocalDateTime.now());
            atribuicao.setDataTermino(LocalDateTime.now().plusDays(1));
            atribuicao.setJustificativa("Teste");

            when(atribuicaoTemporariaRepo.listarTodasComUnidade()).thenReturn(List.of(atribuicao));
            when(usuarioRepo.listarPorTitulosComUnidadeLotacao(List.of("123456789012"))).thenReturn(List.of(usuario));

            List<AtribuicaoDto> resultado = service.buscarTodasAtribuicoes();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().unidadeCodigo()).isEqualTo(10L);
            assertThat(resultado.getFirst().usuario().tituloEleitoral()).isEqualTo(usuario.getTituloEleitoral());
        }

        @Test
        @DisplayName("Deve carregar usuários em lote ao buscar atribuições temporárias")
        void deveCarregarUsuariosEmLoteAoBuscarAtribuicoes() {
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("UNIT");

            AtribuicaoTemporaria atribuicao1 = new AtribuicaoTemporaria();
            atribuicao1.setCodigo(1L);
            atribuicao1.setUnidade(unidade);
            atribuicao1.setUsuarioTitulo("111");
            atribuicao1.setDataInicio(LocalDateTime.now());
            atribuicao1.setDataTermino(LocalDateTime.now().plusDays(1));

            AtribuicaoTemporaria atribuicao2 = new AtribuicaoTemporaria();
            atribuicao2.setCodigo(2L);
            atribuicao2.setUnidade(unidade);
            atribuicao2.setUsuarioTitulo("222");
            atribuicao2.setDataInicio(LocalDateTime.now());
            atribuicao2.setDataTermino(LocalDateTime.now().plusDays(1));

            Usuario usuario1 = new Usuario();
            usuario1.setTituloEleitoral("111");
            Usuario usuario2 = new Usuario();
            usuario2.setTituloEleitoral("222");

            when(atribuicaoTemporariaRepo.listarTodasComUnidade()).thenReturn(List.of(atribuicao1, atribuicao2));
            when(usuarioRepo.listarPorTitulosComUnidadeLotacao(List.of("111", "222")))
                    .thenReturn(List.of(usuario1, usuario2));

            service.buscarTodasAtribuicoes();

            verify(usuarioRepo).listarPorTitulosComUnidadeLotacao(List.of("111", "222"));
            verify(usuarioRepo).listarPorTitulosComUnidadeLotacao(List.of("111", "222"));
        }

        @Test
        @DisplayName("Deve buscar atribuições por unidade")
        void deveBuscarAtribuicoesPorUnidade() {
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("UNIT");

            AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
            atribuicao.setCodigo(1L);
            atribuicao.setUnidade(unidade);
            atribuicao.setUsuarioTitulo("123");
            atribuicao.setDataInicio(LocalDateTime.now());
            atribuicao.setDataTermino(LocalDateTime.now().plusDays(1));

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");

            when(atribuicaoTemporariaRepo.listarPorUnidadeComUnidade(10L)).thenReturn(List.of(atribuicao));
            when(usuarioRepo.listarPorTitulosComUnidadeLotacao(List.of("123"))).thenReturn(List.of(usuario));

            List<AtribuicaoDto> resultado = service.buscarAtribuicoesPorUnidade(10L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().codigo()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Criar atribuição temporária")
    class CriarAtribuicaoTemporariaTests {

        @Test
        @DisplayName("Deve criar atribuição com dataInicio explícita")
        void deveCriarAtribuicaoComDataInicioExplicita() {

            Long codUnidade = 1L;
            LocalDate dataInicio = LocalDate.of(2024, 1, 15);
            LocalDate dataTermino = LocalDate.of(2024, 2, 15);

            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                    "123456789012",
                    dataInicio,
                    dataTermino,
                    "Cobertura de férias"
            );

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);
            unidade.setSigla("UNIT");

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123456789012");
            usuario.setMatricula("12345678");
            usuario.setNome("Usuario Teste");
            usuario.setEmail("usuario@tre-pe.jus.br");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            Alerta alerta = Alerta.builder().codigo(10L).usuarioDestinoTitulo(usuario.getTituloEleitoral()).build();
            when(alertaAplicacaoService.criarAlertaPessoal(
                    usuario.getTituloEleitoral(),
                    "Atribuição temporária para unidade UNIT"
            )).thenReturn(alerta);
            when(emailModelosService.criarEmailAtribuicaoTemporaria(any()))
                    .thenReturn("<html>email</html>");
            when(configAplicacao.isAmbienteTestes()).thenReturn(true);
            when(configAplicacao.getUrlAcessoHom()).thenReturn("http://localhost:5173");

            service.criarAtribuicaoTemporaria(codUnidade, request);

            ArgumentCaptor<AtribuicaoTemporaria> captor = ArgumentCaptor.forClass(AtribuicaoTemporaria.class);
            verify(atribuicaoTemporariaRepo).save(captor.capture());
            verify(cacheOrganizacaoService).invalidarAposCommit();

            AtribuicaoTemporaria atribuicao = captor.getValue();
            assertThat(atribuicao.getUnidade()).isEqualTo(unidade);
            assertThat(atribuicao.getUsuarioTitulo()).isEqualTo("123456789012");
            assertThat(atribuicao.getUsuarioMatricula()).isEqualTo("12345678");
            assertThat(atribuicao.getDataInicio()).isEqualTo(dataInicio.atStartOfDay());
            assertThat(atribuicao.getDataTermino()).isEqualTo(dataTermino.atTime(23, 59, 59));
            assertThat(atribuicao.getJustificativa()).isEqualTo("Cobertura de férias");

            ArgumentCaptor<EmailModelosService.EmailAtribuicaoTemporariaCommand> emailCaptor =
                    ArgumentCaptor.forClass(EmailModelosService.EmailAtribuicaoTemporariaCommand.class);
            verify(emailModelosService).criarEmailAtribuicaoTemporaria(emailCaptor.capture());
            assertThat(emailCaptor.getValue().assunto()).isEqualTo("SGC: Atribuição de perfil CHEFE na unidade UNIT");
            assertThat(emailCaptor.getValue().nomeServidor()).isEqualTo("Usuario Teste");
            assertThat(emailCaptor.getValue().siglaUnidade()).isEqualTo("UNIT");
            assertThat(emailCaptor.getValue().dataInicio()).isEqualTo(dataInicio.atStartOfDay());
            assertThat(emailCaptor.getValue().dataTermino()).isEqualTo(dataTermino.atTime(23, 59, 59));
            assertThat(emailCaptor.getValue().justificativa()).isEqualTo("Cobertura de férias");
            assertThat(emailCaptor.getValue().urlSistema()).isEqualTo("http://localhost:5173");

            ArgumentCaptor<EnfileirarNotificacaoCommand> commandCaptor =
                    ArgumentCaptor.forClass(EnfileirarNotificacaoCommand.class);
            verify(notificacaoService).enfileirar(commandCaptor.capture());
            EnfileirarNotificacaoCommand command = commandCaptor.getValue();
            assertThat(command.subprocesso()).isNull();
            assertThat(command.tipoNotificacao()).isEqualTo(TipoNotificacao.ATRIBUICAO_TEMPORARIA);
            assertThat(command.usuarioDestinoTitulo()).isEqualTo("123456789012");
            assertThat(command.destinatario()).isEqualTo("usuario@tre-pe.jus.br");
            assertThat(command.assunto()).isEqualTo("SGC: Atribuição de perfil CHEFE na unidade UNIT");
            assertThat(command.corpoHtml()).isEqualTo("<html>email</html>");
            assertThat(command.chaveIdempotencia()).contains("atribuicao-temporaria:unidade:1:usuario:123456789012");
        }

        @Test
        @DisplayName("Deve criar atribuição com dataInicio nula (assume hoje)")
        void deveCriarAtribuicaoComDataInicioNula() {
            Long codUnidade = 1L;
            LocalDate dataTermino = LocalDate.now().plusDays(10);
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", null, dataTermino, "Justificativa");

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);
            unidade.setSigla("UNIT");
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");
            usuario.setNome("Usuario Teste");
            usuario.setEmail("usuario@tre-pe.jus.br");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(alertaAplicacaoService.criarAlertaPessoal(eq("123"), anyString()))
                    .thenReturn(Alerta.builder().codigo(10L).build());
            when(emailModelosService.criarEmailAtribuicaoTemporaria(any()))
                    .thenReturn("<html>email</html>");

            service.criarAtribuicaoTemporaria(codUnidade, request);

            ArgumentCaptor<AtribuicaoTemporaria> captor = ArgumentCaptor.forClass(AtribuicaoTemporaria.class);
            verify(atribuicaoTemporariaRepo).save(captor.capture());
            assertThat(captor.getValue().getDataInicio()).isNotNull();
            verify(notificacaoService).enfileirar(any());
        }

        @Test
        @DisplayName("Deve enfileirar email mesmo quando criacao de alerta falhar")
        void deveEnfileirarEmailMesmoQuandoCriacaoDeAlertaFalhar() {
            Long codUnidade = 1L;
            LocalDate dataTermino = LocalDate.now().plusDays(5);
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", null, dataTermino, "Justificativa");

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);
            unidade.setSigla("UNIT");

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");
            usuario.setNome("Usuario Teste");
            usuario.setEmail("usuario@tre-pe.jus.br");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doThrow(new IllegalStateException("falha alerta"))
                    .when(alertaAplicacaoService).criarAlertaPessoal(eq("123"), anyString());
            when(emailModelosService.criarEmailAtribuicaoTemporaria(any()))
                    .thenReturn("<html>email</html>");

            service.criarAtribuicaoTemporaria(codUnidade, request);

            verify(notificacaoService).enfileirar(any());
            verify(cacheOrganizacaoService).invalidarAposCommit();
        }

        @Test
        @DisplayName("Deve rejeitar atribuição temporária para usuário sem e-mail")
        void deveRejeitarAtribuicaoTemporariaParaUsuarioSemEmail() {
            Long codUnidade = 1L;
            LocalDate dataTermino = LocalDate.now().plusDays(5);
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", null, dataTermino, "Justificativa");

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);
            unidade.setSigla("UNIT");

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");
            usuario.setNome("Usuario Teste");
            usuario.setEmail(" ");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage(Mensagens.USUARIO_SEM_EMAIL);

            verifyNoInteractions(alertaAplicacaoService);
            verifyNoInteractions(emailModelosService);
            verifyNoInteractions(notificacaoService);
        }

        @Test
        @DisplayName("Deve rejeitar atribuição quando dataTermino for anterior ao inicio")
        void deveRejeitarAtribuicaoComDataTerminoAnteriorAoInicio() {
            Long codUnidade = 1L;
            LocalDate dataInicio = LocalDate.of(2024, 2, 10);
            LocalDate dataTermino = LocalDate.of(2024, 2, 9);
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", dataInicio, dataTermino, "Justificativa");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(new Unidade());
            when(repo.buscar(Usuario.class, "123")).thenReturn(new Usuario().setTituloEleitoral("123"));

            assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                    .isInstanceOf(ErroValidacao.class);

            verify(atribuicaoTemporariaRepo, never()).save(any());
            verifyNoInteractions(cacheOrganizacaoService);
            verifyNoInteractions(alertaAplicacaoService);
            verifyNoInteractions(notificacaoService);
        }

        @Test
        @DisplayName("Deve rejeitar atribuição com período sobreposto")
        void deveRejeitarAtribuicaoComPeriodoSobreposto() {
            Long codUnidade = 1L;
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                    "123",
                    LocalDate.of(2026, 5, 13),
                    LocalDate.of(2026, 5, 30),
                    "Justificativa"
            );

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);
            Usuario usuario = new Usuario().setTituloEleitoral("123");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.existeSobreposicaoPeriodo(
                    eq(codUnidade),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    isNull()
            )).thenReturn(true);

            assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage(Mensagens.ATRIBUICAO_TEMPORARIA_SOBREPOSTA);
        }

        @Test
        @DisplayName("Deve criar atribuição e usar URL de produção quando não for ambiente de testes")
        void deveCriarAtribuicaoEUsarUrlDeProducao() {
            Long codUnidade = 1L;
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                    "123",
                    LocalDate.of(2026, 5, 13),
                    LocalDate.of(2026, 5, 30),
                    "Justificativa"
            );

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);
            unidade.setSigla("UNIT");
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");
            usuario.setNome("Usuario Teste");
            usuario.setEmail("usuario@tre-pe.jus.br");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(alertaAplicacaoService.criarAlertaPessoal(eq("123"), anyString()))
                    .thenReturn(Alerta.builder().codigo(10L).build());
            when(emailModelosService.criarEmailAtribuicaoTemporaria(any()))
                    .thenReturn("<html>email</html>");

            when(configAplicacao.isAmbienteTestes()).thenReturn(false);
            when(configAplicacao.getUrlAcessoProd()).thenReturn("https://sgc.tre-pe.jus.br");

            service.criarAtribuicaoTemporaria(codUnidade, request);

            ArgumentCaptor<EmailModelosService.EmailAtribuicaoTemporariaCommand> emailCaptor =
                    ArgumentCaptor.forClass(EmailModelosService.EmailAtribuicaoTemporariaCommand.class);
            verify(emailModelosService).criarEmailAtribuicaoTemporaria(emailCaptor.capture());
            assertThat(emailCaptor.getValue().urlSistema()).isEqualTo("https://sgc.tre-pe.jus.br");
        }

        @Test
        @DisplayName("Deve usar URL padrão quando URL de produção for nula ou em branco")
        void deveUsarUrlPadraoQuandoUrlDeProducaoForNulaOuEmBranco() {
            Long codUnidade = 1L;
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                    "123",
                    LocalDate.of(2026, 5, 13),
                    LocalDate.of(2026, 5, 30),
                    "Justificativa"
            );

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);
            unidade.setSigla("UNIT");
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");
            usuario.setNome("Usuario Teste");
            usuario.setEmail("usuario@tre-pe.jus.br");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(alertaAplicacaoService.criarAlertaPessoal(eq("123"), anyString()))
                    .thenReturn(Alerta.builder().codigo(10L).build());
            when(emailModelosService.criarEmailAtribuicaoTemporaria(any()))
                    .thenReturn("<html>email</html>");

            when(configAplicacao.isAmbienteTestes()).thenReturn(false);
            when(configAplicacao.getUrlAcessoProd()).thenReturn("   ");

            service.criarAtribuicaoTemporaria(codUnidade, request);

            ArgumentCaptor<EmailModelosService.EmailAtribuicaoTemporariaCommand> emailCaptor =
                    ArgumentCaptor.forClass(EmailModelosService.EmailAtribuicaoTemporariaCommand.class);
            verify(emailModelosService).criarEmailAtribuicaoTemporaria(emailCaptor.capture());
            assertThat(emailCaptor.getValue().urlSistema()).isEqualTo("http://localhost:5173");
        }
    }

    @Nested
    @DisplayName("Atualizar e remover atribuição temporária")
    class AtualizarERemoverAtribuicaoTemporariaTests {

        @Test
        @DisplayName("Deve atualizar atribuição existente")
        void deveAtualizarAtribuicaoExistente() {
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");
            usuario.setMatricula("MAT1");

            AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
            atribuicao.setCodigo(9L);
            atribuicao.setUnidade(unidade);

            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                    "123",
                    LocalDate.of(2026, 5, 13),
                    LocalDate.of(2026, 5, 30),
                    "Atualizada"
            );

            when(repo.buscar(Unidade.class, 1L)).thenReturn(unidade);
            when(repo.buscar(AtribuicaoTemporaria.class, 9L)).thenReturn(atribuicao);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);
            when(atribuicaoTemporariaRepo.save(atribuicao)).thenReturn(atribuicao);

            service.atualizarAtribuicaoTemporaria(1L, 9L, request);

            assertThat(atribuicao.getUsuarioTitulo()).isEqualTo("123");
            assertThat(atribuicao.getJustificativa()).isEqualTo("Atualizada");
            verify(cacheOrganizacaoService).invalidarAposCommit();
        }

        @Test
        @DisplayName("Deve remover atribuição existente")
        void deveRemoverAtribuicaoExistente() {
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);

            AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
            atribuicao.setCodigo(9L);
            atribuicao.setUnidade(unidade);

            when(repo.buscar(AtribuicaoTemporaria.class, 9L)).thenReturn(atribuicao);

            service.removerAtribuicaoTemporaria(1L, 9L);

            verify(atribuicaoTemporariaRepo).delete(atribuicao);
            verify(cacheOrganizacaoService).invalidarAposCommit();
        }

        @Test
        @DisplayName("Deve rejeitar atualização de atribuição quando pertencer a outra unidade")
        void deveRejeitarAtualizacaoDeAtribuicaoQuandoPertencerAOutraUnidade() {
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);

            Unidade unidadeDiferente = new Unidade();
            unidadeDiferente.setCodigo(2L);

            AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
            atribuicao.setCodigo(9L);
            atribuicao.setUnidade(unidadeDiferente);

            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                    "123",
                    LocalDate.of(2026, 5, 13),
                    LocalDate.of(2026, 5, 30),
                    "Atualizada"
            );

            when(repo.buscar(Unidade.class, 1L)).thenReturn(unidade);
            when(repo.buscar(AtribuicaoTemporaria.class, 9L)).thenReturn(atribuicao);

            assertThatThrownBy(() -> service.atualizarAtribuicaoTemporaria(1L, 9L, request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("A atribuição temporária não pertence à unidade informada.");

            verify(atribuicaoTemporariaRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar responsável atual")
    class BuscarResponsavelAtualTests {

        @Test
        @DisplayName("Deve buscar responsável atual com sucesso")
        void deveBuscarResponsavelAtualComSucesso() {

            String siglaUnidade = "ABC";

            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla(siglaUnidade);

            Responsabilidade resp = new Responsabilidade();
            resp.setUnidadeCodigo(1L);
            resp.setUsuarioTitulo("123456789012");

            Usuario usuarioCompleto = new Usuario();
            usuarioCompleto.setTituloEleitoral("123456789012");
            usuarioCompleto.setNome("João silva");

            when(unidadeRepo.buscarCodigoAtivoPorSigla(siglaUnidade)).thenReturn(Optional.of(1L));
            when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(1L))
                    .thenReturn(Optional.of(new ResponsabilidadeUnidadeLeitura(1L, "123456789012", null, null, null, null)));
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuarioCompleto);

            Usuario resultado = service.buscarResponsavelAtual(siglaUnidade);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo("123456789012");
        }
    }

    @Nested
    @DisplayName("Buscar responsável de Unidade")
    class BuscarResponsavelUnidadeTests {

        @Test
        @DisplayName("Deve buscar responsável com titular e substituto")
        void deveBuscarResponsavelComTitularESubstituto() {

            Long unidadeCodigo = 1L;

            ResponsabilidadeUnidadeResumoLeitura resumo = new ResponsabilidadeUnidadeResumoLeitura(
                    unidadeCodigo, "222222222222", "Maria santos", "111111111111", "João silva"
            );
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(unidadeCodigo)))
                    .thenReturn(List.of(resumo));

            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            assertThat(resultado).isNotNull();
            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.substitutoTitulo()).isEqualTo("222222222222");
        }

        @Test
        @DisplayName("Deve buscar responsável quando titular oficial é o próprio responsável")
        void deveBuscarResponsavelQuandoTitularEhOProprioResponsavel() {
            Long unidadeCodigo = 1L;

            ResponsabilidadeUnidadeResumoLeitura resumo = new ResponsabilidadeUnidadeResumoLeitura(
                    unidadeCodigo, "111111111111", "João Silva", "111111111111", "João Silva"
            );
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(unidadeCodigo)))
                    .thenReturn(List.of(resumo));

            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.substitutoTitulo()).isNull();
            assertThat(resultado.substitutoNome()).isNull();
        }
    }

    @Nested
    @DisplayName("Buscar responsáveis em lote")
    class BuscarResponsaveisEmLoteTests {

        @Test
        @DisplayName("Deve retornar mapa vazio quando lista de unidades vazia")
        void deveRetornarVazioQuandoListaVazia() {
            var result = service.buscarResponsaveisUnidades(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar mapa vazio quando nenhuma responsabilidade encontrada")
        void deveRetornarVazioQuandoNaoEncontrado() {
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList())).thenReturn(List.of());
            var result = service.buscarResponsaveisUnidades(List.of(1L));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar exceção quando responsável não é encontrado no repositório de usuários")
        void deveLancarExcecaoQuandoUsuarioAusente() {
            Long codUnidade = 1L;
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeResumoLeitura(codUnidade, "RESP", null, "TITULAR", "Titular")));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("Responsável ou titular oficial ausente");
        }

        @Test
        @DisplayName("Deve lançar exceção quando apenas o titular oficial estiver ausente")
        void deveLancarExcecaoQuandoTitularOficialAusente() {
            Long codUnidade = 1L;
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeResumoLeitura(codUnidade, "RESP", "Responsavel", "TITULAR", null)));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("Responsável ou titular oficial ausente");
        }

        @Test
        @DisplayName("Deve retornar mapa com responsáveis quando tudo ok")
        void deveRetornarResponsaveisComSucesso() {
            Long codUnidade = 1L;
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeResumoLeitura(codUnidade, "RES", "Responsavel", "TIT", "Titular")));

            var result = service.buscarResponsaveisUnidades(List.of(codUnidade));

            assertThat(result).hasSize(1);
            assertThat(result.get(codUnidade).titularTitulo()).isEqualTo("TIT");
            assertThat(result.get(codUnidade).substitutoTitulo()).isEqualTo("RES");
        }

        @Test
        @DisplayName("Deve lançar exceção quando titular oficial estiver nulo")
        void deveLancarExcecaoQuandoTitularOficialNulo() {
            Long codUnidade = 2L;
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeResumoLeitura(codUnidade, "RESP", "Responsavel", null, null)));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("Titular oficial ausente");
        }

        @Test
        @DisplayName("Deve lançar exceção quando titular oficial estiver em branco")
        void deveLancarExcecaoQuandoTitularOficialEmBranco() {
            Long codUnidade = 3L;
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeResumoLeitura(codUnidade, "RESP", "Responsavel", "   ", null)));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("Titular oficial ausente");
        }
    }

    @Test
    @DisplayName("buscarTodasAtribuicoes - deve retornar lista vazia quando não houver títulos")
    void buscarTodasAtribuicoes_SemTitulos() {
        when(atribuicaoTemporariaRepo.listarTodasComUnidade()).thenReturn(List.of());

        List<AtribuicaoDto> result = service.buscarTodasAtribuicoes();

        assertThat(result).isEmpty();
        verify(usuarioRepo, never()).listarPorTitulosComUnidadeLotacao(anyList());
    }

    @Test
    @DisplayName("buscarTodasAtribuicoes - deve lançar IllegalStateException quando usuário estiver ausente")
    void buscarTodasAtribuicoes_UsuarioAusente() {
        AtribuicaoTemporaria at = new AtribuicaoTemporaria();
        at.setCodigo(1L);
        at.setUsuarioTitulo("123456789012");
        when(atribuicaoTemporariaRepo.listarTodasComUnidade()).thenReturn(List.of(at));
        when(usuarioRepo.listarPorTitulosComUnidadeLotacao(anyList())).thenReturn(List.of());

        assertThatThrownBy(() -> service.buscarTodasAtribuicoes())
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("Usuário ausente");
    }

    @Test
    @DisplayName("buscarResponsabilidadeDetalhadaAtual - deve buscar por sigla")
    void buscarResponsabilidadeDetalhadaAtual_Sigla() {
        String sigla = "U10";
        Long codigo = 10L;
        when(unidadeRepo.buscarCodigoAtivoPorSigla(sigla)).thenReturn(Optional.of(codigo));

        ResponsabilidadeUnidadeLeitura leitura = mock(ResponsabilidadeUnidadeLeitura.class);
        when(leitura.usuarioTitulo()).thenReturn("123");
        when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(codigo)).thenReturn(Optional.of(leitura));
        when(repo.buscar(Usuario.class, "123")).thenReturn(new Usuario());

        service.buscarResponsabilidadeDetalhadaAtual(sigla);

        verify(unidadeRepo).buscarCodigoAtivoPorSigla(sigla);
    }

    @Test
    @DisplayName("buscarResponsavelUnidade - deve lançar erro quando não houver responsável")
    void buscarResponsavelUnidade_NaoEncontrado() {
        Long codigo = 10L;
        when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(codigo))).thenReturn(List.of());

        assertThatThrownBy(() -> service.buscarResponsavelUnidade(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarResponsavelUnidadeOpt - deve retornar Optional.empty() quando não houver responsável")
    void buscarResponsavelUnidadeOpt_NaoEncontrado() {
        Long codigo = 10L;
        when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(codigo))).thenReturn(List.of());

        Optional<UnidadeResponsavelDto> result = service.buscarResponsavelUnidadeOpt(codigo);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("buscarResponsavelUnidadeOpt - deve retornar o responsável com sucesso")
    void buscarResponsavelUnidadeOpt_ComSucesso() {
        Long codigo = 10L;
        ResponsabilidadeUnidadeResumoLeitura resumo = new ResponsabilidadeUnidadeResumoLeitura(
                codigo, "222", "Maria", "111", "João"
        );
        when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(codigo))).thenReturn(List.of(resumo));

        Optional<UnidadeResponsavelDto> result = service.buscarResponsavelUnidadeOpt(codigo);
        assertThat(result).isPresent();
        assertThat(result.get().titularTitulo()).isEqualTo("111");
        assertThat(result.get().substitutoTitulo()).isEqualTo("222");
    }

    @Test
    @DisplayName("buscarResponsavelAtual - deve retornar nulo quando não houver responsabilidade cadastrada")
    void buscarResponsavelAtual_SemResponsabilidade() {
        String sigla = "U1";
        when(unidadeRepo.buscarCodigoAtivoPorSigla(sigla)).thenReturn(Optional.of(1L));
        when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(1L)).thenReturn(Optional.empty());

        Usuario result = service.buscarResponsavelAtual(sigla);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("buscarResponsavelAtual - deve lançar ErroEntidadeNaoEncontrada se usuário correspondente não existir")
    void buscarResponsavelAtual_UsuarioNaoEncontrado() {
        String sigla = "U1";
        ResponsabilidadeUnidadeLeitura leitura = mock(ResponsabilidadeUnidadeLeitura.class);
        when(leitura.usuarioTitulo()).thenReturn("123");
        when(unidadeRepo.buscarCodigoAtivoPorSigla(sigla)).thenReturn(Optional.of(1L));
        when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(1L)).thenReturn(Optional.of(leitura));
        when(repo.buscar(Usuario.class, "123")).thenThrow(new ErroEntidadeNaoEncontrada(Usuario.class.getSimpleName(), "123"));

        assertThatThrownBy(() -> service.buscarResponsavelAtual(sigla))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarResponsabilidadeDetalhadaAtual por sigla - deve retornar nulo quando unidade não for encontrada")
    void buscarResponsabilidadeDetalhadaAtual_SiglaInexistente() {
        String sigla = "U999";
        when(unidadeRepo.buscarCodigoAtivoPorSigla(sigla)).thenReturn(Optional.empty());

        ResponsavelDto result = service.buscarResponsabilidadeDetalhadaAtual(sigla);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("buscarResponsabilidadeDetalhadaAtual por código - deve retornar nulo quando não houver responsabilidade")
    void buscarResponsabilidadeDetalhadaAtual_CodigoSemResponsabilidade() {
        Long codigo = 1L;
        when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(codigo)).thenReturn(Optional.empty());

        ResponsavelDto result = service.buscarResponsabilidadeDetalhadaAtual(codigo);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("buscarResponsabilidadeDetalhadaAtual por código - deve lançar ErroEntidadeNaoEncontrada se usuário correspondente não existir")
    void buscarResponsabilidadeDetalhadaAtual_CodigoUsuarioNaoEncontrado() {
        Long codigo = 1L;
        ResponsabilidadeUnidadeLeitura leitura = mock(ResponsabilidadeUnidadeLeitura.class);
        when(leitura.usuarioTitulo()).thenReturn("123");
        when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(codigo)).thenReturn(Optional.of(leitura));
        when(repo.buscar(Usuario.class, "123")).thenThrow(new ErroEntidadeNaoEncontrada(Usuario.class.getSimpleName(), "123"));

        assertThatThrownBy(() -> service.buscarResponsabilidadeDetalhadaAtual(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("criarAtribuicaoTemporaria - deve lançar ErroEntidadeNaoEncontrada se unidade não for encontrada")
    void criarAtribuicaoTemporaria_UnidadeNaoEncontrada() {
        Long codUnidade = 999L;
        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", LocalDate.now(), LocalDate.now().plusDays(1), "Justificativa");
        when(repo.buscar(Unidade.class, codUnidade)).thenThrow(new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), codUnidade));

        assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("criarAtribuicaoTemporaria - deve lançar ErroEntidadeNaoEncontrada se usuário não for encontrado")
    void criarAtribuicaoTemporaria_UsuarioNaoEncontrado() {
        Long codUnidade = 1L;
        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", LocalDate.now(), LocalDate.now().plusDays(1), "Justificativa");
        when(repo.buscar(Unidade.class, codUnidade)).thenReturn(new Unidade());
        when(repo.buscar(Usuario.class, "123")).thenThrow(new ErroEntidadeNaoEncontrada(Usuario.class.getSimpleName(), "123"));

        assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("atualizarAtribuicaoTemporaria - deve lançar ErroEntidadeNaoEncontrada se unidade não for encontrada")
    void atualizarAtribuicaoTemporaria_UnidadeNaoEncontrada() {
        Long codUnidade = 999L;
        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", LocalDate.now(), LocalDate.now().plusDays(1), "Justificativa");
        when(repo.buscar(Unidade.class, codUnidade)).thenThrow(new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), codUnidade));

        assertThatThrownBy(() -> service.atualizarAtribuicaoTemporaria(codUnidade, 1L, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("atualizarAtribuicaoTemporaria - deve lançar ErroEntidadeNaoEncontrada se atribuição não for encontrada")
    void atualizarAtribuicaoTemporaria_AtribuicaoNaoEncontrada() {
        Long codUnidade = 1L;
        Long codigoAtribuicao = 999L;
        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", LocalDate.now(), LocalDate.now().plusDays(1), "Justificativa");
        when(repo.buscar(Unidade.class, codUnidade)).thenReturn(new Unidade());
        when(repo.buscar(AtribuicaoTemporaria.class, codigoAtribuicao)).thenThrow(new ErroEntidadeNaoEncontrada(AtribuicaoTemporaria.class.getSimpleName(), codigoAtribuicao));

        assertThatThrownBy(() -> service.atualizarAtribuicaoTemporaria(codUnidade, codigoAtribuicao, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("atualizarAtribuicaoTemporaria - deve lançar ErroEntidadeNaoEncontrada se usuário não for encontrado")
    void atualizarAtribuicaoTemporaria_UsuarioNaoEncontrado() {
        Long codUnidade = 1L;
        Long codigoAtribuicao = 2L;
        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", LocalDate.now(), LocalDate.now().plusDays(1), "Justificativa");

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
        atribuicao.setCodigo(codigoAtribuicao);
        atribuicao.setUnidade(unidade);

        when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
        when(repo.buscar(AtribuicaoTemporaria.class, codigoAtribuicao)).thenReturn(atribuicao);
        when(repo.buscar(Usuario.class, "123")).thenThrow(new ErroEntidadeNaoEncontrada(Usuario.class.getSimpleName(), "123"));

        assertThatThrownBy(() -> service.atualizarAtribuicaoTemporaria(codUnidade, codigoAtribuicao, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("removerAtribuicaoTemporaria - deve lançar ErroEntidadeNaoEncontrada se atribuição não for encontrada")
    void removerAtribuicaoTemporaria_AtribuicaoNaoEncontrada() {
        Long codUnidade = 1L;
        Long codigoAtribuicao = 999L;
        when(repo.buscar(AtribuicaoTemporaria.class, codigoAtribuicao)).thenThrow(new ErroEntidadeNaoEncontrada(AtribuicaoTemporaria.class.getSimpleName(), codigoAtribuicao));

        assertThatThrownBy(() -> service.removerAtribuicaoTemporaria(codUnidade, codigoAtribuicao))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarResponsavelUnidade - deve lançar IllegalStateException se titular oficial estiver ausente")
    void buscarResponsavelUnidade_TitularAusente() {
        Long codigo = 10L;
        ResponsabilidadeUnidadeResumoLeitura leitura = mock(ResponsabilidadeUnidadeResumoLeitura.class);
        when(leitura.unidadeCodigo()).thenReturn(codigo);

        when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(codigo))).thenReturn(List.of(leitura));

        assertThatThrownBy(() -> service.buscarResponsavelUnidade(codigo))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("Titular oficial ausente");
    }

    @Test
    @DisplayName("buscarResponsavelUnidade - deve lançar IllegalStateException se nome do titular ou responsável estiver nulo")
    void buscarResponsavelUnidade_NomeNulo() {
        Long codigo = 10L;
        ResponsabilidadeUnidadeResumoLeitura leitura = mock(ResponsabilidadeUnidadeResumoLeitura.class);
        when(leitura.unidadeCodigo()).thenReturn(codigo);
        when(leitura.titularTitulo()).thenReturn("123");

        when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(codigo))).thenReturn(List.of(leitura));

        assertThatThrownBy(() -> service.buscarResponsavelUnidade(codigo))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("Responsável ou titular oficial ausente");
    }
}

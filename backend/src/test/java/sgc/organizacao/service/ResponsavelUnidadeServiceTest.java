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
    private AlertaFacade alertaFacade;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private EmailModelosService emailModelosService;

    @Mock
    private ConfigAplicacao configAplicacao;

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

            when(unidadeRepo.findById(codUnidade)).thenReturn(Optional.of(unidade));
            when(usuarioRepo.findById("123456789012")).thenReturn(Optional.of(usuario));
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            Alerta alerta = Alerta.builder().codigo(10L).usuarioDestinoTitulo(usuario.getTituloEleitoral()).build();
            when(alertaFacade.criarAlertaPessoal(
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

            when(unidadeRepo.findById(codUnidade)).thenReturn(Optional.of(unidade));
            when(usuarioRepo.findById("123")).thenReturn(Optional.of(usuario));
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(alertaFacade.criarAlertaPessoal(eq("123"), anyString()))
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

            when(unidadeRepo.findById(codUnidade)).thenReturn(Optional.of(unidade));
            when(usuarioRepo.findById("123")).thenReturn(Optional.of(usuario));
            when(atribuicaoTemporariaRepo.save(any(AtribuicaoTemporaria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doThrow(new IllegalStateException("falha alerta"))
                    .when(alertaFacade).criarAlertaPessoal(eq("123"), anyString());
            when(emailModelosService.criarEmailAtribuicaoTemporaria(any()))
                    .thenReturn("<html>email</html>");

            service.criarAtribuicaoTemporaria(codUnidade, request);

            verify(notificacaoService).enfileirar(any());
            verify(cacheOrganizacaoService).invalidarAposCommit();
        }

        @Test
        @DisplayName("Deve rejeitar atribuição quando dataTermino for anterior ao inicio")
        void deveRejeitarAtribuicaoComDataTerminoAnteriorAoInicio() {
            Long codUnidade = 1L;
            LocalDate dataInicio = LocalDate.of(2024, 2, 10);
            LocalDate dataTermino = LocalDate.of(2024, 2, 9);
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", dataInicio, dataTermino, "Justificativa");

            when(unidadeRepo.findById(codUnidade)).thenReturn(Optional.of(new Unidade()));
            when(usuarioRepo.findById("123")).thenReturn(Optional.of(new Usuario().setTituloEleitoral("123")));

            assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                    .isInstanceOf(ErroValidacao.class);

            verify(atribuicaoTemporariaRepo, never()).save(any());
            verifyNoInteractions(cacheOrganizacaoService);
            verifyNoInteractions(alertaFacade);
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

            when(unidadeRepo.findById(codUnidade)).thenReturn(Optional.of(unidade));
            when(usuarioRepo.findById("123")).thenReturn(Optional.of(usuario));
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

            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            when(atribuicaoTemporariaRepo.findById(9L)).thenReturn(Optional.of(atribuicao));
            when(usuarioRepo.findById("123")).thenReturn(Optional.of(usuario));
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

            when(atribuicaoTemporariaRepo.findById(9L)).thenReturn(Optional.of(atribuicao));

            service.removerAtribuicaoTemporaria(1L, 9L);

            verify(atribuicaoTemporariaRepo).delete(atribuicao);
            verify(cacheOrganizacaoService).invalidarAposCommit();
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
            when(usuarioRepo.findById("123456789012")).thenReturn(Optional.of(usuarioCompleto));

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
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Responsável ou titular oficial ausente");
        }

        @Test
        @DisplayName("Deve lançar exceção quando apenas o titular oficial estiver ausente")
        void deveLancarExcecaoQuandoTitularOficialAusente() {
            Long codUnidade = 1L;
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeResumoLeitura(codUnidade, "RESP", "Responsavel", "TITULAR", null)));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(IllegalStateException.class)
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
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Titular oficial ausente");
        }

        @Test
        @DisplayName("Deve lançar exceção quando titular oficial estiver em branco")
        void deveLancarExcecaoQuandoTitularOficialEmBranco() {
            Long codUnidade = 3L;
            when(responsabilidadeRepo.listarResumosPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeResumoLeitura(codUnidade, "RESP", "Responsavel", "   ", null)));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(IllegalStateException.class)
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
                .isInstanceOf(IllegalStateException.class)
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
        when(usuarioRepo.findById("123")).thenReturn(Optional.of(new Usuario()));

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
}

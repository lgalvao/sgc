package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;
import sgc.alerta.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;
import static sgc.subprocesso.model.TipoTransicao.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoTransicaoService")
@SuppressWarnings("NullAway.Init")
class SubprocessoTransicaoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoNotificacaoService notificacaoService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private AlertaFacade alertaService;

    @InjectMocks
    private SubprocessoTransicaoService service;

    @Test
    @DisplayName("registrarTransicao deve persistir movimentacao, atualizar localizacao e delegar notificacao")
    void registrarTransicaoDevePersistirMovimentacaoAtualizarLocalizacaoEDelegarNotificacao() {
        Unidade origem = criarUnidade(10L, "ORIG", "Origem");
        Unidade destino = criarUnidade(20L, "DEST", "Destino");
        Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, origem);
        Usuario usuario = criarUsuario();

        service.registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(CADASTRO_DISPONIBILIZADO)
                .origem(origem)
                .destino(destino)
                .usuario(usuario)
                .observacoes("Observacao")
                .build());

        verify(movimentacaoRepo).save(argThat(mov ->
                mov.getSubprocesso().equals(subprocesso)
                        && mov.getUnidadeOrigem().equals(origem)
                        && Objects.equals(mov.getUnidadeDestino(), destino)
                        && mov.getDescricao().equals(CADASTRO_DISPONIBILIZADO.getDescMovimentacao())
                        && mov.getUsuario().equals(usuario)));
        verify(subprocessoRepo).save(subprocesso);
        verify(notificacaoService).registrarComunicacoesTransicao(argThat(cmd ->
                cmd.subprocesso().equals(subprocesso)
                        && cmd.tipoTransicao() == CADASTRO_DISPONIBILIZADO
                        && cmd.unidadeOrigem().equals(origem)
                        && cmd.unidadeDestino().equals(destino)
                        && "Observacao".equals(cmd.observacoes())));
    }

    @Test
    @DisplayName("aceitarValidacao deve encaminhar para a unidade superior")
    void aceitarValidacaoDeveEncaminharParaUnidadeSuperior() {
        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        Unidade admin = criarUnidade(1L, "ADMIN", "Administração");
        unidade.setUnidadeSuperior(admin);

        Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, unidade);
        Usuario usuario = criarUsuario();
        usuario.setUnidadeAtivaCodigo(10L);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(subprocesso);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);
        when(analiseRepo.save(any(Analise.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(admin);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

        service.aceitarValidacao(1L, "Aceite final");

        verify(analiseRepo).save(argThat(analise ->
                analise.getSubprocesso().equals(subprocesso)
                        && analise.getTipo() == TipoAnalise.VALIDACAO
                        && analise.getAcao() == TipoAcaoAnalise.ACEITE_MAPEAMENTO
                        && "Aceite final".equals(analise.getObservacoes())
                        && "Aceite da validação".equals(analise.getMotivo())));
        verify(notificacaoService).registrarComunicacoesTransicao(argThat(cmd ->
                cmd.subprocesso().equals(subprocesso)
                        && cmd.tipoTransicao() == MAPA_VALIDACAO_ACEITA
                        && cmd.unidadeOrigem().equals(unidade)
                        && cmd.unidadeDestino().equals(admin)
                        && "Aceite final".equals(cmd.observacoes())));
        assertThat(subprocesso.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_VALIDADO);
    }

    @Test
    @DisplayName("apresentarSugestoes deve mudar situacao e salvar mapa")
    void apresentarSugestoesDeveMudarSituacaoESalvarMapa() {
        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        Unidade admin = criarUnidade(1L, "ADMIN", "Administração");
        unidade.setUnidadeSuperior(admin);

        Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO, unidade);
        sp.setDataLimiteEtapa1(LocalDateTime.now());
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));
        sp.setMapa(new sgc.mapa.model.Mapa());

        Usuario usuario = criarUsuario();
        usuario.setUnidadeAtivaCodigo(10L);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidade);
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(admin);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

        service.apresentarSugestoes(1L, "Novas sugestões");

        assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_COM_SUGESTOES);
        assertThat(sp.getMapa().getSugestoes()).isEqualTo("Novas sugestões");
        verify(mapaManutencaoService).salvarMapa(sp.getMapa());
    }

    @Test
    @DisplayName("validarMapa deve mudar situacao")
    void validarMapaDeveMudarSituacao() {
        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO, unidade);
        sp.setDataLimiteEtapa1(LocalDateTime.now());
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

        Usuario usuario = criarUsuario();
        usuario.setUnidadeAtivaCodigo(10L);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidade);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

        service.validarMapa(1L);

        assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_VALIDADO);
    }

    @Test
    @DisplayName("alterarDataLimite deve atualizar etapa 2 e enfileirar notificação")
    void alterarDataLimiteDeveAtualizarEtapa2EEnfileirarNotificacao() {
        Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
        Processo processo = criarProcesso(MAPEAMENTO);
        Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, unidade);
        subprocesso.setProcesso(processo);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.of(2026, 4, 10, 0, 0));
        subprocesso.setDataLimiteEtapa2(LocalDateTime.of(2026, 4, 20, 0, 0));

        when(consultaService.buscarSubprocesso(1L)).thenReturn(subprocesso);

        service.alterarDataLimite(1L, LocalDate.of(2026, 4, 25));

        assertThat(subprocesso.getDataLimiteEtapa2()).isEqualTo(LocalDateTime.of(2026, 4, 25, 0, 0));
        verify(subprocessoRepo).save(subprocesso);
        verify(notificacaoService).notificarAlteracaoDataLimite(eq(subprocesso), eq("25/04/2026"), eq(2));
    }

    private Subprocesso criarSubprocesso(TipoProcesso tipoProcesso, SituacaoSubprocesso situacao, Unidade unidade) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(criarProcesso(tipoProcesso));
        subprocesso.setSituacaoForcada(situacao);
        return subprocesso;
    }

    private Processo criarProcesso(TipoProcesso tipoProcesso) {
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        processo.setTipo(tipoProcesso);
        return processo;
    }

    private Unidade criarUnidade(Long codigo, String sigla, String nome) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        return unidade;
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setUnidadeAtivaCodigo(10L);
        return usuario;
    }

    @Nested
    @DisplayName("Cobertura Adicional de Branches")
    class CoberturaAdicional {

        @Test
        @DisplayName("Deve lançar erro quando data limite é anterior à última data (disponibilizarMapa)")
        void deveLancarErroQuandoDataLimiteAnterior() {
            Unidade u = criarUnidade(10L, "U10", "Unidade 10");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_CRIADO, u);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
            sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(30)); // Requisito: sempre definida
            sp.setMapa(new sgc.mapa.model.Mapa());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(LocalDate.now().plusDays(5), "Obs");

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);

            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);
            when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req))
                    .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
        }

        @Test
        @DisplayName("submeterMapaAjustado deve funcionar com data limite nula")
        void submeterMapaAjustadoDeveFuncionarComDataLimiteNula() {
            Unidade u = criarUnidade(10L, "U10", "Unidade 10");
            Subprocesso sp = criarSubprocesso(REVISAO, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, u);
            sp.setMapa(new sgc.mapa.model.Mapa());

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);

            SubmeterMapaAjustadoRequest req = new SubmeterMapaAjustadoRequest("Justificativa", null, List.of());
            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);
            when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

            service.submeterMapaAjustado(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(REVISAO_MAPA_DISPONIBILIZADO);
        }

        @Test
        @DisplayName("devolverValidacao deve filtrar movimentacao por unidade de analise e hierarquia")
        void devolverValidacaoDeveFiltrarMovimentacaoPorUnidadeAnaliseEHierarquia() {
            Unidade uOrigem = criarUnidade(1L, "ORI", "Origem");
            Unidade uAnalise = criarUnidade(10L, "ANA", "Analise");
            Unidade outraUnidade = criarUnidade(3L, "OUT", "Outra");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, uOrigem);

            Movimentacao m1 = new Movimentacao();
            m1.setUnidadeDestino(outraUnidade);
            m1.setUnidadeOrigem(outraUnidade);
            Movimentacao m2 = new Movimentacao();
            m2.setUnidadeDestino(uAnalise);
            m2.setUnidadeOrigem(uOrigem);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uAnalise);
            when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of(m1, m2));
            when(hierarquiaService.isSubordinada(uOrigem, uAnalise)).thenReturn(true);

            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);
            when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

            service.devolverValidacao(1L, "Justif");

            verify(analiseRepo).save(any());
        }

        @Test
        @DisplayName("devolverValidacao deve rejeitar ADMIN quando o mapa estiver validado")
        void devolverValidacaoDeveRejeitarAdminQuandoMapaValidado() {
            Unidade uOrigem = criarUnidade(10L, "ORI", "Origem");
            Subprocesso sp = criarSubprocesso(REVISAO, REVISAO_MAPA_VALIDADO, uOrigem);
            sp.setDataLimiteEtapa1(LocalDateTime.now());
            sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));
            Usuario usuario = criarUsuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            usuario.setUnidadeAtivaCodigo(10L);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uOrigem);
            when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
            doThrow(new sgc.comum.erros.ErroValidacao("Situação inválida"))
                    .when(validacaoService)
                    .validarSituacaoPermitida(eq(sp), any(SituacaoSubprocesso[].class));

            assertThatThrownBy(() -> service.devolverValidacao(1L, "Justif"))
                    .isInstanceOf(sgc.comum.erros.ErroValidacao.class);

            verify(analiseRepo, never()).save(any());
        }

        @Test
        @DisplayName("homologarValidacao deve rejeitar ADMIN quando o mapa tiver sugestões")
        void homologarValidacaoDeveRejeitarQuandoMapaComSugestoes() {
            Unidade uOrigem = criarUnidade(10L, "ORI", "Origem");
            Subprocesso sp = criarSubprocesso(REVISAO, REVISAO_MAPA_COM_SUGESTOES, uOrigem);
            sp.setDataLimiteEtapa1(LocalDateTime.now());
            sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));
            Usuario usuario = criarUsuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            usuario.setUnidadeAtivaCodigo(10L);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uOrigem);
            when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
            doThrow(new sgc.comum.erros.ErroValidacao("Situação inválida"))
                    .when(validacaoService)
                    .validarSituacaoPermitida(eq(sp), any(SituacaoSubprocesso[].class));

            assertThatThrownBy(() -> service.homologarValidacao(1L, "Obs"))
                    .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
        }

        @Test
        @DisplayName("alterarDataLimite deve lançar erro quando data limite é anterior à etapa 2")
        void alterarDataLimiteDeveLancarErroQuandoAnteriorEtapa2() {
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, new Unidade());
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(5));
            sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

            assertThatThrownBy(() -> service.alterarDataLimite(1L, LocalDate.now().plusDays(7)))
                    .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
        }

        @Test
        @DisplayName("disponibilizarMapa deve funcionar quando não há data limite anterior")
        void disponibilizarMapaDeveFuncionarQuandoNaoHaDataLimiteAnterior() {
            Unidade u = criarUnidade(10L, "U10", "Unidade 10");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_CRIADO, u);
            sp.setDataLimiteEtapa1(LocalDateTime.now().minusDays(10)); // Sempre definida
            sp.setDataLimiteEtapa2(null);
            sp.setMapa(new sgc.mapa.model.Mapa());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(LocalDate.now().plusDays(5), "Obs");

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
            when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);
            when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

            service.disponibilizarMapa(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        }

        @Test
        @DisplayName("alterarDataLimite deve funcionar quando não há data limite anterior")
        void alterarDataLimiteDeveFuncionarQuandoNaoHaDataLimiteAnterior() {
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO, new Unidade());
            sp.setDataLimiteEtapa1(null);
            sp.setDataLimiteEtapa2(null);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

            service.alterarDataLimite(1L, LocalDate.now().plusDays(5));

            assertThat(sp.getDataLimiteEtapa2()).isEqualTo(LocalDate.now().plusDays(5).atStartOfDay());
        }

        @Test
        @DisplayName("aceitarValidacao não deve fazer nada quando não há superior")
        void aceitarValidacaoNaoDeveFazerNadaQuandoNaoHaSuperior() {
            Unidade unidade = criarUnidade(10L, "ORIG", "Origem");
            unidade.setUnidadeSuperior(null);

            Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, unidade);
            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);

            when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

            service.aceitarValidacao(1L, "Obs");

            verify(analiseRepo, never()).save(any());
            verify(notificacaoService, never()).registrarComunicacoesTransicao(any());
        }

        @Nested
        @DisplayName("obterUltimaDataLimite private method")
        class ObterUltimaDataLimiteTests {

            @Test
            @DisplayName("deve lançar IllegalStateException quando etapa 2 existe sem etapa 1")
            void deveLancarIllegalStateExceptionQuandoEtapa2SemEtapa1() {
                Subprocesso sp = new Subprocesso();
                sp.setCodigo(1L);
                sp.setDataLimiteEtapa1(null);
                sp.setDataLimiteEtapa2(LocalDateTime.now());

                assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "obterUltimaDataLimite", sp))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("etapa 2 sem data limite da etapa 1");
            }

            @Test
            @DisplayName("deve lançar IllegalStateException quando etapa 1 é posterior à etapa 2")
            void deveLancarIllegalStateExceptionQuandoEtapa1PosteriorEtapa2() {
                Subprocesso sp = new Subprocesso();
                sp.setCodigo(1L);
                sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
                sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(5));

                assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "obterUltimaDataLimite", sp))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("etapa 1 posterior à etapa 2");
            }

            @Test
            @DisplayName("deve retornar null quando ambas são nulas")
            void deveRetornarNullQuandoAmbasNulas() {
                Subprocesso sp = new Subprocesso();
                sp.setDataLimiteEtapa1(null);
                sp.setDataLimiteEtapa2(null);

                LocalDate result = ReflectionTestUtils.invokeMethod(service, "obterUltimaDataLimite", sp);

                assertThat(result).isNull();
            }
        }
    }
}

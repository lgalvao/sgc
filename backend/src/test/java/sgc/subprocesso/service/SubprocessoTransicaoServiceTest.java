package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
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
    private SubprocessoFluxoContextoService fluxoContextoService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioAplicacaoService usuarioAplicacaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private AlertaAplicacaoService alertaService;

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
        when(fluxoContextoService.buscarSuperiorImediato(10L)).thenReturn(admin);

        when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

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
        when(fluxoContextoService.buscarSuperiorImediato(10L)).thenReturn(admin);
        when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

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
        when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

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
        verify(notificacaoService).notificarAlteracaoDataLimite(subprocesso, "25/04/2026", 2);
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
        @DisplayName("Deve lançar erro quando data limite não é posterior ao fim da etapa anterior")
        void deveLancarErroQuandoDataLimiteAnterior() {
            Unidade u = criarUnidade(10L, "U10", "Unidade 10");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_CRIADO, u);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
            sp.setDataFimEtapa1(LocalDate.now().plusDays(5).atStartOfDay());
            sp.setMapa(new sgc.mapa.model.Mapa());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(LocalDate.now().plusDays(5), "Obs");

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);

            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

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
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

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
            when(fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(sp, uAnalise)).thenReturn(uOrigem);

            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

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
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);
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
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);
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
            sp.setDataFimEtapa1(LocalDateTime.now().plusDays(7));
            sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

            assertThatThrownBy(() -> service.alterarDataLimite(1L, LocalDate.now().plusDays(6)))
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
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

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

            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

            service.aceitarValidacao(1L, "Obs");

            verify(analiseRepo, never()).save(any());
            verify(notificacaoService, never()).registrarComunicacoesTransicao(any());
        }

    }

    @Nested
    @DisplayName("Cobertura Extra")
    class CoberturaExtra {
        @Test
        @DisplayName("executarDisponibilizacaoMapa - valida data limite igual")
        void disponibilizarMapa_DataIgual() {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setUnidade(u);
            sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Processo p = new Processo();
            p.setTipo(MAPEAMENTO);
            sp.setProcesso(p);
            sp.setMapa(new sgc.mapa.model.Mapa());
            sp.getMapa().setCodigo(100L);
            sp.setDataLimiteEtapa1(LocalDateTime.of(2026, 1, 1, 0, 0));
            sp.setDataLimiteEtapa2(LocalDateTime.of(2026, 1, 10, 0, 0));
            sp.setDataFimEtapa1(LocalDateTime.of(2026, 1, 10, 0, 0));

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
            when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(LocalDate.of(2026, 1, 15), "Obs");
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(1L);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

            service.disponibilizarMapa(1L, req);

            assertThat(sp.getDataLimiteEtapa2()).isEqualTo(LocalDateTime.of(2026, 1, 15, 0, 0));
        }

        @Test
        @DisplayName("disponibilizarMapa deve manter sugestões nulas quando observação estiver em branco")
        void disponibilizarMapaComObservacaoEmBranco() {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setUnidade(u);
            sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Processo p = new Processo();
            p.setTipo(MAPEAMENTO);
            sp.setProcesso(p);
            sp.setMapa(new sgc.mapa.model.Mapa());
            sp.getMapa().setCodigo(100L);
            sp.setDataLimiteEtapa1(LocalDateTime.of(2026, 1, 1, 0, 0));

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
            when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(1L);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

            service.disponibilizarMapa(1L, new DisponibilizarMapaRequest(LocalDate.of(2026, 1, 15), "   "));

            assertThat(sp.getMapa().getSugestoes()).isNull();
        }

        @Test
        @DisplayName("disponibilizarMapaEmBloco - deve aceitar lista de subprocessos")
        void disponibilizarMapaEmBloco_ComListaDeSubprocessos() {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setUnidade(u);
            sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            sp.setProcesso(Processo.builder().tipo(MAPEAMENTO).build());
            sp.setMapa(new sgc.mapa.model.Mapa());
            sp.setDataLimiteEtapa1(LocalDateTime.of(2026, 1, 1, 0, 0));
            sp.setDataLimiteEtapa2(LocalDateTime.of(2026, 1, 10, 0, 0));

            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
            when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(LocalDate.of(2026, 1, 15), "Obs");
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(1L);

            service.disponibilizarMapaEmBloco(List.of(sp), req, usuario);

            assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        }

        @Test
        @DisplayName("executarAceiteValidacaoEmBloco - deve aceitar múltiplos subprocessos")
        void executarAceiteValidacaoEmBloco() {
            Unidade u = criarUnidade(10L, "U1", "Unidade 1");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, u);
            sp.setMapa(new sgc.mapa.model.Mapa());
            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.ADMIN);

            when(subprocessoRepo.buscarPorCodigosComMapaEAtividades(List.of(1L))).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);
            when(fluxoContextoService.buscarSuperiorImediato(10L)).thenReturn(null); // Admin na raiz

            service.aceitarValidacaoEmBloco(List.of(1L));

            verify(analiseRepo).save(any());
        }

        @Test
        @DisplayName("registrarWorkflowParaSuperiorAtual - admin na raiz deve registrar para si mesmo")
        void registrarWorkflowParaSuperiorAtual_AdminNaRaiz() {
            Unidade u = criarUnidade(1L, "ROOT", "Root");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, u);
            sp.setMapa(new sgc.mapa.model.Mapa());
            Usuario usuario = criarUsuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            usuario.setUnidadeAtivaCodigo(1L);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(any())).thenReturn(u);
            when(fluxoContextoService.buscarSuperiorImediato(1L)).thenReturn(null);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);
            when(analiseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            service.aceitarValidacao(1L, "Obs");

            verify(analiseRepo).save(argThat(a -> a.getUnidadeCodigo().equals(1L)));
            assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_VALIDADO);
        }

        @Test
        @DisplayName("disponibilizarMapa deve negar escrita quando subprocesso está em outra localização")
        void disponibilizarMapaDeveNegarQuandoLocalizacaoDiferente() {
            Unidade unidadeSubprocesso = criarUnidade(10L, "U10", "Unidade 10");
            Unidade unidadeAtual = criarUnidade(20L, "U20", "Unidade 20");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_CRIADO, unidadeSubprocesso);
            sp.setMapa(new sgc.mapa.model.Mapa());

            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidadeAtual);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

            assertThatThrownBy(() -> service.disponibilizarMapa(1L, new DisponibilizarMapaRequest(LocalDate.now().plusDays(1), null)))
                    .isInstanceOf(sgc.comum.erros.ErroAcessoNegado.class)
                    .hasMessageContaining("não está localizado");
        }


        @Test
        @DisplayName("devolverValidacao deve manter situação quando devolução não retorna para unidade do subprocesso")
        void devolverValidacaoMantemSituacaoQuandoDestinoNaoEhUnidadeDoSubprocesso() {
            Unidade unidadeSubprocesso = criarUnidade(1L, "U1", "Unidade 1");
            Unidade unidadeAnalise = criarUnidade(10L, "U10", "Unidade 10");
            Unidade unidadeDevolucao = criarUnidade(20L, "U20", "Unidade 20");
            Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, unidadeSubprocesso);
            sp.setDataFimEtapa2(LocalDateTime.now());

            Movimentacao movimentacao = new Movimentacao();
            movimentacao.setUnidadeDestino(unidadeAnalise);
            movimentacao.setUnidadeOrigem(unidadeDevolucao);

            Usuario usuario = criarUsuario();
            usuario.setUnidadeAtivaCodigo(10L);

            when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidadeAnalise);
            when(fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(sp, unidadeAnalise)).thenReturn(unidadeDevolucao);
            when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);
            when(analiseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            service.devolverValidacao(1L, "just");

            assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_VALIDADO);
            assertThat(sp.getDataFimEtapa2()).isNotNull();
        }
    }

    @Test
    @DisplayName("devolverValidacao deve validar apenas situações com sugestões para ADMIN")
    void devolverValidacaoDeveValidarApenasSituacoesComSugestoesParaAdmin() {
        Unidade unidadeSubprocesso = criarUnidade(1L, "U1", "Unidade 1");
        Unidade unidadeAnalise = criarUnidade(10L, "U10", "Unidade 10");
        Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_COM_SUGESTOES, unidadeSubprocesso);
        sp.setDataFimEtapa2(LocalDateTime.now());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setUnidadeDestino(unidadeAnalise);
        movimentacao.setUnidadeOrigem(unidadeSubprocesso);

        Usuario usuario = criarUsuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setUnidadeAtivaCodigo(10L);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidadeAnalise);
        when(fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(sp, unidadeAnalise)).thenReturn(unidadeSubprocesso);
        when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);
        when(analiseRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.devolverValidacao(1L, "Ajustar");

        verify(validacaoService).validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_COM_SUGESTOES);
    }

    @Test
    @DisplayName("devolverValidacao deve lançar erro quando o histórico não indicar unidade de devolução")
    void devolverValidacaoDeveLancarErroQuandoOHistoricoNaoIndicarUnidadeDeDevolucao() {
        Unidade unidadeSubprocesso = criarUnidade(1L, "U1", "Unidade 1");
        Unidade unidadeAnalise = criarUnidade(10L, "U10", "Unidade 10");
        Unidade outraUnidade = criarUnidade(20L, "U20", "Unidade 20");
        Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, unidadeSubprocesso);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setUnidadeDestino(outraUnidade);
        movimentacao.setUnidadeOrigem(outraUnidade);

        Usuario usuario = criarUsuario();
        usuario.setUnidadeAtivaCodigo(10L);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidadeAnalise);
        when(fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(sp, unidadeAnalise))
                .thenThrow(new sgc.comum.erros.ErroInconsistenciaInterna(
                        "Historico de movimentacoes inconsistente para devolucao do subprocesso 1 na unidade 10"
                ));
        when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);

        assertThatThrownBy(() -> service.devolverValidacao(1L, "Justificativa"))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("Historico de movimentacoes inconsistente");
    }

    @Test
    @DisplayName("registrarTransicaoSemEmail não deve criar alerta quando o tipo não gerar alerta")
    void registrarTransicaoSemEmailNaoDeveCriarAlertaQuandoOTipoNaoGerarAlerta() {
        Unidade origem = criarUnidade(10L, "ORIG", "Origem");
        Unidade destino = criarUnidade(20L, "DEST", "Destino");
        Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, origem);
        Usuario usuario = criarUsuario();

        service.registrarTransicaoSemEmail(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(CADASTRO_HOMOLOGADO)
                .origem(origem)
                .destino(destino)
                .usuario(usuario)
                .build());

        verify(notificacaoService, never()).registrarAlertaTransicao(any());
    }

    @Test
    @DisplayName("aceitarValidacaoEmBloco não deve registrar workflow sem e-mail quando não houver superior e o usuário não for ADMIN")
    void aceitarValidacaoEmBlocoNaoDeveRegistrarWorkflowSemEmailQuandoNaoHouverSuperiorEUsuarioNaoForAdmin() {
        Unidade unidade = criarUnidade(10L, "U10", "Unidade 10");
        Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO, unidade);
        Usuario usuario = criarUsuario();
        usuario.setPerfilAtivo(Perfil.GESTOR);
        usuario.setUnidadeAtivaCodigo(10L);

        when(subprocessoRepo.buscarPorCodigosComMapaEAtividades(List.of(1L))).thenReturn(List.of(subprocesso));
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);
        when(usuarioAplicacaoService.usuarioAutenticado()).thenReturn(usuario);
        when(fluxoContextoService.buscarSuperiorImediato(10L)).thenReturn(null);

        service.aceitarValidacaoEmBloco(List.of(1L));

        verify(analiseRepo, never()).save(any());
    }
}

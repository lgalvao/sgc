package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CadastroFluxoService")
@SuppressWarnings("NullAway.Init")
class CadastroFluxoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private SubprocessoNotificacaoService notificacaoService;

    @InjectMocks
    private CadastroFluxoService service;

    @Test
    @DisplayName("disponibilizar deve encaminhar para unidade superior")
    void disponibilizarDeveEncaminharParaUnidadeSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSigla("U1");
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(new sgc.mapa.model.Mapa());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

        service.disponibilizarCadastro(100L);

        verify(transicaoService).registrarTransicao(argThat(cmd ->
                Objects.equals(cmd.destino(), admin) && cmd.sp().equals(sp)));
    }

    @Test
    @DisplayName("disponibilizarRevisao deve lançar ErroValidacao se situação impediente")
    void disponibilizarRevisaoImpediente() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        doThrow(new sgc.comum.erros.ErroValidacao("erro"))
                .when(validacaoService).validarSituacaoPermitida(any(Subprocesso.class), eq(REVISAO_CADASTRO_EM_ANDAMENTO));

        assertThatThrownBy(() -> service.disponibilizarRevisao(100L))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
    }

    @Test
    @DisplayName("Deve aceitar cadastro em bloco para diferentes tipos de processo")
    void deveAceitarCadastroEmBlocoDiferentesTipos() {
        Subprocesso spMap = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, new Unidade());
        Subprocesso spRev = criarSubprocesso(REVISAO, REVISAO_CADASTRO_DISPONIBILIZADA, new Unidade());

        when(subprocessoRepo.buscarPorCodigosComMapaEAtividades(List.of(10L, 20L))).thenReturn(List.of(spMap, spRev));
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(spMap)).thenReturn(spMap.getUnidade());
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(spRev)).thenReturn(spRev.getUnidade());

        when(usuarioFacade.usuarioAutenticado()).thenReturn(criarUsuario());

        service.aceitarCadastroEmBloco(List.of(10L, 20L));

        assertThat(spMap.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        assertThat(spRev.getSituacao()).isEqualTo(REVISAO_CADASTRO_DISPONIBILIZADA);
    }

    @Test
    @DisplayName("Deve homologar cadastro em bloco para diferentes tipos de processo")
    void deveHomologarCadastroEmBlocoDiferentesTipos() {
        Subprocesso spMap = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, new Unidade());
        Unidade uMap = new Unidade();
        uMap.setCodigo(1L);
        uMap.setSigla("U");
        spMap.setUnidade(uMap);
        Subprocesso spRev = criarSubprocesso(REVISAO, REVISAO_CADASTRO_DISPONIBILIZADA, new Unidade());
        Unidade uRev = new Unidade();
        uRev.setCodigo(2L);
        uRev.setSigla("V");
        spRev.setUnidade(uRev);

        when(subprocessoRepo.buscarPorCodigosComMapaEAtividades(List.of(10L, 20L))).thenReturn(List.of(spMap, spRev));
        when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

        when(usuarioFacade.usuarioAutenticado()).thenReturn(criarUsuario());

        service.homologarCadastroEmBloco(List.of(10L, 20L));

        assertThat(spMap.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        assertThat(spRev.getSituacao()).isEqualTo(REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("executarDevolucao deve falhar sem movimentações compativeis")
    void executarDevolucaoSemMovimentacoesCompativeisDeveFalhar() {
        Unidade u = criarUnidade(1L, "U", "Unid");
        Subprocesso sp = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, u);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
        when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of());

        when(usuarioFacade.usuarioAutenticado()).thenReturn(criarUsuario());

        assertThatThrownBy(() -> service.devolver(1L, "Obs"))
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessageContaining("Historico de movimentacoes inconsistente");
    }

    @Test
    @DisplayName("deve aceitar revisao cadastro clashing")
    void deveAceitarRevisaoCadastroClashing() {
        Unidade u = criarUnidade(1L, "U", "Unid");
        Subprocesso sp = criarSubprocesso(REVISAO, REVISAO_CADASTRO_DISPONIBILIZADA, u);
        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(criarUsuario());

        service.aceitar(1L, "Obs");
        assertThat(sp.getSituacao()).isEqualTo(REVISAO_CADASTRO_DISPONIBILIZADA);
    }

    @Test
    @DisplayName("criarAlertasReabertura - superior direto")
    void criarAlertasReaberturaSuperiorDireto() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        u.setCodigo(20L);
        Unidade sup1 = new Unidade();
        sup1.setCodigo(30L);
        Unidade sup2 = new Unidade();
        sup2.setCodigo(40L);
        u.setUnidadeSuperior(sup1);
        sup1.setUnidadeSuperior(sup2);
        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        sp.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(unidadeHierarquiaService.buscarCodigoPai(20L)).thenReturn(30L);
        when(unidadeService.buscarPorCodigo(30L)).thenReturn(sup1);

        service.reabrirCadastro(100L, "justificativa");

        verify(alertaService, times(1)).criarAlertaReaberturaCadastro(p, u);
        verify(alertaService, times(1)).criarAlertaReaberturaCadastroSuperior(p, sup1, u);
        verify(alertaService, never()).criarAlertaReaberturaCadastroSuperior(p, sup2, u);
    }

    @Test
    @DisplayName("criarAlertasReabertura - revisao para superior direto")
    void criarAlertasReaberturaRevisaoSuperiorDireto() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        u.setCodigo(20L);
        Unidade sup = new Unidade();
        sup.setCodigo(30L);
        u.setUnidadeSuperior(sup);
        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        sp.setSituacaoForcada(REVISAO_MAPA_HOMOLOGADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(unidadeHierarquiaService.buscarCodigoPai(20L)).thenReturn(30L);
        when(unidadeService.buscarPorCodigo(30L)).thenReturn(sup);

        service.reabrirRevisaoCadastro(100L, "justificativa");

        verify(alertaService).criarAlertaReaberturaRevisao(p, u, "justificativa");
        verify(alertaService).criarAlertaReaberturaRevisaoSuperior(p, sup, u);
    }

    @Test
    @DisplayName("iniciarRevisaoCadastro - com subprocesso não iniciado")
    void iniciarRevisaoCadastroComNaoIniciado() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setSituacao(NAO_INICIADO);
        sp.setMapa(new sgc.mapa.model.Mapa());
        sp.getMapa().setCodigo(1000L);
        sp.setUnidade(new Unidade());
        sp.setProcesso(Processo.builder().tipo(TipoProcesso.REVISAO).build());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        service.iniciarRevisaoCadastro(100L);

        assertThat(sp.getSituacao()).isEqualTo(REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("cancelarInicioRevisaoCadastro - com subprocesso em revisão em andamento")
    void cancelarInicioRevisaoCadastroComRevisaoEmAndamento() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(new sgc.mapa.model.Mapa());
        sp.getMapa().setCodigo(1000L);
        sp.setUnidade(new Unidade());
        sp.setProcesso(Processo.builder().tipo(TipoProcesso.REVISAO).build());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        service.cancelarInicioRevisaoCadastro(100L);

        assertThat(sp.getSituacao()).isEqualTo(NAO_INICIADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("executarDevolucao deve encontrar unidade de devolução subordinada")
    void executarDevolucaoEncontraSubordinada() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Unidade uBase = new Unidade();
        uBase.setCodigo(10L);
        sp.setUnidade(uBase);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Unidade uAnalise = new Unidade();
        uAnalise.setCodigo(20L);
        Unidade uOrigem = new Unidade();
        uOrigem.setCodigo(15L);

        Movimentacao mov = new Movimentacao();
        mov.setUnidadeDestino(uAnalise);
        mov.setUnidadeOrigem(uOrigem);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uAnalise);
        when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of(mov));
        when(hierarquiaService.isSubordinada(uOrigem, uAnalise)).thenReturn(true);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.devolver(1L, "Obs");

        verify(transicaoService).registrarAnalise(argThat(cmd ->
                cmd.unidadeDestinoTransicao().equals(uOrigem)
                        && cmd.motivoAnalise() == null
                        && "Obs".equals(cmd.observacoes())));
    }

    @Test
    @DisplayName("executarHomologacao - com processo REVISAO")
    void executarHomologacaoComRevisao() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        sp.setUnidade(unidade);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
        when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

        service.homologar(1L, "Obs");

        assertThat(sp.getSituacao()).isEqualTo(REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("disponibilizar deve falhar quando unidade for raiz ou intermediaria")
    void disponibilizarDeveFalharQuandoUnidadeForRaizOuIntermediaria() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setTipo(TipoUnidade.RAIZ);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        assertThatThrownBy(() -> service.disponibilizarCadastro(100L))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
    }

    @Test
    @DisplayName("iniciarRevisaoCadastro deve lançar erro se o processo estiver finalizado")
    void iniciarRevisaoCadastroDeveLancarErroSeProcessoFinalizado() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(Processo.builder().tipo(TipoProcesso.REVISAO).situacao(SituacaoProcesso.FINALIZADO).build());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        assertThatThrownBy(() -> service.iniciarRevisaoCadastro(100L))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
    }

    @Test
    @DisplayName("homologar deve falhar se o mapa for nulo")
    void homologarDeveFalharSeMapaForNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setMapa(null);
        sp.setProcesso(Processo.builder().tipo(TipoProcesso.REVISAO).build());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        assertThatThrownBy(() -> service.homologar(100L, "Obs"))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
    }    @Test
    @DisplayName("normalizarTexto deve tratar strings corretamente")
    void normalizarTextoDeveTratarStringsCorretamente() throws Exception {
        java.lang.reflect.Method metodo = CadastroFluxoService.class.getDeclaredMethod("normalizarTexto", String.class);
        metodo.setAccessible(true);

        assertThat(metodo.invoke(null, (Object) null)).isNull();
        assertThat(metodo.invoke(null, "")).isNull();
        assertThat(metodo.invoke(null, "    ")).isNull();
        assertThat(metodo.invoke(null, "  texto com espaco  ")).isEqualTo("texto com espaco");
    }

    @Test
    @DisplayName("iniciarRevisaoCadastro deve funcionar mesmo com processo nulo")
    void iniciarRevisaoCadastroDeveFuncionarMesmoComProcessoNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setSituacao(NAO_INICIADO);
        sp.setMapa(new sgc.mapa.model.Mapa());
        sp.getMapa().setCodigo(1000L);
        sp.setUnidade(new Unidade());
        sp.setProcesso(null);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        service.iniciarRevisaoCadastro(100L);

        assertThat(sp.getSituacao()).isEqualTo(REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("criarAlertasReabertura nao deve criar alertas para superior quando nao houver superior imediato")
    void criarAlertasReaberturaSemSuperiorImediato() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        u.setCodigo(20L);
        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        sp.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(unidadeHierarquiaService.buscarCodigoPai(20L)).thenReturn(null);

        service.reabrirCadastro(100L, "justificativa");

        verify(alertaService, times(1)).criarAlertaReaberturaCadastro(p, u);
        verify(alertaService, never()).criarAlertaReaberturaCadastroSuperior(any(), any(), any());
    }

    @Test
    @DisplayName("obterContextoCadastro deve lancar excecao para tipo de processo sem fluxo de cadastro")
    void obterContextoCadastroTipoSemFluxoDeveLancarExcecao() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.DIAGNOSTICO);
        sp.setProcesso(p);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.homologar(1L, "Obs"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem fluxo de cadastro definido");
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
}

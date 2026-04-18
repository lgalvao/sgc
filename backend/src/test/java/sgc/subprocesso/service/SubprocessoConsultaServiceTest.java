package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;
import sgc.comum.erros.*;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoConsultaService")
@SuppressWarnings("NullAway.Init")
class SubprocessoConsultaServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private sgc.organizacao.UsuarioFacade usuarioFacade;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private sgc.mapa.service.MapaManutencaoService mapaManutencaoService;

    @InjectMocks
    private SubprocessoConsultaService service;

    @BeforeEach
    void configurarDependenciasAdicionais() {
        ReflectionTestUtils.setField(service, "analiseHistoricoService", new AnaliseHistoricoService(unidadeService));
        ReflectionTestUtils.setField(
                service,
                "contextoConsultaService",
                new SubprocessoContextoConsultaService(unidadeService, usuarioFacade, hierarquiaService, localizacaoSubprocessoService)
        );
    }

    @Test
    @DisplayName("buscarSubprocesso deve falhar quando codigo nao existir")
    void buscarSubprocessoDeveFalharQuandoCodigoNaoExistir() {
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarSubprocesso(99L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Subprocesso")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("obterSugestoes deve retornar string vazia quando mapa estiver ausente fora de etapa de mapa")
    void obterSugestoesDeveRetornarStringVaziaQuandoMapaEstiverAusenteForaDeEtapaDeMapa() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(subprocesso));

        assertThat(service.obterSugestoes(1L)).containsEntry("sugestoes", "");
    }

    @Test
    @DisplayName("obterSugestoes deve falhar quando mapa estiver ausente em etapa de mapa")
    void obterSugestoesDeveFalharQuandoMapaEstiverAusenteEmEtapaDeMapa() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(subprocesso));

        assertThatThrownBy(() -> service.obterSugestoes(1L))
                .isInstanceOf(ErroInconsistenciaInterna.class)
                .hasMessageContaining("sem mapa vinculado para leitura de sugestoes");
    }

    @Test
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes deve retornar vazio quando lista de unidades ou situacoes estiver vazia")
    void listarPorProcessoEUnidadeCodigosESituacoesDeveRetornarVazio() {
        assertThat(service.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(), List.of(SituacaoSubprocesso.NAO_INICIADO))).isEmpty();
        assertThat(service.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(10L), List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades deve retornar vazio quando lista de unidades estiver vazia")
    void listarEntidadesPorProcessoEUnidadesDeveRetornarVazioQuandoListaDeUnidadesEstiverVazia() {
        assertThat(service.listarEntidadesPorProcessoEUnidades(1L, List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }

    @Test
    @DisplayName("obterContextoCadastroAtividades deve carregar dados com sucesso")
    void obterContextoCadastroAtividadesSucesso() {
        Long codSubprocesso = 1L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        unidade.setSigla("U100");
        unidade.setNome("Unidade 100");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Mapa mapa = new Mapa();
        mapa.setCodigo(500L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        mapa.setSubprocesso(sp);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setProcesso(new sgc.processo.model.Processo());
        sp.getProcesso().setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        when(subprocessoRepo.buscarPorCodigoComMapa(codSubprocesso)).thenReturn(Optional.of(sp));
        when(usuarioFacade.contextoAutenticado()).thenReturn(new sgc.organizacao.ContextoUsuarioAutenticado("123", 100L, sgc.organizacao.model.Perfil.ADMIN));
        when(unidadeService.buscarPorCodigoComSuperior(100L)).thenReturn(unidade);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidade);
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(500L)).thenReturn(List.of());

        var response = service.obterContextoCadastroAtividades(codSubprocesso);

        assertThat(response).isNotNull();
        assertThat(response.unidade()).isEqualTo(unidade);
        assertThat(response.assinaturaCadastroReferencia()).isEmpty();
    }

    @Test
    @DisplayName("verificarAcessoCadastroHabilitado deve permitir ADMIN em qualquer unidade se disponibilizado")
    void verificarAcessoCadastroHabilitadoAdmin() {
        Long codSubprocesso = 1L;
        Unidade unidadeAlvo = new Unidade();
        unidadeAlvo.setCodigo(100L);
        unidadeAlvo.setSigla("U100");

        Unidade unidadeAdmin = new Unidade();
        unidadeAdmin.setCodigo(999L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setUnidade(unidadeAlvo);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        sp.setProcesso(new sgc.processo.model.Processo());
        sp.getProcesso().setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        when(usuarioFacade.contextoAutenticado()).thenReturn(new sgc.organizacao.ContextoUsuarioAutenticado("admin", 999L, sgc.organizacao.model.Perfil.ADMIN));
        when(unidadeService.buscarPorCodigoComSuperior(999L)).thenReturn(unidadeAdmin);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(unidadeAlvo);
        when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(true);

        var permissoes = service.obterPermissoesUI(sp);
        assertThat(permissoes.habilitarAcessoCadastro()).isTrue();
    }

    @Test
    @DisplayName("listarHistoricoCadastro deve carregar unidades em lote")
    void listarHistoricoCadastroDeveCarregarUnidadesEmLote() {
        Analise analise1 = new Analise();
        analise1.setUnidadeCodigo(10L);
        analise1.setTipo(TipoAnalise.CADASTRO);

        Analise analise2 = new Analise();
        analise2.setUnidadeCodigo(20L);
        analise2.setTipo(TipoAnalise.CADASTRO);

        when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(analise1, analise2));
        when(unidadeService.buscarResumosPorCodigos(List.of(10L, 20L))).thenReturn(List.of(
                new UnidadeResumoLeitura(10L, "Unidade 10", "U10", TipoUnidade.OPERACIONAL),
                new UnidadeResumoLeitura(20L, "Unidade 20", "U20", TipoUnidade.OPERACIONAL)
        ));

        assertThat(service.listarHistoricoCadastro(1L)).hasSize(2);
        verify(unidadeService).buscarResumosPorCodigos(List.of(10L, 20L));
        verify(unidadeService, never()).buscarPorCodigo(anyLong());
    }
}

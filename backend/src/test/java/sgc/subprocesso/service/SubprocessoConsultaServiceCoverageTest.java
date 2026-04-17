package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.mapa.model.*;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoConsultaService - Cobertura de Testes")
class SubprocessoConsultaServiceCoverageTest {

    @InjectMocks
    private SubprocessoConsultaService target;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private ImpactoMapaService impactoMapaService;

    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;

    @Mock
    private AnaliseHistoricoService analiseHistoricoService;

    private void stubContextoAutenticado(Usuario usuario) {
        lenient().when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
                usuario.getTituloEleitoral(),
                usuario.getUnidadeAtivaCodigo(),
                usuario.getPerfilAtivo()
        ));
        Unidade u = new Unidade();
        u.setCodigo(usuario.getUnidadeAtivaCodigo());
        u.setNome("Unidade Usuario");
        u.setSigla("UU");
        lenient().when(unidadeService.buscarPorCodigoComSuperior(usuario.getUnidadeAtivaCodigo())).thenReturn(u);
    }

    @Test
    @DisplayName("listarHistoricoValidacao deve delegar conversão para AnaliseHistoricoService")
    void deveDelegarConversaoHistoricoValidacao() {
        Long codSubprocesso = 100L;
        Analise analise = Analise.builder()
                .codigo(10L)
                .unidadeCodigo(1L)
                .dataHora(LocalDateTime.now())
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .tipo(TipoAnalise.VALIDACAO)
                .usuarioTitulo("123456789012")
                .build();
        when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso))
                .thenReturn(List.of(analise));
        when(analiseHistoricoService.converterLista(List.of(analise))).thenReturn(List.of());

        target.listarHistoricoValidacao(codSubprocesso);

        verify(analiseHistoricoService).converterLista(List.of(analise));
    }

    @Test
    @DisplayName("obterContextoEdicao deve separar carga de mapa e atividades")
    void obterContextoEdicaoDeveSepararCargaDeMapaEAtividades() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        unidade.setNome("Unidade 10");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(processo);
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

        Usuario usuario = Usuario.builder()
                .tituloEleitoral("123456789012")
                .perfilAtivo(Perfil.ADMIN)
                .unidadeAtivaCodigo(10L)
                .build();

        Atividade atividade = new Atividade();
        atividade.setCodigo(200L);
        atividade.setDescricao("Atividade");
        atividade.setConhecimentos(Set.of());

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        mapa.setSubprocesso(subprocesso);
        mapa.setCompetencias(Set.of());
        subprocesso.setMapa(mapa);

        stubContextoAutenticado(usuario);
        when(subprocessoRepo.buscarPorCodigoComMapa(100L)).thenReturn(Optional.of(subprocesso));
        when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(100L)).thenReturn(List.of());
        when(unidadeService.temMapaVigente(10L)).thenReturn(false);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(300L)).thenReturn(List.of(atividade));
        when(mapaManutencaoService.mapaComCompetenciasEAtividadesSubprocesso(100L)).thenReturn(mapa);

        ContextoEdicaoResponse contexto = target.obterContextoEdicao(100L);

        assertThat(contexto.atividadesDisponiveis()).hasSize(1);
        assertThat(contexto.atividadesDisponiveis().getFirst().descricao()).isEqualTo("Atividade");
        verify(mapaManutencaoService).mapaComCompetenciasEAtividadesSubprocesso(100L);
        verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(300L);
    }

    @Test
    @DisplayName("obterContextoCadastroAtividades para REVISAO deve gerar assinatura baseada no mapa vigente")
    void obterContextoCadastroAtividadesRevisao() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        unidade.setNome("Unidade 10");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(processo);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        mapa.setSubprocesso(subprocesso);
        subprocesso.setMapa(mapa);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(400L);

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setDescricao("Conhecimento Teste");
        Atividade atividadeVigente = new Atividade();
        atividadeVigente.setCodigo(200L);
        atividadeVigente.setDescricao("Atividade Vigente");
        atividadeVigente.setConhecimentos(Set.of(conhecimento));

        when(subprocessoRepo.buscarPorCodigoComMapa(100L)).thenReturn(Optional.of(subprocesso));
        when(mapaManutencaoService.mapaVigenteUnidade(10L)).thenReturn(Optional.of(mapaVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(400L)).thenReturn(List.of(atividadeVigente));
        lenient().when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(300L)).thenReturn(List.of());

        Usuario usuario = Usuario.builder()
                .tituloEleitoral("123456789012")
                .unidadeAtivaCodigo(10L)
                .perfilAtivo(Perfil.ADMIN)
                .build();
        stubContextoAutenticado(usuario);
        lenient().when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);

        ContextoCadastroAtividadesResponse res = target.obterContextoCadastroAtividades(100L);

        assertThat(res.atividadesDisponiveis()).isEmpty();
        assertThat(res.assinaturaCadastroReferencia()).isNotBlank();

        verify(mapaManutencaoService).mapaVigenteUnidade(10L);
        verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(400L);
    }

    @Test
    @DisplayName("obterContextoCadastroAtividades deve lancar excecao quando mapa for nulo")
    void obterContextoCadastroAtividades_MapaNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setProcesso(new Processo());
        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        uni.setNome("U10");
        sp.setUnidade(uni);

        when(subprocessoRepo.buscarPorCodigoComMapa(1L)).thenReturn(Optional.of(sp));
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("tit", 10L, Perfil.ADMIN));
        lenient().when(localizacaoSubprocessoService.obterLocalizacaoAtual(any())).thenReturn(uni);
        lenient().when(unidadeService.buscarPorCodigoComSuperior(anyLong())).thenReturn(uni);

        assertThatThrownBy(() -> target.obterContextoCadastroAtividades(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem mapa associado");
    }

    @Test
    @DisplayName("obterPermissoesUI deve bloquear acesso ao cadastro para CHEFE de outra unidade")
    void obterPermissoesUI_BloqueadoChefeOutraUnidade() {
        Subprocesso sp = new Subprocesso();
        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        uni.setSigla("U10");
        uni.setNome("Unidade 10");
        sp.setUnidade(uni);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado("tit", 20L, Perfil.CHEFE));
        Unidade u20 = new Unidade();
        u20.setCodigo(20L);
        u20.setNome("Unidade 20");
        when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(u20);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uni);

        PermissoesSubprocessoDto result = target.obterPermissoesUI(sp);

        assertThat(result.habilitarAcessoCadastro()).isFalse();
    }
}

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
        when(usuarioFacade.contextoAutenticado()).thenReturn(new ContextoUsuarioAutenticado(
                usuario.getTituloEleitoral(),
                usuario.getUnidadeAtivaCodigo(),
                usuario.getPerfilAtivo()
        ));
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

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setUnidadeAtivaCodigo(10L);

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
        when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(unidade);
        when(unidadeService.temMapaVigente(10L)).thenReturn(false);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);
        doReturn(null).when(usuarioFacade).buscarResponsabilidadeDetalhadaAtual(10L);
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(300L)).thenReturn(List.of(atividade));
        when(mapaManutencaoService.mapaComCompetenciasEAtividadesSubprocesso(100L)).thenReturn(mapa);

        ContextoEdicaoResponse contexto = target.obterContextoEdicao(100L);

        assertThat(contexto.atividadesDisponiveis()).hasSize(1);
        assertThat(contexto.atividadesDisponiveis().getFirst().descricao()).isEqualTo("Atividade");
        verify(mapaManutencaoService).mapaComCompetenciasEAtividadesSubprocesso(100L);
        verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(300L);
    }
}

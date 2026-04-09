package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.ContextoEdicaoResponse;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.AnaliseRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.organizacao.service.UnidadeService;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.subprocesso.model.MovimentacaoRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    @DisplayName("obterContextoEdicao deve reaproveitar mapa completo para montar atividades")
    void obterContextoEdicaoDeveReaproveitarMapaCompleto() {
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
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setUnidadeAtivaCodigo(10L);

        Atividade atividade = new Atividade();
        atividade.setCodigo(200L);
        atividade.setDescricao("Atividade");
        atividade.setConhecimentos(Set.of());

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        mapa.setSubprocesso(subprocesso);
        mapa.setAtividades(Set.of(atividade));
        mapa.setCompetencias(Set.of());

        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(100L)).thenReturn(Optional.of(subprocesso));
        when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(100L)).thenReturn(List.of());
        when(unidadeService.buscarPorCodigoComSuperior(10L)).thenReturn(unidade);
        when(unidadeService.temMapaVigente(10L)).thenReturn(false);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidade);
        when(usuarioFacade.buscarResponsabilidadeDetalhadaAtual(10L)).thenReturn(null);
        when(mapaManutencaoService.mapaCompletoSubprocesso(100L)).thenReturn(mapa);

        ContextoEdicaoResponse contexto = target.obterContextoEdicao(100L);

        assertThat(contexto.atividadesDisponiveis()).hasSize(1);
        assertThat(contexto.atividadesDisponiveis().getFirst().descricao()).isEqualTo("Atividade");
        verify(mapaManutencaoService).mapaCompletoSubprocesso(100L);
        verify(mapaManutencaoService, never()).atividadesMapaCodigoComConhecimentos(anyLong());
    }
}

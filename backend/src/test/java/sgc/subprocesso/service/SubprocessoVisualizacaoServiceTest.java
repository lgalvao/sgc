package sgc.subprocesso.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.AnaliseRepo;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoVisualizacaoServiceTest {

    @Mock private UsuarioFacade usuarioFacade;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private MapaVisualizacaoService mapaVisualizacaoService;
    @Mock private ImpactoMapaService impactoMapaService;
    @Mock private SubprocessoAcessoService acessoService;
    @Mock private AnaliseRepo analiseRepo;
    @Mock private AnaliseHistoricoService analiseHistoricoService;

    @InjectMocks
    private SubprocessoVisualizacaoService service;

    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("SIGLA");
        unidade.setNome("Nome da Unidade");

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo");

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(processo);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    void shouldDelegarMapaParaVisualizacao() {
        MapaVisualizacaoResponse mockResponse = MapaVisualizacaoResponse.builder().build();
        when(mapaVisualizacaoService.obterMapaParaVisualizacao(subprocesso)).thenReturn(mockResponse);

        MapaVisualizacaoResponse result = service.mapaParaVisualizacao(subprocesso);

        assertThat(result).isSameAs(mockResponse);
    }

    @Test
    void shouldDelegarVerificarImpactos() {
        ImpactoMapaResponse mockResponse = new ImpactoMapaResponse(false, java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), 0, 0, 0, 0);
        when(impactoMapaService.verificarImpactos(subprocesso)).thenReturn(mockResponse);

        ImpactoMapaResponse result = service.verificarImpactos(subprocesso);

        assertThat(result).isSameAs(mockResponse);
    }

    @Test
    void shouldConstruirDetalheCadastro() {
        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = new SubprocessoConsultaService.ContextoConsultaSubprocesso(
                subprocesso, Perfil.SERVIDOR, unidade, false, true, true, true, false
        );

        when(acessoService.resolverPermissoes(contexto)).thenReturn(null);

        SubprocessoDetalheResponse result = service.construirDetalheCadastro(contexto);

        assertThat(result.subprocesso().codigo()).isEqualTo(100L);
        assertThat(result.responsavel()).isNull();
        assertThat(result.titular()).isNull();
        assertThat(result.movimentacoes()).isEmpty();
        assertThat(result.localizacaoAtual()).isEqualTo("SIGLA");
        assertThat(result.permissoes()).isNull();
    }
}

package sgc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import sgc.alerta.dto.AlertaDto;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaSalvamentoService;
import sgc.painel.PainelFacade;
import sgc.subprocesso.SubprocessoMapaController;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra de Controllers e Services")
class ControllersServicesCoverageTest {
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private sgc.alerta.AlertaFacade alertaService;
    @Mock
    private sgc.mapa.mapper.MapaCompletoMapper mapaCompletoMapper;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;
    @Mock
    private sgc.mapa.service.ImpactoMapaService impactoMapaService;
    @Mock
    private sgc.mapa.service.MapaVisualizacaoService mapaVisualizacaoService;
    @Mock
    private sgc.organizacao.UsuarioFacade usuarioService;
    @Mock
    private sgc.organizacao.UnidadeFacade unidadeService;
    @Mock
    private sgc.processo.service.ProcessoFacade processoFacade;
    @Mock
    private sgc.comum.repo.RepositorioComum repo;

    private SubprocessoMapaController subprocessoMapaController;
    private MapaFacade mapaFacade;
    private PainelFacade painelService;

    @BeforeEach
    void setUp() {
        mapaFacade = new MapaFacade(
                mapaRepo, competenciaRepo, mapaCompletoMapper, mapaSalvamentoService,
                mapaVisualizacaoService, impactoMapaService, repo
        );

        subprocessoMapaController = new SubprocessoMapaController(
                subprocessoFacade,
                mapaFacade,
                usuarioService
        );

        painelService = new PainelFacade(
                processoFacade, alertaService, unidadeService
        );
    }

    @Test
    @DisplayName("Deve listar atividades")
    void deveListarAtividades() {
        when(subprocessoFacade.listarAtividadesSubprocesso(1L)).thenReturn(new ArrayList<>());
        ResponseEntity<List<AtividadeVisualizacaoDto>> response = subprocessoMapaController.listarAtividades(1L);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Deve obter contexto de edição")
    void deveObterContextoEdicao() {
        when(subprocessoFacade.obterContextoEdicao(anyLong(), any())).thenReturn(ContextoEdicaoDto.builder().build());
        ContextoEdicaoDto dto = subprocessoMapaController.obterContextoEdicao(1L, null);
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar erro de acesso negado quando usuário não autenticado em verificarImpactos")
    void deveLancarErroAcessoNegado() {
        when(usuarioService.obterUsuarioAutenticado())
                .thenThrow(new ErroAccessoNegado("Usuário não autenticado"));

        assertThatThrownBy(() -> subprocessoMapaController.verificarImpactos(1L))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao obter mapa completo inexistente")
    void deveLancarErroObterMapaCompletoInexistente() {
        when(repo.buscar(Mapa.class, 99L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 99L));
        assertThatThrownBy(() -> mapaFacade.obterMapaCompleto(99L, 1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve delegar salvar mapa completo inexistente para MapaSalvamentoService")
    void deveDelegarSalvarMapaCompletoInexistente() {
        SalvarMapaRequest req = SalvarMapaRequest.builder().build();
        when(mapaSalvamentoService.salvarMapaCompleto(99L, req))
                .thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 99L));
        assertThatThrownBy(() -> mapaFacade.salvarMapaCompleto(99L, req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve delegar salvar mapa completo com competência inexistente para MapaSalvamentoService")
    void deveDelegarSalvarCompetenciaInexistente() {
        CompetenciaMapaDto compDto = CompetenciaMapaDto.builder()
                .codigo(99L)
                .descricao("Desc")
                .build();
        SalvarMapaRequest req = SalvarMapaRequest.builder()
                .observacoes("Obs")
                .competencias(List.of(compDto))
                .build();

        when(mapaSalvamentoService.salvarMapaCompleto(1L, req))
                .thenThrow(new ErroEntidadeNaoEncontrada("Competência não encontrada: 99"));

        assertThatThrownBy(() -> mapaFacade.salvarMapaCompleto(1L, req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Competência não encontrada");
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar mapa inexistente")
    void deveLancarErroAtualizarMapaInexistente() {
        when(repo.buscar(Mapa.class, 99L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 99L));
        Mapa mapa = new Mapa();
        assertThatThrownBy(() -> mapaFacade.atualizar(99L, mapa))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao excluir mapa inexistente")
    void deveLancarErroExcluirMapaInexistente() {
        when(mapaRepo.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> mapaFacade.excluir(99L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve listar alertas com ordenação padrão se não informada")
    void deveListarAlertasComOrdenacaoPadrao() {
        when(alertaService.listarPorUnidade(anyLong(), any())).thenReturn(Page.empty());
        Page<AlertaDto> resultado = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));
        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve respeitar ordenação informada em listarAlertas")
    void deveRespeitarOrdenacaoEmListarAlertas() {
        Pageable p = PageRequest.of(0, 10, Sort.by("codigo"));
        when(alertaService.listarPorUnidade(anyLong(), any())).thenReturn(Page.empty());
        Page<AlertaDto> resultado = painelService.listarAlertas(null, 1L, p);
        assertThat(resultado).isNotNull();
    }
}

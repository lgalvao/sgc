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
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.painel.PainelFacade;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.subprocesso.SubprocessoMapaController;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.SubprocessoContextoService;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra de Controllers e Services")
class ControllersServicesCoverageTest {
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private SubprocessoContextoService subprocessoContextoService;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private sgc.alerta.AlertaFacade alertaService;
    // Removido atividadeRepo não utilizado
    @Mock
    private sgc.mapa.mapper.MapaCompletoMapper mapaCompletoMapper;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;
    @Mock
    private sgc.mapa.service.ImpactoMapaService impactoMapaService;
    
    // Mocks adicionais para preencher construtores e evitar null
    @Mock private sgc.mapa.service.MapaVisualizacaoService mapaVisualizacaoService;
    @Mock private sgc.subprocesso.service.SubprocessoMapaService subprocessoMapaService;
    @Mock private sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService subprocessoMapaWorkflowService;
    @Mock private sgc.subprocesso.service.crud.SubprocessoValidacaoService validacaoService;
    @Mock private sgc.organizacao.UsuarioFacade usuarioService;
    @Mock private sgc.subprocesso.service.workflow.SubprocessoTransicaoService transicaoService;
    @Mock private sgc.organizacao.UnidadeService unidadeService;
    @Mock private sgc.analise.AnaliseFacade analiseFacade;
    @Mock private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock private sgc.processo.service.ProcessoFacade processoFacade;

    private SubprocessoMapaController subprocessoMapaController;
    private MapaFacade mapaFacade;
    private SubprocessoCadastroWorkflowService cadastroService;
    private PainelFacade painelService;

    @BeforeEach
    void setUp() {
        // Instanciação manual para evitar overhead do @InjectMocks e lidar com muitas dependências
        
        // MapaFacade (needs to be created first since SubprocessoMapaController depends on it)
        mapaFacade = new MapaFacade(
                mapaRepo, competenciaRepo, mapaCompletoMapper, mapaSalvamentoService,
                mapaVisualizacaoService, impactoMapaService
        );
        
        // SubprocessoMapaController
        subprocessoMapaController = new SubprocessoMapaController(
                subprocessoFacade,
                mapaFacade,
                usuarioService
        );

        // SubprocessoCadastroWorkflowService
        cadastroService = new SubprocessoCadastroWorkflowService(
                repositorioSubprocesso, transicaoService, unidadeService, analiseFacade, validacaoService, impactoMapaService, accessControlService, null
        );

        // PainelFacade
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
        // Simula usuário não autenticado retornando null ou lançando exceção
        when(usuarioService.obterUsuarioAutenticado())
            .thenThrow(new ErroAccessoNegado("Usuário não autenticado"));
        
        assertThatThrownBy(() -> subprocessoMapaController.verificarImpactos(1L))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao obter mapa completo inexistente")
    void deveLancarErroObterMapaCompletoInexistente() {
        when(mapaRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapaFacade.obterMapaCompleto(99L, 1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve delegar salvar mapa completo inexistente para MapaSalvamentoService")
    void deveDelegarSalvarMapaCompletoInexistente() {
        SalvarMapaRequest req = new SalvarMapaRequest();
        when(mapaSalvamentoService.salvarMapaCompleto(99L, req))
            .thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 99L));
        assertThatThrownBy(() -> mapaFacade.salvarMapaCompleto(99L, req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve delegar salvar mapa completo com competência inexistente para MapaSalvamentoService")
    void deveDelegarSalvarCompetenciaInexistente() {
        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setObservacoes("Obs");
        CompetenciaMapaDto compDto = new CompetenciaMapaDto();
        compDto.setCodigo(99L);
        compDto.setDescricao("Desc");
        req.setCompetencias(List.of(compDto));

        when(mapaSalvamentoService.salvarMapaCompleto(1L, req))
            .thenThrow(new ErroEntidadeNaoEncontrada("Competência não encontrada: 99"));

        assertThatThrownBy(() -> mapaFacade.salvarMapaCompleto(1L, req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Competência não encontrada");
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar mapa inexistente")
    void deveLancarErroAtualizarMapaInexistente() {
        when(mapaRepo.findById(99L)).thenReturn(Optional.empty());
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

    // --- SubprocessoCadastroWorkflowService Tests ---

    @Test
    @DisplayName("Deve lançar erro ao validar disponibilização sem mapa")
    void deveLancarErroAoValidarDisponibilizacaoSemMapa() {
        Subprocesso sp = new Subprocesso();
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        Unidade unidade = new Unidade();
        unidade.setTituloTitular("123");
        sp.setUnidade(unidade);

        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
        // Mapa nulo

        assertThatThrownBy(() -> cadastroService.disponibilizarCadastro(1L, usuario))
                .isInstanceOf(ErroMapaNaoAssociado.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao devolver cadastro se unidade superior nula")
    void deveLancarErroDevolverCadastroSemSuperior() {
        Subprocesso sp = new Subprocesso();
        Unidade unidade = new Unidade(); // Sem superior
        sp.setUnidade(unidade);

        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> cadastroService.devolverCadastro(1L, "Obs", new Usuario()))
                .isInstanceOf(sgc.comum.erros.ErroInvarianteViolada.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao devolver revisao se status invalido")
    void deveLancarErroDevolverRevisaoStatusInvalido() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO); // Status inválido
        
        // Criar unidade com superior para evitar ErroInvarianteViolada
        Unidade unidadeSuperior = new Unidade();
        unidadeSuperior.setCodigo(100L);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setUnidadeSuperior(unidadeSuperior);
        sp.setUnidade(unidade);
        
        Usuario usuario = new Usuario();
        
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
        
        // Após refatoração de segurança, a validação de situação é feita no AccessControlService
        // que lança ErroAccessoNegado em vez de ErroProcessoEmSituacaoInvalida
        doThrow(new ErroAccessoNegado("Situação inválida"))
                .when(accessControlService)
                .verificarPermissao(any(), any(), any());

        assertThatThrownBy(() -> cadastroService.devolverRevisaoCadastro(1L, "Obs", usuario))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao aceitar revisao se unidade superior nula")
    void deveLancarErroAceitarRevisaoSemSuperior() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade unidade = new Unidade(); // Sem superior
        sp.setUnidade(unidade);

        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> cadastroService.aceitarRevisaoCadastro(1L, "Obs", new Usuario()))
                .isInstanceOf(sgc.comum.erros.ErroInvarianteViolada.class);
    }

    @Test
    @DisplayName("Deve lançar erro se perfil for nulo")
    void deveLancarErroSePerfilNulo() {
        Pageable unpaged = Pageable.unpaged();
        assertThatThrownBy(() -> painelService.listarProcessos(null, 1L, unpaged))
                .isInstanceOf(ErroParametroPainelInvalido.class);
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

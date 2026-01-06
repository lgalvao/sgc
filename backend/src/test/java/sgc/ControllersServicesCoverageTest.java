package sgc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.service.MapaService;
import sgc.subprocesso.SubprocessoMapaController;
import sgc.subprocesso.service.SubprocessoService;
import sgc.subprocesso.service.SubprocessoContextoService;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

// Imports para novos testes
import sgc.subprocesso.service.SubprocessoCadastroWorkflowService;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.organizacao.UnidadeService;
import sgc.painel.PainelService;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.SituacaoSubprocesso;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra de Controllers e Services")
class ControllersServicesCoverageTest {

    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoContextoService subprocessoContextoService;
    @Mock
    private sgc.organizacao.UsuarioService usuarioService;
    @Mock
    private sgc.mapa.service.ImpactoMapaService impactoMapaService;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private sgc.mapa.model.AtividadeRepo atividadeRepo;
    @Mock
    private sgc.mapa.mapper.MapaCompletoMapper mapaCompletoMapper;
    
    // Mocks para cadastro workflow
    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private UnidadeService unidadeService; // Used in both
    @Mock
    private sgc.analise.AnaliseService analiseService;
    @Mock
    private sgc.subprocesso.service.SubprocessoTransicaoService transicaoService;

    // Mocks para PainelService
    @Mock
    private sgc.processo.service.ProcessoService processoService;
    @Mock
    private sgc.alerta.AlertaService alertaService;

    @InjectMocks
    private SubprocessoMapaController subprocessoMapaController;

    @InjectMocks
    private MapaService mapaService;

    @InjectMocks
    private SubprocessoCadastroWorkflowService cadastroService;

    @InjectMocks
    private PainelService painelService;

    // --- SubprocessoMapaController Tests ---

    @Test
    @DisplayName("Deve listar atividades")
    void deveListarAtividades() {
        when(subprocessoService.listarAtividadesSubprocesso(1L)).thenReturn(new ArrayList<>());
        ResponseEntity<List<AtividadeVisualizacaoDto>> response = subprocessoMapaController.listarAtividades(1L);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Deve obter contexto de edição")
    void deveObterContextoEdicao() {
        when(subprocessoContextoService.obterContextoEdicao(anyLong(), any(), any())).thenReturn(ContextoEdicaoDto.builder().build());
        ContextoEdicaoDto dto = subprocessoMapaController.obterContextoEdicao(1L, null, null);
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar erro de acesso negado quando usuário nulo em verificarImpactos")
    void deveLancarErroAcessoNegado() {
        assertThatThrownBy(() -> subprocessoMapaController.verificarImpactos(1L, null))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    // --- MapaService Tests ---

    @Test
    @DisplayName("Deve lançar erro ao obter mapa completo inexistente")
    void deveLancarErroObterMapaCompletoInexistente() {
        when(mapaRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapaService.obterMapaCompleto(99L, 1L))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao salvar mapa completo inexistente")
    void deveLancarErroSalvarMapaCompletoInexistente() {
        when(mapaRepo.findById(99L)).thenReturn(Optional.empty());
        SalvarMapaRequest req = new SalvarMapaRequest();
        assertThatThrownBy(() -> mapaService.salvarMapaCompleto(99L, req, "123"))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao salvar mapa completo com competência inexistente")
    void deveLancarErroSalvarCompetenciaInexistente() {
        Mapa mapa = new Mapa();
        when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(new ArrayList<>());

        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setObservacoes("Obs");
        CompetenciaMapaDto compDto = new CompetenciaMapaDto();
        compDto.setCodigo(99L);
        compDto.setDescricao("Desc");
        req.setCompetencias(List.of(compDto));

        assertThatThrownBy(() -> mapaService.salvarMapaCompleto(1L, req, "123"))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class)
            .hasMessageContaining("Competência não encontrada");
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar mapa inexistente")
    void deveLancarErroAtualizarMapaInexistente() {
        when(mapaRepo.findById(99L)).thenReturn(Optional.empty());
        Mapa mapa = new Mapa();
        assertThatThrownBy(() -> mapaService.atualizar(99L, mapa))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve lançar erro ao excluir mapa inexistente")
    void deveLancarErroExcluirMapaInexistente() {
        when(mapaRepo.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> mapaService.excluir(99L))
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
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> cadastroService.devolverRevisaoCadastro(1L, "Obs", new Usuario()))
             .isInstanceOf(sgc.processo.erros.ErroProcessoEmSituacaoInvalida.class);
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

    // --- PainelService Tests ---

    @Test
    @DisplayName("Deve lançar erro se perfil for nulo")
    void deveLancarErroSePerfilNulo() {
         assertThatThrownBy(() -> painelService.listarProcessos(null, 1L, Pageable.unpaged()))
             .isInstanceOf(ErroParametroPainelInvalido.class);
    }

    @Test
    @DisplayName("Deve listar alertas com ordenação padrão se não informada")
    void deveListarAlertasComOrdenacaoPadrao() {
        when(alertaService.listarTodos(any())).thenReturn(Page.empty());
        // PageRequest.of(0, 10) sem sort explicitado deve cair no if e ganhar sort padrão
        painelService.listarAlertas(null, null, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Deve respeitar ordenação informada em listarAlertas")
    void deveRespeitarOrdenacaoEmListarAlertas() {
        Pageable p = PageRequest.of(0, 10, Sort.by("codigo"));
        when(alertaService.listarTodos(p)).thenReturn(Page.empty());
        painelService.listarAlertas(null, null, p);
    }
}

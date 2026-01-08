package sgc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import sgc.mapa.service.MapaSalvamentoService;
import sgc.mapa.service.MapaService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.painel.PainelService;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.subprocesso.SubprocessoMapaController;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.SubprocessoContextoService;
import sgc.subprocesso.service.SubprocessoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra de Controllers e Services")
class ControllersServicesCoverageTest {
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoContextoService subprocessoContextoService;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private sgc.alerta.AlertaService alertaService;
    @Mock
    private sgc.mapa.model.AtividadeRepo atividadeRepo;
    @Mock
    private sgc.mapa.mapper.MapaCompletoMapper mapaCompletoMapper;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;

    @InjectMocks
    private SubprocessoMapaController subprocessoMapaController;
    @InjectMocks
    private MapaService mapaService;

    @InjectMocks
    private SubprocessoCadastroWorkflowService cadastroService;

    @InjectMocks
    private PainelService painelService;

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

    @Test
    @DisplayName("Deve lançar erro ao obter mapa completo inexistente")
    void deveLancarErroObterMapaCompletoInexistente() {
        when(mapaRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapaService.obterMapaCompleto(99L, 1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve delegar salvar mapa completo inexistente para MapaSalvamentoService")
    void deveDelegarSalvarMapaCompletoInexistente() {
        SalvarMapaRequest req = new SalvarMapaRequest();
        when(mapaSalvamentoService.salvarMapaCompleto(99L, req, "123"))
            .thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 99L));
        assertThatThrownBy(() -> mapaService.salvarMapaCompleto(99L, req, "123"))
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

        when(mapaSalvamentoService.salvarMapaCompleto(1L, req, "123"))
            .thenThrow(new ErroEntidadeNaoEncontrada("Competência não encontrada: 99"));

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

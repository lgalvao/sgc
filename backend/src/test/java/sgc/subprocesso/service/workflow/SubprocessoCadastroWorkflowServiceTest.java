package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroInvarianteViolada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SubprocessoCadastroWorkflowServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock
    private RepositorioComum repo;
    @Mock
    private sgc.mapa.service.CompetenciaService competenciaService;
    @Mock
    private sgc.mapa.service.AtividadeService atividadeService;
    @Mock
    private sgc.mapa.service.MapaFacade mapaFacade;
    @Mock
    private sgc.subprocesso.model.MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private sgc.subprocesso.service.crud.SubprocessoCrudService crudService;

    @InjectMocks
    private SubprocessoWorkflowService service;

    // --- Disponibilizar Cadastro ---

    @Test
    @DisplayName("disponibilizarCadastro sucesso")
    void disponibilizarCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        Unidade u = new Unidade();
        u.setTituloTitular("123");
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setMapa(mapa);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        when(validacaoService.obterAtividadesSemConhecimento(id))
                .thenReturn(Collections.emptyList());

        service.disponibilizarCadastro(id, user);

        assertThat(sp.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        verify(analiseFacade).removerPorSubprocesso(id);
        verify(transicaoService).registrar(
                eq(sp),
                eq(TipoTransicao.CADASTRO_DISPONIBILIZADO),
                any(Unidade.class),
                any(),
                eq(user));
    }

    @Test
    @DisplayName("disponibilizarCadastro falha acesso")
    void disponibilizarCadastroAcesso() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("1");
        Usuario titular = new Usuario();
        titular.setTituloEleitoral("2");
        Unidade u = new Unidade();

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        doThrow(new ErroAccessoNegado("Acesso negado para teste"))
                .when(accessControlService).verificarPermissao(any(), any(), any());

        assertThatThrownBy(() -> service.disponibilizarCadastro(id, user))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("disponibilizarCadastro falha validação")
    void disponibilizarCadastroValidacao() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        Unidade u = new Unidade();
        u.setTituloTitular("123");

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        when(validacaoService.obterAtividadesSemConhecimento(id))
                .thenReturn(List.of(new Atividade()));

        assertThatThrownBy(() -> service.disponibilizarCadastro(id, user))
                .isInstanceOf(ErroValidacao.class);
    }


    // --- Disponibilizar Revisão ---

    @Test
    @DisplayName("disponibilizarRevisao sucesso")
    void disponibilizarRevisao() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        Unidade u = new Unidade();
        u.setTituloTitular("123");
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setMapa(mapa);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        when(validacaoService.obterAtividadesSemConhecimento(id))
                .thenReturn(Collections.emptyList());

        service.disponibilizarRevisao(id, user);

        assertThat(sp.getSituacao())
                .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        verify(analiseFacade).removerPorSubprocesso(id);
        verify(transicaoService).registrar(
                eq(sp),
                eq(TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA),
                any(Unidade.class),
                any(),
                eq(user));
    }

    // --- Devolver Cadastro ---

    @Test
    @DisplayName("devolverCadastro sucesso")
    void devolverCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = new Unidade();
        u.setSigla("U1");
        user.setUnidadeLotacao(u);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        u.setUnidadeSuperior(new Unidade());

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);

        service.devolverCadastro(id, "obs", user);

        assertThat(sp.getDataFimEtapa1()).isNull();

        verify(transicaoService).registrarAnaliseETransicao(argThat(req ->
                req.sp().equals(sp) &&
                req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO &&
                req.tipoTransicao() == TipoTransicao.CADASTRO_DEVOLVIDO &&
                req.usuario().equals(user) &&
                "obs".equals(req.observacoes())
        ));
    }

    // --- Aceitar Cadastro ---

    @Test
    @DisplayName("aceitarCadastro sucesso")
    void aceitarCadastro() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);
        Usuario user = new Usuario();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);

        service.aceitarCadastro(id, "obs", user);

        verify(transicaoService).registrarAnaliseETransicao(argThat(req ->
                req.sp().equals(sp) &&
                req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO &&
                req.tipoTransicao() == TipoTransicao.CADASTRO_ACEITO &&
                req.usuario().equals(user) &&
                "obs".equals(req.observacoes())
        ));
    }

    @Test
    @DisplayName("aceitarCadastro falha sem unidade superior")
    void aceitarCadastroSemSuperior() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setUnidadeSuperior(null);
        sp.setUnidade(u);
        Usuario user = new Usuario();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);

        assertThatThrownBy(() -> service.aceitarCadastro(id, "obs", user))
                .isInstanceOf(ErroInvarianteViolada.class);
    }

    // --- Devolver Revisão Cadastro ---

    @Test
    @DisplayName("devolverRevisaoCadastro falha sem unidade superior")
    void devolverRevisaoCadastroSemSuperior() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade u = new Unidade();
        u.setUnidadeSuperior(null);
        sp.setUnidade(u);
        Usuario user = new Usuario();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);

        assertThatThrownBy(() -> service.devolverRevisaoCadastro(id, "obs", user))
                .isInstanceOf(ErroInvarianteViolada.class);
    }

    // --- Homologar Cadastro ---

    @Test
    @DisplayName("homologarCadastro sucesso")
    void homologarCadastro() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Usuario user = new Usuario();
        Unidade sedoc = new Unidade();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(sedoc);

        service.homologarCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        verify(transicaoService).registrar(
                eq(sp),
                eq(TipoTransicao.CADASTRO_HOMOLOGADO),
                eq(sedoc),
                eq(sedoc),
                eq(user),
                eq("obs"));
    }

    @Test
    @DisplayName("homologarCadastro falha situacao invalida")
    void homologarCadastroSituacaoInvalida() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Usuario user = new Usuario();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        doThrow(new ErroProcessoEmSituacaoInvalida("Situação inválida"))
                .when(accessControlService).verificarPermissao(any(), any(), any());

        assertThatThrownBy(() -> service.homologarCadastro(id, "obs", user))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    // --- Devolver Revisão Cadastro ---

    @Test
    @DisplayName("devolverRevisaoCadastro sucesso")
    void devolverRevisaoCadastro() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setSigla("U1");
        Unidade sup = new Unidade(); // Unidade Superior
        sup.setCodigo(20L);
        sup.setSigla("SUP");
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);

        Usuario user = new Usuario();
        user.setUnidadeLotacao(u);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        
        service.devolverRevisaoCadastro(id, "obs", user);

        assertThat(sp.getDataFimEtapa1()).isNull();

        verify(transicaoService).registrarAnaliseETransicao(argThat(req ->
                req.sp().equals(sp) &&
                req.novaSituacao() == SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO &&
                req.tipoTransicao() == TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA &&
                req.usuario().equals(user) &&
                "obs".equals(req.observacoes())
        ));
    }

    // --- Aceitar Revisão Cadastro ---

    @Test
    @DisplayName("aceitarRevisaoCadastro sucesso")
    void aceitarRevisaoCadastro() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade u = new Unidade();
        u.setCodigo(10L);
        Unidade sup = new Unidade();
        sup.setCodigo(20L);
        sup.setSigla("SUP");
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);

        Usuario user = new Usuario();
        user.setUnidadeLotacao(u);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        
        service.aceitarRevisaoCadastro(id, "obs", user);

        verify(transicaoService).registrarAnaliseETransicao(argThat(req ->
                req.sp().equals(sp) &&
                req.novaSituacao() == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA &&
                req.tipoTransicao() == TipoTransicao.REVISAO_CADASTRO_ACEITA &&
                req.usuario().equals(user) &&
                "obs".equals(req.observacoes())
        ));
    }

    @Test
    @DisplayName("aceitarRevisaoCadastro falha sem unidade superior")
    void aceitarRevisaoCadastroSemSuperior() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade u = new Unidade();
        u.setUnidadeSuperior(null);
        sp.setUnidade(u);
        Usuario user = new Usuario();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);

        assertThatThrownBy(() -> service.aceitarRevisaoCadastro(id, "obs", user))
                .isInstanceOf(ErroInvarianteViolada.class);
    }

    // --- Homologar Revisão Cadastro ---

    @Test
    @DisplayName("homologarRevisaoCadastro com impactos")
    void homologarRevisaoCadastroComImpactos() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Usuario user = new Usuario();

        ImpactoMapaDto impactoDto = ImpactoMapaDto.builder().temImpactos(true).build();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        when(impactoMapaService.verificarImpactos(any(Subprocesso.class), eq(user))).thenReturn(impactoDto);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

        service.homologarRevisaoCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        verify(transicaoService).registrar(
                eq(sp),
                eq(TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA),
                any(Unidade.class),
                any(Unidade.class),
                eq(user),
                eq("obs"));
    }

    @Test
    @DisplayName("homologarRevisaoCadastro sem impactos")
    void homologarRevisaoCadastroSemImpactos() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Usuario user = new Usuario();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        when(impactoMapaService.verificarImpactos(any(Subprocesso.class), eq(user)))
                .thenReturn(ImpactoMapaDto.semImpacto());

        service.homologarRevisaoCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
    }

    @Test
    @DisplayName("homologarRevisaoCadastro falha estado invalido")
    void homologarRevisaoCadastroEstadoInvalido() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Usuario user = new Usuario();

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        doThrow(new ErroProcessoEmSituacaoInvalida("Estado inválido"))
                .when(accessControlService).verificarPermissao(any(), any(), any());

        assertThatThrownBy(() -> service.homologarRevisaoCadastro(id, "obs", user))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    // --- Validação de Lista Vazia de Atividades (CDU-09/10) ---

    @Test
    @DisplayName("disponibilizarCadastro deve rejeitar quando não há atividades cadastradas")
    void disponibilizarCadastro_deveRejeitarQuandoListaVazia() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        Unidade u = new Unidade();
        u.setTituloTitular("123");
        Mapa mapa = new Mapa(); 
        mapa.setCodigo(10L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(mapa);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        // Removed unnecessary stubbing
        doThrow(
                        new ErroValidacao(
                                "Pelo menos uma atividade deve ser cadastrada antes de"
                                        + " disponibilizar."))
                .when(validacaoService)
                .validarExistenciaAtividades(id);

        assertThatThrownBy(() -> service.disponibilizarCadastro(id, user))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Pelo menos uma atividade deve ser cadastrada");
    }

    @Test
    @DisplayName("disponibilizarRevisao deve rejeitar quando não há atividades cadastradas")
    void disponibilizarRevisao_deveRejeitarQuandoListaVazia() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        Unidade u = new Unidade();
        u.setTituloTitular("123");
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(mapa);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);
        // Removed unnecessary stubbing
        doThrow(
                        new ErroValidacao(
                                "Pelo menos uma atividade deve ser cadastrada antes de"
                                        + " disponibilizar."))
                .when(validacaoService)
                .validarExistenciaAtividades(id);

        assertThatThrownBy(() -> service.disponibilizarRevisao(id, user))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Pelo menos uma atividade deve ser cadastrada");
    }

    @Test
    @DisplayName("aceitarRevisaoCadastro deve usar unidadeAnalise como destino se superior for null")
    void aceitarRevisaoCadastroFallbackUnidade() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        Unidade sup = new Unidade();
        sup.setUnidadeSuperior(null); // No upper unit
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);

        when(repo.buscar(Subprocesso.class, id)).thenReturn(sp);

        service.aceitarRevisaoCadastro(id, "obs", new Usuario());

        verify(transicaoService).registrarAnaliseETransicao(argThat(req ->
                req.unidadeAnalise().equals(sup) &&
                req.unidadeOrigemTransicao().equals(sup) &&
                req.unidadeDestinoTransicao().equals(sup)
        ));
    }
}

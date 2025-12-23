package sgc.subprocesso.internal.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import sgc.analise.AnaliseService;
import sgc.atividade.internal.model.Atividade;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.api.ImpactoMapaDto;
import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.service.ImpactoMapaService;
import sgc.processo.api.eventos.*;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.internal.model.Usuario;
import sgc.subprocesso.internal.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoCadastroWorkflowServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private ApplicationEventPublisher publicadorDeEventos;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private AnaliseService analiseService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private SubprocessoCadastroWorkflowService service;

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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(subprocessoService.obterAtividadesSemConhecimento(id))
                .thenReturn(Collections.emptyList());

        service.disponibilizarCadastro(id, user);

        assertThat(sp.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        verify(publicadorDeEventos)
                .publishEvent(any(EventoSubprocessoCadastroDisponibilizado.class));
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(subprocessoService.obterAtividadesSemConhecimento(id))
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(subprocessoService.obterAtividadesSemConhecimento(id))
                .thenReturn(Collections.emptyList());

        service.disponibilizarRevisao(id, user);

        assertThat(sp.getSituacao())
                .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        verify(publicadorDeEventos)
                .publishEvent(any(EventoSubprocessoRevisaoDisponibilizada.class));
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.devolverCadastro(id, "obs", user);

        assertThat(sp.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(analiseService).criarAnalise(any());
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoCadastroDevolvido.class));
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.aceitarCadastro(id, "obs", user);

        assertThat(sp.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoCadastroAceito.class));
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.aceitarCadastro(id, "obs", user))
                .isInstanceOf(IllegalStateException.class);
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        service.homologarCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoCadastroHomologado.class));
    }

    @Test
    @DisplayName("homologarCadastro falha situacao invalida")
    void homologarCadastroSituacaoInvalida() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.homologarCadastro(id, "obs", new Usuario()))
                .isInstanceOf(IllegalStateException.class);
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        
        service.devolverRevisaoCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoRevisaoDevolvida.class));
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        
        service.aceitarRevisaoCadastro(id, "obs", user);

        assertThat(sp.getSituacao())
                .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoRevisaoAceita.class));
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(impactoMapaService.verificarImpactos(id, user)).thenReturn(impactoDto);
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(new Unidade()));

        service.homologarRevisaoCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoRevisaoHomologada.class));
    }

    @Test
    @DisplayName("homologarRevisaoCadastro sem impactos")
    void homologarRevisaoCadastroSemImpactos() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Usuario user = new Usuario();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(impactoMapaService.verificarImpactos(id, user))
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.homologarRevisaoCadastro(id, "obs", new Usuario()))
                .isInstanceOf(IllegalStateException.class);
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        // Removed unnecessary stubbing
        org.mockito.Mockito.doThrow(
                        new ErroValidacao(
                                "Pelo menos uma atividade deve ser cadastrada antes de"
                                        + " disponibilizar."))
                .when(subprocessoService)
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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        // Removed unnecessary stubbing
        org.mockito.Mockito.doThrow(
                        new ErroValidacao(
                                "Pelo menos uma atividade deve ser cadastrada antes de"
                                        + " disponibilizar."))
                .when(subprocessoService)
                .validarExistenciaAtividades(id);

        assertThatThrownBy(() -> service.disponibilizarRevisao(id, user))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Pelo menos uma atividade deve ser cadastrada");
    }
}
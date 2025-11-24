package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import sgc.analise.AnaliseService;
import sgc.atividade.model.Atividade;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Mapa;
import sgc.processo.eventos.*;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubprocessoWorkflowServiceTest {

    @Mock private SubprocessoRepo repositorioSubprocesso;
    @Mock private ApplicationEventPublisher publicadorDeEventos;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private AnaliseService analiseService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private ImpactoMapaService impactoMapaService;

    @InjectMocks private SubprocessoWorkflowService service;

    // --- Disponibilizar Cadastro ---

    @Test
    @DisplayName("disponibilizarCadastro sucesso")
    void disponibilizarCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = new Unidade();
        u.setTitular(user);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setMapa(mapa);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(subprocessoService.obterAtividadesSemConhecimento(id)).thenReturn(Collections.emptyList());

        service.disponibilizarCadastro(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoCadastroDisponibilizado.class));
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
        u.setTitular(titular);

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
        Unidade u = new Unidade();
        u.setTitular(user);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(subprocessoService.obterAtividadesSemConhecimento(id)).thenReturn(List.of(new Atividade()));

        assertThatThrownBy(() -> service.disponibilizarCadastro(id, user))
            .isInstanceOf(ErroValidacao.class);
    }

    // --- Disponibilizar Revisão ---

    @Test
    @DisplayName("disponibilizarRevisao sucesso")
    void disponibilizarRevisao() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = new Unidade();
        u.setTitular(user);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setMapa(mapa);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(subprocessoService.obterAtividadesSemConhecimento(id)).thenReturn(Collections.emptyList());

        service.disponibilizarRevisao(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoRevisaoDisponibilizada.class));
    }

    // --- Disponibilizar Mapa ---

    @Test
    @DisplayName("disponibilizarMapa sucesso")
    void disponibilizarMapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);
        Unidade u = new Unidade();
        sp.setUnidade(u);

        Usuario user = new Usuario();
        Unidade sedoc = new Unidade();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        service.disponibilizarMapa(id, "obs", LocalDateTime.now(), user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaDisponibilizado.class));
    }

    @Test
    @DisplayName("disponibilizarMapa falha estado invalido")
    void disponibilizarMapaEstadoInvalido() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.disponibilizarMapa(id, "obs", null, new Usuario()))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("disponibilizarMapa falha mapa null")
    void disponibilizarMapaNull() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        sp.setMapa(null);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.disponibilizarMapa(id, "obs", null, new Usuario()))
            .isInstanceOf(IllegalStateException.class);
    }

    // --- Apresentar Sugestões ---

    @Test
    @DisplayName("apresentarSugestoes sucesso")
    void apresentarSugestoes() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());
        sp.setMapa(new Mapa());
        Usuario user = new Usuario();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.apresentarSugestoes(id, "sug", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_COM_SUGESTOES);
        assertThat(sp.getMapa().getSugestoes()).isEqualTo("sug");
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaComSugestoes.class));
    }

    // --- Validar Mapa ---

    @Test
    @DisplayName("validarMapa sucesso")
    void validarMapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(new Unidade());
        Usuario user = new Usuario();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.validarMapa(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_VALIDADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaValidado.class));
    }

    // --- Devolver Validação ---

    @Test
    @DisplayName("devolverValidacao sucesso")
    void devolverValidacao() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setSigla("U1");
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);

        Usuario user = new Usuario();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.devolverValidacao(id, "justificativa", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        verify(analiseService).criarAnalise(any());
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaDevolvido.class));
    }

    // --- Aceitar Validação ---

    @Test
    @DisplayName("aceitarValidacao homologado se nao houver proxima unidade")
    void aceitarValidacaoHomologado() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        u.setUnidadeSuperior(sup); // Sup nao tem superior
        sp.setUnidade(u);
        Usuario user = new Usuario();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.aceitarValidacao(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        // Should verify no event is published if that's the logic, or verify specific logic
    }

    @Test
    @DisplayName("aceitarValidacao validado se houver proxima unidade")
    void aceitarValidacaoProximaEtapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        Unidade sup2 = new Unidade();
        sup.setUnidadeSuperior(sup2);
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);
        Usuario user = new Usuario();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.aceitarValidacao(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_VALIDADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaAceito.class));
    }

    // --- Homologar Validação ---

    @Test
    @DisplayName("homologarValidacao sucesso")
    void homologarValidacao() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Usuario user = new Usuario();
        Unidade sedoc = new Unidade();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        service.homologarValidacao(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaHomologado.class));
    }

    // --- Submeter Mapa Ajustado ---

    @Test
    @DisplayName("submeterMapaAjustado sucesso")
    void submeterMapaAjustado() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(new Unidade());
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        Usuario user = new Usuario();
        SubmeterMapaAjustadoReq req = new SubmeterMapaAjustadoReq();
        req.setDataLimiteEtapa2(LocalDateTime.now());

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.submeterMapaAjustado(id, req, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaAjustadoSubmetido.class));
    }

    // --- Devolver Cadastro ---

    @Test
    @DisplayName("devolverCadastro sucesso")
    void devolverCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = new Unidade();
        u.setSigla("U1");
        user.setUnidade(u);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        u.setUnidadeSuperior(new Unidade());

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.devolverCadastro(id, "motivo", "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
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

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
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
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        Usuario user = new Usuario();
        Unidade sedoc = new Unidade();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        service.homologarCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
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
        sp.setUnidade(u);

        Usuario user = new Usuario();
        user.setUnidade(u);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findById(10L)).thenReturn(Optional.of(u));

        service.devolverRevisaoCadastro(id, "motivo", "obs", user);

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
        user.setUnidade(u);

        Unidade destino = new Unidade();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findById(10L)).thenReturn(Optional.of(u));
        when(unidadeRepo.findById(20L)).thenReturn(Optional.of(destino));

        service.aceitarRevisaoCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
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
        when(impactoMapaService.verificarImpactos(id, user)).thenReturn(ImpactoMapaDto.semImpacto());

        service.homologarRevisaoCadastro(id, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        // Verify no event or verify a potential homolocated event?
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
}

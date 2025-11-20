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
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Mapa;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubprocessoWorkflowServiceTest {

    @Mock private SubprocessoRepo repositorioSubprocesso;
    @Mock private SubprocessoMovimentacaoRepo repositorioMovimentacao;
    @Mock private ApplicationEventPublisher publicadorDeEventos;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private AnaliseService analiseService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private SubprocessoNotificacaoService subprocessoNotificacaoService;
    @Mock private ImpactoMapaService impactoMapaService;

    @InjectMocks private SubprocessoWorkflowService service;

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
        verify(repositorioMovimentacao).save(any());
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
    }

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
    }

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
    }

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

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.devolverCadastro(id, "motivo", "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        verify(analiseService).criarAnalise(any());
    }

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
    }

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
    }

    @Test
    @DisplayName("apresentarSugestoes sucesso")
    void apresentarSugestoes() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(new Unidade());
        sp.setMapa(new Mapa());
        Usuario user = new Usuario();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        service.apresentarSugestoes(id, "sug", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_COM_SUGESTOES);
        assertThat(sp.getMapa().getSugestoes()).isEqualTo("sug");
    }
}

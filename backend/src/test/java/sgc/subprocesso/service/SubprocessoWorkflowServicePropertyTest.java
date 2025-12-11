package sgc.subprocesso.service;

import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.springframework.context.ApplicationEventPublisher;
import sgc.analise.AnaliseService;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.service.ImpactoMapaService;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubprocessoWorkflowServicePropertyTest {

    private SubprocessoRepo repositorioSubprocesso = mock(SubprocessoRepo.class);
    private ApplicationEventPublisher publicadorDeEventos = mock(ApplicationEventPublisher.class);
    private UnidadeRepo unidadeRepo = mock(UnidadeRepo.class);
    private AnaliseService analiseService = mock(AnaliseService.class);
    private SubprocessoService subprocessoService = mock(SubprocessoService.class);
    private ImpactoMapaService impactoMapaService = mock(ImpactoMapaService.class);

    private SubprocessoWorkflowService service =
            new SubprocessoWorkflowService(
                    repositorioSubprocesso,
                    publicadorDeEventos,
                    unidadeRepo,
                    analiseService,
                    subprocessoService,
                    impactoMapaService);

    @Property
    void disponibilizarMapaDeveFalharSeEstadoInvalido(
            @ForAll SituacaoSubprocesso situacao, @ForAll String obs) {
        Assume.that(
                situacao != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                        && situacao != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO
                        && situacao != SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(situacao);

        when(repositorioSubprocesso.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        Usuario usuario = new Usuario();

        Throwable thrown =
                catchThrowable(
                        () -> service.disponibilizarMapa(codSubprocesso, obs, null, usuario));

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado atual: " + situacao);
    }

    @Property
    void disponibilizarCadastroDeveFalharSeUsuarioNaoForTitular(
            @ForAll String usuarioId, @ForAll String titularId) {
        Assume.that(usuarioId != null && !usuarioId.equals(titularId));

        Long codSubprocesso = 1L;
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);

        Usuario titular = new Usuario();
        titular.setTituloEleitoral(titularId);

        Unidade unidade = new Unidade();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setUnidade(unidade);

        when(repositorioSubprocesso.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        Throwable thrown =
                catchThrowable(() -> service.disponibilizarCadastro(codSubprocesso, usuario));

        assertThat(thrown).isInstanceOf(ErroAccessoNegado.class);
    }
}

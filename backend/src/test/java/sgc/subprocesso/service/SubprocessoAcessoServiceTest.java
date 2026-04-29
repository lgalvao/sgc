package sgc.subprocesso.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoAcessoServiceTest {

    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private SubprocessoAcessoService acessoService;

    @Test
    void shouldResolverPermissoesForFinalizado() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.SERVIDOR, true, true, true, true);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.habilitarEditarCadastro()).isFalse();
        assertThat(dto.habilitarDisponibilizarCadastro()).isFalse();
        assertThat(dto.habilitarDevolverCadastro()).isFalse();
        assertThat(dto.habilitarAceitarCadastro()).isFalse();
        assertThat(dto.habilitarHomologarCadastro()).isFalse();
        assertThat(dto.habilitarEditarMapa()).isFalse();
        assertThat(dto.habilitarDisponibilizarMapa()).isFalse();
        assertThat(dto.habilitarValidarMapa()).isFalse();
        assertThat(dto.habilitarApresentarSugestoes()).isFalse();
        assertThat(dto.habilitarDevolverMapa()).isFalse();
        assertThat(dto.habilitarAceitarMapa()).isFalse();
        assertThat(dto.habilitarHomologarMapa()).isFalse();
        assertThat(dto.habilitarAcessoCadastro()).isTrue();
    }

    @Test
    void shouldResolverPermissoesForMapeamentoCadastroEmAndamentoChefeMesmaUnidade() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.CHEFE, true, true, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.habilitarEditarCadastro()).isTrue();
        assertThat(dto.habilitarDisponibilizarCadastro()).isTrue();
        assertThat(dto.habilitarAcessoCadastro()).isTrue();
        assertThat(dto.podeReabrirCadastro()).isFalse();
    }

    @Test
    void shouldResolverPermissoesForGestorAnaliseCadastro() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.GESTOR, true, true, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.habilitarDevolverCadastro()).isTrue();
        assertThat(dto.habilitarAceitarCadastro()).isTrue();
        assertThat(dto.habilitarHomologarCadastro()).isFalse(); // Only Admin
    }

    @Test
    void shouldResolverPermissoesForAdminAnaliseCadastro() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, true, true, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.habilitarDevolverCadastro()).isTrue();
        assertThat(dto.habilitarAceitarCadastro()).isFalse(); // Only Gestor
        assertThat(dto.habilitarHomologarCadastro()).isTrue();
    }

    @Test
    void shouldResolverPermissoesForAcessoMapaHabilitadoGestor() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.GESTOR, false, true, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.habilitarAcessoMapa()).isTrue();
    }

    @Test
    void shouldResolverPermissoesForAcessoMapaAdminApenasHomologado() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, false, true, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.habilitarAcessoMapa()).isTrue();
    }

    @Test
    void shouldVisualizarImpactoWhenTemMapaVigente() {
        when(impactoMapaService.podeVisualizarImpactos(any())).thenReturn(true);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.SERVIDOR, false, true, false, true);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeVisualizarImpacto()).isTrue();
    }

    @Test
    void shouldReabrirCadastroAdmin() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, false, false, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);
        assertThat(dto.podeReabrirCadastro()).isTrue();
    }

    @Test
    void shouldReabrirRevisaoAdmin() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, false, false, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);
        assertThat(dto.podeReabrirRevisao()).isTrue();
    }

    @Test
    void shouldNaoPermitirEditarMapaQuandoMapaValidadoNaAnaliseFinal() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, true, true, false, true);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeEditarMapa()).isFalse();
        assertThat(dto.habilitarEditarMapa()).isFalse();
        assertThat(dto.podeHomologarMapa()).isTrue();
        assertThat(dto.habilitarHomologarMapa()).isTrue();
    }

    private SubprocessoConsultaService.ContextoConsultaSubprocesso createContexto(
            Subprocesso subprocesso, Perfil perfil, boolean mesmaUnidade, boolean isHierarquia,
            boolean processoFinalizado, boolean temMapaVigente) {

        return new SubprocessoConsultaService.ContextoConsultaSubprocesso(
                subprocesso,
                perfil,
                new Unidade(),
                processoFinalizado,
                mesmaUnidade,
                mesmaUnidade,
                isHierarquia,
                temMapaVigente
        );
    }
}

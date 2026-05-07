package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.mapa.service.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

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
                subprocesso, Perfil.ADMIN, true, true, true, true);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeAlterarDataLimite()).isTrue();
        assertThat(dto.podeReabrirCadastro()).isTrue();
        assertThat(dto.podeReabrirRevisao()).isTrue();
        assertThat(dto.podeEnviarLembrete()).isTrue();
        assertThat(dto.habilitarAlterarDataLimite()).isFalse();
        assertThat(dto.habilitarReabrirCadastro()).isFalse();
        assertThat(dto.habilitarReabrirRevisao()).isFalse();
        assertThat(dto.habilitarEnviarLembrete()).isFalse();
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
        assertThat(dto.habilitarReabrirCadastro()).isTrue();
    }

    @Test
    void shouldReabrirRevisaoAdmin() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, false, false, false, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);
        assertThat(dto.podeReabrirRevisao()).isTrue();
        assertThat(dto.habilitarReabrirRevisao()).isTrue();
    }

    @Test
    void shouldNaoPermitirEditarMapaQuandoMapaValidadoNaAnaliseFinal() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, true, true, false, true);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeEditarMapa()).isTrue();
        assertThat(dto.habilitarEditarMapa()).isFalse();
        assertThat(dto.podeDevolverMapa()).isTrue();
        assertThat(dto.habilitarDevolverMapa()).isFalse();
        assertThat(dto.podeHomologarMapa()).isTrue();
        assertThat(dto.habilitarHomologarMapa()).isTrue();
    }

    @Test
    void shouldManterHomologarMapaVisivelMasDesabilitadoQuandoAdminEstaEmMapaComSugestoes() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, true, true, false, true);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeHomologarMapa()).isTrue();
        assertThat(dto.habilitarHomologarMapa()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(SituacaoSubprocesso.class)
    void shouldNuncaHabilitarEscritaForaDaUnidade(SituacaoSubprocesso situacao) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(situacao);

        for (Perfil perfil : Perfil.values()) {
            PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(
                    createContexto(subprocesso, perfil, false, false, false, false));

            assertThat(permissoes)
                    .satisfies(dto -> assertThat(dto.habilitarEditarCadastro()).as("editar cadastro %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarDisponibilizarCadastro()).as("disponibilizar cadastro %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarDevolverCadastro()).as("devolver cadastro %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarAceitarCadastro()).as("aceitar cadastro %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarHomologarCadastro()).as("homologar cadastro %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarEditarMapa()).as("editar mapa %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarDisponibilizarMapa()).as("disponibilizar mapa %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarValidarMapa()).as("validar mapa %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarApresentarSugestoes()).as("apresentar sugestoes %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarDevolverMapa()).as("devolver mapa %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarAceitarMapa()).as("aceitar mapa %s %s", situacao, perfil).isFalse())
                    .satisfies(dto -> assertThat(dto.habilitarHomologarMapa()).as("homologar mapa %s %s", situacao, perfil).isFalse());
        }
    }

    @Test
    void shouldPermitirEditarCadastroApenasParaChefeNaUnidadeEmRascunho() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        assertThat(acessoService.resolverPermissoes(createContexto(subprocesso, Perfil.CHEFE, true, true, false, false))
                .habilitarEditarCadastro()).isTrue();
        assertThat(acessoService.resolverPermissoes(createContexto(subprocesso, Perfil.GESTOR, true, true, false, false))
                .habilitarEditarCadastro()).isFalse();
        assertThat(acessoService.resolverPermissoes(createContexto(subprocesso, Perfil.ADMIN, true, true, false, false))
                .habilitarEditarCadastro()).isFalse();
    }

    @Test
    void shouldPermitirValidacaoDeMapaApenasParaChefe() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);

        PermissoesSubprocessoDto permissoesChefe = acessoService.resolverPermissoes(
                createContexto(subprocesso, Perfil.CHEFE, true, true, false, false));
        PermissoesSubprocessoDto permissoesGestor = acessoService.resolverPermissoes(
                createContexto(subprocesso, Perfil.GESTOR, true, true, false, false));

        assertThat(permissoesChefe.habilitarValidarMapa()).isTrue();
        assertThat(permissoesChefe.habilitarApresentarSugestoes()).isTrue();
        assertThat(permissoesGestor.habilitarValidarMapa()).isFalse();
        assertThat(permissoesGestor.habilitarApresentarSugestoes()).isFalse();
    }

    @Test
    void shouldManterComandosAdministrativosDisponiveisMasDesabilitarEscritaQuandoProcessoFinalizado() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

        PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(
                createContexto(subprocesso, Perfil.ADMIN, true, true, true, false));

        assertThat(permissoes.podeAlterarDataLimite()).isTrue();
        assertThat(permissoes.podeReabrirCadastro()).isTrue();
        assertThat(permissoes.habilitarAlterarDataLimite()).isFalse();
        assertThat(permissoes.habilitarReabrirCadastro()).isFalse();
        assertThat(permissoes.habilitarEditarCadastro()).isFalse();
        assertThat(permissoes.habilitarDisponibilizarMapa()).isFalse();
        assertThat(permissoes.habilitarHomologarMapa()).isFalse();
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

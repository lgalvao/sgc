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

import java.util.function.*;

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
    void deveDesabilitarEscritaQuandoForaDaUnidade(SituacaoSubprocesso situacao) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(situacao);

        for (Perfil perfil : Perfil.values()) {
            PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(
                    createContexto(subprocesso, perfil, false, false, false, false));

            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarEditarCadastro, "editar cadastro", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarDisponibilizarCadastro, "disponibilizar cadastro", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarDevolverCadastro, "devolver cadastro", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarAceitarCadastro, "aceitar cadastro", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarHomologarCadastro, "homologar cadastro", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarEditarMapa, "editar mapa", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarDisponibilizarMapa, "disponibilizar mapa", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarValidarMapa, "validar mapa", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarApresentarSugestoes, "apresentar sugestões", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarDevolverMapa, "devolver mapa", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarAceitarMapa, "aceitar mapa", situacao, perfil);
            assertPermissaoEscritaDesabilitada(permissoes, PermissoesSubprocessoDto::habilitarHomologarMapa, "homologar mapa", situacao, perfil);
        }
    }

    @Test
    void devePermitirEditarCadastroApenasParaChefeNaUnidadeEmRascunho() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        assertThat(resolverPermissoes(subprocesso, Perfil.CHEFE, true, true, false, false).habilitarEditarCadastro()).isTrue();
        assertThat(resolverPermissoes(subprocesso, Perfil.GESTOR, true, true, false, false).habilitarEditarCadastro()).isFalse();
        assertThat(resolverPermissoes(subprocesso, Perfil.ADMIN, true, true, false, false).habilitarEditarCadastro()).isFalse();
    }

    @Test
    void devePermitirValidacaoDeMapaApenasParaChefe() {
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
    void devePermitirConcluirDiagnosticoParaChefeEmAndamentoNaPropriaUnidade() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(DIAGNOSTICO_EM_ANDAMENTO);

        assertThat(resolverPermissoes(subprocesso, Perfil.CHEFE, true, true, false, false).habilitarConcluirDiagnostico()).isTrue();
        assertThat(resolverPermissoes(subprocesso, Perfil.GESTOR, true, true, false, false).habilitarConcluirDiagnostico()).isFalse();
        assertThat(resolverPermissoes(subprocesso, Perfil.CHEFE, false, true, false, false).habilitarConcluirDiagnostico()).isFalse();
    }

    @Test
    void deveDesabilitarEscritaEmProcessoFinalizadoMasPermitirComandosAdmin() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

        PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(
                createContexto(subprocesso, Perfil.ADMIN, true, true, true, false));

        assertThat(permissoes.podeAlterarDataLimite())
                .as("a ação continua visível para admin mesmo com o processo finalizado")
                .isTrue();
        assertThat(permissoes.podeReabrirCadastro())
                .as("a reabertura continua disponível em tese para admin mesmo com o processo finalizado")
                .isTrue();
        assertThat(permissoes.habilitarAlterarDataLimite())
                .as("processo finalizado deve bloquear a execução imediata da ação")
                .isFalse();
        assertThat(permissoes.habilitarReabrirCadastro())
                .as("processo finalizado deve bloquear a execução imediata da reabertura")
                .isFalse();
        assertThat(permissoes.habilitarEditarCadastro()).isFalse();
        assertThat(permissoes.habilitarDisponibilizarMapa()).isFalse();
        assertThat(permissoes.habilitarHomologarMapa()).isFalse();
    }

    @Test
    void podeVerSugestoesTrueParaAdminEmProcessoFinalizadoComSugestoes() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, true, true, true, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeVerSugestoes()).isTrue();
    }

    @Test
    void podeVerSugestoesFalseParaServidorEmProcessoFinalizadoComSugestoes() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.SERVIDOR, true, true, true, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeVerSugestoes()).isFalse();
    }

    @Test
    void podeVerSugestoesFalseParaAdminEmProcessoFinalizadoSemSugestoes() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.ADMIN, true, true, true, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeVerSugestoes()).isFalse();
    }

    @Test
    void devePermitirDevolverMapaParaGestorEmProcessoFinalizado() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.GESTOR, true, true, true, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeDevolverMapa()).isTrue();
    }

    @Test
    void naoDevePermitirDevolverMapaParaChefeEmProcessoFinalizado() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        SubprocessoConsultaService.ContextoConsultaSubprocesso contexto = createContexto(
                subprocesso, Perfil.CHEFE, true, true, true, false);

        PermissoesSubprocessoDto dto = acessoService.resolverPermissoes(contexto);

        assertThat(dto.podeDevolverMapa()).isFalse();
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

    private PermissoesSubprocessoDto resolverPermissoes(
            Subprocesso subprocesso, Perfil perfil, boolean mesmaUnidade, boolean isHierarquia,
            boolean processoFinalizado, boolean temMapaVigente) {
        return acessoService.resolverPermissoes(createContexto(
                subprocesso, perfil, mesmaUnidade, isHierarquia, processoFinalizado, temMapaVigente));
    }

    private void assertPermissaoEscritaDesabilitada(
            PermissoesSubprocessoDto permissoes,
            Function<PermissoesSubprocessoDto, Boolean> extrator,
            String nomePermissao,
            SituacaoSubprocesso situacao,
            Perfil perfil) {
        assertThat(extrator.apply(permissoes))
                .as("%s %s %s", nomePermissao, situacao, perfil)
                .isFalse();
    }
}

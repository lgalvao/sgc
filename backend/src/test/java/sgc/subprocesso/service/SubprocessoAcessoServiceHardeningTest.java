package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoAcessoService - Endurecimento de Permissões UI")
class SubprocessoAcessoServiceHardeningTest {

    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private SubprocessoAcessoService acessoService;

    @ParameterizedTest
    @EnumSource(SituacaoSubprocesso.class)
    @DisplayName("Regra de Ouro UI: Nenhuma ação de escrita deve estar habilitada fora da unidade")
    void regraDeOuroUI_ForaDaUnidade_NenhumaEscritaHabilitada(SituacaoSubprocesso situacao) {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(situacao);

        // Testamos para todos os perfis, simulando mesmaUnidade = false
        for (Perfil perfil : Perfil.values()) {
            var contexto = createContexto(sp, perfil, false, false, false);
            PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(contexto);

            assertThat(permissoes.habilitarEditarCadastro()).as("Situação %s Perfil %s", situacao, perfil).isFalse();
            assertThat(permissoes.habilitarDisponibilizarCadastro()).isFalse();
            assertThat(permissoes.habilitarDevolverCadastro()).isFalse();
            assertThat(permissoes.habilitarAceitarCadastro()).isFalse();
            assertThat(permissoes.habilitarHomologarCadastro()).isFalse();
            assertThat(permissoes.habilitarEditarMapa()).isFalse();
            assertThat(permissoes.habilitarDisponibilizarMapa()).isFalse();
            assertThat(permissoes.habilitarValidarMapa()).isFalse();
            assertThat(permissoes.habilitarApresentarSugestoes()).isFalse();
            assertThat(permissoes.habilitarDevolverMapa()).isFalse();
            assertThat(permissoes.habilitarAceitarMapa()).isFalse();
            assertThat(permissoes.habilitarHomologarMapa()).isFalse();
        }
    }

    @Test
    @DisplayName("Cadastro: Deve habilitar edição apenas para CHEFE na unidade em situações de rascunho")
    void cadastro_HabilitarEdicaoCorretamente() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        // CHEFE na unidade -> OK
        var ctxChefeOk = createContexto(sp, Perfil.CHEFE, true, true, false);
        assertThat(acessoService.resolverPermissoes(ctxChefeOk).habilitarEditarCadastro()).isTrue();

        // GESTOR na unidade -> NOK (apenas Chefe edita cadastro rascunho)
        var ctxGestorOk = createContexto(sp, Perfil.GESTOR, true, true, false);
        assertThat(acessoService.resolverPermissoes(ctxGestorOk).habilitarEditarCadastro()).isFalse();

        // ADMIN na unidade -> NOK (Admin homologa, não edita rascunho de cadastro)
        var ctxAdminOk = createContexto(sp, Perfil.ADMIN, true, true, false);
        assertThat(acessoService.resolverPermissoes(ctxAdminOk).habilitarEditarCadastro()).isFalse();
    }

    @Test
    @DisplayName("Análise Cadastro: Deve segregar ACEITAR (Gestor) e HOMOLOGAR (Admin)")
    void analiseCadastro_SegregacaoPerfis() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        // GESTOR na unidade -> Pode Aceitar, não pode Homologar
        var ctxGestor = createContexto(sp, Perfil.GESTOR, true, true, false);
        var pGestor = acessoService.resolverPermissoes(ctxGestor);
        assertThat(pGestor.habilitarAceitarCadastro()).isTrue();
        assertThat(pGestor.habilitarHomologarCadastro()).isFalse();

        // ADMIN na unidade -> Pode Homologar, não pode Aceitar
        var ctxAdmin = createContexto(sp, Perfil.ADMIN, true, true, false);
        var pAdmin = acessoService.resolverPermissoes(ctxAdmin);
        assertThat(pAdmin.habilitarAceitarCadastro()).isFalse();
        assertThat(pAdmin.habilitarHomologarCadastro()).isTrue();
    }

    @Test
    @DisplayName("Mapa: Validação deve ser exclusiva para CHEFE")
    void mapa_ValidacaoExclusivaChefe() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);

        // CHEFE na unidade -> Habilita Validar e Sugerir
        var ctxChefe = createContexto(sp, Perfil.CHEFE, true, true, false);
        var pChefe = acessoService.resolverPermissoes(ctxChefe);
        assertThat(pChefe.habilitarValidarMapa()).isTrue();
        assertThat(pChefe.habilitarApresentarSugestoes()).isTrue();

        // GESTOR na unidade -> Não habilita validação (Gestor aceita/devolve depois)
        var ctxGestor = createContexto(sp, Perfil.GESTOR, true, true, false);
        assertThat(acessoService.resolverPermissoes(ctxGestor).habilitarValidarMapa()).isFalse();
    }

    @Test
    @DisplayName("Admin: Comandos de sistema devem respeitar a situação do processo")
    void admin_ComandosSistema() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

        var ctxAdmin = createContexto(sp, Perfil.ADMIN, false, false, false);
        var permissoes = acessoService.resolverPermissoes(ctxAdmin);

        assertThat(permissoes.podeAlterarDataLimite()).isTrue();
        assertThat(permissoes.habilitarAlterarDataLimite()).isTrue(); // Admin altera de qualquer lugar
        assertThat(permissoes.habilitarReabrirCadastro()).isTrue();
    }

    @Test
    @DisplayName("Processo Finalizado: Tudo deve estar desabilitado para escrita")
    void processoFinalizado_DesabilitaEscrita() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

        var contexto = createContexto(sp, Perfil.ADMIN, true, true, true);
        PermissoesSubprocessoDto permissoes = acessoService.resolverPermissoes(contexto);

        assertThat(permissoes.habilitarEditarCadastro()).isFalse();
        assertThat(permissoes.habilitarDisponibilizarMapa()).isFalse();
        assertThat(permissoes.habilitarHomologarMapa()).isFalse();
        // Leitura pode continuar habilitada se necessário, mas escrita NUNCA
    }

    private SubprocessoConsultaService.ContextoConsultaSubprocesso createContexto(
            Subprocesso sp, Perfil perfil, boolean mesmaUnidade, boolean isHierarquia, boolean finalizado) {
        return new SubprocessoConsultaService.ContextoConsultaSubprocesso(
                sp, perfil, new Unidade(), finalizado, mesmaUnidade, mesmaUnidade, isHierarquia, false
        );
    }
}

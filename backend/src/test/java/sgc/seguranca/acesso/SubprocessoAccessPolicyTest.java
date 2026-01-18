package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.service.HierarquiaService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoAccessPolicyTest {

    @InjectMocks
    private SubprocessoAccessPolicy policy;

    @Mock
    private HierarquiaService hierarquiaService;

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Chefe Mesma Unidade")
    void canExecute_VerificarImpactos_ChefeMesmaUnidade() {
        Usuario u = criarUsuario(Perfil.CHEFE, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);

        assertTrue(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Chefe Outra Unidade")
    void canExecute_VerificarImpactos_ChefeOutraUnidade() {
        Usuario u = criarUsuario(Perfil.CHEFE, 2L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);

        assertFalse(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Gestor Disponibilizada")
    void canExecute_VerificarImpactos_GestorDisponibilizada() {
        Usuario u = criarUsuario(Perfil.GESTOR, 2L); // Unidade doesn't matter for Gestor in this logic
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);

        assertTrue(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Admin Homologada")
    void canExecute_VerificarImpactos_AdminHomologada() {
        Usuario u = criarUsuario(Perfil.ADMIN, 99L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, 1L);

        assertTrue(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - Hierarquia MesmaOuSubordinada - OK")
    void canExecute_HierarquiaMesmaOuSubordinada_OK() {
        Usuario u = criarUsuario(Perfil.SERVIDOR, 1L); // Unit 1 (Superior)
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 2L); // Unit 2 (Subordinate)

        // Mock hierarchy service: Unit 2 is subordinate to Unit 1?
        // Wait, "MesmaOuSubordinada" usually means User is in Superior (Unit 1), checking Subprocess (Unit 2).
        // The check is: hierarquiaService.isSubordinada(unidadeSubprocesso, unidadeUsuario)
        // No, let's read code:
        // isSubordinada(unidadeSubprocesso, a.getUnidade())
        // means Subprocesso (Unit 2) is subordinate to User's Unit (Unit 1).

        Unidade unitSub = sp.getUnidade();
        Unidade unitUser = u.getTodasAtribuicoes().iterator().next().getUnidade();

        when(hierarquiaService.isSubordinada(any(), any())).thenAnswer(inv -> {
             Unidade sub = inv.getArgument(0);
             Unidade sup = inv.getArgument(1);
             return sub.getCodigo().equals(2L) && sup.getCodigo().equals(1L);
        });

        assertTrue(policy.canExecute(u, Acao.VISUALIZAR_SUBPROCESSO, sp));
    }

    @Test
    @DisplayName("canExecute - Hierarquia Titular - OK")
    void canExecute_HierarquiaTitular_OK() {
        Usuario u = criarUsuario(Perfil.CHEFE, 1L);
        u.setTituloEleitoral("123");
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);
        sp.getUnidade().setTituloTitular("123");

        assertTrue(policy.canExecute(u, Acao.DISPONIBILIZAR_CADASTRO, sp));
    }

    @Test
    @DisplayName("canExecute - Hierarquia SuperiorImediata - OK")
    void canExecute_HierarquiaSuperiorImediata_OK() {
        // Use GESTOR because ADMIN skips hierarchy checks for this action
        Usuario u = criarUsuario(Perfil.GESTOR, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, 2L);

        Unidade sub = sp.getUnidade();
        Unidade sup = u.getTodasAtribuicoes().iterator().next().getUnidade();
        
        when(hierarquiaService.isSuperiorImediata(sub, sup)).thenReturn(true);

        assertTrue(policy.canExecute(u, Acao.ACEITAR_CADASTRO, sp));
    }

    @Test
    @DisplayName("canExecute - Hierarquia MesmaUnidade - OK")
    void canExecute_HierarquiaMesmaUnidade_OK() {
        Usuario u = criarUsuario(Perfil.ADMIN, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);

        assertTrue(policy.canExecute(u, Acao.EDITAR_CADASTRO, sp));
    }

    @Test
    @DisplayName("canExecute - Acao sem Regras - False")
    void canExecute_AcaoSemRegras() {
        Usuario u = criarUsuario(Perfil.ADMIN, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);

        assertFalse(policy.canExecute(u, Acao.CRIAR_PROCESSO, sp));
    }

    @Test
    @DisplayName("canExecute - Admin Global Override")
    void canExecute_AdminGlobal() {
        Usuario u = criarUsuario(Perfil.ADMIN, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 2L); // Different unit

        // ADMIN should be able to execute administrative actions regardless of hierarchy
        assertTrue(policy.canExecute(u, Acao.VISUALIZAR_SUBPROCESSO, sp));
        assertTrue(policy.canExecute(u, Acao.EDITAR_MAPA, sp));
    }

    @Test
    @DisplayName("canExecute - VerificarImpactos - Complex Logic")
    void canExecute_VerificarImpactos_Complex() {
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);

        // Admin
        Usuario admin = criarUsuario(Perfil.ADMIN, 99L);
        assertTrue(policy.canExecute(admin, Acao.VERIFICAR_IMPACTOS, sp));

        // Gestor
        Usuario gestor = criarUsuario(Perfil.GESTOR, 88L);
        assertTrue(policy.canExecute(gestor, Acao.VERIFICAR_IMPACTOS, sp));

        // Chefe - Wrong Status for Chefe logic (Chefe allows NAO_INICIADO or REVISAO_CADASTRO_EM_ANDAMENTO)
        Usuario chefe = criarUsuario(Perfil.CHEFE, 1L);
        assertFalse(policy.canExecute(chefe, Acao.VERIFICAR_IMPACTOS, sp));

        // Chefe - Correct Status
        Subprocesso spChefe = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);
        assertTrue(policy.canExecute(chefe, Acao.VERIFICAR_IMPACTOS, spChefe));
    }

    private Usuario criarUsuario(Perfil perfil, Long codUnidade) {
        Usuario u = new Usuario();
        u.setTituloEleitoral("123");
        Unidade un = new Unidade();
        un.setCodigo(codUnidade);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(perfil);
        up.setUnidade(un);
        up.setUnidadeCodigo(codUnidade);

        u.setAtribuicoes(Set.of(up));
        return u;
    }

    private Subprocesso criarSubprocesso(SituacaoSubprocesso situacao, Long codUnidade) {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(situacao);
        Unidade un = new Unidade();
        un.setCodigo(codUnidade);
        sp.setUnidade(un);
        return sp;
    }
}

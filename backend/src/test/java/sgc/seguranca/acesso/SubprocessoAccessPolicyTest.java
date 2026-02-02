package sgc.seguranca.acesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.organizacao.model.*;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubprocessoAccessPolicyTest {

    @InjectMocks
    private SubprocessoAccessPolicy policy;

    @Mock
    private HierarquiaService hierarquiaService;
    
    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;
    
    private Map<String, List<UsuarioPerfil>> atribuicoesPorUsuario;
    
    @BeforeEach
    void setUp() {
        atribuicoesPorUsuario = new HashMap<>();
        when(usuarioPerfilRepo.findByUsuarioTitulo(anyString())).thenAnswer(inv -> {
            String titulo = inv.getArgument(0);
            return atribuicoesPorUsuario.getOrDefault(titulo, Collections.emptyList());
        });
    }

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
        // isSubordinada(unidadeSubprocesso, a.unidade())
        // means Subprocesso (Unit 2) is subordinate to User's Unit (Unit 1).

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
        String tituloOriginal = u.getTituloEleitoral();
        u.setTituloEleitoral("123");
        
        // Re-configurar o mock com o novo título
        List<UsuarioPerfil> atribuicoes = atribuicoesPorUsuario.get(tituloOriginal);
        for (UsuarioPerfil up : atribuicoes) {
            up.setUsuarioTitulo("123");
        }
        atribuicoesPorUsuario.put("123", atribuicoes);
        
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
        List<UsuarioPerfil> atribuicoes = atribuicoesPorUsuario.get(u.getTituloEleitoral());
        Unidade sup = atribuicoes.getFirst().getUnidade();
 
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

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Combinacoes e Falhas")
    void canExecute_VerificarImpactos_Combinacoes() {
        // 1. CHEFE (Mesma Unidade) + REVISAO_CADASTRO_EM_ANDAMENTO -> True
        Usuario uChefe = criarUsuario(Perfil.CHEFE, 1L);
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 1L);
        assertTrue(policy.canExecute(uChefe, Acao.VERIFICAR_IMPACTOS, sp));

        // 2. CHEFE (Outra Unidade) + REVISAO_CADASTRO_EM_ANDAMENTO -> False
        Subprocesso spOutraUnidade = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 2L);
        assertFalse(policy.canExecute(uChefe, Acao.VERIFICAR_IMPACTOS, spOutraUnidade));

        // 3. CHEFE (Mesma Unidade) + Situação Inválida (ex: MAPEAMENTO_CADASTRO_EM_ANDAMENTO) -> False
        Subprocesso spSitInv = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);
        assertFalse(policy.canExecute(uChefe, Acao.VERIFICAR_IMPACTOS, spSitInv));

        // 4. GESTOR + REVISAO_CADASTRO_DISPONIBILIZADA -> True
        Usuario uGestor = criarUsuario(Perfil.GESTOR, 99L); // Unidade irrelevante
        Subprocesso spRevDisp = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, 1L);
        assertTrue(policy.canExecute(uGestor, Acao.VERIFICAR_IMPACTOS, spRevDisp));

        // 5. GESTOR + Situação Inválida -> False
        assertFalse(policy.canExecute(uGestor, Acao.VERIFICAR_IMPACTOS, spSitInv));

        // 6. ADMIN + REVISAO_CADASTRO_DISPONIBILIZADA -> True
        Usuario uAdmin = criarUsuario(Perfil.ADMIN, 99L);
        assertTrue(policy.canExecute(uAdmin, Acao.VERIFICAR_IMPACTOS, spRevDisp));

        // 7. ADMIN + REVISAO_CADASTRO_HOMOLOGADA -> True
        Subprocesso spRevHom = criarSubprocesso(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, 1L);
        assertTrue(policy.canExecute(uAdmin, Acao.VERIFICAR_IMPACTOS, spRevHom));

        // 8. ADMIN + REVISAO_MAPA_AJUSTADO -> True
        Subprocesso spRevMapAjus = criarSubprocesso(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, 1L);
        assertTrue(policy.canExecute(uAdmin, Acao.VERIFICAR_IMPACTOS, spRevMapAjus));

        // 9. ADMIN + Situação Inválida -> False
        assertFalse(policy.canExecute(uAdmin, Acao.VERIFICAR_IMPACTOS, spSitInv));

        // 10. SERVIDOR (Perfil Inválido) -> False
        Usuario uServidor = criarUsuario(Perfil.SERVIDOR, 1L);
        assertFalse(policy.canExecute(uServidor, Acao.VERIFICAR_IMPACTOS, spRevDisp));
    }

    @Test
    @DisplayName("verificaHierarquia - Validacao Completa")
    void verificaHierarquia_ValidacaoCompleta() {
        Subprocesso sp = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);

        // Ensure unit properties needed for tests
        sp.getUnidade().setSigla("SIGLA");
        sp.getUnidade().setTituloTitular("TITULAR");

        // Testar mensagens de erro (indiretamente via assert false e verificação manual se necessário)

        // MESMA_UNIDADE -> False
        Usuario uOutra = criarUsuario(Perfil.SERVIDOR, 2L);
        assertFalse(policy.canExecute(uOutra, Acao.EDITAR_MAPA, sp));

        // MESMA_OU_SUBORDINADA -> False (nem mesma, nem subordinada)
        // VISUALIZAR_SUBPROCESSO requer MESMA_OU_SUBORDINADA
        // Configurando para retornar false para subordinação
        when(hierarquiaService.isSubordinada(any(), any())).thenReturn(false);
        assertFalse(policy.canExecute(uOutra, Acao.VISUALIZAR_SUBPROCESSO, sp));

        // SUPERIOR_IMEDIATA -> False
        // ACEITAR_CADASTRO requer SUPERIOR_IMEDIATA
        Usuario uGestor = criarUsuario(Perfil.GESTOR, 2L);
        // Atualiza status para permitir chegar na verificação de hierarquia
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        // Garante que não é superior
        when(hierarquiaService.isSuperiorImediata(any(), any())).thenReturn(false);
        assertFalse(policy.canExecute(uGestor, Acao.ACEITAR_CADASTRO, sp));

        // TITULAR_UNIDADE -> False
        Usuario uChefe = criarUsuario(Perfil.CHEFE, 1L);
        uChefe.setTituloEleitoral("OUTRO");
        // DISPONIBILIZAR_CADASTRO requer TITULAR_UNIDADE
        assertFalse(policy.canExecute(uChefe, Acao.DISPONIBILIZAR_CADASTRO, sp));
    }

    private int contadorUsuarios = 0;
    
    private Usuario criarUsuario(Perfil perfil, Long codUnidade) {
        String titulo = "titulo-" + (++contadorUsuarios);
        Usuario u = new Usuario();
        u.setTituloEleitoral(titulo);
        Unidade un = new Unidade();
        un.setCodigo(codUnidade);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUsuarioTitulo(titulo);
        up.setPerfil(perfil);
        up.setUnidade(un);
        up.setUnidadeCodigo(codUnidade);
        
        List<UsuarioPerfil> atribuicoes = new ArrayList<>();
        atribuicoes.add(up);
        atribuicoesPorUsuario.put(titulo, atribuicoes);

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

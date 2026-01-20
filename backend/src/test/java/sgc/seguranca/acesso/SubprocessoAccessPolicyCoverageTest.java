package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static sgc.organizacao.model.Perfil.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;

@ExtendWith(MockitoExtension.class)
class SubprocessoAccessPolicyCoverageTest {

    @InjectMocks
    private SubprocessoAccessPolicy policy;

    @Mock
    private HierarquiaService hierarquiaService;

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Admin com Situação Inválida")
    void canExecute_VerificarImpactos_AdminSituacaoInvalida() {
        // Covers line 292
        Usuario u = criarUsuario(ADMIN, 1L);
        Subprocesso sp = criarSubprocesso(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);

        assertFalse(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Gestor com Situação Inválida")
    void canExecute_VerificarImpactos_GestorSituacaoInvalida() {
        // Covers line 294
        Usuario u = criarUsuario(GESTOR, 1L);
        Subprocesso sp = criarSubprocesso(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);

        assertFalse(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("canExecute - VERIFICAR_IMPACTOS - Usuario sem perfil adequado")
    void canExecute_VerificarImpactos_UsuarioSemPerfil() {
        // Covers line 286, 287
        Usuario u = criarUsuario(SERVIDOR, 1L);
        Subprocesso sp = criarSubprocesso(REVISAO_CADASTRO_DISPONIBILIZADA, 1L);

        assertFalse(policy.canExecute(u, Acao.VERIFICAR_IMPACTOS, sp));
    }

    @Test
    @DisplayName("obterMotivoNegacaoHierarquia - Default Case via Reflection")
    void obterMotivoNegacaoHierarquia_DefaultCase() throws Exception {
        // Covers line 386 (unreachable via public API)
        Usuario u = criarUsuario(SERVIDOR, 1L);
        Subprocesso sp = criarSubprocesso(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, 1L);

        // Use reflection to access the private method
        Class<?> clazz = SubprocessoAccessPolicy.class;
        Class<?>[] innerClasses = clazz.getDeclaredClasses();
        Class<?> enumClass = null;
        for (Class<?> c : innerClasses) {
            if (c.getSimpleName().equals("RequisitoHierarquia")) {
                enumClass = c;
                break;
            }
        }

        if (enumClass == null) {
            throw new RuntimeException("RequisitoHierarquia enum not found");
        }

        Object[] enumConstants = enumClass.getEnumConstants();
        Object nenhum = null;
        for (Object o : enumConstants) {
            if (o.toString().equals("NENHUM")) {
                nenhum = o;
                break;
            }
        }

        Method method = clazz.getDeclaredMethod("obterMotivoNegacaoHierarquia", Usuario.class, Subprocesso.class, enumClass);
        method.setAccessible(true);

        String result = (String) method.invoke(policy, u, sp, nenhum);

        assertNotNull(result);
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

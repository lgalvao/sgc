package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.mapa.model.Atividade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static sgc.seguranca.acesso.Acao.CRIAR_ATIVIDADE;

@Tag("unit")
@DisplayName("AtividadeAccessPolicy - Reflection Tests for Gaps")
class AtividadeAccessPolicyReflectionTest {

    @Test
    @DisplayName("Deve permitir ação se regra não exigir titular (simulado via reflection)")
    void devePermitirSeNaoRequerTitular() throws Exception {
        // Arrange
        AtividadeAccessPolicy policy = new AtividadeAccessPolicy();
        
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setAtribuicoes(new HashSet<>());
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        up.setUsuarioTitulo("123");
        // Precisamos associar uma unidade mockada/vazia para passar nas verifs anteriores se houver
        // Mas o check de perfil é global na lista de atribuições do usuário no método canExecute?
        // Vamos ver AbstractAccessPolicy... temPerfilPermitido itera sobre atribuicoes.
        usuario.getAtribuicoes().add(up);

        Atividade atividade = new Atividade();

        // Reflection para obter o mapa REGRAS original
        Field regrasField = AtividadeAccessPolicy.class.getDeclaredField("REGRAS");
        regrasField.setAccessible(true);
        Map<Acao, Object> regrasOriginais = (Map<Acao, Object>) regrasField.get(null);
        
        Object regraOriginal = regrasOriginais.get(CRIAR_ATIVIDADE);

        // Criar uma nova regra com requerTitular = false
        // A classe interna é privada: sgc.seguranca.acesso.AtividadeAccessPolicy$RegrasAcaoAtividade
        Class<?> innerClass = Class.forName("sgc.seguranca.acesso.AtividadeAccessPolicy$RegrasAcaoAtividade");
        Constructor<?> constructor = innerClass.getDeclaredConstructor(EnumSet.class, boolean.class);
        constructor.setAccessible(true);
        Object regraFake = constructor.newInstance(EnumSet.of(Perfil.ADMIN), false);

        try {
            // Alterar o mapa
            regrasOriginais.put(CRIAR_ATIVIDADE, regraFake);

            // Act
            boolean result = policy.canExecute(usuario, CRIAR_ATIVIDADE, atividade);

            // Assert
            assertThat(result).isTrue();
        } finally {
            // Restore
            if (regraOriginal != null) {
                regrasOriginais.put(CRIAR_ATIVIDADE, regraOriginal);
            }
        }
    }
}

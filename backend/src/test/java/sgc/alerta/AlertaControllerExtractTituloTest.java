package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import sgc.organizacao.model.Usuario;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários diretos do método privado extractTituloUsuario.
 * 
 * <p>Este arquivo testa os casos do switch expression que não podem ser facilmente testados
 * via MockMvc: Usuario entity e default case.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AlertaController - Extração Título Usuário (Método Privado)")
class AlertaControllerExtractTituloTest {

    @Mock
    private sgc.acompanhamento.AcompanhamentoFacade acompanhamentoFacade;

    @InjectMocks
    private AlertaController controller;

    /**
     * Helper para invocar o método privado extractTituloUsuario via reflection
     */
    private String invokeExtractTituloUsuario(Object principal) throws Exception {
        Method method = AlertaController.class.getDeclaredMethod("extractTituloUsuario", Object.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(controller, principal);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @Nested
    @DisplayName("Switch Expression - Todos os Casos")
    class SwitchExpressionCases {

        @Test
        @DisplayName("Caso String: deve retornar a string diretamente")
        void casoString_DeveRetornarStringDiretamente() throws Exception {
            String tituloEsperado = "12345678901";
            String resultado = invokeExtractTituloUsuario(tituloEsperado);
            assertThat(resultado).isEqualTo(tituloEsperado);
        }

        @Test
        @DisplayName("Caso Usuario: deve extrair getTituloEleitoral()")
        void casoUsuario_DeveExtrairTituloEleitoral() throws Exception {
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("98765432100");
            usuario.setMatricula("12345");
            usuario.setNome("João Silva");

            String resultado = invokeExtractTituloUsuario(usuario);
            assertThat(resultado).isEqualTo("98765432100");
        }

        @Test
        @DisplayName("Caso UserDetails: deve extrair getUsername()")
        void casoUserDetails_DeveExtrairUsername() throws Exception {
            UserDetails userDetails = User.builder()
                    .username("11122233344")
                    .password("password")
                    .authorities("ROLE_USER")
                    .build();

            String resultado = invokeExtractTituloUsuario(userDetails);
            assertThat(resultado).isEqualTo("11122233344");
        }

        @Test
        @DisplayName("Caso Default: deve usar toString()")
        void casoDefault_DeveUsarToString() throws Exception {
            Object objetoGenerico = new Object() {
                @Override
                public String toString() {
                    return "55566677788";
                }
            };

            String resultado = invokeExtractTituloUsuario(objetoGenerico);
            assertThat(resultado).isEqualTo("55566677788");
        }

        @Test
        @DisplayName("Caso Default com Integer: deve usar toString() do Integer")
        void casoDefaultInteger_DeveUsarToString() throws Exception {
            Integer numero = 123456789;

            String resultado = invokeExtractTituloUsuario(numero);
            assertThat(resultado).isEqualTo("123456789");
        }
    }
}

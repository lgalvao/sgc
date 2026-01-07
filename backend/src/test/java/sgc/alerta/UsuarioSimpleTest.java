package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioSimpleTest {
    @Test
    @DisplayName("Verifica se o getUsername do Usuario funciona")
    void testUsername() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("123456789012");
        assertEquals("123456789012", u.getTituloEleitoral());
        assertEquals("123456789012", u.getUsername());
        
        Usuario u2 = Usuario.builder().tituloEleitoral("987").build();
        assertEquals("987", u2.getTituloEleitoral());
        assertEquals("987", u2.getUsername());
    }
}

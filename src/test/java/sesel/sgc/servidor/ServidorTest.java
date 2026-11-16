package sesel.sgc.servidor;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import sesel.sgc.seguranca.Perfil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServidorTest {

    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(Servidor.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    @Test
    void deveRetornarAuthoritiesCorretas() {
        Servidor servidor = new Servidor();
        servidor.setPerfis(List.of(Perfil.CHEFE, Perfil.GESTOR));
        var authorities = servidor.getAuthorities();
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CHEFE")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_GESTOR")));
    }

    @Test
    void deveRetornarUsernameEPassword() {
        Servidor servidor = new Servidor();
        servidor.setEmail("usuario@exemplo.com");
        servidor.setSenha("senha123");
        assertEquals("usuario@exemplo.com", servidor.getUsername());
        assertEquals("senha123", servidor.getPassword());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        Servidor servidor = new Servidor();
        // Nome, email e senha não setados
        assertNull(servidor.getNome());
        assertNull(servidor.getEmail());
        assertNull(servidor.getSenha());
    }
}
package sgc.arquitetura;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;

class EqualsHashCodeTest {

    @Test
    void testUsuario() {
        Unidade u1 = Unidade.builder().nome("Unidade 1").sigla("U1").build();
        Unidade u2 = Unidade.builder().nome("Unidade 2").sigla("U2").build();

        AtribuicaoTemporaria at1 = new AtribuicaoTemporaria();
        at1.setJustificativa("Justificativa 1");
        AtribuicaoTemporaria at2 = new AtribuicaoTemporaria();
        at2.setJustificativa("Justificativa 2");

        UsuarioPerfil up1 = new UsuarioPerfil();
        up1.setUsuarioTitulo("111");
        UsuarioPerfil up2 = new UsuarioPerfil();
        up2.setUsuarioTitulo("222");

        EqualsVerifier.forClass(Usuario.class)
                .usingGetClass()
                .suppress(Warning.SURROGATE_KEY)
                .withPrefabValues(Unidade.class, u1, u2)
                .withPrefabValues(AtribuicaoTemporaria.class, at1, at2)
                .withPrefabValues(UsuarioPerfil.class, up1, up2)
                .verify();
    }

    @Test
    void testUsuarioPerfil() {
        Unidade u1 = Unidade.builder().nome("Unidade 1").sigla("U1").build();
        Unidade u2 = Unidade.builder().nome("Unidade 2").sigla("U2").build();
        Usuario user1 = Usuario.builder().tituloEleitoral("111").build();
        Usuario user2 = Usuario.builder().tituloEleitoral("222").build();

        EqualsVerifier.forClass(UsuarioPerfil.class)
                .usingGetClass()
                .suppress(Warning.SURROGATE_KEY)
                .withPrefabValues(Unidade.class, u1, u2)
                .withPrefabValues(Usuario.class, user1, user2)
                .verify();
    }

    @Test
    void testUsuarioPerfilId() {
        EqualsVerifier.forClass(UsuarioPerfilId.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                .verify();
    }
}

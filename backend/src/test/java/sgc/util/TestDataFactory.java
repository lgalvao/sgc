package sgc.util;

import sgc.comum.modelo.Administrador;
import sgc.sgrh.Usuario;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;

public class TestDataFactory {

    public static Usuario createAdminUser() {
        Usuario adminUser = new Usuario();
        adminUser.setTitulo("admin");
        adminUser.setNome("Admin User");
        adminUser.setEmail("admin@example.com");
        return adminUser;
    }

    public static Administrador createAdministrador(Usuario usuario) {
        return new Administrador(usuario.getTitulo(), usuario);
    }

    public static Unidade createUnidadeOperacional() {
        Unidade unidade = new Unidade("Unidade Operacional", "UOP");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        return unidade;
    }

    public static Unidade createUnidadeIntermediaria() {
        Unidade unidade = new Unidade("Unidade Intermediária", "UIN");
        unidade.setTipo(TipoUnidade.INTERMEDIARIA);
        return unidade;
    }

    public static Usuario createChefeUser(Unidade unidade) {
        Usuario chefe = new Usuario();
        chefe.setTitulo("chefe");
        chefe.setNome("Chefe Teste");
        chefe.setEmail("chefe@example.com");
        chefe.setUnidade(unidade);
        return chefe;
    }

    public static Usuario createGestorUser(Unidade unidade) {
        Usuario gestor = new Usuario();
        gestor.setTitulo("gestor_unidade");
        gestor.setNome("Gestor Teste");
        gestor.setEmail("gestor@example.com");
        gestor.setUnidade(unidade);
        return gestor;
    }
}
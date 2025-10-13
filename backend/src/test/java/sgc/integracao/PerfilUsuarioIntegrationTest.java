package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.comum.modelo.Administrador;
import sgc.comum.modelo.AdministradorRepo;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração para Atribuição de Perfis de Usuário com Entidade Administrador")
class PerfilUsuarioIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private AdministradorRepo administradorRepo;

    private Unidade unidadeOperacional;
    private Unidade unidadeIntermediaria;
    private Unidade unidadeInteroperacional;

    @BeforeEach
    void setUp() {
        unidadeOperacional = new Unidade("Unidade Operacional", "UOP");
        unidadeOperacional.setTipo(TipoUnidade.OPERACIONAL);
        unidadeRepo.save(unidadeOperacional);

        unidadeIntermediaria = new Unidade("Unidade Intermediária", "UIN");
        unidadeIntermediaria.setTipo(TipoUnidade.INTERMEDIARIA);
        unidadeRepo.save(unidadeIntermediaria);

        unidadeInteroperacional = new Unidade("Unidade Interoperacional", "UINT");
        unidadeInteroperacional.setTipo(TipoUnidade.INTEROPERACIONAL);
        unidadeRepo.save(unidadeInteroperacional);
    }

    @Test
    @DisplayName("Deve atribuir ROLE_ADMIN para usuário cadastrado como Administrador")
    void deveAtribuirRoleAdmin() {
        System.out.println("--- Iniciando deveAtribuirRoleAdmin ---");
        Usuario userAdmin = new Usuario();
        userAdmin.setTitulo("111111111111");
        System.out.println("Criado usuário: " + userAdmin.getTitulo());
        usuarioRepo.save(userAdmin);
        System.out.println("Salvo usuário: " + userAdmin.getTitulo());

        Administrador admin = new Administrador(userAdmin.getTitulo(), userAdmin);
        System.out.println("Criado admin para: " + admin.getUsuarioTitulo());
        administradorRepo.save(admin);
        System.out.println("Salvo admin para: " + admin.getUsuarioTitulo());

        Usuario foundUser = usuarioRepo.findById("111111111111").orElseThrow();
        System.out.println("Encontrado usuário: " + foundUser.getTitulo());
        Collection<?> authorities = foundUser.determineAuthorities(administradorRepo);
        System.out.println("Autoridades determinadas: " + authorities);
        assertThat(authorities).extracting("authority").containsExactly("ROLE_ADMIN");
        System.out.println("--- Finalizando deveAtribuirRoleAdmin ---");
    }

    @Test
    @DisplayName("Deve atribuir ROLE_CHEFE para titular de unidade Operacional")
    void deveAtribuirRoleChefeParaTitularDeUnidadeOperacional() {
        Usuario chefe = new Usuario();
        chefe.setTitulo("222222222222");
        chefe.setUnidade(unidadeOperacional);
        usuarioRepo.save(chefe);

        unidadeOperacional.setTitular(chefe);
        unidadeRepo.save(unidadeOperacional);

        Usuario foundChefe = usuarioRepo.findById("222222222222").orElseThrow();
        Collection<?> authorities = foundChefe.determineAuthorities(administradorRepo);
        assertThat(authorities).extracting("authority").containsExactly("ROLE_CHEFE");
    }

    @Test
    @DisplayName("Deve atribuir ROLE_CHEFE para titular de unidade Interoperacional")
    void deveAtribuirRoleChefeParaTitularDeUnidadeInteroperacional() {
        Usuario chefe = new Usuario();
        chefe.setTitulo("333333333333");
        chefe.setUnidade(unidadeInteroperacional);
        usuarioRepo.save(chefe);

        unidadeInteroperacional.setTitular(chefe);
        unidadeRepo.save(unidadeInteroperacional);

        Usuario foundChefe = usuarioRepo.findById("333333333333").orElseThrow();
        Collection<?> authorities = foundChefe.determineAuthorities(administradorRepo);
        assertThat(authorities).extracting("authority").containsExactly("ROLE_CHEFE");
    }

    @Test
    @DisplayName("Deve atribuir ROLE_GESTOR para titular de unidade Intermediária")
    void deveAtribuirRoleGestorParaTitularDeUnidadeIntermediaria() {
        Usuario gestor = new Usuario();
        gestor.setTitulo("444444444444");
        gestor.setUnidade(unidadeIntermediaria);
        usuarioRepo.save(gestor);

        unidadeIntermediaria.setTitular(gestor);
        unidadeRepo.save(unidadeIntermediaria);

        Usuario foundGestor = usuarioRepo.findById("444444444444").orElseThrow();
        Collection<?> authorities = foundGestor.determineAuthorities(administradorRepo);
        assertThat(authorities).extracting("authority").containsExactly("ROLE_GESTOR");
    }

    @Test
    @DisplayName("Deve atribuir ROLE_SERVIDOR para usuário que não é titular de sua unidade")
    void deveAtribuirRoleServidorParaNaoTitular() {
        Usuario titular = new Usuario();
        titular.setTitulo("555555555555");
        titular.setUnidade(unidadeOperacional);
        usuarioRepo.save(titular);

        unidadeOperacional.setTitular(titular);
        unidadeRepo.save(unidadeOperacional);

        Usuario servidor = new Usuario();
        servidor.setTitulo("666666666666");
        servidor.setUnidade(unidadeOperacional);
        usuarioRepo.save(servidor);

        Usuario foundServidor = usuarioRepo.findById("666666666666").orElseThrow();
        Collection<?> authorities = foundServidor.determineAuthorities(administradorRepo);
        assertThat(authorities).extracting("authority").containsExactly("ROLE_SERVIDOR");
    }

    @Test
    @DisplayName("Deve atribuir ROLE_SERVIDOR para usuário sem unidade definida")
    void deveAtribuirRoleServidorParaUsuarioSemUnidade() {
        Usuario semUnidade = new Usuario();
        semUnidade.setTitulo("777777777777");
        usuarioRepo.save(semUnidade);

        Usuario foundUser = usuarioRepo.findById("777777777777").orElseThrow();
        Collection<?> authorities = foundUser.determineAuthorities(administradorRepo);
        assertThat(authorities).extracting("authority").containsExactly("ROLE_SERVIDOR");
    }

    @Test
    @DisplayName("Não deve atribuir ROLE_ADMIN se não estiver na tabela de administradores")
    void naoDeveAtribuirRoleAdminSeNaoForAdministrador() {
        Usuario naoAdmin = new Usuario();
        naoAdmin.setTitulo("888888888888");
        naoAdmin.setUnidade(unidadeOperacional);
        usuarioRepo.save(naoAdmin);

        Usuario foundUser = usuarioRepo.findById("888888888888").orElseThrow();
        Collection<?> authorities = foundUser.determineAuthorities(administradorRepo);
        assertThat(authorities).extracting("authority").doesNotContain("ROLE_ADMIN");
    }
}
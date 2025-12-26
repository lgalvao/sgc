package sgc.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.autenticacao.GerenciadorJwt;
import sgc.usuario.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("secure-test")
@Transactional
@DisplayName("Testes de Segurança do Actuator")
class ActuatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GerenciadorJwt gerenciadorJwt;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @BeforeEach
    void setup() {
        Unidade unidade = unidadeRepo.findAll().stream().findFirst()
                .orElseGet(() -> unidadeRepo.save(new Unidade("Unidade Teste", "TESTE")));

        criarUsuario("123456789", Perfil.ADMIN, unidade);
        criarUsuario("987654321", Perfil.SERVIDOR, unidade);
    }

    private void criarUsuario(String titulo, Perfil perfil, Unidade unidade) {
        if (usuarioRepo.existsById(titulo)) return;

        Usuario usuario = new Usuario(titulo, "Usuario " + perfil, "email@test.com", "1234", unidade);
        usuarioRepo.save(usuario);

        UsuarioPerfil usuarioPerfil = new UsuarioPerfil();
        usuarioPerfil.setUsuarioTitulo(titulo);
        usuarioPerfil.setUsuario(usuario);
        usuarioPerfil.setUnidadeCodigo(unidade.getCodigo());
        usuarioPerfil.setUnidade(unidade);
        usuarioPerfil.setPerfil(perfil);
        usuarioPerfilRepo.save(usuarioPerfil);
    }

    @Test
    @DisplayName("Deve negar acesso anônimo ao Actuator")
    void deveNegarAcessoAnonimoAoActuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve permitir acesso de ADMIN ao Actuator")
    void devePermitirAcessoAdminAoActuator() throws Exception {
        String token = gerenciadorJwt.gerarToken("123456789", Perfil.ADMIN, 1L);

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve negar acesso de usuário comum ao Actuator")
    void deveNegarAcessoUsuarioComumAoActuator() throws Exception {
        String token = gerenciadorJwt.gerarToken("987654321", Perfil.SERVIDOR, 1L);

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}

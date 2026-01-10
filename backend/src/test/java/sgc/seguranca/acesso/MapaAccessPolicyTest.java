package sgc.seguranca.acesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.acesso.Acao.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MapaAccessPolicyTest")
class MapaAccessPolicyTest {

    @InjectMocks
    private MapaAccessPolicy policy;

    private Usuario usuarioAdmin;
    private Usuario usuarioGestor;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        mapa = new Mapa();
        mapa.setCodigo(1L);

        usuarioAdmin = criarUsuario("1", "Admin");
        adicionarAtribuicao(usuarioAdmin, ADMIN);

        usuarioGestor = criarUsuario("2", "Gestor");
        adicionarAtribuicao(usuarioGestor, GESTOR);
    }

    @Test
    @DisplayName("Deve permitir ADMIN criar mapa")
    void devePermitirAdminCriarMapa() {
        assertThat(policy.canExecute(usuarioAdmin, CRIAR_MAPA, mapa)).isTrue();
    }

    @Test
    @DisplayName("Deve negar GESTOR criar mapa")
    void deveNegarGestorCriarMapa() {
        assertThat(policy.canExecute(usuarioGestor, CRIAR_MAPA, mapa)).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não possui um dos perfis necessários");
    }

    @Test
    @DisplayName("Deve permitir GESTOR listar mapas")
    void devePermitirGestorListarMapas() {
        assertThat(policy.canExecute(usuarioGestor, LISTAR_MAPAS, mapa)).isTrue();
    }

    @Test
    @DisplayName("Deve negar se acao nao reconhecida")
    void deveNegarAcaoNaoReconhecida() {
        assertThat(policy.canExecute(usuarioAdmin, CRIAR_PROCESSO, mapa)).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("Ação não reconhecida");
    }

    private Usuario criarUsuario(String titulo, String nome) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome(nome);
        usuario.setAtribuicoes(new HashSet<>());
        return usuario;
    }

    private void adicionarAtribuicao(Usuario usuario, Perfil perfil) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setPerfil(perfil);
        usuario.getAtribuicoes().add(atribuicao);
    }
}

package sgc.seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static sgc.organizacao.model.Perfil.ADMIN;
import static sgc.organizacao.model.Perfil.GESTOR;
import static sgc.seguranca.Acao.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("MapaAccessPolicyTest")
class MapaAccessPolicyTest {

    @InjectMocks
    private MapaAccessPolicy policy;
    
    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;

    private Usuario usuarioAdmin;
    private Usuario usuarioGestor;
    private Mapa mapa;
    @BeforeEach
    void setUp() {
        mapa = new Mapa();
        mapa.setCodigo(1L);

        usuarioAdmin = criarUsuario("1", "Admin");
        usuarioAdmin.setPerfilAtivo(ADMIN);
        adicionarAtribuicao(usuarioAdmin, ADMIN);

        usuarioGestor = criarUsuario("2", "Gestor");
        usuarioGestor.setPerfilAtivo(GESTOR);
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
        return usuario;
    }

    private List<UsuarioPerfil> adicionarAtribuicao(Usuario usuario, Perfil perfil) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setPerfil(perfil);
        
        List<UsuarioPerfil> atribuicoes = new ArrayList<>();
        atribuicoes.add(atribuicao);
        return atribuicoes;
    }
}

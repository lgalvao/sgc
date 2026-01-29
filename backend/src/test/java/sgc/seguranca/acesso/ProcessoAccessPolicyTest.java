package sgc.seguranca.acesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.processo.model.Processo;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static sgc.organizacao.model.Perfil.ADMIN;
import static sgc.organizacao.model.Perfil.GESTOR;
import static sgc.seguranca.acesso.Acao.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoAccessPolicyTest")
class ProcessoAccessPolicyTest {

    @InjectMocks
    private ProcessoAccessPolicy policy;

    private Usuario usuarioAdmin;
    private Usuario usuarioGestor;
    private Processo processo;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);

        usuarioAdmin = criarUsuario("1", "Admin");
        adicionarAtribuicao(usuarioAdmin, ADMIN);

        usuarioGestor = criarUsuario("2", "Gestor");
        adicionarAtribuicao(usuarioGestor, GESTOR);
    }

    @Test
    @DisplayName("Deve permitir ADMIN criar processo")
    void devePermitirAdminCriarProcesso() {
        assertThat(policy.canExecute(usuarioAdmin, CRIAR_PROCESSO, processo)).isTrue();
    }

    @Test
    @DisplayName("Deve negar GESTOR criar processo")
    void deveNegarGestorCriarProcesso() {
        assertThat(policy.canExecute(usuarioGestor, CRIAR_PROCESSO, processo)).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não possui um dos perfis necessários");
    }

    @Test
    @DisplayName("Deve permitir GESTOR visualizar processo")
    void devePermitirGestorVisualizarProcesso() {
        assertThat(policy.canExecute(usuarioGestor, VISUALIZAR_PROCESSO, processo)).isTrue();
    }

    @Test
    @DisplayName("Deve negar se acao nao reconhecida")
    void deveNegarAcaoNaoReconhecida() {
        assertThat(policy.canExecute(usuarioAdmin, CRIAR_ATIVIDADE, processo)).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("Ação não reconhecida");
    }

    private Usuario criarUsuario(String titulo, String nome) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome(nome);
        usuario.setAtribuicoesPermanentes(new HashSet<>());
        return usuario;
    }

    private void adicionarAtribuicao(Usuario usuario, Perfil perfil) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setPerfil(perfil);
        
        Set<UsuarioPerfil> atribuicoes = new HashSet<>(usuario.getTodasAtribuicoes());
        atribuicoes.add(atribuicao);
        usuario.setAtribuicoesPermanentes(atribuicoes);
    }
}

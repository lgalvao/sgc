package sgc.seguranca.acesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.model.Subprocesso;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static sgc.organizacao.model.Perfil.CHEFE;
import static sgc.organizacao.model.Perfil.SERVIDOR;
import static sgc.seguranca.acesso.Acao.CRIAR_ATIVIDADE;
import static sgc.seguranca.acesso.Acao.LISTAR_SUBPROCESSOS;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AtividadeAccessPolicyTest")
class AtividadeAccessPolicyTest {

    @InjectMocks
    private AtividadeAccessPolicy policy;

    private Usuario usuarioChefe;
    private Usuario usuarioServidor;
    private Atividade atividade;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("UNIT");
        unidade.setTituloTitular("123");

        usuarioChefe = criarUsuario("123", "Chefe");
        adicionarAtribuicao(usuarioChefe, CHEFE, unidade);

        usuarioServidor = criarUsuario("456", "Servidor");
        adicionarAtribuicao(usuarioServidor, SERVIDOR, unidade);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(unidade);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(sp);

        atividade = new Atividade();
        atividade.setMapa(mapa);
    }

    @Test
    @DisplayName("Deve permitir CHEFE criar atividade se for titular")
    void devePermitirChefeCriarAtividade() {
        boolean resultado = policy.canExecute(usuarioChefe, CRIAR_ATIVIDADE, atividade);
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve negar SERVIDOR criar atividade (perfil não permitido)")
    void deveNegarServidorCriarAtividade() {
        boolean resultado = policy.canExecute(usuarioServidor, CRIAR_ATIVIDADE, atividade);
        assertThat(resultado).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não possui um dos perfis necessários");
    }

    @Test
    @DisplayName("Deve negar CHEFE se não for titular da unidade")
    void deveNegarChefeSeNaoForTitular() {
        unidade.setTituloTitular("999"); // Muda o titular
        boolean resultado = policy.canExecute(usuarioChefe, CRIAR_ATIVIDADE, atividade);
        assertThat(resultado).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não é o titular da unidade");
    }

    @Test
    @DisplayName("Deve negar se atividade não possui mapa")
    void deveNegarSeMapaNull() {
        atividade.setMapa(null);
        boolean resultado = policy.canExecute(usuarioChefe, CRIAR_ATIVIDADE, atividade);
        assertThat(resultado).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não possui mapa associado");
    }

    @Test
    @DisplayName("Deve negar se mapa não possui subprocesso")
    void deveNegarSeSubprocessoNull() {
        atividade.getMapa().setSubprocesso(null);
        boolean resultado = policy.canExecute(usuarioChefe, CRIAR_ATIVIDADE, atividade);
        assertThat(resultado).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não possui subprocesso associado");
    }

    @Test
    @DisplayName("Deve negar ação não reconhecida")
    void deveNegarAcaoInvalida() {
        boolean resultado = policy.canExecute(usuarioChefe, LISTAR_SUBPROCESSOS, atividade);
        assertThat(resultado).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("Ação não reconhecida");
    }

    private Usuario criarUsuario(String titulo, String nome) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome(nome);
        usuario.setAtribuicoes(new HashSet<>());
        return usuario;
    }

    private void adicionarAtribuicao(Usuario usuario, Perfil perfil, Unidade unidade) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setPerfil(perfil);
        atribuicao.setUnidade(unidade);
        usuario.getAtribuicoes().add(atribuicao);
    }
}

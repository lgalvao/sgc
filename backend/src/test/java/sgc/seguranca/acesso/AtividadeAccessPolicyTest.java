package sgc.seguranca.acesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.*;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.model.Subprocesso;
import sgc.testutils.UnidadeTestBuilder;

import java.util.ArrayList;
import java.util.List;

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
    
    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Mock
    private HierarquiaService hierarquiaService;

    private Usuario usuarioChefe;
    private Usuario usuarioServidor;
    private Atividade atividade;
    private Unidade unidade;
    @BeforeEach
    void setUp() {
        unidade = UnidadeTestBuilder.umaDe()
                .comCodigo("1")
                .comSigla("UNIT")
                .comTituloTitular("123")
                .build();

        usuarioChefe = criarUsuario("123", "Chefe");
        usuarioChefe.setPerfilAtivo(CHEFE);
        adicionarAtribuicao(usuarioChefe, CHEFE, unidade);

        usuarioServidor = criarUsuario("456", "Servidor");
        usuarioServidor.setPerfilAtivo(SERVIDOR);
        adicionarAtribuicao(usuarioServidor, SERVIDOR, unidade);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(unidade);
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(sp);

        atividade = new Atividade();
        atividade.setMapa(mapa);
    }

    @Test
    @DisplayName("Deve permitir CHEFE criar atividade se for titular")
    void devePermitirChefeCriarAtividade() {
        org.mockito.Mockito.when(hierarquiaService.isResponsavel(unidade, usuarioChefe)).thenReturn(true);
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
        org.mockito.Mockito.when(hierarquiaService.isResponsavel(unidade, usuarioChefe)).thenReturn(false);
        boolean resultado = policy.canExecute(usuarioChefe, CRIAR_ATIVIDADE, atividade);
        assertThat(resultado).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não é o titular da unidade");
    }

    @Test
    @DisplayName("Deve negar CHEFE alterar atividade se subprocesso não estiver na situação permitida")
    void deveNegarChefeForaDaSituacaoPermitida() {
        atividade.getMapa().getSubprocesso().setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        boolean resultado = policy.canExecute(usuarioChefe, CRIAR_ATIVIDADE, atividade);
        assertThat(resultado).isFalse();
        assertThat(policy.getMotivoNegacao()).contains("não pode ser executada quando o subprocesso está na situação");
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
        return usuario;
    }

    private List<UsuarioPerfil> adicionarAtribuicao(Usuario usuario, Perfil perfil, Unidade unidade) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setPerfil(perfil);
        atribuicao.setUnidade(unidade);
        atribuicao.setUnidadeCodigo(unidade.getCodigo());
        
        List<UsuarioPerfil> atribuicoes = new ArrayList<>();
        atribuicoes.add(atribuicao);
        return atribuicoes;
    }

}

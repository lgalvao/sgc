package sgc.seguranca.acesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SubprocessoAccessPolicy")
class SubprocessoAccessPolicyTest {

    @Mock
    private HierarchyService hierarchyService;

    @InjectMocks
    private SubprocessoAccessPolicy policy;

    private Usuario usuarioAdmin;
    private Usuario usuarioGestor;
    private Usuario usuarioChefe;
    private Usuario usuarioServidor;
    private Unidade unidadePrincipal;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidadeSuperior = criarUnidade(1L, "SEDOC", null, null);
        unidadePrincipal = criarUnidade(2L, "UNIDADE", unidadeSuperior, "123456789012");
        
        usuarioAdmin = criarUsuario("111111111111", "Admin User");
        usuarioGestor = criarUsuario("222222222222", "Gestor User");
        usuarioChefe = criarUsuario("123456789012", "Chefe User");
        usuarioServidor = criarUsuario("444444444444", "Servidor User");
        
        adicionarAtribuicao(usuarioAdmin, ADMIN, unidadePrincipal);
        adicionarAtribuicao(usuarioGestor, GESTOR, unidadeSuperior);
        adicionarAtribuicao(usuarioChefe, CHEFE, unidadePrincipal);
        adicionarAtribuicao(usuarioServidor, SERVIDOR, unidadePrincipal);
        
        subprocesso = criarSubprocesso(10L, unidadePrincipal, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Nested
    @DisplayName("Testes de CRUD básico")
    class CrudBasicoTest {

        @Test
        @DisplayName("Deve permitir ADMIN listar subprocessos")
        void devePermitirAdminListarSubprocessos() {
            boolean resultado = policy.canExecute(usuarioAdmin, LISTAR_SUBPROCESSOS, subprocesso);
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar GESTOR listar subprocessos")
        void deveNegarGestorListarSubprocessos() {
            boolean resultado = policy.canExecute(usuarioGestor, LISTAR_SUBPROCESSOS, subprocesso);
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("não possui um dos perfis necessários");
        }

        @Test
        @DisplayName("Deve permitir ADMIN criar/editar/excluir subprocesso")
        void devePermitirAdminCrudSubprocesso() {
            assertThat(policy.canExecute(usuarioAdmin, CRIAR_SUBPROCESSO, subprocesso)).isTrue();
            assertThat(policy.canExecute(usuarioAdmin, EDITAR_SUBPROCESSO, subprocesso)).isTrue();
            assertThat(policy.canExecute(usuarioAdmin, EXCLUIR_SUBPROCESSO, subprocesso)).isTrue();
        }

        @Test
        @DisplayName("Deve negar não-ADMIN criar/editar/excluir subprocesso")
        void deveNegarNaoAdminCrudSubprocesso() {
            assertThat(policy.canExecute(usuarioChefe, CRIAR_SUBPROCESSO, subprocesso)).isFalse();
            assertThat(policy.canExecute(usuarioGestor, EDITAR_SUBPROCESSO, subprocesso)).isFalse();
        }

        @Test
        @DisplayName("Deve permitir visualizar subprocesso se usuário da mesma unidade ou superior")
        void devePermitirVisualizarSeHierarquiaCorreta() {
            when(hierarchyService.isSubordinada(any(), any())).thenReturn(true);
            
            assertThat(policy.canExecute(usuarioChefe, VISUALIZAR_SUBPROCESSO, subprocesso)).isTrue();
            assertThat(policy.canExecute(usuarioGestor, VISUALIZAR_SUBPROCESSO, subprocesso)).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Cadastro")
    class CadastroTest {

        @Test
        @DisplayName("Deve permitir CHEFE disponibilizar cadastro se for titular")
        void devePermitirChefeDisponibilizarSeForTitular() {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            
            boolean resultado = policy.canExecute(usuarioChefe, DISPONIBILIZAR_CADASTRO, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar CHEFE disponibilizar cadastro se não for titular")
        void deveNegarChefeDisponibilizarSeNaoForTitular() {
            Unidade outraUnidade = criarUnidade(3L, "OUTRA", unidadeSuperior, "999999999999");
            subprocesso.setUnidade(outraUnidade);
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            
            boolean resultado = policy.canExecute(usuarioChefe, DISPONIBILIZAR_CADASTRO, subprocesso);
            
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("não é o titular");
        }

        @Test
        @DisplayName("Deve negar disponibilizar cadastro em situação inválida")
        void deveNegarDisponibilizarCadastroEmSituacaoInvalida() {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            boolean resultado = policy.canExecute(usuarioChefe, DISPONIBILIZAR_CADASTRO, subprocesso);
            
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("não pode ser executada com o subprocesso na situação");
        }

        @Test
        @DisplayName("Deve permitir GESTOR devolver cadastro se for superior imediata")
        void devePermitirGestorDevolverCadastroSeForSuperiorImediata() {
            when(hierarchyService.isSuperiorImediata(unidadePrincipal, unidadeSuperior)).thenReturn(true);
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            
            boolean resultado = policy.canExecute(usuarioGestor, DEVOLVER_CADASTRO, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve permitir ADMIN homologar cadastro")
        void devePermitirAdminHomologarCadastro() {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            
            boolean resultado = policy.canExecute(usuarioAdmin, HOMOLOGAR_CADASTRO, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar CHEFE homologar cadastro")
        void deveNegarChefeHomologarCadastro() {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            
            boolean resultado = policy.canExecute(usuarioChefe, HOMOLOGAR_CADASTRO, subprocesso);
            
            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Revisão de Cadastro")
    class RevisaoCadastroTest {

        @Test
        @DisplayName("Deve permitir CHEFE disponibilizar revisão se for titular")
        void devePermitirChefeDisponibilizarRevisaoSeForTitular() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            
            boolean resultado = policy.canExecute(usuarioChefe, DISPONIBILIZAR_REVISAO_CADASTRO, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve permitir GESTOR aceitar revisão de cadastro")
        void devePermitirGestorAceitarRevisaoCadastro() {
            when(hierarchyService.isSuperiorImediata(unidadePrincipal, unidadeSuperior)).thenReturn(true);
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            
            boolean resultado = policy.canExecute(usuarioGestor, ACEITAR_REVISAO_CADASTRO, subprocesso);
            
            assertThat(resultado).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Mapa")
    class MapaTest {

        @Test
        @DisplayName("Deve permitir ADMIN disponibilizar mapa")
        void devePermitirAdminDisponibilizarMapa() {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            boolean resultado = policy.canExecute(usuarioAdmin, DISPONIBILIZAR_MAPA, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve permitir CHEFE apresentar sugestões ao mapa")
        void devePermitirChefeApresentarSugestoes() {
            subprocesso.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
            
            boolean resultado = policy.canExecute(usuarioChefe, APRESENTAR_SUGESTOES, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar SERVIDOR apresentar sugestões")
        void deveNegarServidorApresentarSugestoes() {
            subprocesso.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
            
            boolean resultado = policy.canExecute(usuarioServidor, APRESENTAR_SUGESTOES, subprocesso);
            
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve permitir CHEFE validar mapa")
        void devePermitirChefeValidarMapa() {
            subprocesso.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
            
            boolean resultado = policy.canExecute(usuarioChefe, VALIDAR_MAPA, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve permitir ADMIN ajustar mapa")
        void devePermitirAdminAjustarMapa() {
            subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            
            boolean resultado = policy.canExecute(usuarioAdmin, AJUSTAR_MAPA, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar CHEFE ajustar mapa")
        void deveNegarChefeAjustarMapa() {
            subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            
            boolean resultado = policy.canExecute(usuarioChefe, AJUSTAR_MAPA, subprocesso);
            
            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Diagnóstico")
    class DiagnosticoTest {

        @Test
        @DisplayName("Deve permitir CHEFE realizar autoavaliação")
        void devePermitirChefeRealizarAutoavaliacao() {
            subprocesso.setSituacao(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
            
            boolean resultado = policy.canExecute(usuarioChefe, REALIZAR_AUTOAVALIACAO, subprocesso);
            
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar SERVIDOR realizar autoavaliação")
        void deveNegarServidorRealizarAutoavaliacao() {
            subprocesso.setSituacao(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
            
            boolean resultado = policy.canExecute(usuarioServidor, REALIZAR_AUTOAVALIACAO, subprocesso);
            
            assertThat(resultado).isFalse();
        }
    }

    // Métodos auxiliares

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
        atribuicao.setUnidadeCodigo(unidade.getCodigo());
        usuario.getAtribuicoes().add(atribuicao);
    }

    private Unidade criarUnidade(Long codigo, String sigla, Unidade superior, String tituloTitular) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setUnidadeSuperior(superior);
        unidade.setTituloTitular(tituloTitular);
        return unidade;
    }

    private Subprocesso criarSubprocesso(Long codigo, Unidade unidade, SituacaoSubprocesso situacao) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(unidade);
        sp.setSituacao(situacao);
        return sp;
    }
}

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
    private sgc.organizacao.ServicoHierarquia servicoHierarquia;

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
            when(servicoHierarquia.isSubordinada(any(), any())).thenReturn(true);

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
            when(servicoHierarquia.isSuperiorImediata(unidadePrincipal, unidadeSuperior)).thenReturn(true);
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
            when(servicoHierarquia.isSuperiorImediata(unidadePrincipal, unidadeSuperior)).thenReturn(true);
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
    @DisplayName("Testes de Verificar Impactos")
    class VerificarImpactosTest {

        @Test
        @DisplayName("Deve permitir CHEFE verificar impactos na mesma unidade e situação permitida")
        void devePermitirChefeVerificarImpactosMesmaUnidadeSituaçãoPermitida() {
            subprocesso.setSituacao(NAO_INICIADO);
            boolean resultado = policy.canExecute(usuarioChefe, VERIFICAR_IMPACTOS, subprocesso);
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar CHEFE verificar impactos em unidade diferente")
        void deveNegarChefeVerificarImpactosUnidadeDiferente() {
            subprocesso.setSituacao(NAO_INICIADO);
            Unidade outra = criarUnidade(3L, "OUTRA", unidadeSuperior, "X");
            subprocesso.setUnidade(outra);
            
            boolean resultado = policy.canExecute(usuarioChefe, VERIFICAR_IMPACTOS, subprocesso);
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve permitir GESTOR verificar impactos em REVISAO_CADASTRO_DISPONIBILIZADA")
        void devePermitirGestorVerificarImpactos() {
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            boolean resultado = policy.canExecute(usuarioGestor, VERIFICAR_IMPACTOS, subprocesso);
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve permitir ADMIN verificar impactos nas situações de revisão")
        void devePermitirAdminVerificarImpactos() {
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            assertThat(policy.canExecute(usuarioAdmin, VERIFICAR_IMPACTOS, subprocesso)).isTrue();
            
            subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            assertThat(policy.canExecute(usuarioAdmin, VERIFICAR_IMPACTOS, subprocesso)).isTrue();
            
            subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
            assertThat(policy.canExecute(usuarioAdmin, VERIFICAR_IMPACTOS, subprocesso)).isTrue();
        }

        @Test
        @DisplayName("Deve negar SERVIDOR verificar impactos")
        void deveNegarServidorVerificarImpactos() {
            boolean resultado = policy.canExecute(usuarioServidor, VERIFICAR_IMPACTOS, subprocesso);
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("não possui um dos perfis necessários");
        }

        @Test
        @DisplayName("Deve negar ADMIN verificar impactos em situação fora do padrão")
        void deveNegarAdminVerificarImpactosSituacaoInvalida() {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            boolean resultado = policy.canExecute(usuarioAdmin, VERIFICAR_IMPACTOS, subprocesso);
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("não pode ser executada");
        }
    }

    @Nested
    @DisplayName("Testes de Hierarquia e Casos de Borda")
    class HierarquiaCasosBordaTest {

        @Test
        @DisplayName("Deve permitir acesso quando requisito é NENHUM mesmo sem unidade")
        void devePermitirAcessoRequisitoNenhumSemUnidade() {
            subprocesso.setUnidade(null);
            // LISTAR_SUBPROCESSOS tem RequisitoHierarquia.NENHUM
            boolean resultado = policy.canExecute(usuarioAdmin, LISTAR_SUBPROCESSOS, subprocesso);
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar acesso quando requisito requer unidade e unidade é null")
        void deveNegarAcessoQuandoRequisitoUnidadeMasUnidadeNull() {
            subprocesso.setUnidade(null);
            // EDITAR_CADASTRO tem RequisitoHierarquia.MESMA_UNIDADE
            boolean resultado = policy.canExecute(usuarioChefe, EDITAR_CADASTRO, subprocesso);
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve permitir GESTOR em unidade superior (MESMA_OU_SUBORDINADA)")
        void devePermitirGestorUnidadeSuperior() {
            when(servicoHierarquia.isSubordinada(unidadePrincipal, unidadeSuperior)).thenReturn(true);
            boolean resultado = policy.canExecute(usuarioGestor, VISUALIZAR_SUBPROCESSO, subprocesso);
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve negar se não for unidade superior imediata")
        void deveNegarSeNaoForSuperiorImediata() {
            when(servicoHierarquia.isSuperiorImediata(unidadePrincipal, unidadeSuperior)).thenReturn(false);
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            
            boolean resultado = policy.canExecute(usuarioGestor, ACEITAR_CADASTRO, subprocesso);
            
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("não pertence à unidade superior imediata");
        }
        
        @Test
        @DisplayName("Deve negar se ação não for reconhecida")
        void deveNegarAcaoNaoReconhecida() {
            // Ação que não está no mapa de regras do Subprocesso (ex: CRIAR_ATIVIDADE que é da AtividadeAccessPolicy)
            boolean resultado = policy.canExecute(usuarioAdmin, CRIAR_ATIVIDADE, subprocesso);
            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("Ação não reconhecida");
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

    @Nested
    @DisplayName("Testes de Cobertura Extra")
    class CoberturaExtraTest {

        @Test
        @DisplayName("Deve formatar resumo de situações quando houver mais de 5")
        void deveFormatarResumoSituacoes() {
            // EDITAR_MAPA possui 10 situações permitidas, o que deve ativar o resumo (> 5)
            subprocesso.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO); // Situação inválida para EDITAR_MAPA

            boolean resultado = policy.canExecute(usuarioChefe, EDITAR_MAPA, subprocesso);

            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("10 situações");
        }

        @Test
        @DisplayName("Deve validar mensagens para Verificar Impactos com múltiplos perfis ou GESTOR")
        void deveValidarMensagensVerificarImpactos() {
            // Caso Múltiplos Perfis (ADMIN + GESTOR) em situação inválida
            adicionarAtribuicao(usuarioAdmin, GESTOR, unidadePrincipal);
            subprocesso.setSituacao(NAO_INICIADO); // Inválido para ADMIN/GESTOR na verificação de impactos

            policy.canExecute(usuarioAdmin, VERIFICAR_IMPACTOS, subprocesso);
            assertThat(policy.getMotivoNegacao()).contains("REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA");

            // Caso GESTOR em situação inválida
            subprocesso.setSituacao(NAO_INICIADO);
            policy.canExecute(usuarioGestor, VERIFICAR_IMPACTOS, subprocesso);
            assertThat(policy.getMotivoNegacao()).contains("REVISAO_CADASTRO_DISPONIBILIZADA");
        }

        @Test
        @DisplayName("Deve falhar CHEFE verificar impactos se hierarquia incorreta")
        void deveFalharChefeVerificarImpactosHierarquiaIncorreta() {
            subprocesso.setSituacao(NAO_INICIADO);
            // Chefe da unidadePrincipal, mas subprocesso de outra unidade
            Unidade outra = criarUnidade(99L, "OUTRA", null, null);
            subprocesso.setUnidade(outra);

            boolean resultado = policy.canExecute(usuarioChefe, VERIFICAR_IMPACTOS, subprocesso);

            assertThat(resultado).isFalse();
            assertThat(policy.getMotivoNegacao()).contains("não pertence à unidade");
        }

        @Test
        @DisplayName("Deve validar mensagens de erro de hierarquia detalhadas")
        void deveValidarMensagensHierarquia() {
            // Caso TITULAR_UNIDADE indefinido
            Unidade unidadeSemTitular = criarUnidade(3L, "SEMTIT", null, "não definido");
            unidadeSemTitular.setTituloTitular("não definido"); // Explicitando para garantir o teste
            subprocesso.setUnidade(unidadeSemTitular);
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            policy.canExecute(usuarioChefe, DISPONIBILIZAR_CADASTRO, subprocesso);
            assertThat(policy.getMotivoNegacao()).contains("Titular: não definido");

            // Caso TITULAR_UNIDADE definido mas diferente
            unidadePrincipal.setTituloTitular("999999999999");
            subprocesso.setUnidade(unidadePrincipal);

            policy.canExecute(usuarioChefe, DISPONIBILIZAR_CADASTRO, subprocesso);
            assertThat(policy.getMotivoNegacao()).contains("Titular: 999999999999");

            // Caso MESMA_OU_SUBORDINADA falhando
            when(servicoHierarquia.isSubordinada(any(), any())).thenReturn(false);
            Unidade unidadeAlheia = criarUnidade(4L, "ALHEIA", null, null);
            subprocesso.setUnidade(unidadeAlheia);

            policy.canExecute(usuarioChefe, VISUALIZAR_SUBPROCESSO, subprocesso);
            assertThat(policy.getMotivoNegacao()).contains("nem a uma unidade superior");
        }
    }
}

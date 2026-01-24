package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do AccessControlService")
class AccessControlServiceTest {

    @Mock
    private AccessAuditService auditService;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private SubprocessoAccessPolicy subprocessoAccessPolicy;

    @Mock
    private ProcessoAccessPolicy processoAccessPolicy;

    @Mock
    private AtividadeAccessPolicy atividadeAccessPolicy;

    @Mock
    private MapaAccessPolicy mapaAccessPolicy;

    @InjectMocks
    private AccessControlService accessControlService;

    @Test
    @DisplayName("Deve permitir todas as ações na implementação inicial (skeleton)")
    void devePermitirTodasAcoesNaImplementacaoInicial() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.VISUALIZAR_PROCESSO;
        Processo processo = criarProcesso(1L);

        when(processoAccessPolicy.canExecute(usuario, acao, processo)).thenReturn(true);

        boolean resultado = accessControlService.podeExecutar(usuario, acao, processo);

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve auditar acesso concedido ao verificar permissão")
    void deveAuditarAcessoConcedidoAoVerificarPermissao() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.VISUALIZAR_PROCESSO;
        Processo processo = criarProcesso(1L);

        when(processoAccessPolicy.canExecute(usuario, acao, processo)).thenReturn(true);

        assertDoesNotThrow(() -> accessControlService.verificarPermissao(usuario, acao, processo));

        verify(auditService).logAccessGranted(usuario, acao, processo);
    }

    @Test
    @DisplayName("Deve retornar true para podeExecutar na implementação inicial")
    void deveRetornarTrueParaPodeExecutarNaImplementacaoInicial() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.EDITAR_PROCESSO;
        Processo processo = criarProcesso(1L);

        when(processoAccessPolicy.canExecute(usuario, acao, processo)).thenReturn(true);

        boolean resultado = accessControlService.podeExecutar(usuario, acao, processo);

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve funcionar com diferentes tipos de ações")
    void deveFuncionarComDiferentesTiposDeAcoes() {
        Usuario usuario = criarUsuario("123456789012");
        Processo processo = criarProcesso(1L);

        when(processoAccessPolicy.canExecute(eq(usuario), any(Acao.class), eq(processo))).thenReturn(true);

        assertThat(accessControlService.podeExecutar(usuario, Acao.CRIAR_PROCESSO, processo)).isTrue();
        assertThat(accessControlService.podeExecutar(usuario, Acao.EDITAR_PROCESSO, processo)).isTrue();
        assertThat(accessControlService.podeExecutar(usuario, Acao.EXCLUIR_PROCESSO, processo)).isTrue();
    }

    @Test
    @DisplayName("Deve delegar para SubprocessoAccessPolicy quando recurso é Subprocesso")
    void deveDelegarParaSubprocessoAccessPolicy() {
        Usuario usuario = criarUsuario("123456789012");
        Subprocesso subprocesso = new Subprocesso();
        Acao acao = Acao.EDITAR_SUBPROCESSO;

        when(subprocessoAccessPolicy.canExecute(usuario, acao, subprocesso)).thenReturn(true);

        assertThat(accessControlService.podeExecutar(usuario, acao, subprocesso)).isTrue();
        verify(subprocessoAccessPolicy).canExecute(usuario, acao, subprocesso);
    }

    @Test
    @DisplayName("Deve delegar para AtividadeAccessPolicy quando recurso é Atividade")
    void deveDelegarParaAtividadeAccessPolicy() {
        Usuario usuario = criarUsuario("123456789012");
        Atividade atividade = new Atividade();
        Acao acao = Acao.EDITAR_ATIVIDADE;

        when(atividadeAccessPolicy.canExecute(usuario, acao, atividade)).thenReturn(true);

        assertThat(accessControlService.podeExecutar(usuario, acao, atividade)).isTrue();
        verify(atividadeAccessPolicy).canExecute(usuario, acao, atividade);
    }

    @Test
    @DisplayName("Deve delegar para MapaAccessPolicy quando recurso é Mapa")
    void deveDelegarParaMapaAccessPolicy() {
        Usuario usuario = criarUsuario("123456789012");
        Mapa mapa = new Mapa();
        Acao acao = Acao.EDITAR_MAPA;

        when(mapaAccessPolicy.canExecute(usuario, acao, mapa)).thenReturn(true);

        assertThat(accessControlService.podeExecutar(usuario, acao, mapa)).isTrue();
        verify(mapaAccessPolicy).canExecute(usuario, acao, mapa);
    }

    @Test
    @DisplayName("Deve negar acesso para recurso desconhecido")
    void deveNegarAcessoParaRecursoDesconhecido() {
        Usuario usuario = criarUsuario("123456789012");
        Object recursoDesconhecido = new Object();
        Acao acao = Acao.VISUALIZAR_PROCESSO;

        boolean resultado = accessControlService.podeExecutar(usuario, acao, recursoDesconhecido);

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Deve retornar motivo de negação para diferentes tipos de recurso")
    void deveRetornarMotivoNegacaoParaDiferentesRecursos() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.EDITAR_PROCESSO;

        when(processoAccessPolicy.getMotivoNegacao()).thenReturn("Motivo Processo");
        when(atividadeAccessPolicy.getMotivoNegacao()).thenReturn("Motivo Atividade");
        when(mapaAccessPolicy.getMotivoNegacao()).thenReturn("Motivo Mapa");

        assertThat(accessControlService.podeExecutar(null, acao, new Processo())).isFalse(); // Linha 48-49

        // Testando obterMotivoNegacao (chamado via verificarPermissao quando falha)
        when(processoAccessPolicy.canExecute(any(), any(), any())).thenReturn(false);
        try {
            accessControlService.verificarPermissao(usuario, acao, new Processo());
        } catch (sgc.comum.erros.ErroAccessoNegado e) {
            assertThat(e.getMessage()).isEqualTo("Motivo Processo");
        }

        when(atividadeAccessPolicy.canExecute(any(), any(), any())).thenReturn(false);
        try {
            accessControlService.verificarPermissao(usuario, acao, new Atividade());
        } catch (sgc.comum.erros.ErroAccessoNegado e) {
            assertThat(e.getMessage()).isEqualTo("Motivo Atividade");
        }

        when(mapaAccessPolicy.canExecute(any(), any(), any())).thenReturn(false);
        try {
            accessControlService.verificarPermissao(usuario, acao, new Mapa());
        } catch (sgc.comum.erros.ErroAccessoNegado e) {
            assertThat(e.getMessage()).isEqualTo("Motivo Mapa");
        }
    }

    @Test
    @DisplayName("Deve lidar com usuário nulo ao obter motivo de negação")
    void deveLidarComUsuarioNuloAoObterMotivo() {
        try {
            // Chamando o método privado via reflexão ou apenas testando o efeito colateral se possível
            // Como obterMotivoNegacao é privado e chamado por verificarPermissao, 
            // mas verificarPermissao chama podeExecutar que retorna false se usuário é null
            accessControlService.verificarPermissao(null, Acao.VISUALIZAR_PROCESSO, new Processo());
        } catch (sgc.comum.erros.ErroAccessoNegado e) {
            assertThat(e.getMessage()).contains("Usuário não autenticado");
        }
    }

    @Test
    @DisplayName("Deve retornar mensagem genérica para tipo de recurso desconhecido")
    void deveRetornarMensagemGenericaParaRecursoDesconhecido() {
        Usuario usuario = criarUsuario("1");
        try {
            accessControlService.verificarPermissao(usuario, Acao.VISUALIZAR_PROCESSO, "String de recurso");
        } catch (sgc.comum.erros.ErroAccessoNegado e) {
            assertThat(e.getMessage()).contains("não tem permissão para executar a ação");
        }
    }

    private Usuario criarUsuario(String tituloEleitoral) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(tituloEleitoral);
        usuario.setNome("Usuário Teste");
        // Add perfil to avoid null pointer in access policies
        usuario.setAtribuicoes(new java.util.HashSet<>());
        return usuario;
    }

    private Processo criarProcesso(Long codigo) {
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        return processo;
    }

    @Test
    @DisplayName("Deve retornar false e não lançar NPE quando recurso é nulo")
    void deveRetornarFalseQuandoRecursoEhNulo() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.VISUALIZAR_PROCESSO;

        boolean resultado = accessControlService.podeExecutar(usuario, acao, null);

        assertThat(resultado).isFalse();
    }
}

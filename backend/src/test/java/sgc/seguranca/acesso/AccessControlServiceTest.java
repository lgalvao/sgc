package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AccessControlService")
class AccessControlServiceTest {

    @Mock
    private AccessAuditService auditService;

    @Mock
    private HierarchyService hierarchyService;

    @InjectMocks
    private AccessControlService accessControlService;

    @Test
    @DisplayName("Deve permitir todas as ações na implementação inicial (skeleton)")
    void devePermitirTodasAcoesNaImplementacaoInicial() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.VISUALIZAR_PROCESSO;
        Processo processo = criarProcesso(1L);
        
        boolean resultado = accessControlService.podeExecutar(usuario, acao, processo);
        
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve auditar acesso concedido ao verificar permissão")
    void deveAuditarAcessoConcedidoAoVerificarPermissao() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.VISUALIZAR_PROCESSO;
        Processo processo = criarProcesso(1L);
        
        assertDoesNotThrow(() -> 
            accessControlService.verificarPermissao(usuario, acao, processo)
        );
        
        verify(auditService).logAccessGranted(eq(usuario), eq(acao), eq(processo));
    }

    @Test
    @DisplayName("Deve retornar true para podeExecutar na implementação inicial")
    void deveRetornarTrueParaPodeExecutarNaImplementacaoInicial() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.EDITAR_PROCESSO;
        Processo processo = criarProcesso(1L);
        
        boolean resultado = accessControlService.podeExecutar(usuario, acao, processo);
        
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve funcionar com diferentes tipos de ações")
    void deveFuncionarComDiferentesTiposDeAcoes() {
        Usuario usuario = criarUsuario("123456789012");
        Processo processo = criarProcesso(1L);
        
        assertThat(accessControlService.podeExecutar(usuario, Acao.CRIAR_PROCESSO, processo)).isTrue();
        assertThat(accessControlService.podeExecutar(usuario, Acao.EDITAR_PROCESSO, processo)).isTrue();
        assertThat(accessControlService.podeExecutar(usuario, Acao.EXCLUIR_PROCESSO, processo)).isTrue();
    }

    private Usuario criarUsuario(String tituloEleitoral) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(tituloEleitoral);
        usuario.setNome("Usuário Teste");
        return usuario;
    }

    private Processo criarProcesso(Long codigo) {
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        return processo;
    }
}

package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do AccessAuditService")
class AccessAuditServiceTest {

    @InjectMocks
    private AccessAuditService auditService;

    @Test
    @DisplayName("Deve registrar acesso concedido sem lançar exceção")
    void deveRegistrarAcessoConcedido() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.VISUALIZAR_PROCESSO;
        Processo processo = criarProcesso(1L);
        
        assertDoesNotThrow(() -> 
            auditService.logAccessGranted(usuario, acao, processo)
        );
    }

    @Test
    @DisplayName("Deve registrar acesso negado sem lançar exceção")
    void deveRegistrarAcessoNegado() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.EDITAR_PROCESSO;
        Processo processo = criarProcesso(1L);
        String motivo = "Usuário não tem permissão ADMIN";
        
        assertDoesNotThrow(() -> 
            auditService.logAccessDenied(usuario, acao, processo, motivo)
        );
    }

    @Test
    @DisplayName("Deve registrar acesso com subprocesso")
    void deveRegistrarAcessoComSubprocesso() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.VISUALIZAR_SUBPROCESSO;
        Subprocesso subprocesso = criarSubprocesso(10L);
        
        assertDoesNotThrow(() -> 
            auditService.logAccessGranted(usuario, acao, subprocesso)
        );
    }

    @Test
    @DisplayName("Deve registrar acesso com objeto genérico")
    void deveRegistrarAcessoComObjetoGenerico() {
        Usuario usuario = criarUsuario("123456789012");
        Acao acao = Acao.EDITAR_CONFIGURACOES;
        String recurso = "ConfiguracaoGenerica";
        
        assertDoesNotThrow(() -> 
            auditService.logAccessGranted(usuario, acao, recurso)
        );
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

    private Subprocesso criarSubprocesso(Long codigo) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(codigo);
        return subprocesso;
    }
}

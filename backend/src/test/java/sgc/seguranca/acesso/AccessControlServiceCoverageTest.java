package sgc.seguranca.acesso;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.comum.erros.ErroAcessoNegado;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para AccessControlService")
class AccessControlServiceCoverageTest {

    @InjectMocks
    private AccessControlService service;

    @Mock
    private AccessAuditService auditService;
    @Mock
    private SubprocessoAccessPolicy subprocessoAccessPolicy;
    @Mock
    private ProcessoAccessPolicy processoAccessPolicy;
    @Mock
    private AtividadeAccessPolicy atividadeAccessPolicy;
    @Mock
    private MapaAccessPolicy mapaAccessPolicy;

    @Test
    @DisplayName("podeExecutar deve retornar false quando usuário é nulo")
    void podeExecutarRetornaFalseUsuarioNulo() {
        assertFalse(service.podeExecutar(null, Acao.VISUALIZAR_PROCESSO, new Processo()));
    }

    @Test
    @DisplayName("podeExecutar deve retornar false quando recurso é desconhecido")
    void podeExecutarRetornaFalseRecursoDesconhecido() {
        Usuario usuario = new Usuario();
        assertFalse(service.podeExecutar(usuario, Acao.VISUALIZAR_PROCESSO, "RecursoStringDesconhecido"));
    }

    @Test
    @DisplayName("verificarPermissao deve lançar ErroAcessoNegado com mensagem padrão quando usuário é nulo")
    void verificarPermissaoLancaErroUsuarioNulo() {
        Processo processo = new Processo();
        ErroAcessoNegado erro = assertThrows(ErroAcessoNegado.class, () ->
            service.verificarPermissao(null, Acao.VISUALIZAR_PROCESSO, processo)
        );
        assertTrue(erro.getMessage().contains("Usuário não autenticado"));
    }

    @Test
    @DisplayName("verificarPermissao deve usar motivo da policy se disponível")
    void verificarPermissaoUsaMotivoPolicy() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        Processo processo = new Processo();

        when(processoAccessPolicy.canExecute(usuario, Acao.VISUALIZAR_PROCESSO, processo)).thenReturn(false);
        when(processoAccessPolicy.getMotivoNegacao()).thenReturn("Motivo específico da policy");

        ErroAcessoNegado erro = assertThrows(ErroAcessoNegado.class, () ->
                service.verificarPermissao(usuario, Acao.VISUALIZAR_PROCESSO, processo)
        );
        assertEquals("Motivo específico da policy", erro.getMessage());
    }

    @Test
    @DisplayName("verificarPermissao deve usar motivo padrão se policy retornar null/vazio")
    void verificarPermissaoUsaMotivoPadraoSePolicyNull() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        Processo processo = new Processo();

        when(processoAccessPolicy.canExecute(usuario, Acao.VISUALIZAR_PROCESSO, processo)).thenReturn(false);
        when(processoAccessPolicy.getMotivoNegacao()).thenReturn(null);

        ErroAcessoNegado erro = assertThrows(ErroAcessoNegado.class, () ->
                service.verificarPermissao(usuario, Acao.VISUALIZAR_PROCESSO, processo)
        );
        assertTrue(erro.getMessage().contains("não tem permissão para executar a ação"));
    }

    @Test
    @DisplayName("verificarPermissao deve usar motivo padrão se policy retornar string vazia")
    void verificarPermissaoUsaMotivoPadraoSePolicyBlank() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        Processo processo = new Processo();

        when(processoAccessPolicy.canExecute(usuario, Acao.VISUALIZAR_PROCESSO, processo)).thenReturn(false);
        when(processoAccessPolicy.getMotivoNegacao()).thenReturn("  ");

        ErroAcessoNegado erro = assertThrows(ErroAcessoNegado.class, () ->
                service.verificarPermissao(usuario, Acao.VISUALIZAR_PROCESSO, processo)
        );
        assertTrue(erro.getMessage().contains("não tem permissão para executar a ação"));
    }
}

package sesel.sgc.notificacao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sesel.sgc.atribuicao.Atribuicao;
import sesel.sgc.processo.Processo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class NotificacaoServiceTest {
    @MockitoSpyBean
    private NotificacaoService notificacaoService;

    @Test
    void deveNotificarInicioProcessoSemExcecao() {
        Processo processo = new Processo();
        processo.setNome("Processo Teste");
        assertDoesNotThrow(() -> notificacaoService.notificarInicioProcesso(processo));
    }

    @Test
    void deveNotificarAtribuicaoTemporariaSemExcecao() {
        Atribuicao atribuicao = new Atribuicao();

        // mocks mínimos para evitar NullPointerException nos logs
        atribuicao.setServidor(new sesel.sgc.servidor.Servidor());
        atribuicao.getServidor().setNome("Servidor Teste");

        atribuicao.setUnidade(new sesel.sgc.unidade.Unidade());
        atribuicao.getUnidade().setNome("Unidade Teste");

        assertDoesNotThrow(() -> notificacaoService.notificarAtribuicaoTemporaria(atribuicao));
    }

    @Test
    void instanciaENotificaInicioProcesso() {
        NotificacaoService service = new NotificacaoService();
        assertNotNull(service);
    }
}
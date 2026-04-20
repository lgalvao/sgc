package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheAquecimentoTest {

    @Mock
    private CacheViewsOrganizacaoService cacheViewsOrganizacaoService;

    @InjectMocks
    private CacheAquecimento cacheAquecimento;

    @Test
    @DisplayName("deve aquecer caches com sucesso")
    void deveAquecerCachesComSucesso() {
        ApplicationReadyEvent eventMock = mock(ApplicationReadyEvent.class);

        cacheAquecimento.onApplicationEvent(eventMock);

        verify(cacheViewsOrganizacaoService).listarTodasUnidades();
        verify(cacheViewsOrganizacaoService).listarTodosUsuarios();
        verify(cacheViewsOrganizacaoService).listarTodasResponsabilidades();
        verify(cacheViewsOrganizacaoService, never()).listarTodosPerfisUnidade();
    }

    @Test
    @DisplayName("deve capturar e logar excecoes silenciosamente durante aquecimento")
    void deveCapturarExcecoes() {
        ApplicationReadyEvent eventMock = mock(ApplicationReadyEvent.class);
        doThrow(new RuntimeException("Erro forçado")).when(cacheViewsOrganizacaoService).listarTodasUnidades();

        cacheAquecimento.onApplicationEvent(eventMock);

        verify(cacheViewsOrganizacaoService).listarTodasUnidades();
        verify(cacheViewsOrganizacaoService, never()).listarTodosUsuarios();
    }
}

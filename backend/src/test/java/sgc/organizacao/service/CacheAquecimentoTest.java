package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.boot.context.event.*;

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
        verify(cacheViewsOrganizacaoService).listarTodosPerfisUnidade();
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

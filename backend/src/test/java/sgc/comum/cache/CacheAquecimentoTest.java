package sgc.comum.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import sgc.organizacao.service.CacheViewsOrganizacaoService;
import sgc.organizacao.service.UnidadeHierarquiaService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheAquecimentoTest {

    @Mock
    private CacheViewsOrganizacaoService cacheViewsOrganizacaoService;

    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;

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
        verify(unidadeHierarquiaService).buscarArvoreHierarquica();
        verify(unidadeHierarquiaService).buscarMapaHierarquia();
        verify(unidadeHierarquiaService).buscarMapaFilhoPai();
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

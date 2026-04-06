package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.cache.*;
import org.springframework.jdbc.core.*;
import sgc.comum.erros.*;
import sgc.processo.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoExclusaoCompletaService")
class ProcessoExclusaoCompletaServiceTest {
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cacheA;
    @Mock
    private Cache cacheB;

    @InjectMocks
    private ProcessoExclusaoCompletaService service;

    @Test
    @DisplayName("deve falhar quando o processo nao existe")
    void deveFalharQuandoProcessoNaoExiste() {
        when(processoRepo.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.excluirCompleto(10L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Processo");

        verifyNoInteractions(jdbcTemplate, cacheManager);
    }

    @Test
    @DisplayName("deve excluir dependentes em ordem e limpar caches")
    void deveExcluirDependentesEmOrdemELimparCaches() {
        when(processoRepo.existsById(10L)).thenReturn(true);
        when(cacheManager.getCacheNames()).thenReturn(Set.of("cacheA", "cacheB"));
        when(cacheManager.getCache("cacheA")).thenReturn(cacheA);
        when(cacheManager.getCache("cacheB")).thenReturn(cacheB);

        service.excluirCompleto(10L);

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).update(contains("DELETE FROM sgc.alerta_usuario"), eq(10L));
        inOrder.verify(jdbcTemplate).update(eq("DELETE FROM sgc.alerta WHERE processo_codigo = ?"), eq(10L));
        inOrder.verify(jdbcTemplate).update(contains("DELETE FROM sgc.avaliacao_servidor"), eq(10L), eq(10L));
        inOrder.verify(jdbcTemplate).update(contains("DELETE FROM sgc.ocupacao_critica"), eq(10L), eq(10L));
        inOrder.verify(jdbcTemplate).update(contains("DELETE FROM sgc.diagnostico"), eq(10L));
        inOrder.verify(jdbcTemplate).update(contains("DELETE FROM sgc.mapa"), eq(10L));
        inOrder.verify(jdbcTemplate).update(eq("DELETE FROM sgc.processo WHERE codigo = ?"), eq(10L));

        verify(cacheA).clear();
        verify(cacheB).clear();
    }
}

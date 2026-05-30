package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.cache.*;
import org.springframework.jdbc.core.*;
import sgc.comum.erros.*;
import sgc.comum.model.ComumRepo;
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
    @Mock
    private ComumRepo comumRepo;

    @InjectMocks
    private ProcessoExclusaoCompletaService service;

    @Test
    @DisplayName("deve falhar quando o processo nao existe")
    void deveFalharQuandoProcessoNaoExiste() {
        when(comumRepo.buscar(Processo.class, 10L)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 10L));

        assertThatThrownBy(() -> service.excluirCompleto(10L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Processo");

        verifyNoInteractions(jdbcTemplate, cacheManager);
    }

    @Test
    @DisplayName("deve excluir dependentes em ordem e limpar caches")
    void deveExcluirDependentesEmOrdemELimparCaches() {
        when(comumRepo.buscar(Processo.class, 10L)).thenReturn(new Processo());
        when(cacheManager.getCacheNames()).thenReturn(Set.of("cacheA", "cacheB"));
        when(cacheManager.getCache("cacheA")).thenReturn(cacheA);
        when(cacheManager.getCache("cacheB")).thenReturn(cacheB);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);

        service.excluirCompleto(10L);

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).update(
                argThat(sql -> sql.contains("DELETE FROM sgc.notificacao_email") && sql.contains("subprocesso_codigo")),
                eq(10L)
        );
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

    @Test
    @DisplayName("deve pular tabelas opcionais de diagnostico ausentes")
    void devePularTabelasOpcionaisDeDiagnosticoAusentes() {
        when(comumRepo.buscar(Processo.class, 10L)).thenReturn(new Processo());
        when(cacheManager.getCacheNames()).thenReturn(Set.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("AVALIACAO_SERVIDOR"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("OCUPACAO_CRITICA"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("DIAGNOSTICO"))).thenReturn(0);

        service.excluirCompleto(10L);

        verify(jdbcTemplate, never()).update(contains("DELETE FROM sgc.avaliacao_servidor"), anyLong(), anyLong());
        verify(jdbcTemplate, never()).update(contains("DELETE FROM sgc.ocupacao_critica"), anyLong(), anyLong());
        verify(jdbcTemplate, never()).update(contains("DELETE FROM sgc.diagnostico"), anyLong());
        verify(jdbcTemplate).update(eq("DELETE FROM sgc.processo WHERE codigo = ?"), eq(10L));
    }

    @Test
    @DisplayName("deve lidar com cache retornando null")
    void deveLidarComCacheRetornandoNull() {
        when(comumRepo.buscar(Processo.class, 10L)).thenReturn(new Processo());
        when(cacheManager.getCacheNames()).thenReturn(Set.of("cacheC"));
        when(cacheManager.getCache("cacheC")).thenReturn(null);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);

        service.excluirCompleto(10L);

        verify(cacheManager).getCache("cacheC");
        verify(cacheA, never()).clear();
        verify(cacheB, never()).clear();
    }

    @Test
    @DisplayName("deve lidar com query de tabela existente retornando null")
    void deveLidarComQueryDeTabelaExistenteRetornandoNull() {
        when(comumRepo.buscar(Processo.class, 10L)).thenReturn(new Processo());
        when(cacheManager.getCacheNames()).thenReturn(Set.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(null);

        service.excluirCompleto(10L);

        verify(jdbcTemplate, never()).update(contains("DELETE FROM sgc.avaliacao_servidor"), anyLong(), anyLong());
        verify(jdbcTemplate, never()).update(contains("DELETE FROM sgc.ocupacao_critica"), anyLong(), anyLong());
        verify(jdbcTemplate, never()).update(contains("DELETE FROM sgc.diagnostico"), anyLong());
        verify(jdbcTemplate).update(eq("DELETE FROM sgc.processo WHERE codigo = ?"), eq(10L));
    }
}

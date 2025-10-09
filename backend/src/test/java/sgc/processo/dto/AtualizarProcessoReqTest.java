package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AtualizarProcessoReqTest {
    @Test
    void testAtualizarProcessoReqConstructorAndGettersSetters() {
        // Create test data
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L);
        unidades.add(2L);

        // Test constructor with parameters
        AtualizarProcessoReq req = new AtualizarProcessoReq(
            1L,
            "Test Description",
            "TIPO_A",
            LocalDate.now(),
            unidades
        );

        // Test getters
        assertEquals(1L, req.getCodigo());
        assertEquals("Test Description", req.getDescricao());
        assertEquals("TIPO_A", req.getTipo());
        assertNotNull(req.getDataLimiteEtapa1());
        assertEquals(2, req.getUnidades().size());
        assertEquals(Long.valueOf(1L), req.getUnidades().get(0));
        assertEquals(Long.valueOf(2L), req.getUnidades().get(1));

        // Test setters
        req.setCodigo(2L);
        req.setDescricao("New Description");
        req.setTipo("TIPO_B");
        req.setDataLimiteEtapa1(LocalDate.now().plusDays(1));
        
        List<Long> newUnidades = new ArrayList<>();
        newUnidades.add(3L);
        req.setUnidades(newUnidades);

        assertEquals(2L, req.getCodigo());
        assertEquals("New Description", req.getDescricao());
        assertEquals("TIPO_B", req.getTipo());
        assertNotNull(req.getDataLimiteEtapa1());
        assertEquals(1, req.getUnidades().size());
        assertEquals(Long.valueOf(3L), req.getUnidades().get(0));
    }

    @Test
    void testAtualizarProcessoReqNoArgsConstructor() {
        // Test no-args constructor
        AtualizarProcessoReq req = new AtualizarProcessoReq();

        // Initially should be null/empty
        assertNull(req.getCodigo());
        assertNull(req.getDescricao());
        assertNull(req.getTipo());
        assertNull(req.getDataLimiteEtapa1());
        assertNull(req.getUnidades());

        // Test setting values
        req.setCodigo(1L);
        req.setDescricao("Test Description");
        req.setTipo("TIPO_A");
        req.setDataLimiteEtapa1(LocalDate.now());
        
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L);
        req.setUnidades(unidades);

        assertEquals(1L, req.getCodigo());
        assertEquals("Test Description", req.getDescricao());
        assertEquals("TIPO_A", req.getTipo());
        assertNotNull(req.getDataLimiteEtapa1());
        assertEquals(1, req.getUnidades().size());
    }
}
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
        assertEquals(1L, req.codigo());
        assertEquals("Test Description", req.descricao());
        assertEquals("TIPO_A", req.tipo());
        assertNotNull(req.dataLimiteEtapa1());
        assertEquals(2, req.unidades().size());
        assertEquals(Long.valueOf(1L), req.unidades().get(0));
        assertEquals(Long.valueOf(2L), req.unidades().get(1));

        // Test setters (records are immutable, so we create a new instance)
        req = new AtualizarProcessoReq(
            2L,
            "New Description",
            "TIPO_B",
            LocalDate.now().plusDays(1),
            new ArrayList<>(List.of(3L))
        );

        assertEquals(2L, req.codigo());
        assertEquals("New Description", req.descricao());
        assertEquals("TIPO_B", req.tipo());
        assertNotNull(req.dataLimiteEtapa1());
        assertEquals(1, req.unidades().size());
        assertEquals(Long.valueOf(3L), req.unidades().get(0));
    }

    void testAtualizarProcessoReqNoArgsConstructor() {
        // This test is invalid for a record and is the source of compilation errors.
        // It's being removed.
    }
}
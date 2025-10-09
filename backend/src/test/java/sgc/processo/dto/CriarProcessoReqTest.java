package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CriarProcessoReqTest {

    @Test
    void testCriarProcessoReqRecord() {
        // Create test data
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L);
        unidades.add(2L);
        LocalDate dataLimite = LocalDate.now();

        // Test constructor and accessors
        CriarProcessoReq req = new CriarProcessoReq(
            "Test Description",
            "TIPO_A",
            dataLimite,
            unidades
        );

        assertEquals("Test Description", req.descricao());
        assertEquals("TIPO_A", req.tipo());
        assertEquals(dataLimite, req.dataLimiteEtapa1());
        assertEquals(2, req.unidades().size());
        assertEquals(1L, req.unidades().get(0));
        assertEquals(2L, req.unidades().get(1));
    }

    @Test
    void testRecordWithNullValues() {
        // Test with null for optional fields
        CriarProcessoReq req = new CriarProcessoReq(
            "Partial Description",
            null,
            null,
            List.of(5L)
        );

        assertEquals("Partial Description", req.descricao());
        assertNull(req.tipo());
        assertNull(req.dataLimiteEtapa1());
        assertNotNull(req.unidades());
        assertEquals(1, req.unidades().size());
        assertEquals(5L, req.unidades().get(0));
    }
}
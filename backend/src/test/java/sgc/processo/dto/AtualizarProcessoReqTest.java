package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AtualizarProcessoReqTest {

    @Test
    void testAtualizarProcessoReqRecord() {
        // Create test data
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L);
        unidades.add(2L);
        LocalDate dataLimite = LocalDate.now();

        // Test constructor and accessors
        AtualizarProcessoReq req = new AtualizarProcessoReq(
            1L,
            "Test Description",
            "TIPO_A",
            dataLimite,
            unidades
        );

        assertEquals(1L, req.codigo());
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
        AtualizarProcessoReq req = new AtualizarProcessoReq(
            5L,
            "Partial Description",
            null,
            null,
            List.of(1L)
        );

        assertEquals(5L, req.codigo());
        assertEquals("Partial Description", req.descricao());
        assertNull(req.tipo());
        assertNull(req.dataLimiteEtapa1());
        assertNotNull(req.unidades());
    }
}
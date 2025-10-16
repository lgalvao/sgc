package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AtualizarProcessoReqTest {
    @Test
    void testAtualizarProcessoReqConstructorAndGettersSetters() {
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L);
        unidades.add(2L);

        AtualizarProcessoReq req = new AtualizarProcessoReq(
            1L,
            "Test Description",
            "TIPO_A",
            LocalDateTime.now(),
            unidades
        );

        assertEquals(1L, req.codigo());
        assertEquals("Test Description", req.descricao());
        assertEquals("TIPO_A", req.tipo());
        assertNotNull(req.dataLimiteEtapa1());
        assertEquals(2, req.unidades().size());
        assertEquals(Long.valueOf(1L), req.unidades().get(0));
        assertEquals(Long.valueOf(2L), req.unidades().get(1));

        req = new AtualizarProcessoReq(
            2L,
            "New Description",
            "TIPO_B",
            LocalDateTime.now().plusDays(1),
            new ArrayList<>(List.of(3L))
        );

        assertEquals(2L, req.codigo());
        assertEquals("New Description", req.descricao());
        assertEquals("TIPO_B", req.tipo());
        assertNotNull(req.dataLimiteEtapa1());
        assertEquals(1, req.unidades().size());
        assertEquals(Long.valueOf(3L), req.unidades().getFirst());
    }
}
package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CriarProcessoReqTest {
    @Test
    void testCriarProcessoReqConstructorAndAccessors() {
        var unidades = List.of(1L, 2L);
        var dataLimite = LocalDate.now();

        var req = new CriarProcessoReq(
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
}
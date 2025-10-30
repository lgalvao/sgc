package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import sgc.processo.modelo.TipoProcesso;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CriarProcessoReqTest {
    @Test
    void testCriarProcessoReqConstructorAndAccessors() {
        var unidades = List.of(1L, 2L);
        var dataLimite = LocalDateTime.now();

        var req = new CriarProcessoReq(
            "Test Description",
            TipoProcesso.MAPEAMENTO,
            dataLimite,
            unidades
        );

        assertEquals("Test Description", req.descricao());
        assertEquals(TipoProcesso.MAPEAMENTO, req.tipo());
        assertEquals(dataLimite, req.dataLimiteEtapa1());
        assertEquals(2, req.unidades().size());
        assertEquals(1L, req.unidades().get(0));
        assertEquals(2L, req.unidades().get(1));
    }
}
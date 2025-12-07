package sgc.processo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import sgc.processo.model.TipoProcesso;

class CriarProcessoReqTest {
    @Test
    void testCriarProcessoReqConstructorAndAccessors() {
        var unidades = List.of(1L, 2L);
        var dataLimite = LocalDateTime.now();

        var req =
                new CriarProcessoReq(
                        "Test Description", TipoProcesso.MAPEAMENTO, dataLimite, unidades);

        assertEquals("Test Description", req.getDescricao());
        assertEquals(TipoProcesso.MAPEAMENTO, req.getTipo());
        assertEquals(dataLimite, req.getDataLimiteEtapa1());
        assertEquals(2, req.getUnidades().size());
        assertEquals(1L, req.getUnidades().get(0));
        assertEquals(2L, req.getUnidades().get(1));
    }
}

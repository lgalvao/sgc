package sgc.processo.dto;

import org.junit.jupiter.api.Test;
import sgc.processo.model.TipoProcesso;

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

        AtualizarProcessoReq req =
                new AtualizarProcessoReq(
                        1L,
                        "Test Description",
                        TipoProcesso.MAPEAMENTO,
                        LocalDateTime.now(),
                        unidades);

        assertEquals(1L, req.getCodigo());
        assertEquals("Test Description", req.getDescricao());
        assertEquals(TipoProcesso.MAPEAMENTO, req.getTipo());
        assertNotNull(req.getDataLimiteEtapa1());
        assertEquals(2, req.getUnidades().size());
        assertEquals(Long.valueOf(1L), req.getUnidades().get(0));
        assertEquals(Long.valueOf(2L), req.getUnidades().get(1));

        req =
                new AtualizarProcessoReq(
                        2L,
                        "New Description",
                        TipoProcesso.REVISAO,
                        LocalDateTime.now().plusDays(1),
                        new ArrayList<>(List.of(3L)));

        assertEquals(2L, req.getCodigo());
        assertEquals("New Description", req.getDescricao());
        assertEquals(TipoProcesso.REVISAO, req.getTipo());
        assertNotNull(req.getDataLimiteEtapa1());
        assertEquals(1, req.getUnidades().size());
        assertEquals(Long.valueOf(3L), req.getUnidades().getFirst());
    }
}

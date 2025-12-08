package sgc.processo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import sgc.processo.erros.ErroProcesso;

class ModeloTest {
    @Test
    void processo_GettersAndSetters() {
        Processo processo = new Processo();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataLimite = now.plusDays(30);

        processo.setCodigo(1L);
        processo.setDataCriacao(now);
        processo.setDataFinalizacao(now);
        processo.setDataLimite(dataLimite);
        processo.setDescricao("Descrição do Processo");
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        assertEquals(1L, processo.getCodigo());
        assertNotNull(processo.getDataCriacao());
        assertNotNull(processo.getDataFinalizacao());
        assertTrue(ChronoUnit.SECONDS.between(dataLimite, processo.getDataLimite()) < 1);
        assertEquals("Descrição do Processo", processo.getDescricao());
        assertEquals(SituacaoProcesso.CRIADO, processo.getSituacao());
        assertEquals(TipoProcesso.MAPEAMENTO, processo.getTipo());

        Processo processo2 =
                new Processo(
                        "Descricao", TipoProcesso.MAPEAMENTO, SituacaoProcesso.CRIADO, dataLimite);
        assertNotNull(processo2);
    }

    @Test
    void erroProcesso_ConstructorAndGetMessage() {
        ErroProcesso erro = new ErroProcesso("Mensagem de erro do processo");

        assertEquals("Mensagem de erro do processo", erro.getMessage());
        assertInstanceOf(RuntimeException.class, erro);
    }
}

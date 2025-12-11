package sgc.processo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SituacaoProcessoTest {

    @Test
    void deveRetornarLabelCorretoParaCriado() {
        assertEquals("Criado", SituacaoProcesso.CRIADO.getLabel());
    }

    @Test
    void deveRetornarLabelCorretoParaEmAndamento() {
        assertEquals("Em andamento", SituacaoProcesso.EM_ANDAMENTO.getLabel());
    }

    @Test
    void deveRetornarLabelCorretoParaFinalizado() {
        assertEquals("Finalizado", SituacaoProcesso.FINALIZADO.getLabel());
    }

    @Test
    void deveTerTresSituacoes() {
        assertEquals(3, SituacaoProcesso.values().length);
    }
}

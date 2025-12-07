package sgc.processo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoProcessoTest {

    @Test
    void deveRetornarLabelCorretoParaMapeamento() {
        assertEquals("Mapeamento", TipoProcesso.MAPEAMENTO.getLabel());
    }

    @Test
    void deveRetornarLabelCorretoParaRevisao() {
        assertEquals("Revisão", TipoProcesso.REVISAO.getLabel());
    }

    @Test
    void deveRetornarLabelCorretoParaDiagnostico() {
        assertEquals("Diagnóstico", TipoProcesso.DIAGNOSTICO.getLabel());
    }

    @Test
    void deveTerTresTipos() {
        assertEquals(3, TipoProcesso.values().length);
    }
}

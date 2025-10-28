package sgc.processo.modelo;

import org.junit.jupiter.api.Test;
import sgc.unidade.modelo.TipoUnidade;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

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

        Processo processo2 = new Processo(now, now, dataLimite, "Descricao", SituacaoProcesso.CRIADO, TipoProcesso.MAPEAMENTO);
        assertNotNull(processo2);
    }

    @Test
    void unidadeProcesso_GettersAndSetters() {
        UnidadeProcesso unidadeProcesso = new UnidadeProcesso();

        unidadeProcesso.setCodigo(1L);
        unidadeProcesso.setCodProcesso(10L);
        unidadeProcesso.setNome("Unidade Teste");
        unidadeProcesso.setSigla("UT");
        unidadeProcesso.setTitularTitulo("Titular Teste");
        unidadeProcesso.setTipo(TipoUnidade.OPERACIONAL);
        unidadeProcesso.setSituacao("PENDENTE");
        unidadeProcesso.setCodUnidadeSuperior(100L);

        assertEquals(1L, unidadeProcesso.getCodigo());
        assertEquals(10L, unidadeProcesso.getCodProcesso());
        assertEquals("Unidade Teste", unidadeProcesso.getNome());
        assertEquals("UT", unidadeProcesso.getSigla());
        assertEquals("Titular Teste", unidadeProcesso.getTitularTitulo());
        assertEquals(TipoUnidade.OPERACIONAL, unidadeProcesso.getTipo());
        assertEquals("PENDENTE", unidadeProcesso.getSituacao());
        assertEquals(100L, unidadeProcesso.getCodUnidadeSuperior());

        UnidadeProcesso unidadeProcesso2 = new UnidadeProcesso(10L, 20L, "Nome", "Sigla",
                "Titular", TipoUnidade.OPERACIONAL, "Situacao", 100L);
        assertNotNull(unidadeProcesso2);
    }

    @Test
    void erroProcesso_ConstructorAndGetMessage() {
        ErroProcesso erro = new ErroProcesso("Mensagem de erro do processo");

        assertEquals("Mensagem de erro do processo", erro.getMessage());
        assertInstanceOf(RuntimeException.class, erro);
    }
}
package sgc.processo.modelo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModeloTest {
    @Test
    void processo_GettersAndSetters() {
        Processo processo = new Processo();

        processo.setCodigo(1L);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataFinalizacao(LocalDateTime.now());
        processo.setDataLimite(LocalDate.now().plusDays(30));
        processo.setDescricao("Descrição do Processo");
        processo.setSituacao("CRIADO");
        processo.setTipo("MAPEAMENTO");

        assertEquals(1L, processo.getCodigo());
        assertNotNull(processo.getDataCriacao());
        assertNotNull(processo.getDataFinalizacao());
        assertEquals(LocalDate.now().plusDays(30), processo.getDataLimite());
        assertEquals("Descrição do Processo", processo.getDescricao());
        assertEquals("CRIADO", processo.getSituacao());
        assertEquals("MAPEAMENTO", processo.getTipo());

        Processo processo2 = new Processo(LocalDateTime.now(), LocalDateTime.now(),
                LocalDate.now().plusDays(30), "Descricao", "Situacao", "Tipo");
        assertNotNull(processo2);
    }

    @Test
    void unidadeProcesso_GettersAndSetters() {
        UnidadeProcesso unidadeProcesso = new UnidadeProcesso();

        unidadeProcesso.setCodigo(1L);
        unidadeProcesso.setProcessoCodigo(10L);
        unidadeProcesso.setNome("Unidade Teste");
        unidadeProcesso.setSigla("UT");
        unidadeProcesso.setTitularTitulo("Titular Teste");
        unidadeProcesso.setTipo("OPERACIONAL");
        unidadeProcesso.setSituacao("PENDENTE");
        unidadeProcesso.setUnidadeSuperiorCodigo(100L);

        assertEquals(1L, unidadeProcesso.getCodigo());
        assertEquals(10L, unidadeProcesso.getProcessoCodigo());
        assertEquals("Unidade Teste", unidadeProcesso.getNome());
        assertEquals("UT", unidadeProcesso.getSigla());
        assertEquals("Titular Teste", unidadeProcesso.getTitularTitulo());
        assertEquals("OPERACIONAL", unidadeProcesso.getTipo());
        assertEquals("PENDENTE", unidadeProcesso.getSituacao());
        assertEquals(100L, unidadeProcesso.getUnidadeSuperiorCodigo());

        UnidadeProcesso unidadeProcesso2 = new UnidadeProcesso(10L, 20L, "Nome", "Sigla",
                "Titular", "Tipo", "Situacao", 100L);
        assertNotNull(unidadeProcesso2);
    }

    @Test
    void erroProcesso_ConstructorAndGetMessage() {
        ErroProcesso erro = new ErroProcesso("Mensagem de erro do processo");

        assertEquals("Mensagem de erro do processo", erro.getMessage());
        assertTrue(erro instanceof RuntimeException);
    }
}
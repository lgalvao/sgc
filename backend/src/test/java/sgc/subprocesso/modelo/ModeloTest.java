package sgc.subprocesso.modelo;

import org.junit.jupiter.api.Test;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.mapa.modelo.Mapa;
import sgc.processo.modelo.Processo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModeloTest {
    @Test
    void Subprocesso_GettersAndSetters() {
        Subprocesso subprocesso = new Subprocesso();
        
        Processo processo = new Processo();
        processo.setCodigo(1L);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        
        subprocesso.setCodigo(1L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
        subprocesso.setDataLimiteEtapa1(LocalDate.now());
        subprocesso.setDataFimEtapa1(LocalDateTime.now());
        subprocesso.setDataLimiteEtapa2(LocalDate.now());
        subprocesso.setDataFimEtapa2(LocalDateTime.now());
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        
        assertEquals(1L, subprocesso.getCodigo());
        assertEquals(processo, subprocesso.getProcesso());
        assertEquals(unidade, subprocesso.getUnidade());
        assertEquals(mapa, subprocesso.getMapa());
        assertNotNull(subprocesso.getDataLimiteEtapa1());
        assertNotNull(subprocesso.getDataFimEtapa1());
        assertNotNull(subprocesso.getDataLimiteEtapa2());
        assertNotNull(subprocesso.getDataFimEtapa2());
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, subprocesso.getSituacao());
    }

    @Test
    void Subprocesso_ConstructorWithParameters() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        
        Subprocesso subprocesso = new Subprocesso(
            processo, unidade, mapa, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, LocalDate.now()
        );
        
        assertEquals(processo, subprocesso.getProcesso());
        assertEquals(unidade, subprocesso.getUnidade());
        assertEquals(mapa, subprocesso.getMapa());
        assertEquals(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, subprocesso.getSituacao());
        assertNotNull(subprocesso.getDataLimiteEtapa1());
    }

    @Test
    void Movimentacao_GettersAndSetters() {
        Movimentacao movimentacao = new Movimentacao();
        
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(1L);
        
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(2L);
        
        movimentacao.setCodigo(1L);
        movimentacao.setSubprocesso(subprocesso);
        movimentacao.setDataHora(LocalDateTime.now());
        movimentacao.setUnidadeOrigem(unidadeOrigem);
        movimentacao.setUnidadeDestino(unidadeDestino);
        movimentacao.setDescricao("Descrição");
        
        assertEquals(1L, movimentacao.getCodigo());
        assertEquals(subprocesso, movimentacao.getSubprocesso());
        assertNotNull(movimentacao.getDataHora());
        assertEquals(unidadeOrigem, movimentacao.getUnidadeOrigem());
        assertEquals(unidadeDestino, movimentacao.getUnidadeDestino());
        assertEquals("Descrição", movimentacao.getDescricao());
    }

    @Test
    void Movimentacao_ConstructorWithParameters() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(1L);
        
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(2L);
        
        Movimentacao movimentacao = new Movimentacao(
            subprocesso, unidadeOrigem, unidadeDestino, "Descrição de movimentação"
        );
        
        assertEquals(subprocesso, movimentacao.getSubprocesso());
        assertEquals(unidadeOrigem, movimentacao.getUnidadeOrigem());
        assertEquals(unidadeDestino, movimentacao.getUnidadeDestino());
        assertEquals("Descrição de movimentação", movimentacao.getDescricao());
        assertNotNull(movimentacao.getDataHora());
    }

    @Test
    void ErroSubprocesso_ConstructorAndGetMessage() {
        ErroSubprocesso erro = new ErroSubprocesso("Mensagem de erro");
        assertEquals("Mensagem de erro", erro.getMessage());
    }
}

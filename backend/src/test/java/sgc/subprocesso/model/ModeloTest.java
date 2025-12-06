package sgc.subprocesso.model;

import org.junit.jupiter.api.Test;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModeloTest {
    @Test
    void subprocessoGettersAndSetters() {
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
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now());
        subprocesso.setDataFimEtapa1(LocalDateTime.now());
        subprocesso.setDataLimiteEtapa2(LocalDateTime.now());
        subprocesso.setDataFimEtapa2(LocalDateTime.now());
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        
        assertEquals(1L, subprocesso.getCodigo());
        assertEquals(processo.getCodigo(), subprocesso.getProcesso().getCodigo());
        assertEquals(unidade.getCodigo(), subprocesso.getUnidade().getCodigo());
        assertEquals(mapa.getCodigo(), subprocesso.getMapa().getCodigo());
        assertNotNull(subprocesso.getDataLimiteEtapa1());
        assertNotNull(subprocesso.getDataFimEtapa1());
        assertNotNull(subprocesso.getDataLimiteEtapa2());
        assertNotNull(subprocesso.getDataFimEtapa2());
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, subprocesso.getSituacao());
    }

    @Test
    void subprocessoConstructorWithParameters() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        
        Subprocesso subprocesso = new Subprocesso(
            processo, unidade, mapa, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, LocalDateTime.now()
        );
        
        assertEquals(processo.getCodigo(), subprocesso.getProcesso().getCodigo());
        assertEquals(unidade.getCodigo(), subprocesso.getUnidade().getCodigo());
        assertEquals(mapa.getCodigo(), subprocesso.getMapa().getCodigo());
        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, subprocesso.getSituacao());
        assertNotNull(subprocesso.getDataLimiteEtapa1());
    }

    @Test
    void movimentacaoGettersAndSetters() {
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
        assertEquals(subprocesso.getCodigo(), movimentacao.getSubprocesso().getCodigo());
        assertNotNull(movimentacao.getDataHora());
        assertEquals(unidadeOrigem.getCodigo(), movimentacao.getUnidadeOrigem().getCodigo());
        assertEquals(unidadeDestino.getCodigo(), movimentacao.getUnidadeDestino().getCodigo());
        assertEquals("Descrição", movimentacao.getDescricao());
    }

    @Test
    void movimentacaoConstructorWithParameters() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(1L);
        
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(2L);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        
        Movimentacao movimentacao = new Movimentacao(
            subprocesso, unidadeOrigem, unidadeDestino, "Descrição de movimentação", usuario
        );
        
        assertEquals(subprocesso.getCodigo(), movimentacao.getSubprocesso().getCodigo());
        assertEquals(unidadeOrigem.getCodigo(), movimentacao.getUnidadeOrigem().getCodigo());
        assertEquals(unidadeDestino.getCodigo(), movimentacao.getUnidadeDestino().getCodigo());
        assertEquals("Descrição de movimentação", movimentacao.getDescricao());
        assertNotNull(movimentacao.getDataHora());
    }

    @Test
    void erroSubprocessoConstructorAndGetMessage() {
        ErroSubprocesso erro = new ErroSubprocesso("Mensagem de erro");
        assertEquals("Mensagem de erro", erro.getMessage());
    }
}

package sgc.subprocesso.model;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.comum.model.ComumViews;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class SubprocessoJsonViewTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveOcultarRelacionamentosNaVisaoComum() throws Exception {
        Processo processo = new Processo();
        processo.setCodigo(50L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setProcesso(processo);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        String json = objectMapper
                .writerWithView(ComumViews.Publica.class)
                .writeValueAsString(sp);

        assertThat(json).contains("\"codigo\":1");
        assertThat(json).contains("\"codProcesso\":50");
        
        // Relacionamentos completos devem ser ignorados na visão Comum
        assertThat(json).doesNotContain("\"processo\":{");
        assertThat(json).doesNotContain("\"unidade\":{");
        assertThat(json).doesNotContain("\"mapa\":{");
    }

    @Test
    void deveIncluirRelacionamentosNaVisaoSubprocessoPublica() throws Exception {
        Processo processo = new Processo();
        processo.setCodigo(50L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U1");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setProcesso(processo);
        sp.setUnidade(unidade);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        String json = objectMapper
                .writerWithView(SubprocessoViews.Publica.class)
                .writeValueAsString(sp);

        assertThat(json).contains("\"processo\":{");
        assertThat(json).contains("\"unidade\":{");
        assertThat(json).contains("\"sigla\":\"U1\"");
    }
}

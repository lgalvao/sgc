package sgc.subprocesso.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.json.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import tools.jackson.databind.*;

import static org.assertj.core.api.Assertions.*;

@JsonTest
class SubprocessoJsonViewTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveIncluirRelacionamentosNaVisaoPublica() {
        Processo processo = new Processo();
        processo.setCodigo(50L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U1");

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
        assertThat(json).contains("\"processo\":{");
        assertThat(json).contains("\"unidade\":{");
        assertThat(json).contains("\"sigla\":\"U1\"");
        assertThat(json).contains("\"mapa\":{");
    }
}

package sgc.alerta.model;

import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Alerta")
class AlertaTest {

    @Test
    @DisplayName("deve expor getters sinteticos do alerta")
    void deveExporGettersSinteticosDoAlerta() {
        Processo processo = new Processo();
        processo.setCodigo(50L);
        processo.setDescricao("Processo sintetico");

        Unidade origem = new Unidade();
        origem.setSigla("ORG");

        Unidade destino = new Unidade();
        destino.setSigla("DST");

        Alerta alerta = new Alerta();
        alerta.setProcesso(processo);
        alerta.setUnidadeOrigem(origem);
        alerta.setUnidadeDestino(destino);
        alerta.setDescricao("Mensagem");

        assertThat(alerta.getCodProcessoSintetico()).isEqualTo(50L);
        assertThat(alerta.getProcessoDescricaoSintetica()).isEqualTo("Processo sintetico");
        assertThat(alerta.getOrigemSiglaSintetica()).isEqualTo("ORG");
        assertThat(alerta.getUnidadeDestinoSigla()).isEqualTo("DST");
        assertThat(alerta.getMensagemSintetica()).isEqualTo("Mensagem");
    }
}

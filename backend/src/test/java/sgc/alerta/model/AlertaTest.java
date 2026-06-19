package sgc.alerta.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;

import static org.assertj.core.api.Assertions.assertThat;

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

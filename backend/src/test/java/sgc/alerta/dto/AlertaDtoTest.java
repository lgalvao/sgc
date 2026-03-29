package sgc.alerta.dto;

import org.junit.jupiter.api.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AlertaDto")
class AlertaDtoTest {

    @Test
    @DisplayName("deve mapear alerta a partir da entidade")
    void deveMapearAlertaAPartirDaEntidade() {
        Processo processo = new Processo();
        processo.setCodigo(10L);
        processo.setDescricao("Processo X");

        Unidade origem = new Unidade();
        origem.setSigla("ORG");

        Unidade destino = new Unidade();
        destino.setSigla("DST");

        Alerta alerta = new Alerta();
        alerta.setCodigo(5L);
        alerta.setProcesso(processo);
        alerta.setUnidadeOrigem(origem);
        alerta.setUnidadeDestino(destino);
        alerta.setDescricao("Mensagem");
        alerta.setDataHora(LocalDateTime.of(2025, 1, 1, 10, 0));
        alerta.setDataHoraLeitura(LocalDateTime.of(2025, 1, 1, 11, 0));

        AlertaDto dto = AlertaDto.fromEntity(alerta);

        assertThat(dto.codigo()).isEqualTo(5L);
        assertThat(dto.codProcesso()).isEqualTo(10L);
        assertThat(dto.processo()).isEqualTo("Processo X");
        assertThat(dto.origem()).isEqualTo("ORG");
        assertThat(dto.unidadeDestino()).isEqualTo("DST");
        assertThat(dto.mensagem()).isEqualTo("Mensagem");
        assertThat(dto.dataHoraLeitura()).isEqualTo(LocalDateTime.of(2025, 1, 1, 11, 0));
    }
}

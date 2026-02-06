package sgc.alerta.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.processo.model.Processo;
import sgc.organizacao.model.Unidade;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AlertaMapper Tests")
class AlertaMapperTest {

    private final AlertaMapper mapper = new AlertaMapperImpl();

    @Test
    @DisplayName("Deve cobrir formatDataHora")
    void deveCobrirFormatDataHora() {
        assertThat(mapper.formatDataHora(null)).isEmpty();

        LocalDateTime now = LocalDateTime.of(2026, 2, 5, 10, 30, 0);
        assertThat(mapper.formatDataHora(now)).isEqualTo("05/02/2026 10:30:00");
    }

    @Test
    @DisplayName("Deve retornar null quando Alerta é nulo")
    void deveRetornarNullQuandoAlertaNulo() {
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toDto(null, LocalDateTime.now())).isNull();
    }

    @Test
    @DisplayName("Deve cobrir toDto com todos os campos preenchidos")
    void deveCobrirToDtoCompleto() {
        Processo p = Processo.builder().codigo(1L).descricao("Proc").build();
        Unidade uo = Unidade.builder().sigla("ORIG").build();
        Unidade ud = Unidade.builder().sigla("DEST").build();

        Alerta alerta = new Alerta();
        alerta.setCodigo(100L);
        alerta.setProcesso(p);
        alerta.setUnidadeOrigem(uo);
        alerta.setUnidadeDestino(ud);
        alerta.setDescricao("Mensagem");
        alerta.setDataHora(LocalDateTime.now());

        AlertaDto dto = mapper.toDto(alerta, LocalDateTime.now().plusHours(1));

        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(100L);
        assertThat(dto.getMensagem()).isEqualTo("Mensagem");
        assertThat(dto.getUnidadeOrigem()).isEqualTo("ORIG");
        assertThat(dto.getUnidadeDestino()).isEqualTo("DEST");
    }

    @Test
    @DisplayName("Deve cobrir toDto com campos nulos")
    void deveCobrirToDtoCamposNulos() {
        Alerta alerta = new Alerta();
        AlertaDto dto = mapper.toDto(alerta);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodProcesso()).isNull();
        assertThat(dto.getUnidadeOrigem()).isNull();
        assertThat(dto.getUnidadeDestino()).isNull();
        assertThat(dto.getProcesso()).isNull();
    }
}

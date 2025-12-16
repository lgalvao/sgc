package sgc.comum.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatadorDataTest {
    @Test
    void deveFormatarDataCorretamente() {
        LocalDateTime data = LocalDateTime.of(2025, 12, 7, 14, 30);
        assertEquals("07/12/2025", FormatadorData.formatarData(data));
    }

    @Test
    void deveFormatarDataHoraCorretamente() {
        LocalDateTime data = LocalDateTime.of(2025, 12, 7, 14, 30);
        assertEquals("07/12/2025 14:30", FormatadorData.formatarDataHora(data));
    }

    @Test
    void deveRetornarHifenQuandoDataNullEmFormatarData() {
        assertEquals("-", FormatadorData.formatarData(null));
    }

    @Test
    void deveRetornarHifenQuandoDataNullEmFormatarDataHora() {
        assertEquals("-", FormatadorData.formatarDataHora(null));
    }

    @Test
    void deveFormatarDataComZerosPadding() {
        LocalDateTime data = LocalDateTime.of(2025, 1, 5, 9, 5);
        assertEquals("05/01/2025", FormatadorData.formatarData(data));
        assertEquals("05/01/2025 09:05", FormatadorData.formatarDataHora(data));
    }
}

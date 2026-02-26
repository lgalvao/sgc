package sgc.comum.util;

import org.junit.jupiter.api.*;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Utilitário: FormatadorData")
class FormatadorDataTest {
    @Test
    @DisplayName("Deve formatar data corretamente")
    void deveFormatarDataCorretamente() {
        LocalDateTime data = LocalDateTime.of(2025, 12, 7, 14, 30);
        assertEquals("07/12/2025", FormatadorData.formatarData(data));
    }

    @Test
    @DisplayName("Deve formatar data e hora corretamente")
    void deveFormatarDataHoraCorretamente() {
        LocalDateTime data = LocalDateTime.of(2025, 12, 7, 14, 30);
        assertEquals("07/12/2025 14:30", FormatadorData.formatarDataHora(data));
    }

    @Test
    @DisplayName("Deve retornar hífen quando data for null em formatarData")
    void deveRetornarHifenQuandoDataNullEmFormatarData() {
        assertEquals("-", FormatadorData.formatarData(null));
    }

    @Test
    @DisplayName("Deve retornar hífen quando data for null em formatarDataHora")
    void deveRetornarHifenQuandoDataNullEmFormatarDataHora() {
        assertEquals("-", FormatadorData.formatarDataHora(null));
    }

    @Test
    @DisplayName("Deve formatar data com zeros à esquerda (padding)")
    void deveFormatarDataComZerosPadding() {
        LocalDateTime data = LocalDateTime.of(2025, 1, 5, 9, 5);
        assertEquals("05/01/2025", FormatadorData.formatarData(data));
        assertEquals("05/01/2025 09:05", FormatadorData.formatarDataHora(data));
    }
}

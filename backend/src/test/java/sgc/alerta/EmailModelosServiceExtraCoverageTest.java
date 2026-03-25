package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailModelosService Extra Coverage Test")
class EmailModelosServiceExtraCoverageTest {

    @InjectMocks
    private EmailModelosService service;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Test
    @DisplayName("criarEmailInicioProcessoConsolidado")
    void criarEmailInicioProcessoConsolidado() {
        when(templateEngine.process(eq("email-inicio-processo-consolidado"), any(Context.class))).thenReturn("html");

        String res1 = service.criarEmailInicioProcessoConsolidado("U1", "P1", LocalDateTime.now(), true, List.of("U2"));
        String res2 = service.criarEmailInicioProcessoConsolidado("U1", "P1", LocalDateTime.now(), false, List.of());

        assertThat(res1).isEqualTo("html");
        assertThat(res2).isEqualTo("html");
    }

    @Test
    @DisplayName("criarEmailProcessoFinalizadoPorUnidade")
    void criarEmailProcessoFinalizadoPorUnidade() {
        when(templateEngine.process(eq("processo-finalizado-por-unidade"), any(Context.class))).thenReturn("html");

        String res = service.criarEmailProcessoFinalizadoPorUnidade("U1", "P1");

        assertThat(res).isEqualTo("html");
    }

    @Test
    @DisplayName("criarEmailProcessoFinalizadoUnidadesSubordinadas")
    void criarEmailProcessoFinalizadoUnidadesSubordinadas() {
        when(templateEngine.process(eq("processo-finalizado-unidades-subordinadas"), any(Context.class))).thenReturn("html");

        String res = service.criarEmailProcessoFinalizadoUnidadesSubordinadas("U1", "P1", List.of("U2"));

        assertThat(res).isEqualTo("html");
    }

    @Test
    @DisplayName("criarEmailLembretePrazo")
    void criarEmailLembretePrazo() {
        when(templateEngine.process(eq("lembrete-prazo"), any(Context.class))).thenReturn("html");

        String res = service.criarEmailLembretePrazo("U1", "P1", LocalDateTime.now());

        assertThat(res).isEqualTo("html");
    }
}

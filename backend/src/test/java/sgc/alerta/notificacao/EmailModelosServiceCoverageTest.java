package sgc.alerta.notificacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.alerta.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailModelosService - Cobertura adicional")
class EmailModelosServiceCoverageTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailModelosService emailModelosService;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    @Captor
    private ArgumentCaptor<String> templateNameCaptor;

    @BeforeEach
    void setUp() {
        when(templateEngine.process(templateNameCaptor.capture(), contextCaptor.capture()))
                .thenReturn("<html></html>");
    }

    @Test
    @DisplayName("criarEmailInicioProcessoConsolidado com participante")
    void deveCriarEmailInicioProcessoConsolidadoParticipante() {
        LocalDateTime data = LocalDateTime.of(2023, 10, 15, 12, 0);
        List<String> subordinadas = List.of("SUB1");

        emailModelosService.criarEmailInicioProcessoConsolidado("UN", "Proc1", data, true, subordinadas);

        assertThat(templateNameCaptor.getValue()).isEqualTo("email-inicio-processo-consolidado");
        Context ctx = contextCaptor.getValue();
        assertThat(ctx.getVariable("titulo")).isEqualTo("SGC: Início de processo de mapeamento de competências");
        assertThat(ctx.getVariable("siglaUnidade")).isEqualTo("UN");
        assertThat(ctx.getVariable("nomeProcesso")).isEqualTo("Proc1");
        assertThat(ctx.getVariable("dataLimite")).isEqualTo("15/10/2023");
        assertThat(ctx.getVariable("isParticipante")).isEqualTo(true);
        assertThat(ctx.getVariable("siglasSubordinadas")).isEqualTo(subordinadas);
        assertThat(ctx.getVariable("hasSubordinadas")).isEqualTo(true);
    }

    @Test
    @DisplayName("criarEmailInicioProcessoConsolidado sem participante")
    void deveCriarEmailInicioProcessoConsolidadoNaoParticipante() {
        LocalDateTime data = LocalDateTime.of(2023, 10, 15, 12, 0);
        List<String> subordinadas = Collections.emptyList();

        emailModelosService.criarEmailInicioProcessoConsolidado("UN", "Proc1", data, false, subordinadas);

        assertThat(templateNameCaptor.getValue()).isEqualTo("email-inicio-processo-consolidado");
        Context ctx = contextCaptor.getValue();
        assertThat(ctx.getVariable("titulo")).isEqualTo("SGC: Início de processo de mapeamento de competências em unidades subordinadas");
        assertThat(ctx.getVariable("hasSubordinadas")).isEqualTo(false);
    }

    @Test
    @DisplayName("criarEmailLembretePrazo deve criar email com template de lembrete")
    void deveCriarEmailLembretePrazo() {
        LocalDateTime data = LocalDateTime.of(2023, 10, 15, 12, 0);

        emailModelosService.criarEmailLembretePrazo("UN", "Proc1", data);

        assertThat(templateNameCaptor.getValue()).isEqualTo("lembrete-prazo");
        Context ctx = contextCaptor.getValue();
        assertThat(ctx.getVariable("titulo")).isEqualTo("SGC: Lembrete de prazo - Proc1");
        assertThat(ctx.getVariable("siglaUnidade")).isEqualTo("UN");
        assertThat(ctx.getVariable("nomeProcesso")).isEqualTo("Proc1");
        assertThat(ctx.getVariable("dataLimite")).isEqualTo("15/10/2023");
    }
}

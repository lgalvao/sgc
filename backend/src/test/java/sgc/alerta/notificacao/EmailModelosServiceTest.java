package sgc.alerta.notificacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.alerta.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailModelosService")
class EmailModelosServiceTest {

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

    @Nested
    @DisplayName("Geração de E-mails de Finalização")
    class Finalizacao {
        @Test
        @DisplayName("Deve criar email de processo finalizado por unidade com os dados corretos")
        void criarEmailProcessoFinalizadoPorUnidade() {

            String siglaUnidade = "UT";
            String nomeProcesso = "Processo teste";

            emailModelosService.criarEmailProcessoFinalizadoPorUnidade(
                    siglaUnidade, nomeProcesso);

            assertEquals("processo-finalizado-por-unidade", templateNameCaptor.getValue());
            Context context = contextCaptor.getValue();
            assertEquals("Conclusão do processo " + nomeProcesso, context.getVariable("titulo"));
            assertEquals(siglaUnidade, context.getVariable("siglaUnidade"));
            assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
        }

        @Test
        @DisplayName("Deve criar email de processo finalizado para unidades subordinadas")
        void criarEmailProcessoFinalizadoUnidadesSubordinadas() {

            String siglaUnidade = "UT";
            String nomeProcesso = "Processo teste";
            List<String> siglas = List.of("SUB1", "SUB2");

            emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(
                    siglaUnidade, nomeProcesso, siglas);

            assertEquals("processo-finalizado-unidades-subordinadas", templateNameCaptor.getValue());
            Context context = contextCaptor.getValue();
            assertEquals(
                    "Conclusão do processo " + nomeProcesso + " em unidades subordinadas",
                    context.getVariable("titulo"));
            assertEquals(siglaUnidade, context.getVariable("siglaUnidade"));
            assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
            assertEquals(siglas, context.getVariable("siglasUnidadesSubordinadas"));
        }
    }
}

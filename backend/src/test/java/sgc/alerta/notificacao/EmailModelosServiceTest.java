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

    @Test
    @DisplayName("Deve criar email de atribuição temporária com os dados corretos")
    void criarEmailAtribuicaoTemporaria() {
        EmailModelosService.EmailAtribuicaoTemporariaCommand command =
                new EmailModelosService.EmailAtribuicaoTemporariaCommand(
                        "SGC: Atribuição de perfil CHEFE na unidade UNIT",
                        "Usuario Teste",
                        "UNIT",
                        LocalDateTime.of(2026, 4, 21, 0, 0),
                        LocalDateTime.of(2026, 5, 21, 23, 59, 59),
                        "Férias do titular",
                        "http://localhost:5173"
                );

        emailModelosService.criarEmailAtribuicaoTemporaria(command);

        assertEquals("atribuicao-temporaria", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals(command.assunto(), context.getVariable("titulo"));
        assertEquals(command.nomeServidor(), context.getVariable("nomeServidor"));
        assertEquals(command.siglaUnidade(), context.getVariable("siglaUnidade"));
        assertEquals("21/04/2026", context.getVariable("dataInicio"));
        assertEquals("21/05/2026", context.getVariable("dataTermino"));
        assertEquals(command.justificativa(), context.getVariable("justificativa"));
        assertEquals(command.urlSistema(), context.getVariable("urlSistema"));
    }

    @Nested
    @DisplayName("Geração de E-mails de Início")
    class Inicio {
        @Test
        @DisplayName("Deve criar email de início consolidado com os dados corretos")
        void criarEmailInicioProcessoConsolidado() {
            LocalDateTime dataLimite = LocalDateTime.of(2026, 4, 30, 0, 0);
            List<String> siglasSubordinadas = List.of("SUB1", "SUB2");

            emailModelosService.criarEmailInicioProcessoConsolidado(
                    "UT",
                    "Processo teste",
                    dataLimite,
                    "MAPEAMENTO",
                    false,
                    siglasSubordinadas
            );

            assertEquals("email-inicio-processo-consolidado", templateNameCaptor.getValue());
            Context context = contextCaptor.getValue();
            assertEquals("SGC: Início de processo de mapeamento de competências em unidades subordinadas",
                    context.getVariable("titulo"));
            assertEquals("UT", context.getVariable("siglaUnidade"));
            assertEquals("Processo teste", context.getVariable("nomeProcesso"));
            assertEquals("30/04/2026", context.getVariable("dataLimite"));
            assertEquals("MAPEAMENTO", context.getVariable("tipoProcesso"));
            assertEquals(false, context.getVariable("isParticipante"));
            assertEquals(siglasSubordinadas, context.getVariable("siglasSubordinadas"));
            assertEquals(true, context.getVariable("hasSubordinadas"));
        }
    }

    @Test
    @DisplayName("Deve criar email de lembrete de prazo")
    void criarEmailLembretePrazo() {
        String res = emailModelosService.criarEmailLembretePrazo("U1", "P1", LocalDateTime.now());

        assertEquals("lembrete-prazo", templateNameCaptor.getValue());
        assertNotNull(res);
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
            assertEquals("Finalização do processo " + nomeProcesso, context.getVariable("titulo"));
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
                    "Finalização do processo " + nomeProcesso + " em unidades subordinadas",
                    context.getVariable("titulo"));
            assertEquals(siglaUnidade, context.getVariable("siglaUnidade"));
            assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
            assertEquals(siglas, context.getVariable("siglasUnidadesSubordinadas"));
        }
    }
}

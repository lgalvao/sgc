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

        assertThat(templateNameCaptor.getValue()).isEqualTo("atribuicao-temporaria");
        Context context = contextCaptor.getValue();
        assertThat(context.getVariable("titulo")).isEqualTo(command.assunto());
        assertThat(context.getVariable("nomeServidor")).isEqualTo(command.nomeServidor());
        assertThat(context.getVariable("siglaUnidade")).isEqualTo(command.siglaUnidade());
        assertThat(context.getVariable("dataInicio")).isEqualTo("21/04/2026");
        assertThat(context.getVariable("dataTermino")).isEqualTo("21/05/2026");
        assertThat(context.getVariable("justificativa")).isEqualTo(command.justificativa());
        assertThat(context.getVariable("urlSistema")).isEqualTo(command.urlSistema());
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

            assertThat(templateNameCaptor.getValue()).isEqualTo("email-inicio-processo-consolidado");
            Context context = contextCaptor.getValue();
            assertThat(context.getVariable("titulo"))
                    .isEqualTo("SGC: Início de processo de mapeamento de competências em unidades subordinadas");
            assertThat(context.getVariable("siglaUnidade")).isEqualTo("UT");
            assertThat(context.getVariable("nomeProcesso")).isEqualTo("Processo teste");
            assertThat(context.getVariable("dataLimite")).isEqualTo("30/04/2026");
            assertThat(context.getVariable("tipoProcesso")).isEqualTo("MAPEAMENTO");
            assertThat(context.getVariable("isParticipante")).isEqualTo(false);
            assertThat(context.getVariable("siglasSubordinadas")).isEqualTo(siglasSubordinadas);
            assertThat(context.getVariable("hasSubordinadas")).isEqualTo(true);
        }

        @Test
        @DisplayName("Deve criar email de início com tipo REVISAO e participante verdadeiro")
        void criarEmailInicioProcessoRevisaoEParticipante() {
            LocalDateTime dataLimite = LocalDateTime.of(2026, 4, 30, 0, 0);
            emailModelosService.criarEmailInicioProcessoConsolidado(
                    "UT",
                    "Processo teste",
                    dataLimite,
                    "REVISAO",
                    true,
                    List.of()
            );

            Context context = contextCaptor.getValue();
            assertThat(context.getVariable("titulo"))
                    .isEqualTo("SGC: Início de processo de revisão do mapa de competências");
        }

        @Test
        @DisplayName("Deve criar email de início com tipo DIAGNOSTICO e participante falso")
        void criarEmailInicioProcessoDiagnosticoEParticipanteFalso() {
            LocalDateTime dataLimite = LocalDateTime.of(2026, 4, 30, 0, 0);
            emailModelosService.criarEmailInicioProcessoConsolidado(
                    "UT",
                    "Processo teste",
                    dataLimite,
                    "DIAGNOSTICO",
                    false,
                    List.of()
            );

            Context context = contextCaptor.getValue();
            assertThat(context.getVariable("titulo"))
                    .isEqualTo("SGC: Início de processo de diagnóstico em unidades subordinadas");
        }

        @Test
        @DisplayName("Deve criar email de início com tipo default inexistente convertendo para minúsculas")
        void criarEmailInicioProcessoDefaultInexistente() {
            LocalDateTime dataLimite = LocalDateTime.of(2026, 4, 30, 0, 0);
            emailModelosService.criarEmailInicioProcessoConsolidado(
                    "UT",
                    "Processo teste",
                    dataLimite,
                    "COMPETENCIAS",
                    true,
                    List.of()
            );

            Context context = contextCaptor.getValue();
            assertThat(context.getVariable("titulo"))
                    .isEqualTo("SGC: Início de processo de competencias");
        }
    }

    @Test
    @DisplayName("Deve criar email de lembrete de prazo")
    void criarEmailLembretePrazo() {
        String res = emailModelosService.criarEmailLembretePrazo("U1", "P1", LocalDateTime.now());

        assertThat(templateNameCaptor.getValue()).isEqualTo("lembrete-prazo");
        assertThat(res).isNotNull();
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

            assertThat(templateNameCaptor.getValue()).isEqualTo("processo-finalizado-por-unidade");
            Context context = contextCaptor.getValue();
            assertThat(context.getVariable("titulo")).isEqualTo("SGC: Finalização do processo " + nomeProcesso);
            assertThat(context.getVariable("siglaUnidade")).isEqualTo(siglaUnidade);
            assertThat(context.getVariable("nomeProcesso")).isEqualTo(nomeProcesso);
        }

        @Test
        @DisplayName("Deve criar email de processo finalizado para unidades subordinadas")
        void criarEmailProcessoFinalizadoUnidadesSubordinadas() {

            String siglaUnidade = "UT";
            String nomeProcesso = "Processo teste";
            List<String> siglas = List.of("SUB1", "SUB2");

            emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(
                    siglaUnidade, nomeProcesso, siglas);

            assertThat(templateNameCaptor.getValue()).isEqualTo("processo-finalizado-unidades-subordinadas");
            Context context = contextCaptor.getValue();
            assertThat(context.getVariable("titulo"))
                    .isEqualTo("SGC: Finalização do processo " + nomeProcesso + " em unidades subordinadas");
            assertThat(context.getVariable("siglaUnidade")).isEqualTo(siglaUnidade);
            assertThat(context.getVariable("nomeProcesso")).isEqualTo(nomeProcesso);
            assertThat(context.getVariable("siglasUnidadesSubordinadas")).isEqualTo(siglas);
        }
    }
}

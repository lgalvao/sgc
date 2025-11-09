package sgc.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoModelosServiceTest {
    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private NotificacaoModelosService notificacaoModelosService;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    @Captor
    private ArgumentCaptor<String> templateNameCaptor;

    @BeforeEach
    void setUp() {
        when(templateEngine.process(templateNameCaptor.capture(), contextCaptor.capture())).thenReturn("<html></html>");
    }

    @Test
    @DisplayName("Deve criar email de processo iniciado com os dados corretos")
    void criarEmailDeProcessoIniciado() {
        String nomeUnidade = "Unidade Teste";
        String nomeProcesso = "Processo Teste";
        String tipoProcesso = "REVISAO";
        LocalDateTime dataLimite = LocalDateTime.now();

        notificacaoModelosService.criarEmailDeProcessoIniciado(nomeUnidade, nomeProcesso, tipoProcesso, dataLimite);

        assertEquals("processo-iniciado", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("Processo Iniciado - " + tipoProcesso, context.getVariable("titulo"));
        assertEquals(nomeUnidade, context.getVariable("nomeUnidade"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
        assertEquals(tipoProcesso, context.getVariable("tipoProcesso"));
        assertEquals(dataLimite.format(FORMATADOR), context.getVariable("dataLimite"));
    }

    @Test
    @DisplayName("Deve criar email de cadastro disponibilizado com os dados corretos")
    void criarEmailCadastroDisponibilizado() {
        String nomeUnidade = "Unidade Teste";
        String nomeProcesso = "Processo Teste";
        int quantidadeAtividades = 10;

        notificacaoModelosService.criarEmailCadastroDisponibilizado(nomeUnidade, nomeProcesso, quantidadeAtividades);

        assertEquals("cadastro-disponibilizado", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("Cadastro Disponibilizado para Análise", context.getVariable("titulo"));
        assertEquals(nomeUnidade, context.getVariable("nomeUnidade"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
        assertEquals(quantidadeAtividades, context.getVariable("quantidadeAtividades"));
    }

    @Test
    @DisplayName("Deve criar email de cadastro devolvido com os dados corretos")
    void criarEmailCadastroDevolvido() {
        String nomeUnidade = "Unidade Teste";
        String nomeProcesso = "Processo Teste";
        String motivo = "Motivo Teste";
        String observacoes = "Observações Teste";

        notificacaoModelosService.criarEmailCadastroDevolvido(nomeUnidade, nomeProcesso, motivo, observacoes);

        assertEquals("cadastro-devolvido", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("Cadastro Devolvido para Ajustes", context.getVariable("titulo"));
        assertEquals(nomeUnidade, context.getVariable("nomeUnidade"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
        assertEquals(motivo, context.getVariable("motivo"));
        assertEquals(observacoes, context.getVariable("observacoes"));
    }

    @Test
    @DisplayName("Deve criar email de mapa disponibilizado com os dados corretos")
    void criarEmailMapaDisponibilizado() {
        String nomeUnidade = "Unidade Teste";
        String nomeProcesso = "Processo Teste";
        LocalDateTime dataLimite = LocalDateTime.now();

        notificacaoModelosService.criarEmailMapaDisponibilizado(nomeUnidade, nomeProcesso, dataLimite);

        assertEquals("mapa-disponibilizado", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("Mapa de Competências Disponibilizado", context.getVariable("titulo"));
        assertEquals(nomeUnidade, context.getVariable("nomeUnidade"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
        assertEquals(dataLimite.format(FORMATADOR), context.getVariable("dataLimiteValidacao"));
    }

    @Test
    @DisplayName("Deve criar email de mapa validado com os dados corretos")
    void criarEmailMapaValidado() {
        String nomeUnidade = "Unidade Teste";
        String nomeProcesso = "Processo Teste";

        notificacaoModelosService.criarEmailMapaValidado(nomeUnidade, nomeProcesso);

        assertEquals("mapa-validado", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("Mapa de Competências Validado", context.getVariable("titulo"));
        assertEquals(nomeUnidade, context.getVariable("nomeUnidade"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
    }

    @Test
    @DisplayName("Deve criar email de processo finalizado com os dados corretos")
    void criarEmailProcessoFinalizado() {
        String nomeProcesso = "Processo Teste";
        LocalDateTime dataFinalizacao = LocalDateTime.now();
        int quantidadeMapas = 5;

        notificacaoModelosService.criarEmailProcessoFinalizado(nomeProcesso, dataFinalizacao, quantidadeMapas);

        assertEquals("processo-finalizado", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("Processo Finalizado - Mapas Vigentes", context.getVariable("titulo"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
        assertEquals(dataFinalizacao.format(FORMATADOR), context.getVariable("dataFinalizacao"));
        assertEquals(quantidadeMapas, context.getVariable("quantidadeMapas"));
    }

    @Test
    @DisplayName("Deve criar email de processo finalizado por unidade com os dados corretos")
    void criarEmailProcessoFinalizadoPorUnidade() {
        String siglaUnidade = "UT";
        String nomeProcesso = "Processo Teste";

        notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(siglaUnidade, nomeProcesso);

        assertEquals("processo-finalizado-por-unidade", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("SGC: Conclusão do processo " + nomeProcesso, context.getVariable("titulo"));
        assertEquals(siglaUnidade, context.getVariable("siglaUnidade"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
    }

    @Test
    @DisplayName("Deve criar email de processo finalizado para unidades subordinadas com os dados corretos")
    void criarEmailProcessoFinalizadoUnidadesSubordinadas() {
        String siglaUnidade = "UT";
        String nomeProcesso = "Processo Teste";
        List<String> siglas = List.of("SUB1", "SUB2");

        notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(siglaUnidade, nomeProcesso, siglas);

        assertEquals("processo-finalizado-unidades-subordinadas", templateNameCaptor.getValue());
        Context context = contextCaptor.getValue();
        assertEquals("SGC: Conclusão do processo " + nomeProcesso + " em unidades subordinadas", context.getVariable("titulo"));
        assertEquals(siglaUnidade, context.getVariable("siglaUnidade"));
        assertEquals(nomeProcesso, context.getVariable("nomeProcesso"));
        assertEquals(siglas, context.getVariable("siglasUnidadesSubordinadas"));
    }
}
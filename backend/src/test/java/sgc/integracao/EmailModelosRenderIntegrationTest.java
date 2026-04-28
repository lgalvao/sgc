package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.alerta.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@DisplayName("Integração: Renderização de modelos de e-mail")
class EmailModelosRenderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EmailModelosService emailModelosService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Test
    @DisplayName("Deve renderizar início de processo de mapeamento para unidade participante")
    void deveRenderizarInicioProcessoMapeamentoParticipante() {
        String html = emailModelosService.criarEmailInicioProcessoConsolidado(
                "SESEL",
                "Mapeamento 2026",
                LocalDateTime.of(2026, 4, 30, 0, 0),
                "MAPEAMENTO",
                true,
                List.of()
        );

        assertThat(html)
                .contains("SGC: Início de processo de mapeamento de competências")
                .contains("Prezado(a) responsável pela <strong>SESEL</strong>")
                .contains("Comunicamos o início do processo <strong>Mapeamento 2026</strong> para a sua unidade.")
                .contains("cadastro de atividades e conhecimentos")
                .contains("30/04/2026");
    }

    @Test
    @DisplayName("Deve renderizar início de processo de revisão para unidades subordinadas")
    void deveRenderizarInicioProcessoRevisaoConsolidado() {
        String html = emailModelosService.criarEmailInicioProcessoConsolidado(
                "COSIS",
                "Revisão 2026",
                LocalDateTime.of(2026, 5, 15, 0, 0),
                "REVISAO",
                false,
                List.of("SESEL", "STIC")
        );

        assertThat(html)
                .contains("SGC: Início de processo de revisão do mapa de competências em unidades subordinadas")
                .contains("Comunicamos o início do processo <strong>Revisão 2026</strong> nas unidades")
                .contains("SESEL, STIC")
                .contains("iniciar a revisão do cadastro de atividades e conhecimentos")
                .contains("Acompanhe o processo no Sistema de Gestão de Competências")
                .contains("15/05/2026");
    }

    @Test
    @DisplayName("Deve renderizar finalização de processo para unidade participante")
    void deveRenderizarFinalizacaoDireta() {
        String html = emailModelosService.criarEmailProcessoFinalizadoPorUnidade("SESEL", "Processo final 2026");

        assertThat(html)
                .contains("Finalização do processo Processo final 2026")
                .contains("Comunicamos a finalização do processo <strong>Processo final 2026</strong> para a sua unidade.")
                .contains("menu \"Minha unidade\"")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar lembrete de prazo conforme CDU")
    void deveRenderizarLembreteDePrazo() {
        String html = emailModelosService.criarEmailLembretePrazo(
                "SESEL",
                "Processo prazo 2026",
                LocalDateTime.of(2026, 6, 10, 0, 0)
        );

        assertThat(html)
                .contains("SGC: Lembrete de prazo - Processo prazo 2026")
                .contains("Prezado(a) responsável pela <strong>SESEL</strong>")
                .contains("Processo prazo 2026")
                .contains("10/06/2026")
                .contains("Acesse o sistema para concluir essas pendências")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar atribuição temporária conforme CDU")
    void deveRenderizarAtribuicaoTemporaria() {
        String html = emailModelosService.criarEmailAtribuicaoTemporaria(
                new EmailModelosService.EmailAtribuicaoTemporariaCommand(
                        "SGC: Atribuição de perfil CHEFE na unidade STIC",
                        "Maria da Silva",
                        "STIC",
                        LocalDateTime.of(2026, 4, 21, 0, 0),
                        LocalDateTime.of(2026, 5, 21, 0, 0),
                        "Férias do titular",
                        "https://sgc.tre-pe.jus.br"
                )
        );

        assertThat(html)
                .contains("SGC: Atribuição de perfil CHEFE na unidade STIC")
                .contains("Prezado(a) Maria da Silva,")
                .contains("Foi registrada uma atribuição temporária de perfil de CHEFE para você na unidade")
                .contains("21/04/2026")
                .contains("21/05/2026")
                .contains("Férias do titular")
                .contains("escolha o perfil <strong>CHEFE</strong>")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar cadastro disponibilizado conforme CDU-09")
    void deveRenderizarCadastroDisponibilizado() {
        Context context = new Context();
        context.setVariable("siglaUnidade", "SESEL");
        context.setVariable("siglaUnidadeDestino", "COSIS");
        context.setVariable("nomeProcesso", "Processo cadastro 2026");

        String html = templateEngine.process("cadastro-disponibilizado", context);

        assertThat(html)
                .contains("Cadastro de atividades e conhecimentos disponibilizado")
                .contains("Prezado(a) responsável pela <strong>COSIS</strong>")
                .contains("A unidade <strong>SESEL</strong> disponibilizou o cadastro de atividades e")
                .contains("conhecimentos do processo")
                .contains("Processo cadastro 2026")
                .contains("A análise desse cadastro já pode ser realizada no Sistema de Gestão de Competências")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar cadastro disponibilizado para unidade superior conforme CDU-09")
    void deveRenderizarCadastroDisponibilizadoSuperior() {
        Context context = new Context();
        context.setVariable("siglaUnidade", "SESEL");
        context.setVariable("siglaUnidadeSuperior", "STIC");
        context.setVariable("nomeProcesso", "Processo cadastro 2026");

        String html = templateEngine.process("cadastro-disponibilizado-superior", context);

        assertThat(html)
                .contains("Cadastro disponibilizado em unidade subordinada")
                .contains("Prezado(a) responsável pela <strong>STIC</strong>")
                .contains("cadastro de atividades da unidade <strong>SESEL</strong>")
                .contains("Processo cadastro 2026")
                .contains("A análise desse cadastro já pode ser realizada no Sistema de Gestão de Competências")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar cadastro devolvido conforme CDU-13")
    void deveRenderizarCadastroDevolvido() {
        Context context = new Context();
        context.setVariable("siglaUnidade", "SESEL");
        context.setVariable("siglaUnidadeDestino", "SESEL");
        context.setVariable("nomeProcesso", "Processo devolucao 2026");
        context.setVariable("observacoes", "Favor revisar as atividades cadastradas.");

        String html = templateEngine.process("cadastro-devolvido", context);

        assertThat(html)
                .contains("Cadastro de atividades e conhecimentos devolvido para ajustes")
                .contains("Prezado(a) responsável pela <strong>SESEL</strong>")
                .contains("O cadastro de atividades e conhecimentos da <strong>SESEL</strong> no processo")
                .contains("Processo devolucao 2026")
                .contains("Observações da análise")
                .contains("Favor revisar as atividades cadastradas.")
                .contains("Acompanhe o processo no Sistema de Gestão de Competências")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar cadastro aceito conforme CDU-13")
    void deveRenderizarCadastroAceito() {
        Context context = new Context();
        context.setVariable("siglaUnidadeOrigem", "SESEL");
        context.setVariable("siglaUnidadeDestino", "COSIS");
        context.setVariable("nomeProcesso", "Processo aceite 2026");

        String html = templateEngine.process("aceite-cadastro", context);

        assertThat(html)
                .contains("Cadastro de atividades e conhecimentos submetido para análise")
                .contains("Prezado(a) responsável pela <strong>COSIS</strong>")
                .contains("O cadastro de atividades e conhecimentos da")
                .contains("<strong>SESEL</strong>")
                .contains("Processo aceite 2026")
                .contains("foi submetido para análise por essa unidade")
                .contains("A análise já pode ser realizada no Sistema de Gestão de Competências")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar revisão de cadastro disponibilizada conforme CDU-10")
    void deveRenderizarDisponibilizacaoRevisaoCadastro() {
        Context context = new Context();
        context.setVariable("siglaUnidadeOrigem", "SESEL");
        context.setVariable("siglaUnidadeDestino", "COSIS");
        context.setVariable("nomeProcesso", "Processo revisao 2026");

        String html = templateEngine.process("disponibilizacao-revisao-cadastro", context);

        assertThat(html)
                .contains("Revisão do cadastro de atividades e conhecimentos disponibilizada")
                .contains("Prezado(a) responsável pela <strong>COSIS</strong>")
                .contains("A unidade <strong>SESEL</strong> concluiu a revisão")
                .contains("Processo revisao 2026")
                .contains("A análise desse cadastro já pode ser realizada no Sistema de Gestão de Competências")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar revisão de cadastro devolvida conforme CDU-14")
    void deveRenderizarDevolucaoRevisaoCadastro() {
        Context context = new Context();
        context.setVariable("siglaUnidadeOrigem", "SESEL");
        context.setVariable("siglaUnidadeDestino", "SESEL");
        context.setVariable("nomeProcesso", "Processo revisao 2026");
        context.setVariable("observacoes", "Favor ajustar o cadastro revisado.");

        String html = templateEngine.process("devolucao-revisao-cadastro", context);

        assertThat(html)
                .contains("Revisão do cadastro de atividades e conhecimentos devolvida para ajustes")
                .contains("Prezado(a) responsável pela <strong>SESEL</strong>")
                .contains("A revisão do cadastro de atividades e conhecimentos da")
                .contains("Processo revisao 2026")
                .contains("Favor ajustar o cadastro revisado.")
                .contains("Acompanhe o processo no Sistema de Gestão de Competências")
                .contains("https://sgc.tre-pe.jus.br");
    }

    @Test
    @DisplayName("Deve renderizar aceite de revisão de cadastro conforme CDU-14")
    void deveRenderizarAceiteRevisaoCadastro() {
        Context context = new Context();
        context.setVariable("siglaUnidadeOrigem", "SESEL");
        context.setVariable("siglaUnidadeDestino", "COSIS");
        context.setVariable("nomeProcesso", "Processo revisao 2026");

        String html = templateEngine.process("aceite-revisao-cadastro", context);

        assertThat(html)
                .contains("Revisão do cadastro de atividades e conhecimentos submetida para análise")
                .contains("Prezado(a) responsável pela <strong>COSIS</strong>")
                .contains("A revisão do cadastro de atividades e conhecimentos da")
                .contains("Processo revisao 2026")
                .contains("foi submetida para análise por essa unidade")
                .contains("A análise já pode ser realizada no Sistema de Gestão de Competências")
                .contains("https://sgc.tre-pe.jus.br");
    }
}

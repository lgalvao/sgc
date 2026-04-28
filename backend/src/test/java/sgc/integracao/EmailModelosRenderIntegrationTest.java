package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import sgc.alerta.*;
import sgc.integracao.mocks.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@DisplayName("Integração: Renderização de modelos de e-mail")
class EmailModelosRenderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EmailModelosService emailModelosService;

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
}

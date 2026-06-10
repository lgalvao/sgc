package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.NotificacaoEmailRepo;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.integracao.mocks.WithMockCustomUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-45: Aprovar avaliação de consenso")
class CDU45IntegrationTest extends DiagnosticoCduIntegrationTestBase {

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherAutoavaliacao("50003", 5, 3, 4, 2, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        preencherConsenso("50003", 6, 4, 6, 4, 5, 3, 5, 3, SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
    }

    @Test
    @WithMockCustomUser(tituloEleitoral = "50003", unidadeId = 9L, perfis = {"SERVIDOR"})
    @DisplayName("Deve aprovar a avaliação de consenso do próprio servidor")
    void deveAprovarConsenso() throws Exception {
        mockMvc.perform(post("/api/diagnosticos/subprocessos/{codSubprocesso}/consenso/aprovar", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).hasSize(2);
        assertThat(avaliacoes).allSatisfy(avaliacao ->
                assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_APROVADO));

        // Validar que o alerta de transicao foi criado conforme especificado no CDU-45
        List<Alerta> alertas = alertaRepo.findAll();
        assertThat(alertas).anySatisfy(alerta -> {
            assertThat(alerta.getDescricao()).isEqualTo("Avaliação de consenso aprovada: " + servidor.getNome());
            assertThat(alerta.getProcesso().getCodigo()).isEqualTo(processo.getCodigo());
            assertThat(alerta.getUnidadeOrigem().getCodigo()).isEqualTo(unidade.getCodigo());
            assertThat(alerta.getUnidadeDestino().getCodigo()).isEqualTo(unidade.getCodigo());
        });

        // Validar o enfileiramento e conteudo do e-mail de notificacao no banco de dados
        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll();
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getAssunto()).contains("Avaliação de consenso de " + servidor.getNome() + " aprovada");
            assertThat(notificacao.getDestinatario()).isEqualTo("chefe.teste@tre-pe.jus.br");
            assertThat(notificacao.getCorpoHtml()).contains("Prezado(a) responsável pela <strong>" + unidade.getSigla() + "</strong>");
            assertThat(notificacao.getCorpoHtml()).contains("O servidor <strong>" + servidor.getNome() + "</strong> aprovou a avaliação de consenso do processo");
            assertThat(notificacao.getCorpoHtml()).contains("Acompanhe o processo no Sistema de Gestão de Competências");
        });
    }
}

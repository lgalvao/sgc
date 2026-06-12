package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sgc.comum.ComumDtos;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.integracao.mocks.WithMockChefe;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-47: Indicar impossibilidade de avaliação")
class CDU47IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";


    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherAutoavaliacao("50003", 5, 3, 4, 2, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve impossibilitar a avaliação de um servidor com justificativa")
    void deveImpossibilitarAvaliacao() throws Exception {
        ComumDtos.JustificativaRequest request = new ComumDtos.JustificativaRequest("Servidor afastado");

        mockMvc.perform(post(API_DIAGNOSTICO + "/avaliacoes/{servidorTitulo}/impossibilitar",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).hasSize(2);
        assertThat(avaliacoes).allSatisfy(avaliacao -> {
            assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
            assertThat(avaliacao.getImportancia()).isNull();
            assertThat(avaliacao.getDominio()).isNull();
            assertThat(avaliacao.getGap()).isNull();
            assertThat(avaliacao.getObservacao()).isEqualTo("Servidor afastado");
        });
    }
}

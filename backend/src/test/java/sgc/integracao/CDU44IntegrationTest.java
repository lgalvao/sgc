package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import sgc.diagnostico.dto.AvaliacaoCompetenciaDto;
import sgc.diagnostico.dto.ConsensoCompetenciaDto;
import sgc.diagnostico.dto.ConsensoRequest;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.integracao.mocks.WithMockChefe;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-44: Manter avaliação de consenso")
class CDU44IntegrationTest extends DiagnosticoCduIntegrationTestBase {

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherAutoavaliacao("50003", 5, 3, 4, 2, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve salvar o consenso da chefia e disponibilizar a consulta detalhada")
    void deveSalvarConsenso() throws Exception {
        ConsensoRequest request = new ConsensoRequest(
                List.of(
                        AvaliacaoCompetenciaDto.builder().competenciaCodigo(competencia1.getCodigo()).importancia(6).dominio(4).build(),
                        AvaliacaoCompetenciaDto.builder().competenciaCodigo(competencia2.getCodigo()).importancia(5).dominio(3).build()
                ),
                List.of(
                        ConsensoCompetenciaDto.builder()
                                .competenciaCodigo(competencia1.getCodigo())
                                .autoimportancia(5)
                                .autodominio(3)
                                .chefiaImportancia(6)
                                .chefiaDominio(4)
                                .consensoImportancia(6)
                                .consensoDominio(4)
                                .build(),
                        ConsensoCompetenciaDto.builder()
                                .competenciaCodigo(competencia2.getCodigo())
                                .autoimportancia(4)
                                .autodominio(2)
                                .chefiaImportancia(5)
                                .chefiaDominio(3)
                                .consensoImportancia(5)
                                .consensoDominio(3)
                                .build()
                )
        );

        mockMvc.perform(post("/api/diagnosticos/subprocessos/{codSubprocesso}/consenso/{servidorTitulo}",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/diagnosticos/subprocessos/{codSubprocesso}/consenso/{servidorTitulo}",
                        subprocesso.getCodigo(), "50003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoServidor").value("CONSENSO_CRIADO"))
                .andExpect(jsonPath("$.competenciasDetalhadas.length()").value(2));

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).allSatisfy(avaliacao -> {
            assertThat(avaliacao.getChefiaImportancia()).isNotNull();
            assertThat(avaliacao.getChefiaDominio()).isNotNull();
            assertThat(avaliacao.getImportancia()).isNotNull();
            assertThat(avaliacao.getDominio()).isNotNull();
            assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        });
    }
}

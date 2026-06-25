package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import sgc.comum.*;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.integracao.mocks.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
            assertThat(avaliacao.getImportancia()).isNotNull();
            assertThat(avaliacao.getDominio()).isNotNull();
            assertThat(avaliacao.getGap()).isNotNull();
            assertThat(avaliacao.getObservacao()).isEqualTo("Servidor afastado");
        });
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve restaurar o consenso salvo ao reverter impossibilidade de avaliação")
    void deveRestaurarConsensoAoReverterImpossibilidade() throws Exception {
        preencherConsenso("50003", 6, 4, 6, 4, 5, 3, 4, 2, SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        buscarAvaliacoes("50003").forEach(avaliacao -> {
            avaliacao.setConsensoImportancia(avaliacao.getImportancia());
            avaliacao.setConsensoDominio(avaliacao.getDominio());
        });
        avaliacaoServidorRepo.flush();
        ComumDtos.JustificativaRequest request = new ComumDtos.JustificativaRequest("Servidor afastado");

        mockMvc.perform(post(API_DIAGNOSTICO + "/avaliacoes/{servidorTitulo}/impossibilitar",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post(API_DIAGNOSTICO + "/avaliacoes/{servidorTitulo}/reverter-impossibilidade",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf()))
                .andExpect(status().isOk());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).allSatisfy(avaliacao -> {
            assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
            assertThat(avaliacao.getConsensoImportancia()).isNotNull();
            assertThat(avaliacao.getConsensoDominio()).isNotNull();
            assertThat(avaliacao.getImportancia()).isEqualTo(avaliacao.getConsensoImportancia());
            assertThat(avaliacao.getDominio()).isEqualTo(avaliacao.getConsensoDominio());
            assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
        });
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve restaurar autoavaliação concluída ao reverter impossibilidade após rascunho manual de consenso sem concluir")
    void deveRestaurarAutoavaliacaoConcluidaAoReverterImpossibilidadeAposRascunhoManual() throws Exception {
        ConsensoRequest request = new ConsensoRequest(
                List.of(
                        ConsensoCompetenciaDto.builder()
                                .competenciaCodigo(competencia1.getCodigo())
                                .servidorImportancia(5)
                                .servidorDominio(3)
                                .chefiaImportancia(6)
                                .chefiaDominio(4)
                                .consensoImportancia(6)
                                .consensoDominio(4)
                                .build(),
                        ConsensoCompetenciaDto.builder()
                                .competenciaCodigo(competencia2.getCodigo())
                                .servidorImportancia(4)
                                .servidorDominio(2)
                                .chefiaImportancia(5)
                                .chefiaDominio(3)
                                .consensoImportancia(5)
                                .consensoDominio(3)
                                .build()
                )
        );

        mockMvc.perform(post(API_DIAGNOSTICO + "/consenso/{servidorTitulo}",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post(API_DIAGNOSTICO + "/avaliacoes/{servidorTitulo}/impossibilitar",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComumDtos.JustificativaRequest("Servidor afastado"))))
                .andExpect(status().isOk());

        mockMvc.perform(post(API_DIAGNOSTICO + "/avaliacoes/{servidorTitulo}/reverter-impossibilidade",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf()))
                .andExpect(status().isOk());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).allSatisfy(avaliacao -> {
            assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
            assertThat(avaliacao.getChefiaImportancia()).isNotNull();
            assertThat(avaliacao.getChefiaDominio()).isNotNull();
            assertThat(avaliacao.getConsensoImportancia()).isNotNull();
            assertThat(avaliacao.getConsensoDominio()).isNotNull();
            assertThat(avaliacao.getImportancia()).isEqualTo(avaliacao.getAutoimportancia());
            assertThat(avaliacao.getDominio()).isEqualTo(avaliacao.getAutodominio());
            assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
            assertThat(avaliacao.getObservacao()).isNull();
        });
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve restaurar consenso criado ao reverter impossibilidade após consenso automático concluído")
    void deveRestaurarConsensoCriadoAoReverterImpossibilidadeAposConsensoAutomaticoConcluido() throws Exception {
        ConsensoRequest request = new ConsensoRequest(
                List.of(
                        ConsensoCompetenciaDto.builder()
                                .competenciaCodigo(competencia1.getCodigo())
                                .servidorImportancia(5)
                                .servidorDominio(3)
                                .chefiaImportancia(5)
                                .chefiaDominio(3)
                                .consensoImportancia(5)
                                .consensoDominio(3)
                                .build(),
                        ConsensoCompetenciaDto.builder()
                                .competenciaCodigo(competencia2.getCodigo())
                                .servidorImportancia(4)
                                .servidorDominio(2)
                                .chefiaImportancia(4)
                                .chefiaDominio(2)
                                .consensoImportancia(4)
                                .consensoDominio(2)
                                .build()
                )
        );

        mockMvc.perform(post(API_DIAGNOSTICO + "/consenso/{servidorTitulo}",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post(API_DIAGNOSTICO + "/consenso/{servidorTitulo}/concluir",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post(API_DIAGNOSTICO + "/avaliacoes/{servidorTitulo}/impossibilitar",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComumDtos.JustificativaRequest("Servidor afastado"))))
                .andExpect(status().isOk());

        mockMvc.perform(post(API_DIAGNOSTICO + "/avaliacoes/{servidorTitulo}/reverter-impossibilidade",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf()))
                .andExpect(status().isOk());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).allSatisfy(avaliacao -> {
            assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
            assertThat(avaliacao.getConsensoImportancia()).isNotNull();
            assertThat(avaliacao.getConsensoDominio()).isNotNull();
            assertThat(avaliacao.getImportancia()).isEqualTo(avaliacao.getConsensoImportancia());
            assertThat(avaliacao.getDominio()).isEqualTo(avaliacao.getConsensoDominio());
            assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
            assertThat(avaliacao.getObservacao()).isNull();
        });
    }
}

package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.NotificacaoEmailRepo;
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
@DisplayName("CDU-45: Manter avaliação de consenso")
class CDU45IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherAutoavaliacao("50003", 5, 3, 4, 2, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve salvar o rascunho do consenso sem concluir e só concluir após validação completa")
    void deveSalvarConsenso() throws Exception {
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

        mockMvc.perform(get(API_DIAGNOSTICO + "/consenso/{servidorTitulo}",
                        subprocesso.getCodigo(), "50003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoServidor").value("AUTOAVALIACAO_CONCLUIDA"))
                .andExpect(jsonPath("$.competencias.length()").value(2))
                .andExpect(jsonPath("$.competencias[0].competenciaDescricao").isNotEmpty());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        assertThat(avaliacoes).allSatisfy(avaliacao -> {
            assertThat(avaliacao.getChefiaImportancia()).isNotNull();
            assertThat(avaliacao.getChefiaDominio()).isNotNull();
            assertThat(avaliacao.getImportancia()).isNotNull();
            assertThat(avaliacao.getDominio()).isNotNull();
            assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        });

        mockMvc.perform(post(API_DIAGNOSTICO + "/consenso/{servidorTitulo}/concluir",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(buscarAvaliacoes("50003")).allSatisfy(avaliacao ->
                assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO));
        assertThat(notificacaoEmailRepo.findAll()).anySatisfy(notificacao -> {
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Avaliação de consenso criada");
            assertThat(notificacao.getDestinatario()).isEqualTo(servidor.getEmail());
            assertThat(notificacao.getCorpoHtml()).contains("concluiu a avaliação de consenso no processo");
            assertThat(notificacao.getCorpoHtml()).contains(processo.getDescricao());
        });
        assertThat(alertaRepo.findAll()).anySatisfy(alerta -> {
            assertThat(alerta.getDescricao()).isEqualTo("Avaliação de consenso criada");
            assertThat(alerta.getProcesso().getCodigo()).isEqualTo(processo.getCodigo());
            assertThat(alerta.getUsuarioDestinoTitulo()).isEqualTo(servidor.getTituloEleitoral());
            assertThat(alerta.getUnidadeOrigem().getCodigo()).isEqualTo(unidade.getCodigo());
            assertThat(alerta.getUnidadeDestino().getCodigo()).isEqualTo(unidade.getCodigo());
        });
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve permitir salvar o consenso em etapas com subconjunto de competências")
    void deveSalvarConsensoEmEtapas() throws Exception {
        int autoimportanciaPrimeira = 5;
        int autodominioPrimeira = 3;
        int chefiaImportanciaPrimeira = 6;
        int chefiaDominioPrimeira = 4;
        int autoimportanciaSegunda = 4;
        int autodominioSegunda = 2;

        ConsensoRequest request = new ConsensoRequest(
                List.of(
                        ConsensoCompetenciaDto.builder()
                                .competenciaCodigo(competencia1.getCodigo())
                                .servidorImportancia(autoimportanciaPrimeira)
                                .servidorDominio(autodominioPrimeira)
                                .chefiaImportancia(chefiaImportanciaPrimeira)
                                .chefiaDominio(chefiaDominioPrimeira)
                                .consensoImportancia(chefiaImportanciaPrimeira)
                                .consensoDominio(chefiaDominioPrimeira)
                                .build()
                )
        );

        mockMvc.perform(post(API_DIAGNOSTICO + "/consenso/{servidorTitulo}",
                        subprocesso.getCodigo(), "50003")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<AvaliacaoServidor> avaliacoes = buscarAvaliacoes("50003");
        AvaliacaoServidor avaliacao1 = avaliacoes.stream()
                .filter(avaliacao -> avaliacao.getCompetencia().getCodigo().equals(competencia1.getCodigo()))
                .findFirst()
                .orElseThrow();
        AvaliacaoServidor avaliacao2 = avaliacoes.stream()
                .filter(avaliacao -> avaliacao.getCompetencia().getCodigo().equals(competencia2.getCodigo()))
                .findFirst()
                .orElseThrow();

        assertThat(avaliacao1.getChefiaImportancia()).isEqualTo(chefiaImportanciaPrimeira);
        assertThat(avaliacao1.getChefiaDominio()).isEqualTo(chefiaDominioPrimeira);
        assertThat(avaliacao1.getImportancia()).isEqualTo(chefiaImportanciaPrimeira);
        assertThat(avaliacao1.getDominio()).isEqualTo(chefiaDominioPrimeira);
        assertThat(avaliacao2.getChefiaImportancia()).isNull();
        assertThat(avaliacao2.getChefiaDominio()).isNull();
        assertThat(avaliacao2.getImportancia()).isEqualTo(autoimportanciaSegunda);
        assertThat(avaliacao2.getDominio()).isEqualTo(autodominioSegunda);
        assertThat(avaliacoes).allSatisfy(avaliacao ->
                assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA));
    }
}

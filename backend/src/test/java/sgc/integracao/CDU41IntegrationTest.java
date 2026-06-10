package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.DiagnosticoRepo;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.IniciarProcessoRequest;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-41: Iniciar processo de diagnóstico")
class CDU41IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DiagnosticoRepo diagnosticoRepo;

    @Test
    @WithMockAdmin
    @DisplayName("Deve iniciar processo de diagnóstico com subprocesso e estrutura inicial de avaliação")
    void deveIniciarProcessoDiagnosticoComEstruturasIniciais() throws Exception {
        CriarProcessoRequest criarRequest = CriarProcessoRequest.builder()
                .descricao("Processo diagnóstico CDU-41")
                .tipo(TipoProcesso.DIAGNOSTICO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(20))
                .unidades(List.of(10L))
                .build();

        String respostaCriacao = mockMvc.perform(post("/api/processos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long codigoProcesso = objectMapper.readTree(respostaCriacao).get("codigo").asLong();

        IniciarProcessoRequest iniciarRequest = new IniciarProcessoRequest(TipoProcesso.DIAGNOSTICO, List.of(10L));
        mockMvc.perform(post("/api/processos/{codigo}/iniciar", codigoProcesso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarRequest)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        List<Subprocesso> subprocessos = subprocessoRepo.listarPorProcessoComUnidade(codigoProcesso);
        assertThat(subprocessos).hasSize(1);
        Subprocesso subprocesso = subprocessos.getFirst();
        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        assertThat(subprocesso.getMapa()).isNotNull();

        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(subprocesso.getCodigo()).orElseThrow();
        assertThat(entityManager.createQuery(
                "select count(sp) from ServidorProcesso sp where sp.processo.codigo = :codigoProcesso and sp.unidadeCodigo = :codigoUnidade",
                Long.class
        ).setParameter("codigoProcesso", codigoProcesso)
                .setParameter("codigoUnidade", subprocesso.getUnidade().getCodigo())
                .getSingleResult()).isGreaterThan(0);
        assertThat(entityManager.createQuery(
                "select count(a) from AvaliacaoServidor a where a.diagnostico.codigo = :codigoDiagnostico",
                Long.class
        ).setParameter("codigoDiagnostico", diagnostico.getCodigo()).getSingleResult()).isGreaterThan(0);
        assertThat(entityManager.createQuery(
                "select count(o) from SituacaoCapacitacao o where o.diagnostico.codigo = :codigoDiagnostico",
                Long.class
        ).setParameter("codigoDiagnostico", diagnostico.getCodigo()).getSingleResult()).isGreaterThan(0);
    }
}

package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockGestor;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-25: Aceitar validação de mapas em bloco")
class CDU25IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;
    private Processo processo;

    @BeforeEach
    void setUp() {
        // Use existing 3-level hierarchy from data.sql:
        // Unit 2 (STIC - INTEROPERACIONAL) - top level
        // Unit 6 (COSIS - INTERMEDIARIA) - subordinate to 2, user '666666666666' is GESTOR
        // Unit 8 (SEDESENV - OPERACIONAL) - subordinate to 6
        // Unit 9 (SEDIA - OPERACIONAL) - subordinate to 6

        Unidade unidade1 = unidadeRepo.findById(8L)
                .orElseThrow(() -> new RuntimeException("Unit 8 not found in data.sql"));
        Unidade unidade2 = unidadeRepo.findById(9L)
                .orElseThrow(() -> new RuntimeException("Unit 9 not found in data.sql"));

        // Create test process
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo Validação CDU-25");
        processo = processoRepo.save(processo);

        // Create subprocesses in MAPEAMENTO_MAPA_VALIDADO state
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso1 = subprocessoRepo.save(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso2 = subprocessoRepo.save(subprocesso2);

        // Subprocessos devem estar na unidade superior (6) para o Gestor aceitar
        Unidade unidadeSuperior = unidadeRepo.findById(6L).orElseThrow();
        Movimentacao m1 = Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(unidadeSuperior)
                .descricao("Mapa validado")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(m1);

        Movimentacao m2 = Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(unidade2)
                .unidadeDestino(unidadeSuperior)
                .descricao("Mapa validado")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(m2);

        entityManager.flush();
        entityManager.clear();

        processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve aceitar validação de mapas em bloco")
    @WithMockGestor("666666666666")
        // GESTOR of unit 6 (parent of units 8 and 9)
    void aceitarValidacaoEmBloco_deveAceitarSucesso() throws Exception {
        // Given
        Long codigoContexto = processo.getCodigo();
        List<Long> subprocessosSelecionados = List.of(subprocesso1.getCodigo(), subprocesso2.getCodigo());

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .acao("ACEITAR_VALIDACAO")
                .subprocessos(subprocessosSelecionados)
                .build();

        // When
        mockMvc.perform(
                        post("/api/subprocessos/{id}/aceitar-validacao-bloco", codigoContexto)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        // Check Subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        List<Analise> analises1 = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(analises1).isNotEmpty();
        assertThat(analises1.getFirst().getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);

        List<Movimentacao> movs1 = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).contains("Validação do mapa aceita");

        // Check Subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s2.getCodigo())).isNotEmpty();
    }
}

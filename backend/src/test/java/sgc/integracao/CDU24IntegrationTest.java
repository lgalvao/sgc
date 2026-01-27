package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-24: Disponibilizar mapas de competências em bloco")
class CDU24IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoMovimentacaoRepo movimentacaoRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidade1;
    private Unidade unidade2;
    private Processo processo;
    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Use existing units from data.sql:
        // Unit 8 (SEDESENV - OPERACIONAL) subordinate to 6
        // Unit 9 (SEDIA - OPERACIONAL) subordinate to 6
        // User '111111111111' is ADMIN (can disponibilizar mapas em bloco)
        unidade1 = unidadeRepo.findById(8L)
                .orElseThrow(() -> new RuntimeException("Unit 8 not found in data.sql"));
        unidade2 = unidadeRepo.findById(9L)
                .orElseThrow(() -> new RuntimeException("Unit 9 not found in data.sql"));

        // Create test process
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo Mapa CDU-24");
        processo = processoRepo.save(processo);

        // Create subprocesses with complete maps (Status: MAPEAMENTO_MAPA_CRIADO)
        subprocesso1 = createSubprocessoComMapaCompleto(unidade1);
        subprocesso2 = createSubprocessoComMapaCompleto(unidade2);

        entityManager.flush();
        entityManager.clear();

        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }

    private Subprocesso createSubprocessoComMapaCompleto(Unidade unidade) {
        // Criar Subprocesso in correct state for disponibilizar mapa
        Subprocesso sub = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        sub.setCodigo(null);
        sub.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO); // Changed from MAPEAMENTO_MAPA_CRIADO
        sub = subprocessoRepo.save(sub);

        // Criar Mapa
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(sub);
        mapa = mapaRepo.save(mapa);

        // Associar mapa ao subprocesso
        sub.setMapa(mapa);
        sub = subprocessoRepo.save(sub);

        // Criar Atividade
        Atividade ativ = Atividade.builder().mapa(mapa).descricao("Atividade Teste " + unidade.getSigla()).build();
        atividadeRepo.save(ativ);

        // Criar Competência
        Competencia comp = Competencia.builder().descricao("Competência Teste " + unidade.getSigla()).mapa(mapa).build();
        competenciaRepo.save(comp);

        // Associar (ManyToMany)
        ativ.getCompetencias().add(comp);
        atividadeRepo.save(ativ);

        return sub;
    }

    @Test
    @DisplayName("Deve disponibilizar mapas de competências em bloco (sucesso)")
    @WithMockAdmin
    void disponibilizarMapaEmBloco_deveDisponibilizarSucesso() throws Exception {
        // Given
        Long codigoContexto = subprocesso1.getCodigo();
        List<Long> unidadesSelecionadas = List.of(unidade1.getCodigo(), unidade2.getCodigo());

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(unidadesSelecionadas)
                .acao("DISPONIBILIZAR")
                .build();

        // When
        mockMvc.perform(
                        post("/api/subprocessos/{id}/disponibilizar-mapa-bloco", codigoContexto)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        // Verificações para Subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        assertThat(s1.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        List<Movimentacao> movs1 = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).contains("Disponibilização do mapa");

        // Verificações para Subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(s2.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
    }
}

package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-24: Disponibilizar mapas de competências em bloco")
class CDU24IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

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
        sub.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO); // Changed from MAPEAMENTO_MAPA_CRIADO
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

        Long codigoContexto = processo.getCodigo();
        List<Long> unidadesSelecionadas = List.of(subprocesso1.getCodigo(), subprocesso2.getCodigo());

        // Garante que os subprocessos estejam na unidade do ADMIN (1)
        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        Movimentacao movAdmin1 = Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(movAdmin1);

        Movimentacao movAdmin2 = Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(unidade2)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(movAdmin2);

        entityManager.flush();
        entityManager.clear();

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(unidadesSelecionadas)
                .acao("DISPONIBILIZAR")
                .dataLimite(LocalDate.now().plusDays(10))
                .build();


        mockMvc.perform(
                        post("/api/subprocessos/{id}/disponibilizar-mapa-bloco", codigoContexto)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());


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

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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.*;
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
        processo.setDescricao("Processo mapa CDU-24");
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
        // Criar subprocesso in correct state for disponibilizar mapa
        Subprocesso sub = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        sub.setCodigo(null);
        sub.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO); // Changed from MAPEAMENTO_MAPA_CRIADO
        sub = subprocessoRepo.save(sub);

        // Criar mapa
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(sub);
        mapa = mapaRepo.save(mapa);

        // Associar mapa ao subprocesso
        sub.setMapa(mapa);
        sub = subprocessoRepo.save(sub);

        // Criar atividade
        Atividade ativ = Atividade.builder().mapa(mapa).descricao("Atividade teste " + unidade.getSigla()).build();
        atividadeRepo.save(ativ);

        // Criar competência
        Competencia comp = Competencia.builder().descricao("Competência teste " + unidade.getSigla()).mapa(mapa).build();
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
                .dataLimite(subprocesso1.getDataLimiteEtapa1().toLocalDate().plusDays(1))
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/disponibilizar-mapa-bloco", codigoContexto)
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

    @Test
    @DisplayName("Deve rejeitar disponibilização em bloco quando existir atividade sem competência")
    @WithMockAdmin
    void disponibilizarMapaEmBloco_deveFalharComAtividadeSemCompetencia() throws Exception {
        Atividade atividadeSemCompetencia = Atividade.builder()
                .mapa(subprocesso1.getMapa())
                .descricao("Atividade sem competência")
                .build();
        atividadeRepo.saveAndFlush(atividadeSemCompetencia);

        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .build());

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(List.of(subprocesso1.getCodigo()))
                .acao("DISPONIBILIZAR")
                .dataLimite(LocalDate.now().plusDays(10))
                .build();

                mockMvc.perform(
                        post("/api/subprocessos/{codigo}/disponibilizar-mapa-bloco", processo.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("Todas as atividades devem estar associadas a pelo menos uma competência")));
    }

    @Test
    @DisplayName("Deve rejeitar disponibilização em bloco com data menor que a última data limite de algum subprocesso")
    @WithMockAdmin
    void disponibilizarMapaEmBloco_deveFalharQuandoDataMenorQueUltimaDataLimite() throws Exception {
        subprocesso1.setDataLimiteEtapa1(LocalDate.now().plusDays(7).atStartOfDay());
        subprocesso1.setDataLimiteEtapa2(LocalDate.now().plusDays(15).atStartOfDay());
        subprocessoRepo.saveAndFlush(subprocesso1);

        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .build());

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(List.of(subprocesso1.getCodigo()))
                .acao("DISPONIBILIZAR")
                .dataLimite(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/disponibilizar-mapa-bloco", processo.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("A data limite deve ser maior ou igual à última data limite do subprocesso.")));
    }
}

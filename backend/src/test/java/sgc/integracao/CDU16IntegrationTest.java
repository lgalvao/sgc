package sgc.integracao;

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
@DisplayName("CDU-16: Ajustar mapa de competências")
@WithMockAdmin
class CDU16IntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSO_MAPA_AJUSTE =
            "/api/subprocessos/{codSubprocesso}/mapa-ajuste/atualizar";

    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private Subprocesso subprocesso;
    private Atividade atividade1;

    @BeforeEach
    void setUp() {
        // Criar Unidade via Fixture
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Unidade CDU-16");
        unidade.setSigla("U16");
        unidade = unidadeRepo.save(unidade);

        // Add responsabilidade to prevent 404 during email notification
        jdbcTemplate.update("INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidade.getCodigo(), "111111111111", "00000", "TITULAR", LocalDateTime.now());

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo de Revisão");
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        // Criar Subprocesso via Fixture (Primeiro o subprocesso, pois Mapa depende dele)
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(null); // Importante: limpar mapa da fixture para evitar dependência circular errada
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocesso = subprocessoRepo.save(subprocesso);

        // Criar Mapa via Fixture (Ligado ao Subprocesso)
        Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        // Atualizar referência no subprocesso (para consistência do objeto em memória)
        subprocesso.setMapa(mapa);

        // Garante que o subprocesso esteja na unidade do ADMIN (1) para permitir ajuste
        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        Movimentacao movAdmin = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin para Ajuste")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(movAdmin);

        // Criar Competências e Atividades
        var c1 = competenciaRepo.save(Competencia.builder().descricao("Competência 1").mapa(mapa).build());

        // As atividades devem ser salvas antes de serem associadas à competência
        atividade1 = Atividade.builder().mapa(mapa).descricao("Atividade 1").build();
        var atividade2 = Atividade.builder().mapa(mapa).descricao("Atividade 2").build();

        List<Atividade> atividadesSalvas = atividadeRepo.saveAll(List.of(atividade1, atividade2));
        atividade1 = atividadesSalvas.get(0);
        var atividade2Salva = atividadesSalvas.get(1);

        // Associar atividades à competência
        atividade1.getCompetencias().add(c1);
        atividade2Salva.getCompetencias().add(c1);
        c1.getAtividades().add(atividade1);
        c1.getAtividades().add(atividade2Salva);

        // Salvar associações
        atividadeRepo.saveAll(List.of(atividade1, atividade2Salva));
        competenciaRepo.save(c1);
    }

    @Test
    @DisplayName("Deve submeter o mapa ajustado e alterar a situação do subprocesso")
    void deveSubmeterMapaAjustadoComSucesso() throws Exception {
        var request =
                SubmeterMapaAjustadoRequest.builder()
                        .justificativa("Ajustes realizados conforme solicitado.")
                        .dataLimiteEtapa2(LocalDateTime.now().plusDays(10))
                        .build();

        mockMvc.perform(
                        post(
                                "/api/subprocessos/{id}/submeter-mapa-ajustado",
                                subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Subprocesso subprocessoAtualizado =
                subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao())
                .isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
    }

    @Nested
    @DisplayName("Testes de ajuste do mapa")
    class AjusteDoMapa {
        @Test
        @DisplayName("Deve salvar ajustes no mapa e alterar a situação do subprocesso")
        void deveSalvarAjustesComSucesso() throws Exception {
            Competencia c1 = competenciaRepo.findAll().getFirst();
            Atividade a1 = atividadeRepo.findAll().stream().filter(a -> a.getDescricao().equals("Atividade 1")).findFirst().orElseThrow();

            var request =
                    new SalvarAjustesRequest(
                            List.of(
                                    CompetenciaAjusteDto.builder()
                                            .codCompetencia(c1.getCodigo())
                                            .nome("Competência Ajustada")
                                            .atividades(
                                                    List.of(
                                                            AtividadeAjusteDto.builder()
                                                                    .codAtividade(
                                                                            a1.getCodigo())
                                                                    .nome("Atividade 1 Ajustada")
                                                                    .conhecimentos(List.of())
                                                                    .build()))
                                            .build()));

            mockMvc.perform(
                            post(API_SUBPROCESSO_MAPA_AJUSTE, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            Atividade atividadeAtualizada =
                    atividadeRepo.findById(atividade1.getCodigo()).orElseThrow();
            assertThat(atividadeAtualizada.getDescricao()).isEqualTo("Atividade 1 Ajustada");
        }

        @Test
        @DisplayName("Deve retornar 409 se tentar ajustar mapa em situação inválida")
        void deveRetornarErroParaSituacaoInvalida() throws Exception {
            subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            subprocessoRepo.save(subprocesso);

            var request = new SalvarAjustesRequest(
                    List.of(
                            CompetenciaAjusteDto.builder()
                                    .codCompetencia(1L)
                                    .nome("Competência Dummy")
                                    .atividades(List.of())
                                    .build()));

            mockMvc.perform(
                            post(API_SUBPROCESSO_MAPA_AJUSTE, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent());
        }
    }
}

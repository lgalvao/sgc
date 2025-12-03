package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ImportarAtividadesReq;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest

@AutoConfigureMockMvc

@Transactional

@ActiveProfiles("test")

@Import({ TestSecurityConfig.class, TestThymeleafConfig.class })

@DisplayName("CDU-08: Manter cadastro de atividades e conhecimentos")

class CDU08IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    private Subprocesso subprocessoOrigem;
    private Subprocesso subprocessoDestino;

    @BeforeEach
    void setUp() {
        Unidade unidadeOrigem = unidadeRepo.findById(8L).orElseThrow(); // SEDESENV
        Unidade unidadeDestino = unidadeRepo.findById(9L).orElseThrow(); // SEDIA

        Processo processo = new Processo("Processo Teste", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO,
                LocalDateTime.now().plusDays(30));
        processoRepo.save(processo);

        Mapa mapaOrigem = new Mapa();
        mapaRepo.save(mapaOrigem);

        Atividade atividade1 = new Atividade(mapaOrigem, "Atividade 1");
        atividadeRepo.save(atividade1);
        conhecimentoRepo.save(new Conhecimento("Conhecimento 1.1", atividade1));

        Atividade atividade2 = new Atividade(mapaOrigem, "Atividade 2");
        atividadeRepo.save(atividade2);
        conhecimentoRepo.save(new Conhecimento("Conhecimento 2.1", atividade2));
        conhecimentoRepo.save(new Conhecimento("Conhecimento 2.2", atividade2));

        subprocessoOrigem = new Subprocesso()
                .setUnidade(unidadeOrigem)
                .setMapa(mapaOrigem)
                .setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO)
                .setDataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .setProcesso(processo);

        subprocessoRepo.save(subprocessoOrigem);

        Mapa mapa = new Mapa(); // Inicializa o mapa para os testes de CRUD
        mapaRepo.save(mapa);

        subprocessoDestino = new Subprocesso();
        subprocessoDestino.setUnidade(unidadeDestino);
        subprocessoDestino.setMapa(mapa); // Usa o 'mapa' para os testes de CRUD
        subprocessoDestino.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        subprocessoDestino.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocessoDestino.setProcesso(processo);
        subprocessoRepo.save(subprocessoDestino);
    }

    @Nested
    @DisplayName("Testes de importação de atividades")
    @WithMockChefe("888888888888")
    class ImportacaoAtividades {
        @Test
        @DisplayName("Deve importar atividades e conhecimentos")
        void deveImportarAtividadesEConhecimentosComSucesso() throws Exception {
            ImportarAtividadesReq request = new ImportarAtividadesReq(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                    post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Atividades importadas.")));

            List<Atividade> atividadesDestino = atividadeRepo
                    .findByMapaCodigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).hasSize(2);

            Atividade atividade1Importada = atividadesDestino.stream()
                    .filter(a -> a.getDescricao().equals("Atividade 1")).findFirst().orElse(null);
            assertThat(atividade1Importada).isNotNull();
            List<Conhecimento> conhecimentos1 = conhecimentoRepo.findByAtividadeCodigo(atividade1Importada.getCodigo());
            assertThat(conhecimentos1).hasSize(1);
            assertThat(conhecimentos1.getFirst().getDescricao()).isEqualTo("Conhecimento 1.1");

            Atividade atividade2Importada = atividadesDestino.stream()
                    .filter(a -> a.getDescricao().equals("Atividade 2")).findFirst().orElse(null);
            assertThat(atividade2Importada).isNotNull();
            List<Conhecimento> conhecimentos2 = conhecimentoRepo.findByAtividadeCodigo(atividade2Importada.getCodigo());
            assertThat(conhecimentos2).hasSize(2);
            assertThat(conhecimentos2.stream().map(Conhecimento::getDescricao).toList())
                    .containsExactlyInAnyOrder("Conhecimento 2.1", "Conhecimento 2.2");

            List<sgc.subprocesso.model.Movimentacao> movimentacoes = movimentacaoRepo
                    .findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoDestino.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao())
                    .contains("Importação de atividades do subprocesso #" + subprocessoOrigem.getCodigo());
        }

        @Test
        @DisplayName("Deve importar e atualizar status se NAO_INICIADO")
        void deveImportarEAtualizarStatus() throws Exception {
            subprocessoDestino.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            subprocessoRepo.save(subprocessoDestino);

            ImportarAtividadesReq request = new ImportarAtividadesReq(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                    post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            Subprocesso atualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("Deve falhar ao importar para subprocesso em estado inválido")
        void deveFalharAoImportarParaSubprocessoEmEstadoInvalido() throws Exception {
            subprocessoDestino.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
            subprocessoRepo.save(subprocessoDestino);

            ImportarAtividadesReq request = new ImportarAtividadesReq(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                    post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Deve falhar ao importar de subprocesso inexistente")
        void deveFalharAoImportarDeSubprocessoInexistente() throws Exception {
            ImportarAtividadesReq request = new ImportarAtividadesReq(9999L);

            mockMvc.perform(
                    post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Não deve importar nada de subprocesso sem atividades")
        void naoDeveImportarNadaDeSubprocessoSemAtividades() throws Exception {
            Mapa mapaOrigemVazio = new Mapa();
            mapaRepo.save(mapaOrigemVazio);
            subprocessoOrigem.setMapa(mapaOrigemVazio);
            subprocessoRepo.save(subprocessoOrigem);

            ImportarAtividadesReq request = new ImportarAtividadesReq(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                    post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            List<Atividade> atividadesDestino = atividadeRepo
                    .findByMapaCodigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).isEmpty();
        }

        @Test
        @DisplayName("Deve importar apenas atividades não existentes no destino")
        void deveImportarApenasAtividadesNaoExistentesNoDestino() throws Exception {
            Atividade atividadeExistente = new Atividade(subprocessoDestino.getMapa(), "Atividade 2");
            atividadeRepo.save(atividadeExistente);

            ImportarAtividadesReq request = new ImportarAtividadesReq(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                    post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            List<Atividade> atividadesDestino = atividadeRepo
                    .findByMapaCodigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).hasSize(2);
            assertThat(atividadesDestino.stream().map(Atividade::getDescricao).toList())
                    .containsExactlyInAnyOrder("Atividade 1", "Atividade 2");
        }
    }
}

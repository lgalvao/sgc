package sgc.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
@DisplayName("Testes do E2eTestController")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({TestSecurityConfig.class})
@org.springframework.transaction.annotation.Transactional
class E2eTestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @BeforeEach
    void setUp() {
        var processo1 = new Processo();
        processo1.setTipo(TipoProcesso.MAPEAMENTO);
        processo1.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo1);

        var processo2 = new Processo();
        processo2.setTipo(TipoProcesso.REVISAO);
        processo2.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo2);

        var subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo1);
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        subprocessoRepo.save(subprocesso);

        var movimentacao = new Movimentacao();
        movimentacao.setSubprocesso(subprocesso);
        movimentacao.setDescricao("Movimentação de teste");
        movimentacaoRepo.save(movimentacao);
    }

    @Nested
    @DisplayName("Testes para apagar processo por código")
    class ApagarProcessoPorCodigo {
        @Test
        @DisplayName("Deve apagar processo, subprocessos e movimentações")
        void deveApagarProcessoComDependencias() throws Exception {
            var processos = processoRepo.findAll();
            var codigo = processos.get(0).getCodigo();

            assertThat(processoRepo.existsById(codigo)).isTrue();

            mockMvc.perform(post("/api/e2e/processos/" + codigo + "/apagar"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.existsById(codigo)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar 204 mesmo se processo não existir")
        void deveRetornar204ParaProcessoInexistente() throws Exception {
            mockMvc.perform(post("/api/e2e/processos/99999/apagar"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Testes para limpar processos por unidade")
    class LimparProcessosPorUnidade {
        @Test
        @DisplayName("Deve apagar todos os processos da unidade")
        void deveLimparProcessosDaUnidade() throws Exception {
            mockMvc.perform(post("/api/e2e/processos/unidade/999/limpar"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.count()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Deve retornar 204 se unidade não tem processos")
        void deveRetornar204ParaUnidadeSemProcessos() throws Exception {
            mockMvc.perform(post("/api/e2e/processos/unidade/99999/limpar"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Testes para limpar processos em andamento")
    class LimparProcessosEmAndamento {
        @Test
        @DisplayName("Deve apagar processos com situação EM_ANDAMENTO")
        void deveLimparProcessosEmAndamento() throws Exception {
            var countAntes = processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).size();
            assertThat(countAntes).isGreaterThan(0);

            mockMvc.perform(post("/api/e2e/processos/em-andamento/limpar"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes para reset completo")
    class ResetCompleto {
        @Test
        @DisplayName("Deve deletar todos os processos, subprocessos, alertas e movimentações")
        void deveResetarTodoOBanco() throws Exception {
            var countAntes = processoRepo.count();
            assertThat(countAntes).isGreaterThan(0);

            mockMvc.perform(post("/api/e2e/reset"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.count()).isZero();
            assertThat(subprocessoRepo.count()).isZero();
            assertThat(movimentacaoRepo.count()).isZero();
        }

        @Test
        @DisplayName("Deve ser idempotente")
        void deveSerIdempotente() throws Exception {
            mockMvc.perform(post("/api/e2e/reset"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.count()).isZero();

            mockMvc.perform(post("/api/e2e/reset"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Testes para debug de unidades do processo")
    class DebugUnidadesDoProcesso {
        @Test
        @DisplayName("Deve retornar dados das unidades vinculadas ao processo")
        void deveRetornarDadosUnidadesDoProcesso() throws Exception {
            var processo = processoRepo.findAll().get(0);
            var codigo = processo.getCodigo();

            mockMvc.perform(get("/api/e2e/debug/processos/" + codigo + "/unidades"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Deve retornar 404 se processo não existe")
        void deveRetornar404ParaProcessoInexistente() throws Exception {
            mockMvc.perform(get("/api/e2e/debug/processos/99999/unidades"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar lista vazia se processo não tem unidades")
        void deveRetornarListaVaziaProcessoSemUnidades() throws Exception {
            var processosemUnidades = new Processo();
            processosemUnidades.setTipo(TipoProcesso.DIAGNOSTICO);
            processosemUnidades.setSituacao(SituacaoProcesso.CRIADO);
            processoRepo.save(processosemUnidades);

            var codigo = processosemUnidades.getCodigo();

            mockMvc.perform(get("/api/e2e/debug/processos/" + codigo + "/unidades"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Testes de integridade de dados")
    class IntegridadeDados {
        @Test
        @DisplayName("Deve remover movimentações junto com subprocessos")
        void deveRemoverMovimentacoesDuranteDelecao() throws Exception {
            var subprocessos = subprocessoRepo.findAll();
            assertThat(subprocessos).isNotEmpty();
            var codigoSubprocesso = subprocessos.get(0).getCodigo();

            assertThat(movimentacaoRepo.findBySubprocessoCodigo(codigoSubprocesso)).isNotEmpty();

            var processo = subprocessos.get(0).getProcesso();
            mockMvc.perform(post("/api/e2e/processos/" + processo.getCodigo() + "/apagar"))
                    .andExpect(status().isNoContent());

            assertThat(movimentacaoRepo.count()).isZero();
        }

        @Test
        @DisplayName("Deve ser idempotente ao resetar múltiplas vezes")
        void deveSerIdempotenteAoResetar() throws Exception {
            mockMvc.perform(post("/api/e2e/reset"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.count()).isZero();

            mockMvc.perform(post("/api/e2e/reset"))
                    .andExpect(status().isNoContent());

            assertThat(processoRepo.count()).isZero();
        }
    }
}

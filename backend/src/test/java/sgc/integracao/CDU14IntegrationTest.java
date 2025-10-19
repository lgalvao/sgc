package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.SubprocessoWorkflowService;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-14: Analisar Revisão de Cadastro")
@Import(TestSecurityConfig.class)
class CDU14IntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private SubprocessoWorkflowService subprocessoWorkflowService;

    private Processo processo;
    private Subprocesso subprocesso;
    private Unidade unidade, unidadeGestor, unidadeAdmin;
    private Usuario gestor;

    @BeforeEach
    void setUp() {
        // Hierarquia de Unidades
        unidadeAdmin = new Unidade("ADMIN-UNIT", "ADMIN-UNIT");
        unidadeRepo.save(unidadeAdmin);
        Usuario admin = usuarioRepo.save(new Usuario(111111111111L, "Admin", "admin@email.com", "1111", unidadeAdmin, Set.of(Perfil.ADMIN)));
        unidadeAdmin.setTitular(admin);
        unidadeRepo.save(unidadeAdmin);

        unidadeGestor = new Unidade("GESTOR-UNIT", "GESTOR-UNIT");
        unidadeGestor.setUnidadeSuperior(unidadeAdmin);
        unidadeRepo.save(unidadeGestor);
        gestor = usuarioRepo.save(new Usuario(222222222222L, "Gestor", "gestor@email.com", "2222", unidadeGestor, Set.of(Perfil.GESTOR)));
        unidadeGestor.setTitular(gestor);
        unidadeRepo.save(unidadeGestor);

        unidade = new Unidade("SUB-UNIT", "SUB-UNIT");
        unidade.setUnidadeSuperior(unidadeGestor);
        unidadeRepo.save(unidade);
        Usuario chefe = usuarioRepo.save(new Usuario(333333333333L, "Chefe", "chefe@email.com", "3333", unidade, Set.of(Perfil.CHEFE)));
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        // Processo e Subprocesso
        processo = new Processo("Processo de Revisão Teste", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now());
        processoRepo.save(processo);

        Mapa mapa = mapaRepo.save(new Mapa());
        subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, LocalDateTime.now());
        subprocessoRepo.save(subprocesso);

        // Coloca o subprocesso na fila da unidade gestora para análise
        movimentacaoRepo.save(new Movimentacao(subprocesso, unidade, unidadeGestor, "Disponibilização da revisão do cadastro"));
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Devolver para Ajustes'")
    class DevolverCadastroTest {
        @Test
        @DisplayName("GESTOR deve devolver revisão do cadastro, alterar status, registrar análise, movimentação e alerta")
        @WithMockGestor
        void testGestorDevolveRevisaoCadastro() throws Exception {
            // Ação
            mockMvc.perform(post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"motivo\": \"Teste\", \"observacoes\": \"Ajustar X, Y, Z\"}"))
                .andExpect(status().isOk());

            // Verificações
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

            var analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            assertThat(analises.getFirst().getAcao()).isEqualTo(sgc.analise.modelo.TipoAcaoAnalise.DEVOLUCAO_REVISAO);
            assertThat(analises.getFirst().getObservacoes()).isEqualTo("Ajustar X, Y, Z");
            assertThat(analises.getFirst().getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            var movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());

            var alertas = alertaRepo.findAll();
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(alertas.getFirst().getDescricao()).contains("devolvido para ajustes");
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Registrar Aceite'")
    class AceitarCadastroTest {
        @Test
        @DisplayName("GESTOR deve aceitar revisão do cadastro, registrar análise, e mover para unidade superior")
        @WithMockGestor
        void testGestorAceitaRevisaoCadastro() throws Exception {
            // Ação
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"observacoes\": \"Tudo certo.\"}"))
                .andExpect(status().isOk());

            // Verificações
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

            var analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            assertThat(analises.getFirst().getAcao()).isEqualTo(sgc.analise.modelo.TipoAcaoAnalise.ACEITE_REVISAO);
            assertThat(analises.getFirst().getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            var movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidadeGestor.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeAdmin.getSigla());

            var alertas = alertaRepo.findAll();
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeAdmin.getSigla());
            assertThat(alertas.getFirst().getDescricao()).contains("Revisão do cadastro de atividades e conhecimentos da unidade SUB-UNIT submetida para análise");
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Homologar'")
    class HomologarCadastroTest {
        @BeforeEach
        void setup() {
            // Mover o subprocesso para a unidade do ADMIN para que a homologação seja possível
            subprocessoWorkflowService.aceitarRevisaoCadastro(subprocesso.getCodigo(), "", gestor);
        }

        @Test
        @DisplayName("ADMIN deve homologar revisão e alterar status para MAPA_HOMOLOGADO quando não há impactos")
        @WithMockAdmin
        void testAdminHomologaRevisaoSemImpactos() throws Exception {
            // Ação
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"observacoes\": \"\"}"))
                .andExpect(status().isOk());

            // Verificação
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        }

        @Test
        @DisplayName("ADMIN deve homologar revisão e alterar status para REVISAO_CADASTRO_HOMOLOGADA quando há impactos")
        @WithMockAdmin
        void testAdminHomologaRevisaoComImpactos() throws Exception {
            // Cenário: Adicionar uma atividade para simular impacto
            atividadeRepo.save(new Atividade(subprocesso.getMapa(), "Nova Atividade"));

            // Ação
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"observacoes\": \"\"}"))
                .andExpect(status().isOk());

            // Verificação
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        }
    }
}
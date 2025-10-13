package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import sgc.Sgc;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseValidacao;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.processo.SituacaoProcesso;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.notificacao.NotificacaoServico;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.subprocesso.dto.DevolverValidacaoReq;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("CDU-20: Analisar validação de mapa de competências")
public class CDU20IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired private ProcessoRepo processoRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @Autowired private MovimentacaoRepo movimentacaoRepo;
    @Autowired private AnaliseValidacaoRepo analiseValidacaoRepo;
    @Autowired private AlertaRepo alertaRepo;
    @Autowired private EntityManager entityManager;

    @MockitoBean
    private NotificacaoServico notificacaoServico;

    private Unidade unidadeSubordinada;
    private Unidade unidadeGestor;
    private Unidade unidadeAdmin;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Hierarquia de Unidades
        unidadeAdmin = unidadeRepo.save(new Unidade("Unidade Admin", "ADM"));
        unidadeGestor = new Unidade("Unidade Gestor", "GES");
        unidadeGestor.setUnidadeSuperior(unidadeAdmin);
        unidadeRepo.save(unidadeGestor);
        unidadeSubordinada = new Unidade("Unidade Subordinada", "SUB");
        unidadeSubordinada.setUnidadeSuperior(unidadeGestor);
        unidadeRepo.save(unidadeSubordinada);

        // Usuários
        Usuario admin = new Usuario();
        admin.setTitulo("admin");
        admin.setNome("Admin");
        admin.setEmail("admin@test.com");
        admin.setUnidade(unidadeAdmin);
        usuarioRepo.save(admin);
        unidadeAdmin.setTitular(admin);
        unidadeRepo.save(unidadeAdmin);

        Usuario gestor = new Usuario();
        gestor.setTitulo("gestor_unidade");
        gestor.setNome("Gestor");
        gestor.setEmail("gestor@test.com");
        gestor.setUnidade(unidadeGestor);
        usuarioRepo.save(gestor);
        unidadeGestor.setTitular(gestor);
        unidadeRepo.save(unidadeGestor);

        Usuario chefeSubordinado = new Usuario();
        chefeSubordinado.setTitulo("chefe_sub");
        chefeSubordinado.setNome("Chefe Sub");
        chefeSubordinado.setEmail("chefe@test.com");
        chefeSubordinado.setUnidade(unidadeSubordinada);
        usuarioRepo.save(chefeSubordinado);
        unidadeSubordinada.setTitular(chefeSubordinado);
        unidadeRepo.save(unidadeSubordinada);

        // Processo e Subprocesso
        processo = new Processo("Processo de Validação",
                TipoProcesso.MAPEAMENTO,
                SituacaoProcesso.EM_ANDAMENTO,
                LocalDate.now().plusDays(30));

        processoRepo.save(processo);

        subprocesso = new Subprocesso(processo,
                unidadeSubordinada,
                null,
                SituacaoSubprocesso.MAPA_VALIDADO,
                processo.getDataLimite());

        subprocessoRepo.save(subprocesso);

        // Movimentação inicial: O mapa da unidade SUB foi validado e enviado para a unidade GES
        movimentacaoRepo.save(new Movimentacao(subprocesso,
                unidadeSubordinada,
                unidadeGestor,
                "Validação do mapa de competências")
        );
    }

    @Nested
    @DisplayName("Fluxo de Devolução")
    class Devolucao {

        @Test
        @DisplayName("GESTOR deve devolver validação, registrar análise e alterar situação")
        @WithMockGestor
        void gestorDeveDevolverValidacao() throws Exception {
            // Arrange
            String justificativa = "Mapa precisa de ajustes na competência X.";
            DevolverValidacaoReq request = new DevolverValidacaoReq(justificativa);

            // Act
            mockMvc.perform(post("/api/subprocessos/{id}/devolver-validacao", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            // Assert
            // 1. Subprocesso
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
            assertThat(spAtualizado.getDataFimEtapa2()).isNull();

            // 2. Análise de Validação
            List<AnaliseValidacao> analises = analiseValidacaoRepo.findBySubprocesso_Codigo(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            AnaliseValidacao analise = analises.getFirst();
            assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO);
            assertThat(analise.getObservacoes()).isEqualTo(justificativa);
            assertThat(analise.getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            // 3. Movimentação
            Movimentacao mov = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).getFirst();
            assertThat(mov.getDescricao()).isEqualTo("Devolução da validação do mapa de competências para ajustes");
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(unidadeGestor.getSigla());
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidadeSubordinada.getSigla());

            // 4. Alerta e Notificação
            List<Alerta> alertas = alertaRepo.findAll().stream().filter(a -> a.getProcesso().getCodigo().equals(processo.getCodigo())).collect(Collectors.toList());
            assertThat(alertas).hasSize(1);
            Alerta alerta = alertas.getFirst();
            assertThat(alerta.getUnidadeDestino().getSigla()).isEqualTo(unidadeSubordinada.getSigla());
            assertThat(alerta.getDescricao()).contains("devolvido para ajustes");

            verify(notificacaoServico).enviarEmail(eq(unidadeSubordinada.getSigla()), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Fluxo de Aceite")
    class Aceite {

        @Test
        @DisplayName("GESTOR deve aceitar validação, registrar análise e mover para unidade superior")
        @WithMockGestor
        void gestorDeveAceitarValidacao() throws Exception {
            // Act
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-validacao", subprocesso.getCodigo())
                    .with(csrf()))
                .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            // Assert
            // 1. Subprocesso (situação não muda, apenas a localização via movimentação)
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_VALIDADO);

            // 2. Análise de Validação
            List<AnaliseValidacao> analises = analiseValidacaoRepo.findBySubprocesso_Codigo(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            AnaliseValidacao analise = analises.getFirst();
            assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE);
            assertThat(analise.getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            // 3. Movimentação
            Movimentacao mov = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()).getFirst();
            assertThat(mov.getDescricao()).isEqualTo("Mapa de competências validado");
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(unidadeGestor.getSigla());
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidadeAdmin.getSigla());

            // 4. Alerta e Notificação
            List<Alerta> alertas = alertaRepo.findAll().stream().filter(a -> a.getProcesso().getCodigo().equals(processo.getCodigo())).collect(Collectors.toList());
            assertThat(alertas).hasSize(1);
            Alerta alerta = alertas.getFirst();
            assertThat(alerta.getUnidadeDestino().getSigla()).isEqualTo(unidadeAdmin.getSigla());
            assertThat(alerta.getDescricao()).contains("submetida para análise");

            verify(notificacaoServico).enviarEmail(eq(unidadeAdmin.getSigla()), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Fluxo de Homologação")
    class Homologacao {

        @Test
        @DisplayName("ADMIN deve homologar validação e alterar situação para MAPA_HOMOLOGADO")
        @WithMockAdmin
        void adminDeveHomologarValidacao() throws Exception {
            // Arrange: Simular que o processo chegou na unidade do admin
            movimentacaoRepo.save(new Movimentacao(subprocesso, unidadeGestor, unidadeAdmin, "Mapa de competências validado"));

            // Act
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-validacao", subprocesso.getCodigo())
                    .with(csrf()))
                .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            // Assert
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);

            // Verificar que não há novas análises ou movimentações para a homologação simples
            assertThat(analiseValidacaoRepo.count()).isZero();

            // A movimentação inicial, a do arrange e a da ação
            assertThat(movimentacaoRepo.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("GESTOR não deve conseguir homologar")
        @WithMockGestor
        void gestorNaoPodeHomologar() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-validacao", subprocesso.getCodigo())
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }
}
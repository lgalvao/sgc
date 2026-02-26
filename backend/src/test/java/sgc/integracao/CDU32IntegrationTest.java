package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.ComumDtos.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-32: Reabrir cadastro")
class CDU32IntegrationTest extends BaseIntegrationTest {
    private static final String API_REABRIR_CADASTRO = "/api/subprocessos/{codigo}/reabrir-cadastro";

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Garantir que ADMIN existe
        if (unidadeRepo.findBySigla("ADMIN").isEmpty()) {
            Unidade admin = new Unidade();
            admin.setSigla("ADMIN");
            admin.setNome("Administração");
            admin.setSituacao(SituacaoUnidade.ATIVA);
            admin.setTipo(TipoUnidade.RAIZ);
            unidadeRepo.save(admin);
        }

        // Obter Unidade
        Unidade unidade = unidadeRepo.findById(1L).orElseGet(() -> {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setSigla("TESTE");
            u.setNome("Unidade Teste");
            u.setSituacao(SituacaoUnidade.ATIVA);
            u.setTipo(TipoUnidade.OPERACIONAL);
            return unidadeRepo.save(u);
        });

        // Criar Processo
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-32");
        processo = processoRepo.save(processo);

        // Criar Subprocesso em estado que permite reabertura
        // (MAPEAMENTO_CADASTRO_HOMOLOGADO)
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve reabrir cadastro com justificativa válida quando ADMIN")
    @WithMockAdmin
    void reabrirCadastro_comoAdmin_sucesso() throws Exception {
        // Given
        JustificativaRequest request = new JustificativaRequest(
                "Necessário ajustar informações do cadastro");

        // When
        mockMvc.perform(
                        post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        Subprocesso reaberto = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(reaberto.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        // Verificar se foi criada uma movimentação
        List<Movimentacao> movimentacoes = movimentacaoRepo
                .findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(movimentacoes).isNotEmpty();
        boolean movimentacaoExiste = movimentacoes.stream()
                .anyMatch(m -> m.getDescricao() != null &&
                        m.getDescricao().contains("Reabertura de cadastro"));
        assertThat(movimentacaoExiste).isTrue();

        // Verificar se foi criado um alerta
        List<Alerta> alerts = alertaRepo.findAll();
        assertThat(alerts).isNotEmpty();
        boolean alertaExiste = alerts.stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        a.getUnidadeDestino().getCodigo()
                                .equals(reaberto.getUnidade().getCodigo())
                        &&
                        a.getDescricao().contains("reaberto"));
        assertThat(alertaExiste).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir reabrir cadastro sem ser ADMIN")
    @WithMockUser(roles = "GESTOR")
    void reabrirCadastro_semPermissao_proibido() throws Exception {
        // Given
        JustificativaRequest request = new JustificativaRequest("Tentativa sem permissão");

        // When/Then
        mockMvc.perform(
                        post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir reabrir cadastro sem justificativa")
    @WithMockAdmin
    void reabrirCadastro_semJustificativa_erro() throws Exception {
        // Given
        JustificativaRequest request = new JustificativaRequest("");

        // When/Then
        mockMvc.perform(
                        post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

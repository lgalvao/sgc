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
import static sgc.organizacao.model.SituacaoUnidade.*;
import static sgc.organizacao.model.TipoUnidade.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-33: Reabrir revisão de cadastro")
class CDU33IntegrationTest extends BaseIntegrationTest {
    private static final String API_REABRIR_REVISAO = "/api/subprocessos/{codigo}/reabrir-revisao-cadastro";

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
            admin.setSituacao(ATIVA);
            admin.setTipo(TipoUnidade.RAIZ);
            unidadeRepo.save(admin);
        }

        // Obter unidade
        Unidade unidade = unidadeRepo.findById(1L).orElseGet(() -> {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setSigla("TESTE");
            u.setNome("Unidade teste");
            u.setSituacao(ATIVA);
            u.setTipo(OPERACIONAL);
            return unidadeRepo.save(u);
        });

        // Criar processo de REVISAO
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setSituacao(EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-33");
        processo = processoRepo.save(processo);

        // Criar subprocesso em estado que permita reabertura de revisão (REVISAO_MAPA_HOMOLOGADO)
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso.setSituacaoForcada(REVISAO_MAPA_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve reabrir revisão de cadastro com justificativa válida quando ADMIN")
    @WithMockAdmin
    void reabrirRevisaoCadastro_comoAdmin_sucesso() throws Exception {
        JustificativaRequest request = new JustificativaRequest("Necessário corrigir erros identificados na revisão");

        Long codSp = subprocesso.getCodigo();
        mockMvc.perform(post(API_REABRIR_REVISAO, codSp)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Subprocesso spReaberto = subprocessoRepo.findById(codSp).orElseThrow();
        assertThat(spReaberto.getSituacao()).isEqualTo(REVISAO_CADASTRO_EM_ANDAMENTO);

        // Verificar se foi criada uma movimentação
        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSp);
        assertThat(movimentacoes).isNotEmpty();
        boolean movimentacaoExiste = movimentacoes.stream()
                .anyMatch(m -> m.getDescricao().contains("Reabertura de revisão de cadastro"));

        assertThat(movimentacaoExiste).isTrue();

        // Verificar se foi criado um alerta
        List<Alerta> alerts = alertaRepo.findAll();
        assertThat(alerts).isNotEmpty();
        boolean alertaExiste = alerts.stream().anyMatch(a -> {
            Long unidadeDestinoCodigo = a.getUnidadeDestino().getCodigo();
            Long unidadeSpReabertoCodigo = spReaberto.getUnidade().getCodigo();
            return Objects.equals(unidadeDestinoCodigo, unidadeSpReabertoCodigo) &&
                    a.getDescricao().contains("reaberta");
        });

        assertThat(alertaExiste).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir reabrir revisão de cadastro sem ser ADMIN")
    @WithMockUser(roles = "GESTOR")
    void reabrirRevisaoCadastro_semPermissao_proibido() throws Exception {
        JustificativaRequest request = new JustificativaRequest("Tentativa sem permissão");

        mockMvc.perform(post(API_REABRIR_REVISAO, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir reabrir revisão sem justificativa")
    @WithMockAdmin
    void reabrirRevisaoCadastro_semJustificativa_erro() throws Exception {
        JustificativaRequest request = new JustificativaRequest("");

        mockMvc.perform(post(API_REABRIR_REVISAO, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Não deve permitir reabrir revisão quando em situação insuficiente (ex: Revisão homologada precoce)")
    @WithMockAdmin
    void reabrirRevisaoCadastro_SituacaoInsuficiente_Erro() throws Exception {
        // Forçar situação que ainda não atingiu REVISAO_MAPA_HOMOLOGADO
        subprocesso.setSituacaoForcada(REVISAO_CADASTRO_HOMOLOGADA);
        subprocessoRepo.save(subprocesso);
        entityManager.flush();
        entityManager.clear();

        JustificativaRequest request = new JustificativaRequest("Tentativa em estado precoce");

        mockMvc.perform(post(API_REABRIR_REVISAO, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }
}

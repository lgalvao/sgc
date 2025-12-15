package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.AlertaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.IniciarProcessoReq;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@WithMockAdmin
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-04: Iniciar processo de mapeamento")
public class CDU04IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @MockitoBean
    private NotificacaoEmailService notificacaoEmailService;

    @Test
    @DisplayName("Deve iniciar processo de mapeamento com sucesso e gerar subprocessos, alertas e notificações")
    void deveIniciarProcessoMapeamento() throws Exception {
        // 1. Arrange: Criar um processo em estado 'CRIADO'
        // Usar unidade 9 (ASSESSORIA_12) que não está em nenhum processo no import.sql
        Unidade livre = unidadeRepo.findById(9L).orElseThrow();

        CriarProcessoReq criarReq = new CriarProcessoReq(
                "Processo Mapeamento Teste CDU-04",
                TipoProcesso.MAPEAMENTO,
                LocalDateTime.now().plusDays(10),
                List.of(livre.getCodigo())
        );

        var result = mockMvc.perform(post("/api/processos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // 2. Act: Iniciar Processo
        // O endpoint espera IniciarProcessoReq com a lista de unidades (para validação/confirmação)
        IniciarProcessoReq iniciarReq = new IniciarProcessoReq(TipoProcesso.MAPEAMENTO, List.of(livre.getCodigo()));

        mockMvc.perform(post("/api/processos/{id}/iniciar", processoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isOk());

        // 3. Assert: Mudança de Status do Processo
        Processo processo = processoRepo.findById(processoId).orElseThrow();
        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        // 4. Assert: Criação de Subprocessos
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processoId);
        assertThat(subprocessos).hasSize(1);

        // Verificar subprocesso da unidade livre
        Subprocesso sub = subprocessos.stream()
                .filter(s -> s.getUnidade().getCodigo().equals(9L))
                .findFirst()
                .orElseThrow();
        assertThat(sub.getMapa()).isNotNull();

        // Verificar que o mapa não tem competências (Mapeamento inicia vazio)
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(sub.getMapa().getCodigo());
        assertThat(competencias).isEmpty();

        // 5. Assert: Geração de Alertas
        long alertasCount = alertaRepo.count();
        assertThat(alertasCount).isGreaterThan(0);

        // 6. Assert: Envio de Notificação por Email
        verify(notificacaoEmailService, atLeastOnce()).enviarEmailHtml(any(), any(), any());
    }
}

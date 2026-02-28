package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.result.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
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
@DisplayName("CDU-34: Enviar lembrete de prazo")
class CDU34IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private Unidade unidade;
    private Processo processo;

    @BeforeEach
    void setUp() {
        // Obter Unidade
        unidade = unidadeRepo.findById(2L).orElseThrow();

        // Criar Processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-34");
        processo.adicionarParticipantes(Set.of(unidade)); // Adicionar unidade como participante
        processo = processoRepo.save(processo);

        // Criar Subprocesso com prazo próximo
        Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(3));
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve enviar lembrete de prazo para unidade quando ADMIN")
    @WithMockAdmin
    void enviarLembrete_comoAdmin_sucesso() throws Exception {

        EnviarLembreteRequest request = EnviarLembreteRequest.builder()
                .unidadeCodigo(unidade.getCodigo())
                .build();


        mockMvc.perform(
                        post("/api/processos/{codigo}/enviar-lembrete", processo.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());


        entityManager.flush();
        entityManager.clear();

        // Verificar se foi criado um alerta
        boolean alertaExiste = alertaRepo.findAll().stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        a.getUnidadeDestino().getCodigo().equals(unidade.getCodigo()) &&
                        a.getDescricao().toLowerCase().contains("lembrete"));
        assertThat(alertaExiste).isTrue();

        boolean movimentacaoExiste = movimentacaoRepo.findAll().stream()
                .anyMatch(m -> "Lembrete de prazo enviado".equals(m.getDescricao())
                        && m.getUnidadeDestino() != null
                        && m.getUnidadeDestino().getCodigo().equals(unidade.getCodigo()));
        assertThat(movimentacaoExiste).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir enviar lembrete sem ser ADMIN")
    @WithMockUser(roles = "GESTOR")
    void enviarLembrete_semPermissao_proibido() throws Exception {

        EnviarLembreteRequest request = EnviarLembreteRequest.builder()
                .unidadeCodigo(unidade.getCodigo())
                .build();

        // When/Then
        mockMvc.perform(
                        post("/api/processos/{codigo}/enviar-lembrete", processo.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar erro ao enviar lembrete para unidade inexistente")
    @WithMockAdmin
    void enviarLembrete_unidadeInexistente_erro() throws Exception {

        EnviarLembreteRequest request = EnviarLembreteRequest.builder()
                .unidadeCodigo(99999L)
                .build();

        // When/Then
        mockMvc.perform(
                        post("/api/processos/{codigo}/enviar-lembrete", processo.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}

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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-27: Alterar data limite de subprocesso")
class CDU27IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Obter Unidade
        Unidade unidade = unidadeRepo.findById(1L).orElseThrow();

        // Criar Processo
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-27");
        processo = processoRepo.save(processo);

        // Criar Subprocesso
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve alterar a data limite e gerar alerta quando ADMIN")
    @WithMockAdmin
    void alterarDataLimite_comoAdmin_sucesso() throws Exception {

        LocalDate novaData = LocalDate.now().plusDays(20);
        DataRequest request = new DataRequest(novaData);


        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/data-limite", subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());


        entityManager.flush();
        entityManager.clear();

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getDataLimiteEtapa1().toLocalDate()).isEqualTo(novaData);

        // Verify Alerta
        boolean alertaExiste = alertaRepo.findAll().stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        a.getUnidadeDestino().getCodigo().equals(atualizado.getUnidade().getCodigo()) &&
                        a.getDescricao().contains("Data limite"));
        assertThat(alertaExiste).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir alterar data limite se não for ADMIN")
    @WithMockUser(roles = "GESTOR")
    void alterarDataLimite_semPermissao_proibido() throws Exception {

        LocalDate novaData = LocalDate.now().plusDays(20);
        DataRequest request = new DataRequest(novaData);

        // When/Then
        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/data-limite", subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

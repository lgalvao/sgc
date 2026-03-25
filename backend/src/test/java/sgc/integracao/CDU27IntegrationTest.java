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
import java.util.List;

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
        // Obter unidade
        Unidade unidade = unidadeRepo.findById(1L).orElseThrow();

        // Criar processo
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-27");
        processo = processoRepo.save(processo);

        // Criar subprocesso
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
    @DisplayName("Deve alterar a data limite, gerar alerta e enviar e-mail quando ADMIN (Etapa 1)")
    @WithMockAdmin
    void alterarDataLimite_etapa1_sucesso() throws Exception {

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

        // 1. Verificar Alerta
        List<Alerta> alertas = alertaRepo.findAll();
        Alerta alerta = alertas.getLast();
        assertThat(alerta.getDescricao()).contains("Data limite da etapa 1 alterada");
        assertThat(alerta.getUnidadeDestino().getCodigo()).isEqualTo(atualizado.getUnidade().getCodigo());
        assertThat(alerta.getProcesso().getCodigo()).isEqualTo(subprocesso.getProcesso().getCodigo());

        // 2. Verificar E-mail
        aguardarEmail(1);
        assertThat(algumEmailContem("A data limite da etapa atual no processo Processo CDU-27 foi alterada")).isTrue();
    }

    @Test
    @DisplayName("Deve alterar a data limite e gerar alerta para Etapa 2 (MAPA)")
    @WithMockAdmin
    void alterarDataLimite_etapa2_sucesso() throws Exception {

        // Mudar situação para MAPA
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        subprocessoRepo.save(subprocesso);
        entityManager.flush();

        LocalDate novaData = LocalDate.now().plusDays(25);
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
        assertThat(atualizado.getDataLimiteEtapa2().toLocalDate()).isEqualTo(novaData);

        // Verificar Alerta para Etapa 2
        List<Alerta> alertas = alertaRepo.findAll();
        Alerta alerta = alertas.getLast();
        assertThat(alerta.getDescricao()).contains("Data limite da etapa 2 alterada");
    }

    @Test
    @DisplayName("Não deve permitir alterar data limite se não for ADMIN")
    @WithMockUser(roles = "GESTOR")
    void alterarDataLimite_semPermissao_proibido() throws Exception {

        LocalDate novaData = LocalDate.now().plusDays(20);
        DataRequest request = new DataRequest(novaData);

        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/data-limite", subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir alterar data limite para data passada ou hoje")
    @WithMockAdmin
    void alterarDataLimite_dataPassada_erro() throws Exception {

        LocalDate dataOntem = LocalDate.now().minusDays(1);
        DataRequest request = new DataRequest(dataOntem);

        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/data-limite", subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

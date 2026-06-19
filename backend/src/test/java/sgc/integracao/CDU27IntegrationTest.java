package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.*;
import sgc.comum.ComumDtos.DataRequest;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-27: Alterar data limite de subprocesso")
class CDU27IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

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

        // 2. Verificar E-mail no outbox
        NotificacaoEmail notificacao = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.DATA_LIMITE_ALTERADA)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Notificação de data limite não encontrada no outbox"));
        assertThat(notificacao.getAssunto()).isEqualTo("SGC: Data limite alterada");
        assertThat(notificacao.getCorpoHtml()).contains("Processo CDU-27");
        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);

        // 3. Verificar entrega do e-mail com GreenMail (destinatário, assunto e corpo)
        aguardarEmail(1);
        assertThat(algumEmailPara(notificacao.getDestinatario())).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Data limite alterada")).isTrue();
        assertThat(algumEmailContem("Processo CDU-27")).isTrue();
        assertThat(algumEmailContem("A data limite da etapa atual no processo")).isTrue();
    }

    @Test
    @DisplayName("Deve alterar a data limite e gerar alerta para Etapa 2 (MAPA)")
    @WithMockAdmin
    void alterarDataLimite_etapa2_sucesso() throws Exception {

        // Mudar situação para MAPA
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        subprocesso.setDataFimEtapa1(LocalDate.now().plusDays(15).atStartOfDay());
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
        assertThat(atualizado.getDataLimiteEtapa2()).isNotNull();
        assertThat(atualizado.getDataLimiteEtapa2().toLocalDate()).isEqualTo(novaData);

        // Verificar Alerta para Etapa 2
        List<Alerta> alertas = alertaRepo.findAll();
        Alerta alerta = alertas.getLast();
        assertThat(alerta.getDescricao()).contains("Data limite da etapa 2 alterada");
    }

    @Test
    @DisplayName("Não deve permitir alterar data limite da etapa 2 para data menor ou igual ao fim da etapa anterior")
    @WithMockAdmin
    void alterarDataLimite_etapa2_dataMenorOuIgualFimEtapaAnterior_erro() throws Exception {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        subprocesso.setDataFimEtapa1(LocalDate.now().plusDays(15).atStartOfDay());
        subprocessoRepo.save(subprocesso);
        entityManager.flush();

        LocalDate novaData = LocalDate.now().plusDays(15);
        DataRequest request = new DataRequest(novaData);

        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/data-limite", subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("A data limite deve ser maior que a data de fim da etapa anterior.")));
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

    @Test
    @DisplayName("Deve permitir reduzir a data limite desde que a nova data continue no futuro")
    @WithMockAdmin
    void alterarDataLimite_dataMenorQueAtual_masFutura_sucesso() throws Exception {
        LocalDate novaData = subprocesso.getDataLimiteEtapa1().toLocalDate().minusDays(1);
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
    }
}

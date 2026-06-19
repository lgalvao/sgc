package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.NotificacaoEmailRepo;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.alerta.model.TipoNotificacao;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CDU-38 — Acompanhar notificações por e-mail")
class CDU38IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setup() {
        notificacaoEmailRepo.deleteAll();

        Unidade unidade = unidadeRepo.findById(102L).orElseThrow();
        Processo processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(null);
        processo = processoRepo.save(processo);

        subprocesso = SubprocessoFixture.novoSubprocesso(processo, unidade);
        subprocesso = subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("Deve listar notificações ordenadas por criticidade e data")
    @WithMockAdmin
    void deveListarNotificacoesOrdenadas() throws Exception {
        LocalDateTime agora = LocalDateTime.now();

        // Criando notificações em ordem inversa de criticidade para testar o sort
        notificacaoEmailRepo.save(criarNotificacao("Enviado", SituacaoNotificacao.ENVIADO, agora.minusMinutes(1)));
        notificacaoEmailRepo.save(criarNotificacao("Pendente", SituacaoNotificacao.PENDENTE, agora.minusMinutes(2)));
        notificacaoEmailRepo.save(criarNotificacao("Falha Temporária", SituacaoNotificacao.FALHA_TEMPORARIA, agora.minusMinutes(3)));
        notificacaoEmailRepo.save(criarNotificacao("Falha Definitiva Recente", SituacaoNotificacao.FALHA_DEFINITIVA, agora.minusMinutes(4)));
        notificacaoEmailRepo.save(criarNotificacao("Falha Definitiva Antiga", SituacaoNotificacao.FALHA_DEFINITIVA, agora.minusMinutes(5)));

        mockMvc.perform(get("/api/admin/notificacoes/listar").param("limite", "10"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    List<String> assuntos = new ArrayList<>();
                    objectMapper.readTree(body).forEach(node -> assuntos.add(node.get("assunto").asString()));

                    // Ordem esperada:
                    // 1. Falha Definitiva Recente (mais crítica, mais recente entre as mesmas situações)
                    // 2. Falha Definitiva Antiga
                    // 3. Falha Temporária
                    // 4. Pendente
                    // 5. Enviado
                    assertThat(assuntos).containsExactly(
                            "Falha Definitiva Recente",
                            "Falha Definitiva Antiga",
                            "Falha Temporária",
                            "Pendente",
                            "Enviado"
                    );
                });
    }

    @Test
    @DisplayName("Deve reenviar notificação com falha definitiva")
    @WithMockAdmin
    void deveReenviarFalhaDefinitiva() throws Exception {
        NotificacaoEmail falha = notificacaoEmailRepo.save(criarNotificacao("Falha", SituacaoNotificacao.FALHA_DEFINITIVA, LocalDateTime.now()));

        mockMvc.perform(post("/api/admin/notificacoes/{codigo}/reenviar", falha.getCodigo()))
                .andExpect(status().isOk());

        NotificacaoEmail atualizada = notificacaoEmailRepo.findById(falha.getCodigo()).orElseThrow();
        assertThat(atualizada.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
    }

    private NotificacaoEmail criarNotificacao(String assunto, SituacaoNotificacao situacao, LocalDateTime data) {
        return NotificacaoEmail.builder()
                .subprocesso(subprocesso)
                .tipoNotificacao(TipoNotificacao.PROCESSO_INICIADO)
                .destinatario("teste@tre-pe.jus.br")
                .assunto(assunto)
                .corpoHtml("<p>teste</p>")
                .situacao(situacao)
                .dataHoraCriacao(data)
                .chaveIdempotencia(UUID.randomUUID().toString())
                .build();
    }
}

package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.NotificacaoService;
import sgc.alerta.dto.NotificacaoSubprocessoResumoDto;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.NotificacaoEmailRepo;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.alerta.model.TipoNotificacao;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("NotificacaoAdmin — integração")
class NotificacaoAdminIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Autowired
    private NotificacaoService notificacaoService;

    private Subprocesso subprocessoAtivo;
    private Long codigoFalhaDefinitiva;

    @BeforeEach
    void setUp() {
        Unidade unidade = unidadeRepo.findById(102L).orElseThrow();

        Processo processoAtivo = ProcessoFixture.processoEmAndamento();
        processoAtivo.setCodigo(null);
        processoAtivo.setDescricao("Processo ativo notificações");
        processoAtivo = processoRepo.save(processoAtivo);

        subprocessoAtivo = SubprocessoFixture.novoSubprocesso(processoAtivo, unidade);
        subprocessoAtivo.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoAtivo = subprocessoRepo.save(subprocessoAtivo);

        Processo processoFinalizado = ProcessoFixture.processoPadrao();
        processoFinalizado.setCodigo(null);
        processoFinalizado.setDescricao("Processo finalizado notificações");
        processoFinalizado.setSituacao(SituacaoProcesso.FINALIZADO);
        processoFinalizado = processoRepo.save(processoFinalizado);

        Subprocesso subprocessoFinalizado = SubprocessoFixture.novoSubprocesso(processoFinalizado, unidade);
        subprocessoFinalizado.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoFinalizado = subprocessoRepo.save(subprocessoFinalizado);

        List<NotificacaoEmail> notificacoesSalvas = notificacaoEmailRepo.saveAll(List.of(
                criarNotificacao(
                        subprocessoAtivo,
                        "Notificação enviada do processo ativo",
                        SituacaoNotificacao.ENVIADO,
                        LocalDateTime.now().minusHours(3),
                        null,
                        1,
                        null,
                        LocalDateTime.now().minusHours(2)
                ),
                criarNotificacao(
                        subprocessoAtivo,
                        "Notificação com falha definitiva",
                        SituacaoNotificacao.FALHA_DEFINITIVA,
                        LocalDateTime.now().minusHours(1),
                        "SMTP indisponível",
                        5,
                        null,
                        null
                ),
                criarNotificacao(
                        subprocessoFinalizado,
                        "Notificação de processo finalizado",
                        SituacaoNotificacao.ENVIADO,
                        LocalDateTime.now().minusMinutes(30),
                        null,
                        1,
                        null,
                        LocalDateTime.now().minusMinutes(20)
                )
        ));
        codigoFalhaDefinitiva = notificacoesSalvas.stream()
                .filter(notificacao -> notificacao.getSituacao() == SituacaoNotificacao.FALHA_DEFINITIVA)
                .map(NotificacaoEmail::getCodigo)
                .findFirst()
                .orElseThrow();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve listar notificações administrativas como ADMIN")
    @WithMockAdmin
    void listarTodasAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/notificacoes/listar").param("limite", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).contains("Notificação enviada do processo ativo");
                    assertThat(body).contains("Notificação com falha definitiva");
                    assertThat(body).contains("Notificação de processo finalizado");
                });
    }

    @Test
    @DisplayName("Deve resumir notificações de subprocessos de processos ativos")
    void listarResumoSubprocessosAtivos() {
        List<NotificacaoSubprocessoResumoDto> resumo = notificacaoService.listarResumoSubprocessosAtivos();

        assertThat(resumo)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.subprocessoCodigo()).isEqualTo(subprocessoAtivo.getCodigo());
                    assertThat(item.processoDescricao()).isEqualTo("Processo ativo notificações");
                    assertThat(item.totalNotificacoes()).isEqualTo(2);
                    assertThat(item.enviadas()).isEqualTo(1);
                    assertThat(item.falhasDefinitivas()).isEqualTo(1);
                    assertThat(item.statusGeral().name()).isEqualTo("FALHA_DEFINITIVA");
                    assertThat(item.ultimoErro()).isEqualTo("SMTP indisponível");
                    assertThat(item.podeReenviar()).isTrue();
                });
    }

    @Test
    @DisplayName("Deve reenfileirar falhas definitivas por subprocesso")
    void reenfileirarFalhasDefinitivasPorSubprocesso() {
        int reenfileiradas = notificacaoService.reenfileirarFalhasDefinitivasPorSubprocesso(subprocessoAtivo.getCodigo());
        entityManager.flush();
        entityManager.clear();

        NotificacaoEmail notificacaoAtualizada = notificacaoEmailRepo.findById(codigoFalhaDefinitiva).orElseThrow();

        assertThat(reenfileiradas).isEqualTo(1);
        assertThat(notificacaoAtualizada.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
        assertThat(notificacaoAtualizada.getTentativas()).isZero();
        assertThat(notificacaoAtualizada.getProximaTentativaEm()).isNotNull();
        assertThat(notificacaoAtualizada.getDataHoraEnvio()).isNull();
        assertThat(notificacaoAtualizada.getUltimoErro()).isNull();
    }

    @Test
    @DisplayName("Deve reenviar notificação específica por código como ADMIN")
    @WithMockAdmin
    void reenviarPorCodigo() throws Exception {
        String corpo = mockMvc.perform(post("/api/admin/notificacoes/{codigo}/reenviar", codigoFalhaDefinitiva))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        NotificacaoEmail notificacaoAtualizada = notificacaoEmailRepo.findById(codigoFalhaDefinitiva).orElseThrow();

        assertThat(objectMapper.readTree(corpo).get("codigo").asLong()).isEqualTo(codigoFalhaDefinitiva);
        assertThat(objectMapper.readTree(corpo).get("reenfileiradas").asInt()).isEqualTo(1);
        assertThat(notificacaoAtualizada.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
        assertThat(notificacaoAtualizada.getTentativas()).isZero();
        assertThat(notificacaoAtualizada.getProximaTentativaEm()).isNotNull();
        assertThat(notificacaoAtualizada.getDataHoraEnvio()).isNull();
        assertThat(notificacaoAtualizada.getUltimoErro()).isNull();
    }

    @Test
    @DisplayName("Deve listar notificações por subprocesso quando o endpoint existe")
    @WithMockAdmin
    void listarPorSubprocesso() throws Exception {
        String corpo = mockMvc.perform(get("/api/subprocessos/{codigo}/notificacoes-email", subprocessoAtivo.getCodigo())
                        .param("limite", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Long> subprocessos = new ArrayList<>();
        List<String> assuntos = new ArrayList<>();
        objectMapper.readTree(corpo).forEach(item -> {
            subprocessos.add(item.get("subprocessoCodigo").asLong());
            assuntos.add(item.get("assunto").stringValue());
        });

        assertThat(subprocessos)
                .hasSize(2)
                .containsOnly(subprocessoAtivo.getCodigo());
        assertThat(assuntos)
                .contains("Notificação enviada do processo ativo", "Notificação com falha definitiva")
                .doesNotContain("Notificação de processo finalizado");
    }

    private NotificacaoEmail criarNotificacao(
            Subprocesso subprocesso,
            String assunto,
            SituacaoNotificacao situacao,
            LocalDateTime dataHoraCriacao,
            String ultimoErro,
            int tentativas,
            LocalDateTime proximaTentativaEm,
            LocalDateTime dataHoraEnvio
    ) {
        return NotificacaoEmail.builder()
                .subprocesso(subprocesso)
                .tipoNotificacao(TipoNotificacao.PROCESSO_INICIADO)
                .usuarioDestinoTitulo(null)
                .unidadeDestinoSigla(subprocesso.getUnidade().getSigla())
                .destinatario("destino@tre-pe.jus.br")
                .assunto(assunto)
                .corpoHtml("<p>teste</p>")
                .situacao(situacao)
                .tentativas(tentativas)
                .proximaTentativaEm(proximaTentativaEm)
                .dataHoraCriacao(dataHoraCriacao)
                .dataHoraEnvio(dataHoraEnvio)
                .ultimoErro(ultimoErro)
                .chaveIdempotencia(UUID.randomUUID().toString())
                .build();
    }
}

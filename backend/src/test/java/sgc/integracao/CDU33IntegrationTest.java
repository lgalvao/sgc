package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.ComumDtos.*;
import sgc.comum.*;
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
    private NotificacaoEmailRepo notificacaoEmailRepo;

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

        Unidade unidade = buscarOuCriarUnidadeComDoisSuperiores("CDU33");

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
        List<Movimentacao> movimentacoes = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(codSp);
        assertThat(movimentacoes).isNotEmpty();
        boolean movimentacaoExiste = movimentacoes.stream()
                .anyMatch(m -> m.getDescricao().contains(Mensagens.HIST_REVISAO_REABERTA));

        assertThat(movimentacaoExiste).isTrue();

        Movimentacao movimentacao = movimentacoes.stream()
                .filter(m -> m.getDescricao().contains(Mensagens.HIST_REVISAO_REABERTA))
                .findFirst()
                .orElseThrow();
        assertThat(movimentacao.getUnidadeOrigem().getSigla()).isEqualTo("ADMIN");
        assertThat(movimentacao.getUnidadeDestino().getSigla()).isEqualTo(spReaberto.getUnidade().getSigla());

        // Verificar se foi criado um alerta
        List<Alerta> alerts = alertaRepo.findAll();
        assertThat(alerts).isNotEmpty();
        boolean alertaExiste = alerts.stream().anyMatch(a -> {
            Long unidadeDestinoCodigo = a.getUnidadeDestino().getCodigo();
            Long unidadeSpReabertoCodigo = spReaberto.getUnidade().getCodigo();
            return Objects.equals(unidadeDestinoCodigo, unidadeSpReabertoCodigo) &&
                    a.getDescricao().contains("Revisão de cadastro da unidade %s reaberta pela ADMIN. Justificativa: %s"
                            .formatted(spReaberto.getUnidade().getSigla(), request.justificativa()));
        });

        assertThat(alertaExiste).isTrue();

        Long codigoUnidadeSuperior = Optional.ofNullable(spReaberto.getUnidade().getUnidadeSuperior())
                .map(Unidade::getCodigo)
                .orElse(null);
        assertThat(codigoUnidadeSuperior).isNotNull();

        boolean alertaSuperiorExiste = alerts.stream().anyMatch(a -> {
            Long unidadeDestinoCodigo = a.getUnidadeDestino().getCodigo();
            return Objects.equals(unidadeDestinoCodigo, codigoUnidadeSuperior) &&
                    a.getDescricao().contains("Revisão de cadastro da unidade %s reaberta pela ADMIN".formatted(spReaberto.getUnidade().getSigla()));
        });
        assertThat(alertaSuperiorExiste).isTrue();

        Long codigoSuperiorIndireto = Optional.ofNullable(spReaberto.getUnidade().getUnidadeSuperior())
                .map(Unidade::getUnidadeSuperior)
                .map(Unidade::getCodigo)
                .orElse(null);
        assertThat(codigoSuperiorIndireto).isNotNull();

        long quantidadeAlertasSuperiores = alerts.stream()
                .filter(a -> a.getUnidadeDestino() != null)
                .filter(a -> Objects.equals(a.getUnidadeDestino().getCodigo(), codigoUnidadeSuperior)
                        || Objects.equals(a.getUnidadeDestino().getCodigo(), codigoSuperiorIndireto))
                .filter(a -> a.getDescricao().contains(
                        "Revisão de cadastro da unidade %s reaberta pela ADMIN".formatted(spReaberto.getUnidade().getSigla())))
                .count();
        assertThat(quantidadeAlertasSuperiores)
                .as("CDU-33 deve criar alerta apenas para a superior direta")
                .isEqualTo(1);

        Unidade unidadeSuperior = Optional.ofNullable(spReaberto.getUnidade().getUnidadeSuperior()).orElseThrow();

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.REVISAO_CADASTRO_REABERTA)
                .toList();
        assertThat(notificacoes).hasSize(2);

        NotificacaoEmail notificacaoUnidade = notificacoes.stream()
                .filter(n -> spReaberto.getUnidade().getSigla().equals(n.getUnidadeDestinoSigla()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Notificação da unidade reaberta não encontrada"));
        assertThat(notificacaoUnidade.getAssunto())
                .isEqualTo("SGC: Reabertura de revisão de cadastro - %s".formatted(spReaberto.getUnidade().getSigla()));
        assertThat(notificacaoUnidade.getCorpoHtml())
                .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(spReaberto.getUnidade().getSigla()))
                .contains("A revisão do cadastro de atividades da sua unidade foi reaberta para ajustes")
                .contains("Processo CDU-33")
                .contains(request.justificativa());
        assertThat(notificacaoUnidade.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);

        NotificacaoEmail notificacaoSuperior = notificacoes.stream()
                .filter(n -> unidadeSuperior.getSigla().equals(n.getUnidadeDestinoSigla()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Notificação da unidade superior não encontrada"));
        assertThat(notificacaoSuperior.getAssunto())
                .isEqualTo("SGC: Reabertura de revisão de cadastro - %s".formatted(spReaberto.getUnidade().getSigla()));
        assertThat(notificacaoSuperior.getCorpoHtml())
                .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(unidadeSuperior.getSigla()))
                .contains("revisão do cadastro de atividades da unidade <strong>%s</strong> foi reaberta para ajustes"
                        .formatted(spReaberto.getUnidade().getSigla()))
                .contains("Processo CDU-33")
                .contains(request.justificativa());
        assertThat(notificacaoSuperior.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);

        aguardarEmail(2);
        assertThat(algumEmailPara(notificacaoUnidade.getDestinatario())).isTrue();
        assertThat(algumEmailPara(notificacaoSuperior.getDestinatario())).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Reabertura de revisão de cadastro - %s".formatted(spReaberto.getUnidade().getSigla()))).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Reabertura de revisão de cadastro - %s".formatted(spReaberto.getUnidade().getSigla()))).isTrue();
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

    private Unidade buscarOuCriarUnidadeComDoisSuperiores(String sufixo) {
        String siglaSecao = "SEC_T_%s".formatted(sufixo);
        Optional<Unidade> secaoExistente = unidadeRepo.findBySigla(siglaSecao);
        if (secaoExistente.isPresent()) {
            return secaoExistente.get();
        }

        Unidade secretaria = new Unidade();
        secretaria.setSigla("SECRET_T_%s".formatted(sufixo));
        secretaria.setNome("Secretaria teste %s".formatted(sufixo));
        secretaria.setSituacao(ATIVA);
        secretaria.setTipo(INTEROPERACIONAL);
        secretaria = unidadeRepo.save(secretaria);

        Unidade coordenadoria = new Unidade();
        coordenadoria.setSigla("COORD_T_%s".formatted(sufixo));
        coordenadoria.setNome("Coordenadoria teste %s".formatted(sufixo));
        coordenadoria.setSituacao(ATIVA);
        coordenadoria.setTipo(INTERMEDIARIA);
        coordenadoria.setUnidadeSuperior(secretaria);
        coordenadoria = unidadeRepo.save(coordenadoria);

        Unidade secao = new Unidade();
        secao.setSigla(siglaSecao);
        secao.setNome("Seção teste %s".formatted(sufixo));
        secao.setSituacao(ATIVA);
        secao.setTipo(OPERACIONAL);
        secao.setUnidadeSuperior(coordenadoria);
        return unidadeRepo.save(secao);
    }
}

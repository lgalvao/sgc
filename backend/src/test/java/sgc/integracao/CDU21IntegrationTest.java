package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.RealEventListenerConfig;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.model.MapaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.api.ResponsavelDto;
import sgc.sgrh.api.UsuarioDto;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioRepo;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.subprocesso.internal.service.SubprocessoNotificacaoService;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("integration-test")
@Import({TestSecurityConfig.class, RealEventListenerConfig.class})
@DisplayName("CDU-21: Finalizar Processo")
class CDU21IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;

    @MockitoBean
    private SgrhService sgrhService;

    @MockitoBean
    private SubprocessoNotificacaoService subprocessoNotificacaoService;

    @MockitoBean
    private NotificacaoEmailService notificacaoEmailService;

    private Processo processo;
    private Unidade unidadeIntermediaria;
    private Unidade unidadeOperacional1;
    private Unidade unidadeOperacional2;
    private Usuario titularIntermediaria;
    private Usuario titularOp1;
    private Usuario titularOp2;

    @BeforeEach
    void setUp() {
        // Criar hierarquia de unidades via Fixture
        // Unidade intermediária
        unidadeIntermediaria = UnidadeFixture.unidadePadrao();
        unidadeIntermediaria.setCodigo(null);
        unidadeIntermediaria.setNome("Coordenadoria CDU-21");
        unidadeIntermediaria.setSigla("COORD21");
        unidadeIntermediaria = unidadeRepo.save(unidadeIntermediaria);

        // Unidades operacionais subordinadas
        unidadeOperacional1 = UnidadeFixture.unidadePadrao();
        unidadeOperacional1.setCodigo(null);
        unidadeOperacional1.setNome("Seção Op1 CDU-21");
        unidadeOperacional1.setSigla("OP1-21");
        unidadeOperacional1.setUnidadeSuperior(unidadeIntermediaria);
        unidadeOperacional1 = unidadeRepo.save(unidadeOperacional1);

        unidadeOperacional2 = UnidadeFixture.unidadePadrao();
        unidadeOperacional2.setCodigo(null);
        unidadeOperacional2.setNome("Seção Op2 CDU-21");
        unidadeOperacional2.setSigla("OP2-21");
        unidadeOperacional2.setUnidadeSuperior(unidadeIntermediaria);
        unidadeOperacional2 = unidadeRepo.save(unidadeOperacional2);

        // Criar Usuários via Fixture
        titularIntermediaria = UsuarioFixture.usuarioComTitulo("111111111111");
        titularIntermediaria.setNome("Titular Intermediaria");
        titularIntermediaria.setEmail("titular.intermediaria@test.com");
        titularIntermediaria = usuarioRepo.save(titularIntermediaria);

        titularOp1 = UsuarioFixture.usuarioComTitulo("222222222222");
        titularOp1.setNome("Titular Op1");
        titularOp1.setEmail("titular.op1@test.com");
        titularOp1 = usuarioRepo.save(titularOp1);

        titularOp2 = UsuarioFixture.usuarioComTitulo("333333333333");
        titularOp2.setNome("Titular Op2");
        titularOp2.setEmail("titular.op2@test.com");
        titularOp2 = usuarioRepo.save(titularOp2);

        // Criar Processo via Fixture
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo de Teste para Finalizar");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setParticipantes(
                new HashSet<>(
                        Set.of(unidadeIntermediaria, unidadeOperacional1, unidadeOperacional2)));
        processo = processoRepo.save(processo);

        // Criar Mapas e Subprocessos via Fixture
        Mapa mapa1 = MapaFixture.mapaPadrao(null);
        mapa1.setCodigo(null);
        mapa1 = mapaRepo.save(mapa1);

        Subprocesso sp1 = SubprocessoFixture.subprocessoPadrao(processo, unidadeOperacional1);
        sp1.setCodigo(null);
        sp1.setMapa(mapa1);
        sp1.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoRepo.save(sp1);

        Mapa mapa2 = MapaFixture.mapaPadrao(null);
        mapa2.setCodigo(null);
        mapa2 = mapaRepo.save(mapa2);

        Subprocesso sp2 = SubprocessoFixture.subprocessoPadrao(processo, unidadeOperacional2);
        sp2.setCodigo(null);
        sp2.setMapa(mapa2);
        sp2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoRepo.save(sp2);

        // Configurar mocks do SgrhService com dados dinâmicos
        when(sgrhService.buscarResponsaveisUnidades(anyList()))
                .thenReturn(
                        Map.of(
                                unidadeIntermediaria.getCodigo(),
                                new ResponsavelDto(
                                        unidadeIntermediaria.getCodigo(),
                                        titularIntermediaria.getTituloEleitoral(),
                                        titularIntermediaria.getNome(),
                                        null,
                                        null),
                                unidadeOperacional1.getCodigo(),
                                new ResponsavelDto(
                                        unidadeOperacional1.getCodigo(),
                                        titularOp1.getTituloEleitoral(),
                                        titularOp1.getNome(),
                                        null,
                                        null),
                                unidadeOperacional2.getCodigo(),
                                new ResponsavelDto(
                                        unidadeOperacional2.getCodigo(),
                                        titularOp2.getTituloEleitoral(),
                                        titularOp2.getNome(),
                                        null,
                                        null)));
        when(sgrhService.buscarUsuariosPorTitulos(anyList()))
                .thenReturn(
                        Map.of(
                                titularIntermediaria.getTituloEleitoral(),
                                UsuarioDto.builder()
                                        .tituloEleitoral(titularIntermediaria.getTituloEleitoral())
                                        .nome(titularIntermediaria.getNome())
                                        .email(titularIntermediaria.getEmail())
                                        .matricula("123")
                                        .build(),
                                titularOp1.getTituloEleitoral(),
                                UsuarioDto.builder()
                                        .tituloEleitoral(titularOp1.getTituloEleitoral())
                                        .nome(titularOp1.getNome())
                                        .email(titularOp1.getEmail())
                                        .matricula("456")
                                        .build(),
                                titularOp2.getTituloEleitoral(),
                                UsuarioDto.builder()
                                        .tituloEleitoral(titularOp2.getTituloEleitoral())
                                        .nome(titularOp2.getNome())
                                        .email(titularOp2.getEmail())
                                        .matricula("789")
                                        .build()));
    }

    @Test
    @WithMockAdmin
    @DisplayName(
            "Deve finalizar processo, atualizar status, tornar mapas vigentes e notificar todas as"
                    + " unidades corretamente")
    void finalizarProcesso_ComSucesso_DeveAtualizarStatusENotificarUnidades() throws Exception {
        mockMvc.perform(post("/api/processos/{id}/finalizar", processo.getCodigo()).with(csrf()))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Processo processoFinalizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(processoFinalizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(processoFinalizado.getDataFinalizacao()).isNotNull();
        
        // Aguarda processamento assíncrono de eventos antes de verificar chamadas ao mock
        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> verify(notificacaoEmailService, times(3)).enviarEmailHtml(anyString(), anyString(), anyString()));
    }

    @Test
    @WithMockAdmin
    @DisplayName(
            "Não deve finalizar processo se houver subprocessos pendentes e deve retornar 409"
                    + " Conflict")
    void finalizarProcesso_ComSubprocessoPendente_DeveRetornarConflito() throws Exception {
        Subprocesso spPendente =
                subprocessoRepo.findByProcessoCodigo(processo.getCodigo()).stream()
                        .filter(
                                s ->
                                        s.getUnidade()
                                                .getCodigo()
                                                .equals(unidadeOperacional1.getCodigo()))
                        .findFirst()
                        .orElseThrow();
        spPendente.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(spPendente);

        mockMvc.perform(post("/api/processos/{id}/finalizar", processo.getCodigo()).with(csrf()))
                .andExpect(status().isConflict());

        entityManager.flush();
        entityManager.clear();

        Processo processoNaoFinalizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(processoNaoFinalizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
    }
}

package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoNotificacaoService;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
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
    private Unidade unidadeOperacional1;
    private Unidade unidadeOperacional2;

    @BeforeEach
    void setUp() {

        Usuario titularIntermediaria = usuarioRepo.findById("1").orElseThrow();
        Usuario titularOp1 = usuarioRepo.findById("2").orElseThrow();
        Usuario titularOp2 = usuarioRepo.findById("3").orElseThrow();

        Unidade unidadeIntermediaria = unidadeRepo.findById(3L).orElseThrow();
        unidadeOperacional1 = unidadeRepo.findById(5L).orElseThrow();
        unidadeOperacional2 = unidadeRepo.findById(4L).orElseThrow();

        processo =
                new Processo(
                        "Processo de Teste para Finalizar",
                        TipoProcesso.MAPEAMENTO,
                        SituacaoProcesso.EM_ANDAMENTO,
                        LocalDateTime.now().plusDays(30));
        processo.setParticipantes(
                new HashSet<>(
                        Set.of(unidadeIntermediaria, unidadeOperacional1, unidadeOperacional2)));
        processoRepo.save(processo);

        Mapa mapa1 = mapaRepo.save(new Mapa());
        Subprocesso sp1 =
                new Subprocesso(
                        processo,
                        unidadeOperacional1,
                        mapa1,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        processo.getDataLimite());
        subprocessoRepo.save(sp1);

        Mapa mapa2 = mapaRepo.save(new Mapa());
        Subprocesso sp2 =
                new Subprocesso(
                        processo,
                        unidadeOperacional2,
                        mapa2,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        processo.getDataLimite());
        subprocessoRepo.save(sp2);

        when(sgrhService.buscarResponsaveisUnidades(anyList()))
                .thenReturn(
                        Map.of(
                                unidadeIntermediaria.getCodigo(),
                                new ResponsavelDto(
                                        unidadeIntermediaria.getCodigo(),
                                        String.valueOf(
                                                titularIntermediaria.getTituloEleitoral()),
                                        "Titular Intermediaria",
                                        null,
                                        null),
                                unidadeOperacional1.getCodigo(),
                                new ResponsavelDto(
                                        unidadeOperacional1.getCodigo(),
                                        String.valueOf(titularOp1.getTituloEleitoral()),
                                        "Titular Op1",
                                        null,
                                        null),
                                unidadeOperacional2.getCodigo(),
                                new ResponsavelDto(
                                        unidadeOperacional2.getCodigo(),
                                        String.valueOf(titularOp2.getTituloEleitoral()),
                                        "Titular Op2",
                                        null,
                                        null)));
        when(sgrhService.buscarUsuariosPorTitulos(anyList()))
                .thenReturn(
                        Map.of(
                                "1",
                                UsuarioDto.builder()
                                        .codigo("1")
                                        .tituloEleitoral("1")
                                        .nome("Titular Intermediaria")
                                        .email("titular.intermediaria@test.com")
                                        .matricula("123")
                                        .build(),
                                "2",
                                UsuarioDto.builder()
                                        .codigo("2")
                                        .tituloEleitoral("2")
                                        .nome("Titular Op1")
                                        .email("titular.op1@test.com")
                                        .matricula("123")
                                        .build(),
                                "3",
                                UsuarioDto.builder()
                                        .codigo("3")
                                        .tituloEleitoral("3")
                                        .nome("Titular Op2")
                                        .email("titular.op2@test.com")
                                        .matricula("123")
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
        verify(notificacaoEmailService, times(3)).enviarEmailHtml(anyString(), anyString(), anyString());
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

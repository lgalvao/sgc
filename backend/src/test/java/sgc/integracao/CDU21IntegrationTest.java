package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoNotificacaoService;
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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("CDU-21: Finalizar Processo")
class CDU21IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @MockitoBean
    private ProcessoNotificacaoService processoNotificacaoService;

    @MockitoBean
    private SgrhService sgrhService;

    @MockitoBean
    private SubprocessoNotificacaoService subprocessoNotificacaoService;

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

        processo = new Processo("Processo de Teste para Finalizar", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(30));
        processo.setParticipantes(new HashSet<>(Set.of(unidadeIntermediaria, unidadeOperacional1, unidadeOperacional2)));
        processoRepo.save(processo);

        Mapa mapa1 = mapaRepo.save(new Mapa());
        Subprocesso sp1 = new Subprocesso(processo, unidadeOperacional1, mapa1, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite());
        subprocessoRepo.save(sp1);

        Mapa mapa2 = mapaRepo.save(new Mapa());
        Subprocesso sp2 = new Subprocesso(processo, unidadeOperacional2, mapa2, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite());
        subprocessoRepo.save(sp2);

        when(sgrhService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            unidadeIntermediaria.getCodigo(), new ResponsavelDto(unidadeIntermediaria.getCodigo(), String.valueOf(titularIntermediaria.getTituloEleitoral()), "Titular Intermediaria", null, null),
            unidadeOperacional1.getCodigo(), new ResponsavelDto(unidadeOperacional1.getCodigo(), String.valueOf(titularOp1.getTituloEleitoral()), "Titular Op1", null, null),
            unidadeOperacional2.getCodigo(), new ResponsavelDto(unidadeOperacional2.getCodigo(), String.valueOf(titularOp2.getTituloEleitoral()), "Titular Op2", null, null)
        ));
        when(sgrhService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
            "1", new UsuarioDto("1", "Titular Intermediaria", "titular.intermediaria@test.com", "123", "Cargo"),
            "2", new UsuarioDto("2", "Titular Op1", "titular.op1@test.com", "123", "Cargo"),
            "3", new UsuarioDto("3", "Titular Op2", "titular.op2@test.com", "123", "Cargo")
        ));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve finalizar processo, atualizar status, tornar mapas vigentes e notificar todas as unidades corretamente")
    void finalizarProcesso_ComSucesso_DeveAtualizarStatusENotificarUnidades() throws Exception {
        mockMvc.perform(post("/api/processos/{id}/finalizar", processo.getCodigo())
                .with(csrf()))
            .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Processo processoFinalizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(processoFinalizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(processoFinalizado.getDataFinalizacao()).isNotNull();

        Unidade un1 = unidadeRepo.findById(unidadeOperacional1.getCodigo()).orElseThrow();
        Subprocesso sp1 = subprocessoRepo.findByProcessoCodigo(processo.getCodigo()).stream().filter(s -> s.getUnidade().getCodigo().equals(unidadeOperacional1.getCodigo())).findFirst().orElseThrow();
        assertThat(un1.getMapaVigente().getCodigo()).isEqualTo(sp1.getMapa().getCodigo());
        assertThat(un1.getDataVigenciaMapa()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        Unidade un2 = unidadeRepo.findById(unidadeOperacional2.getCodigo()).orElseThrow();
        Subprocesso sp2 = subprocessoRepo.findByProcessoCodigo(processo.getCodigo()).stream().filter(s -> s.getUnidade().getCodigo().equals(unidadeOperacional2.getCodigo())).findFirst().orElseThrow();
        assertThat(un2.getMapaVigente().getCodigo()).isEqualTo(sp2.getMapa().getCodigo());

        verify(processoNotificacaoService, times(1)).enviarNotificacoesDeFinalizacao(eq(processo), anyList());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Não deve finalizar processo se houver subprocessos pendentes e deve retornar 409 Conflict")
    void finalizarProcesso_ComSubprocessoPendente_DeveRetornarConflito() throws Exception {
        Subprocesso spPendente = subprocessoRepo.findByProcessoCodigo(processo.getCodigo()).stream()
                .filter(s -> s.getUnidade().getCodigo().equals(unidadeOperacional1.getCodigo()))
                .findFirst().orElseThrow();
        spPendente.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(spPendente);

        mockMvc.perform(post("/api/processos/{id}/finalizar", processo.getCodigo())
                        .with(csrf()))
                .andExpect(status().isConflict());

        entityManager.flush();
        entityManager.clear();

        Processo processoNaoFinalizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(processoNaoFinalizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        Unidade u1 = unidadeRepo.findById(unidadeOperacional1.getCodigo()).orElseThrow();
        Unidade u2 = unidadeRepo.findById(unidadeOperacional2.getCodigo()).orElseThrow();
        assertThat(u1.getMapaVigente()).isNull();
        assertThat(u2.getMapaVigente()).isNull();

    }
}

package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoService;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.*;
import sgc.sgrh.Perfil;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.SituacaoUnidade;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
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

    // Repositories
    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;
    @Autowired private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired private UnidadeProcessoRepo unidadeProcessoRepo;
    @Autowired private UsuarioRepo usuarioRepo;

    @MockitoBean
    private SgrhService sgrhService;

    @MockitoBean
    private NotificacaoService notificacaoService;

    private Processo processo;
    private Unidade unidadeOperacional1;
    private Unidade unidadeOperacional2;

    @BeforeEach
    void setUp() {
        // 1. Setup Mocks
        doNothing().when(notificacaoService).enviarEmailHtml(anyString(), anyString(), anyString());

        // 2. Create Users
        Usuario titularIntermediaria = new Usuario(101010101010L, "Titular Intermediaria", "titular.intermediaria@test.com", null, null, Set.of(Perfil.CHEFE));
        Usuario titularOp1 = new Usuario(202020202020L, "Titular Op1", "titular.op1@test.com", null, null, Set.of(Perfil.CHEFE));
        Usuario titularOp2 = new Usuario(303030303030L, "Titular Op2", "titular.op2@test.com", null, null, Set.of(Perfil.CHEFE));
        usuarioRepo.saveAll(List.of(titularIntermediaria, titularOp1, titularOp2));

        // 3. Create Units
        Unidade unidadeIntermediaria = new Unidade("Unidade Intermediária", "UINT", titularIntermediaria, TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, null);
        unidadeRepo.save(unidadeIntermediaria);

        unidadeOperacional1 = new Unidade("Unidade Operacional 1", "UOP1", titularOp1, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, unidadeIntermediaria);
        unidadeOperacional2 = new Unidade("Unidade Operacional 2", "UOP2", titularOp2, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, unidadeIntermediaria);
        unidadeRepo.saveAll(List.of(unidadeOperacional1, unidadeOperacional2));

        // 4. Create Process
        processo = processoRepo.save(new Processo("Processo de Teste para Finalizar", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(30)));

        // 5. Create Subprocesses and Maps
        Mapa mapa1 = mapaRepo.save(new Mapa());
        Subprocesso sp1 = new Subprocesso(processo, unidadeOperacional1, mapa1, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite());
        subprocessoRepo.save(sp1);

        Mapa mapa2 = mapaRepo.save(new Mapa());
        Subprocesso sp2 = new Subprocesso(processo, unidadeOperacional2, mapa2, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite());
        subprocessoRepo.save(sp2);

        // 6. Create UnidadeProcesso snapshots
        unidadeProcessoRepo.save(new UnidadeProcesso(processo.getCodigo(), unidadeIntermediaria.getCodigo(), unidadeIntermediaria.getNome(), unidadeIntermediaria.getSigla(), String.valueOf(titularIntermediaria.getTituloEleitoral()), unidadeIntermediaria.getTipo(), "PARTICIPANTE", null));
        unidadeProcessoRepo.save(new UnidadeProcesso(processo.getCodigo(), unidadeOperacional1.getCodigo(), unidadeOperacional1.getNome(), unidadeOperacional1.getSigla(), String.valueOf(titularOp1.getTituloEleitoral()), unidadeOperacional1.getTipo(), "HOMOLOGADO", unidadeIntermediaria.getCodigo()));
        unidadeProcessoRepo.save(new UnidadeProcesso(processo.getCodigo(), unidadeOperacional2.getCodigo(), unidadeOperacional2.getNome(), unidadeOperacional2.getSigla(), String.valueOf(titularOp2.getTituloEleitoral()), unidadeOperacional2.getTipo(), "HOMOLOGADO", unidadeIntermediaria.getCodigo()));

        // 7. Mock SGRH Service
        when(sgrhService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            unidadeIntermediaria.getCodigo(), new ResponsavelDto(unidadeIntermediaria.getCodigo(), String.valueOf(titularIntermediaria.getTituloEleitoral()), "Titular Intermediaria", null, null),
            unidadeOperacional1.getCodigo(), new ResponsavelDto(unidadeOperacional1.getCodigo(), String.valueOf(titularOp1.getTituloEleitoral()), "Titular Op1", null, null),
            unidadeOperacional2.getCodigo(), new ResponsavelDto(unidadeOperacional2.getCodigo(), String.valueOf(titularOp2.getTituloEleitoral()), "Titular Op2", null, null)
        ));
        when(sgrhService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
            String.valueOf(101010101010L), new UsuarioDto("101010101010", "Titular Intermediaria", "titular.intermediaria@test.com", "123", "Cargo"),
            String.valueOf(202020202020L), new UsuarioDto("202020202020", "Titular Op1", "titular.op1@test.com", "123", "Cargo"),
            String.valueOf(303030303030L), new UsuarioDto("303030303030", "Titular Op2", "titular.op2@test.com", "123", "Cargo")
        ));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve finalizar processo, atualizar status, tornar mapas vigentes e notificar todas as unidades corretamente")
    void finalizarProcesso_ComSucesso_DeveAtualizarStatusENotificarUnidades() throws Exception {
        // Arrange
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        mockMvc.perform(post("/api/processos/{id}/finalizar", processo.getCodigo())
                .with(csrf()))
            .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Assert Process
        Processo processoFinalizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(processoFinalizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(processoFinalizado.getDataFinalizacao()).isNotNull();

        // Assert Maps
        UnidadeMapa um1 = unidadeMapaRepo.findByUnidadeCodigo(unidadeOperacional1.getCodigo()).orElseThrow();
        Subprocesso sp1 = subprocessoRepo.findByProcessoCodigo(processo.getCodigo()).stream().filter(s -> s.getUnidade().getCodigo().equals(unidadeOperacional1.getCodigo())).findFirst().orElseThrow();
        assertThat(um1.getMapaVigenteCodigo()).isEqualTo(sp1.getMapa().getCodigo());
        assertThat(um1.getDataVigencia()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        UnidadeMapa um2 = unidadeMapaRepo.findByUnidadeCodigo(unidadeOperacional2.getCodigo()).orElseThrow();
        Subprocesso sp2 = subprocessoRepo.findByProcessoCodigo(processo.getCodigo()).stream().filter(s -> s.getUnidade().getCodigo().equals(unidadeOperacional2.getCodigo())).findFirst().orElseThrow();
        assertThat(um2.getMapaVigenteCodigo()).isEqualTo(sp2.getMapa().getCodigo());

        // Assert Notifications
        verify(notificacaoService, times(3)).enviarEmailHtml(emailCaptor.capture(), subjectCaptor.capture(), bodyCaptor.capture());

        List<String> allEmails = emailCaptor.getAllValues();
        List<String> allSubjects = subjectCaptor.getAllValues();
        List<String> allBodies = bodyCaptor.getAllValues();

        // Email para Unidade Operacional 1
        int indexOp1 = allEmails.indexOf("titular.op1@test.com");
        assertThat(indexOp1).isGreaterThanOrEqualTo(0);
        assertThat(allSubjects.get(indexOp1)).isEqualTo("SGC: Conclusão do processo " + processo.getDescricao());
        assertThat(allBodies.get(indexOp1)).contains("<p>Prezado(a) responsável pela <span>UOP1</span>,</p>");
        assertThat(allBodies.get(indexOp1)).contains("<p>Comunicamos a conclusão do processo <strong>Processo de Teste para Finalizar</strong> para a sua unidade.</p>");
        assertThat(allBodies.get(indexOp1)).doesNotContain("para as unidades:");

        // Email para Unidade Operacional 2
        int indexOp2 = allEmails.indexOf("titular.op2@test.com");
        assertThat(indexOp2).isGreaterThanOrEqualTo(0);
        assertThat(allSubjects.get(indexOp2)).isEqualTo("SGC: Conclusão do processo " + processo.getDescricao());
        assertThat(allBodies.get(indexOp2)).contains("<p>Prezado(a) responsável pela <span>UOP2</span>,</p>");

        // Email para Unidade Intermediária
        int indexIntermediaria = allEmails.indexOf("titular.intermediaria@test.com");
        assertThat(indexIntermediaria).isGreaterThanOrEqualTo(0);
        assertThat(allSubjects.get(indexIntermediaria)).isEqualTo("SGC: Conclusão do processo " + processo.getDescricao() + " em unidades subordinadas");
        assertThat(allBodies.get(indexIntermediaria)).contains("<p>Prezado(a) responsável pela <span>UINT</span>,</p>");
        assertThat(allBodies.get(indexIntermediaria)).contains("<p>Comunicamos a conclusão do processo <strong>Processo de Teste para Finalizar</strong> para as unidades:</p>");
        assertThat(allBodies.get(indexIntermediaria)).contains("<li>UOP1</li>");
        assertThat(allBodies.get(indexIntermediaria)).contains("<li>UOP2</li>");
    }

    @Test
    @WithMockAdmin
    @DisplayName("Não deve finalizar processo se houver subprocessos pendentes e deve retornar 409 Conflict")
    void finalizarProcesso_ComSubprocessoPendente_DeveRetornarConflito() throws Exception {
        // Arrange: Alterar um dos subprocessos para um estado não homologado
        Subprocesso spPendente = subprocessoRepo.findByProcessoCodigo(processo.getCodigo()).stream()
                .filter(s -> s.getUnidade().getCodigo().equals(unidadeOperacional1.getCodigo()))
                .findFirst().orElseThrow();
        spPendente.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(spPendente);

        // Act
        mockMvc.perform(post("/api/processos/{id}/finalizar", processo.getCodigo())
                        .with(csrf()))
                .andExpect(status().isConflict()); // Espera status 409 Conflict

        entityManager.flush();
        entityManager.clear();

        // Assert: Verificar que o status do processo principal não foi alterado
        Processo processoNaoFinalizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(processoNaoFinalizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        // Verificar que nenhum mapa se tornou vigente
        assertThat(unidadeMapaRepo.findByUnidadeCodigo(unidadeOperacional1.getCodigo())).isEmpty();
        assertThat(unidadeMapaRepo.findByUnidadeCodigo(unidadeOperacional2.getCodigo())).isEmpty();

        // Verificar que nenhuma notificação foi enviada
        verify(notificacaoService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
    }
}
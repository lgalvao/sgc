
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        Usuario titularIntermediaria = usuarioRepo.save(new Usuario("T01", "Titular Intermediaria", "titular.intermediaria@test.com", null, null, null));
        Usuario titularOp1 = usuarioRepo.save(new Usuario("T02", "Titular Op1", "titular.op1@test.com", null, null, null));
        Usuario titularOp2 = usuarioRepo.save(new Usuario("T03", "Titular Op2", "titular.op2@test.com", null, null, null));

        // 3. Create Units
        Unidade unidadeIntermediaria = unidadeRepo.save(new Unidade("Unidade Intermediária", "UINT", titularIntermediaria, TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, null));
        unidadeOperacional1 = unidadeRepo.save(new Unidade("Unidade Operacional 1", "UOP1", titularOp1, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, unidadeIntermediaria));
        unidadeOperacional2 = unidadeRepo.save(new Unidade("Unidade Operacional 2", "UOP2", titularOp2, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, unidadeIntermediaria));

        // 4. Create Process
        processo = processoRepo.save(new Processo("Processo de Teste para Finalizar", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now().plusDays(30)));

        // 5. Create Subprocesses and Maps
        Mapa mapa1 = mapaRepo.save(new Mapa());
        subprocessoRepo.save(new Subprocesso(processo, unidadeOperacional1, mapa1, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite()));

        Mapa mapa2 = mapaRepo.save(new Mapa());
        subprocessoRepo.save(new Subprocesso(processo, unidadeOperacional2, mapa2, SituacaoSubprocesso.MAPA_HOMOLOGADO, processo.getDataLimite()));

        // 6. Create UnidadeProcesso snapshots
        unidadeProcessoRepo.save(new UnidadeProcesso(processo.getCodigo(), unidadeIntermediaria.getCodigo(), unidadeIntermediaria.getNome(), unidadeIntermediaria.getSigla(), titularIntermediaria.getTitulo(), unidadeIntermediaria.getTipo(), "PARTICIPANTE", null));
        unidadeProcessoRepo.save(new UnidadeProcesso(processo.getCodigo(), unidadeOperacional1.getCodigo(), unidadeOperacional1.getNome(), unidadeOperacional1.getSigla(), titularOp1.getTitulo(), unidadeOperacional1.getTipo(), "HOMOLOGADO", unidadeIntermediaria.getCodigo()));
        unidadeProcessoRepo.save(new UnidadeProcesso(processo.getCodigo(), unidadeOperacional2.getCodigo(), unidadeOperacional2.getNome(), unidadeOperacional2.getSigla(), titularOp2.getTitulo(), unidadeOperacional2.getTipo(), "HOMOLOGADO", unidadeIntermediaria.getCodigo()));

        // 7. Mock SGRH Service
        when(sgrhService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(
            unidadeIntermediaria.getCodigo(), new ResponsavelDto(unidadeIntermediaria.getCodigo(), titularIntermediaria.getTitulo(), "Titular Intermediaria", null, null),
            unidadeOperacional1.getCodigo(), new ResponsavelDto(unidadeOperacional1.getCodigo(), titularOp1.getTitulo(), "Titular Op1", null, null),
            unidadeOperacional2.getCodigo(), new ResponsavelDto(unidadeOperacional2.getCodigo(), titularOp2.getTitulo(), "Titular Op2", null, null)
        ));
        when(sgrhService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of(
            "T01", new UsuarioDto("T01", "Titular Intermediaria", "titular.intermediaria@test.com", "123", "Cargo"),
            "T02", new UsuarioDto("T02", "Titular Op1", "titular.op1@test.com", "123", "Cargo"),
            "T03", new UsuarioDto("T03", "Titular Op2", "titular.op2@test.com", "123", "Cargo")
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
        assertThat(um1.getDataVigencia()).isEqualTo(LocalDate.now());

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
        assertThat(allBodies.get(indexOp1)).contains("Prezado(a) responsável pela UOP1");
        assertThat(allBodies.get(indexOp1)).contains("conclusão do processo <strong>" + processo.getDescricao() + "</strong> para a sua unidade.");
        assertThat(allBodies.get(indexOp1)).doesNotContain("para as unidades:");

        // Email para Unidade Operacional 2
        int indexOp2 = allEmails.indexOf("titular.op2@test.com");
        assertThat(indexOp2).isGreaterThanOrEqualTo(0);
        assertThat(allSubjects.get(indexOp2)).isEqualTo("SGC: Conclusão do processo " + processo.getDescricao());
        assertThat(allBodies.get(indexOp2)).contains("Prezado(a) responsável pela UOP2");

        // Email para Unidade Intermediária
        int indexIntermediaria = allEmails.indexOf("titular.intermediaria@test.com");
        assertThat(indexIntermediaria).isGreaterThanOrEqualTo(0);
        assertThat(allSubjects.get(indexIntermediaria)).isEqualTo("SGC: Conclusão do processo " + processo.getDescricao() + " em unidades subordinadas");
        assertThat(allBodies.get(indexIntermediaria)).contains("Prezado(a) responsável pela UINT");
        assertThat(allBodies.get(indexIntermediaria)).contains("para as unidades:");
        assertThat(allBodies.get(indexIntermediaria)).contains("<li>UOP1</li>");
        assertThat(allBodies.get(indexIntermediaria)).contains("<li>UOP2</li>");
    }
}

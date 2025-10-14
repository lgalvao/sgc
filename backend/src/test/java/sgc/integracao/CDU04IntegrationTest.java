package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.comum.modelo.Administrador;
import sgc.comum.modelo.AdministradorRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.notificacao.NotificacaoService;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.SituacaoUnidade;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("CDU-04: Iniciar processo de mapeamento")
@Import(TestSecurityConfig.class)
class CDU04IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private AdministradorRepo administradorRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SgrhService sgrhService;

    @MockitoBean
    private NotificacaoService notificacaoService;

    private Processo processo;
    private Unidade unidadeIntermediaria, unidadeOperacional, unidadeInteroperacional;

    @BeforeEach
    void setUp() {
        Usuario adminUser = new Usuario();
        adminUser.setTitulo("admin");
        adminUser.setNome("Admin User");
        usuarioRepo.save(adminUser);
        administradorRepo.save(new Administrador(adminUser.getTitulo(), adminUser));

        when(sgrhService.buscarUnidadePorCodigo(anyLong())).thenAnswer(i -> {
            Long id = i.getArgument(0);
            Unidade u = unidadeRepo.findById(id).orElseThrow();
            return Optional.of(new UnidadeDto(u.getCodigo(),
                    u.getNome(),
                    u.getSigla(),
                    u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getCodigo() : null,
                    u.getTipo().name()));
        });

        when(sgrhService.buscarResponsavelUnidade(anyLong())).thenAnswer(
            i -> Optional.of(
                new ResponsavelDto(
                    i.getArgument(0),
                    "T000000",
                    "Titular Fulano",
                    null,
                    null
                )
            )
        );

        when(sgrhService.buscarUsuarioPorTitulo(anyString()))
                .thenReturn(Optional.of(new UsuarioDto("T000000",
                        "Fulano de Tal",
                        "fulano@tre.jus.br",
                        "12345",
                        "Analista")));

        doNothing().when(notificacaoService).enviarEmailHtml(anyString(), anyString(), anyString());

        unidadeIntermediaria = unidadeRepo.save(new Unidade("Unidade Intermediária",
                "UINT",
                null,
                TipoUnidade.INTERMEDIARIA,
                SituacaoUnidade.ATIVA,
                null));

        unidadeOperacional = unidadeRepo.save(new Unidade("Unidade Operacional",
                "UOP",
                null,
                TipoUnidade.OPERACIONAL,
                SituacaoUnidade.ATIVA,
                unidadeIntermediaria));

        unidadeInteroperacional = unidadeRepo.save(new Unidade("Unidade Interoperacional",
                "UINTER",
                null,
                TipoUnidade.INTEROPERACIONAL,
                SituacaoUnidade.ATIVA,
                unidadeIntermediaria));

        processo = processoRepo.save(new Processo(
            "Processo de Mapeamento Teste",
            TipoProcesso.MAPEAMENTO,
            SituacaoProcesso.CRIADO,
            LocalDate.now().plusDays(30)
        ));
    }

    @Test
    @DisplayName("CDU-04: Deve iniciar processo, criar subprocessos, alertas e movimentações corretamente")
    @WithMockAdmin
    void iniciarProcesso_ComUnidadesDiversas_DeveRealizarTodasAsAcoesCorretamente() throws Exception {
        Long codProcesso = processo.getCodigo();
        mockMvc.perform(
            post("/api/processos/{id}/iniciar", codProcesso)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(unidadeIntermediaria.getCodigo(), unidadeOperacional.getCodigo(), unidadeInteroperacional.getCodigo())))
        ).andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Processo processoIniciado = processoRepo.findById(codProcesso).orElseThrow();
        assertThat(processoIniciado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(codProcesso);
        assertThat(subprocessos).hasSize(2);
        assertThat(subprocessos).extracting(s -> s.getUnidade().getSigla()).containsExactlyInAnyOrder("UOP", "UINTER");

        subprocessos.forEach(sp -> {
            assertThat(sp.getMapa()).isNotNull();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        });

        List<Movimentacao> movimentacoes = movimentacaoRepo.findAll().stream()
                .filter(m -> m.getSubprocesso().getProcesso().getCodigo().equals(codProcesso))
                .collect(Collectors.toList());

        assertThat(movimentacoes).hasSize(2);
        movimentacoes.forEach(m -> {
            assertThat(m.getDescricao()).isEqualTo("Processo iniciado");
        });

        List<Alerta> alertas = alertaRepo.findAll();
        assertThat(alertas).hasSize(3); // 1 para cada unidade folha + 1 para a intermediária
    }
}
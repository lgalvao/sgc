package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockCustomUser;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Perfil;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.dto.UsuarioDto;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("CDU-06: Detalhar processo")
public class CDU06IntegrationTest {

    private final String API_URL = "/api/processos/{id}/detalhes";
    private static final long TITULO_GESTOR = 123456789012L;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @MockitoBean
    private SgrhService sgrhService;

    // Entidades reutilizáveis
    private Processo processoMapeamento;
    private Processo processoRevisao;
    private Unidade unidadeGestora;
    private Unidade unidadeSubordinada1;
    private Unidade unidadeSubordinada2;
    private Unidade unidadeNeta;
    private Subprocesso subprocessoGestora;
    private Subprocesso subprocessoSubordinada1;
    private Subprocesso subprocessoSubordinada2;
    private Subprocesso subprocessoNeta;

    @BeforeEach
    void setUp() {
        unidadeGestora = unidadeRepo.save(new Unidade("Gestora", "UG"));
        unidadeSubordinada1 = unidadeRepo.save(new Unidade("Subordinada 1", "US1"));
        unidadeSubordinada2 = unidadeRepo.save(new Unidade("Subordinada 2", "US2"));
        unidadeNeta = unidadeRepo.save(new Unidade("Neta", "UN"));

        unidadeSubordinada1.setUnidadeSuperior(unidadeGestora);
        unidadeSubordinada2.setUnidadeSuperior(unidadeGestora);
        unidadeNeta.setUnidadeSuperior(unidadeSubordinada1);
        unidadeRepo.saveAll(List.of(unidadeSubordinada1, unidadeSubordinada2, unidadeNeta));

        processoMapeamento = processoRepo.save(
            new Processo("Processo de Mapeamento", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(10))
        );
        processoRevisao = processoRepo.save(
            new Processo("Processo de Revisão", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(10))
        );

        subprocessoGestora = new Subprocesso(processoMapeamento, unidadeGestora, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processoMapeamento.getDataLimite());
        subprocessoSubordinada1 = new Subprocesso(processoMapeamento, unidadeSubordinada1, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processoMapeamento.getDataLimite());
        subprocessoSubordinada2 = new Subprocesso(processoMapeamento, unidadeSubordinada2, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processoMapeamento.getDataLimite());
        subprocessoNeta = new Subprocesso(processoMapeamento, unidadeNeta, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processoMapeamento.getDataLimite());
        subprocessoRepo.saveAll(List.of(subprocessoGestora, subprocessoSubordinada1, subprocessoSubordinada2, subprocessoNeta));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar os detalhes do processo e a hierarquia de unidades")
    void testDetalharProcesso_sucesso() throws Exception {
        mockMvc.perform(get(API_URL, processoMapeamento.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value(processoMapeamento.getCodigo()))
            .andExpect(jsonPath("$.descricao").value("Processo de Mapeamento"))
            .andExpect(jsonPath("$.unidades.length()").value(4))
            .andExpect(jsonPath("$.unidades[0].sigla").value("UG"))
            .andExpect(jsonPath("$.unidades[1].sigla").value("US1"))
            .andExpect(jsonPath("$.unidades[2].sigla").value("US2"))
            .andExpect(jsonPath("$.unidades[3].sigla").value("UN"));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 para processo inexistente")
    void testDetalharProcesso_naoEncontrado_falha() throws Exception {
        mockMvc.perform(get(API_URL, 999L)) // ID que não existe
            .andExpect(status().isNotFound());
    }

    @Nested
    @DisplayName("Botão Finalizar Processo")
    class FinalizarProcessoTests {

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode finalizar")
        void testPodeFinalizar_Admin_sucesso() throws Exception {
            mockMvc.perform(get(API_URL, processoMapeamento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(true));
        }

        @Test
        @DisplayName("GESTOR não pode finalizar")
        void testPodeFinalizar_Gestor_falha() throws Exception {
            // Simula o contexto de segurança após o @BeforeEach ter rodado
            setupSecurityContext(TITULO_GESTOR, unidadeGestora, Perfil.GESTOR);

            mockMvc.perform(get(API_URL, processoMapeamento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(false));
        }
    }

    @Nested
    @DisplayName("Botões de Homologação em Bloco")
    class HomologacaoEmBlocoTests {

        @Test
        @DisplayName("Deve habilitar homologação de CADASTRO quando UGs subordinadas estão prontas")
        void testPodeHomologarCadastro_Habilitado_sucesso() throws Exception {
            // Prepara o cenário
            subprocessoSubordinada1.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
            subprocessoSubordinada2.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.saveAll(List.of(subprocessoSubordinada1, subprocessoSubordinada2));

            setupSecurityContext(TITULO_GESTOR, unidadeGestora, Perfil.GESTOR);

            mockMvc.perform(get(API_URL, processoMapeamento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarCadastro").value(true));
        }

        @Test
        @DisplayName("Deve desabilitar homologação de CADASTRO se não houver UGs prontas")
        void testPodeHomologarCadastro_Desabilitado_sucesso() throws Exception {
            // Cenário: Nenhuma unidade em situação de homologação
            setupSecurityContext(TITULO_GESTOR, unidadeGestora, Perfil.GESTOR);

            mockMvc.perform(get(API_URL, processoMapeamento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarCadastro").value(false));
        }

        @Test
        @DisplayName("Deve habilitar homologação de MAPA quando UGs subordinadas estão prontas")
        void testPodeHomologarMapa_Habilitado_sucesso() throws Exception {
            // Prepara o cenário
            subprocessoSubordinada1.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
            subprocessoSubordinada2.setSituacao(SituacaoSubprocesso.MAPA_COM_SUGESTOES);
            subprocessoRepo.saveAll(List.of(subprocessoSubordinada1, subprocessoSubordinada2));

            setupSecurityContext(TITULO_GESTOR, unidadeGestora, Perfil.GESTOR);

            mockMvc.perform(get(API_URL, processoMapeamento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarMapa").value(true));
        }

        @Test
        @DisplayName("Deve desabilitar homologação de MAPA se não houver UGs prontas")
        void testPodeHomologarMapa_Desabilitado_sucesso() throws Exception {
            // Cenário: Nenhuma unidade em situação de homologação
            setupSecurityContext(TITULO_GESTOR, unidadeGestora, Perfil.GESTOR);

            mockMvc.perform(get(API_URL, processoMapeamento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarMapa").value(false));
        }
    }

    // --- Métodos de Apoio ---

    private void setupSecurityContext(long tituloEleitoral, Unidade unidade, Perfil perfil) {
        Usuario usuario = new Usuario(tituloEleitoral, "Usuário de Teste", "teste@sgc.com", "1234", unidade, Collections.singletonList(perfil));

        when(sgrhService.buscarUsuarioPorTitulo(String.valueOf(tituloEleitoral))).thenReturn(Optional.of(new UsuarioDto(
            String.valueOf(tituloEleitoral), "Usuário de Teste", "teste@sgc.com", "1234", "Cargo Teste"
        )));
        when(sgrhService.buscarPerfisUsuario(String.valueOf(tituloEleitoral))).thenReturn(List.of(
            new sgc.sgrh.dto.PerfilDto(String.valueOf(tituloEleitoral), unidade.getCodigo(), unidade.getNome(), perfil.name())
        ));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

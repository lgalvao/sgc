package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.comum.BeanUtil;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.modelo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.modelo.Usuario;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, BeanUtil.class})
@Transactional
@DisplayName("CDU-07: Detalhar Subprocesso")
public class CDU07IntegrationTest {
    private static final String UNIDADE_SIGLA = "UT";
    private static final long CHEFE_TITULO = 111111111111L;
    private static final long OUTRO_CHEFE_TITULO = 333333333333L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private SubprocessoDtoService subprocessoDtoService;

    private Subprocesso subprocesso;
    private Unidade unidade;
    private Unidade outraUnidade;

    @BeforeEach
    void setUp() {
        unidade = new Unidade("Unidade de Teste", UNIDADE_SIGLA);
        unidadeRepo.save(unidade);

        outraUnidade = new Unidade("Outra Unidade", "OUT");
        unidadeRepo.save(outraUnidade);

        Processo processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        processoRepo.save(processo);

        subprocesso = new Subprocesso(processo, unidade, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        Movimentacao movimentacao = new Movimentacao(subprocesso, null, unidade, "Subprocesso iniciado");
        movimentacaoRepo.save(movimentacao);
    }

    private void setupSecurityContext(long tituloEleitoral, Unidade unidade, String... perfis) {
        Usuario principal = new Usuario(
            tituloEleitoral,
            "Usuario de Teste",
            "teste@sgc.com",
            "123",
            unidade,
            Arrays.stream(perfis).map(Perfil::valueOf).collect(Collectors.toList())
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("Testes de Acesso e Visualização")
    class AcessoVisualizacao {
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode visualizar qualquer subprocesso")
        void adminPodeVisualizar() throws Exception {
            mockMvc.perform(get("/api/subprocessos/{id}", subprocesso.getCodigo())
                        .param("perfil", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.nome").value("Unidade de Teste"))
                .andExpect(jsonPath("$.situacao").value(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO.name()))
                .andExpect(jsonPath("$.localizacaoAtual").value(UNIDADE_SIGLA))
                .andExpect(jsonPath("$.movimentacoes[0].descricao").value("Subprocesso iniciado"));
        }

        @Test
        @DisplayName("CHEFE pode visualizar o subprocesso da sua unidade")
        void chefePodeVisualizarSuaUnidade() throws Exception {
            setupSecurityContext(CHEFE_TITULO, unidade, "CHEFE");

            mockMvc.perform(get("/api/subprocessos/{id}", subprocesso.getCodigo())
                        .param("perfil", "CHEFE")
                        .param("unidadeUsuario", String.valueOf(unidade.getCodigo())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.sigla").value(UNIDADE_SIGLA));
        }

        @Test
        @DisplayName("CHEFE NÃO pode visualizar o subprocesso de outra unidade")
        void chefeNaoPodeVisualizarOutraUnidade() throws Exception {
            setupSecurityContext(OUTRO_CHEFE_TITULO, outraUnidade, "CHEFE");

            mockMvc.perform(get("/api/subprocessos/{id}", subprocesso.getCodigo())
                        .param("perfil", "CHEFE")
                        .param("unidadeUsuario", String.valueOf(outraUnidade.getCodigo())))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve falhar ao buscar subprocesso inexistente")
        void falhaSubprocessoInexistente() {
            assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoDtoService.obterDetalhes(999L, Perfil.ADMIN, null));
        }
    }
}
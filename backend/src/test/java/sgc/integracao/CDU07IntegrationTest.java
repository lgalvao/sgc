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
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("CDU-07: Detalhar Subprocesso")
public class CDU07IntegrationTest {
    private static final String UNIDADE_SIGLA = "SESEL";
    private static final String CHEFE_TITULO = "111111111111";
    private static final String OUTRO_CHEFE_TITULO = "333333333333";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Autowired
    private SubprocessoDtoService subprocessoDtoService;
    @Autowired
    private UsuarioRepo usuarioRepo;

    private Subprocesso subprocesso;
    private Unidade unidade;
    private Unidade outraUnidade;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(10L).orElseThrow(); // SESEL
        outraUnidade = unidadeRepo.findById(11L).orElseThrow(); // SENIC

        Processo processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        processoRepo.save(processo);

        subprocesso = new Subprocesso(processo, unidade, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        Usuario usuario = new Usuario("999999999999", "Usuário Movimentação", "mov@test.com", "123", unidade, List.of(Perfil.SERVIDOR));
        usuarioRepo.save(usuario);

        Movimentacao movimentacao = new Movimentacao(subprocesso, null, unidade, "Subprocesso iniciado", usuario);
        movimentacaoRepo.save(movimentacao);
    }

    private void setupSecurityContext(String tituloEleitoral, Unidade unidade, String... perfis) {
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
                .andExpect(jsonPath("$.unidade.nome").value("Se&ccedil;&atilde;o de Sistemas Eleitorais"))
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
            assertThrows(ErroEntidadeNaoEncontrada.class, () -> subprocessoDtoService.obterDetalhes(999L, Perfil.ADMIN, null));
        }
    }
}

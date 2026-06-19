package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-06: Detalhar processo")
class CDU06IntegrationTest extends BaseIntegrationTest {
    private static final String TEST_USER_ID = "123456789";

    @MockitoBean
    private UsuarioAplicacaoService usuarioService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    private Processo processo;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        // Cria unidade programaticamente
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setSigla("U_TESTE");
        unidade.setNome("Unidade teste");
        unidade = unidadeRepo.save(unidade);

        // Cria processo
        processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        processo = processoRepo.save(processo);

        SecurityContextHolder.clearContext();
        when(usuarioService.usuarioAutenticado()).thenAnswer(invocacao -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
                return usuario;
            }
            return null;
        });
    }

    private Authentication setupSecurityContext(Unidade unidade, Perfil perfil) {
        Usuario principal = UsuarioFixture.usuarioPadrao()
                .setTituloEleitoral(TEST_USER_ID)
                .setUnidadeLotacao(unidade)
                .setPerfilAtivo(perfil)
                .setUnidadeAtivaCodigo(unidade.getCodigo());
        usuarioRepo.save(principal);

        principal.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));

        UsuarioPerfil up = UsuarioPerfil.builder()
                .usuarioTitulo(TEST_USER_ID)
                .unidadeCodigo(unidade.getCodigo())
                .perfil(perfil)
                .build();
        usuarioPerfilRepo.saveAndFlush(up);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        when(usuarioService.buscarPerfisUsuario(anyString())).thenReturn(List.of(
                new PerfilDto(TEST_USER_ID, unidade.getCodigo(), unidade.getNome(), perfil.name(), perfil.name())));

        return auth;
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve detalhar processo para Admin")
    void testDetalharProcesso_sucesso() throws Exception {
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);

        Subprocesso subprocesso = subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());
        registrarMovimentacaoInicial(subprocesso);

        mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo de Teste"));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 para processo inexistente")
    void testDetalharProcesso_naoEncontrado() throws Exception {
        mockMvc.perform(get("/api/processos/{codigo}/detalhes", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve mostrar 'podeFinalizar' como true para Admin com subprocessos homologados")
    void testPodeFinalizar_true_comAdmin() throws Exception {
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);
        Subprocesso subprocesso = subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());
        registrarMovimentacaoInicial(subprocesso);

        mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(true));
    }

    @Test
    @DisplayName("Deve mostrar 'podeFinalizar' como false para não Admin")
    void testPodeFinalizar_false_semAdmin() throws Exception {
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);
        Authentication auth = setupSecurityContext(unidade, Perfil.CHEFE);
        Subprocesso subprocesso = subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());
        registrarMovimentacaoInicial(subprocesso);

        mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeFinalizar").value(false));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarCadastro' como false para Gestor com cadastro disponibilizado")
    void testPodeHomologarCadastro_false_paraGestor() throws Exception {
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);
        Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        Subprocesso subprocesso = subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());
        registrarMovimentacaoInicial(subprocesso);

        mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarCadastro").value(false));
    }

    @Test
    @DisplayName("Deve mostrar 'podeHomologarMapa' como false para Gestor com mapa validado")
    void testPodeHomologarMapa_false_paraGestor() throws Exception {
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);
        Authentication auth = setupSecurityContext(unidade, Perfil.GESTOR);
        Subprocesso subprocesso = subprocessoRepo.save(Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build());
        registrarMovimentacaoInicial(subprocesso);

        mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeHomologarMapa").value(false));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar detalhes da unidade com situação do subprocesso correta")
    void testDetalharProcesso_dadosSubprocesso() throws Exception {
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);

        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        subprocesso = subprocessoRepo.save(subprocesso);
        registrarMovimentacaoInicial(subprocesso);

        mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidades[0].situacaoSubprocesso")
                        .value("MAPEAMENTO_MAPA_HOMOLOGADO"))
                .andExpect(jsonPath("$.unidades[0].dataLimite").exists());
    }
}

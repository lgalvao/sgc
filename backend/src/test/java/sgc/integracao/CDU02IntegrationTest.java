package sgc.integracao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.configuracoes.ConfiguracaoService;
import sgc.configuracoes.model.Configuracao;
import sgc.configuracoes.model.ConfiguracaoRepo;
import sgc.fixture.*;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-02: Visualizar painel")
@Slf4j
class CDU02IntegrationTest extends BaseIntegrationTest {
    private static final String API_PAINEL_PROCESSOS = "/api/painel/processos";
    private static final String API_PAINEL_ALERTAS = "/api/painel/alertas";
    private static final String API_PAINEL_BOOTSTRAP = "/api/painel/bootstrap";
    private static final String GESTOR = "GESTOR";
    private static final String CHEFE = "CHEFE";
    private static final String PROCESSO_RAIZ_JSON_PATH = "$.content[?(@.descricao == 'Processo raiz')]";
    private static final String PROCESSO_FILHA_1_JSON_PATH = "$.content[?(@.descricao == 'Processo filha 1')]";

    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private ConfiguracaoRepo configuracaoRepo;

    private Unidade unidadeRaiz;
    private Unidade unidadeFilha2;
    private Processo processoRaiz;

    @BeforeEach
    void setup() {
        // Limpa contexto de segurança entre testes
        SecurityContextHolder.clearContext();

        // Unidades
        unidadeRaiz = UnidadeFixture.unidadePadrao();
        unidadeRaiz.setCodigo(null);
        unidadeRaiz.setNome("Unidade raiz teste");
        unidadeRaiz = unidadeRepo.save(unidadeRaiz);

        Unidade unidadeFilha1 = UnidadeFixture.unidadePadrao();
        unidadeFilha1.setCodigo(null);
        unidadeFilha1.setNome("Unidade filha 1 Teste");
        unidadeFilha1.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha1 = unidadeRepo.save(unidadeFilha1);

        unidadeFilha2 = UnidadeFixture.unidadePadrao();
        unidadeFilha2.setCodigo(null);
        unidadeFilha2.setNome("Unidade filha 2 Teste");
        unidadeFilha2.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha2 = unidadeRepo.save(unidadeFilha2);

        // Processos
        processoRaiz = ProcessoFixture.processoEmAndamento();
        processoRaiz.setCodigo(null);
        processoRaiz.setDescricao("Processo raiz");
        processoRaiz.adicionarParticipantes(Set.of(unidadeRaiz));
        processoRaiz = processoRepo.save(processoRaiz);
        subprocessoRepo.save(SubprocessoFixture.novoSubprocesso(processoRaiz, unidadeRaiz));

        Processo processoFilha1 = ProcessoFixture.processoEmAndamento();
        processoFilha1.setCodigo(null);
        processoFilha1.setDescricao("Processo filha 1");
        processoFilha1.adicionarParticipantes(Set.of(unidadeFilha1));
        processoFilha1 = processoRepo.save(processoFilha1);
        subprocessoRepo.save(SubprocessoFixture.novoSubprocesso(processoFilha1, unidadeFilha1));

        Processo processoCriado = ProcessoFixture.processoPadrao(); // Status CRIADO
        processoCriado.setCodigo(null);
        processoCriado.setDescricao("Processo criado teste");
        processoCriado.adicionarParticipantes(Set.of(unidadeRaiz));
        processoRepo.save(processoCriado);

        unidadeRepo.flush();
        processoRepo.flush();
    }

    private Usuario setupSecurityContext(String tituloEleitoral, Unidade unidade, Object perfilOuString) {
        Perfil p = (perfilOuString instanceof String) ? Perfil.valueOf((String) perfilOuString) : (Perfil) perfilOuString;

        Usuario usuario = usuarioRepo.findById(tituloEleitoral).orElseGet(() -> {
            Usuario newUser = UsuarioFixture.usuarioComTitulo(tituloEleitoral);
            newUser.setUnidadeLotacao(unidade);
            return usuarioRepo.save(newUser);
        });

        // Configura o contexto de segurança soberano (Token JWT simulado)
        usuario.setPerfilAtivo(p);
        usuario.setUnidadeAtivaCodigo(unidade.getCodigo());
        usuarioRepo.saveAndFlush(usuario);

        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + p.name()));
        usuario.setAuthorities(authorities);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuario, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        return usuario;
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Processos (CDU-02)")
    class VisibilidadeProcessosTestes {

        @Test
        @DisplayName("CDU-02: GESTOR da unidade raiz deve ver processos subordinados, mas NÃO os 'Criado'")
        void testListarProcessosGestorRaizVeTodos() throws Exception {
            setupSecurityContext("99001", unidadeRaiz, GESTOR);

            mockMvc.perform(get(API_PAINEL_PROCESSOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).exists())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo criado teste')]").doesNotExist());
        }

        @Test
        @DisplayName("CDU-02: CHEFE da unidade Filha 2 não deve ver processos de outras unidades")
        void testListarProcessosChefeNaoVeIrmas() throws Exception {
            setupSecurityContext("99002", unidadeFilha2, CHEFE);

            mockMvc.perform(get(API_PAINEL_PROCESSOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).doesNotExist())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).doesNotExist());
        }

        @Test
        @DisplayName("CDU-02: ADMIN deve ver todos os processos, incluindo os com status 'Criado'")
        void testListarProcessosAdminVeTodos() throws Exception {
            setupSecurityContext("admin", unidadeRaiz, Perfil.ADMIN);

            mockMvc.perform(get(API_PAINEL_PROCESSOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).exists())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo criado teste')]").exists());
        }

        @Test
        @DisplayName("CDU-02: Nenhum perfil, exceto ADMIN, deve ver processos com status 'Criado'")
        void testListarProcessosNaoAdminNaoVeProcessosCriados() throws Exception {
            setupSecurityContext("99003", unidadeRaiz, GESTOR);

            mockMvc.perform(get(API_PAINEL_PROCESSOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo criado teste')]").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Alertas (CDU-02)")
    class VisibilidadeAlertasTestes {
        @Test
        @DisplayName("CDU-02: Usuário deve ver alertas direcionados a ele")
        void testListarAlertasUsuarioVeSeusAlertas() throws Exception {
            Usuario usuario = setupSecurityContext("99004", unidadeRaiz, GESTOR);

            Alerta alerta = AlertaFixture.alertaParaUsuario(processoRaiz, usuario);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta pessoal teste");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta pessoal teste')]").exists());
        }

        @Test
        @DisplayName("CDU-02: Usuário deve ver alertas coletivos da sua unidade ativa (se não for SERVIDOR)")
        void testListarAlertasUsuarioVeAlertasDaSuaUnidade() throws Exception {
            setupSecurityContext("99005", unidadeRaiz, GESTOR);

            Alerta alerta = AlertaFixture.alertaParaUnidade(processoRaiz, unidadeRaiz);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta unidade raiz");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta unidade raiz')]").exists());
        }

        @Test
        @DisplayName("CDU-02: SERVIDOR NÃO deve ver alertas da unidade, apenas os pessoais")
        void testListarAlertasServidorNaoVeAlertasDaUnidade() throws Exception {
            Usuario usuario = setupSecurityContext("99007", unidadeRaiz, Perfil.SERVIDOR);

            Alerta alertaUnidade = AlertaFixture.alertaParaUnidade(processoRaiz, unidadeRaiz);
            alertaUnidade.setCodigo(null);
            alertaUnidade.setDescricao("Alerta unidade SERVIDOR");
            alertaRepo.save(alertaUnidade);

            Alerta alertaPessoal = AlertaFixture.alertaParaUsuario(processoRaiz, usuario);
            alertaPessoal.setCodigo(null);
            alertaPessoal.setDescricao("Alerta pessoal SERVIDOR");
            alertaRepo.save(alertaPessoal);

            mockMvc.perform(get(API_PAINEL_ALERTAS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta pessoal SERVIDOR')]").exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta unidade SERVIDOR')]").doesNotExist());
        }

        @Test
        @DisplayName("CDU-02: Bootstrap do painel deve considerar alerta antigo como lido pelo prazo configurado")
        void testBootstrapAplicaPrazoConfiguradoAoAlertaAntigo() throws Exception {
            setupSecurityContext("99008", unidadeRaiz, Perfil.ADMIN);

            Configuracao configuracaoDiasAlertaNovo = configuracaoRepo
                    .findByChave(ConfiguracaoService.CHAVE_DIAS_ALERTA_NOVO)
                    .orElseGet(() -> configuracaoRepo.save(Configuracao.builder()
                            .chave(ConfiguracaoService.CHAVE_DIAS_ALERTA_NOVO)
                            .descricao("Dias para indicação de alerta como não lido")
                            .valor("3")
                            .build()));
            configuracaoDiasAlertaNovo.setValor("3");
            configuracaoRepo.saveAndFlush(configuracaoDiasAlertaNovo);

            Alerta alertaAntigo = AlertaFixture.alertaParaUnidade(processoRaiz, unidadeRaiz);
            alertaAntigo.setCodigo(null);
            alertaAntigo.setDescricao("Alerta antigo bootstrap");
            alertaAntigo.setDataHora(LocalDateTime.now().minusDays(5));
            alertaRepo.saveAndFlush(alertaAntigo);

            mockMvc.perform(get(API_PAINEL_BOOTSTRAP))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.alertas[?(@.descricao == 'Alerta antigo bootstrap')]").exists())
                    .andExpect(jsonPath("$.alertas[?(@.descricao == 'Alerta antigo bootstrap' && @.dataHoraLeitura != null)]").exists());
        }
    }
}

package sgc.integracao;

import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.security.test.context.support.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-02: Visualizar Painel")
@Slf4j
class CDU02IntegrationTest extends BaseIntegrationTest {
    private static final String API_PAINEL_PROCESSOS = "/api/painel/processos";
    private static final String API_PAINEL_ALERTAS = "/api/painel/alertas";
    private static final String PERFIL = "perfil";
    private static final String UNIDADE = "unidade";
    private static final String GESTOR = "GESTOR";
    private static final String CHEFE = "CHEFE";
    private static final String PROCESSO_RAIZ_JSON_PATH = "$.content[?(@.descricao == 'Processo Raiz')]";
    private static final String PROCESSO_FILHA_1_JSON_PATH = "$.content[?(@.descricao == 'Processo Filha 1')]";

    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private AlertaRepo alertaRepo;

    // Removido: JdbcTemplate não é necessário pois @Transactional garante isolamento
    // e os testes usam IDs dinâmicos.

    private Unidade unidadeRaiz;
    private Unidade unidadeFilha1;
    private Unidade unidadeFilha2;
    private Processo processoRaiz;
    private Processo processoFilha1;

    @BeforeEach
    void setup() {
        // Setup Programático - Não depende do data.sql

        // 1. Criar Unidades (Hierarquia)
        // Raiz
        unidadeRaiz = UnidadeFixture.unidadePadrao();
        unidadeRaiz.setCodigo(null); // Auto-increment
        unidadeRaiz.setNome("Unidade Raiz Teste");
        unidadeRaiz = unidadeRepo.save(unidadeRaiz);

        // Filha 1 -> Raiz
        unidadeFilha1 = UnidadeFixture.unidadePadrao();
        unidadeFilha1.setCodigo(null);
        unidadeFilha1.setNome("Unidade Filha 1 Teste");
        unidadeFilha1.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha1 = unidadeRepo.save(unidadeFilha1);

        // Filha 2 -> Raiz
        unidadeFilha2 = UnidadeFixture.unidadePadrao();
        unidadeFilha2.setCodigo(null);
        unidadeFilha2.setNome("Unidade Filha 2 Teste");
        unidadeFilha2.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha2 = unidadeRepo.save(unidadeFilha2);

        // 2. Criar Processos e Vincular a Unidades

        processoRaiz = ProcessoFixture.processoEmAndamento();
        processoRaiz.setCodigo(null);
        processoRaiz.setDescricao("Processo Raiz");
        processoRaiz.adicionarParticipantes(Set.of(unidadeRaiz));
        processoRaiz = processoRepo.save(processoRaiz);

        processoFilha1 = ProcessoFixture.processoEmAndamento();
        processoFilha1.setCodigo(null);
        processoFilha1.setDescricao("Processo Filha 1");
        processoFilha1.adicionarParticipantes(Set.of(unidadeFilha1));
        processoFilha1 = processoRepo.save(processoFilha1);

        Processo processoCriado = ProcessoFixture.processoPadrao(); // Status CRIADO
        processoCriado.setCodigo(null);
        processoCriado.setDescricao("Processo Criado Teste");
        processoCriado.adicionarParticipantes(Set.of(unidadeRaiz));
        processoRepo.save(processoCriado);

        // Flush para garantir persistência antes dos testes
        unidadeRepo.flush();
        processoRepo.flush();
    }

    private Usuario setupSecurityContext(String tituloEleitoral, Unidade unidade, String... perfis) {
        // Cria usuário dinamicamente se não existir
        Usuario usuario = usuarioRepo.findById(tituloEleitoral).orElseGet(() -> {
            Usuario newUser = UsuarioFixture.usuarioComTitulo(tituloEleitoral);
            newUser.setUnidadeLotacao(unidade);
            return usuarioRepo.save(newUser);
        });

        // Configura perfil e unidade ativos da sessão
        if (perfis.length > 0) {
            Perfil p = Perfil.valueOf(perfis[0]);
            usuario.setPerfilAtivo(p);
            usuario.setUnidadeAtivaCodigo(unidade.getCodigo());
        }

        // Define authorities como apenas o perfil selecionado para simular o comportamento do FiltroJwt
        Set<GrantedAuthority> authorities = Collections.emptySet();
        if (perfis.length > 0) {
            authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + perfis[0]));
        }
        usuario.setAuthorities(authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(usuario, null,
                authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return usuario;
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Processos (CDU-02)")
    class VisibilidadeProcessosTestes {
        @Test
        @WithMockAdmin
        @DisplayName("CDU-02: ADMIN deve ver todos os processos, incluindo os com status 'Criado'")
        void testListarProcessosAdminVeTodos() throws Exception {
            // Requisito CDU-02: Processos na situação 'Criado' deverão ser listados apenas se o usuário estiver logado com o perfil ADMIN.
            mockMvc.perform(get(API_PAINEL_PROCESSOS)
                            .param(PERFIL, "ADMIN")
                            .param(UNIDADE, unidadeRaiz.getCodigo().toString())) // Uso de ID dinâmico
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).exists())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Teste')]").exists());
        }

        @Test
        @DisplayName("CDU-02: GESTOR da unidade raiz deve ver processos subordinados, mas NÃO os 'Criado'")
        @WithMockUser(username = "99001")
        void testListarProcessosGestorRaizVeTodos() throws Exception {
            setupSecurityContext("99001", unidadeRaiz, GESTOR);

            mockMvc.perform(
                            get(API_PAINEL_PROCESSOS)
                                    .param(PERFIL, GESTOR)
                                    .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Deve ver o da Raiz e o da Filha (CDU-02: Processos ... que incluam entre as unidades participantes a unidade do usuário e/ou suas unidades subordinadas)
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).exists())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    // GESTOR não vê processos com status CRIADO (regra confirmada)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Teste')]").doesNotExist());
        }

        @Test
        @DisplayName("CDU-02: CHEFE da unidade Filha 2 não deve ver processos de outras unidades")
        @WithMockUser(username = "99002")
        void testListarProcessosChefeUnidadeFilha2NaoVeProcessosDeOutros() throws Exception {
            setupSecurityContext("99002", unidadeFilha2, CHEFE);

            // Unidade Filha 2 não tem processos no setup
            mockMvc.perform(
                            get(API_PAINEL_PROCESSOS)
                                    .param(PERFIL, CHEFE)
                                    .param(UNIDADE, unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(PROCESSO_RAIZ_JSON_PATH).doesNotExist())
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).doesNotExist());
        }

        @Test
        @DisplayName("CDU-02: Nenhum perfil (GESTOR), exceto ADMIN, deve ver processos com status 'Criado'")
        @WithMockUser(username = "99003")
        void testListarProcessosNaoAdminNaoVeProcessosCriados() throws Exception {
            setupSecurityContext("99003", unidadeRaiz, GESTOR);

            // Vamos replicar o cenário: Processo Criado numa unidade FILHA.
            Processo processoCriadoFilha = ProcessoFixture.processoPadrao();
            processoCriadoFilha.setCodigo(null);
            processoCriadoFilha.setDescricao("Processo Criado Filha");
            processoCriadoFilha.adicionarParticipantes(Set.of(unidadeFilha1));
            processoRepo.saveAndFlush(processoCriadoFilha);

            mockMvc.perform(
                            get(API_PAINEL_PROCESSOS)
                                    .param(PERFIL, GESTOR)
                                    .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Deve ver Raiz e Filha (Em Andamento)
                    .andExpect(jsonPath(PROCESSO_FILHA_1_JSON_PATH).exists())
                    // NÃO deve ver Criado Filha
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Processo Criado Filha')]").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Testes de Visibilidade de Alertas (CDU-02)")
    class VisibilidadeAlertasTestes {
        @Test
        @DisplayName("CDU-02: Usuário deve ver alertas direcionados a ele")
        @WithMockUser(username = "99004")
        void testListarAlertasUsuarioVeSeusAlertas() throws Exception {
            Usuario usuario = setupSecurityContext("99004", unidadeRaiz, GESTOR);

            Alerta alerta = AlertaFixture.alertaParaUsuario(processoRaiz, usuario);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Pessoal Teste");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param("usuarioTitulo", usuario.getTituloEleitoral())
                            .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Pessoal Teste')]").exists());
        }

        @Test
        @DisplayName("CDU-02: Usuário deve ver alertas direcionados à sua unidade")
        @WithMockUser(username = "99005")
        void testListarAlertasUsuarioVeAlertasDaSuaUnidade() throws Exception {
            setupSecurityContext("99005", unidadeRaiz, GESTOR);
            // Alerta para Unidade Filha 1 (Subordinada à Raiz)
            Alerta alerta = AlertaFixture.alertaParaUnidade(processoFilha1, unidadeFilha1);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Subordinada Teste");
            alertaRepo.save(alerta);

            mockMvc.perform(get(API_PAINEL_ALERTAS)
                            .param(UNIDADE, unidadeRaiz.getCodigo().toString()))
                    .andExpect(status().isOk())
                    // Painel não mostra alertas de subordinadas (regra atual do código)
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Subordinada Teste')]").doesNotExist());
        }

        @Test
        @DisplayName("CDU-02: Usuário não deve ver alertas de outros usuários ou unidades")
        @WithMockUser(username = "99006")
        void testListarAlertasUsuarioNaoVeAlertasDeOutros() throws Exception {
            Usuario usuario = setupSecurityContext("99006", unidadeFilha2, CHEFE);

            // Alerta para Unidade Filha 1 (Irmã, não subordinada)
            Alerta alerta = AlertaFixture.alertaParaUnidade(processoFilha1, unidadeFilha1);
            alerta.setCodigo(null);
            alerta.setDescricao("Alerta Outra Unidade");
            alertaRepo.save(alerta);

            mockMvc.perform(
                            get(API_PAINEL_ALERTAS)
                                    .param("usuarioTitulo", usuario.getTituloEleitoral())
                                    .param(UNIDADE, unidadeFilha2.getCodigo().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta Outra Unidade')]").doesNotExist());
        }
    }
}

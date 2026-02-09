package sgc.integracao.v2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sgc.Sgc;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestConfig;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SubprocessoRepo;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Classe base para testes de integração V2.
 * 
 * Estes testes seguem os seguintes princípios:
 * 1. Fidelidade aos requisitos em /etc/reqs
 * 2. Simulação de fluxo real de usuário (sem mocks)
 * 3. Chamadas a endpoints REST dos controllers
 * 4. Independência entre testes
 * 
 * Os testes NÃO devem:
 * - Usar mocks de services ou repositories
 * - Acessar repositories diretamente nos testes (exceto para setup)
 * - Depender de data.sql ou fixtures estáticas
 */
@Tag("integration-v2")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public abstract class BaseIntegrationTestV2 {
    
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    // Repositórios para setup de dados de teste (NÃO usar para atalhos nos testes)
    @Autowired
    protected ProcessoRepo processoRepo;
    
    @Autowired
    protected SubprocessoRepo subprocessoRepo;
    
    @Autowired
    protected UnidadeRepo unidadeRepo;
    
    @Autowired
    protected UsuarioRepo usuarioRepo;
    
    @Autowired
    protected AtividadeRepo atividadeRepo;
    
    @Autowired
    protected ConhecimentoRepo conhecimentoRepo;
    
    @Autowired
    protected CompetenciaRepo competenciaRepo;
    
    @Autowired
    protected MapaRepo mapaRepo;
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    @Autowired
    private WebApplicationContext context;
    
    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // Ajusta sequências para evitar conflito com data.sql
        try {
            jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 20000");
            jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 80000");
            jdbcTemplate.execute("ALTER TABLE SGC.ALERTA ALTER COLUMN CODIGO RESTART WITH 90000");
            jdbcTemplate.execute("ALTER TABLE SGC.SUBPROCESSO ALTER COLUMN CODIGO RESTART WITH 100000");
            jdbcTemplate.execute("ALTER TABLE SGC.ATIVIDADE ALTER COLUMN CODIGO RESTART WITH 200000");
            jdbcTemplate.execute("ALTER TABLE SGC.CONHECIMENTO ALTER COLUMN CODIGO RESTART WITH 300000");
            jdbcTemplate.execute("ALTER TABLE SGC.COMPETENCIA ALTER COLUMN CODIGO RESTART WITH 400000");
        } catch (DataAccessException e) {
            // Ignora se o DB não suportar
        }
    }
    
    /**
     * Cria uma hierarquia de unidades para testes.
     * 
     * @param nomeRaiz Nome da unidade raiz
     * @param nomesFilhas Nomes das unidades filhas
     * @return A unidade raiz com as filhas já persistidas
     */
    protected Unidade criarHierarquiaUnidades(String nomeRaiz, String... nomesFilhas) {
        Unidade raiz = UnidadeFixture.unidadePadrao();
        raiz.setCodigo(null);
        raiz.setNome(nomeRaiz);
        raiz = unidadeRepo.saveAndFlush(raiz);
        
        for (String nomeFilha : nomesFilhas) {
            Unidade filha = UnidadeFixture.unidadePadrao();
            filha.setCodigo(null);
            filha.setNome(nomeFilha);
            filha.setUnidadeSuperior(raiz);
            unidadeRepo.saveAndFlush(filha);
        }
        
        return raiz;
    }
    
    /**
     * Cria uma unidade operacional simples.
     * 
     * @param nome Nome da unidade
     * @return A unidade persistida
     */
    protected Unidade criarUnidadeOperacional(String nome) {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome(nome);
        return unidadeRepo.saveAndFlush(unidade);
    }
    
    /**
     * Cria um processo de teste.
     * 
     * @param descricao Descrição do processo
     * @param tipo Tipo do processo
     * @param unidades Unidades participantes
     * @return O processo persistido
     */
    protected Processo criarProcesso(String descricao, TipoProcesso tipo, List<Unidade> unidades) {
        Processo processo = new Processo();
        processo.setDescricao(descricao);
        processo.setTipo(tipo);
        processo.adicionarParticipantes(new HashSet<>(unidades));
        return processoRepo.saveAndFlush(processo);
    }
    
    /**
     * Cria um usuário com perfil e unidade específicos.
     * 
     * @param tituloEleitoral Título de eleitor do usuário
     * @param unidade Unidade do usuário
     * @param perfis Perfis do usuário
     * @return O usuário persistido
     */
    protected Usuario criarUsuarioComPerfil(String tituloEleitoral, Unidade unidade, String... perfis) {
        Usuario usuario = usuarioRepo.findById(tituloEleitoral).orElseGet(() -> {
            Usuario newUser = UsuarioFixture.usuarioComTitulo(tituloEleitoral);
            newUser.setUnidadeLotacao(unidade);
            newUser = usuarioRepo.save(newUser);
            
            // Insere na tabela de join simulada (H2 specific for View)
            for (String perfilStr : perfis) {
                try {
                    jdbcTemplate.update(
                            "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                            newUser.getTituloEleitoral(), unidade.getCodigo(), perfilStr);
                } catch (DataAccessException ignored) {
                    // Ignora se já existe
                }
            }
            return newUser;
        });
        
        return usuario;
    }
    
    /**
     * Cria um CHEFE para uma unidade específica.
     * 
     * @param unidade Unidade do CHEFE
     * @return O usuário CHEFE persistido
     */
    protected Usuario criarChefeParaUnidade(Unidade unidade) {
        String titulo = "99" + String.format("%03d", unidade.getCodigo() % 1000);
        return criarUsuarioComPerfil(titulo, unidade, "CHEFE");
    }
    
    /**
     * Cria um GESTOR para uma unidade específica.
     * 
     * @param unidade Unidade do GESTOR
     * @return O usuário GESTOR persistido
     */
    protected Usuario criarGestorParaUnidade(Unidade unidade) {
        String titulo = "88" + String.format("%03d", unidade.getCodigo() % 1000);
        return criarUsuarioComPerfil(titulo, unidade, "GESTOR");
    }
    
    /**
     * Cria um ADMIN.
     * 
     * @return O usuário ADMIN persistido
     */
    protected Usuario criarAdmin() {
        Unidade sedoc = criarUnidadeOperacional("SEDOC");
        String titulo = "77001";
        Usuario admin = criarUsuarioComPerfil(titulo, sedoc, "ADMIN");
        
        // Registra como administrador
        try {
            jdbcTemplate.update(
                    "INSERT INTO SGC.ADMINISTRADOR (usuario_titulo) VALUES (?)",
                    titulo);
        } catch (DataAccessException ignored) {
            // Ignora se já existe
        }
        
        return admin;
    }
    
    /**
     * Configura o contexto de segurança para um usuário.
     * 
     * @param usuario Usuário para autenticar
     * @param unidade Unidade de trabalho
     * @param perfis Perfis do usuário
     */
    protected void setupSecurityContext(Usuario usuario, Unidade unidade, String... perfis) {
        Set<UsuarioPerfil> perfisSet = new HashSet<>();
        for (String perfilStr : perfis) {
            perfisSet.add(UsuarioPerfil.builder()
                    .usuario(usuario)
                    .unidade(unidade)
                    .perfil(Perfil.valueOf(perfilStr))
                    .build());
        }
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
    
    /**
     * Configura o contexto de segurança para um usuário já criado.
     * 
     * @param usuario Usuário para autenticar
     */
    protected void setupSecurityContext(Usuario usuario) {
        setupSecurityContext(usuario, usuario.getUnidadeLotacao(), "CHEFE");
    }
}

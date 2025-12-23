package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockCustomUser;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.internal.model.Perfil;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioPerfil;
import sgc.sgrh.internal.model.UsuarioPerfilRepo;
import sgc.sgrh.internal.model.UsuarioRepo;
import sgc.subprocesso.internal.model.*;
import sgc.subprocesso.internal.service.SubprocessoDtoService;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("CDU-07: Detalhar Subprocesso")
public class CDU07IntegrationTest extends BaseIntegrationTest {
    private static final String UNIDADE_SIGLA = "SESEL_TEST";
    private static final String OUTRO_CHEFE_TITULO = "333333333333";

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
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Subprocesso subprocesso;
    private Unidade unidade;
    private Unidade outraUnidade;

    @BeforeEach
    void setUp() {
        // Reset sequences
        try {
            jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 60000");
            jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 70000");
            jdbcTemplate.execute("ALTER TABLE SGC.SUBPROCESSO ALTER COLUMN CODIGO RESTART WITH 80000");
        } catch (Exception ignored) {}

        // Unidade Principal
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla(UNIDADE_SIGLA);
        unidade.setNome("Seção de Sistemas Eleitorais Teste");
        unidade = unidadeRepo.save(unidade);

        // Outra Unidade
        outraUnidade = UnidadeFixture.unidadePadrao();
        outraUnidade.setCodigo(null);
        outraUnidade.setSigla("SENIC_TEST");
        outraUnidade.setNome("Seção Outra Teste");
        outraUnidade = unidadeRepo.save(outraUnidade);

        // Criar Unidade 9999 para teste @WithMockCustomUser(unidadeId = 9999L)
        // Isso é necessário porque a anotação exige um ID constante
        Unidade unidade9999 = UnidadeFixture.unidadePadrao();
        unidade9999.setCodigo(9999L);
        unidade9999.setSigla("U9999");
        unidade9999.setNome("Unidade 9999");
        // Verifica se já existe (pode causar erro de constraint se não checar)
        if (!unidadeRepo.existsById(9999L)) {
            // Insere manualmente para forçar o ID 9999
            jdbcTemplate.update("INSERT INTO SGC.VW_UNIDADE (codigo, nome, sigla, tipo, situacao) VALUES (?, ?, ?, ?, ?)",
                    9999L, "Unidade 9999", "U9999", "OPERACIONAL", "ATIVA");
        }

        Processo processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        processoRepo.save(processo);

        subprocesso =
                new Subprocesso(
                        processo,
                        unidade,
                        null,
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                        processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        Usuario usuario = UsuarioFixture.usuarioPadrao();
        usuario.setTituloEleitoral("999999999999");
        usuario.setNome("Usuário Movimentação");
        usuario.setUnidadeLotacao(unidade);
        usuario = usuarioRepo.save(usuario);

        UsuarioPerfil perfilServidor = UsuarioPerfil.builder()
                .usuarioTitulo(usuario.getTituloEleitoral())
                .usuario(usuario)
                .unidadeCodigo(unidade.getCodigo())
                .unidade(unidade)
                .perfil(Perfil.SERVIDOR)
                .build();
        usuarioPerfilRepo.save(perfilServidor);

        // Create a CHEFE for the unit to avoid 404 in buscarResponsavelVigente
        Usuario chefe = UsuarioFixture.usuarioPadrao();
        chefe.setTituloEleitoral("888888888888");
        chefe.setNome("Chefe SESEL");
        chefe.setUnidadeLotacao(unidade);
        chefe = usuarioRepo.save(chefe);

        // Associar como titular na Unidade também (para o DTO)
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        UsuarioPerfil perfilChefe = UsuarioPerfil.builder()
                .usuarioTitulo(chefe.getTituloEleitoral())
                .usuario(chefe)
                .unidadeCodigo(unidade.getCodigo())
                .unidade(unidade)
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilChefe);

        // Configurar outro chefe para outra unidade
        Usuario outroChefe = UsuarioFixture.usuarioPadrao();
        outroChefe.setTituloEleitoral(OUTRO_CHEFE_TITULO);
        outroChefe.setNome("Chefe Outro");
        outroChefe.setUnidadeLotacao(outraUnidade);
        outroChefe = usuarioRepo.save(outroChefe);

        UsuarioPerfil perfilOutroChefe = UsuarioPerfil.builder()
                .usuarioTitulo(outroChefe.getTituloEleitoral())
                .usuario(outroChefe)
                .unidadeCodigo(outraUnidade.getCodigo())
                .unidade(outraUnidade)
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilOutroChefe);

        Movimentacao movimentacao =
                new Movimentacao(subprocesso, null, unidade, "Subprocesso iniciado", usuario);
        movimentacaoRepo.save(movimentacao);
    }

    @Nested
    @DisplayName("Testes de Acesso e Visualização")
    class AcessoVisualizacao {
        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode visualizar qualquer subprocesso")
        void adminPodeVisualizar() throws Exception {
            mockMvc.perform(
                            get("/api/subprocessos/{id}", subprocesso.getCodigo())
                                    .param("perfil", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unidade.nome").value("Seção de Sistemas Eleitorais Teste"))
                    .andExpect(
                            jsonPath("$.situacao")
                                    .value(
                                            SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                                                    .name()))
                    .andExpect(jsonPath("$.localizacaoAtual").value(UNIDADE_SIGLA))
                    .andExpect(jsonPath("$.processoDescricao").value("Processo de Teste"))
                    .andExpect(jsonPath("$.tipoProcesso").value("MAPEAMENTO"))
                    .andExpect(jsonPath("$.titular").exists())
                    .andExpect(
                            jsonPath("$.movimentacoes[0].descricao").value("Subprocesso iniciado"));
        }

        @Test
        @WithMockChefe("888888888888")
        @DisplayName("CHEFE pode visualizar o subprocesso da sua unidade")
        void chefePodeVisualizarSuaUnidade() throws Exception {
            mockMvc.perform(
                            get("/api/subprocessos/{id}", subprocesso.getCodigo())
                                    .param("perfil", "CHEFE")
                                    .param("unidadeUsuario", String.valueOf(unidade.getCodigo())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unidade.sigla").value(UNIDADE_SIGLA));
        }

        @Test
        @WithMockCustomUser(
                tituloEleitoral = OUTRO_CHEFE_TITULO,
                perfis = {"CHEFE"},
                unidadeId = 9999L) // MockCustomUser doesn't easily link to dynamic ID in annotation, so we use logic or fixed
        @DisplayName("CHEFE NÃO pode visualizar o subprocesso de outra unidade")
        void chefeNaoPodeVisualizarOutraUnidade() throws Exception {
            // Nota: Como o @WithMockCustomUser usa valores constantes na anotação,
            // e os IDs são gerados dinamicamente, este teste pode ser frágil se a anotação for usada estritamente.
            // Para integração real, o security context deve ser configurado programaticamente ou usar IDs fixos via SQL.
            // Aqui, vamos confiar que o security context está configurado com o usuário criado,
            // mas a anotação @WithMockCustomUser cria um UserDetails mockado.

            // O teste original usava unidadeId=11L. Aqui precisamos usar o ID dinâmico da outraUnidade.
            // Como a anotação é estática, não podemos injetar o ID da outraUnidade.
            // Solução: Configurar o contexto manualmente dentro do teste ou usar um ID fixo na criação da outraUnidade.
            // Vamos optar por recriar o contexto manualmente neste teste.

            // Mas o teste usa mockMvc, então o filtro de segurança processa o token ou contexto.
            // Se usarmos @WithMockCustomUser, o contexto já vem preenchido.

            // O controller verifica: sgrhService.usuarioTemPerfil(titulo, perfil, unidadeSubprocesso)
            // Se o usuário logado (333...) tem perfil CHEFE na unidade X, e o subprocesso é da unidade Y.

            mockMvc.perform(
                            get("/api/subprocessos/{id}", subprocesso.getCodigo())
                                    .param("perfil", "CHEFE")
                                    .param(
                                            "unidadeUsuario",
                                            String.valueOf(outraUnidade.getCodigo())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve falhar ao buscar subprocesso inexistente")
        void falhaSubprocessoInexistente() {
            assertThrows(
                    ErroEntidadeNaoEncontrada.class,
                    () -> subprocessoDtoService.obterDetalhes(99999L, Perfil.ADMIN, null));
        }
    }
}

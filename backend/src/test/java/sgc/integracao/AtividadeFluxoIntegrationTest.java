package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.model.AtividadeRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.*;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@DisplayName("Integração: Fluxo de Atividades (Criação, Exclusão, Validação)")
class AtividadeFluxoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Subprocesso subprocesso;
    private Unidade unidade;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
         try {
            jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 75000");
            jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 85000");
            jdbcTemplate.execute("ALTER TABLE SGC.SUBPROCESSO ALTER COLUMN CODIGO RESTART WITH 95000");
            jdbcTemplate.execute("ALTER TABLE SGC.MAPA ALTER COLUMN CODIGO RESTART WITH 95000");
        } catch (Exception ignored) {
            // Ignorado: falha ao resetar sequências no H2 não deve impedir o teste
        }

        // 1. Criar unidade e chefe
        unidade = new Unidade();
        unidade.setNome("Unidade Teste Fluxo");
        unidade.setSigla("U_FLUXO");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("888888888888");
        unidade = unidadeRepo.save(unidade);

        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral("888888888888");
        chefe.setNome("Chefe Fluxo");
        chefe.setEmail("chefe@fluxo.com");
        chefe.setUnidadeLotacao(unidade);
        chefe = usuarioRepo.save(chefe);

        UsuarioPerfil perfilChefe = UsuarioPerfil.builder()
                .usuarioTitulo(chefe.getTituloEleitoral())
                .usuario(chefe)
                .unidadeCodigo(unidade.getCodigo())
                .unidade(unidade)
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilChefe);

        // 2. Criar Processo
        Processo processo = new Processo(
                "Processo Fluxo",
                TipoProcesso.MAPEAMENTO,
                SituacaoProcesso.EM_ANDAMENTO,
                LocalDateTime.now().plusDays(30));
        processoRepo.save(processo);

        // 3. Criar Subprocesso e Mapa
        subprocesso = new Subprocesso()
                .setUnidade(unidade)
                .setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .setDataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .setProcesso(processo);
        subprocesso = subprocessoRepo.save(subprocesso);

        mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);

        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @WithMockChefe("888888888888")
    @DisplayName("Deve criar e excluir atividade com sucesso")
    void deveCriarEExcluirAtividade() throws Exception {
        // Criar Atividade
        AtividadeDto request = AtividadeDto.builder()
                .descricao("Atividade Temporária")
                .mapaCodigo(mapa.getCodigo())
                .build();

        String responseJson = mockMvc.perform(post("/api/atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.atividade.descricao", is("Atividade Temporária")))
                .andReturn().getResponse().getContentAsString();

        // Extrair ID
        Long codigoAtividade = objectMapper.readTree(responseJson).get("atividade").get("codigo").asLong();

        // Verificar DB
        assertThat(atividadeRepo.existsById(codigoAtividade)).isTrue();

        // Excluir Atividade
        mockMvc.perform(post("/api/atividades/{id}/excluir", codigoAtividade)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verificar DB (não deve existir)
        assertThat(atividadeRepo.existsById(codigoAtividade)).isFalse();
    }

    @Test
    @WithMockChefe("888888888888")
    @DisplayName("Deve validar cadastro: Atividade sem conhecimento gera erro")
    void deveValidarAtividadeSemConhecimento() throws Exception {
        // Criar Atividade sem conhecimento
        AtividadeDto request = AtividadeDto.builder()
                .descricao("Atividade Incompleta")
                .mapaCodigo(mapa.getCodigo())
                .build();

        mockMvc.perform(post("/api/atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Validar Cadastro
        mockMvc.perform(get("/api/subprocessos/{id}/validar-cadastro", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido", is(false)))
                .andExpect(jsonPath("$.erros", hasSize(1)))
                .andExpect(jsonPath("$.erros[0].tipo", is("ATIVIDADE_SEM_CONHECIMENTO")))
                .andExpect(jsonPath("$.erros[0].descricaoAtividade", is("Atividade Incompleta")));
    }

    @Test
    @WithMockChefe("888888888888")
    @DisplayName("Deve validar cadastro: Mapa sem atividades gera erro SEM_ATIVIDADES")
    void deveValidarMapaSemAtividades() throws Exception {
        // Validar Cadastro (mapa vazio)
        mockMvc.perform(get("/api/subprocessos/{id}/validar-cadastro", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido", is(false)))
                .andExpect(jsonPath("$.erros", hasSize(1)))
                .andExpect(jsonPath("$.erros[0].tipo", is("SEM_ATIVIDADES")));
    }
}

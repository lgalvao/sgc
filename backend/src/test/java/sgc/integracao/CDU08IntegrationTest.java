package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.dto.AtividadeDto;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("CDU-08: Manter cadastro de atividades e conhecimentos")
class CDU08IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    private Mapa mapa;

    @BeforeEach
    void setUp() {
        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral(888888888888L);
        chefe.setNome("Chefe de Teste");
        chefe.setPerfis(java.util.Set.of(Perfil.CHEFE));
        usuarioRepo.save(chefe);

        Unidade unidade = new Unidade("UNIDADE-CDU08", "U08");
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        mapa = new Mapa();
        mapaRepo.save(mapa);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocessoRepo.save(subprocesso);
    }

    @Nested
    @DisplayName("Testes de fluxo completo")
    @WithMockChefe("888888888888")
    class FluxoCompleto {

        @Test
        @DisplayName("Deve criar uma atividade, adicionar, atualizar e remover um conhecimento")
        void fluxoCompletoCrudConhecimento() throws Exception {
            // 1. Criar Atividade
            var atividadeDto = new AtividadeDto(null, mapa.getCodigo(), "Analisar Documentos");
            var result = mockMvc.perform(post("/api/atividades").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(atividadeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao", is("Analisar Documentos")))
                .andReturn();

            var atividadeCriada = objectMapper.readValue(result.getResponse().getContentAsString(), AtividadeDto.class);
            Long atividadeId = atividadeCriada.codigo();

            // 2. Adicionar Conhecimento
            var conhecimentoDto = new ConhecimentoDto(null, atividadeId, "Legislação Aplicada");
            var conhecimentoResult = mockMvc.perform(post("/api/atividades/{id}/conhecimentos", atividadeId).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(conhecimentoDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao", is("Legislação Aplicada")))
                .andReturn();

            var conhecimentoCriado = objectMapper.readValue(conhecimentoResult.getResponse().getContentAsString(), ConhecimentoDto.class);
            Long conhecimentoId = conhecimentoCriado.codigo();

            // Verificar se o conhecimento foi listado
            mockMvc.perform(get("/api/atividades/{id}/conhecimentos", atividadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Legislação Aplicada")));

            // 3. Atualizar Conhecimento
            var conhecimentoAtualizadoDto = new ConhecimentoDto(conhecimentoId, atividadeId, "Legislação Específica");
            mockMvc.perform(post("/api/atividades/{id}/conhecimentos/{cid}/atualizar", atividadeId, conhecimentoId).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(conhecimentoAtualizadoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", is("Legislação Específica")));

            // 4. Remover Conhecimento
            mockMvc.perform(post("/api/atividades/{id}/conhecimentos/{cid}/excluir", atividadeId, conhecimentoId).with(csrf()))
                .andExpect(status().isNoContent());

            // Verificar se a lista de conhecimentos está vazia
            mockMvc.perform(get("/api/atividades/{id}/conhecimentos", atividadeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.model.TipoProcesso;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-03: Manter processo")
@WithMockAdmin
@Import(TestSecurityConfig.class)
class CDU03IntegrationTest extends BaseIntegrationTest {
        private static final String API_PROCESSOS = "/api/processos";
        private static final String API_PROCESSOS_ID = "/api/processos/{codigo}";

        @Autowired
        private JdbcTemplate jdbcTemplate;

        private Unidade unidade1;
        private Unidade unidade2;

        @BeforeEach
        @SuppressWarnings("unused")
        void setup() {
                // Reset sequences to avoid conflicts
                try {
                        jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 20000");
                        jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 80000");
                } catch (DataAccessException ignored) {
                        // ignore
                }

                // Create fixtures using saveAndFlush to ensure visibility
                unidade1 = UnidadeFixture.unidadePadrao();
                unidade1.setCodigo(null);
                unidade1.setNome("Unidade 1");
                unidade1.setSigla("U1");
                unidade1 = unidadeRepo.saveAndFlush(unidade1);

                unidade2 = UnidadeFixture.unidadePadrao();
                unidade2.setCodigo(null);
                unidade2.setNome("Unidade 2");
                unidade2.setSigla("U2");
                unidade2 = unidadeRepo.saveAndFlush(unidade2);
        }

        private CriarProcessoRequest criarCriarProcessoReq(
                        String descricao, List<Long> unidades, LocalDateTime dataLimiteEtapa1) {
                return new CriarProcessoRequest(descricao, TipoProcesso.MAPEAMENTO, dataLimiteEtapa1, unidades);
        }

        private AtualizarProcessoRequest criarAtualizarProcessoReq(
                        Long codigo, String descricao, List<Long> unidades, LocalDateTime dataLimiteEtapa1) {
                return new AtualizarProcessoRequest(
                                codigo, descricao, TipoProcesso.MAPEAMENTO, dataLimiteEtapa1, unidades);
        }

        @Test
        void testCriarProcesso_sucesso() throws Exception {
                List<Long> unidades = List.of(unidade1.getCodigo());

                CriarProcessoRequest requestDTO = criarCriarProcessoReq(
                                "Processo de Mapeamento Teste", unidades, LocalDateTime.now().plusDays(30));

                mockMvc.perform(
                                post(API_PROCESSOS)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.codigo").isNumber())
                                .andExpect(jsonPath("$.descricao").value("Processo de Mapeamento Teste"))
                                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                                .andExpect(
                                                jsonPath("$.situacao")
                                                                .value("CRIADO")); // Verifica se a situação inicial é
                // 'Criado'
        }

        @Test
        void testCriarProcesso_descricaoVazia_falha() throws Exception {
                List<Long> unidades = List.of(unidade1.getCodigo());

                CriarProcessoRequest requestDTO = criarCriarProcessoReq(
                                "", // Descrição vazia
                                unidades,
                                LocalDateTime.now().plusDays(30));

                mockMvc.perform(
                                post(API_PROCESSOS)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(
                                                jsonPath("$.subErrors[0].message")
                                                                .value("Preencha a descrição")); // Mensagem de
                // validação
        }

        @Test
        void testCriarProcesso_semUnidades_falha() throws Exception {
                CriarProcessoRequest requestDTO = criarCriarProcessoReq(
                                "Processo sem unidades",
                                Collections.emptyList(), // Sem unidades
                                LocalDateTime.now().plusDays(30));

                MvcResult result = mockMvc.perform(
                                post(API_PROCESSOS)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                try {
                        Files.createDirectories(Paths.get("build"));
                        Files.writeString(Paths.get("build/test-output.txt"), responseBody);
                } catch (java.io.IOException e) {
                        // Ignore for test purposes
                }

                // Re-add the assertion to keep the test failing, but now we get the output
                mockMvc.perform(
                                post(API_PROCESSOS)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(
                                                jsonPath("$.subErrors[0].message")
                                                                .value("Pelo menos uma unidade participante deve ser incluída."));
        }

        // Teste para edição de processo (requer um processo existente)
        @Test
        void testEditarProcesso_sucesso() throws Exception {
                // 1. Criar um processo para ser editado
                List<Long> unidadesIniciais = List.of(unidade1.getCodigo());
                CriarProcessoRequest criarRequestDTO = criarCriarProcessoReq(
                                "Processo para Edição", unidadesIniciais, LocalDateTime.now().plusDays(20));

                MvcResult result = mockMvc.perform(
                                post(API_PROCESSOS)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(criarRequestDTO)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long processoId = objectMapper
                                .readTree(result.getResponse().getContentAsString())
                                .get("codigo")
                                .asLong();

                // 2. Editar o processo
                List<Long> unidadesEditadas = new ArrayList<>();
                unidadesEditadas.add(unidade1.getCodigo());
                unidadesEditadas.add(unidade2.getCodigo()); // Adiciona outra unidade

                AtualizarProcessoRequest editarRequestDTO = criarAtualizarProcessoReq(
                                processoId,
                                "Processo Editado",
                                // Tipo não pode ser alterado na edição, mas é enviado no DTO
                                unidadesEditadas,
                                LocalDateTime.now().plusDays(40) // Nova data limite
                );

                mockMvc.perform(
                                post(API_PROCESSOS + "/{codProcesso}/atualizar", processoId)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(editarRequestDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.codigo").value(processoId))
                                .andExpect(jsonPath("$.descricao").value("Processo Editado"));
        }

        @Test
        void testEditarProcesso_processoNaoEncontrado_falha() throws Exception {
                List<Long> unidades = List.of(unidade1.getCodigo());

                AtualizarProcessoRequest editarRequestDTO = criarAtualizarProcessoReq(
                                99999L, // Código que não existe
                                "Processo Inexistente",
                                unidades,
                                LocalDateTime.now().plusDays(30));

                mockMvc.perform(
                                post(
                                                API_PROCESSOS + "/{codProcesso}/atualizar",
                                                99999L) // código que não existe
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(editarRequestDTO)))
                                .andExpect(status().isNotFound()); // Ou outro status de erro apropriado
        }

        @Test
        void testRemoverProcesso_sucesso() throws Exception {
                // 1. Criar um processo para ser removido
                List<Long> unidadesIniciais = List.of(unidade1.getCodigo());
                CriarProcessoRequest criarRequestDTO = criarCriarProcessoReq(
                                "Processo para Remoção",
                                unidadesIniciais,
                                LocalDateTime.now().plusDays(20));

                MvcResult result = mockMvc.perform(
                                post(API_PROCESSOS)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(criarRequestDTO)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long processoId = objectMapper
                                .readTree(result.getResponse().getContentAsString())
                                .get("codigo")
                                .asLong();

                // 2. Remover o processo
                mockMvc.perform(post(API_PROCESSOS + "/{codProcesso}/excluir", processoId).with(csrf()))
                                .andExpect(status().isNoContent()); // 204 No Content para remoção bem-sucedida

                // 3. Tentar buscar o processo removido para confirmar que não existe mais
                mockMvc.perform(get(API_PROCESSOS_ID, processoId)).andExpect(status().isNotFound());
        }

        @Test
        void testRemoverProcesso_processoNaoEncontrado_falha() throws Exception {
                mockMvc.perform(
                                post(
                                                API_PROCESSOS + "/{codProcesso}/excluir",
                                                99999L) // código que não existe
                                                .with(csrf()))
                                .andExpect(status().isNotFound());
        }
}

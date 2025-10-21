package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;
import sgc.comum.erros.RestExceptionHandler;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.ProcessoControle;
import sgc.processo.ProcessoService;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.modelo.TipoProcesso;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CDU-03: Manter processo")
@WithMockAdmin
@Import(TestSecurityConfig.class)
public class CDU03IntegrationTest {
    private static final String API_PROCESSOS = "/api/processos";
    private static final String API_PROCESSOS_ID = "/api/processos/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoService processoService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProcessoControle(processoService))
                .setControllerAdvice(new RestExceptionHandler())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }



    private CriarProcessoReq criarCriarProcessoReq(String descricao, List<Long> unidades, LocalDateTime dataLimiteEtapa1) {
        return new CriarProcessoReq(descricao, TipoProcesso.MAPEAMENTO.name(), dataLimiteEtapa1, unidades);
    }

    private AtualizarProcessoReq criarAtualizarProcessoReq(Long codigo, String descricao, List<Long> unidades, LocalDateTime dataLimiteEtapa1) {
        return new AtualizarProcessoReq(codigo, descricao, TipoProcesso.MAPEAMENTO.name(), dataLimiteEtapa1, unidades);
    }

    @Test
    void testCriarProcesso_sucesso() throws Exception {
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L); // Assumindo que a unidade com ID 1 existe

        CriarProcessoReq requestDTO = criarCriarProcessoReq(
                "Processo de Mapeamento Teste",
                unidades,
                LocalDateTime.now().plusDays(30)
        );

        mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").isNumber())
                .andExpect(jsonPath("$.descricao").value("Processo de Mapeamento Teste"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value("CRIADO")); // Verifica se a situação inicial é 'Criado'
    }

    @Test
    void testCriarProcesso_descricaoVazia_falha() throws Exception {
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L);

        CriarProcessoReq requestDTO = criarCriarProcessoReq(
                "", // Descrição vazia
                unidades,
                LocalDateTime.now().plusDays(30)
        );

        mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Preencha a descrição")); // Mensagem de validação
    }

    @Test
    void testCriarProcesso_semUnidades_falha() throws Exception {
        CriarProcessoReq requestDTO = criarCriarProcessoReq(
                "Processo sem unidades",
                Collections.emptyList(), // Sem unidades
                LocalDateTime.now().plusDays(30)
        );

        MvcResult result = mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        try {
            Files.createDirectories(Paths.get("build"));
            Files.write(Paths.get("build/test-output.txt"), responseBody.getBytes(StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            // Ignore for test purposes
        }

        // Re-add the assertion to keep the test failing, but now we get the output
        mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Pelo menos uma unidade participante deve ser incluída."));
    }

    // Teste para edição de processo (requer um processo existente)
    @Test
    void testEditarProcesso_sucesso() throws Exception {
        // 1. Criar um processo para ser editado
        List<Long> unidadesIniciais = new ArrayList<>();
        unidadesIniciais.add(1L);
        CriarProcessoReq criarRequestDTO = criarCriarProcessoReq(
                "Processo para Edição",
                unidadesIniciais,
                LocalDateTime.now().plusDays(20)
        );

        MvcResult result = mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // 2. Editar o processo
        List<Long> unidadesEditadas = new ArrayList<>();
        unidadesEditadas.add(1L);
        unidadesEditadas.add(2L); // Adiciona outra unidade

        AtualizarProcessoReq editarRequestDTO = criarAtualizarProcessoReq(
                processoId,
                "Processo Editado",
                // Tipo não pode ser alterado na edição, mas é enviado no DTO
                unidadesEditadas,
                LocalDateTime.now().plusDays(40) // Nova data limite
        );

        mockMvc.perform(put(API_PROCESSOS_ID, processoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(processoId))
                .andExpect(jsonPath("$.descricao").value("Processo Editado"));
    }

    @Test
    void testEditarProcesso_processoNaoEncontrado_falha() throws Exception {
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L);

        AtualizarProcessoReq editarRequestDTO = criarAtualizarProcessoReq(
                999L, // Código que não existe
                "Processo Inexistente",
                unidades,
                LocalDateTime.now().plusDays(30)
        );

        mockMvc.perform(put(API_PROCESSOS_ID, 999L) // ID que não existe
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarRequestDTO)))
                .andExpect(status().isNotFound()); // Ou outro status de erro apropriado
    }

    @Test
    void testRemoverProcesso_sucesso() throws Exception {
        // 1. Criar um processo para ser removido
        List<Long> unidadesIniciais = new ArrayList<>();
        unidadesIniciais.add(1L);
        CriarProcessoReq criarRequestDTO = criarCriarProcessoReq(
                "Processo para Remoção",
                unidadesIniciais,
                LocalDateTime.now().plusDays(20)
        );

        MvcResult result = mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // 2. Remover o processo
        mockMvc.perform(delete(API_PROCESSOS_ID, processoId))
                .andExpect(status().isNoContent()); // 204 No Content para remoção bem-sucedida

        // 3. Tentar buscar o processo removido para confirmar que não existe mais
        mockMvc.perform(get(API_PROCESSOS_ID, processoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoverProcesso_processoNaoEncontrado_falha() throws Exception {
        mockMvc.perform(delete(API_PROCESSOS_ID, 999L)) // ID que não existe
                .andExpect(status().isNotFound());
    }
}

package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;
import sgc.comum.erros.RestExceptionHandler;
import sgc.processo.ProcessoControle;
import sgc.processo.ProcessoService;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.modelo.TipoProcesso;

import java.time.LocalDate;
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
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class CDU03IntegrationTest {
    private static final String API_PROCESSOS = "/api/processos";
    private static final String API_PROCESSOS_ID = "/api/processos/{id}";

    @TestConfiguration
    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }

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

    private CriarProcessoReq criarCriarProcessoReq(String descricao, List<Long> unidades, LocalDate dataLimiteEtapa1) {
        return new CriarProcessoReq(descricao, TipoProcesso.MAPEAMENTO.name(), dataLimiteEtapa1, unidades);
    }

    private AtualizarProcessoReq criarAtualizarProcessoReq(Long codigo, String descricao, List<Long> unidades, LocalDate dataLimiteEtapa1) {
        return new AtualizarProcessoReq(codigo, descricao, TipoProcesso.MAPEAMENTO.name(), dataLimiteEtapa1, unidades);
    }

    @Test
    void testCriarProcesso_sucesso() throws Exception {
        List<Long> unidades = new ArrayList<>();
        unidades.add(1L); // Assumindo que a unidade com ID 1 existe

        CriarProcessoReq requestDTO = criarCriarProcessoReq(
                "Processo de Mapeamento Teste",
                unidades,
                LocalDate.now().plusDays(30)
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
                LocalDate.now().plusDays(30)
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
                LocalDate.now().plusDays(30)
        );

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
                LocalDate.now().plusDays(20)
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
                LocalDate.now().plusDays(40) // Nova data limite
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
                LocalDate.now().plusDays(30)
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
                LocalDate.now().plusDays(20)
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

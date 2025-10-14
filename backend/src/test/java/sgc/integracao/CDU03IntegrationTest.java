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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.modelo.TipoProcesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

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
@Transactional
@DisplayName("CDU-03: Manter processo")
@WithMockUser(username = "admin", roles = "ADMIN")
@Import(TestSecurityConfig.class)
public class CDU03IntegrationTest {
    private static final String API_PROCESSOS = "/api/processos";
    private static final String API_PROCESSOS_ID = "/api/processos/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Unidade unidade1;
    private Unidade unidade2;

    @BeforeEach
    void setUp() {
        unidade1 = unidadeRepo.save(new Unidade("Unidade Teste 1", "UT1"));
        unidade2 = unidadeRepo.save(new Unidade("Unidade Teste 2", "UT2"));
    }

    private CriarProcessoReq criarCriarProcessoReq(String descricao, List<Long> unidades, LocalDate dataLimiteEtapa1) {
        return new CriarProcessoReq(descricao, TipoProcesso.MAPEAMENTO.name(), dataLimiteEtapa1, unidades);
    }

    private AtualizarProcessoReq criarAtualizarProcessoReq(Long codigo, String descricao, List<Long> unidades, LocalDate dataLimiteEtapa1) {
        return new AtualizarProcessoReq(codigo, descricao, TipoProcesso.MAPEAMENTO.name(), dataLimiteEtapa1, unidades);
    }

    @Test
    void testCriarProcesso_sucesso() throws Exception {
        List<Long> unidades = List.of(unidade1.getCodigo());

        CriarProcessoReq requestDTO = criarCriarProcessoReq(
                "Processo de Mapeamento Teste",
                unidades,
                LocalDate.now().plusDays(30)
        );

        mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").isNumber())
                .andExpect(jsonPath("$.descricao").value("Processo de Mapeamento Teste"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value("CRIADO"));
    }

    @Test
    void testCriarProcesso_descricaoVazia_falha() throws Exception {
        List<Long> unidades = List.of(unidade1.getCodigo());

        CriarProcessoReq requestDTO = criarCriarProcessoReq(
                "", // Descrição vazia
                unidades,
                LocalDate.now().plusDays(30)
        );

        mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Preencha a descrição"));
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
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Pelo menos uma unidade participante deve ser incluída."));
    }

    @Test
    void testEditarProcesso_sucesso() throws Exception {
        List<Long> unidadesIniciais = List.of(unidade1.getCodigo());
        CriarProcessoReq criarRequestDTO = criarCriarProcessoReq(
                "Processo para Edição",
                unidadesIniciais,
                LocalDate.now().plusDays(20)
        );

        MvcResult result = mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        List<Long> unidadesEditadas = List.of(unidade1.getCodigo(), unidade2.getCodigo());

        AtualizarProcessoReq editarRequestDTO = criarAtualizarProcessoReq(
                processoId,
                "Processo Editado",
                unidadesEditadas,
                LocalDate.now().plusDays(40)
        );

        mockMvc.perform(put(API_PROCESSOS_ID, processoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarRequestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(processoId))
                .andExpect(jsonPath("$.descricao").value("Processo Editado"));
    }

    @Test
    void testEditarProcesso_processoNaoEncontrado_falha() throws Exception {
        List<Long> unidades = List.of(unidade1.getCodigo());

        AtualizarProcessoReq editarRequestDTO = criarAtualizarProcessoReq(
                999L, // Código que não existe
                "Processo Inexistente",
                unidades,
                LocalDate.now().plusDays(30)
        );

        mockMvc.perform(put(API_PROCESSOS_ID, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarRequestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoverProcesso_sucesso() throws Exception {
        List<Long> unidadesIniciais = List.of(unidade1.getCodigo());
        CriarProcessoReq criarRequestDTO = criarCriarProcessoReq(
                "Processo para Remoção",
                unidadesIniciais,
                LocalDate.now().plusDays(20)
        );

        MvcResult result = mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        mockMvc.perform(delete(API_PROCESSOS_ID, processoId))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_PROCESSOS_ID, processoId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoverProcesso_processoNaoEncontrado_falha() throws Exception {
        mockMvc.perform(delete(API_PROCESSOS_ID, 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
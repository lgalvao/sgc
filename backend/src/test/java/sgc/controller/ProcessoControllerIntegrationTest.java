package sgc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.model.Unidade;
import sgc.repository.ProcessoRepository;
import sgc.repository.UnidadeProcessoRepository;
import sgc.repository.UnidadeRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para os endpoints de Processo: create, iniciar, finalizar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProcessoControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Autowired
    private UnidadeProcessoRepository unidadeProcessoRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @Test
    public void create_and_start_and_finalize_flow() throws Exception {
        // Criar unidades que participarão do processo
        Unidade u1 = new Unidade();
        u1.setNome("Unidade Teste 1");
        u1.setSigla("UT1");
        u1.setTipo("OPERACIONAL");
        u1 = unidadeRepository.save(u1);

        Unidade u2 = new Unidade();
        u2.setNome("Unidade Teste 2");
        u2.setSigla("UT2");
        u2.setTipo("OPERACIONAL");
        u2 = unidadeRepository.save(u2);

        // Montar CreateProcessRequest JSON usando os IDs reais das unidades
        ObjectNode createReq = objectMapper.createObjectNode();
        createReq.put("descricao", "Processo integrado");
        createReq.put("tipo", "MAPEAMENTO");
        createReq.put("dataLimiteEtapa1", LocalDate.now().plusDays(7).toString());
        ArrayNode unidadesArray = createReq.putArray("unidades");
        unidadesArray.add(u1.getCodigo());
        unidadesArray.add(u2.getCodigo());
 
        // Criar processo
        String createBody = objectMapper.writeValueAsString(createReq);
        String processLocation = mockMvc.perform(post("/api/processos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assertThat(processLocation).isNotNull();

        // Extrair id do processo da Location
        String[] parts = processLocation.split("/");
        Long processoId = Long.valueOf(parts[parts.length - 1]);

        // Iniciar processo (MAPEAMENTO) — enviar lista de unidades no corpo
        String unidadesJson = objectMapper.writeValueAsString(List.of(u1.getCodigo(), u2.getCodigo()));
        mockMvc.perform(post("/api/processos/%d/iniciar".formatted(processoId))
                        .param("tipo", "MAPEAMENTO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unidadesJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_ANDAMENTO"));

        // Verificar registros criados em UNIDADE_PROCESSO
        var ups = unidadeProcessoRepository.findByProcessoCodigo(processoId);
        assertThat(ups).hasSize(2);
        assertThat(ups).anyMatch(u -> "UT1".equals(u.getSigla()));
        assertThat(ups).anyMatch(u -> "UT2".equals(u.getSigla()));

        // Tentar iniciar novamente — deve retornar 400 (já iniciado)
        mockMvc.perform(post("/api/processos/%d/iniciar".formatted(processoId))
                        .param("tipo", "MAPEAMENTO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unidadesJson))
                .andExpect(status().isBadRequest());

        // Finalizar processo
        mockMvc.perform(post("/api/processos/%d/finalizar".formatted(processoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("FINALIZADO"));

        // Verificar processo no repositório está com situação FINALIZADO
        var proc = processoRepository.findById(processoId).orElseThrow();
        assertThat(proc.getSituacao()).isEqualTo("FINALIZADO");
        assertThat(proc.getDataFinalizacao()).isNotNull();
    }
}
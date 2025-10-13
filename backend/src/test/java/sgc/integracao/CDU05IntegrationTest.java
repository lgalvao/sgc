package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.modelo.TipoProcesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = {"ADMIN"})
@Import(TestSecurityConfig.class)
@Transactional
public class CDU05IntegrationTest {
    private static final String API_PROCESSOS_ID_INICIAR_TIPO_REVISAO = "/api/processos/{id}/iniciar?tipo=REVISAO";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = new Unidade();
        unidade.setNome("Test Unit");
        unidade.setSigla("TU");
        unidadeRepo.save(unidade);

        Mapa mapa = new Mapa();
        mapaRepo.save(mapa);

        UnidadeMapa unidadeMapa = new UnidadeMapa(unidade.getCodigo());
        unidadeMapa.setMapaVigenteCodigo(mapa.getCodigo());
        unidadeMapaRepo.save(unidadeMapa);
    }

    private CriarProcessoReq criarCriarProcessoReq(String descricao, List<Long> unidades, LocalDate dataLimiteEtapa1) {
        return new CriarProcessoReq(descricao, TipoProcesso.REVISAO.name(), dataLimiteEtapa1, unidades);
    }

    @Test
    void testIniciarProcessoRevisao_sucesso() throws Exception {
        // 1. Criar um processo de revisão para ser iniciado
        List<Long> unidades = new ArrayList<>();
        unidades.add(unidade.getCodigo());
        CriarProcessoReq criarRequestDTO = criarCriarProcessoReq(
                "Processo de Revisão para Iniciar",
                unidades,
                LocalDate.now().plusDays(30)
        );

        MvcResult result = mockMvc.perform(post("/api/processos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // 2. Iniciar o processo de revisão
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR_TIPO_REVISAO, processoId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidades)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(processoId))
                .andExpect(jsonPath("$.situacao").value("EM_ANDAMENTO"));
    }

    @Test
    void testIniciarProcessoRevisao_unidadeSemMapaVigente_falha() throws Exception {
        // 1. Criar uma unidade e um processo sem associar um mapa vigente
        Unidade unidadeSemMapa = new Unidade();
        unidadeSemMapa.setNome("Unidade Sem Mapa");
        unidadeSemMapa.setSigla("USM");
        unidadeRepo.save(unidadeSemMapa);

        List<Long> unidades = new ArrayList<>();
        unidades.add(unidadeSemMapa.getCodigo());
        CriarProcessoReq criarRequestDTO = criarCriarProcessoReq(
                "Processo de Revisão para Unidade Sem Mapa",
                unidades,
                LocalDate.now().plusDays(30)
        );

        MvcResult result = mockMvc.perform(post("/api/processos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // 2. Tentar iniciar o processo de revisão (deve falhar)
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR_TIPO_REVISAO, processoId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidades)))
                .andExpect(status().isUnprocessableEntity()); // Espera-se um erro de negócio
    }

    @Test
    void testIniciarProcessoRevisao_processoNaoEncontrado_falha() throws Exception {
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR_TIPO_REVISAO, 999L).with(csrf())) // ID que não existe
                .andExpect(status().isNotFound());
    }

    @Test
    void testIniciarProcessoRevisao_processoJaIniciado_falha() throws Exception {
        // 1. Criar e iniciar um processo de revisão
        List<Long> unidades = new ArrayList<>();
        unidades.add(unidade.getCodigo());
        CriarProcessoReq criarRequestDTO = criarCriarProcessoReq(
                "Processo de Revisão para Iniciar Duas Vezes",
                unidades,
                LocalDate.now().plusDays(30)
        );

        MvcResult result = mockMvc.perform(post("/api/processos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // Inicia o processo a primeira vez
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR_TIPO_REVISAO, processoId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidades)))
                .andExpect(status().isOk());

        // 2. Tentar iniciar o processo novamente
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR_TIPO_REVISAO, processoId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unidades)))
                .andExpect(status().isConflict()); // Espera-se um erro de negócio
    }
}
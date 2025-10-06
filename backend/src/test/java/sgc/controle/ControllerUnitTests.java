package sgc.controle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeController;
import sgc.atividade.AtividadeRepository;
import sgc.competencia.*;
import sgc.conhecimento.ConhecimentoController;
import sgc.conhecimento.ConhecimentoRepository;
import sgc.mapa.MapaController;
import sgc.mapa.MapaRepository;
import sgc.processo.ProcessoController;
import sgc.processo.ProcessoRepository;
import sgc.processo.ProcessoService;
import sgc.subprocesso.SubprocessoController;
import sgc.subprocesso.SubprocessoRepository;
import sgc.subprocesso.SubprocessoService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários dos controladores REST principais.
 * Usei @WebMvcTest para testar a camada web isolada, com repositórios mocked.
 * <p>
 * Os testes cobrem:
 * - Competência (listar, criar)
 * - Atividade (listar, criar)
 * - Associação CompetenciaAtividade (vincular)
 */
@WebMvcTest(controllers = {
        CompetenciaController.class,
        AtividadeController.class,
        CompetenciaAtividadeController.class,
        ProcessoController.class,
        SubprocessoController.class,
        MapaController.class,
        ConhecimentoController.class
})
public class ControllerUnitTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompetenciaRepository competenciaRepository;

    @MockitoBean
    private AtividadeRepository atividadeRepository;

    @MockitoBean
    private ConhecimentoRepository conhecimentoRepository;

    @MockitoBean
    private MapaRepository mapaRepository;

    @MockitoBean
    private ProcessoRepository processoRepository;

    @MockitoBean
    private ProcessoService processoService;

    @MockitoBean
    private SubprocessoService subprocessoService;

    @MockitoBean
    private SubprocessoRepository subprocessoRepository;

    @MockitoBean
    private CompetenciaAtividadeRepository competenciaAtividadeRepository;

    // --- Competencia --------------------------------
    @Test
    public void listarCompetencias_deveRetornarLista() throws Exception {
        Competencia c = new Competencia();
        c.setCodigo(1L);
        c.setDescricao("Competência Teste");

        when(competenciaRepository.findAll()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/competencias"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].codigo").value(1))
                .andExpect(jsonPath("$[0].descricao").value("Competência Teste"));
    }

    @Test
    public void criarCompetencia_deveSalvarERetornarCriado() throws Exception {
        Competencia entrada = new Competencia();
        entrada.setDescricao("Nova Competência");

        Competencia salvo = new Competencia();
        salvo.setCodigo(10L);
        salvo.setDescricao("Nova Competência");

        when(competenciaRepository.save(any(Competencia.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/competencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/competencias/10")))
                .andExpect(jsonPath("$.codigo").value(10))
                .andExpect(jsonPath("$.descricao").value("Nova Competência"));
    }

    // --- Atividade --------------------------------
    @Test
    public void listarAtividades_deveRetornarLista() throws Exception {
        Atividade a = new Atividade();
        a.setCodigo(5L);
        a.setDescricao("Atividade Teste");

        when(atividadeRepository.findAll()).thenReturn(List.of(a));

        mockMvc.perform(get("/api/atividades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(5))
                .andExpect(jsonPath("$[0].descricao").value("Atividade Teste"));
    }

    @Test
    public void criarAtividade_deveSalvarERetornarCriado() throws Exception {
        Atividade entrada = new Atividade();
        entrada.setDescricao("Atividade Nova");

        Atividade salvo = new Atividade();
        salvo.setCodigo(20L);
        salvo.setDescricao("Atividade Nova");

        when(atividadeRepository.save(any(Atividade.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/atividades/20")))
                .andExpect(jsonPath("$.codigo").value(20))
                .andExpect(jsonPath("$.descricao").value("Atividade Nova"));
    }

    // --- CompetenciaAtividade (vincular/desvincular) ---
    @Test
    public void vincularCompetenciaAtividade_deveCriarVinculo() throws Exception {
        // Preparar entidades existentes
        Atividade at = new Atividade();
        at.setCodigo(100L);

        Competencia comp = new Competencia();
        comp.setCodigo(200L);

        when(atividadeRepository.findById(100L)).thenReturn(Optional.of(at));
        when(competenciaRepository.findById(200L)).thenReturn(Optional.of(comp));

        // Simular inexistência do vínculo e salvar
        CompetenciaAtividade.Id id = new CompetenciaAtividade.Id();
        id.setAtividadeCodigo(100L);
        id.setCompetenciaCodigo(200L);

        CompetenciaAtividade salvo = new CompetenciaAtividade();
        salvo.setId(id);
        salvo.setAtividade(at);
        salvo.setCompetencia(comp);

        when(competenciaAtividadeRepository.existsById(ArgumentMatchers.any())).thenReturn(false);
        when(competenciaAtividadeRepository.save(any(CompetenciaAtividade.class))).thenReturn(salvo);

        var payload = new java.util.HashMap<String, Long>();
        payload.put("atividadeCodigo", 100L);
        payload.put("competenciaCodigo", 200L);

        mockMvc.perform(post("/api/competencia-atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.atividadeCodigo").value(100))
                .andExpect(jsonPath("$.id.competenciaCodigo").value(200));
    }

    @Test
    public void desvincularCompetenciaAtividade_deveRemoverVinculo() throws Exception {
        CompetenciaAtividade.Id id = new CompetenciaAtividade.Id();
        id.setAtividadeCodigo(1000L);
        id.setCompetenciaCodigo(2000L);

        // Como a classe Id não sobrescreve equals/hashCode, usar matcher genérico
        when(competenciaAtividadeRepository.existsById(ArgumentMatchers.any())).thenReturn(true);

        mockMvc.perform(delete("/api/competencia-atividades")
                        .param("atividadeCodigo", "1000")
                        .param("competenciaCodigo", "2000"))
                .andExpect(status().isNoContent());
    }
}
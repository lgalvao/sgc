package sgc.subprocesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.usuario.UsuarioService;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesReq;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoMapaService;
import sgc.subprocesso.service.SubprocessoMapaWorkflowService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubprocessoMapaController.class)
@Import(RestExceptionHandler.class)
class SubprocessoMapaControllerTest {

    @MockitoBean
    private SubprocessoMapaService subprocessoMapaService;
    @MockitoBean
    private MapaService mapaService;
    @MockitoBean
    private MapaVisualizacaoService mapaVisualizacaoService;
    @MockitoBean
    private ImpactoMapaService impactoMapaService;
    @MockitoBean
    private SubprocessoDtoService subprocessoDtoService;
    @MockitoBean
    private SubprocessoMapaWorkflowService subprocessoMapaWorkflowService;
    @MockitoBean
    private SubprocessoConsultaService subprocessoConsultaService;
    @MockitoBean
    private UsuarioService usuarioService;
    @MockitoBean
    private sgc.subprocesso.service.SubprocessoService subprocessoService;
    @MockitoBean
    private sgc.subprocesso.service.SubprocessoContextoService subprocessoContextoService;

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("verificarImpactos")
    @WithMockUser
    void verificarImpactos() throws Exception {
        when(impactoMapaService.verificarImpactos(eq(1L), any()))
                .thenReturn(ImpactoMapaDto.semImpacto());

        mockMvc.perform(get("/api/subprocessos/1/impactos-mapa")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapa")
    @WithMockUser
    void obterMapa() throws Exception {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(subprocessoConsultaService.getSubprocessoComMapa(1L)).thenReturn(sp);
        when(mapaService.obterMapaCompleto(10L, 1L)).thenReturn(new MapaCompletoDto());

        mockMvc.perform(get("/api/subprocessos/1/mapa")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapaVisualizacao")
    @WithMockUser
    void obterMapaVisualizacao() throws Exception {
        when(mapaVisualizacaoService.obterMapaParaVisualizacao(1L))
                .thenReturn(new MapaVisualizacaoDto());

        mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("salvarMapa")
    @WithMockUser
    void salvarMapa() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setObservacoes("obs");
        req.setCompetencias(List.of());

        when(subprocessoMapaWorkflowService.salvarMapaSubprocesso(eq(1L), any(), any()))
                .thenReturn(new MapaCompletoDto());

        mockMvc.perform(
                        post("/api/subprocessos/1/mapa/atualizar")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapaParaAjuste")
    @WithMockUser
    void obterMapaParaAjuste() throws Exception {
        when(subprocessoDtoService.obterMapaParaAjuste(1L))
                .thenReturn(MapaAjusteDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("salvarAjustesMapa")
    @WithMockUser
    void salvarAjustesMapa() throws Exception {
        SalvarAjustesReq req = new SalvarAjustesReq();
        req.setCompetencias(List.of());

        mockMvc.perform(
                        post("/api/subprocessos/1/mapa-ajuste/atualizar")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapaCompleto")
    @WithMockUser
    void obterMapaCompleto() throws Exception {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(subprocessoConsultaService.getSubprocessoComMapa(1L)).thenReturn(sp);
        when(mapaService.obterMapaCompleto(10L, 1L)).thenReturn(new MapaCompletoDto());

        mockMvc.perform(get("/api/subprocessos/1/mapa-completo")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("salvarMapaCompleto")
    @WithMockUser
    void salvarMapaCompleto() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setObservacoes("obs");
        req.setCompetencias(List.of());

        when(subprocessoMapaWorkflowService.salvarMapaSubprocesso(eq(1L), any(), any()))
                .thenReturn(new MapaCompletoDto());

        mockMvc.perform(
                        post("/api/subprocessos/1/mapa-completo/atualizar")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("adicionarCompetencia")
    @WithMockUser
    void adicionarCompetencia() throws Exception {
        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao("Comp");
        req.setAtividadesIds(List.of(1L, 2L)); // Corrigido: lista não pode ser vazia

        when(subprocessoMapaWorkflowService.adicionarCompetencia(eq(1L), any(), any()))
                .thenReturn(new MapaCompletoDto());

        mockMvc.perform(
                        post("/api/subprocessos/1/competencias")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("adicionarCompetencia - deve retornar 400 quando descrição está vazia")
    @WithMockUser
    void adicionarCompetencia_DeveRetornar400QuandoDescricaoVazia() throws Exception {
        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao(""); // Descrição vazia - deve falhar
        req.setAtividadesIds(List.of(1L));

        mockMvc.perform(
                        post("/api/subprocessos/1/competencias")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("adicionarCompetencia - deve retornar 400 quando lista de atividades está vazia")
    @WithMockUser
    void adicionarCompetencia_DeveRetornar400QuandoAtividadesVazio() throws Exception {
        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao("Competência válida");
        req.setAtividadesIds(List.of()); // Lista vazia - deve falhar

        mockMvc.perform(
                        post("/api/subprocessos/1/competencias")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("adicionarCompetencia - deve retornar 400 quando lista de atividades é null")
    @WithMockUser
    void adicionarCompetencia_DeveRetornar400QuandoAtividadesNull() throws Exception {
        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao("Competência válida");
        req.setAtividadesIds(null); // Null - deve falhar

        mockMvc.perform(
                        post("/api/subprocessos/1/competencias")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("atualizarCompetencia")
    @WithMockUser
    void atualizarCompetencia() throws Exception {
        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao("Comp");
        req.setAtividadesIds(List.of(1L)); // Corrigido: lista não pode ser vazia

        when(subprocessoMapaWorkflowService.atualizarCompetencia(eq(1L), eq(10L), any(), any()))
                .thenReturn(new MapaCompletoDto());

        mockMvc.perform(
                        post("/api/subprocessos/1/competencias/10/atualizar")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("removerCompetencia")
    @WithMockUser
    void removerCompetencia() throws Exception {
        when(subprocessoMapaWorkflowService.removerCompetencia(eq(1L), eq(10L), any()))
                .thenReturn(new MapaCompletoDto());

        mockMvc.perform(post("/api/subprocessos/1/competencias/10/remover").with(csrf()))
                .andExpect(status().isOk());
    }


}

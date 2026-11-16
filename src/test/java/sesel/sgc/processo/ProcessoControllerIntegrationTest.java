package sesel.sgc.processo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@AutoConfigureMockMvc
class ProcessoControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoRepository processoRepository;

    @MockitoBean
    private ProcessoService processoService;

    @WithMockUser(username = "admin", roles = { "SEDOC" })
    @Test
    void deveRetornarOkNaListagemDeProcessos() throws Exception {
        Processo processo = new Processo();
        processo.setNome("Processo Teste %s".formatted(UUID.randomUUID()));
        processo.setDataLimite(LocalDate.now().plusDays(10));
        processoRepository.save(processo);

        mockMvc.perform(get("/processo"))
                .andExpect(status().isOk())
                .andExpect(view().name("processo/processo-lista"));
    }

    @WithMockUser
    @Test
    void deveListarProcessos() throws Exception {
        when(processoService.listarTodosProcessosDTO()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/processo"))
                .andExpect(status().isOk())
                .andExpect(view().name("processo/processo-lista"));
    }

    @WithMockUser
    @Test
    void deveExibirDetalhesDoProcesso() throws Exception {
        UUID processoId = UUID.randomUUID();
        ProcessoDto dto = new ProcessoDto();
        when(processoService.buscarProcessoDTO(processoId)).thenReturn(dto);

        mockMvc.perform(get("/processo/%s".formatted(processoId)))
                .andExpect(status().isOk())
                .andExpect(view().name("processo/processo-detalhes"));
    }

    @WithMockUser
    @Test
    void deveRetornarErroQuandoProcessoNaoEncontrado() throws Exception {
        UUID processoId = UUID.randomUUID();
        when(processoService.buscarProcessoDTO(processoId)).thenThrow(new ProcessoNaoEncontradoErro(processoId));

        mockMvc.perform(get("/processo/%s".formatted(processoId)))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @WithMockUser
    @Test
    void deveExibirFormularioNovoProcesso() throws Exception {
        when(processoService.listarUnidadesDisponiveis()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/processo/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("processo/processo-form"));
    }

    @WithMockUser
    @Test
    void deveCriarProcessoERedirecionarParaHome() throws Exception {
        mockMvc.perform(post("/processo")
                .param("nome", "Processo Teste Integração")
                        .param("dataLimite", "2030-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(processoService).criarProcesso(any(Processo.class));
    }
}
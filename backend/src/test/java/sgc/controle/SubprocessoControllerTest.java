package sgc.controle;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.atividade.Atividade;
import sgc.subprocesso.SubprocessoCadastroDTO;
import sgc.subprocesso.SubprocessoController;
import sgc.subprocesso.SubprocessoRepository;
import sgc.subprocesso.SubprocessoService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários do controlador SubprocessoController.
 * - Usa @WebMvcTest para isolar o controle e mockar o SubprocessoService.
 * - Verifica respostas esperadas para os endpoints de disponibilização e do agregador de cadastro.
 */
@WebMvcTest(controllers = SubprocessoController.class)
public class SubprocessoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubprocessoService subprocessoService;

    @MockitoBean
    private SubprocessoRepository subprocessoRepository;

    @Test
    public void disponibilizarCadastro_whenAtividadesSemConhecimento_thenBadRequest() throws Exception {
        Atividade a = new Atividade();
        a.setCodigo(123L);
        a.setDescricao("Atividade sem conhecimento");

        Mockito.when(subprocessoService.obterAtividadesSemConhecimento(eq(1L)))
                .thenReturn(List.of(a));

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-cadastro")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.atividadesSemConhecimento").isArray())
                .andExpect(jsonPath("$.atividadesSemConhecimento[0].id").value(123));
    }

    @Test
    public void disponibilizarCadastro_whenAllGood_thenOkAndServiceCalled() throws Exception {
        Mockito.when(subprocessoService.obterAtividadesSemConhecimento(eq(2L)))
                .thenReturn(Collections.emptyList());
        doNothing().when(subprocessoService).disponibilizarCadastroAcao(eq(2L));

        mockMvc.perform(post("/api/subprocessos/2/disponibilizar-cadastro")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cadastro de atividades disponibilizado")));

        verify(subprocessoService).disponibilizarCadastroAcao(eq(2L));
    }

    @Test
    public void disponibilizarRevisao_whenAllGood_thenOkAndServiceCalled() throws Exception {
        Mockito.when(subprocessoService.obterAtividadesSemConhecimento(eq(3L)))
                .thenReturn(Collections.emptyList());
        doNothing().when(subprocessoService).disponibilizarRevisaoAcao(eq(3L));

        mockMvc.perform(post("/api/subprocessos/3/disponibilizar-revisao")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Revisão do cadastro de atividades disponibilizada")));

        verify(subprocessoService).disponibilizarRevisaoAcao(eq(3L));
    }

    @Test
    public void obterCadastro_returnsAggregatedPayload() throws Exception {
        SubprocessoCadastroDTO payload = new SubprocessoCadastroDTO(
                5L,
                "UNI",
                Collections.emptyList()
        );

        Mockito.when(subprocessoService.obterCadastro(eq(5L))).thenReturn(payload);

        mockMvc.perform(get("/api/subprocessos/5/cadastro")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocessoId").value(5))
                .andExpect(jsonPath("$.unidadeSigla").value("UNI"))
                .andExpect(jsonPath("$.atividades").isArray());
    }
}

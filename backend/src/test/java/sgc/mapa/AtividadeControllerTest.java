package sgc.mapa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtividadeController.class)
@Import({TestSecurityConfig.class, RestExceptionHandler.class})
@DisplayName("Testes do Controlador de Atividades")
class AtividadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtividadeService atividadeService;

    @MockitoBean
    private SubprocessoService subprocessoService;

    @Nested
    @DisplayName("Operações de Atividade")
    class OperacoesAtividade {
        @Test
        @DisplayName("Deve listar atividades")
        @WithMockUser
        void deveListar() throws Exception {
            Mockito.when(atividadeService.listar()).thenReturn(List.of());
            mockMvc.perform(get("/api/atividades"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve obter por ID")
        @WithMockUser
        void deveObterPorId() throws Exception {
            AtividadeDto dto = new AtividadeDto();
            dto.setCodigo(1L);
            Mockito.when(atividadeService.obterPorCodigo(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/atividades/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1));
        }

        @Test
        @DisplayName("Deve criar atividade")
        @WithMockUser(username = "123")
        void deveCriarAtividade() throws Exception {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);
            dto.setDescricao("Teste");

            AtividadeDto salvo = new AtividadeDto();
            salvo.setCodigo(10L);
            salvo.setMapaCodigo(1L);

            Mockito.when(atividadeService.criar(any(), any())).thenReturn(salvo);

            // Mocking helper methods implicit calls
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(99L);
            Mockito.when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
            Mockito.when(subprocessoService.obterStatus(99L)).thenReturn(SubprocessoSituacaoDto.builder().build());
            Mockito.when(subprocessoService.listarAtividadesSubprocesso(99L)).thenReturn(List.of());

            mockMvc.perform(post("/api/atividades")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));
        }

        @Test
        @DisplayName("Deve atualizar atividade")
        @WithMockUser
        void deveAtualizarAtividade() throws Exception {
            // Mocking helper methods implicit calls
            Atividade atividade = new Atividade();
            atividade.setMapa(new Mapa());
            atividade.getMapa().setCodigo(1L);
            Mockito.when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividade);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(99L);
            Mockito.when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
            Mockito.when(subprocessoService.obterStatus(99L)).thenReturn(SubprocessoSituacaoDto.builder().build());

            mockMvc.perform(post("/api/atividades/1/atualizar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mapaCodigo\": 1, \"descricao\": \"Teste\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve excluir atividade")
        @WithMockUser
        void deveExcluirAtividade() throws Exception {
            // Mocking helper methods implicit calls
            Atividade atividade = new Atividade();
            atividade.setMapa(new Mapa());
            atividade.getMapa().setCodigo(1L);
            Mockito.when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividade);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(99L);
            Mockito.when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
            Mockito.when(subprocessoService.obterStatus(99L)).thenReturn(SubprocessoSituacaoDto.builder().build());

            mockMvc.perform(post("/api/atividades/1/excluir")
                            .with(csrf()))
                    .andExpect(status().isOk());

            Mockito.verify(atividadeService).excluir(1L);
        }
    }

    @Nested
    @DisplayName("Operações de Conhecimento")
    class OperacoesConhecimento {
        @Test
        @DisplayName("Deve listar conhecimentos")
        @WithMockUser
        void deveListarConhecimentos() throws Exception {
            Mockito.when(atividadeService.listarConhecimentos(1L)).thenReturn(List.of());
            mockMvc.perform(get("/api/atividades/1/conhecimentos"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve criar conhecimento")
        @WithMockUser
        void deveCriarConhecimento() throws Exception {
            ConhecimentoDto salvo = new ConhecimentoDto();
            salvo.setCodigo(20L);

            Mockito.when(atividadeService.criarConhecimento(eq(1L), any())).thenReturn(salvo);

            // Helpers
            Atividade atividade = new Atividade();
            atividade.setMapa(new Mapa());
            atividade.getMapa().setCodigo(1L);
            Mockito.when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividade);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(99L);
            Mockito.when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
            Mockito.when(subprocessoService.obterStatus(99L)).thenReturn(SubprocessoSituacaoDto.builder().build());

            mockMvc.perform(post("/api/atividades/1/conhecimentos")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"atividadeCodigo\": 1, \"descricao\": \"C1\"}"))
                    .andExpect(status().isCreated());
        }
    }
}

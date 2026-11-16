package sesel.sgc.processounidade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.processo.ProcessoDto;
import sesel.sgc.processo.ProcessoService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProcessoUnidadeControllerIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ProcessoService processoService;

        @MockitoBean
        private ProcessoUnidadeService processoUnidadeService;

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveVisualizarProcessoUnidadeComSucesso() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();

                when(processoService.buscarProcessoDTO(processoId))
                                .thenReturn(new ProcessoDto());

                when(processoUnidadeService.buscarProcessoUnidadeDetalhada(processoId, unidadeId))
                                .thenReturn(new ProcessoUnidade());

                ProcessoDto.ProcessoUnidadeDto dto = new ProcessoDto.ProcessoUnidadeDto();
                dto.setSituacao(MapaUnidadeSituacao.EM_ANDAMENTO);

                when(processoUnidadeService.toProcessoUnidadeDto(any())).thenReturn(dto);

                mockMvc.perform(get("/processo/" + processoId + "/unidade/" + unidadeId))
                                .andExpect(status().isOk())
                                .andExpect(view().name("processo/processo-unidade-detalhes"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveRedirecionarQuandoProcessoUnidadeNaoEncontrado() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();

                when(processoService.buscarProcessoDTO(processoId)).thenReturn(new ProcessoDto());

                when(processoUnidadeService.buscarProcessoUnidadeDetalhada(processoId, unidadeId))
                                .thenThrow(new ProcessoUnidadeNaoEncontradoErro("erro"));

                mockMvc.perform(get("/processo/%s/unidade/%s".formatted(processoId, unidadeId)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*?error=*"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveFinalizarProcessoUnidadeComSucesso() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();

                mockMvc.perform(post("/processo/%s/unidade/%s/finalizar".formatted(processoId, unidadeId)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*/unidade/*?success=*"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveRedirecionarAoFinalizarProcessoUnidadeNaoEncontrado() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();
                doThrow(new ProcessoUnidadeNaoEncontradoErro("erro")).when(processoUnidadeService)
                                .finalizarProcessoUnidade(processoId, unidadeId);
                mockMvc.perform(post("/processo/" + processoId + "/unidade/" + unidadeId + "/finalizar"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*?error=*"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveReabrirProcessoUnidadeComSucesso() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();
                mockMvc.perform(post("/processo/" + processoId + "/unidade/" + unidadeId + "/reabrir"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*/unidade/*?success=*"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveRedirecionarAoReabrirProcessoUnidadeNaoEncontrado() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();
                doThrow(new ProcessoUnidadeNaoEncontradoErro("erro")).when(processoUnidadeService)
                                .reabrirProcessoUnidade(processoId, unidadeId);
                mockMvc.perform(post("/processo/" + processoId + "/unidade/" + unidadeId + "/reabrir"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*?error=*"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveRedirecionarQuandoErroGenericoAoVisualizarProcessoUnidade() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();
                when(processoService.buscarProcessoDTO(processoId)).thenReturn(new ProcessoDto());
                when(processoUnidadeService.buscarProcessoUnidadeDetalhada(processoId, unidadeId))
                                .thenThrow(new RuntimeException("erro inesperado"));
                mockMvc.perform(get("/processo/%s/unidade/%s".formatted(processoId, unidadeId)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*?error=*"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveRedirecionarQuandoErroGenericoAoFinalizarProcessoUnidade() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();
                doThrow(new RuntimeException("erro inesperado")).when(processoUnidadeService)
                                .finalizarProcessoUnidade(processoId, unidadeId);
                mockMvc.perform(post("/processo/%s/unidade/%s/finalizar".formatted(processoId, unidadeId)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*?error=*"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Test
        void deveRedirecionarQuandoErroGenericoAoReabrirProcessoUnidade() throws Exception {
                UUID processoId = UUID.randomUUID();
                UUID unidadeId = UUID.randomUUID();
                doThrow(new RuntimeException("erro inesperado")).when(processoUnidadeService)
                                .reabrirProcessoUnidade(processoId, unidadeId);
                mockMvc.perform(post("/processo/%s/unidade/%s/reabrir".formatted(processoId, unidadeId)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("/processo/*?error=*"));
        }
}
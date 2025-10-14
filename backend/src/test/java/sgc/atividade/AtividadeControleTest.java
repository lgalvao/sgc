package sgc.atividade;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtividadeControle.class)
@WithMockUser
@AutoConfigureMockMvc
class AtividadeControleTest {
    private static final String ATIVIDADE_TESTE = "Atividade Teste";
    private static final String API_ATIVIDADES = "/api/atividades";
    private static final String API_ATIVIDADES_ID = "/api/atividades/{id}";
    private static final String API_ATIVIDADES_1 = "/api/atividades/1";
    private static final String API_ATIVIDADES_99 = "/api/atividades/99";
    private static final String NOVA_ATIVIDADE = "Nova Atividade";
    private static final String DESCRICAO_ATUALIZADA = "Descrição Atualizada";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AtividadeService atividadeService;

    @MockitoBean
    private AtividadeMapper atividadeMapper;

    @MockitoBean
    private ConhecimentoMapper conhecimentoMapper;

    @Nested
    @DisplayName("Testes para CRUD de Atividades")
    class ListarAtividades {
        @Test
        @DisplayName("Deve retornar lista de atividades com status 200 OK")
        void deveRetornarListaDeAtividades() throws Exception {
            var atividadeDto = new AtividadeDto(1L, null, ATIVIDADE_TESTE);

            when(atividadeService.listar()).thenReturn(List.of(atividadeDto));

            mockMvc.perform(get(API_ATIVIDADES))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].descricao").value(ATIVIDADE_TESTE));
        }

        @Test
        @DisplayName("Deve retornar lista vazia com status 200 OK")
        void deveRetornarListaVazia() throws Exception {
            when(atividadeService.listar()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_ATIVIDADES))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes para obter atividade por ID")
    class ObterAtividadePorId {

        @Test
        @DisplayName("Deve retornar uma atividade com status 200 OK")
        void deveRetornarAtividadePorId() throws Exception {
            var atividadeDto = new AtividadeDto(1L, null, ATIVIDADE_TESTE);

            when(atividadeService.obterPorId(1L)).thenReturn(atividadeDto);

            mockMvc.perform(get(API_ATIVIDADES_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(atividadeService.obterPorId(99L)).thenThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(""));

            mockMvc.perform(get(API_ATIVIDADES_99))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para criar atividade")
    class CriarAtividade {

        @Test
        @DisplayName("Deve criar uma atividade e retornar 201 Created")
        void deveCriarAtividade() throws Exception {
            var atividadeDto = new AtividadeDto(null, 10L, NOVA_ATIVIDADE);
            var atividadeSalvaDto = new AtividadeDto(1L, 10L, NOVA_ATIVIDADE);

            when(atividadeService.criar(any(AtividadeDto.class), eq("user"))).thenReturn(atividadeSalvaDto);

            mockMvc.perform(post(API_ATIVIDADES).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atividadeDto))
                            .with(user("user")))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", API_ATIVIDADES_1))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value(NOVA_ATIVIDADE))
                    .andExpect(jsonPath("$.mapaCodigo").value(10L));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para DTO inválido")
        void deveRetornarBadRequestParaDtoInvalido() throws Exception {
            var atividadeDto = new AtividadeDto(null, null, ""); // Descrição vazia

            mockMvc.perform(post(API_ATIVIDADES).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atividadeDto))
                            .with(user("user")))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testes para atualizar atividade")
    class AtualizarAtividade {
        @Test
        @DisplayName("Deve atualizar uma atividade e retornar 200 OK")
        void deveAtualizarAtividade() throws Exception {
            var atividadeDto = new AtividadeDto(1L, null, DESCRICAO_ATUALIZADA);
            var atividadeAtualizadaDto = new AtividadeDto(1L, null, DESCRICAO_ATUALIZADA);

            when(atividadeService.atualizar(eq(1L), any(AtividadeDto.class))).thenReturn(atividadeAtualizadaDto);

            mockMvc.perform(put(API_ATIVIDADES_1).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atividadeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descricao").value(DESCRICAO_ATUALIZADA));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            var atividadeDto = new AtividadeDto(99L, null, "Tanto faz");

            when(atividadeService.atualizar(eq(99L), any(AtividadeDto.class))).thenThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(""));

            mockMvc.perform(put(API_ATIVIDADES_99).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atividadeDto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para excluir atividade")
    class ExcluirAtividade {

        @Test
        @DisplayName("Deve excluir uma atividade e retornar 204 No Content")
        void deveExcluirAtividade() throws Exception {
            doNothing().when(atividadeService).excluir(1L);

            mockMvc.perform(delete(API_ATIVIDADES_1).with(csrf()))
                    .andExpect(status().isNoContent());

            verify(atividadeService, times(1)).excluir(1L);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            doThrow(new sgc.comum.erros.ErroDominioNaoEncontrado("")).when(atividadeService).excluir(99L);

            mockMvc.perform(delete(API_ATIVIDADES_99).with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para CRUD de Conhecimentos aninhados")
    class ConhecimentoEndpoints {

        private static final String API_CONHECIMENTOS = "/api/atividades/1/conhecimentos";
        private static final String API_CONHECIMENTOS_ID = "/api/atividades/1/conhecimentos/{id}";
        private static final String API_CONHECIMENTOS_1 = "/api/atividades/1/conhecimentos/1";
        private static final String NOVO_CONHECIMENTO = "Novo Conhecimento";

        @Test
        @DisplayName("Deve listar conhecimentos de uma atividade")
        void deveListarConhecimentos() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(1L, 1L, "Conhecimento Teste");

            when(atividadeService.listarConhecimentos(1L)).thenReturn(List.of(conhecimentoDto));

            mockMvc.perform(get(API_CONHECIMENTOS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @DisplayName("Deve criar um conhecimento para uma atividade")
        void deveCriarConhecimento() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(null, 1L, NOVO_CONHECIMENTO);
            var conhecimentoSalvoDto = new ConhecimentoDto(1L, 1L, NOVO_CONHECIMENTO);

            when(atividadeService.criarConhecimento(eq(1L), any(ConhecimentoDto.class))).thenReturn(conhecimentoSalvoDto);

            mockMvc.perform(post(API_CONHECIMENTOS).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(conhecimentoDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_CONHECIMENTOS_1))
                .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @DisplayName("Deve atualizar um conhecimento")
        void deveAtualizarConhecimento() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(1L, 1L, "Atualizado");

            when(atividadeService.atualizarConhecimento(eq(1L), eq(1L), any(ConhecimentoDto.class))).thenReturn(conhecimentoDto);

            mockMvc.perform(put(API_CONHECIMENTOS_1).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(conhecimentoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Atualizado"));
        }

        @Test
        @DisplayName("Deve excluir um conhecimento")
        void deveExcluirConhecimento() throws Exception {
            doNothing().when(atividadeService).excluirConhecimento(1L, 1L);

            mockMvc.perform(delete(API_CONHECIMENTOS_1).with(csrf()))
                .andExpect(status().isNoContent());

            verify(atividadeService, times(1)).excluirConhecimento(1L, 1L);
        }
    }
}
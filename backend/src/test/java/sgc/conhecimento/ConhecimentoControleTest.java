package sgc.conhecimento;

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
import sgc.atividade.modelo.Atividade;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConhecimentoControle.class)
@AutoConfigureMockMvc
@WithMockUser
class ConhecimentoControleTest {
    private static final String CONHECIMENTO_TESTE = "Conhecimento Teste";
    private static final String API_CONHECIMENTOS = "/api/conhecimentos";
    private static final String API_CONHECIMENTOS_1 = "/api/conhecimentos/1";
    private static final String API_CONHECIMENTOS_99 = "/api/conhecimentos/99";
    private static final String NOVO_CONHECIMENTO = "Novo Conhecimento";
    private static final String DESCRICAO_ATUALIZADA = "Descrição Atualizada";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConhecimentoRepo conhecimentoRepo;

    @MockitoBean
    private ConhecimentoMapper conhecimentoMapper;

    @Nested
    @DisplayName("Testes para listar conhecimentos")
    class ListarConhecimentos {

        @Test
        @DisplayName("Deve retornar lista de conhecimentos com status 200 OK")
        void deveRetornarListaDeConhecimentos() throws Exception {
            var conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setDescricao(CONHECIMENTO_TESTE);

            var conhecimentoDto = new ConhecimentoDto(1L, 10L, CONHECIMENTO_TESTE);

            when(conhecimentoRepo.findAll()).thenReturn(List.of(conhecimento));
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoDto);

            mockMvc.perform(get(API_CONHECIMENTOS))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].descricao").value(CONHECIMENTO_TESTE));
        }

        @Test
        @DisplayName("Deve retornar lista vazia com status 200 OK")
        void deveRetornarListaVazia() throws Exception {
            when(conhecimentoRepo.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_CONHECIMENTOS))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes para obter conhecimento por ID")
    class ObterConhecimentoPorId {

        @Test
        @DisplayName("Deve retornar um conhecimento com status 200 OK")
        void deveRetornarConhecimentoPorId() throws Exception {
            var conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            var conhecimentoDto = new ConhecimentoDto(1L, 10L, CONHECIMENTO_TESTE);

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoDto);

            mockMvc.perform(get(API_CONHECIMENTOS_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(conhecimentoRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get(API_CONHECIMENTOS_99))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para criar conhecimento")
    class CriarConhecimento {

        @Test
        @DisplayName("Deve criar um conhecimento e retornar 201 Created")
        void deveCriarConhecimento() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(null, 10L, NOVO_CONHECIMENTO);
            var conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setDescricao(NOVO_CONHECIMENTO);
            var atividade = new Atividade();
            atividade.setCodigo(10L);
            conhecimento.setAtividade(atividade);

            var conhecimentoSalvoDto = new ConhecimentoDto(1L, 10L, NOVO_CONHECIMENTO);

            when(conhecimentoMapper.toEntity(any(ConhecimentoDto.class))).thenReturn(conhecimento);
            when(conhecimentoRepo.save(any(Conhecimento.class))).thenReturn(conhecimento);
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoSalvoDto);

            mockMvc.perform(post(API_CONHECIMENTOS).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", API_CONHECIMENTOS_1))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value(NOVO_CONHECIMENTO))
                    .andExpect(jsonPath("$.atividadeCodigo").value(10L));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para DTO inválido")
        void deveRetornarBadRequestParaDtoInvalido() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(null, null, ""); // atividadeCodigo nulo e descrição vazia

            mockMvc.perform(post(API_CONHECIMENTOS).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testes para atualizar conhecimento")
    class AtualizarConhecimento {

        @Test
        @DisplayName("Deve atualizar um conhecimento e retornar 200 OK")
        void deveAtualizarConhecimento() throws Exception {
            var conhecimentoExistente = new Conhecimento();
            conhecimentoExistente.setCodigo(1L);
            conhecimentoExistente.setDescricao("Descrição Antiga");

            var conhecimentoDto = new ConhecimentoDto(1L, 10L, DESCRICAO_ATUALIZADA);
            var entidadeParaAtualizar = new Conhecimento();
            entidadeParaAtualizar.setDescricao(DESCRICAO_ATUALIZADA);
            var atividade = new Atividade();
            atividade.setCodigo(10L);
            entidadeParaAtualizar.setAtividade(atividade);

            var conhecimentoAtualizado = new Conhecimento();
            conhecimentoAtualizado.setCodigo(1L);
            conhecimentoAtualizado.setDescricao(DESCRICAO_ATUALIZADA);

            var conhecimentoAtualizadoDto = new ConhecimentoDto(1L, 10L, DESCRICAO_ATUALIZADA);

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimentoExistente));
            when(conhecimentoMapper.toEntity(any(ConhecimentoDto.class))).thenReturn(entidadeParaAtualizar);
            when(conhecimentoRepo.save(any(Conhecimento.class))).thenReturn(conhecimentoAtualizado);
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoAtualizadoDto);

            mockMvc.perform(put(API_CONHECIMENTOS_1).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descricao").value(DESCRICAO_ATUALIZADA));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(99L, 10L, "Tanto faz");

            when(conhecimentoRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(put(API_CONHECIMENTOS_99).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para excluir conhecimento")
    class ExcluirConhecimento {

        @Test
        @DisplayName("Deve excluir um conhecimento e retornar 204 No Content")
        void deveExcluirConhecimento() throws Exception {
            var conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));
            doNothing().when(conhecimentoRepo).deleteById(1L);

            mockMvc.perform(delete(API_CONHECIMENTOS_1).with(csrf()))
                    .andExpect(status().isNoContent());

            verify(conhecimentoRepo, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(conhecimentoRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(delete(API_CONHECIMENTOS_99).with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}

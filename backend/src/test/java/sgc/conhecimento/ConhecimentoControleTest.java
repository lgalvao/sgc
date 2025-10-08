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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConhecimentoControle.class)
@DisplayName("Testes do ConhecimentoControle")
@AutoConfigureMockMvc
@WithMockUser
class ConhecimentoControleTest {
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
            conhecimento.setDescricao("Conhecimento Teste");

            var conhecimentoDto = new ConhecimentoDto(1L, 10L, "Conhecimento Teste");

            when(conhecimentoRepo.findAll()).thenReturn(List.of(conhecimento));
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoDto);

            mockMvc.perform(get("/api/conhecimentos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].descricao").value("Conhecimento Teste"));
        }

        @Test
        @DisplayName("Deve retornar lista vazia com status 200 OK")
        void deveRetornarListaVazia() throws Exception {
            when(conhecimentoRepo.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/conhecimentos"))
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
            var conhecimentoDto = new ConhecimentoDto(1L, 10L, "Conhecimento Teste");

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoDto);

            mockMvc.perform(get("/api/conhecimentos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(conhecimentoRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/conhecimentos/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para criar conhecimento")
    class CriarConhecimento {

        @Test
        @DisplayName("Deve criar um conhecimento e retornar 201 Created")
        void deveCriarConhecimento() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(null, 10L, "Novo Conhecimento");
            var conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setDescricao("Novo Conhecimento");
            var atividade = new Atividade();
            atividade.setCodigo(10L);
            conhecimento.setAtividade(atividade);

            var conhecimentoSalvoDto = new ConhecimentoDto(1L, 10L, "Novo Conhecimento");

            when(conhecimentoMapper.toEntity(any(ConhecimentoDto.class))).thenReturn(conhecimento);
            when(conhecimentoRepo.save(any(Conhecimento.class))).thenReturn(conhecimento);
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoSalvoDto);

            mockMvc.perform(post("/api/conhecimentos").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/conhecimentos/1"))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value("Novo Conhecimento"))
                    .andExpect(jsonPath("$.atividadeCodigo").value(10L));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para DTO inválido")
        void deveRetornarBadRequestParaDtoInvalido() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(null, null, ""); // atividadeCodigo nulo e descrição vazia

            mockMvc.perform(post("/api/conhecimentos").with(csrf())
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

            var conhecimentoDto = new ConhecimentoDto(1L, 10L, "Descrição Atualizada");
            var entidadeParaAtualizar = new Conhecimento();
            entidadeParaAtualizar.setDescricao("Descrição Atualizada");
            var atividade = new Atividade();
            atividade.setCodigo(10L);
            entidadeParaAtualizar.setAtividade(atividade);

            var conhecimentoAtualizado = new Conhecimento();
            conhecimentoAtualizado.setCodigo(1L);
            conhecimentoAtualizado.setDescricao("Descrição Atualizada");

            var conhecimentoAtualizadoDto = new ConhecimentoDto(1L, 10L, "Descrição Atualizada");

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimentoExistente));
            when(conhecimentoMapper.toEntity(any(ConhecimentoDto.class))).thenReturn(entidadeParaAtualizar);
            when(conhecimentoRepo.save(any(Conhecimento.class))).thenReturn(conhecimentoAtualizado);
            when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(conhecimentoAtualizadoDto);

            mockMvc.perform(put("/api/conhecimentos/1").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descricao").value("Descrição Atualizada"));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(99L, 10L, "Tanto faz");

            when(conhecimentoRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/conhecimentos/99").with(csrf())
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

            mockMvc.perform(delete("/api/conhecimentos/1").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(conhecimentoRepo, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(conhecimentoRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/conhecimentos/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}

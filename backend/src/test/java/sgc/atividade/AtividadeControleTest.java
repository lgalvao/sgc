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
import sgc.mapa.modelo.Mapa;

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
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AtividadeRepo atividadeRepo;

    @MockitoBean
    private AtividadeMapper atividadeMapper;

    @MockitoBean
    private sgc.conhecimento.modelo.ConhecimentoRepo conhecimentoRepo;

    @MockitoBean
    private sgc.subprocesso.modelo.SubprocessoRepo subprocessoRepo;

    @Nested
    @DisplayName("Testes para listar atividades")
    class ListarAtividades {
        @Test
        @DisplayName("Deve retornar lista de atividades com status 200 OK")
        void deveRetornarListaDeAtividades() throws Exception {
            var atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setDescricao("Atividade Teste");

            var atividadeDto = new AtividadeDto(1L, null, "Atividade Teste");

            when(atividadeRepo.findAll()).thenReturn(List.of(atividade));
            when(atividadeMapper.toDTO(any(Atividade.class))).thenReturn(atividadeDto);

            mockMvc.perform(get("/api/atividades"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].codigo").value(1L))
                    .andExpect(jsonPath("$[0].descricao").value("Atividade Teste"));
        }

        @Test
        @DisplayName("Deve retornar lista vazia com status 200 OK")
        void deveRetornarListaVazia() throws Exception {
            when(atividadeRepo.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/atividades"))
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
            var atividade = new Atividade();
            atividade.setCodigo(1L);
            var atividadeDto = new AtividadeDto(1L, null, "Atividade Teste");

            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));
            when(atividadeMapper.toDTO(any(Atividade.class))).thenReturn(atividadeDto);

            mockMvc.perform(get("/api/atividades/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(atividadeRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/atividades/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para criar atividade")
    class CriarAtividade {

        @Test
        @DisplayName("Deve criar uma atividade e retornar 201 Created")
        void deveCriarAtividade() throws Exception {
            var atividadeDto = new AtividadeDto(null, 10L, "Nova Atividade");
            var atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setDescricao("Nova Atividade");
            var mapa = new Mapa();
            mapa.setCodigo(10L);
            atividade.setMapa(mapa);

            var chefe = new sgc.comum.modelo.Usuario();
            chefe.setTitulo("chefe");
            var unidade = new sgc.unidade.modelo.Unidade();
            unidade.setTitular(chefe);
            var subprocesso = new sgc.subprocesso.modelo.Subprocesso();
            subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            subprocesso.setUnidade(unidade);

            var atividadeSalvaDto = new AtividadeDto(1L, 10L, "Nova Atividade");

            when(subprocessoRepo.findByMapaCodigo(10L)).thenReturn(Optional.of(subprocesso));
            when(atividadeMapper.toEntity(any(AtividadeDto.class))).thenReturn(atividade);
            when(atividadeRepo.save(any(Atividade.class))).thenReturn(atividade);
            when(atividadeMapper.toDTO(any(Atividade.class))).thenReturn(atividadeSalvaDto);

            mockMvc.perform(post("/api/atividades").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atividadeDto))
                            .with(user("chefe")))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/atividades/1"))
                    .andExpect(jsonPath("$.codigo").value(1L))
                    .andExpect(jsonPath("$.descricao").value("Nova Atividade"))
                    .andExpect(jsonPath("$.mapaCodigo").value(10L));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para DTO inválido")
        void deveRetornarBadRequestParaDtoInvalido() throws Exception {
            var atividadeDto = new AtividadeDto(null, null, ""); // Descrição vazia

            mockMvc.perform(post("/api/atividades").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atividadeDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testes para atualizar atividade")
    class AtualizarAtividade {
        @Test
        @DisplayName("Deve atualizar uma atividade e retornar 200 OK")
        void deveAtualizarAtividade() throws Exception {
            var atividadeExistente = new Atividade();
            atividadeExistente.setCodigo(1L);
            atividadeExistente.setDescricao("Descrição Antiga");

            var atividadeDto = new AtividadeDto(1L, null, "Descrição Atualizada");
            var entidadeParaAtualizar = new Atividade();
            entidadeParaAtualizar.setDescricao("Descrição Atualizada");

            var atividadeAtualizada = new Atividade();
            atividadeAtualizada.setCodigo(1L);
            atividadeAtualizada.setDescricao("Descrição Atualizada");

            var atividadeAtualizadaDto = new AtividadeDto(1L, null, "Descrição Atualizada");

            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividadeExistente));
            when(atividadeMapper.toEntity(any(AtividadeDto.class))).thenReturn(entidadeParaAtualizar);
            when(atividadeRepo.save(any(Atividade.class))).thenReturn(atividadeAtualizada);
            when(atividadeMapper.toDTO(any(Atividade.class))).thenReturn(atividadeAtualizadaDto);

            mockMvc.perform(put("/api/atividades/1").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atividadeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descricao").value("Descrição Atualizada"));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            var atividadeDto = new AtividadeDto(99L, null, "Tanto faz");

            when(atividadeRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/atividades/99").with(csrf())
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
            var atividade = new Atividade();
            atividade.setCodigo(1L);

            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));
            when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(Collections.emptyList());
            doNothing().when(atividadeRepo).delete(any(Atividade.class));

            mockMvc.perform(delete("/api/atividades/1").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(atividadeRepo, times(1)).delete(any(Atividade.class));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(atividadeRepo.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/atividades/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}
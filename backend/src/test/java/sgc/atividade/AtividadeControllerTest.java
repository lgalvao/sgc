package sgc.atividade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import sgc.atividade.internal.AtividadeController;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.atividade.api.AtividadeDto;
import sgc.atividade.internal.AtividadeMapper;
import sgc.atividade.api.ConhecimentoDto;
import sgc.atividade.internal.ConhecimentoMapper;
import sgc.atividade.api.model.Atividade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.api.model.Mapa;
import sgc.subprocesso.api.AtividadeVisualizacaoDto;
import sgc.subprocesso.api.ConhecimentoVisualizacaoDto;
import sgc.subprocesso.api.SubprocessoSituacaoDto;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.service.SubprocessoService;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtividadeController.class)
@Import(RestExceptionHandler.class)
class AtividadeControllerTest {
    private static final String ATIVIDADE_TESTE = "Atividade Teste";
    private static final String API_ATIVIDADES = "/api/atividades";
    private static final String API_ATIVIDADES_1 = "/api/atividades/1";
    private static final String API_ATIVIDADES_99 = "/api/atividades/99";
    private static final String NOVA_ATIVIDADE = "Nova Atividade";
    private static final String DESCRICAO_ATUALIZADA = "Descrição Atualizada";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private AtividadeService atividadeService;
    @MockitoBean
    private SubprocessoService subprocessoService;
    @MockitoBean
    private AtividadeMapper atividadeMapper;
    @MockitoBean
    private ConhecimentoMapper conhecimentoMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        // Mock para SubprocessoService.obterEntidadePorCodigoMapa
        Subprocesso subprocessoMock = new Subprocesso();
        subprocessoMock.setCodigo(100L); // Valor arbitrário para o código do subprocesso
        when(subprocessoService.obterEntidadePorCodigoMapa(anyLong())).thenReturn(subprocessoMock);

        // Mock para SubprocessoService.obterStatus
        SubprocessoSituacaoDto statusDtoMock = SubprocessoSituacaoDto.builder()
                .codigo(100L)
                .situacao(SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO) // valor de exemplo
                .situacaoLabel("SITUACAO_TESTE") // valor de exemplo para assert
                .build();
        when(subprocessoService.obterSituacao(anyLong())).thenReturn(statusDtoMock);

        // Mock para SubprocessoService.listarAtividadesPorSubprocesso
        AtividadeVisualizacaoDto atividadeVisualizacaoDtoMock = AtividadeVisualizacaoDto.builder()
                .codigo(1L)
                .descricao(ATIVIDADE_TESTE)
                .conhecimentos(Collections.emptyList())
                .build();
        when(subprocessoService.listarAtividadesSubprocesso(anyLong())).thenReturn(List.of(atividadeVisualizacaoDtoMock));
    }

    @Nested
    @DisplayName("Testes para CRUD de Atividades")
    class ListarAtividades {
        @Test
        @DisplayName("Deve retornar lista de atividades com status 200 OK")
        @WithMockUser
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
        @WithMockUser
        void deveRetornarListaVazia() throws Exception {
            when(atividadeService.listar()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_ATIVIDADES))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes para obter atividade por código")
    class ObterAtividadePorId {
        @Test
        @DisplayName("Deve retornar uma atividade com status 200 OK")
        @WithMockUser
        void deveRetornarAtividadePorId() throws Exception {
            var atividadeDto = new AtividadeDto(1L, null, ATIVIDADE_TESTE);

            when(atividadeService.obterPorCodigo(1L)).thenReturn(atividadeDto);

            mockMvc.perform(get(API_ATIVIDADES_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(1L));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para código inexistente")
        @WithMockUser
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(atividadeService.obterPorCodigo(99L)).thenThrow(new ErroEntidadeNaoEncontrada(""));

            mockMvc.perform(get(API_ATIVIDADES_99)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para criar atividade")
    class CriarAtividade {
        @Test
        @DisplayName("Deve criar uma atividade e retornar 201 Created")
        @WithMockUser
        void deveCriarAtividade() throws Exception {
            var atividadeDto = new AtividadeDto(null, 10L, NOVA_ATIVIDADE);
            var atividadeSalvaDto = new AtividadeDto(1L, 10L, NOVA_ATIVIDADE);

            when(atividadeService.criar(any(AtividadeDto.class), any()))
                    .thenReturn(atividadeSalvaDto);

            // Mock específico para este teste, para que a atividade retorne a descrição correta.
            AtividadeVisualizacaoDto atividadeCriadaVis = AtividadeVisualizacaoDto.builder()
                    .codigo(1L)
                    .descricao(NOVA_ATIVIDADE)
                    .conhecimentos(Collections.emptyList())
                    .build();
            // O código do subprocesso mockado no setup é 100L
            when(subprocessoService.listarAtividadesSubprocesso(100L)).thenReturn(List.of(atividadeCriadaVis));

            mockMvc.perform(
                            post(API_ATIVIDADES)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atividadeDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", API_ATIVIDADES_1))
                    .andExpect(jsonPath("$.atividade.codigo").value(1L))
                    .andExpect(jsonPath("$.atividade.descricao").value(NOVA_ATIVIDADE))
                    .andExpect(jsonPath("$.subprocesso.codigo").value(100))
                    .andExpect(jsonPath("$.subprocesso.situacaoLabel").value("SITUACAO_TESTE"));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para DTO inválido")
        @WithMockUser
        void deveRetornarBadRequestParaDtoInvalido() throws Exception {
            var atividadeDto = new AtividadeDto(null, null, ""); // Descrição vazia

            mockMvc.perform(post(API_ATIVIDADES).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(atividadeDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testes para atualizar atividade")
    class AtualizarAtividade {
        @Test
        @DisplayName("Deve atualizar uma atividade e retornar 200 OK")
        @WithMockUser
        void deveAtualizarAtividade() throws Exception {
            var atividadeDto = new AtividadeDto(1L, null, DESCRICAO_ATUALIZADA);
            var atividadeAtualizadaDto = new AtividadeDto(1L, null, DESCRICAO_ATUALIZADA);

            when(atividadeService.atualizar(eq(1L), any(AtividadeDto.class)))
                    .thenReturn(atividadeAtualizadaDto);

            // Mock da entidade Atividade para recuperar o Mapa e o Subprocesso
            Atividade atividadeMock = new Atividade();
            atividadeMock.setCodigo(1L);

            Mapa mapaMock = new Mapa();
            mapaMock.setCodigo(10L);
            atividadeMock.setMapa(mapaMock);

            when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividadeMock);

            // Mock específico para este teste, para que a atividade retorne a descrição correta.
            AtividadeVisualizacaoDto atividadeAtualizadaVis = AtividadeVisualizacaoDto.builder()
                    .codigo(1L)
                    .descricao(DESCRICAO_ATUALIZADA)
                    .conhecimentos(Collections.emptyList())
                    .build();
            when(subprocessoService.listarAtividadesSubprocesso(100L)).thenReturn(List.of(atividadeAtualizadaVis));

            mockMvc.perform(post("/api/atividades/1/atualizar")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atividadeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atividade.descricao").value(DESCRICAO_ATUALIZADA))
                    .andExpect(jsonPath("$.subprocesso.codigo").value(100))
                    .andExpect(jsonPath("$.subprocesso.situacaoLabel").value("SITUACAO_TESTE"));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para código inexistente")
        @WithMockUser
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            var atividadeDto = new AtividadeDto(99L, null, "Tanto faz");

            when(atividadeService.atualizar(eq(99L), any(AtividadeDto.class)))
                    .thenThrow(new ErroEntidadeNaoEncontrada(""));

            mockMvc.perform(post("/api/atividades/99/atualizar")
                                    .with(csrf())
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
        @WithMockUser
        void deveExcluirAtividade() throws Exception {
            // Mock da entidade Atividade para recuperar o Mapa e o Subprocesso antes da exclusão
            Atividade atividadeMock = new Atividade();
            atividadeMock.setCodigo(1L);

            Mapa mapaMock = new Mapa();
            mapaMock.setCodigo(10L);
            atividadeMock.setMapa(mapaMock);

            when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividadeMock);

            doNothing().when(atividadeService).excluir(1L);

            // Mock para listar atividades retornando vazio (atividade excluída)
            when(subprocessoService.listarAtividadesSubprocesso(100L)).thenReturn(Collections.emptyList());

            mockMvc.perform(post("/api/atividades/1/excluir").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atividade").doesNotExist())
                    .andExpect(jsonPath("$.subprocesso.codigo").value(100))
                    .andExpect(jsonPath("$.subprocesso.situacaoLabel").value("SITUACAO_TESTE"));

            verify(atividadeService, times(1)).excluir(1L);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para código inexistente")
        @WithMockUser
        void deveRetornarNotFoundParaIdInexistente() throws Exception {
            when(atividadeService.obterEntidadePorCodigo(99L))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 99L));

            mockMvc.perform(post("/api/atividades/99/excluir").with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para CRUD de Conhecimentos aninhados")
    class ConhecimentoEndpoints {
        private static final String API_CONHECIMENTOS = "/api/atividades/1/conhecimentos";
        private static final String API_CONHECIMENTOS_1 = "/api/atividades/1/conhecimentos/1";
        private static final String API_CONHECIMENTOS_1_ATUALIZAR = "/api/atividades/1/conhecimentos/1/atualizar";
        private static final String API_CONHECIMENTOS_1_EXCLUIR = "/api/atividades/1/conhecimentos/1/excluir";
        private static final String NOVO_CONHECIMENTO = "Novo Conhecimento";

        @Test
        @DisplayName("Deve listar conhecimentos de uma atividade")
        @WithMockUser
        void deveListarConhecimentos() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(1L, 1L, "Conhecimento Teste");

            when(atividadeService.listarConhecimentos(1L)).thenReturn(List.of(conhecimentoDto));

            mockMvc.perform(get(API_CONHECIMENTOS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].codigo").value(1L));
        }

        @Test
        @DisplayName("Deve criar um conhecimento para uma atividade")
        @WithMockUser
        void deveCriarConhecimento() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(null, 1L, NOVO_CONHECIMENTO);
            var conhecimentoSalvoDto = new ConhecimentoDto(1L, 1L, NOVO_CONHECIMENTO);

            when(atividadeService.criarConhecimento(eq(1L), any(ConhecimentoDto.class)))
                    .thenReturn(conhecimentoSalvoDto);

            // Mock da entidade Atividade para recuperar o Mapa e o Subprocesso
            Atividade atividadeMock = new Atividade();
            atividadeMock.setCodigo(1L);
            Mapa mapaMock = new Mapa();
            mapaMock.setCodigo(10L);
            atividadeMock.setMapa(mapaMock);
            when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividadeMock);

            // Mock específico para este teste, para que a atividade retorne o conhecimento criado.
            AtividadeVisualizacaoDto atividadeComConhecimento = AtividadeVisualizacaoDto.builder()
                    .codigo(1L)
                    .descricao(ATIVIDADE_TESTE)
                    .conhecimentos(List.of(ConhecimentoVisualizacaoDto.builder()
                            .codigo(1L)
                            .descricao(NOVO_CONHECIMENTO)
                            .build()))
                    .build();
            when(subprocessoService.listarAtividadesSubprocesso(100L)).thenReturn(List.of(atividadeComConhecimento));

            mockMvc.perform(post(API_CONHECIMENTOS)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", API_CONHECIMENTOS_1))
                    .andExpect(jsonPath("$.atividade.conhecimentos[0].codigo").value(1L))
                    .andExpect(jsonPath("$.subprocesso.codigo").value(100))
                    .andExpect(jsonPath("$.subprocesso.situacaoLabel").value("SITUACAO_TESTE"));
        }

        @Test
        @DisplayName("Deve atualizar um conhecimento")
        @WithMockUser
        void deveAtualizarConhecimento() throws Exception {
            var conhecimentoDto = new ConhecimentoDto(1L, 1L, "Atualizado");

            when(atividadeService.atualizarConhecimento(eq(1L), eq(1L), any(ConhecimentoDto.class)))
                    .thenReturn(conhecimentoDto);

            // Mock da entidade Atividade para recuperar o Mapa e o Subprocesso
            Atividade atividadeMock = new Atividade();
            atividadeMock.setCodigo(1L);
            Mapa mapaMock = new Mapa();
            mapaMock.setCodigo(10L);
            atividadeMock.setMapa(mapaMock);
            when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividadeMock);

            // Mock específico para este teste
            AtividadeVisualizacaoDto atividadeComConhecimentoAtualizado = AtividadeVisualizacaoDto.builder()
                    .codigo(1L)
                    .descricao(ATIVIDADE_TESTE) 
                    .conhecimentos(List.of(ConhecimentoVisualizacaoDto.builder()
                                    .codigo(1L)
                                    .descricao("Atualizado")
                                    .build()
                    ))
                    .build();

            when(subprocessoService.listarAtividadesSubprocesso(100L))
                    .thenReturn(List.of(atividadeComConhecimentoAtualizado));

            mockMvc.perform(post(API_CONHECIMENTOS_1_ATUALIZAR)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(conhecimentoDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atividade.conhecimentos[0].descricao").value("Atualizado"))
                    .andExpect(jsonPath("$.subprocesso.codigo").value(100))
                    .andExpect(jsonPath("$.subprocesso.situacaoLabel").value("SITUACAO_TESTE"));
        }

        @Test
        @DisplayName("Deve excluir um conhecimento")
        @WithMockUser
        void deveExcluirConhecimento() throws Exception {
            doNothing().when(atividadeService).excluirConhecimento(1L, 1L);

            // Mock da entidade Atividade para recuperar o Mapa e o Subprocesso
            Atividade atividadeMock = new Atividade();
            atividadeMock.setCodigo(1L);

            Mapa mapaMock = new Mapa();
            mapaMock.setCodigo(10L);
            atividadeMock.setMapa(mapaMock);

            when(atividadeService.obterEntidadePorCodigo(1L)).thenReturn(atividadeMock);

            // Mock específico para este teste, para simular a remoção do conhecimento
            AtividadeVisualizacaoDto atividadeSemConhecimento = AtividadeVisualizacaoDto.builder()
                    .codigo(1L)
                    .descricao(ATIVIDADE_TESTE) // ou outra descrição relevante
                    .conhecimentos(Collections.emptyList()) // Lista vazia após exclusão
                    .build();
            when(subprocessoService.listarAtividadesSubprocesso(100L)).thenReturn(List.of(atividadeSemConhecimento));

            mockMvc.perform(post(API_CONHECIMENTOS_1_EXCLUIR).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atividade.conhecimentos").isEmpty())
                    .andExpect(jsonPath("$.subprocesso.codigo").value(100))
                    .andExpect(jsonPath("$.subprocesso.situacaoLabel").value("SITUACAO_TESTE"));

            verify(atividadeService, times(1)).excluirConhecimento(1L, 1L);
        }
    }
}

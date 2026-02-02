package sgc.subprocesso;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesRequest;
import sgc.subprocesso.model.Subprocesso;
import tools.jackson.databind.ObjectMapper;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.service.SubprocessoFacade;

@WebMvcTest(SubprocessoMapaController.class)
@Import(RestExceptionHandler.class)
class SubprocessoMapaControllerTest {
        @MockitoBean
        private SubprocessoFacade subprocessoFacade;

        @MockitoBean
        private MapaFacade mapaFacade;

        @MockitoBean
        private UsuarioFacade usuarioFacade;

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
                when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
                when(mapaFacade.verificarImpactos(any(Subprocesso.class), any()))
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

                when(subprocessoFacade.buscarSubprocessoComMapa(1L)).thenReturn(sp);
                when(mapaFacade.obterMapaCompleto(10L, 1L)).thenReturn(MapaCompletoDto.builder().build());

                mockMvc.perform(get("/api/subprocessos/1/mapa")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("obterMapaVisualizacao")
        @WithMockUser
        void obterMapaVisualizacao() throws Exception {
                when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
                when(mapaFacade.obterMapaParaVisualizacao(any(Subprocesso.class)))
                                .thenReturn(MapaVisualizacaoDto.builder().build());

                mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("salvarMapa")
        @WithMockUser
        void salvarMapa() throws Exception {
                SalvarMapaRequest req = SalvarMapaRequest.builder()
                                .observacoes("obs")
                                .competencias(List.of(CompetenciaMapaDto.builder().descricao("Comp 1")
                                                .atividadesCodigos(List.of(1L)).build()))
                                .build();

                when(subprocessoFacade.salvarMapaSubprocesso(eq(1L), any()))
                                .thenReturn(MapaCompletoDto.builder().build());

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
                when(subprocessoFacade.obterMapaParaAjuste(1L))
                                .thenReturn(MapaAjusteDto.builder().build());

                mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste")).andExpect(status().isOk());
        }

    @Test
    @DisplayName("salvarAjustesMapa")
    @WithMockUser
    void salvarAjustesMapa() throws Exception {
        CompetenciaAjusteDto comp = CompetenciaAjusteDto.builder()
                .codCompetencia(1L)
                .nome("Competencia Teste")
                .atividades(List.of())
                .build();

        SalvarAjustesRequest req = SalvarAjustesRequest.builder()
                .competencias(List.of(comp))
                .build();

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

                when(subprocessoFacade.buscarSubprocessoComMapa(1L)).thenReturn(sp);
                when(mapaFacade.obterMapaCompleto(10L, 1L)).thenReturn(MapaCompletoDto.builder().build());

                mockMvc.perform(get("/api/subprocessos/1/mapa-completo")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("salvarMapaCompleto")
        @WithMockUser
        void salvarMapaCompleto() throws Exception {
                SalvarMapaRequest req = SalvarMapaRequest.builder()
                                .observacoes("obs")
                                .competencias(List.of(CompetenciaMapaDto.builder().descricao("Comp 1")
                                                .atividadesCodigos(List.of(1L)).build()))
                                .build();

                when(subprocessoFacade.salvarMapaSubprocesso(eq(1L), any()))
                                .thenReturn(MapaCompletoDto.builder().build());

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
                CompetenciaRequest req = CompetenciaRequest.builder()
                                .descricao("Comp")
                                .atividadesIds(List.of(1L, 2L)) // Corrigido: lista não pode ser vazia
                                .build();

                when(subprocessoFacade.adicionarCompetencia(eq(1L), any()))
                                .thenReturn(MapaCompletoDto.builder().build());

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
                CompetenciaRequest req = CompetenciaRequest.builder()
                                .descricao("") // Descrição vazia - deve falhar
                                .atividadesIds(List.of(1L))
                                .build();

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
                CompetenciaRequest req = CompetenciaRequest.builder()
                                .descricao("Competência válida")
                                .atividadesIds(List.of()) // Lista vazia - deve falhar
                                .build();

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
                CompetenciaRequest req = CompetenciaRequest.builder()
                                .descricao("Competência válida")
                                .atividadesIds(null) // Null - deve falhar
                                .build();

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
                CompetenciaRequest req = CompetenciaRequest.builder()
                                .descricao("Comp")
                                .atividadesIds(List.of(1L)) // Corrigido: lista não pode ser vazia
                                .build();

                when(subprocessoFacade.atualizarCompetencia(eq(1L), eq(10L), any()))
                                .thenReturn(MapaCompletoDto.builder().build());

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
                when(subprocessoFacade.removerCompetencia(1L, 10L))
                                .thenReturn(MapaCompletoDto.builder().build());

                mockMvc.perform(post("/api/subprocessos/1/competencias/10/remover").with(csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("obterContextoEdicao - sem perfil")
        @WithMockUser
        void obterContextoEdicaoSemPerfil() throws Exception {
                ContextoEdicaoDto dto = ContextoEdicaoDto.builder().build();
                when(subprocessoFacade.obterContextoEdicao(1L)).thenReturn(dto);

                mockMvc.perform(get("/api/subprocessos/1/contexto-edicao"))
                                .andExpect(status().isOk());

                verify(subprocessoFacade).obterContextoEdicao(1L);
        }

        @Test
        @DisplayName("obterContextoEdicao - com perfil")
        @WithMockUser
        void obterContextoEdicaoComPerfil() throws Exception {
                ContextoEdicaoDto dto = ContextoEdicaoDto.builder().build();
                when(subprocessoFacade.obterContextoEdicao(1L)).thenReturn(dto);

                mockMvc.perform(get("/api/subprocessos/1/contexto-edicao")
                                .param("perfil", "ADMIN"))
                                .andExpect(status().isOk());

                verify(subprocessoFacade).obterContextoEdicao(1L);
        }

        @Test
        @DisplayName("listarAtividades")
        @WithMockUser
        void listarAtividades() throws Exception {
                List<AtividadeDto> atividades = List.of(
                                AtividadeDto.builder().codigo(1L).descricao("Atividade 1").build());
                when(subprocessoFacade.listarAtividadesSubprocesso(1L)).thenReturn(atividades);

                mockMvc.perform(get("/api/subprocessos/1/atividades"))
                                .andExpect(status().isOk());

                verify(subprocessoFacade).listarAtividadesSubprocesso(1L);
        }

        @Test
        @DisplayName("disponibilizarMapaEmBloco - com dataLimite")
        @WithMockUser(roles = "ADMIN")
        void disponibilizarMapaEmBlocoComDataLimite() throws Exception {
                ProcessarEmBlocoRequest req = ProcessarEmBlocoRequest.builder()
                                .acao("DISPONIBILIZAR")
                                .subprocessos(List.of(1L, 2L, 3L))
                                .dataLimite(LocalDate.now().plusDays(20))
                                .build();

                mockMvc.perform(
                                post("/api/subprocessos/100/disponibilizar-mapa-bloco")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk());

                verify(subprocessoFacade).disponibilizarMapaEmBloco(
                                eq(List.of(1L, 2L, 3L)),
                                eq(100L),
                                any(),
                                any(Usuario.class));
        }

        @Test
        @DisplayName("disponibilizarMapaEmBloco - sem dataLimite (usa padrão +15 dias)")
        @WithMockUser(roles = "ADMIN")
        void disponibilizarMapaEmBlocoSemDataLimite() throws Exception {
                ProcessarEmBlocoRequest req = ProcessarEmBlocoRequest.builder()
                                .acao("DISPONIBILIZAR")
                                .subprocessos(List.of(1L))
                                .dataLimite(null)
                                .build();

                mockMvc.perform(
                                post("/api/subprocessos/100/disponibilizar-mapa-bloco")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk());

                verify(subprocessoFacade).disponibilizarMapaEmBloco(
                                eq(List.of(1L)),
                                eq(100L),
                                any(),
                                any(Usuario.class));
        }

}

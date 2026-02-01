package sgc.subprocesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.mapper.AnaliseMapper;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.model.Atividade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoFacade;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoCadastroController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoCadastroController")
class SubprocessoCadastroControllerTest {

    @MockitoBean
    private SubprocessoFacade subprocessoFacade;

    @MockitoBean
    private AnaliseFacade analiseFacade;

    @MockitoBean
    private AnaliseMapper analiseMapper;

    @MockitoBean
    private UsuarioFacade usuarioService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // ObjectMapper is autowired, no manual setup needed
    }

    @Nested
    @DisplayName("obterHistoricoCadastro")
    class ObterHistoricoCadastroTests {
        @Test
        @DisplayName("deve obter histórico de análises de cadastro")
        @WithMockUser
        void deveObterHistoricoCadastro() throws Exception {
            // Arrange
            Analise analise = new Analise();
            AnaliseHistoricoDto dto = AnaliseHistoricoDto.builder()
                    .dataHora(null)
                    .observacoes("Observação")
                    .acao(null)
                    .unidadeSigla(null)
                    .analistaUsuarioTitulo(null)
                    .motivo(null)
                    .tipo(null)
                    .build();
            
            when(analiseFacade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO))
                    .thenReturn(List.of(analise));
            when(analiseMapper.toAnaliseHistoricoDto(any(Analise.class)))
                    .thenReturn(dto);

            // Act & Assert
            mockMvc.perform(get("/api/subprocessos/1/historico-cadastro"))
                    .andExpect(status().isOk());

            verify(analiseFacade).listarPorSubprocesso(1L, TipoAnalise.CADASTRO);
        }
    }

    @Nested
    @DisplayName("disponibilizarCadastro")
    class DisponibilizarCadastroTests {
        @Test
        @DisplayName("deve disponibilizar cadastro quando todas atividades têm conhecimentos")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarCadastro() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);
            when(subprocessoFacade.obterAtividadesSemConhecimento(1L))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Cadastro de atividades disponibilizado"));

            verify(subprocessoFacade).disponibilizarCadastro(1L, usuario);
        }

        @Test
        @DisplayName("deve lançar erro quando existem atividades sem conhecimentos")
        @WithMockUser(roles = "CHEFE")
        void deveLancarErroQuandoAtividadesSemConhecimentos() throws Exception {
            // Arrange
            Atividade atividade = new Atividade();
            atividade.setCodigo(10L);
            atividade.setDescricao("Atividade sem conhecimento");
            
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(new Usuario());
            when(subprocessoFacade.obterAtividadesSemConhecimento(1L))
                    .thenReturn(List.of(atividade));

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar")
                            .with(csrf()))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("disponibilizarRevisao")
    class DisponibilizarRevisaoTests {
        @Test
        @DisplayName("deve disponibilizar revisão quando todas atividades têm conhecimentos")
        @WithMockUser(roles = "CHEFE")
        void deveDisponibilizarRevisao() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);
            when(subprocessoFacade.obterAtividadesSemConhecimento(1L))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Revisão do cadastro de atividades disponibilizada"));

            verify(subprocessoFacade).disponibilizarRevisao(1L, usuario);
        }

        @Test
        @DisplayName("deve lançar erro quando existem atividades sem conhecimentos na revisão")
        @WithMockUser(roles = "CHEFE")
        void deveLancarErroQuandoAtividadesSemConhecimentosRevisao() throws Exception {
            // Arrange
            Atividade atividade = new Atividade();
            atividade.setCodigo(20L);
            atividade.setDescricao("Atividade sem conhecimento");
            
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(new Usuario());
            when(subprocessoFacade.obterAtividadesSemConhecimento(1L))
                    .thenReturn(List.of(atividade));

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao")
                            .with(csrf()))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("obterCadastro")
    class ObterCadastroTests {
        @Test
        @DisplayName("deve obter dados do cadastro")
        @WithMockUser
        void deveObterCadastro() throws Exception {
            // Arrange
            SubprocessoCadastroDto dto = SubprocessoCadastroDto.builder()
                    .subprocessoCodigo(1L)
                    .build();
            when(subprocessoFacade.obterCadastro(1L)).thenReturn(dto);

            // Act & Assert
            mockMvc.perform(get("/api/subprocessos/1/cadastro"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subprocessoCodigo").value(1));

            verify(subprocessoFacade).obterCadastro(1L);
        }
    }

    @Nested
    @DisplayName("devolverCadastro")
    class DevolverCadastroTests {
        @Test
        @DisplayName("deve devolver cadastro com observações sanitizadas")
        @WithMockUser(roles = "ADMIN")
        void deveDevolverCadastro() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            DevolverCadastroRequest request = new DevolverCadastroRequest(
                    "Precisa de ajustes");

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/devolver-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).devolverCadastro(eq(1L), anyString(), eq(usuario));
        }
    }

    @Nested
    @DisplayName("aceitarCadastro")
    class AceitarCadastroTests {
        @Test
        @DisplayName("deve aceitar cadastro com observações")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarCadastro() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            AceitarCadastroRequest request = new AceitarCadastroRequest("Aprovado");

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/aceitar-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).aceitarCadastro(eq(1L), anyString(), eq(usuario));
        }
    }

    @Nested
    @DisplayName("homologarCadastro")
    class HomologarCadastroTests {
        @Test
        @DisplayName("deve homologar cadastro")
        @WithMockUser(roles = "ADMIN")
        void deveHomologarCadastro() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            HomologarCadastroRequest request = new HomologarCadastroRequest("Homologado");

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/homologar-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).homologarCadastro(eq(1L), anyString(), eq(usuario));
        }
    }

    @Nested
    @DisplayName("devolverRevisaoCadastro")
    class DevolverRevisaoCadastroTests {
        @Test
        @DisplayName("deve devolver revisão do cadastro")
        @WithMockUser(roles = "GESTOR")
        void deveDevolverRevisaoCadastro() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            DevolverCadastroRequest request = new DevolverCadastroRequest("Revisar");

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/devolver-revisao-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).devolverRevisaoCadastro(eq(1L), anyString(), eq(usuario));
        }
    }

    @Nested
    @DisplayName("aceitarRevisaoCadastro")
    class AceitarRevisaoCadastroTests {
        @Test
        @DisplayName("deve aceitar revisão do cadastro")
        @WithMockUser(roles = "ADMIN")
        void deveAceitarRevisaoCadastro() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            AceitarCadastroRequest request = new AceitarCadastroRequest("Revisão aceita");

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/aceitar-revisao-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).aceitarRevisaoCadastro(eq(1L), anyString(), eq(usuario));
        }
    }

    @Nested
    @DisplayName("homologarRevisaoCadastro")
    class HomologarRevisaoCadastroTests {
        @Test
        @DisplayName("deve homologar revisão do cadastro")
        @WithMockUser(roles = "ADMIN")
        void deveHomologarRevisaoCadastro() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            HomologarCadastroRequest request = new HomologarCadastroRequest("Homologado");

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/homologar-revisao-cadastro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).homologarRevisaoCadastro(eq(1L), anyString(), eq(usuario));
        }
    }

    @Nested
    @DisplayName("importarAtividades")
    class ImportarAtividadesTests {
        @Test
        @DisplayName("deve importar atividades de outro subprocesso")
        @WithMockUser(roles = "CHEFE")
        void deveImportarAtividades() throws Exception {
            // Arrange
            ImportarAtividadesRequest request = new ImportarAtividadesRequest(2L);

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/1/importar-atividades")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Atividades importadas."));

            verify(subprocessoFacade).importarAtividades(1L, 2L);
        }
    }

    @Nested
    @DisplayName("aceitarCadastroEmBloco")
    class AceitarCadastroEmBlocoTests {
        @Test
        @DisplayName("deve aceitar cadastros em bloco")
        @WithMockUser(roles = "GESTOR")
        void deveAceitarCadastroEmBloco() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                    .acao("ACEITAR")
                    .subprocessos(List.of(1L, 2L, 3L))
                    .dataLimite(null)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/100/aceitar-cadastro-bloco")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).aceitarCadastroEmBloco(
                    List.of(1L, 2L, 3L), 100L, usuario);
        }
    }

    @Nested
    @DisplayName("homologarCadastroEmBloco")
    class HomologarCadastroEmBlocoTests {
        @Test
        @DisplayName("deve homologar cadastros em bloco")
        @WithMockUser(roles = "ADMIN")
        void deveHomologarCadastroEmBloco() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.extrairTituloUsuario(any())).thenReturn("123");
            when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

            ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                    .acao("HOMOLOGAR")
                    .subprocessos(List.of(1L, 2L))
                    .dataLimite(null)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/100/homologar-cadastro-bloco")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(subprocessoFacade).homologarCadastroEmBloco(
                    List.of(1L, 2L), 100L, usuario);
        }
    }
}

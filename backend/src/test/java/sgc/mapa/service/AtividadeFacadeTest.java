package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.dto.*;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.Acao;
import sgc.seguranca.AccessControlService;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtividadeFacade")
class AtividadeFacadeTest {

    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private SubprocessoFacade subprocessoFacade;
    @Mock private AccessControlService accessControlService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private MapaFacade mapaFacade;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AtividadeFacade atividadeFacade;

    @Nested
    @DisplayName("obterAtividadePorId")
    class ObterAtividadePorIdTests {
        
        @Test
        @DisplayName("deve obter atividade por ID com sucesso")
        void deveObterAtividadePorId() {
            Long atividadeCodigo = 300L;
            Atividade expected = Atividade.builder()
                    .codigo(atividadeCodigo)
                    .descricao("Atividade")
                    .build();
            
            when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(expected);
            
            Atividade result = atividadeFacade.obterAtividadePorId(atividadeCodigo);
            
            assertNotNull(result);
            assertEquals(expected, result);
            verify(mapaManutencaoService).obterAtividadePorCodigo(atividadeCodigo);
        }
    }

    @Nested
    @DisplayName("listarConhecimentosPorAtividade")
    class ListarConhecimentosPorAtividadeTests {
        
        @Test
        @DisplayName("deve listar conhecimentos por atividade")
        void deveListarConhecimentosPorAtividade() {
            Long atividadeCodigo = 300L;
            List<Conhecimento> expected = List.of(
                    Conhecimento.builder().codigo(1L).descricao("Conhecimento 1").build(),
                    Conhecimento.builder().codigo(2L).descricao("Conhecimento 2").build()
            );
            
            when(mapaManutencaoService.listarConhecimentosPorAtividade(atividadeCodigo)).thenReturn(expected);
            
            List<Conhecimento> result = atividadeFacade.listarConhecimentosPorAtividade(atividadeCodigo);
            
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(mapaManutencaoService).listarConhecimentosPorAtividade(atividadeCodigo);
        }
    }

    @Nested
    @DisplayName("criarAtividade")
    class CriarAtividadeTests {
        
        @Test
        @DisplayName("deve criar atividade com sucesso")
        void deveCriarAtividade() {
            Long mapaCodigo = 100L;
            Long subCodigo = 200L;
            Long atividadeCodigo = 300L;
            CriarAtividadeRequest request = new CriarAtividadeRequest(mapaCodigo, "Desc");
            
            Usuario usuario = new Usuario();
            Mapa mapa = new Mapa();
            mapa.setCodigo(mapaCodigo);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(subCodigo);
            mapa.setSubprocesso(subprocesso);
            
            Atividade salvo = Atividade.builder()
                    .codigo(atividadeCodigo)
                    .mapa(mapa)
                    .descricao("Desc")
                    .build();
            
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(mapaFacade.obterPorCodigo(mapaCodigo)).thenReturn(mapa);
            when(mapaManutencaoService.criarAtividade(request)).thenReturn(salvo);
            when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
            when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.singletonList(
                    new AtividadeDto(atividadeCodigo, "Desc", null)
            ));

            AtividadeOperacaoResponse result = atividadeFacade.criarAtividade(request);

            assertNotNull(result);
            verify(accessControlService).verificarPermissao(eq(usuario), eq(Acao.CRIAR_ATIVIDADE), any(Atividade.class));
        }
    }

    @Nested
    @DisplayName("atualizarAtividade")
    class AtualizarAtividadeTests {
        
        @Test
        @DisplayName("deve atualizar atividade com sucesso")
        void deveAtualizarAtividade() {
            Long atividadeCodigo = 300L;
            Long mapaCodigo = 100L;
            Long subCodigo = 200L;
            
            AtualizarAtividadeRequest request = new AtualizarAtividadeRequest("Nova Desc");
            
            Usuario usuario = new Usuario();
            Mapa mapa = new Mapa();
            mapa.setCodigo(mapaCodigo);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(subCodigo);
            mapa.setSubprocesso(subprocesso);
            
            Atividade atividade = new Atividade();
            atividade.setCodigo(atividadeCodigo);
            atividade.setMapa(mapa);
            atividade.setDescricao("Antiga Desc");

            when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
            when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.emptyList());

            AtividadeOperacaoResponse result = atividadeFacade.atualizarAtividade(atividadeCodigo, request);

            assertNotNull(result);
            verify(accessControlService).verificarPermissao(usuario, Acao.EDITAR_ATIVIDADE, atividade);
            verify(mapaManutencaoService).atualizarAtividade(atividadeCodigo, request);
        }
    }

    @Nested
    @DisplayName("excluirAtividade")
    class ExcluirAtividadeTests {
        
        @Test
        @DisplayName("deve excluir atividade com sucesso")
        void deveExcluirAtividade() {
            Long atividadeCodigo = 300L;
            Long mapaCodigo = 100L;
            Long subCodigo = 200L;
            
            Usuario usuario = new Usuario();
            Mapa mapa = new Mapa();
            mapa.setCodigo(mapaCodigo);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(subCodigo);
            mapa.setSubprocesso(subprocesso);
            
            Atividade atividade = new Atividade();
            atividade.setCodigo(atividadeCodigo);
            atividade.setMapa(mapa);
            
            when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
            when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.emptyList());

            AtividadeOperacaoResponse result = atividadeFacade.excluirAtividade(atividadeCodigo);

            assertNotNull(result);
            verify(accessControlService).verificarPermissao(usuario, Acao.EXCLUIR_ATIVIDADE, atividade);
            verify(mapaManutencaoService).excluirAtividade(atividadeCodigo);
        }
    }

    @Nested
    @DisplayName("criarConhecimento")
    class CriarConhecimentoTests {
        
        @Test
        @DisplayName("deve criar conhecimento com sucesso")
        void deveCriarConhecimento() {
            Long atividadeCodigo = 300L;
            Long mapaCodigo = 100L;
            Long subCodigo = 200L;
            CriarConhecimentoRequest request = new CriarConhecimentoRequest(atividadeCodigo, "Conhecimento");
            
            Usuario usuario = new Usuario();
            Mapa mapa = new Mapa();
            mapa.setCodigo(mapaCodigo);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(subCodigo);
            mapa.setSubprocesso(subprocesso);
            
            Atividade atividade = new Atividade();
            atividade.setCodigo(atividadeCodigo);
            atividade.setMapa(mapa);
            
            Conhecimento conhecimentoSalvo = Conhecimento.builder()
                    .codigo(500L)
                    .atividade(atividade)
                    .descricao("Conhecimento")
                    .build();
            
            when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(mapaManutencaoService.criarConhecimento(atividadeCodigo, request)).thenReturn(conhecimentoSalvo);
            
            // Mocking for response creation
            when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
            when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.singletonList(
                    new AtividadeDto(atividadeCodigo, "Desc", null)
            ));

            ResultadoOperacaoConhecimento result = atividadeFacade.criarConhecimento(atividadeCodigo, request);

            assertNotNull(result);
            assertEquals(500L, result.novoConhecimentoId());
            verify(accessControlService).verificarPermissao(usuario, Acao.ASSOCIAR_CONHECIMENTOS, atividade);
        }
    }

    @Nested
    @DisplayName("atualizarConhecimento")
    class AtualizarConhecimentoTests {
        
        @Test
        @DisplayName("deve atualizar conhecimento com sucesso")
        void deveAtualizarConhecimento() {
            Long atividadeCodigo = 300L;
            Long conhecimentoCodigo = 500L;
            Long mapaCodigo = 100L;
            Long subCodigo = 200L;
            AtualizarConhecimentoRequest request = new AtualizarConhecimentoRequest("Conhecimento Atualizado");
            
            Usuario usuario = new Usuario();
            Mapa mapa = new Mapa();
            mapa.setCodigo(mapaCodigo);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(subCodigo);
            mapa.setSubprocesso(subprocesso);
            
            Atividade atividade = new Atividade();
            atividade.setCodigo(atividadeCodigo);
            atividade.setMapa(mapa);
            
            when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
            when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.emptyList());

            AtividadeOperacaoResponse result = atividadeFacade.atualizarConhecimento(atividadeCodigo, conhecimentoCodigo, request);

            assertNotNull(result);
            verify(accessControlService).verificarPermissao(usuario, Acao.ASSOCIAR_CONHECIMENTOS, atividade);
            verify(mapaManutencaoService).atualizarConhecimento(atividadeCodigo, conhecimentoCodigo, request);
        }
    }

    @Nested
    @DisplayName("excluirConhecimento")
    class ExcluirConhecimentoTests {
        
        @Test
        @DisplayName("deve excluir conhecimento com sucesso")
        void deveExcluirConhecimento() {
            Long atividadeCodigo = 300L;
            Long conhecimentoCodigo = 500L;
            Long mapaCodigo = 100L;
            Long subCodigo = 200L;
            
            Usuario usuario = new Usuario();
            Mapa mapa = new Mapa();
            mapa.setCodigo(mapaCodigo);
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(subCodigo);
            mapa.setSubprocesso(subprocesso);
            
            Atividade atividade = new Atividade();
            atividade.setCodigo(atividadeCodigo);
            atividade.setMapa(mapa);
            
            when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
            when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.emptyList());

            AtividadeOperacaoResponse result = atividadeFacade.excluirConhecimento(atividadeCodigo, conhecimentoCodigo);

            assertNotNull(result);
            verify(accessControlService).verificarPermissao(usuario, Acao.ASSOCIAR_CONHECIMENTOS, atividade);
            verify(mapaManutencaoService).excluirConhecimento(atividadeCodigo, conhecimentoCodigo);
        }
    }
}

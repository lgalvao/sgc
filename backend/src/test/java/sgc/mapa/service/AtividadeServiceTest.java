package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do Serviço de Atividade")
class AtividadeServiceTest {

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private RepositorioComum repo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoService conhecimentoService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AtividadeService service;

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {
        @Test
        @DisplayName("Deve listar todas as atividades")
        void deveListarTodas() {
             when(atividadeRepo.findAll()).thenReturn(List.of(new Atividade()));
             when(atividadeMapper.toResponse(any())).thenReturn(AtividadeResponse.builder().build());
             assertThat(service.listar())
                 .isNotNull()
                 .hasSize(1);
        }

        @Test
        @DisplayName("Deve obter por código Response")
        void deveObterPorCodigoDto() {
             when(repo.buscar(Atividade.class, 1L)).thenReturn(new Atividade());
             when(atividadeMapper.toResponse(any())).thenReturn(AtividadeResponse.builder().build());
             assertThat(service.obterResponse(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro se obter por código não encontrar")
        void deveLancarErroObterPorCodigo() {
             when(repo.buscar(Atividade.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 1L));
             assertThatThrownBy(() -> service.obterResponse(1L))
                 .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve listar entidades por código")
        void deveListarEntidades() {
             Atividade ativ = new Atividade();
             when(repo.buscar(Atividade.class, 1L)).thenReturn(ativ);
             assertThat(service.obterPorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro entidade não encontrada")
        void deveLancarErro() {
             when(repo.buscar(Atividade.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 1L));
             assertThatThrownBy(() -> service.obterPorCodigo(1L))
                 .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar por mapa")
        void deveBuscarPorMapa() {
             when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(List.of(new Atividade()));
             assertThat(service.buscarPorMapaCodigo(1L))
                 .isNotNull()
                 .hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando mapa não possui atividades")
        void deveRetornarListaVaziaQuandoMapaSemAtividades() {
             when(atividadeRepo.findByMapaCodigo(999L)).thenReturn(List.of());
             assertThat(service.buscarPorMapaCodigo(999L))
                 .isNotNull()
                 .isEmpty();
        }

        @Test
        @DisplayName("Deve buscar por mapa com conhecimentos")
        void deveBuscarPorMapaComConhecimentos() {
             when(atividadeRepo.findByMapaCodigoWithConhecimentos(1L)).thenReturn(List.of(new Atividade()));
             assertThat(service.buscarPorMapaCodigoComConhecimentos(1L))
                 .isNotNull()
                 .hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando mapa não possui atividades com conhecimentos")
        void deveRetornarListaVaziaQuandoMapaSemAtividadesComConhecimentos() {
             when(atividadeRepo.findByMapaCodigoWithConhecimentos(999L)).thenReturn(List.of());
             assertThat(service.buscarPorMapaCodigoComConhecimentos(999L))
                 .isNotNull()
                 .isEmpty();
        }
    }

    @Nested
    @DisplayName("Criação de Atividade")
    class Criacao {
        @Test
        @DisplayName("Deve criar atividade com sucesso")
        void deveCriarAtividade() {
            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .mapaCodigo(1L)
                    .build();
            AtividadeResponse dto = AtividadeResponse.builder()
                    .mapaCodigo(1L)
                    .build();
            String titulo = "123";

            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            Unidade unidade = new Unidade();
            unidade.setTituloTitular(titulo);
            Subprocesso sub = new Subprocesso();
            sub.setUnidade(unidade);
            mapa.setSubprocesso(sub);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(titulo);

            when(repo.buscar(Mapa.class, 1L)).thenReturn(mapa);
            when(atividadeMapper.toEntity(request)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(new Atividade());
            when(atividadeMapper.toResponse(any())).thenReturn(dto);

            AtividadeResponse res = service.criar(request);

            assertThat(res).isNotNull();
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade sem mapa")
        void deveLancarErroAoCriarSemMapa() {
            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .mapaCodigo(null)
                    .build();

            when(repo.buscar(Mapa.class, null)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", null));

            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade em mapa inexistente")
        void deveLancarErroAoCriarEmMapaInexistente() {
            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .mapaCodigo(1L)
                    .build();

            when(repo.buscar(Mapa.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 1L));

            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade se mapa não tem subprocesso")
        void deveLancarErroAoCriarSemSubprocesso() {
            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .mapaCodigo(1L)
                    .build();

            Mapa mapa = new Mapa();
            mapa.setSubprocesso(null);

            when(repo.buscar(Mapa.class, 1L)).thenReturn(mapa);

            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso");
        }
    }

    @Nested
    @DisplayName("Atualização e Exclusão")
    class AtualizacaoExclusao {
        @Test
        @DisplayName("Deve atualizar atividade")
        void deveAtualizarAtividade() {
            Long id = 1L;
            AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder().build();
            Atividade atividade = new Atividade();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            atividade.setMapa(mapa);

            when(repo.buscar(Atividade.class, id)).thenReturn(atividade);
            when(atividadeMapper.toEntity(request)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(atividade);

            service.atualizar(id, request);

            verify(atividadeRepo).save(atividade);
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve atualizar atividade sem mapa associado (sem publicar evento)")
        void deveAtualizarAtividadeSemMapa() {
            Long id = 1L;
            AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder().build();
            Atividade atividade = new Atividade();
            atividade.setMapa(null);

            when(repo.buscar(Atividade.class, id)).thenReturn(atividade);
            when(atividadeMapper.toEntity(request)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(atividade);

            service.atualizar(id, request);

            verify(atividadeRepo).save(atividade);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se ocorrer erro inesperado")
        void deveRelancarExcecaoAoAtualizar() {
            Long id = 1L;
            AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder().build();

            when(repo.buscar(Atividade.class, id)).thenThrow(new RuntimeException("Erro banco"));

            assertThatThrownBy(() -> service.atualizar(id, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Erro banco");
        }

        @Test
        @DisplayName("Deve excluir atividade")
        void deveExcluirAtividade() {
            Long id = 1L;
            Atividade atividade = new Atividade();
            atividade.setMapa(new Mapa());

            when(repo.buscar(Atividade.class, id)).thenReturn(atividade);

            service.excluir(id);

            verify(conhecimentoService).excluirTodosDaAtividade(atividade);
            verify(atividadeRepo).delete(atividade);
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir atividade inexistente")
        void deveLancarErroAoExcluirAtividadeInexistente() {
            Long id = 1L;
            when(repo.buscar(Atividade.class, id)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", id));

            assertThatThrownBy(() -> service.excluir(id))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }
}

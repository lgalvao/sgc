package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

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
    private MapaRepo mapaRepo;
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
             when(atividadeMapper.toDto(any())).thenReturn(new AtividadeDto());
             assertThat(service.listar())
                 .isNotNull()
                 .hasSize(1);
        }

        @Test
        @DisplayName("Deve obter por código DTO")
        void deveObterPorCodigoDto() {
             when(atividadeRepo.findById(1L)).thenReturn(Optional.of(new Atividade()));
             when(atividadeMapper.toDto(any())).thenReturn(new AtividadeDto());
             assertThat(service.obterDto(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro se obter por código não encontrar")
        void deveLancarErroObterPorCodigo() {
             when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());
             assertThatThrownBy(() -> service.obterDto(1L))
                 .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve listar entidades por código")
        void deveListarEntidades() {
             Atividade ativ = new Atividade();
             when(atividadeRepo.findById(1L)).thenReturn(Optional.of(ativ));
             assertThat(service.obterPorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro entidade não encontrada")
        void deveLancarErro() {
             when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());
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
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);
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

            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(new Atividade());
            when(atividadeMapper.toDto(any())).thenReturn(dto);

            AtividadeDto res = service.criar(dto);

            assertThat(res).isNotNull();
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade sem mapa")
        void deveLancarErroAoCriarSemMapa() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(null);

            assertThatThrownBy(() -> service.criar(dto))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Mapa");
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade em mapa inexistente")
        void deveLancarErroAoCriarEmMapaInexistente() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);

            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criar(dto))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Mapa");
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade se mapa não tem subprocesso")
        void deveLancarErroAoCriarSemSubprocesso() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);

            Mapa mapa = new Mapa();
            mapa.setSubprocesso(null);

            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));

            assertThatThrownBy(() -> service.criar(dto))
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
            AtividadeDto dto = new AtividadeDto();
            Atividade atividade = new Atividade();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            atividade.setMapa(mapa);

            when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(atividade);

            service.atualizar(id, dto);

            verify(atividadeRepo).save(atividade);
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve atualizar atividade sem mapa associado (sem publicar evento)")
        void deveAtualizarAtividadeSemMapa() {
            Long id = 1L;
            AtividadeDto dto = new AtividadeDto();
            Atividade atividade = new Atividade();
            atividade.setMapa(null); // Explicitly no map

            when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(atividade);

            service.atualizar(id, dto);

            verify(atividadeRepo).save(atividade);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se ocorrer erro inesperado")
        void deveRelancarExcecaoAoAtualizar() {
            Long id = 1L;
            AtividadeDto dto = new AtividadeDto();

            // Simula erro ao buscar ou processar
            when(atividadeRepo.findById(id)).thenThrow(new RuntimeException("Erro banco"));

            assertThatThrownBy(() -> service.atualizar(id, dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Erro banco");
        }

        @Test
        @DisplayName("Deve excluir atividade")
        void deveExcluirAtividade() {
            Long id = 1L;
            Atividade atividade = new Atividade();
            atividade.setMapa(new Mapa());

            when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));

            service.excluir(id);

            verify(conhecimentoService).excluirTodosDaAtividade(atividade);
            verify(atividadeRepo).delete(atividade);
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir atividade inexistente")
        void deveLancarErroAoExcluirAtividadeInexistente() {
            Long id = 1L;
            when(atividadeRepo.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluir(id))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }
}

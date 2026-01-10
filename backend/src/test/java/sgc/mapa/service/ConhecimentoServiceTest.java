package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Conhecimento")
class ConhecimentoServiceTest {

    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConhecimentoService service;

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {
        @Test
        @DisplayName("Deve listar conhecimentos por atividade DTO")
        void deveListarConhecimentosDto() {
            when(atividadeRepo.existsById(1L)).thenReturn(true);
            when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of(new Conhecimento()));
            when(conhecimentoMapper.toDto(any())).thenReturn(new ConhecimentoDto());

            var resultado = service.listarPorAtividade(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado).isNotEmpty();
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando atividade não possui conhecimentos")
        void deveRetornarListaVaziaQuandoAtividadeSemConhecimentos() {
            when(atividadeRepo.existsById(1L)).thenReturn(true);
            when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of());

            var resultado = service.listarPorAtividade(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao listar conhecimentos se atividade inexistente")
        void deveLancarErroListarConhecimentos() {
            when(atividadeRepo.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> service.listarPorAtividade(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve listar conhecimentos entidades por atividade")
        void deveListarEntidadesPorAtividade() {
            when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of(new Conhecimento()));

            var resultado = service.listarEntidadesPorAtividade(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar conhecimentos por mapa")
        void deveListarPorMapa() {
            when(conhecimentoRepo.findByMapaCodigo(1L)).thenReturn(List.of(new Conhecimento()));

            var resultado = service.listarPorMapa(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Criação de Conhecimento")
    class CriacaoTests {
        @Test
        @DisplayName("Deve criar conhecimento com sucesso")
        void deveCriarConhecimento() {
            Long ativId = 1L;
            ConhecimentoDto dto = new ConhecimentoDto();
            Atividade atividade = new Atividade();
            atividade.setMapa(new Mapa());
            Conhecimento conhecimento = new Conhecimento();

            when(atividadeRepo.findById(ativId)).thenReturn(Optional.of(atividade));
            when(conhecimentoMapper.toEntity(dto)).thenReturn(new Conhecimento());
            when(conhecimentoRepo.save(any())).thenReturn(conhecimento);
            when(conhecimentoMapper.toDto(any())).thenReturn(dto);

            var resultado = service.criar(ativId, dto);

            assertThat(resultado).isNotNull();
            verify(conhecimentoRepo).save(any());
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao criar conhecimento para atividade inexistente")
        void deveLancarErroAoCriarConhecimentoAtividadeInexistente() {
            Long ativId = 1L;
            ConhecimentoDto dto = new ConhecimentoDto();
            when(atividadeRepo.findById(ativId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criar(ativId, dto))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Atualização de Conhecimento")
    class AtualizacaoTests {
        @Test
        @DisplayName("Deve atualizar conhecimento com sucesso")
        void deveAtualizarConhecimento() {
            Long ativId = 1L;
            Long conhId = 2L;
            ConhecimentoDto dto = new ConhecimentoDto();
            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(conhId);
            Atividade atividade = new Atividade();
            atividade.setCodigo(ativId);
            atividade.setMapa(new Mapa());
            conhecimento.setAtividade(atividade);

            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.of(conhecimento));
            when(conhecimentoMapper.toEntity(dto)).thenReturn(new Conhecimento());
            when(conhecimentoRepo.save(any())).thenReturn(conhecimento);
            when(conhecimentoMapper.toDto(any())).thenReturn(dto);

            service.atualizar(ativId, conhId, dto);

            verify(conhecimentoRepo).save(conhecimento);
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro se conhecimento não pertencer à atividade")
        void deveErroSeConhecimentoNaoPertenceAtividade() {
            Long ativId = 1L;
            Long conhId = 2L;
            Conhecimento conhecimento = new Conhecimento();
            Atividade outraAtividade = new Atividade();
            outraAtividade.setCodigo(99L);
            conhecimento.setAtividade(outraAtividade);

            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.of(conhecimento));

            assertThatThrownBy(() -> service.atualizar(ativId, conhId, new ConhecimentoDto()))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar conhecimento inexistente")
        void deveLancarErroAoAtualizarConhecimentoInexistente() {
            Long ativId = 1L;
            Long conhId = 2L;
            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(ativId, conhId, new ConhecimentoDto()))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Exclusão de Conhecimento")
    class ExclusaoTests {
        @Test
        @DisplayName("Deve excluir conhecimento com sucesso")
        void deveExcluirConhecimento() {
            Long ativId = 1L;
            Long conhId = 2L;
            Conhecimento conhecimento = new Conhecimento();
            Atividade atividade = new Atividade();
            atividade.setCodigo(ativId);
            atividade.setMapa(new Mapa());
            conhecimento.setAtividade(atividade);

            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.of(conhecimento));

            service.excluir(ativId, conhId);

            verify(conhecimentoRepo).delete(conhecimento);
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir conhecimento se não pertencer à atividade")
        void deveLancarErroAoExcluirConhecimentoOutraAtividade() {
            Long ativId = 1L;
            Long conhId = 2L;
            Conhecimento conhecimento = new Conhecimento();
            Atividade outra = new Atividade();
            outra.setCodigo(99L);
            conhecimento.setAtividade(outra);

            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.of(conhecimento));

            assertThatThrownBy(() -> service.excluir(ativId, conhId))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir conhecimento inexistente")
        void deveLancarErroAoExcluirConhecimentoInexistente() {
            Long ativId = 1L;
            Long conhId = 2L;
            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluir(ativId, conhId))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve excluir todos os conhecimentos de uma atividade")
        void deveExcluirTodosDaAtividade() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            Conhecimento c1 = new Conhecimento();
            Conhecimento c2 = new Conhecimento();

            when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of(c1, c2));

            service.excluirTodosDaAtividade(atividade);

            verify(conhecimentoRepo).deleteAll(List.of(c1, c2));
        }
    }
}

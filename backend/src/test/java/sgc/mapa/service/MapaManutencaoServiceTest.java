package sgc.mapa.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do Serviço de Manutenção de Mapa")
class MapaManutencaoServiceTest {

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private RepositorioComum repo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MapaManutencaoService service;

    @Test
    @DisplayName("Deve buscar por mapa sem relacionamentos")
    void deveBuscarPorMapaSemRelacionamentos() {
        when(atividadeRepo.findByMapaCodigoSemFetch(1L)).thenReturn(List.of(new Atividade()));
        assertThat(service.buscarAtividadesPorMapaCodigoSemRelacionamentos(1L))
                .isNotNull()
                .hasSize(1);
    }

    @Nested
    @DisplayName("Cenários de Leitura (Atividade)")
    class LeituraTests {
        @Test
        @DisplayName("Deve listar todas as atividades")
        void deveListarTodas() {
            when(atividadeRepo.findAllWithMapa()).thenReturn(List.of(new Atividade()));
            when(atividadeMapper.toResponse(any())).thenReturn(AtividadeResponse.builder().build());
            assertThat(service.listarAtividades())
                    .isNotNull()
                    .hasSize(1);
        }

        @Test
        @DisplayName("Deve obter por código Response")
        void deveObterPorCodigoDto() {
            when(repo.buscar(Atividade.class, 1L)).thenReturn(new Atividade());
            when(atividadeMapper.toResponse(any())).thenReturn(AtividadeResponse.builder().build());
            assertThat(service.obterAtividadeResponse(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro se obter por código não encontrar")
        void deveLancarErroObterPorCodigo() {
            when(repo.buscar(Atividade.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 1L));
            assertThatThrownBy(() -> service.obterAtividadeResponse(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve listar entidades por código")
        void deveListarEntidades() {
            Atividade ativ = new Atividade();
            when(repo.buscar(Atividade.class, 1L)).thenReturn(ativ);
            assertThat(service.obterAtividadePorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro entidade não encontrada")
        void deveLancarErro() {
            when(repo.buscar(Atividade.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 1L));
            assertThatThrownBy(() -> service.obterAtividadePorCodigo(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar por mapa")
        void deveBuscarPorMapa() {
            when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(List.of(new Atividade()));
            assertThat(service.buscarAtividadesPorMapaCodigo(1L))
                    .isNotNull()
                    .hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando mapa não possui atividades")
        void deveRetornarListaVaziaQuandoMapaSemAtividades() {
            when(atividadeRepo.findByMapaCodigo(999L)).thenReturn(List.of());
            assertThat(service.buscarAtividadesPorMapaCodigo(999L))
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("Deve buscar por mapa com conhecimentos")
        void deveBuscarPorMapaComConhecimentos() {
            when(atividadeRepo.findWithConhecimentosByMapaCodigo(1L)).thenReturn(List.of(new Atividade()));
            assertThat(service.buscarAtividadesPorMapaCodigoComConhecimentos(1L))
                    .isNotNull()
                    .hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando mapa não possui atividades com conhecimentos")
        void deveRetornarListaVaziaQuandoMapaSemAtividadesComConhecimentos() {
            when(atividadeRepo.findWithConhecimentosByMapaCodigo(999L)).thenReturn(List.of());
            assertThat(service.buscarAtividadesPorMapaCodigoComConhecimentos(999L))
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

            when(repo.buscar(Mapa.class, 1L)).thenReturn(mapa);
            when(atividadeMapper.toEntity(request)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(new Atividade());
            when(atividadeMapper.toResponse(any())).thenReturn(dto);

            AtividadeResponse res = service.criarAtividade(request);

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

            assertThatThrownBy(() -> service.criarAtividade(request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade em mapa inexistente")
        void deveLancarErroAoCriarEmMapaInexistente() {
            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .mapaCodigo(1L)
                    .build();

            when(repo.buscar(Mapa.class, 1L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 1L));

            assertThatThrownBy(() -> service.criarAtividade(request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Atualização e Exclusão (Atividade)")
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

            service.atualizarAtividade(id, request);

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

            service.atualizarAtividade(id, request);

            verify(atividadeRepo).save(atividade);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se ocorrer erro inesperado")
        void deveRelancarExcecaoAoAtualizar() {
            Long id = 1L;
            AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder().build();

            when(repo.buscar(Atividade.class, id)).thenThrow(new RuntimeException("Erro banco"));

            assertThatThrownBy(() -> service.atualizarAtividade(id, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Erro banco");
        }

        @Test
        @DisplayName("Deve excluir atividade")
        void deveExcluirAtividade() {
            Long id = 1L;
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(new Mapa());

            when(repo.buscar(Atividade.class, id)).thenReturn(atividade);
            when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of());

            service.excluirAtividade(id);

            verify(conhecimentoRepo).deleteAll(anyList());
            verify(atividadeRepo).delete(atividade);
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir atividade inexistente")
        void deveLancarErroAoExcluirAtividadeInexistente() {
            Long id = 1L;
            when(repo.buscar(Atividade.class, id)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", id));

            assertThatThrownBy(() -> service.excluirAtividade(id))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Atualização em Lote")
    class AtualizacaoLote {

        @Test
        @DisplayName("Deve atualizar descrições em lote")
        void deveAtualizarDescricoesEmLote() {
            Atividade atividade1 = new Atividade();
            atividade1.setCodigo(1L);
            atividade1.setDescricao("Antiga 1");
            Mapa mapa1 = new Mapa();
            mapa1.setCodigo(10L);
            atividade1.setMapa(mapa1);

            Atividade atividade2 = new Atividade();
            atividade2.setCodigo(2L);
            atividade2.setDescricao("Antiga 2");
            // Sem mapa

            java.util.Map<Long, String> descricoes = java.util.Map.of(
                    1L, "Nova 1",
                    2L, "Nova 2"
            );

            when(atividadeRepo.findAllById(descricoes.keySet())).thenReturn(List.of(atividade1, atividade2));

            service.atualizarDescricoesAtividadeEmLote(descricoes);

            assertThat(atividade1.getDescricao()).isEqualTo("Nova 1");
            assertThat(atividade2.getDescricao()).isEqualTo("Nova 2");

            verify(atividadeRepo).saveAll(anyList());
            verify(eventPublisher, times(1)).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve ignorar descrição nula")
        void deveIgnorarDescricaoNula() {
            Atividade atividade1 = new Atividade();
            atividade1.setCodigo(1L);
            atividade1.setDescricao("Antiga 1");

            java.util.Map<Long, String> descricoes = new java.util.HashMap<>();
            descricoes.put(1L, null);

            when(atividadeRepo.findAllById(descricoes.keySet())).thenReturn(List.of(atividade1));

            service.atualizarDescricoesAtividadeEmLote(descricoes);

            assertThat(atividade1.getDescricao()).isEqualTo("Antiga 1");
        }
    }
}
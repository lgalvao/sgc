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
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.*;
import sgc.mapa.eventos.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.*;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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
    private MapaRepo mapaRepo;
    @Mock
    private ComumRepo repo;
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
            when(atividadeRepo.findAll()).thenReturn(List.of(new Atividade()));
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

            Map<Long, String> descricoes = Map.of(
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

            Map<Long, String> descricoes = new HashMap<>();
            descricoes.put(1L, null);

            when(atividadeRepo.findAllById(descricoes.keySet())).thenReturn(List.of(atividade1));

            service.atualizarDescricoesAtividadeEmLote(descricoes);

            assertThat(atividade1.getDescricao()).isEqualTo("Antiga 1");
        }
    }

    @Nested
    @DisplayName("Competência - Leitura")
    class CompetenciaLeitura {

        @Test
        @DisplayName("Deve buscar competência por código")
        void deveBuscarCompetenciaPorCodigo() {
            Competencia competencia = Competencia.builder()
                    .codigo(1L)
                    .descricao("Competência Teste")
                    .build();

            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(competencia));

            Competencia resultado = service.buscarCompetenciaPorCodigo(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(1L);
            verify(competenciaRepo).findById(1L);
        }

        @Test
        @DisplayName("Deve lançar erro ao buscar competência inexistente")
        void deveLancarErroAoBuscarCompetenciaInexistente() {
            when(competenciaRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarCompetenciaPorCodigo(999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Competência")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Deve buscar competências por código de mapa")
        void deveBuscarCompetenciasPorCodMapa() {
            Competencia comp1 = Competencia.builder().codigo(1L).build();
            Competencia comp2 = Competencia.builder().codigo(2L).build();

            when(competenciaRepo.findByMapaCodigo(10L)).thenReturn(List.of(comp1, comp2));

            List<Competencia> resultado = service.buscarCompetenciasPorCodMapa(10L);

            assertThat(resultado).hasSize(2);
            verify(competenciaRepo).findByMapaCodigo(10L);
        }

        @Test
        @DisplayName("Deve buscar competências por código de mapa sem relacionamentos")
        void deveBuscarCompetenciasPorCodMapaSemRelacionamentos() {
            Competencia comp1 = Competencia.builder().codigo(1L).build();

            when(competenciaRepo.findByMapaCodigoSemFetch(10L)).thenReturn(List.of(comp1));

            List<Competencia> resultado = service.buscarCompetenciasPorCodMapaSemRelacionamentos(10L);

            assertThat(resultado).hasSize(1);
            verify(competenciaRepo).findByMapaCodigoSemFetch(10L);
        }

        @Test
        @DisplayName("Deve buscar IDs de associações competência-atividade")
        void deveBuscarIdsAssociacoesCompetenciaAtividade() {
            // Simula resultado da query: [compId, compDescricao, ativId]
            Object[] row1 = new Object[]{1L, "Comp 1", 10L};
            Object[] row2 = new Object[]{1L, "Comp 1", 20L};
            Object[] row3 = new Object[]{2L, "Comp 2", 30L};
            Object[] row4 = new Object[]{3L, "Comp 3", null}; // Competência sem atividade

            when(competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(10L))
                    .thenReturn(List.of(row1, row2, row3, row4));

            Map<Long, Set<Long>> resultado = service.buscarIdsAssociacoesCompetenciaAtividade(10L);

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(1L)).containsExactlyInAnyOrder(10L, 20L);
            assertThat(resultado.get(2L)).containsExactly(30L);
            assertThat(resultado.get(3L)).isNull(); // Competência sem atividade não deve aparecer
            verify(competenciaRepo).findCompetenciaAndAtividadeIdsByMapaCodigo(10L);
        }

        @Test
        @DisplayName("Deve buscar competências por códigos")
        void deveBuscarCompetenciasPorCodigos() {
            Competencia comp1 = Competencia.builder().codigo(1L).build();
            Competencia comp2 = Competencia.builder().codigo(2L).build();

            when(competenciaRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(comp1, comp2));

            List<Competencia> resultado = service.buscarCompetenciasPorCodigos(List.of(1L, 2L));

            assertThat(resultado).hasSize(2);
            verify(competenciaRepo).findAllById(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("Competência - Criação e Atualização")
    class CompetenciaCriacaoAtualizacao {

        @Test
        @DisplayName("Deve criar competência com atividades")
        void deveCriarCompetenciaComAtividades() {
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);

            Atividade ativ1 = new Atividade();
            ativ1.setCodigo(1L);
            ativ1.setCompetencias(new HashSet<>());

            Atividade ativ2 = new Atividade();
            ativ2.setCodigo(2L);
            ativ2.setCompetencias(new HashSet<>());

            when(atividadeRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(ativ1, ativ2));

            service.criarCompetenciaComAtividades(mapa, "Competência Nova", List.of(1L, 2L));

            verify(competenciaRepo).save(any(Competencia.class));
            verify(atividadeRepo).saveAll(argThat(atividades -> 
                atividades != null && ((Collection<?>) atividades).size() == 2
            ));
        }

        @Test
        @DisplayName("Deve criar competência sem atividades (lista vazia)")
        void deveCriarCompetenciaSemAtividades() {
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);

            service.criarCompetenciaComAtividades(mapa, "Competência Vazia", List.of());

            verify(competenciaRepo).save(any(Competencia.class));
            verify(atividadeRepo).saveAll(argThat(atividades -> 
                atividades != null && ((Collection<?>) atividades).isEmpty()
            ));
            verify(atividadeRepo, never()).findAllById(anyList());
        }

        @Test
        @DisplayName("Deve atualizar competência com novas atividades")
        void deveAtualizarCompetencia() {
            Competencia competencia = Competencia.builder()
                    .codigo(1L)
                    .descricao("Competência Antiga")
                    .atividades(new HashSet<>())
                    .build();

            Atividade ativAntiga = new Atividade();
            ativAntiga.setCodigo(10L);
            ativAntiga.setCompetencias(new HashSet<>(Set.of(competencia)));

            Atividade ativNova = new Atividade();
            ativNova.setCodigo(20L);
            ativNova.setCompetencias(new HashSet<>());

            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(competencia));
            when(atividadeRepo.listarPorCompetencia(competencia)).thenReturn(List.of(ativAntiga));
            when(atividadeRepo.findAllById(List.of(20L))).thenReturn(List.of(ativNova));

            service.atualizarCompetencia(1L, "Competência Atualizada", List.of(20L));

            assertThat(competencia.getDescricao()).isEqualTo("Competência Atualizada");
            verify(competenciaRepo).save(competencia);
            verify(atividadeRepo, atLeastOnce()).saveAll(anyList());
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar competência inexistente")
        void deveLancarErroAoAtualizarCompetenciaInexistente() {
            when(competenciaRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarCompetencia(999L, "Nova", List.of(1L)))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Competência")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Deve salvar competência única")
        void deveSalvarCompetencia() {
            Competencia competencia = Competencia.builder().codigo(1L).build();

            service.salvarCompetencia(competencia);

            verify(competenciaRepo).save(competencia);
        }

        @Test
        @DisplayName("Deve salvar todas as competências")
        void deveSalvarTodasCompetencias() {
            Competencia comp1 = Competencia.builder().codigo(1L).build();
            Competencia comp2 = Competencia.builder().codigo(2L).build();

            service.salvarTodasCompetencias(List.of(comp1, comp2));

            verify(competenciaRepo).saveAll(List.of(comp1, comp2));
        }
    }

    @Nested
    @DisplayName("Competência - Remoção")
    class CompetenciaRemocao {

        @Test
        @DisplayName("Deve remover competência com atividades associadas")
        void deveRemoverCompetenciaComAtividades() {
            Competencia competencia = Competencia.builder()
                    .codigo(1L)
                    .descricao("Competência")
                    .build();

            Atividade ativ1 = new Atividade();
            ativ1.setCodigo(10L);
            ativ1.setCompetencias(new HashSet<>(Set.of(competencia)));

            Atividade ativ2 = new Atividade();
            ativ2.setCodigo(20L);
            ativ2.setCompetencias(new HashSet<>(Set.of(competencia)));

            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(competencia));
            when(atividadeRepo.listarPorCompetencia(competencia)).thenReturn(List.of(ativ1, ativ2));

            service.removerCompetencia(1L);

            assertThat(ativ1.getCompetencias()).doesNotContain(competencia);
            assertThat(ativ2.getCompetencias()).doesNotContain(competencia);
            verify(atividadeRepo).saveAll(List.of(ativ1, ativ2));
            verify(competenciaRepo).delete(competencia);
        }

        @Test
        @DisplayName("Deve remover competência sem atividades associadas")
        void deveRemoverCompetenciaSemAtividades() {
            Competencia competencia = Competencia.builder()
                    .codigo(1L)
                    .descricao("Competência Vazia")
                    .build();

            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(competencia));
            when(atividadeRepo.listarPorCompetencia(competencia)).thenReturn(List.of());

            service.removerCompetencia(1L);

            verify(atividadeRepo).saveAll(List.of());
            verify(competenciaRepo).delete(competencia);
        }

        @Test
        @DisplayName("Deve lançar erro ao remover competência inexistente")
        void deveLancarErroAoRemoverCompetenciaInexistente() {
            when(competenciaRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removerCompetencia(999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Competência")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Conhecimento - Leitura")
    class ConhecimentoLeitura {

        @Test
        @DisplayName("Deve listar conhecimentos por atividade")
        void deveListarConhecimentosPorAtividade() {
            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);

            when(atividadeRepo.existsById(10L)).thenReturn(true);
            when(conhecimentoRepo.findByAtividadeCodigo(10L)).thenReturn(List.of(conhecimento));
            when(conhecimentoMapper.toResponse(any())).thenReturn(ConhecimentoResponse.builder().build());

            List<ConhecimentoResponse> resultado = service.listarConhecimentosPorAtividade(10L);

            assertThat(resultado).hasSize(1);
            verify(atividadeRepo).existsById(10L);
            verify(conhecimentoRepo).findByAtividadeCodigo(10L);
        }

        @Test
        @DisplayName("Deve lançar erro ao listar conhecimentos de atividade inexistente")
        void deveLancarErroAoListarConhecimentosDeAtividadeInexistente() {
            when(atividadeRepo.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.listarConhecimentosPorAtividade(999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Atividade")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Deve listar conhecimentos entidades por atividade")
        void deveListarConhecimentosEntidadesPorAtividade() {
            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);

            when(conhecimentoRepo.findByAtividadeCodigo(10L)).thenReturn(List.of(conhecimento));

            List<Conhecimento> resultado = service.listarConhecimentosEntidadesPorAtividade(10L);

            assertThat(resultado).hasSize(1);
            verify(conhecimentoRepo).findByAtividadeCodigo(10L);
        }

        @Test
        @DisplayName("Deve listar conhecimentos por mapa")
        void deveListarConhecimentosPorMapa() {
            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);

            when(conhecimentoRepo.findByMapaCodigo(10L)).thenReturn(List.of(conhecimento));

            List<Conhecimento> resultado = service.listarConhecimentosPorMapa(10L);

            assertThat(resultado).hasSize(1);
            verify(conhecimentoRepo).findByMapaCodigo(10L);
        }
    }

    @Nested
    @DisplayName("Conhecimento - Criação")
    class ConhecimentoCriacao {

        @Test
        @DisplayName("Deve criar conhecimento com mapa associado")
        void deveCriarConhecimentoComMapa() {
            CriarConhecimentoRequest request = CriarConhecimentoRequest.builder()
                    .descricao("Conhecimento Novo")
                    .build();

            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);

            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(mapa);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);

            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));
            when(conhecimentoMapper.toEntity(request)).thenReturn(conhecimento);
            when(conhecimentoRepo.save(any())).thenReturn(conhecimento);
            when(conhecimentoMapper.toResponse(conhecimento)).thenReturn(ConhecimentoResponse.builder().build());

            ConhecimentoResponse resultado = service.criarConhecimento(1L, request);

            assertThat(resultado).isNotNull();
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
            verify(conhecimentoRepo).save(conhecimento);
        }

        @Test
        @DisplayName("Deve criar conhecimento sem mapa associado (sem publicar evento)")
        void deveCriarConhecimentoSemMapa() {
            CriarConhecimentoRequest request = CriarConhecimentoRequest.builder()
                    .descricao("Conhecimento Novo")
                    .build();

            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(null);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);

            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(atividade));
            when(conhecimentoMapper.toEntity(request)).thenReturn(conhecimento);
            when(conhecimentoRepo.save(any())).thenReturn(conhecimento);
            when(conhecimentoMapper.toResponse(conhecimento)).thenReturn(ConhecimentoResponse.builder().build());

            ConhecimentoResponse resultado = service.criarConhecimento(1L, request);

            assertThat(resultado).isNotNull();
            verify(eventPublisher, never()).publishEvent(any());
            verify(conhecimentoRepo).save(conhecimento);
        }

        @Test
        @DisplayName("Deve lançar erro ao criar conhecimento em atividade inexistente")
        void deveLancarErroAoCriarConhecimentoEmAtividadeInexistente() {
            CriarConhecimentoRequest request = CriarConhecimentoRequest.builder()
                    .descricao("Conhecimento")
                    .build();

            when(atividadeRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarConhecimento(999L, request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Atividade")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Conhecimento - Atualização")
    class ConhecimentoAtualizacao {

        @Test
        @DisplayName("Deve atualizar conhecimento com mapa associado")
        void deveAtualizarConhecimentoComMapa() {
            AtualizarConhecimentoRequest request = AtualizarConhecimentoRequest.builder()
                    .descricao("Conhecimento Atualizado")
                    .build();

            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);

            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(mapa);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setAtividade(atividade);

            Conhecimento paraAtualizar = new Conhecimento();
            paraAtualizar.setDescricao("Conhecimento Atualizado");

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));
            when(conhecimentoMapper.toEntity(request)).thenReturn(paraAtualizar);

            service.atualizarConhecimento(1L, 1L, request);

            assertThat(conhecimento.getDescricao()).isEqualTo("Conhecimento Atualizado");
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
            verify(conhecimentoRepo).save(conhecimento);
        }

        @Test
        @DisplayName("Deve atualizar conhecimento sem mapa associado (sem publicar evento)")
        void deveAtualizarConhecimentoSemMapa() {
            AtualizarConhecimentoRequest request = AtualizarConhecimentoRequest.builder()
                    .descricao("Conhecimento Atualizado")
                    .build();

            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(null);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setAtividade(atividade);

            Conhecimento paraAtualizar = new Conhecimento();
            paraAtualizar.setDescricao("Conhecimento Atualizado");

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));
            when(conhecimentoMapper.toEntity(request)).thenReturn(paraAtualizar);

            service.atualizarConhecimento(1L, 1L, request);

            assertThat(conhecimento.getDescricao()).isEqualTo("Conhecimento Atualizado");
            verify(eventPublisher, never()).publishEvent(any());
            verify(conhecimentoRepo).save(conhecimento);
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar conhecimento de atividade diferente")
        void deveLancarErroAoAtualizarConhecimentoDeAtividadeDiferente() {
            AtualizarConhecimentoRequest request = AtualizarConhecimentoRequest.builder()
                    .descricao("Conhecimento")
                    .build();

            Atividade atividade = new Atividade();
            atividade.setCodigo(999L); // Diferente de 1L

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setAtividade(atividade);

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));

            assertThatThrownBy(() -> service.atualizarConhecimento(1L, 1L, request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Conhecimento")
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar conhecimento inexistente")
        void deveLancarErroAoAtualizarConhecimentoInexistente() {
            AtualizarConhecimentoRequest request = AtualizarConhecimentoRequest.builder()
                    .descricao("Conhecimento")
                    .build();

            when(conhecimentoRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarConhecimento(1L, 999L, request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Conhecimento")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Conhecimento - Exclusão")
    class ConhecimentoExclusao {

        @Test
        @DisplayName("Deve excluir conhecimento com mapa associado")
        void deveExcluirConhecimentoComMapa() {
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);

            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(mapa);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setAtividade(atividade);

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));

            service.excluirConhecimento(1L, 1L);

            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
            verify(conhecimentoRepo).delete(conhecimento);
        }

        @Test
        @DisplayName("Deve excluir conhecimento sem mapa associado (sem publicar evento)")
        void deveExcluirConhecimentoSemMapa() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(null);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setAtividade(atividade);

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));

            service.excluirConhecimento(1L, 1L);

            verify(eventPublisher, never()).publishEvent(any());
            verify(conhecimentoRepo).delete(conhecimento);
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir conhecimento de atividade diferente")
        void deveLancarErroAoExcluirConhecimentoDeAtividadeDiferente() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(999L); // Diferente de 1L

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setAtividade(atividade);

            when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));

            assertThatThrownBy(() -> service.excluirConhecimento(1L, 1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Conhecimento")
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir conhecimento inexistente")
        void deveLancarErroAoExcluirConhecimentoInexistente() {
            when(conhecimentoRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluirConhecimento(1L, 999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Conhecimento")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Exclusão de Atividade - Cenários de Mapa")
    class ExclusaoAtividadeMapaCenarios {

        @Test
        @DisplayName("Deve excluir atividade sem mapa associado (sem publicar evento)")
        void deveExcluirAtividadeSemMapa() {
            Long id = 1L;
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setMapa(null); // Sem mapa

            when(repo.buscar(Atividade.class, id)).thenReturn(atividade);
            when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of());

            service.excluirAtividade(id);

            verify(conhecimentoRepo).deleteAll(anyList());
            verify(atividadeRepo).delete(atividade);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
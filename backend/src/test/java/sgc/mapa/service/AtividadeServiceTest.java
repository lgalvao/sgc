package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Usuario;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Atividade")
class AtividadeServiceTest {

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private UsuarioService usuarioService;
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
             var resultado = service.listar();
             assertThat(resultado).isNotNull();
             assertThat(resultado).isNotEmpty();
             assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve obter por código DTO")
        void deveObterPorCodigoDto() {
             when(atividadeRepo.findById(1L)).thenReturn(Optional.of(new Atividade()));
             when(atividadeMapper.toDto(any())).thenReturn(new AtividadeDto());
             assertThat(service.obterPorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro se obter por código não encontrar")
        void deveLancarErroObterPorCodigo() {
             when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());
             assertThatThrownBy(() -> service.obterPorCodigo(1L))
                 .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve listar entidades por código")
        void deveListarEntidades() {
             Atividade ativ = new Atividade();
             when(atividadeRepo.findById(1L)).thenReturn(Optional.of(ativ));
             assertThat(service.obterEntidadePorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar erro entidade não encontrada")
        void deveLancarErro() {
             when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());
             assertThatThrownBy(() -> service.obterEntidadePorCodigo(1L))
                 .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar por mapa")
        void deveBuscarPorMapa() {
             when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(List.of(new Atividade()));
             var resultado = service.buscarPorMapaCodigo(1L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isNotEmpty();
             assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando mapa não possui atividades")
        void deveRetornarListaVaziaQuandoMapaSemAtividades() {
             when(atividadeRepo.findByMapaCodigo(999L)).thenReturn(List.of());
             var resultado = service.buscarPorMapaCodigo(999L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar por mapa com conhecimentos")
        void deveBuscarPorMapaComConhecimentos() {
             when(atividadeRepo.findByMapaCodigoWithConhecimentos(1L)).thenReturn(List.of(new Atividade()));
             var resultado = service.buscarPorMapaCodigoComConhecimentos(1L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isNotEmpty();
             assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando mapa não possui atividades com conhecimentos")
        void deveRetornarListaVaziaQuandoMapaSemAtividadesComConhecimentos() {
             when(atividadeRepo.findByMapaCodigoWithConhecimentos(999L)).thenReturn(List.of());
             var resultado = service.buscarPorMapaCodigoComConhecimentos(999L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve listar conhecimentos por atividade e mapa")
        void deveListarConhecimentosEntidade() {
             when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of(new Conhecimento()));
             var resultadoAtividade = service.listarConhecimentosPorAtividade(1L);
             assertThat(resultadoAtividade).isNotNull();
             assertThat(resultadoAtividade).isNotEmpty();
             assertThat(resultadoAtividade).hasSize(1);

             when(conhecimentoRepo.findByMapaCodigo(1L)).thenReturn(List.of(new Conhecimento()));
             var resultadoMapa = service.listarConhecimentosPorMapa(1L);
             assertThat(resultadoMapa).isNotNull();
             assertThat(resultadoMapa).isNotEmpty();
             assertThat(resultadoMapa).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando atividade não possui conhecimentos")
        void deveRetornarListaVaziaQuandoAtividadeSemConhecimentos() {
             when(conhecimentoRepo.findByAtividadeCodigo(999L)).thenReturn(List.of());
             var resultado = service.listarConhecimentosPorAtividade(999L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando mapa não possui conhecimentos")
        void deveRetornarListaVaziaQuandoMapaSemConhecimentos() {
             when(conhecimentoRepo.findByMapaCodigo(999L)).thenReturn(List.of());
             var resultado = service.listarConhecimentosPorMapa(999L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve listar conhecimentos DTO")
        void deveListarConhecimentosDto() {
             when(atividadeRepo.existsById(1L)).thenReturn(true);
             when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of(new Conhecimento()));
             when(conhecimentoMapper.toDto(any())).thenReturn(new ConhecimentoDto());
             var resultado = service.listarConhecimentos(1L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isNotEmpty();
             assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia de conhecimentos DTO quando atividade não possui conhecimentos")
        void deveRetornarListaVaziaConhecimentosDtoQuandoAtividadeSemConhecimentos() {
             when(atividadeRepo.existsById(1L)).thenReturn(true);
             when(conhecimentoRepo.findByAtividadeCodigo(1L)).thenReturn(List.of());
             var resultado = service.listarConhecimentos(1L);
             assertThat(resultado).isNotNull();
             assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao listar conhecimentos se atividade inexistente")
        void deveLancarErroListarConhecimentos() {
             when(atividadeRepo.existsById(1L)).thenReturn(false);
             assertThatThrownBy(() -> service.listarConhecimentos(1L))
                 .isInstanceOf(ErroEntidadeNaoEncontrada.class);
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
            when(usuarioService.buscarEntidadePorId(titulo)).thenReturn(usuario);
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(new Atividade());
            when(atividadeMapper.toDto(any())).thenReturn(dto);

            AtividadeDto res = service.criar(dto, titulo);

            assertThat(res).isNotNull();
            verify(eventPublisher).publishEvent(any(EventoMapaAlterado.class));
        }

        @Test
        @DisplayName("Deve negar criação se usuário não for titular")
        void deveNegarSeUsuarioNaoTitular() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);
            String titulo = "123";
            String outroTitulo = "456";

            Mapa mapa = new Mapa();
            Unidade unidade = new Unidade();
            unidade.setTituloTitular(outroTitulo);
            Subprocesso sub = new Subprocesso();
            sub.setUnidade(unidade);
            mapa.setSubprocesso(sub);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(titulo);

            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            when(usuarioService.buscarEntidadePorId(titulo)).thenReturn(usuario);

            assertThatThrownBy(() -> service.criar(dto, titulo))
                    .isInstanceOf(ErroAccessoNegado.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade sem mapa")
        void deveLancarErroAoCriarSemMapa() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(null);
            String titulo = "123";

            assertThatThrownBy(() -> service.criar(dto, titulo))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Mapa");
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade em mapa inexistente")
        void deveLancarErroAoCriarEmMapaInexistente() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);
            String titulo = "123";

            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criar(dto, titulo))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Mapa");
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade se mapa não tem subprocesso")
        void deveLancarErroAoCriarSemSubprocesso() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);
            String titulo = "123";

            Mapa mapa = new Mapa();
            mapa.setSubprocesso(null);

            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));

            assertThatThrownBy(() -> service.criar(dto, titulo))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso");
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade se subprocesso não tem unidade")
        void deveLancarErroAoCriarSemUnidade() {
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(1L);
            String titulo = "123";

            Mapa mapa = new Mapa();
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setUnidade(null);
            mapa.setSubprocesso(subprocesso);

            Usuario usuario = new Usuario();

            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            when(usuarioService.buscarEntidadePorId(titulo)).thenReturn(usuario);

            assertThatThrownBy(() -> service.criar(dto, titulo))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade");
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
            atividade.setMapa(new Mapa());

            when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(atividade);
            when(atividadeMapper.toDto(any())).thenReturn(dto);

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
            when(atividadeMapper.toDto(any())).thenReturn(dto);

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

    @Nested
    @DisplayName("Gestão de Conhecimentos")
    class Conhecimentos {
        @Test
        @DisplayName("Deve criar conhecimento")
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

            var resultado = service.criarConhecimento(ativId, dto);

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

            assertThatThrownBy(() -> service.criarConhecimento(ativId, dto))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve atualizar conhecimento")
        void deveAtualizarConhecimento() {
            Long ativId = 1L;
            Long conhId = 2L;
            ConhecimentoDto dto = new ConhecimentoDto();
            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(conhId);
            Atividade atividade = new Atividade();
            atividade.setCodigo(ativId); // Ensure ID is set for getCodigoAtividade()
            atividade.setMapa(new Mapa());
            conhecimento.setAtividade(atividade);

            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.of(conhecimento));
            when(conhecimentoMapper.toEntity(dto)).thenReturn(new Conhecimento());
            when(conhecimentoRepo.save(any())).thenReturn(conhecimento);

            // Mocking the mapper for the result
            when(conhecimentoMapper.toDto(any())).thenReturn(dto);

            service.atualizarConhecimento(ativId, conhId, dto);

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

            assertThatThrownBy(() -> service.atualizarConhecimento(ativId, conhId, new ConhecimentoDto()))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar conhecimento inexistente")
        void deveLancarErroAoAtualizarConhecimentoInexistente() {
            Long ativId = 1L;
            Long conhId = 2L;
            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarConhecimento(ativId, conhId, new ConhecimentoDto()))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

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

            service.excluirConhecimento(ativId, conhId);

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

            assertThatThrownBy(() -> service.excluirConhecimento(ativId, conhId))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir conhecimento inexistente")
        void deveLancarErroAoExcluirConhecimentoInexistente() {
            Long ativId = 1L;
            Long conhId = 2L;
            when(conhecimentoRepo.findById(conhId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluirConhecimento(ativId, conhId))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Importação de Atividades")
    class Importacao {
        @Test
        @DisplayName("Deve importar atividades de outro mapa")
        void deveImportarAtividades() {
             Long origem = 1L;
             Long destino = 2L;

             Atividade ativOrigem = new Atividade();
             ativOrigem.setDescricao("A1");
             ativOrigem.setCodigo(10L);
             // Setup knowledge directly on the activity, simulating fetch join
             ativOrigem.setConhecimentos(List.of(new Conhecimento("C1", ativOrigem)));

             Mapa mapaDestino = new Mapa();

             // Changed mock to expect the optimized query
             when(atividadeRepo.findByMapaCodigoWithConhecimentos(origem)).thenReturn(List.of(ativOrigem));
             when(atividadeRepo.findByMapaCodigo(destino)).thenReturn(List.of()); // Nenhuma existente
             when(mapaRepo.findById(destino)).thenReturn(Optional.of(mapaDestino));
             when(atividadeRepo.save(any())).thenReturn(new Atividade());
             // Removed mock for conhecimentoRepo.findByAtividadeCodigo since it should not be called

             service.importarAtividadesDeOutroMapa(origem, destino);

             verify(atividadeRepo).save(any(Atividade.class));
             verify(conhecimentoRepo).save(any(Conhecimento.class));
        }

        @Test
        @DisplayName("Deve ignorar atividade existente ao importar")
        void deveIgnorarAtividadeExistenteAoImportar() {
            Long origem = 1L;
            Long destino = 2L;

            Atividade ativOrigem = new Atividade();
            ativOrigem.setDescricao("A1");
            ativOrigem.setCodigo(10L);

            Atividade ativExistente = new Atividade();
            ativExistente.setDescricao("A1"); // Mesma descrição

            Mapa mapaDestino = new Mapa();

            when(atividadeRepo.findByMapaCodigoWithConhecimentos(origem)).thenReturn(List.of(ativOrigem));
            when(atividadeRepo.findByMapaCodigo(destino)).thenReturn(List.of(ativExistente));
            when(mapaRepo.findById(destino)).thenReturn(Optional.of(mapaDestino));

            service.importarAtividadesDeOutroMapa(origem, destino);

            // Verifica que NÃO salvou nada
            verify(atividadeRepo, never()).save(any(Atividade.class));
            verify(conhecimentoRepo, never()).save(any(Conhecimento.class));
        }

        @Test
        @DisplayName("Deve importar sem conhecimentos se lista for nula")
        void deveImportarSemConhecimentos() {
            Long origem = 1L;
            Long destino = 2L;

            Atividade ativOrigem = new Atividade();
            ativOrigem.setDescricao("A1");
            ativOrigem.setConhecimentos(null); // Lista nula

            Mapa mapaDestino = new Mapa();

            when(atividadeRepo.findByMapaCodigoWithConhecimentos(origem)).thenReturn(List.of(ativOrigem));
            when(atividadeRepo.findByMapaCodigo(destino)).thenReturn(List.of());
            when(mapaRepo.findById(destino)).thenReturn(Optional.of(mapaDestino));
            when(atividadeRepo.save(any())).thenReturn(new Atividade());

            service.importarAtividadesDeOutroMapa(origem, destino);

            verify(atividadeRepo).save(any(Atividade.class));
            verify(conhecimentoRepo, never()).save(any(Conhecimento.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao importar se mapa destino não existir")
        void deveLancarErroAoImportarSeMapaDestinoInexistente() {
            Long origem = 1L;
            Long destino = 2L;

            Atividade ativOrigem = new Atividade();
            ativOrigem.setDescricao("A1");

            when(atividadeRepo.findByMapaCodigoWithConhecimentos(origem)).thenReturn(List.of(ativOrigem));
            when(atividadeRepo.findByMapaCodigo(destino)).thenReturn(List.of());
            when(mapaRepo.findById(destino)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.importarAtividadesDeOutroMapa(origem, destino))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }
}

package sgc.subprocesso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.atividade.internal.model.Atividade;
import sgc.atividade.internal.model.AtividadeRepo;
import sgc.atividade.internal.model.ConhecimentoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.MapaFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para SubprocessoService")
class SubprocessoServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoRepo repositorioConhecimento;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private MapaRepo mapaRepo;

    @InjectMocks
    private SubprocessoService service;

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {

        @Test
        @DisplayName("Deve retornar situação quando subprocesso existe")
        void deveRetornarSituacaoQuandoSubprocessoExiste() {
            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(null, null);
            sp.setCodigo(1L);
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            assertThat(service.obterSituacao(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando subprocesso não existe ao buscar situação")
        void deveLancarExcecaoQuandoSubprocessoNaoExiste() {
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.obterSituacao(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve retornar entidade por código do mapa")
        void deveRetornarEntidadePorCodigoMapa() {
            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(SubprocessoFixture.subprocessoPadrao(null, null)));
            assertThat(service.obterEntidadePorCodigoMapa(100L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades sem conhecimento")
        void deveRetornarListaVaziaSeNaoHouverAtividadesSemConhecimento() {
            Subprocesso subprocesso = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            subprocesso.setMapa(mapa);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(Collections.emptyList());

            List<Atividade> result = service.obterAtividadesSemConhecimento(1L);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cenários de Escrita (CRUD)")
    class CrudTests {
        @Test
        @DisplayName("Deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            SubprocessoDto dto = SubprocessoDto.builder().build();
            Subprocesso entity = SubprocessoFixture.subprocessoPadrao(null, null);

            when(subprocessoMapper.toEntity(dto)).thenReturn(entity);
            when(repositorioSubprocesso.save(any())).thenReturn(entity);
            when(mapaRepo.save(any())).thenReturn(MapaFixture.mapaPadrao(null));
            when(subprocessoMapper.toDTO(any())).thenReturn(dto);

            assertThat(service.criar(dto)).isNotNull();
            verify(repositorioSubprocesso, times(2)).save(any());
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            SubprocessoDto dto = SubprocessoDto.builder().codMapa(100L).build();
            Subprocesso entity = SubprocessoFixture.subprocessoPadrao(null, null);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(entity));
            when(repositorioSubprocesso.save(any())).thenReturn(entity);
            when(subprocessoMapper.toDTO(any())).thenReturn(dto);

            assertThat(service.atualizar(1L, dto)).isNotNull();
        }

        @Test
        @DisplayName("Deve excluir subprocesso com sucesso")
        void deveExcluirSubprocessoComSucesso() {
            when(repositorioSubprocesso.existsById(1L)).thenReturn(true);
            service.excluir(1L);
            verify(repositorioSubprocesso).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao excluir subprocesso inexistente")
        void deveLancarExcecaoAoExcluirSubprocessoInexistente() {
            when(repositorioSubprocesso.existsById(1L)).thenReturn(false);
            assertThatThrownBy(() -> service.excluir(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasNoCause();
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    class ValidacaoTests {
        @Test
        @DisplayName("Deve lançar exceção se competência não estiver associada")
        void deveLancarExcecaoSeCompetenciaNaoEstiverAssociada() {
            Competencia competencia = new Competencia();
            competencia.setDescricao("Competencia de Teste");
            when(competenciaRepo.findByMapaCodigo(1L))
                    .thenReturn(Collections.singletonList(competencia));

            assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("competência")
                    .hasNoCause();
        }
    }
}

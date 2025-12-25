package sgc.atividade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.api.AtividadeDto;
import sgc.atividade.internal.AtividadeMapper;
import sgc.atividade.api.ConhecimentoDto;
import sgc.atividade.internal.ConhecimentoMapper;
import sgc.atividade.internal.model.Atividade;
import sgc.atividade.internal.model.AtividadeRepo;
import sgc.atividade.internal.model.Conhecimento;
import sgc.atividade.internal.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.internal.model.Mapa;
import sgc.processo.api.model.Processo;
import sgc.processo.api.model.TipoProcesso;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioRepo;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;

import sgc.fixture.AtividadeFixture;
import sgc.fixture.MapaFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: AtividadeService")
class AtividadeServiceTest {

    @InjectMocks
    private AtividadeService atividadeService;

    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private UsuarioRepo usuarioRepo;

    @Nested
    @DisplayName("Método: listar")
    class Listar {
        @Test
        @DisplayName("Deve retornar todas as atividades")
        void deveRetornarTodasAtividades() {
            // Given
            when(atividadeRepo.findAll()).thenReturn(List.of(AtividadeFixture.atividadePadrao(null)));
            when(atividadeMapper.toDto(any())).thenReturn(new AtividadeDto());

            // When
            var result = atividadeService.listar();

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Método: obterPorCodigo")
    class ObterPorCodigo {
        @Test
        @DisplayName("Deve retornar atividade quando existir")
        void deveRetornarAtividadeQuandoExistir() {
            // Given
            Long id = 1L;
            when(atividadeRepo.findById(id)).thenReturn(Optional.of(AtividadeFixture.atividadePadrao(null)));
            when(atividadeMapper.toDto(any())).thenReturn(new AtividadeDto());

            // When
            var result = atividadeService.obterPorCodigo(id);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando atividade não existir")
        void deveLancarExcecaoQuandoNaoExistir() {
            // Given
            Long id = 1L;
            when(atividadeRepo.findById(id)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> atividadeService.obterPorCodigo(id))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Atividade")
                    .hasNoCause();
        }
    }

    @Nested
    @DisplayName("Método: criar")
    class Criar {
        @Test
        @DisplayName("Deve salvar e retornar atividade quando usuario autorizado")
        void deveSalvarERetornarAtividadeQuandoUsuarioAutorizado() {
            // Given
            Long mapaId = 10L;
            String usuarioId = "user1";
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(mapaId);

            Unidade unidade = UnidadeFixture.unidadePadrao();
            unidade.setTituloTitular(usuarioId);

            Usuario usuario = UsuarioFixture.usuarioComTitulo(usuarioId);

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(null, unidade);
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
            mapa.setCodigo(mapaId);
            subprocesso.setMapa(mapa);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(new Atividade());
            when(atividadeMapper.toDto(any())).thenReturn(dto);

            // When
            var result = atividadeService.criar(dto, usuarioId);

            // Then
            assertThat(result).isNotNull();
            verify(atividadeRepo).save(any());
        }

        @Test
        @DisplayName("Deve atualizar situacao do subprocesso quando nao iniciado")
        void deveAtualizarSituacaoQuandoNaoIniciado() {
            // Given
            Long mapaId = 10L;
            String usuarioId = "user1";
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(mapaId);

            Unidade unidade = UnidadeFixture.unidadePadrao();
            unidade.setTituloTitular(usuarioId);

            Usuario usuario = UsuarioFixture.usuarioComTitulo(usuarioId);

            Processo processo = new Processo();
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
            subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
            mapa.setCodigo(mapaId);
            subprocesso.setMapa(mapa);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any())).thenReturn(new Atividade());
            when(atividadeMapper.toDto(any())).thenReturn(dto);

            // When
            atividadeService.criar(dto, usuarioId);

            // Then
            assertThat(subprocesso.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            verify(subprocessoRepo).save(subprocesso);
        }

        @Test
        @DisplayName("Deve falhar quando subprocesso nao encontrado")
        void deveFalharQuandoSubprocessoNaoEncontrado() {
            // Given
            Long mapaId = 10L;
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(mapaId);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> atividadeService.criar(dto, "user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve falhar quando usuario nao encontrado")
        void deveFalharQuandoUsuarioNaoEncontrado() {
            // Given
            Long mapaId = 10L;
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(mapaId);

            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(null, null);
            Mapa mapa = MapaFixture.mapaPadrao(sp);
            mapa.setCodigo(mapaId);
            sp.setMapa(mapa);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(sp));
            when(usuarioRepo.findById("user")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> atividadeService.criar(dto, "user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Usuário")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuario não for titular")
        void deveLancarExcecaoQuandoUsuarioNaoTitular() {
            // Given
            Long mapaId = 10L;
            String usuarioId = "user1";
            AtividadeDto dto = new AtividadeDto();
            dto.setMapaCodigo(mapaId);

            Unidade unidade = UnidadeFixture.unidadePadrao();
            unidade.setTituloTitular("outro");

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(null, unidade);
            Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
            mapa.setCodigo(mapaId);
            subprocesso.setMapa(mapa);

            Usuario usuario = UsuarioFixture.usuarioComTitulo(usuarioId);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));

            // When / Then
            assertThatThrownBy(() -> atividadeService.criar(dto, usuarioId))
                    .isInstanceOf(ErroAccessoNegado.class)
                    .hasMessageContaining("autorizado")
                    .hasNoCause();
        }
    }

    @Nested
    @DisplayName("Método: atualizar")
    class Atualizar {
        @Test
        @DisplayName("Deve atualizar e retornar dto quando dados válidos")
        void deveAtualizarERetornarDtoQuandoDadosValidos() {
            // Given
            Long id = 1L;
            Long mapaId = 100L;
            AtividadeDto dto = new AtividadeDto();
            dto.setDescricao("Nova desc");

            Mapa mapa = MapaFixture.mapaPadrao(null);
            mapa.setCodigo(mapaId);

            Atividade atividade = AtividadeFixture.atividadePadrao(mapa);
            atividade.setDescricao("Velha desc");
            atividade.setCodigo(id);

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(null, null);
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
            when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade(null, "Nova desc"));
            when(atividadeRepo.save(any())).thenReturn(atividade);
            when(atividadeMapper.toDto(any())).thenReturn(dto);

            // When
            var result = atividadeService.atualizar(id, dto);

            // Then
            assertThat(result.getDescricao()).isEqualTo("Nova desc");
        }

        @Test
        @DisplayName("Deve falhar quando atividade não encontrada")
        void deveFalharQuandoAtividadeNaoEncontrada() {
            // Given
            when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> atividadeService.atualizar(1L, new AtividadeDto()))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Método: excluir")
    class Excluir {
        @Test
        @DisplayName("Deve remover atividade e conhecimentos quando existir")
        void deveRemoverAtividadeEConhecimentosQuandoExistir() {
            // Given
            Long id = 1L;
            Long mapaId = 100L;

            Mapa mapa = MapaFixture.mapaPadrao(null);
            mapa.setCodigo(mapaId);

            Atividade atividade = AtividadeFixture.atividadePadrao(mapa);
            atividade.setCodigo(id);

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(null, null);
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
            when(conhecimentoRepo.findByAtividadeCodigo(id)).thenReturn(List.of());

            // When
            atividadeService.excluir(id);

            // Then
            verify(conhecimentoRepo).deleteAll(any());
            verify(atividadeRepo).delete(atividade);
        }

        @Test
        @DisplayName("Deve falhar quando atividade não encontrada")
        void deveFalharQuandoAtividadeNaoEncontrada() {
            // Given
            when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> atividadeService.excluir(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Método: listarConhecimentos")
    class ListarConhecimentos {
        @Test
        @DisplayName("Deve retornar lista de conhecimentos quando atividade existe")
        void deveRetornarListaDeConhecimentosQuandoAtividadeExiste() {
            // Given
            Long id = 1L;
            when(atividadeRepo.existsById(id)).thenReturn(true);
            when(conhecimentoRepo.findByAtividadeCodigo(id)).thenReturn(List.of(new Conhecimento()));
            when(conhecimentoMapper.toDto(any())).thenReturn(new ConhecimentoDto());

            // When
            var result = atividadeService.listarConhecimentos(id);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve falhar quando atividade não existe")
        void deveFalharQuandoAtividadeNaoExiste() {
            // Given
            when(atividadeRepo.existsById(1L)).thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> atividadeService.listarConhecimentos(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Método: criarConhecimento")
    class CriarConhecimento {
        @Test
        @DisplayName("Deve salvar e retornar conhecimento")
        void deveSalvarERetornarConhecimento() {
            // Given
            Long id = 1L;
            Long mapaId = 100L;
            ConhecimentoDto dto = new ConhecimentoDto();

            Mapa mapa = MapaFixture.mapaPadrao(null);
            mapa.setCodigo(mapaId);

            Atividade atividade = AtividadeFixture.atividadePadrao(mapa);

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(null, null);
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
            when(conhecimentoMapper.toEntity(dto)).thenReturn(new Conhecimento());
            when(conhecimentoRepo.save(any())).thenReturn(new Conhecimento());
            when(conhecimentoMapper.toDto(any())).thenReturn(dto);

            // When
            var result = atividadeService.criarConhecimento(id, dto);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Deve falhar quando atividade não encontrada")
        void deveFalharQuandoAtividadeNaoEncontrada() {
            // Given
            when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> atividadeService.criarConhecimento(1L, new ConhecimentoDto()))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Método: atualizarConhecimento")
    class AtualizarConhecimento {
        @Test
        @DisplayName("Deve atualizar quando pertencer à atividade")
        void deveAtualizarQuandoPertencerAAtividade() {
            // Given
            Long ativId = 1L;
            Long conId = 2L;
            Long mapaId = 100L;

            ConhecimentoDto dto = new ConhecimentoDto().setDescricao("Novo");

            Mapa mapa = MapaFixture.mapaPadrao(null);
            mapa.setCodigo(mapaId);

            Atividade atividade = AtividadeFixture.atividadePadrao(mapa);
            atividade.setCodigo(ativId);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setAtividade(atividade);

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(null, null);
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));
            when(conhecimentoMapper.toEntity(dto)).thenReturn(new Conhecimento("Novo", atividade));
            when(conhecimentoRepo.save(any())).thenReturn(conhecimento);
            when(conhecimentoMapper.toDto(any())).thenReturn(dto);

            // When
            var result = atividadeService.atualizarConhecimento(ativId, conId, dto);

            // Then
            assertThat(result.getDescricao()).isEqualTo("Novo");
        }

        @Test
        @DisplayName("Deve falhar quando não pertence à atividade")
        void deveFalharQuandoNaoPertenceAAtividade() {
            // Given
            Long ativId = 1L;
            Long conId = 2L;

            Atividade atividadeOutra = AtividadeFixture.atividadePadrao(null);
            atividadeOutra.setCodigo(99L);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setAtividade(atividadeOutra);

            when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));

            // When / Then
            assertThatThrownBy(
                    () ->
                            atividadeService.atualizarConhecimento(
                                    ativId, conId, new ConhecimentoDto()))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Método: excluirConhecimento")
    class ExcluirConhecimento {
        @Test
        @DisplayName("Deve excluir quando pertencer à atividade")
        void deveExcluirQuandoPertencerAAtividade() {
            // Given
            Long ativId = 1L;
            Long conId = 2L;
            Long mapaId = 100L;

            Mapa mapa = MapaFixture.mapaPadrao(null);
            mapa.setCodigo(mapaId);

            Atividade atividade = AtividadeFixture.atividadePadrao(mapa);
            atividade.setCodigo(ativId);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setAtividade(atividade);

            Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(null, null);
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
            when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));

            // When
            atividadeService.excluirConhecimento(ativId, conId);

            // Then
            verify(conhecimentoRepo).delete(conhecimento);
        }

        @Test
        @DisplayName("Deve falhar quando não pertence à atividade")
        void deveFalharQuandoNaoPertenceAAtividade() {
            // Given
            Long ativId = 1L;
            Long conId = 2L;

            Atividade atividadeOutra = AtividadeFixture.atividadePadrao(null);
            atividadeOutra.setCodigo(99L);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setAtividade(atividadeOutra);

            when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));

            // When / Then
            assertThatThrownBy(() -> atividadeService.excluirConhecimento(ativId, conId))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }
}

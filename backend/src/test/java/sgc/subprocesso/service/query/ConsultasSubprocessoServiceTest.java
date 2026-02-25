package sgc.subprocesso.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.ConsultasSubprocessoService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ConsultasSubprocessoService.
 *
 * <p>Valida que o serviço de query funciona corretamente sem dependências circulares
 * e com queries otimizadas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultasSubprocessoService")
class ConsultasSubprocessoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private ConsultasSubprocessoService queryService;

    @Nested
    @DisplayName("verificarAcessoUnidadeAoProcesso")
    class VerificarAcessoUnidadeAoProcesso {

        @Test
        @DisplayName("deve retornar true quando unidade participa do processo")
        void deveRetornarTrueQuandoUnidadeParticipaDoProcesso() {
            // Arrange
            Long processoId = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos))
                    .thenReturn(true);

            // Act
            boolean resultado = queryService.verificarAcessoUnidadeAoProcesso(processoId, unidadeCodigos);

            // Assert
            assertThat(resultado).isTrue();
            verify(subprocessoRepo).existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos);
        }

        @Test
        @DisplayName("deve retornar false quando unidade não participa do processo")
        void deveRetornarFalseQuandoUnidadeNaoParticipaDoProcesso() {
            // Arrange
            Long processoId = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos))
                    .thenReturn(false);

            // Act
            boolean resultado = queryService.verificarAcessoUnidadeAoProcesso(processoId, unidadeCodigos);

            // Assert
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando lista de unidades está vazia")
        void deveRetornarFalseQuandoListaUnidadesVazia() {
            // Act
            boolean resultado = queryService.verificarAcessoUnidadeAoProcesso(1L, List.of());

            // Assert
            assertThat(resultado).isFalse();
            verifyNoInteractions(subprocessoRepo);
        }
    }

    @Nested
    @DisplayName("validarSubprocessosParaFinalizacao")
    class ValidarSubprocessosParaFinalizacao {

        @Test
        @DisplayName("deve retornar válido quando todos subprocessos estão homologados")
        void deveRetornarValidoQuandoTodosHomologados() {
            // Arrange
            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(3L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(
                    eq(processoId),
                    argThat(situacoes -> situacoes.contains(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                            && situacoes.contains(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO))
            )).thenReturn(3L);

            // Act
            var resultado = queryService.validarSubprocessosParaFinalizacao(processoId);

            // Assert
            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("deve retornar inválido quando nem todos subprocessos estão homologados")
        void deveRetornarInvalidoQuandoNemTodosHomologados() {
            // Arrange
            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(5L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(3L);

            // Act
            var resultado = queryService.validarSubprocessosParaFinalizacao(processoId);

            // Assert
            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("Apenas 3 de 5 subprocessos foram homologados")
                    .contains("Todos os subprocessos devem estar homologados");
        }

        @Test
        @DisplayName("deve retornar inválido quando processo não possui subprocessos")
        void deveRetornarInvalidoQuandoProcessoSemSubprocessos() {
            // Arrange
            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(0L);

            // Act
            var resultado = queryService.validarSubprocessosParaFinalizacao(processoId);

            // Assert
            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("processo não possui subprocessos para finalizar");
            verify(subprocessoRepo, never()).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
        }

        @Test
        @DisplayName("deve otimizar query usando count ao invés de carregar entidades")
        void deveOtimizarQueryUsandoCount() {
            // Arrange
            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(100L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(100L);

            // Act
            queryService.validarSubprocessosParaFinalizacao(processoId);

            // Assert
            // Verifica que apenas queries de contagem foram chamadas, não findAll
            verify(subprocessoRepo).countByProcessoCodigo(processoId);
            verify(subprocessoRepo).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
            verify(subprocessoRepo, never()).findByProcessoCodigo(anyLong());
            verify(subprocessoRepo, never()).findByProcessoCodigoWithUnidade(anyLong());
        }
    }

    @Nested
    @DisplayName("Listagem de Subprocessos")
    class ListagemSubprocessos {

        @Test
        @DisplayName("deve listar por processo e situações")
        void deveListarPorProcessoESituacoes() {
            Long processoId = 1L;
            List<SituacaoSubprocesso> situacoes = List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            queryService.listarPorProcessoESituacoes(processoId, situacoes);
            verify(subprocessoRepo).findByProcessoCodigoAndSituacaoInWithUnidade(processoId, situacoes);
        }

        @Test
        @DisplayName("deve listar entidades por processo")
        void deveListarEntidadesPorProcesso() {
            Long processoId = 1L;
            queryService.listarEntidadesPorProcesso(processoId);
            verify(subprocessoRepo).findByProcessoCodigoWithUnidade(processoId);
        }

        @Test
        @DisplayName("deve listar por processo, unidade e situações")
        void deveListarPorProcessoUnidadeESituacoes() {
            Long processoId = 1L;
            Long unidadeId = 2L;
            List<SituacaoSubprocesso> situacoes = List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            queryService.listarPorProcessoUnidadeESituacoes(processoId, unidadeId, situacoes);
            verify(subprocessoRepo).findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(processoId, unidadeId, situacoes);
        }
    }

    @Nested
    @DisplayName("ValidationResult")
    class ValidationResultTest {

        @Test
        @DisplayName("valido() deve criar resultado válido sem mensagem")
        void validoDeveCriarResultadoValidoSemMensagem() {
            // Act
            var resultado = ConsultasSubprocessoService.ValidationResult.ofValido();

            // Assert
            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("invalido() deve criar resultado inválido com mensagem")
        void invalidoDeveCriarResultadoInvalidoComMensagem() {
            // Arrange
            String mensagem = "Erro de validação";

            // Act
            var resultado = ConsultasSubprocessoService.ValidationResult.ofInvalido(mensagem);

            // Assert
            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem()).isEqualTo(mensagem);
        }
    }
}

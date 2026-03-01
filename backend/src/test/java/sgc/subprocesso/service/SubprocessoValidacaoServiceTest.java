package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoValidacaoService")
class SubprocessoValidacaoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private SubprocessoValidacaoService validacaoService;

    @Nested
    @DisplayName("verificarAcessoUnidadeAoProcesso")
    class VerificarAcessoUnidadeAoProcesso {

        @Test
        @DisplayName("deve retornar true quando unidade participa do processo")
        void deveRetornarTrueQuandoUnidadeParticipaDoProcesso() {

            Long processoId = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos))
                    .thenReturn(true);

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(processoId, unidadeCodigos);

            assertThat(resultado).isTrue();
            verify(subprocessoRepo).existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos);
        }

        @Test
        @DisplayName("deve retornar false quando unidade não participa do processo")
        void deveRetornarFalseQuandoUnidadeNaoParticipaDoProcesso() {

            Long processoId = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos))
                    .thenReturn(false);

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(processoId, unidadeCodigos);

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando lista de unidades está vazia")
        void deveRetornarFalseQuandoListaUnidadesVazia() {

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(1L, List.of());

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

            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(3L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(
                    eq(processoId),
                    argThat(situacoes -> situacoes.contains(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                            && situacoes.contains(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO))
            )).thenReturn(3L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(processoId);

            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("deve retornar inválido quando nem todos subprocessos estão homologados")
        void deveRetornarInvalidoQuandoNemTodosHomologados() {

            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(5L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(3L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(processoId);

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("Apenas 3 de 5 subprocessos foram homologados")
                    .contains("Todos os subprocessos devem estar homologados");
        }

        @Test
        @DisplayName("deve retornar inválido quando processo não possui subprocessos")
        void deveRetornarInvalidoQuandoProcessoSemSubprocessos() {

            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(0L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(processoId);

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("processo não possui subprocessos para finalizar");
            verify(subprocessoRepo, never()).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
        }

        @Test
        @DisplayName("deve otimizar query usando count ao invés de carregar entidades")
        void deveOtimizarQueryUsandoCount() {

            Long processoId = 1L;
            when(subprocessoRepo.countByProcessoCodigo(processoId)).thenReturn(100L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(100L);

            validacaoService.validarSubprocessosParaFinalizacao(processoId);

            // Verifica que apenas queries de contagem foram chamadas, não findAll
            verify(subprocessoRepo).countByProcessoCodigo(processoId);
            verify(subprocessoRepo).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
            verify(subprocessoRepo, never()).findByProcessoCodigo(anyLong());
            verify(subprocessoRepo, never()).findByProcessoCodigoWithUnidade(anyLong());
        }
    }

    @Nested
    @DisplayName("ValidationResult")
    class ValidationResultTest {

        @Test
        @DisplayName("valido() deve criar resultado válido sem mensagem")
        void validoDeveCriarResultadoValidoSemMensagem() {
            var resultado = SubprocessoValidacaoService.ValidationResult.ofValido();
            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("invalido() deve criar resultado inválido com mensagem")
        void invalidoDeveCriarResultadoInvalidoComMensagem() {
            String mensagem = "Erro de validação";
            var resultado = SubprocessoValidacaoService.ValidationResult.ofInvalido(mensagem);
            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem()).isEqualTo(mensagem);
        }
    }
}

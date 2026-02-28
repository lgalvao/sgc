package sgc.subprocesso.service.query;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
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

            Long processoId = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos))
                    .thenReturn(true);


            boolean resultado = queryService.verificarAcessoUnidadeAoProcesso(processoId, unidadeCodigos);


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


            boolean resultado = queryService.verificarAcessoUnidadeAoProcesso(processoId, unidadeCodigos);


            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando lista de unidades está vazia")
        void deveRetornarFalseQuandoListaUnidadesVazia() {

            boolean resultado = queryService.verificarAcessoUnidadeAoProcesso(1L, List.of());


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


            var resultado = queryService.validarSubprocessosParaFinalizacao(processoId);


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


            var resultado = queryService.validarSubprocessosParaFinalizacao(processoId);


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


            var resultado = queryService.validarSubprocessosParaFinalizacao(processoId);


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


            queryService.validarSubprocessosParaFinalizacao(processoId);


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

            var resultado = ConsultasSubprocessoService.ValidationResult.ofValido();


            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("invalido() deve criar resultado inválido com mensagem")
        void invalidoDeveCriarResultadoInvalidoComMensagem() {

            String mensagem = "Erro de validação";


            var resultado = ConsultasSubprocessoService.ValidationResult.ofInvalido(mensagem);


            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem()).isEqualTo(mensagem);
        }
    }
}

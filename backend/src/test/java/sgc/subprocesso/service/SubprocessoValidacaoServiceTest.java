package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoValidacaoService")
class SubprocessoValidacaoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @InjectMocks
    private SubprocessoValidacaoService validacaoService;

    @Nested
    @DisplayName("validarExistenciaAtividades")
    class ValidarExistenciaAtividades {

        @Test
        @DisplayName("deve lançar erro se mapa for nulo")
        void erroSemMapa() {
            Subprocesso sp = new Subprocesso();
            assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(sp))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.SUBPROCESSO_SEM_MAPA);
        }
    }

    @Nested
    @DisplayName("validarCadastro")
    class ValidarCadastro {

        @Test
        @DisplayName("deve retornar inválido se não tiver mapa")
        void invalidoSemMapa() {
            Subprocesso sp = new Subprocesso();
            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).hasSize(1);
            assertThat(dto.erros().getFirst().tipo()).isEqualTo("SEM_MAPA");
        }

        @Test
        @DisplayName("deve retornar inválido se mapa não tiver atividades")
        void invalidoSemAtividades() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).hasSize(1);
            assertThat(dto.erros().getFirst().tipo()).isEqualTo("SEM_ATIVIDADES");
        }

        @Test
        @DisplayName("deve retornar inválido se atividade não tiver conhecimentos")
        void invalidoAtividadeSemConhecimento() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a = new Atividade();
            a.setCodigo(10L);
            a.setDescricao("A1");
            a.setConhecimentos(Set.of());

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isFalse();
            assertThat(dto.erros()).hasSize(1);
            assertThat(dto.erros().getFirst().tipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
        }

        @Test
        @DisplayName("deve retornar válido se tudo estiver correto")
        void validoTudoCorreto() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            Atividade a = new Atividade();
            a.setCodigo(10L);
            a.setDescricao("A1");
            Conhecimento c = new Conhecimento();
            c.setDescricao("C1");
            a.setConhecimentos(Set.of(c));

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

            ValidacaoCadastroDto dto = validacaoService.validarCadastro(sp);
            assertThat(dto.valido()).isTrue();
            assertThat(dto.erros()).isEmpty();
        }
    }

    @Nested
    @DisplayName("validarSituacaoPermitida")
    class ValidarSituacaoPermitida {

        @Test
        @DisplayName("deve lançar erro se situacao for nula")
        void erroSituacaoNula() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(null); // Explicitly ensuring null
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, Set.of(SituacaoSubprocesso.NAO_INICIADO)))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar erro se permitidas for vazio")
        void erroPermitidasVazia() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar erro de validacao se situacao nao permitida")
        void erroSituacaoNaoPermitida() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)))
                .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("não deve lançar erro se situacao permitida")
        void sucessoSituacaoPermitida() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            validacaoService.validarSituacaoPermitida(sp, Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
        }

        @Test
        @DisplayName("deve lançar erro se varargs vazio")
        void erroVarargsVazio() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve usar a mensagem fornecida no erro")
        void usaMensagemFornecida() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            assertThatThrownBy(() -> validacaoService.validarSituacaoPermitida(sp, "Msg custom", SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Msg custom");
        }
    }

    @Nested
    @DisplayName("verificarAcessoUnidadeAoProcesso")
    class VerificarAcessoUnidadeAoProcesso {

        @Test
        @DisplayName("deve retornar true quando unidade participa do processo")
        void deveRetornarTrueQuandoUnidadeParticipaDoProcesso() {

            Long codProcesso = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadeCodigos))
                    .thenReturn(true);

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(codProcesso, unidadeCodigos);

            assertThat(resultado).isTrue();
            verify(subprocessoRepo).existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadeCodigos);
        }

        @Test
        @DisplayName("deve retornar false quando unidade não participa do processo")
        void deveRetornarFalseQuandoUnidadeNaoParticipaDoProcesso() {

            Long codProcesso = 1L;
            List<Long> unidadeCodigos = List.of(10L, 20L);
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadeCodigos))
                    .thenReturn(false);

            boolean resultado = validacaoService.verificarAcessoUnidadeAoProcesso(codProcesso, unidadeCodigos);

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

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(3L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(
                    eq(codProcesso),
                    argThat(situacoes -> situacoes.contains(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                            && situacoes.contains(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO))
            )).thenReturn(3L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            assertThat(resultado.valido()).isTrue();
            assertThat(resultado.mensagem()).isNull();
        }

        @Test
        @DisplayName("deve retornar inválido quando nem todos subprocessos estão homologados")
        void deveRetornarInvalidoQuandoNemTodosHomologados() {

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(5L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(3L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("Apenas 3 de 5 subprocessos foram homologados")
                    .contains("Todos os subprocessos devem estar homologados");
        }

        @Test
        @DisplayName("deve retornar inválido quando processo não possui subprocessos")
        void deveRetornarInvalidoQuandoProcessoSemSubprocessos() {

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(0L);

            var resultado = validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            assertThat(resultado.valido()).isFalse();
            assertThat(resultado.mensagem())
                    .contains("processo não possui subprocessos para finalizar");
            verify(subprocessoRepo, never()).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
        }

        @Test
        @DisplayName("deve otimizar query usando count ao invés de carregar entidades")
        void deveOtimizarQueryUsandoCount() {

            Long codProcesso = 1L;
            when(subprocessoRepo.countByProcessoCodigo(codProcesso)).thenReturn(100L);
            when(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(anyLong(), anyList()))
                    .thenReturn(100L);

            validacaoService.validarSubprocessosParaFinalizacao(codProcesso);

            // Verifica que apenas queries de contagem foram chamadas, não findAll
            verify(subprocessoRepo).countByProcessoCodigo(codProcesso);
            verify(subprocessoRepo).countByProcessoCodigoAndSituacaoIn(anyLong(), anyList());
            verify(subprocessoRepo, never()).findByProcessoCodigoComUnidade(anyLong());
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

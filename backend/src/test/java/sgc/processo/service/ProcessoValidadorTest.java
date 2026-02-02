package sgc.processo.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.service.query.ProcessoSubprocessoQueryService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoValidador - Testes Unitários")
class ProcessoValidadorTest {

    @Mock
    private UnidadeFacade unidadeService;

    @Mock
    private ProcessoSubprocessoQueryService queryService;

    @InjectMocks
    private ProcessoValidador validador;

    @Test
    @DisplayName("getMensagemErroUnidadesSemMapa deve retornar vazio se lista nula ou vazia")
    void getMensagemErroUnidadesSemMapaListaVazia() {
        assertThat(validador.getMensagemErroUnidadesSemMapa(null)).isEmpty();
        assertThat(validador.getMensagemErroUnidadesSemMapa(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("getMensagemErroUnidadesSemMapa deve retornar erro se unidade sem mapa")
    void getMensagemErroUnidadesSemMapaComErro() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeService.buscarEntidadesPorIds(List.of(1L))).thenReturn(List.of(u));
        when(unidadeService.verificarMapaVigente(1L)).thenReturn(false);
        when(unidadeService.buscarSiglasPorIds(List.of(1L))).thenReturn(List.of("SIGLA"));

        Optional<String> msg = validador.getMensagemErroUnidadesSemMapa(List.of(1L));
        assertThat(msg).isPresent();
        assertThat(msg.get()).contains("SIGLA");
    }

    @Test
    @DisplayName("validarFinalizacaoProcesso falha se situação inválida")
    void validarFinalizacaoProcessoSituacaoInvalida() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);

        assertThatThrownBy(() -> validador.validarFinalizacaoProcesso(p))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("EM ANDAMENTO");
    }

    @Test
    @DisplayName("validarTodosSubprocessosHomologados sucesso")
    void validarTodosSubprocessosHomologadosSucesso() {
        Processo p = new Processo();
        p.setCodigo(1L);

        when(queryService.validarSubprocessosParaFinalizacao(1L))
                .thenReturn(ProcessoSubprocessoQueryService.ValidationResult.ofValido());

        // Should not throw exception
        Assertions.assertDoesNotThrow(() -> validador.validarTodosSubprocessosHomologados(p));
    }
}

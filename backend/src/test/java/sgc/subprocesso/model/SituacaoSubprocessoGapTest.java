package sgc.subprocesso.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.processo.model.TipoProcesso;

import static org.assertj.core.api.Assertions.assertThat;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@DisplayName("SituacaoSubprocesso Branch Coverage Tests")
class SituacaoSubprocessoGapTest {

    @Test
    @DisplayName("Deve cobrir todas as branches de podeIniciar (Linha 69)")
    void deveCobrirPodeIniciar() {
        // MAPEAMENTO
        assertThat(invocarPodeIniciar(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, TipoProcesso.MAPEAMENTO)).isTrue();
        assertThat(invocarPodeIniciar(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO, TipoProcesso.MAPEAMENTO)).isFalse();

        // REVISAO
        assertThat(invocarPodeIniciar(REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO, TipoProcesso.REVISAO)).isTrue();
        assertThat(invocarPodeIniciar(REVISAO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, TipoProcesso.REVISAO)).isFalse();

        // DIAGNOSTICO
        assertThat(invocarPodeIniciar(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, TipoProcesso.DIAGNOSTICO)).isTrue();
        assertThat(invocarPodeIniciar(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO, TipoProcesso.DIAGNOSTICO)).isFalse();

        // Tipo nulo
        assertThat(invocarPodeIniciar(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, null)).isFalse();
    }

    private boolean invocarPodeIniciar(SituacaoSubprocesso target, SituacaoSubprocesso nova, TipoProcesso tipo) {
        return (boolean) ReflectionTestUtils.invokeMethod(target, "podeIniciar", nova, tipo);
    }
}

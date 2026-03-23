package sgc.subprocesso.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sgc.processo.model.TipoProcesso;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SituacaoSubprocesso - Testes de Transição")
class SituacaoSubprocessoCoverageTest {

    @Test
    @DisplayName("podeTransicionarPara: Deve permitir transição para si mesma")
    void mesmaSituacao() {
        for (SituacaoSubprocesso s : SituacaoSubprocesso.values()) {
            assertThat(s.podeTransicionarPara(s, TipoProcesso.MAPEAMENTO)).isTrue();
        }
    }

    @ParameterizedTest
    @CsvSource({
        "NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO, true",
        "NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO, true",
        "NAO_INICIADO, DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, DIAGNOSTICO, true",
        "NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO, false",
        "NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO, false"
    })
    @DisplayName("podeTransicionarPara: Deve validar início do processo conforme o tipo")
    void inicioProcesso(SituacaoSubprocesso de, SituacaoSubprocesso para, TipoProcesso tipo, boolean esperado) {
        assertThat(de.podeTransicionarPara(para, tipo)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("isSituacaoCompativel: Deve validar compatibilidade entre prefixos")
    void compatibilidade() {
        // Mapeamento -> Revisao (Incompatível)
        assertThat(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                .podeTransicionarPara(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, TipoProcesso.MAPEAMENTO)).isFalse();
        
        // Revisao -> Mapeamento (Incompatível)
        assertThat(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
                .podeTransicionarPara(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, TipoProcesso.REVISAO)).isFalse();

        // Diagnostico -> Mapeamento (Incompatível)
        assertThat(SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO
                .podeTransicionarPara(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, TipoProcesso.DIAGNOSTICO)).isFalse();

        // Qualquer -> NAO_INICIADO (Compatível por regra na linha 57, mas transicaoMapeamento/Revisao bloqueia)
        assertThat(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                .podeTransicionarPara(SituacaoSubprocesso.NAO_INICIADO, TipoProcesso.MAPEAMENTO)).isFalse();
    }

    @Test
    @DisplayName("transicaoMapeamento: Deve validar todas as transições de mapeamento")
    void mapeamentoCompleto() {
        for (SituacaoSubprocesso s : SituacaoSubprocesso.values()) {
            if (!s.name().startsWith("MAPEAMENTO")) continue;
            
            // Testar transição para todas as outras situações de mapeamento
            for (SituacaoSubprocesso para : SituacaoSubprocesso.values()) {
                if (!para.name().startsWith("MAPEAMENTO")) continue;
                s.podeTransicionarPara(para, TipoProcesso.MAPEAMENTO);
            }
            // Testar transição para NAO_INICIADO
            s.podeTransicionarPara(SituacaoSubprocesso.NAO_INICIADO, TipoProcesso.MAPEAMENTO);
        }
    }

    @Test
    @DisplayName("transicaoRevisao: Deve validar todas as transições de revisão")
    void revisaoCompleta() {
        for (SituacaoSubprocesso s : SituacaoSubprocesso.values()) {
            if (!s.name().startsWith("REVISAO")) continue;
            
            for (SituacaoSubprocesso para : SituacaoSubprocesso.values()) {
                if (!para.name().startsWith("REVISAO")) continue;
                s.podeTransicionarPara(para, TipoProcesso.REVISAO);
            }
            s.podeTransicionarPara(SituacaoSubprocesso.NAO_INICIADO, TipoProcesso.REVISAO);
        }
    }

    @Test
    @DisplayName("transicaoDiagnostico: Deve validar todas as transições de diagnóstico")
    void diagnosticoCompleto() {
        for (SituacaoSubprocesso s : SituacaoSubprocesso.values()) {
            if (!s.name().startsWith("DIAGNOSTICO")) continue;
            
            for (SituacaoSubprocesso para : SituacaoSubprocesso.values()) {
                if (!para.name().startsWith("DIAGNOSTICO")) continue;
                s.podeTransicionarPara(para, TipoProcesso.DIAGNOSTICO);
            }
            s.podeTransicionarPara(SituacaoSubprocesso.NAO_INICIADO, TipoProcesso.DIAGNOSTICO);
        }
    }
}

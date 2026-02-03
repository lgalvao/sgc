package sgc.subprocesso.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@DisplayName("SituacaoSubprocesso - Teste de Transições")
class SituacaoSubprocessoTest {

    @ParameterizedTest(name = "De {0} para {1} (Tipo {3}) deve ser {2}")
    @CsvSource({
        "NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, true, MAPEAMENTO",
        "NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO, true, REVISAO",
        "NAO_INICIADO, DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, true, DIAGNOSTICO",
        "MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, true, MAPEAMENTO",
        "MAPEAMENTO_CADASTRO_DISPONIBILIZADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, true, MAPEAMENTO",
        "MAPEAMENTO_CADASTRO_DISPONIBILIZADO, MAPEAMENTO_CADASTRO_HOMOLOGADO, true, MAPEAMENTO",
        "MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, true, MAPEAMENTO",
        "MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_DISPONIBILIZADO, true, MAPEAMENTO",
        "MAPEAMENTO_MAPA_DISPONIBILIZADO, MAPEAMENTO_MAPA_COM_SUGESTOES, true, MAPEAMENTO",
        "MAPEAMENTO_MAPA_DISPONIBILIZADO, MAPEAMENTO_MAPA_VALIDADO, true, MAPEAMENTO",
        "MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_DISPONIBILIZADO, true, MAPEAMENTO",
        "MAPEAMENTO_MAPA_VALIDADO, MAPEAMENTO_MAPA_HOMOLOGADO, true, MAPEAMENTO",
        
        // Transições Inválidas (Misturando tipos)
        "MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO, false, MAPEAMENTO",
        "NAO_INICIADO, MAPEAMENTO_MAPA_HOMOLOGADO, false, MAPEAMENTO",
        "MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_MAPA_CRIADO, false, MAPEAMENTO",
        "MAPEAMENTO_MAPA_HOMOLOGADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, false, MAPEAMENTO"
    })
    void testTransicoes(SituacaoSubprocesso de, SituacaoSubprocesso para, boolean esperado, sgc.processo.model.TipoProcesso tipo) {
        assertThat(de.podeTransicionarPara(para, tipo)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Deve permitir transição para si mesmo")
    void testMesmaSituacao() {
        assertThat(NAO_INICIADO.podeTransicionarPara(NAO_INICIADO, sgc.processo.model.TipoProcesso.MAPEAMENTO)).isTrue();
    }
}

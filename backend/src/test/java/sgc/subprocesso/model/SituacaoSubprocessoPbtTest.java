package sgc.subprocesso.model;

import net.jqwik.api.*;
import sgc.processo.model.*;

import static org.assertj.core.api.Assertions.*;

@Tag("PBT")
class SituacaoSubprocessoPbtTest {

    @Property
    void reflexividade(@ForAll SituacaoSubprocesso situacao, @ForAll TipoProcesso tipo) {
        assertThat(situacao.podeTransicionarPara(situacao, tipo))
                .as("Situação %s deve poder transicionar para ela mesma no tipo %s", situacao, tipo)
                .isTrue();
    }

    @Property
    void imutabilidadeTipo(@ForAll SituacaoSubprocesso s1,
                           @ForAll SituacaoSubprocesso s2,
                           @ForAll TipoProcesso tipo) {
        // Se a transição é permitida para um tipo, elas devem ser compatíveis
        if (s1.podeTransicionarPara(s2, tipo) && s1 != s2 && s2 != SituacaoSubprocesso.NAO_INICIADO) {
            String prefixoS1 = getPrefix(s1);
            String prefixoS2 = getPrefix(s2);

            if (prefixoS1 != null && prefixoS2 != null) {
                assertThat(prefixoS1)
                        .as("Transição proibida entre tipos diferentes: %s -> %s", s1, s2)
                        .isEqualTo(prefixoS2);
            }
        }
    }

    @Property
    void transicaoIniciaApenasNoTipoCorreto(@ForAll SituacaoSubprocesso s, @ForAll TipoProcesso tipo) {
        if (SituacaoSubprocesso.NAO_INICIADO.podeTransicionarPara(s, tipo) && s != SituacaoSubprocesso.NAO_INICIADO) {
            if (tipo == TipoProcesso.MAPEAMENTO) {
                assertThat(s).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            } else if (tipo == TipoProcesso.REVISAO) {
                assertThat(s).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            } else if (tipo == TipoProcesso.DIAGNOSTICO) {
                assertThat(s).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
            }
        }
    }

    private String getPrefix(SituacaoSubprocesso s) {
        if (s == SituacaoSubprocesso.NAO_INICIADO) return null;
        if (s.name().startsWith("MAPEAMENTO")) return "MAPEAMENTO";
        if (s.name().startsWith("REVISAO")) return "REVISAO";
        if (s.name().startsWith("DIAGNOSTICO")) return "DIAGNOSTICO";
        return null;
    }
}

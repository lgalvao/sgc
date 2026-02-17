package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auditoria do Fluxo de Mapeamento")
class AuditoriaFluxoMapeamentoTest {

    @Test
    @DisplayName("Deve verificar se as situações do enum SituacaoSubprocesso correspondem ao _intro.md")
    void verificarEnums() {
        // Situações de Mapeamento listadas em _intro.md
        assertThat(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.getDescricao()).isEqualTo("Cadastro em andamento");
        assertThat(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO.getDescricao()).isEqualTo("Cadastro disponibilizado");
        assertThat(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO.getDescricao()).isEqualTo("Cadastro homologado");
        assertThat(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO.getDescricao()).isEqualTo("Mapa criado");
        assertThat(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO.getDescricao()).isEqualTo("Mapa disponibilizado");
        assertThat(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES.getDescricao()).isEqualTo("Mapa com sugestões");
        assertThat(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO.getDescricao()).isEqualTo("Mapa validado");
        assertThat(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO.getDescricao()).isEqualTo("Mapa homologado");
    }

    @Test
    @DisplayName("Deve permitir sequência completa de transições de mapeamento")
    void fluxoFelizMapeamento() {
        SituacaoSubprocesso s = SituacaoSubprocesso.NAO_INICIADO;

        // Cadastro
        s = transicionar(s, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        s = transicionar(s, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        s = transicionar(s, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        // Mapa
        s = transicionar(s, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        s = transicionar(s, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        s = transicionar(s, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        s = transicionar(s, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        assertThat(s).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }

    private SituacaoSubprocesso transicionar(SituacaoSubprocesso atual, SituacaoSubprocesso nova) {
        assertThat(atual.podeTransicionarPara(nova, TipoProcesso.MAPEAMENTO))
                .withFailMessage("Transição inválida: " + atual + " -> " + nova)
                .isTrue();
        return nova;
    }
}

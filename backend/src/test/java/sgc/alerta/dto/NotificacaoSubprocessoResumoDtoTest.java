package sgc.alerta.dto;

import org.junit.jupiter.api.Test;
import sgc.alerta.dto.SituacaoNotificacao;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoSubprocessoResumoDtoTest {

    @Test
    void fromQuery_deveCalcularStatusCorretamente_FalhaDefinitiva() {
        NotificacaoSubprocessoResumoQuery query = mockQuery(10, 0, 0, 5, 2, 3);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);

        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.FALHA_DEFINITIVA);
        assertThat(dto.podeReenviar()).isTrue();
    }

    @Test
    void fromQuery_deveCalcularStatusCorretamente_FalhaTemporaria() {
        NotificacaoSubprocessoResumoQuery query = mockQuery(10, 0, 0, 5, 5, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);

        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.FALHA_TEMPORARIA);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void fromQuery_deveCalcularStatusCorretamente_Pendente() {
        NotificacaoSubprocessoResumoQuery query = mockQuery(10, 5, 0, 5, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);

        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.PENDENTE);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void fromQuery_deveCalcularStatusCorretamente_Enviando() {
        NotificacaoSubprocessoResumoQuery query = mockQuery(10, 0, 5, 5, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);

        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.PENDENTE);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void fromQuery_deveCalcularStatusCorretamente_Inconsistente() {
        NotificacaoSubprocessoResumoQuery query = mockQuery(0, 0, 0, 0, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);

        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.INCONSISTENTE);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void fromQuery_deveCalcularStatusCorretamente_Ok() {
        NotificacaoSubprocessoResumoQuery query = mockQuery(10, 0, 0, 10, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);

        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.OK);
        assertThat(dto.podeReenviar()).isFalse();
    }

    private NotificacaoSubprocessoResumoQuery mockQuery(long total, long pendentes, long enviando, long enviadas, long falhasTemporarias, long falhasDefinitivas) {
        LocalDateTime agora = LocalDateTime.now();
        return new NotificacaoSubprocessoResumoQuery(
                1L,
                2L,
                "Processo Teste",
                "SGL",
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                total,
                pendentes,
                enviando,
                enviadas,
                falhasTemporarias,
                falhasDefinitivas,
                agora,
                agora.plusHours(1),
                3,
                "Erro"
        );
    }
}

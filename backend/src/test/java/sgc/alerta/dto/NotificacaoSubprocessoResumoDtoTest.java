package sgc.alerta.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificacaoSubprocessoResumoDto - Cobertura de Testes")
class NotificacaoSubprocessoResumoDtoTest {

    private NotificacaoSubprocessoResumoQuery createQuery(
            long total, long pendentes, long enviando, long falhasTemporarias, long falhasDefinitivas) {
        return new NotificacaoSubprocessoResumoQuery(
                1L, 2L, "Proc Teste", "U10", SituacaoSubprocesso.NAO_INICIADO,
                total, pendentes, enviando, 0L, falhasTemporarias, falhasDefinitivas,
                LocalDateTime.now(), LocalDateTime.now(), 0, "Erro"
        );
    }

    @Test
    @DisplayName("calcularStatus deve retornar FALHA_DEFINITIVA quando falhasDefinitivas > 0")
    void deveRetornarFalhaDefinitiva() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 0, 0, 1);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.FALHA_DEFINITIVA);
        assertThat(dto.podeReenviar()).isTrue();
    }

    @Test
    @DisplayName("calcularStatus deve retornar FALHA_TEMPORARIA quando falhasTemporarias > 0")
    void deveRetornarFalhaTemporaria() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 0, 1, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.FALHA_TEMPORARIA);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    @DisplayName("calcularStatus deve retornar PENDENTE quando pendentes > 0")
    void deveRetornarPendentePorPendentes() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 1, 0, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.PENDENTE);
    }

    @Test
    @DisplayName("calcularStatus deve retornar PENDENTE quando enviando > 0")
    void deveRetornarPendentePorEnviando() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 1, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.PENDENTE);
    }

    @Test
    @DisplayName("calcularStatus deve retornar INCONSISTENTE quando total == 0")
    void deveRetornarInconsistente() {
        NotificacaoSubprocessoResumoQuery query = createQuery(0, 0, 0, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.INCONSISTENTE);
    }

    @Test
    @DisplayName("calcularStatus deve retornar OK nos demais casos")
    void deveRetornarOk() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 0, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.OK);
    }
}

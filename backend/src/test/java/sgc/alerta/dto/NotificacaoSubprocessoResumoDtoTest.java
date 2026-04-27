package sgc.alerta.dto;

import org.junit.jupiter.api.Test;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoSubprocessoResumoDtoTest {

    @Test
    void shouldCreateDtoWithFalhaDefinitiva() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 0, 0, 0, 1);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.FALHA_DEFINITIVA);
        assertThat(dto.podeReenviar()).isTrue();
    }

    @Test
    void shouldCreateDtoWithFalhaTemporaria() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 0, 0, 1, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.FALHA_TEMPORARIA);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void shouldCreateDtoWithPendenteFromPendentes() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 1, 0, 0, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.PENDENTE);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void shouldCreateDtoWithPendenteFromEnviando() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 1, 0, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.PENDENTE);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void shouldCreateDtoWithInconsistente() {
        NotificacaoSubprocessoResumoQuery query = createQuery(0, 0, 0, 0, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.INCONSISTENTE);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void shouldCreateDtoWithOk() {
        NotificacaoSubprocessoResumoQuery query = createQuery(10, 0, 0, 10, 0, 0);
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);
        assertThat(dto.statusGeral()).isEqualTo(SituacaoNotificacao.OK);
        assertThat(dto.podeReenviar()).isFalse();
    }

    @Test
    void shouldMapAllFields() {
        LocalDateTime ultima = LocalDateTime.now().minusHours(1);
        LocalDateTime proxima = LocalDateTime.now().plusHours(1);
        NotificacaoSubprocessoResumoQuery query = new NotificacaoSubprocessoResumoQuery(
                1L, 2L, "Processo", "SIGLA", SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                10, 1, 2, 3, 4, 0, ultima, proxima, 5, "Erro"
        );
        NotificacaoSubprocessoResumoDto dto = NotificacaoSubprocessoResumoDto.fromQuery(query);

        assertThat(dto.subprocessoCodigo()).isEqualTo(1L);
        assertThat(dto.processoCodigo()).isEqualTo(2L);
        assertThat(dto.processoDescricao()).isEqualTo("Processo");
        assertThat(dto.unidadeSigla()).isEqualTo("SIGLA");
        assertThat(dto.situacaoSubprocesso()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        assertThat(dto.totalNotificacoes()).isEqualTo(10);
        assertThat(dto.pendentes()).isEqualTo(1);
        assertThat(dto.enviando()).isEqualTo(2);
        assertThat(dto.enviadas()).isEqualTo(3);
        assertThat(dto.falhasTemporarias()).isEqualTo(4);
        assertThat(dto.falhasDefinitivas()).isEqualTo(0);
        assertThat(dto.ultimaNotificacaoEm()).isEqualTo(ultima);
        assertThat(dto.proximaTentativaEm()).isEqualTo(proxima);
        assertThat(dto.maiorTentativas()).isEqualTo(5);
        assertThat(dto.ultimoErro()).isEqualTo("Erro");
    }

    private NotificacaoSubprocessoResumoQuery createQuery(
            long total, long pendentes, long enviando, long enviadas, long falhasTemporarias, long falhasDefinitivas) {
        return new NotificacaoSubprocessoResumoQuery(
                1L, 1L, "Processo 1", "UN", SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                total, pendentes, enviando, enviadas, falhasTemporarias, falhasDefinitivas,
                LocalDateTime.now(), LocalDateTime.now(), 0, null
        );
    }
}

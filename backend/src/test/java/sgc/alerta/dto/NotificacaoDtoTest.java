package sgc.alerta.dto;

import org.junit.jupiter.api.Test;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.alerta.model.TipoNotificacao;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificacaoDtoTest {

    @Test
    void fromEntity_deveMapearCorretamente() {
        LocalDateTime dataHoraCriacao = LocalDateTime.now().minusDays(1);
        LocalDateTime dataHoraEnvio = LocalDateTime.now();
        LocalDateTime proximaTentativaEm = LocalDateTime.now().plusHours(1);

        NotificacaoEmail notificacaoMock = mock(NotificacaoEmail.class);
        when(notificacaoMock.getCodigo()).thenReturn(1L);
        when(notificacaoMock.getTipoNotificacao()).thenReturn(TipoNotificacao.PROCESSO_INICIADO);
        when(notificacaoMock.getUsuarioDestinoTitulo()).thenReturn("titular@tse.jus.br");
        when(notificacaoMock.getDestinatario()).thenReturn("destinatario@tse.jus.br");
        when(notificacaoMock.getAssunto()).thenReturn("Assunto Teste");
        when(notificacaoMock.getSituacao()).thenReturn(SituacaoNotificacao.ENVIADO);
        when(notificacaoMock.getTentativas()).thenReturn(2);
        when(notificacaoMock.getDataHoraCriacao()).thenReturn(dataHoraCriacao);
        when(notificacaoMock.getDataHoraEnvio()).thenReturn(dataHoraEnvio);
        when(notificacaoMock.getProximaTentativaEm()).thenReturn(proximaTentativaEm);
        when(notificacaoMock.getUltimoErro()).thenReturn("Nenhum erro");

        Long subprocessoCodigo = 123L;

        NotificacaoDto dto = NotificacaoDto.fromEntity(notificacaoMock, subprocessoCodigo);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.subprocessoCodigo()).isEqualTo(subprocessoCodigo);
        assertThat(dto.tipoNotificacao()).isEqualTo(TipoNotificacao.PROCESSO_INICIADO);
        assertThat(dto.usuarioDestinoTitulo()).isEqualTo("titular@tse.jus.br");
        assertThat(dto.destinatario()).isEqualTo("destinatario@tse.jus.br");
        assertThat(dto.assunto()).isEqualTo("Assunto Teste");
        assertThat(dto.situacao()).isEqualTo(SituacaoNotificacao.ENVIADO);
        assertThat(dto.tentativas()).isEqualTo(2);
        assertThat(dto.dataHoraCriacao()).isEqualTo(dataHoraCriacao);
        assertThat(dto.dataHoraEnvio()).isEqualTo(dataHoraEnvio);
        assertThat(dto.proximaTentativaEm()).isEqualTo(proximaTentativaEm);
        assertThat(dto.ultimoErro()).isEqualTo("Nenhum erro");
    }
}

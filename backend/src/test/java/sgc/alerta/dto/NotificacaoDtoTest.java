package sgc.alerta.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.alerta.model.TipoNotificacao;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificacaoDto - Cobertura de Testes")
class NotificacaoDtoTest {

    @Test
    @DisplayName("fromEntity deve mapear todos os campos corretamente")
    void fromEntity() {
        LocalDateTime agora = LocalDateTime.now();
        NotificacaoEmail email = new NotificacaoEmail();
        email.setCodigo(1L);
        email.setTipoNotificacao(TipoNotificacao.CADASTRO_DISPONIBILIZADO);
        email.setUsuarioDestinoTitulo("123");
        email.setDestinatario("teste@teste.com");
        email.setAssunto("Assunto Teste");
        email.setSituacao(SituacaoNotificacao.PENDENTE);
        email.setTentativas(2);
        email.setDataHoraCriacao(agora);
        email.setDataHoraEnvio(agora.plusMinutes(10));
        email.setProximaTentativaEm(agora.plusMinutes(20));
        email.setUltimoErro("Erro X");

        NotificacaoDto dto = NotificacaoDto.fromEntity(email, 100L);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.subprocessoCodigo()).isEqualTo(100L);
        assertThat(dto.tipoNotificacao()).isEqualTo(TipoNotificacao.CADASTRO_DISPONIBILIZADO);
        assertThat(dto.usuarioDestinoTitulo()).isEqualTo("123");
        assertThat(dto.destinatario()).isEqualTo("teste@teste.com");
        assertThat(dto.assunto()).isEqualTo("Assunto Teste");
        assertThat(dto.situacao()).isEqualTo(SituacaoNotificacao.PENDENTE);
        assertThat(dto.tentativas()).isEqualTo(2);
        assertThat(dto.dataHoraCriacao()).isEqualTo(agora);
        assertThat(dto.dataHoraEnvio()).isEqualTo(agora.plusMinutes(10));
        assertThat(dto.proximaTentativaEm()).isEqualTo(agora.plusMinutes(20));
        assertThat(dto.ultimoErro()).isEqualTo("Erro X");
    }
}

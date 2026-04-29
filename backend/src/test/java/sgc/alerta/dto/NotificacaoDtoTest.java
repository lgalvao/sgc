package sgc.alerta.dto;

import org.junit.jupiter.api.*;
import sgc.alerta.model.*;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

class NotificacaoDtoTest {

    @Test
    void shouldCreateNotificacaoDtoFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Unidade unidade = Unidade.builder().sigla("SIGLA").build();
        Processo processo = Processo.builder().descricao("Descricao Processo").build();
        Subprocesso subprocesso = Subprocesso.builder()
                .codigo(123L)
                .unidade(unidade)
                .processo(processo)
                .build();

        NotificacaoEmail entity = NotificacaoEmail.builder()
                .codigo(1L)
                .subprocesso(subprocesso)
                .unidadeDestinoSigla("SIGLA")
                .tipoNotificacao(TipoNotificacao.MAPA_DISPONIBILIZADO)
                .usuarioDestinoTitulo("123456789012")
                .destinatario("test@example.com")
                .assunto("Test Subject")
                .corpoHtml("<p>corpo</p>")
                .situacao(SituacaoNotificacao.ENVIADO)
                .tentativas(1)
                .dataHoraCriacao(now.minusDays(1))
                .dataHoraEnvio(now)
                .proximaTentativaEm(null)
                .ultimoErro(null)
                .build();

        // When
        NotificacaoDto dto = NotificacaoDto.fromEntity(entity);

        // Then
        assertThat(dto.codigo()).isEqualTo(entity.getCodigo());
        assertThat(dto.subprocessoCodigo()).isEqualTo(123L);
        assertThat(dto.unidadeSigla()).isEqualTo("SIGLA");
        assertThat(dto.processoDescricao()).isEqualTo("Descricao Processo");
        assertThat(dto.tipoNotificacao()).isEqualTo(entity.getTipoNotificacao());
        assertThat(dto.usuarioDestinoTitulo()).isEqualTo(entity.getUsuarioDestinoTitulo());
        assertThat(dto.destinatario()).isEqualTo(entity.getDestinatario());
        assertThat(dto.assunto()).isEqualTo(entity.getAssunto());
        assertThat(dto.corpoHtml()).isEqualTo("<p>corpo</p>");
        assertThat(dto.situacao()).isEqualTo(entity.getSituacao());
        assertThat(dto.tentativas()).isEqualTo(entity.getTentativas());
        assertThat(dto.dataHoraCriacao()).isEqualTo(entity.getDataHoraCriacao());
        assertThat(dto.dataHoraEnvio()).isEqualTo(entity.getDataHoraEnvio());
        assertThat(dto.proximaTentativaEm()).isNull();
        assertThat(dto.ultimoErro()).isNull();
    }
}

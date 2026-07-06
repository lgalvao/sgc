package sgc.alerta.dto;

import org.junit.jupiter.api.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

class NotificacaoDtoTest {

    private final AlertaDtoMapper mapper = new AlertaDtoMapper();

    @Test
    void shouldCreateNotificacaoDtoFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Unidade unidade = Unidade.builder().sigla("SIGLA").build();
        Processo processo = Processo.builder()
                .descricao("Descricao Processo")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .build();
        Subprocesso subprocesso = Subprocesso.builder()
                .codigo(123L)
                .unidade(unidade)
                .processo(processo)
                .build();

        NotificacaoEmail entity = NotificacaoEmail.builder()
                .codigo(1L)
                .subprocesso(subprocesso)
                .unidadeDestinoSigla("SIGLA")
                .unidadeOrigemSigla("ORIGEM")
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
        NotificacaoDto dto = mapper.paraNotificacaoDto(entity);

        // Then
        assertThat(dto.codigo()).isEqualTo(entity.getCodigo());
        assertThat(dto.subprocessoCodigo()).isEqualTo(123L);
        assertThat(dto.unidadeSigla()).isEqualTo("SIGLA");
        assertThat(dto.unidadeOrigemSigla()).isEqualTo("ORIGEM");
        assertThat(dto.processoDescricao()).isEqualTo("Descricao Processo");
        assertThat(dto.processoFinalizado()).isFalse();
        assertThat(dto.tipoNotificacao()).isEqualTo(TipoNotificacaoDto.MAPA_DISPONIBILIZADO);
        assertThat(dto.notificacaoFinalizacaoProcesso()).isFalse();
        assertThat(dto.usuarioDestinoTitulo()).isEqualTo(entity.getUsuarioDestinoTitulo());
        assertThat(dto.destinatario()).isEqualTo(entity.getDestinatario());
        assertThat(dto.assunto()).isEqualTo(entity.getAssunto());
        assertThat(dto.corpoHtml()).isEqualTo("<p>corpo</p>");
        assertThat(dto.situacao()).isEqualTo(SituacaoNotificacaoEmailDto.ENVIADO);
        assertThat(dto.tentativas()).isEqualTo(entity.getTentativas());
        assertThat(dto.dataHoraCriacao()).isEqualTo(entity.getDataHoraCriacao());
        assertThat(dto.dataHoraEnvio()).isEqualTo(entity.getDataHoraEnvio());
        assertThat(dto.proximaTentativaEm()).isNull();
        assertThat(dto.ultimoErro()).isNull();
    }

    @Test
    void shouldFlagFinalizedProcessNotification() {
        Unidade unidade = Unidade.builder().sigla("SIGLA").build();
        Processo processo = Processo.builder()
                .descricao("Descricao Processo")
                .situacao(SituacaoProcesso.FINALIZADO)
                .build();
        Subprocesso subprocesso = Subprocesso.builder()
                .codigo(123L)
                .unidade(unidade)
                .processo(processo)
                .build();

        NotificacaoEmail entity = NotificacaoEmail.builder()
                .codigo(1L)
                .subprocesso(subprocesso)
                .unidadeDestinoSigla("SIGLA")
                .unidadeOrigemSigla("ADMIN")
                .tipoNotificacao(TipoNotificacao.PROCESSO_FINALIZADO)
                .destinatario("test@example.com")
                .assunto("Test Subject")
                .corpoHtml("<p>corpo</p>")
                .situacao(SituacaoNotificacao.ENVIADO)
                .tentativas(1)
                .dataHoraCriacao(LocalDateTime.now())
                .build();

        NotificacaoDto dto = mapper.paraNotificacaoDto(entity);

        assertThat(dto.processoFinalizado()).isTrue();
        assertThat(dto.notificacaoFinalizacaoProcesso()).isTrue();
    }
}

package sesel.sgc.notificacao;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import sesel.sgc.processo.Processo;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificacaoTest {
    @Test
    void testGettersAndSetters() {
        Notificacao notificacao = new Notificacao();
        NotificacaoTipo tipo = NotificacaoTipo.INICIO_PROCESSO;
        Unidade unidade = new Unidade();
        Processo processo = new Processo();
        String assunto = "Assunto";
        String conteudo = "Conteúdo da notificação";
        LocalDateTime dataHoraEnvio = LocalDateTime.now();
        UUID id = UUID.randomUUID();

        notificacao.setTipo(tipo);
        notificacao.setUnidade(unidade);
        notificacao.setProcesso(processo);
        notificacao.setAssunto(assunto);
        notificacao.setConteudo(conteudo);
        notificacao.setDataHoraEnvio(dataHoraEnvio);
        notificacao.setId(id);

        assertEquals(tipo, notificacao.getTipo());
        assertEquals(unidade, notificacao.getUnidade());
        assertEquals(processo, notificacao.getProcesso());
        assertEquals(assunto, notificacao.getAssunto());
        assertEquals(conteudo, notificacao.getConteudo());
        assertEquals(dataHoraEnvio, notificacao.getDataHoraEnvio());
        assertEquals(id, notificacao.getId());
    }

    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(Notificacao.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    @Test
    void construtorEGetters() {
        Notificacao notificacao = new Notificacao();
        assertNotNull(notificacao);
    }
}
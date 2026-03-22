package sgc.alerta.notificacao;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.mail.javamail.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.comum.config.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceCoverageTest {

    @Mock
    private NotificacaoRepo notificacaoRepo;
    @Mock
    private JavaMailSender enviadorEmail;
    @Mock
    private ConfigAplicacao config;
    @InjectMocks
    private EmailService notificacaoServico;

    @Test
    @DisplayName("processarEnvioEmail deve lancar RuntimeException se enviarEmail falhar")
    void deveLancarExcecaoAoFalharEnvio() throws MessagingException {
        ConfigAplicacao.Email emailConfig = new ConfigAplicacao.Email();
        emailConfig.setRemetente("noreply@test.com");
        emailConfig.setRemetenteNome("Remetente teste");
        emailConfig.setAssuntoPrefixo("[Teste]");
        when(config.getEmail()).thenReturn(emailConfig);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(enviadorEmail.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new org.springframework.mail.MailSendException("Erro envio")).when(enviadorEmail).send(any(MimeMessage.class));

        when(notificacaoRepo.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> notificacaoServico.enviarEmail("valido@test.com", "assunto", "corpo"))
                .isInstanceOf(RuntimeException.class);
    }
}

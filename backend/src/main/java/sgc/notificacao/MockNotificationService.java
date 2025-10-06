package sgc.notificacao;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Getter
public class MockNotificationService implements NotificationService {
    private final List<EmailEnviado> sent = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void enviarEmail(String to, String subject, String body) {
        sent.add(new EmailEnviado(to, subject, body, Instant.now()));
    }

    public record EmailEnviado(String to, String subject, String body, Instant sentAt) {
    }
}
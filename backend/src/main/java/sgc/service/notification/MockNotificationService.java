package sgc.service.notification;

import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Profile("test")
@Getter
public class MockNotificationService implements NotificationService {
    private final List<SentEmail> sent = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void sendEmail(String to, String subject, String body) {
        sent.add(new SentEmail(to, subject, body, Instant.now()));
    }

    public List<SentEmail> getSentMessages() {
        synchronized (sent) {
            return new ArrayList<>(sent);
        }
    }

    public void clear() {
        sent.clear();
    }

    public record SentEmail(String to, String subject, String body, Instant sentAt) {
    }
}
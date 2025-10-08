package sgc.notificacao;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Getter
public class NotificacaoMockService implements NotificacaoService {
    private final List<EmailEnviado> emailsEnviados = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void enviarEmail(String para, String assunto, String corpo) {
        emailsEnviados.add(new EmailEnviado(para, assunto, corpo, Instant.now()));
    }

    public record EmailEnviado(String para, String assunto, String corpo, Instant enviadoEm) {
    }
}
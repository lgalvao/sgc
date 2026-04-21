package sgc.comum.util;

import lombok.*;
import org.springframework.boot.context.properties.*;

@ConfigurationProperties(prefix = "sgc.monitoramento")
@Getter
@Setter
public class MonitoramentoProperties {
    public enum Modo {
        OFF,
        HTTP,
        LENTO,
        COMPLETO
    }

    private Modo modo = Modo.OFF;
    private long tempoMinimoJavaMs = 500;
    private long tempoHttpLentoMs = 100;
    private long tempoHttpMuitoLentoMs = 300;

    public boolean isMonitoramentoHttpAtivo() {
        return modo != Modo.OFF;
    }

    public boolean isMonitoramentoJavaLentoAtivo() {
        return modo == Modo.LENTO || modo == Modo.COMPLETO;
    }

    public boolean isMonitoramentoJavaCompletoAtivo() {
        return modo == Modo.COMPLETO;
    }
}

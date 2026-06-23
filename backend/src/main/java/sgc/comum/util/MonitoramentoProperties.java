package sgc.comum.util;

import lombok.*;
import org.springframework.boot.context.properties.*;

@ConfigurationProperties(prefix = "sgc.monitoramento")
@Getter
@Setter
public class MonitoramentoProperties {
    private Modo modo = Modo.NAO;
    private long tempoMinimoJavaMs = 500;
    private long tempoHttpLentoMs = 100;

    public boolean isMonitoramentoHttpAtivo() {
        return modo == Modo.SIM;
    }

    public boolean isMonitoramentoJavaLentoAtivo() {
        return modo == Modo.SIM;
    }

    public enum Modo {
        SIM,
        NAO
    }

}

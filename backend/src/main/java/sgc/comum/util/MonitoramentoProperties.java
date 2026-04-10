package sgc.comum.util;

import lombok.*;
import org.springframework.boot.context.properties.*;

@ConfigurationProperties(prefix = "sgc.monitoramento")
@Getter
@Setter
public class MonitoramentoProperties {
    private boolean ativo = false;
    private boolean traceCompleto = false;
    private long limiteAlertaMs = 500;
    private boolean permitirAtivacaoPorHeader = true;
    private double taxaAmostragem = 0.0;
}

package sgc.comum.util;

import org.springframework.stereotype.Component;

@Component
public class Sleeper {
    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}

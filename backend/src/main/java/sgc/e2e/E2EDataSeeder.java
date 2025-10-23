package sgc.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("e2e")
@RequiredArgsConstructor
public class E2EDataSeeder {

    private final E2ESeederService e2eSeederService;
}
